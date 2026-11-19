# Changelog

History of the InSilico Core library. Until version 1.3.21 it was kept in `src/main/resources/version/version.xml`,
which also carried the version number; the version is now taken from the pom at build time, so only the history
remains and it lives here.

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
