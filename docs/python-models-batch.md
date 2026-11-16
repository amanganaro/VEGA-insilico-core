# Batch execution of the Python models

## Why

Every model deriving from `iInsilicoModelPython` used to start **one Python process per molecule**. Each process
re-imports torch/sklearn/rdkit and reloads the model weights from disk, so the cost of a run grew with the number of
molecules and became the dominant cost on large sets. The CDDD descriptors were never the bottleneck:
`CdddDescriptors.calculateDescriptors()` has always been computed in a single process for all the molecules.

The batch mode applies the same idea to the predictions: all the molecules are predicted in one execution of the
script, the results are cached, and the per-molecule loop only reads from the cache.

## How it works

1. The runner calls `prepareBatch(molecules)` on every Python model whose `supportsBatchExecution()` is true, before
   the per-molecule loop.
2. `prepareBatch` builds one input file per chunk of molecules (`buildBatchInputFile`), runs the script once per
   chunk, reads every output row and caches one prediction per molecule, keyed by input SMILES.
3. The output rows are matched to the input molecules **by position**, and the match is verified against the
   `smiles` column that the scripts echo back. If the row count differs or a SMILES does not match, nothing is
   cached and the model falls back to the per-molecule execution.
4. During the loop, `calculatePythonModel` finds the prediction in the cache and returns it without starting a
   process. The models themselves were not modified: they keep calling `calculatePythonModel` and keep reading the
   same `Map<String,String>`.
5. The runner calls `clearBatch()` at the end of the run.

The input file is built differently for the two families of Python models, which is what keeps the API usable by
future models:

- **models using the CDDD descriptors** (all of them today): the per-molecule files written by `app-cddd.py` share
  the same header, so they are merged into a single CSV (`CdddDescriptors.writeCombinedDescriptorFile`). This is
  exactly the format the scripts already read, so **no Python script had to be changed**;
- **models computing their own descriptors in Python** (none today): they get the plain SMILES file written by
  `prepareInputData(List, String)`, the multi-row form of the existing `prepareInputData()`.

## Enabling a model

Override `supportsBatchExecution()` to return `true`, after checking that the script reads the whole input file and
writes one output row per input row, in the same order. If the script takes its arguments in a different form than
`--input`/`--output`, override `buildBatchCommandParams` as well.

| Script | Batch | Measured | Note |
|---|---|---|---|
| `app-apical-cardiotox.py` | **enabled** | 5.6x | calls `inference(..., singlemol=False)` and loops over all the SMILES for the AD |
| `app-cardio-tox-multitask.py` | **enabled** | 16.3x | explicitly handles `prediction.shape[0] == 1` against the N-row case |
| `app-<tag>-ontox.py` (×25) | **enabled** | 11.9x / 16.5x | `for index, row in test_df_renamed.iterrows()` over all the rows |
| `app-dili-bayer.py` | **enabled in 1.0.1** | 22.7x | index realigned before the merge; positional arguments, see below |
| `app-mitochondrial-dysfunction.py` | **enabled in 1.0.1** | 3.6x | `inference(..., singlemol=False)` and one row per molecule, see below |

Measured on the same 20 molecules, per-molecule run against batch run, with identical results in every cell. The
ONTOX figures are for the tags `ACE_ONTOX` and `AHR_ONTOX`; in real use the gain on that module is larger still,
because today every molecule costs 25 Python processes, one per model.

The figures are not comparable with each other: what the batch removes is the fixed cost of starting a process and
loading the weights, so the gain follows how heavy that cost is per molecule. `app-dili-bayer.py` loads five
networks, about 480 MB of weights, on every execution, which is why it gains the most (114,755 ms to 5,045 ms).
`app-mitochondrial-dysfunction.py` gains the least because most of its time is not the startup but the
applicability domain, which compares every molecule against the whole KE2 dataset and stays O(N × dataset) in
batch too — only inside a single process.

The checks live in `model.PythonBatchComparison`, published in the test-jar of the core: each module has a small
`main` that builds its own model and calls it. They cover the identity of the results, a single molecule, a
molecule the model cannot predict, a repeated molecule and a set crossing the chunk boundary. The alignment guard
belongs to the core mechanism rather than to a single model and is checked once, by `BatchGuardHarness` in the
ApicalCardioTox module.

One caveat specific to ONTOX: its output has no `smiles` column, so the guard can only check the number of rows
and not match them one by one. The script builds its results with `iterrows()`, so the order is preserved by
construction.

## The two scripts changed in 1.0.1

These two models ran one process per molecule longer than the others, because their scripts could not predict more
than one molecule per execution. The changes are described below as they were found and as they were applied, since
they are the only places where this work touched Python code: they are **for review by the authors of the scripts**.
Both were made in `VEGA-python-models` and shipped by rebuilding the archive under the new version number.

### `app-dili-bayer.py` — rows get lost or misaligned

`prepare_structures()` drops rows (`dropna` on `molecule` and on `canonical_smiles`), and `main()` then does:

```python
test_pred = test_df.merge(mean_pred, right_index=True, left_index=True)
```

`mean_pred` is built from the predictions and has a fresh `0..M-1` index, while `test_df` keeps its original index
with gaps where rows were dropped. With a single molecule this is invisible. With several molecules, as soon as one
is dropped, the merge loses rows and can pair a prediction with the wrong molecule.

*Change applied*: `test_df = test_df.reset_index(drop=True)` right after `prepare_structures`.

*Residual limitation, accepted*: a molecule that cannot be standardized still disappears from the output. The Java
guard notices the missing row and falls back to the per-molecule execution, so the results stay correct and only
the speed-up is lost on that set.

*Not changing*: this script reads its arguments as `sys.argv[1]`/`sys.argv[2]` while every other script uses
`--input`/`--output`. Making it uniform would also require changing `ismDiliBayer.java`, which passes them
positionally today, in the very same release. The script keeps its arguments and the Java side overrides
`buildBatchCommandParams` instead, so this fix stays a one-line change to the Python code.

### `app-mitochondrial-dysfunction.py` — inference runs in single-molecule mode

Two separate problems, both inside `inference`.

**The call site** does not ask for the multi-molecule mode:

```python
results = inference(models=models, smiles=target_CDDD['smiles'].tolist(), target_dict=target_dict)
```

`singlemol` defaults to `True` and the function builds `pd.DataFrame(results_final, index=[smiles])`. That is
correct for one molecule and wrong for several.

**The multi-molecule branch itself is wrong too**, so passing `singlemol=False` is not enough:

```python
else:
    return pd.DataFrame(pd.DataFrame([results_final]))
```

`results_final` is `{model_name: array of N predictions}`. Wrapping it in a list gives **one single row** whose
cells contain the arrays, instead of N rows. `app-apical-cardiotox.py` transposes at the same point
(`pd.DataFrame([pred]).T`), and that transposition is what is missing here.

*Change applied*: `singlemol=False` at the call site, and the `else` branch replaced with

```python
return pd.DataFrame(results_final)
```

which yields N rows and one column per model — the same column names the single-molecule branch produces, so the
output for a single molecule is unchanged. Nothing else needs to change: the applicability domain part already
loops over all the SMILES and `results['smiles']` is filled afterwards, so the Java guard works in full.

## Shipping a changed script

The Python sources live in their own repository, `VEGA-python-models`, one directory per model named exactly like
the folder inside the shipped zip. Changes are made there; when they are final the directory is zipped and the
archive replaces the one in `src/main/resources/data/` of the model module. Every `.py` of all 29 shipped zips is
currently byte-identical to its source in that repository, so the repository is the authoritative copy.

**The version must be bumped** whenever a script changes. `setSupportFiles()` extracts the archive only when the
destination folder does not exist yet, so an installation that already holds
`<user data dir>/vega-models/<name>_1_0_0` would keep running the old script forever. A bump touches three things
per model: the zip file name, the folder name inside it, and the string passed to the `super(...)` constructor of
the model class. The same applies to a development machine: delete the extracted folder, or the new script is
never used.

For a model whose zip holds large weights, rebuild the archive once rather than iterating on it: agree on the
script and try it outside the archive first. `dili-bayer_*.zip` is 428 MB and tracked in Git LFS, so every version
is another 430 MB in the LFS store.

A note on the compiled bytecode, for whoever repackages: a `.pyc` is the compiled form of an imported module,
written by Python under `__pycache__/` with the interpreter version in its name. It is a cache, never a
distribution format — Python rebuilds it when missing, and the entry script itself never gets one. The copies
shipped so far could not be used anyway: `extractFilesFromZip` does not restore the archived timestamps, so the
extracted sources never match the modification time a `.pyc` records, and every one of them is discarded on first
import. Nothing is lost by leaving them out.

**Rebuild the archive from the source directory**, and do not carry over the compiled bytecode. Two archives
contain `__pycache__/` directories that do not exist in `VEGA-python-models`: `dili-bayer_*.zip` (five files) and
`cardio-tox-multitask_*.zip` (four). The other 27 archives match the repository file for file — not because they
were packaged differently, but because their models consist of a single `app-*.py`, which is the entry script and
therefore never produces a cache. In other words, every archive looks like a snapshot of the folder the model was
tried out in; only the two models that import a local module could accumulate anything.

Two details confirm it: the caches are tagged `cpython-37`, `-39`, `-310` and `-312` while the environment pins
`python=3.10`, so several interpreters ran in those folders over time; and
`cardio-tox-multitask_1_0_0/model/__pycache__/model.cpython-312.pyc` is the cache of a `model/model.py` that
exists neither in the archive nor in the repository, so it outlived its own source.

The caches are dead weight in any case — see the note below — so they are dropped on the next repackaging.

## Known limitations

- `GeneralUtilities.executeCommandLine` has no timeout and no `destroyForcibly`: a Python process that hangs blocks
  the JVM. In batch mode a hang blocks the whole chunk rather than a single molecule.
- The cache holds one row per molecule per model. `setBatchChunkSize` bounds the memory used while a chunk runs,
  but the cache itself grows with the number of molecules; this matters most for modules holding many models.
