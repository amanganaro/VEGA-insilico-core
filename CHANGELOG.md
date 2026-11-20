# Changelog

History of the InSilico Core library. Until version 1.3.21 it was kept in `src/main/resources/version/version.xml`,
which also carried the version number; the version is now taken from the pom at build time, so only the history
remains and it lives here.

## 1.3.23

- PaDEL descriptors run out of process: PadelAdapter 0.4 talks to PadelService, so PadelFullLib (with its old CDK
  and the unrelocated Guava, Jena, ICU4J and RapidMiner it bundles) is no longer on the classpath of the core and the
  models. Models that call the old adapter API (`SetSMILES`/`CalculateDescriptors`) must move to
  `newRequest(...).add(...).calculate()`.
- `SludgeDescriptors` calculates its 13 descriptors in a single request per molecule; values unchanged.
- `docs/models-dependency-analysis.md`: survey of risky dependencies across the model repositories.

## 1.3.22

- The version reported by `InsilicoInfo` is taken from the pom at build time instead of a hand-maintained XML
  resource, which had already drifted (it declared 1.3.20 while the library was 1.3.21).
- `InsilicoInfo.getMajor()`, `getMinor()` and `getRevision()` removed: `getVersion()` returns the whole version
  string as the pom declares it.
- Version history moved from `version/version.xml` to this file.

## 1.3.21

- Model regression test support: the shared base classes are published in the test-jar.
- Batch execution for Python models: one process per set instead of one per molecule, with the returned rows
  matched by SMILES rather than by position.
- Golden tests for the descriptor blocks and the similarity matrix.

## 1.3.20

- Removed support for PMML models (moved to the Core-Aux artifact).

## 1.2.7

- Added alerts for Reprotox-IRFMN.

## 1.2.6

- Modified sulfonic fragment for Meylan LogP (also modifies Meylan BCF).
- Modified -Cl aliphatic attack for Meylan LogP (also modifies Meylan BCF).
- Small fix in nRSR and nRSSR functional groups (added exception for C-S-C=S).

## 1.2.5

- Modified AD output, now AD assessment provided as a separate column in output.
- Fixed PNG for ER/CERAPP alert set.
- Added Barysz matrix.
- Implemented BIC descriptors (information content).
- Implemented IVDE.
- Implemented MAXDN, MAXDP.
- Corrected nRNNOx group in Functional Groups count.
- Added catch for error with very long SMILES (>1000 atoms) from the CDK canonical labeler.
- Added wrapper for ANN models from PMML files.
- Added fix for aromaticity detection for P with valence 5 in rings.

## 1.2.4

- Extension of Information Content descriptors with the CICk series.
- Added hepatotoxicity structural alerts.
- Added descriptors/fragments for kM.

## 1.2.3

- Correction for the Benigni-Bossa ruleset.

## 1.2.2

- Correction in similarity calculation for the Bray-Curtis index (non binary distance index) to avoid a global
  similarity of zero for strongly different compounds.
- Correction for the Benigni-Bossa SA1 image.
- Correction in the description of 2 structural alerts for sediment persistence.
