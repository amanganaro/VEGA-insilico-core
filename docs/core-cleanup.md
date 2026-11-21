# Pulizia del core

Elenco delle pulizie da fare sul core, decise una per una con Alberto. Si raccolgono qui e si eseguono insieme
quando il progetto viene lanciato, non appena vengono trovate. Per ogni voce: perché, cosa fare, come verificarla.

## Versione

Il progetto **non alza la versione**: si parte da Core 1.3.24 (2026-11-21) e, a lavoro finito, si ripubblica
**1.3.24** su Nexus sopra l'artefatto esistente. Così VegaModels-All 1.3.0 e i moduli, che fissano Core 1.3.24, non
vanno ri-bumpati. È ammesso a tre condizioni:

1. **Solo voci che non cambiano gli output.** Una voce che può cambiare i risultati dei modelli va in un rilascio
   minor a parte, mai sopra 1.3.24.
2. **Finestra breve.** Maven non riscarica mai una release già in cache: chi scarica 1.3.24 prima della
   ripubblicazione tiene la copia vecchia per sempre, senza avvisi. Se il backlog resta aperto per settimane,
   si pubblica 1.3.25 invece di sovrascrivere.
3. **Nexus deve accettare il redeploy** (policy del repository `maven-releases`). Se lo rifiuta, si passa a 1.3.25
   senza forzare nulla.

Alla ripubblicazione: estendere la sezione `## 1.3.24` del CHANGELOG, spostare il tag annotato `1.3.24` sul nuovo
commit (push forzato del solo tag, previa conferma), fast-forward di `release`, e avvisare che chi ha già
`~/.m2/repository/InSilico/Core/1.3.24` deve cancellarlo.

## Voci

| # | Voce | Effetto sugli output | Ammessa in 1.3.24 | Stato |
|---|---|---|---|---|
| 1 | Guava: tenerla, documentarla, 33.6.0 → 33.8.0-jre | nessuno | sì | fatto |
| 2 | Jackson 2.22.0 → 2.22.3 | nessuno | sì | fatto |
| 3 | Escludere da CDK jena, junit e xalan | nessuno | sì | fatto |

Verifica del 2026-11-21: golden test del core 6/6; `scripts/run-model-regression.sh --dataset core` verde su tutti
i 121 moduli dei cinque repository (fate 9, ecotox 31, other 22, properties 11, human 48); depiction PNG del core e
SVG/PDF/EPS di CDK funzionanti senza jena/junit/xalan. Ripubblicato come 1.3.24.

### 1. Guava: tenerla, documentarla, portarla a 33.8.0-jre

**Cosa si era pensato all'inizio, ed era sbagliato.** La voce nasceva come "togliere Guava": il core non la importa
da nessuna parte e la riga sembrava un residuo di PMML (dichiarata nel 2020, commit `492041f`, per imporre una
versione più recente di quella di `pmml-*` 1.5.6; PMML tolto nel 2026, commit `89cb230`). Ma
`mvn dependency:tree` senza `-Dverbose` nasconde le versioni che perdono un conflitto: con `-Dverbose` si vede che
**23 moduli di `cdk-bundle` 2.3 dichiarano `guava 17.0`** (2014). La dichiarazione diretta del core è ciò che porta
CDK sulla Guava corrente; togliendola, CDK tornerebbe alla 17.0, con le sue CVE e su un ambiente mai testato.

**Verificato il 2026-11-21.** 15 jar di CDK 2.3 usano Guava, in tutto 33 classi (19 di `collect`, 6 di `base`,
4 di `cache`, e qualcuna di `primitives`, `io`, `escape`, `xml`): tutte presenti sia nella 33.6.0 sia nella
33.8.0 (controllo a livello di classe con `jdeps`, non di singolo metodo). CDK gira su Guava forzata dal 2020.
Il core e i consumatori elencati alla voce 3 non la importano.

**Cosa fare.** Tenere la dipendenza, con un commento nel `pom.xml` che spiega perché c'è, e portarla all'ultima
versione, 33.8.0-jre (chiude la CVE alta del report, punto 2). Il suffisso `-jre` indica la variante per Java
standard (l'altra è `-android`).

**Verifica.** `mvn dependency:tree -Dverbose -Dincludes=com.google.guava`: 33.8.0-jre, la 17.0 di CDK omessa per
conflitto; golden test e regression test dei modelli.

### 2. Jackson 2.22.0 → 2.22.3

**Perché.** Report, punto 1: `jackson-databind` 2.22.0 (che porta `jackson-core` 2.22.0) ha 5 CVE alte e 5 medie,
chiuse in 2.22.3. Nel core nessuna è raggiungibile, ma il core passa questa versione a tutte le applicazioni che lo
usano, comprese quelle che deserializzano JSON ricevuto dall'esterno.

**Uso nel core.** Solo l'annotazione `@JsonAutoDetect`, su 16 classi che finiscono serializzate in JSON
(`ad/item/*`, `molecule/InsilicoMolecule*`, `model/InsilicoModelOutput`, `alerts/AlertList`,
`constant/MessagesAD`). Patch senza cambi di API.

**Cosa fare.** `jackson-databind` a 2.22.3 nel `pom.xml`; controllare che `jackson-core` e
`jackson-annotations` si allineino (altrimenti dichiararli o usare il `jackson-bom`).

**Verifica.** `mvn dependency:tree -Dincludes=com.fasterxml.jackson.core` tutto a 2.22.3 (annotations 2.22);
build e test.

### 3. Escludere da CDK jena, junit e xalan

**Perché.** Report, punto 4: `cdk-bundle` 2.3 porta librerie che il core non usa, con CVE note:
- `org.apache.jena:jena-core` 2.7.4 (+ `jena-iri`), da `cdk-iordf` (lettura/scrittura RDF);
- `junit:junit` 4.10 (+ `hamcrest-core`), da `freehep-graphics2d` (sotto `cdk-depict`): una libreria di test
  finita nel runtime;
- `xalan:xalan` 2.7.0, da `xom`, che lo usa solo per XSLT.
Insieme chiudono 3 CVE alte e 1 media, e tolgono dal classpath dei consumatori codice che nessuno chiama.

**Verificato il 2026-11-21:** nessun import di `org.apache.jena`, `com.hp.hpl.jena`, `org.apache.xalan`,
`junit.framework` o `org.openscience.cdk.io.rdf` nel `src/main` del core né dei consumatori: i cinque repository
VEGA-models-*, VEGA-models-all, vega-gui, VEGA-padeladapter, VEGA-insilico-core-aux, VEGA-coral, VEGA-NRMEA,
VEGA-toxtree. Con le esclusioni attive, la depiction del core (PNG) e i formati vettoriali di CDK (SVG, PDF, EPS,
che passano da freehep) funzionano.

**Cosa fare.** `<exclusions>` sulla dipendenza `cdk-bundle` per `org.apache.jena:*`, `junit:junit`,
`org.hamcrest:hamcrest-core` e `xalan:xalan`.

**Rischio e verifica.** Il rischio non è un risultato diverso ma un `NoClassDefFoundError` a runtime, se una
classe di CDK o di freehep li tocca. Verifica: golden test del core e `scripts/run-model-regression.sh --dataset
core` sui cinque repository modelli con il core installato in locale; in più una depiction SVG di prova (passa da
freehep).

## Candidati non inclusi

Valutati il 2026-11-21 e lasciati fuori da questo progetto:
- **log4j 1.2.16** (report punto 5, da `cmlxom`): bridge `log4j-1.2-api` o eliminazione di `InsilicoLogger`.
- **xerces 2.8.0** (report punto 7): escluderlo cambia il parser XML di tutto il core; per la regola dei bump va
  in un rilascio minor a parte, se si fa.
- **CDK 2.3 → 2.14** (report punto 6): sospesa, cambia i risultati.
- Major di `openpdf` 3, `poi-ooxml` 6, `jaxb` → jakarta: cambi di API, nessuna CVE chiusa.
- **Togliere `poi-ooxml`** (annotato il 2026-11-21, non deciso). POI non serve per Excel: lo usa solo
  `model/qmrf/QMRFMSDoc`, che legge un QMRF in Word (`.docx`) e lo converte nell'XML QMRF. Nessuno la chiama:
  l'unico riferimento è una riga commentata in `insilico/core/main.java:108` (più il suo `import`), e nessun
  consumatore usa POI o `QMRFMSDoc`. POI porta 11 jar, circa 16 MB (`poi`, `poi-ooxml-lite`, `xmlbeans`,
  `commons-compress`, `commons-codec`, `commons-io`, `commons-collections4`, `curvesapi`, `SparseBitSet`,
  `log4j-api`). **Trappola:** due di questi sono usati davvero e arrivano solo tramite POI: `commons-io`
  (`FileUtils` in `InsilicoModelPython`, `CdddDescriptors`, `python/Communication`, `tools/utils/FileUtilities`) e
  `log4j-api` (`devops/ModelsDeployment` nel core e molti moduli modello, per esempio gli Algae* in ecotox, che la
  ricevono dal core). Se si fa: eliminare `QMRFMSDoc` e la riga commentata con l'import, togliere `poi-ooxml`,
  dichiarare direttamente `commons-io` e `log4j-api` alle versioni attuali. Nessun effetto sugli output.
