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
| `app-dili-bayer.py` | **not possible** | — | see below |
| `app-mitochondrial-dysfunction.py` | **not possible** | — | see below |

Measured on 20 molecules, per-molecule run against batch run, with identical results in every cell. The ONTOX
figures are for the tags `ACE_ONTOX` and `AHR_ONTOX`; in real use the gain on that module is larger still, because
today every molecule costs 25 Python processes, one per model.

The checks live in `model.PythonBatchComparison`, published in the test-jar of the core: each module has a small
`main` that builds its own model and calls it. They cover the identity of the results, a single molecule, a
molecule the model cannot predict, a repeated molecule and a set crossing the chunk boundary. The alignment guard
belongs to the core mechanism rather than to a single model and is checked once, by `BatchGuardHarness` in the
ApicalCardioTox module.

One caveat specific to ONTOX: its output has no `smiles` column, so the guard can only check the number of rows
and not match them one by one. The script builds its results with `iterrows()`, so the order is preserved by
construction.

## Scripts that cannot run in batch yet

These two models keep running one process per molecule. The changes below have **not** been applied: the scripts are
owned by their authors and must be agreed with them first.

### `app-dili-bayer.py` — rows get lost or misaligned

`prepare_structures()` drops rows (`dropna` on `molecule` and on `canonical_smiles`), and `main()` then does:

```python
test_pred = test_df.merge(mean_pred, right_index=True, left_index=True)
```

`mean_pred` is built from the predictions and has a fresh `0..M-1` index, while `test_df` keeps its original index
with gaps where rows were dropped. With a single molecule this is invisible. With several molecules, as soon as one
is dropped, the merge loses rows and can pair a prediction with the wrong molecule.

*Proposed change*: realign the indices before the merge, either `test_df = test_df.reset_index(drop=True)` right
after `prepare_structures`, or `mean_pred.index = test_df.index`.

*Secondary*: this script reads its arguments as `sys.argv[1]`/`sys.argv[2]` while every other script uses
`--input`/`--output`. Making it uniform would remove the need for a model-specific override on the Java side.

### `app-mitochondrial-dysfunction.py` — inference runs in single-molecule mode

The script calls:

```python
results = inference(models=models, smiles=target_CDDD['smiles'].tolist(), target_dict=target_dict)
```

without `singlemol`, which defaults to `True` and builds `pd.DataFrame(results_final, index=[smiles])`. That is
correct for one molecule and wrong for several.

*Proposed change*: pass `singlemol=False`, exactly as `app-apical-cardiotox.py` already does. The applicability
domain part of the script already loops over all the SMILES, so nothing else needs to change.

## Known limitations

- `GeneralUtilities.executeCommandLine` has no timeout and no `destroyForcibly`: a Python process that hangs blocks
  the JVM. In batch mode a hang blocks the whole chunk rather than a single molecule.
- The cache holds one row per molecule per model. `setBatchChunkSize` bounds the memory used while a chunk runs,
  but the cache itself grows with the number of molecules; this matters most for modules holding many models.
