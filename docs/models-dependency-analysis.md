# Analisi delle dipendenze dei modelli VEGA (VEGA-models-*)

Data: notte fra il 19 e il 20/11/2026. Analisi in sola lettura, nessun file modificato.

**Metodo:**
- `dependency:tree -Dverbose` sui 128 moduli e sul core;
- classpath risolti di singoli moduli e di VegaModels-All;
- ispezione dei jar nel `.m2`;
- richieste HEAD a Maven Central, al Nexus ProtoQSAR (maven-public) e al Nexus IdeaConsult;
- confronto fra i pom nel `.m2` locale e quelli su Nexus.

**Stato di partenza:** la migrazione PaDEL è conclusa ma non committata: Core 1.3.23 e PadelAdapter 0.4, installati solo in locale.

Ogni punto ha: problema, evidenze, soluzione proposta, verifica. Sono ordinati per gravità e si possono eseguire uno alla volta.

| # | Gravità | Titolo | Moduli |
|---|---|---|---|
| 1 | ALTA | Import Jena inutilizzato in TPOOberon | TPOOberon |
| 2 | ALTA | `.m2` locale con pom diversi da Nexus a parità di versione | VegaModels-All (quasi tutti i modelli) |
| 3 | ALTA | PadelFullLib porta Guava/Jena/ICU/RapidMiner non rilocati | tutti, con Core ≤ 1.3.22 |
| 4 | ALTA | `toxtree-cramer` da repository di terzi con ~40 jar e classi duplicate | CramerToxTree |
| 5 | ALTA | ToxTreeWrapper "shaded" lascia beam/log4j/OPSIN non rilocati | CramerToxTree, VerhaarToxtree |
| 6 | MEDIA | Dipendenze fra modelli a versioni vecchie | BCFArnotGobas, BCFMeylan, KmArnot, SkinPermeationPotts/Tenberge |
| 7 | MEDIA | log4j 2.18 vince sul 2.24.3 del core in VegaModels-All | GUI / All |
| 8 | MEDIA | Ambiente Python da 1,3 GB solo Windows | 5 modelli Python + CDDD |
| 9 | MEDIA | weka 3.5.8 con un'unica sorgente | DevtoxCaesar |
| 10 | MEDIA | Guava 30 vince sul 33.6 del core | 80 moduli con Core-Aux |
| 11 | BASSA | slf4j 1.7/1.6 imposti da Coral | 18 moduli Coral |
| 12 | BASSA | xercesImpl 2.8.0 / xalan 2.7.0 | tutti |
| 13 | BASSA | Import accidentali `javassist.runtime.Desc` | EyeIrritation, MeltingPoint, PPARg_up |
| 14 | BASSA | `LATEST` nel plugin asciidoctor | pom padre ecotox, fate, other, properties |
| 15 | BASSA | Lombok in scope compile | FishLC50, FishNOEC, MoaEpa, ZebrafishCoral, FatheadEPA |
| 16 | BASSA | jpmml 1.5.6 e Core-Aux legato a Core 1.3.21 | 80 moduli |

---

## 1. ALTA: import Jena inutilizzato in TPOOberon
**STATO: FATTO il 20/11/2026.** Import rimosso e regressione verde su Core 1.3.23. Non ancora committato; versione del
modulo da alzare al momento del rilascio.

**Problema.** `VEGA-models-other/VegaModels-TPOOberon/src/main/java/insilico/tpo_oberon/utils/ModelsDeployment.java:3`
contiene `import com.hp.hpl.jena.reasoner.rulesys.builtins.GE;`, che non viene mai usato. Oggi compila solo perché
Jena è dentro PadelFullLib. Con Core 1.3.23, che non ha più PadelFullLib, il modulo non compila.

**Evidenza.** Una ricerca sugli import mostra che è l'unico import, in tutti i repository, soddisfatto solo dai package
non rilocati di PadelFullLib.

**Soluzione.**
- Eliminare la riga e includerla nei commit della migrazione PaDEL, prima di rilasciare Core 1.3.23.
- Alzare la versione di TPOOberon: 1.0.2 → 1.0.3.

**Verifica.** `mvn -o test -Pregression -pl VegaModels-TPOOberon -Dvega.core.version=1.3.23` deve essere verde.

## 2. ALTA: `.m2` locale con pom diversi da Nexus a parità di versione
**Problema.** Su questo PC circa 100 artefatti di modelli hanno nel `.m2` un pom diverso da quello pubblicato su Nexus
con la stessa versione.

Le copie locali vengono dal vecchio repository tech-amcc.it (http, non più raggiungibile) o da vecchi `mvn install`:
- hanno come padre `InSilico:VegaModels:0.1`;
- hanno dipendenze che **non esistono su nessun repository raggiungibile**: `InSilico:ModelsUtils:1.0`,
  `InSilico:ProtoQSAR:0.3`, `InSilico:CPANNAtnic:1.0`, `InSilico:VegaModels:0.1`;
- hanno anche jackson-databind 2.13.3, beam `LATEST` e `Insilico:PadelFullLib` (groupId con la maiuscola sbagliata,
  quindi una seconda copia di PadelFullLib sul classpath).

Di conseguenza VegaModels-All e la GUI costruiti su questo PC hanno un classpath diverso da una build pulita, su
un'altra macchina o in CI.

**Evidenza.**
- Su VegaModels-All sono diversi 102 pom e identici 25.
- Ancora su VegaModels-All, i pom locali hanno come padre VegaModels 0.1, quelli su Nexus VegaModels-Human/Ecotox/Other/Fate 1.0.

Esempi:
- TPOOberon 1.0.2: in locale porta ModelsUtils e log4j 2.18, su Nexus no.
- TotalHLQsarins 1.0.1: in locale ha una seconda `Insilico:PadelFullLib`.

Nota sui repository dei modelli. Quando si compilano i singoli moduli dai sorgenti, il problema riguarda solo le
dipendenze fra modelli: LogPMeylan, LogPALogP, AlgaeCombaseClass, KmArnot e i Mutagenicity usati da Consensus.

**Soluzione.**
1. Fare una build di prova di VegaModels-All (e della GUI) con un repository locale vuoto, per esempio
   `mvn -Dmaven.repo.local=<scratchpad>/m2-clean dependency:tree`, che serve la rete. Così si ottiene il classpath vero
   e l'elenco degli artefatti che mancano davvero su Nexus.
2. Se la build pulita è a posto, rinominare la cartella `~/.m2/repository/InSilico` (non cancellarla subito) e
   lasciare che Maven riscarichi tutto da Nexus. Prima di farlo, controllare che gli artefatti installati solo in
   locale e ancora necessari (Core 1.3.23, PadelAdapter 0.4, PadelService 1.0.0) siano stati pubblicati.
3. Regola d'ora in poi: una versione pubblicata non si ripubblica con un contenuto diverso; ogni modifica del pom
   richiede una nuova versione.

**Verifica.** Dopo il punto 2, `dependency:tree` di VegaModels-All non deve più contenere ModelsUtils, ProtoQSAR,
CPANNAtnic, `Insilico:` né VegaModels 0.1.

## 3. ALTA: PadelFullLib contiene librerie non rilocate
**Problema.** PadelFullLib 1.1 (27 MB) riloca solo CDK (`cdk1`). Lascia con i package originali:
- Guava di circa il 2014 (versione ~17, 1.686 classi);
- Jena (1.439 classi);
- ICU4J;
- RapidMiner (4.271 classi);
- log4j 1;
- jaxen, xom, xmlcml, jgrapht, vecmath, signature, una parte di ambit2.smarts.

In VegaModels-All PadelFullLib viene **prima** di guava-30 sul classpath, quindi tutta VEGA, CDK 2.3 compreso, usa le
classi Guava del 2014.

**Soluzione.** È già risolto dalla migrazione al servizio PaDEL: Core 1.3.23 più i 7 moduli convertiti. Resta da:
- completare commit e pubblicazione (core, adapter, service, modelli);
- portare la GUI e VegaModels-All a Core ≥ 1.3.23 e alle nuove versioni dei modelli PaDEL;
- dopo il punto 2, verificare che nell'aggregato non compaia più nessuna PadelFullLib, né `InSilico` né `Insilico`.

**Verifica.** Nel classpath di VegaModels-All: niente PadelFullLib, guava una sola volta, nessuna classe `com.hp.hpl`.

## 4. ALTA: CramerToxTree usa `toxtree-cramer` da un repository di terzi
**Problema.** CramerToxTree dichiara `toxtree:toxtree-cramer:3.1.0`, che si trova solo sul Nexus di IdeaConsult (la
URL è dichiarata nel pom del modulo). Tira dentro circa 40 jar:
- ambit2 3.2.0: core, db, smarts, rendering e altri;
- mysql-connector-java 5.1.36 (con CVE), c3p0, commons-pool;
- poi-scratchpad 3.13, incompatibile con il poi 5.5.1 del core;
- jfreechart, prefuse, saxon-dom 8.7, sjsxp, woodstox;
- 20 moduli CDK 1.5.13, forzati a 2.3 dalla risoluzione Maven.

Inoltre:
- `joelib:joelib:1.0` e `org.openscience.cdk:cdk-jchempaint:26` esistono **solo** nel repository "thirdparty" di
  IdeaConsult. Si scaricano solo perché i pom di ambit dichiarano quel repository.
- 37 classi `toxTree.tree.cramer.*` sono duplicate fra toxtree-cramer e ToxTreeWrapper. Nella versione di
  toxtree-cramer puntano al CDK non rilocato (`org/openscience/cdk/...`), in quella del wrapper al CDK rilocato
  (`shadedTTv31/...`).
- Il modello funziona solo perché ToxTreeWrapper viene prima di toxtree-cramer sul classpath, sia nel modulo sia in
  VegaModels-All (posizioni 197 e 198).

**Soluzione.**
- Provare a togliere `toxtree-cramer` dal pom: `ismCramerToxtree` usa solo `toxTree.tree.cramer.CramerRules`, che è
  già nel wrapper e collegata al CDK rilocato, come per VerhaarToxtree.
- Togliere anche la dichiarazione del repository IdeaConsult dal pom del modulo.
- Se manca qualche classe (toxtree-cramer ne ha 41, il wrapper 39), copiarla nel wrapper rilocata oppure ripubblicare
  solo quel jar sul Nexus ProtoQSAR.

**Verifica.**
- Test di regressione di CramerToxTree verde (dataset core: non c'è training set).
- `dependency:tree` senza ambit, joelib, mysql né CDK 1.5.13.

## 5. ALTA: ToxTreeWrapper lascia librerie non rilocate
**Problema.** ToxTreeWrapper 1.0 (72 MB) riloca quasi tutto sotto `shadedTTv31`, ma lascia con i package originali:
- **beam 0.9.1** (`uk/ac/ebi`, 104 classi);
- log4j 1.x (314 classi);
- OPSIN (`uk/ac/cam`);
- prefuse, dk.brics.automaton;
- le classi toxtree (`toxtree/`, `toxTree/`).

beam 0.9.1 è in conflitto con il beam 1.3.x che serve a CDK 2.3. Oggi funziona solo perché `beam-core` è dichiarato
prima del wrapper nei pom: è il motivo dei pin a 1.3.3 e 1.3.10, e del NoSuchMethodError già incontrato. Dipende
dall'ordine del classpath, quindi è fragile, soprattutto nell'aggregato della GUI.

**Soluzione.** Ripubblicare ToxTreeWrapper 1.1, rilocando anche `uk.ac.ebi.beam`, `uk.ac.cam`, `org.apache.log4j`,
`prefuse` e `dk.brics` sotto `shadedTTv31` (aggiungere le relocation al maven-shade-plugin del progetto wrapper).

Poi, in VerhaarToxtree e CramerToxTree:
- passare a ToxTreeWrapper 1.1;
- togliere i pin espliciti di beam-core: basta quello che porta CDK.

**Verifica.** Test di regressione di Verhaar e Cramer verdi. Nel jar del wrapper non devono restare classi
`uk/ac/ebi` fuori da `shadedTTv31` (`unzip -l`).

## 6. MEDIA: dipendenze fra modelli a versioni vecchie
**Problema.**
- BCFArnotGobas, BCFMeylan e KmArnot dipendono da `VegaModels-LogPMeylan:1.1.5`, mentre VegaModels-All usa 1.1.6.
- SkinPermeationPotts e SkinPermeationTenberge dipendono da `VegaModels-LogPALogP:1.0.1`, mentre All usa 1.0.2.

Quindi i modelli vengono testati con una versione e distribuiti con un'altra, quella scelta da Maven nell'aggregato.
In locale le versioni vecchie portano anche ModelsUtils e log4j 2.18 (vedi punto 2).

**Soluzione.** Alzare le dipendenze a LogPMeylan 1.1.6 e LogPALogP 1.0.2. Meglio ancora, gestire le versioni dei
modelli usati da altri modelli in un unico punto: una property nel pom padre del repository.

**Verifica.** Test di regressione dei 5 moduli verdi. `dependency:tree` coerente con VegaModels-All.

## 7. MEDIA: log4j 2.18 al posto del 2.24.3 del core in VegaModels-All
**Problema.** Nell'aggregato, `log4j-api` e `log4j-core` 2.18.0 (portati dalle versioni locali vecchie di TPOOberon e
dei modelli LogP) vincono sul log4j-api 2.24.3 del core. La GUI gira quindi sull'API 2.18 con un core compilato per
la 2.24.

**Soluzione.**
- Si risolve in gran parte con i punti 2 e 6.
- In più, nella GUI e in VegaModels-All conviene fissare log4j-api e log4j-core alla versione del core in
  `dependencyManagement`, oppure con un BOM `log4j-bom`.

**Verifica.** Classpath di VegaModels-All con un solo log4j-api, alla versione 2.24.3.

## 8. MEDIA: ambiente Python da 1,3 GB, solo Windows
**Problema.** `InSilico:VegaPythonEnv-Global:1.0.0` è un jar da 1,3 GB con dentro un ambiente conda-pack per Windows
(`python.exe`, dll, pyd; TensorFlow 2.10, torch 2.5.1, protobuf 3.19, xgboost, scikit-learn 1.6.1). Il core ha
`python.exe` scritto nel codice (`insilico/core/python/Communication.java`).

I modelli Python (ApicalCardioTox, CardioToxMultitask, DiliBayer, MitochondrialDysfunction, ONTOX-assay) e CDDD
quindi:
- girano solo su Windows;
- non sono utilizzabili nel futuro VEGA REST in un container Linux;
- pesano molto in download e deploy.

**Soluzione** (da decidere, è un progetto a sé):
1. **Breve termine.** Rendere il percorso dell'interprete dipendente dal sistema operativo (`bin/python` su
   Linux/macOS), come già fatto per le cartelle utente.
2. **Ambienti per piattaforma.** Pubblicare un ambiente per ogni piattaforma con un classifier Maven (`windows-x86_64`,
   `linux-x86_64`), scelto tramite un profilo attivato dal sistema operativo.
3. **Per i container.** Un'immagine Docker con l'ambiente già installato, e il jar che non lo incorpora più.

**Verifica.** Test di regressione dei modelli Python su Windows ancora verdi; prova su Linux (WSL) con l'ambiente
Linux.

## 9. MEDIA: weka 3.5.8 con un'unica sorgente
**Problema.** DevtoxCaesar usa `weka:weka:3.5.8` (2008), che non è su Maven Central. Ora è stato ripubblicato sul
Nexus ProtoQSAR, che è diventato la sua unica sorgente.

**Soluzione.**
- Documentare nel pom del modulo che l'artefatto è ospitato solo sul Nexus ProtoQSAR (c'è già un commento simile).
- Tenere un backup del jar.
- A lungo termine, valutare se il modello serializzato si può caricare con `nz.ac.waikato.cms.weka:weka-stable`
  (3.8.x, su Central). Non è garantito: la serializzazione Java di weka cambia fra versioni.

**Verifica.** Una build con `.m2` pulito (punto 2) che scarica weka da Nexus.

## 10. MEDIA: Guava 30 vince sul 33.6 del core
**Problema.** Negli 80 moduli che usano Core-Aux viene risolta guava 30.0-jre (portata da pmml-evaluator, a profondità
3) invece della 33.6.0-jre dichiarata dal core (profondità 2). Dovrebbe vincere la più vicina. È una risoluzione
anomala, probabilmente dovuta a Maven 3.6.3 (del 2019) combinato con le exclusion su Core dentro Core-Aux.

Impatto oggi basso: il core non importa Guava, la usano CDK e jpmml. Però il classpath potrebbe cambiare usando
un'altra versione di Maven.

**Soluzione.**
- Fissare guava in `dependencyManagement` nei pom padre dei repository dei modelli (o un BOM interno).
- Aggiornare Maven sulla macchina di build a 3.9.x.

**Verifica.** `dependency:build-classpath` di un modulo con Core-Aux mostra guava 33.6.0-jre.

## 11. BASSA: slf4j vecchio imposto da Coral
**Problema.** Coral 2.2 dichiara `slf4j-api:1.7.30` e `slf4j-simple:1.6.1` (2010), che vincono in 18 moduli Coral. Nei
moduli human il binding risulta slf4j-simple 2.0.17 con api 1.7.30: è una coppia incompatibile, il binding non viene
trovato e i log slf4j vanno persi (NOP).

**Soluzione.** In Coral (prossima versione), togliere `slf4j-simple`, perché una libreria non deve imporre il binding,
e alzare slf4j-api a 2.0.x, oppure dichiararlo `provided`.

**Verifica.** Nei moduli Coral, api e simple alla stessa versione 2.0.17.

## 12. BASSA: xercesImpl 2.8.0 e xalan 2.7.0
**Problema.** CDK 2.3 porta (tramite cdk-dict → xom 1.2.5) xercesImpl 2.8.0 (2006) e xalan 2.7.0, con CVE note. Il
2.10.0 richiesto altrove perde. Questi jar sostituiscono il parser XML del JDK.

**Soluzione.** Escludere xercesImpl e xalan da xom nel core, perché il JDK ha già un parser. In alternativa, forzare
xercesImpl 2.12.2 in `dependencyManagement`.

**Verifica.** Test golden del core e regressione di un campione di modelli verdi, controllando quelli che leggono
XML/PMML.

## 13. BASSA: import accidentali di javassist
**STATO: FATTO il 20/11/2026.** Rimossi i 3 import; i moduli compilano (`test-compile`). Non committato.

**Problema.** `import javassist.runtime.Desc;` inutilizzato in:
- `VegaModels-EyeIrritation/.../ismEyeIrritation.java:13`;
- `VegaModels-MeltingPoint/.../ismMeltingPoint.java`;
- `VegaModels-PPARg_up/.../mainScript.java`.

Compilano solo perché javassist arriva in modo transitivo dal core.

**Soluzione.** Eliminare le tre righe al prossimo rilascio di ciascun modulo.

**Verifica.** I moduli compilano.

## 14. BASSA: `LATEST` nel plugin asciidoctor
**STATO: FATTO il 20/11/2026.** `LATEST` si risolveva in **4.1.0-RC1**, una release candidate. Fissato a 2.0.5.RELEASE (compatibile con asciidoctor-maven-plugin 1.5.8) nei 4 pom padre; `package` verde su un modulo per repository. Nessun repository ha file `.adoc`: il plugin non produce nulla e si può valutare di toglierlo del tutto. I pom padre cambiati richiedono una nuova versione al rilascio (oggi 1.1). Non committato.

**Problema.** `spring-restdocs-asciidoctor` ha versione `LATEST` nel plugin asciidoctor dei pom padre di ecotox, fate,
other e properties, anche nei padri già pubblicati. La build non è riproducibile e può rompersi all'uscita di una
nuova versione.

**Soluzione.** Fissare una versione precisa, quella risolta oggi nel `.m2`.

**Verifica.** `mvn -o validate` sui 4 repository.

## 15. BASSA: Lombok in scope compile
**STATO: FATTO il 20/11/2026.** Scope `provided` nei 5 moduli; compilano e Lombok non è più nel classpath di runtime. Non committato.

**Problema.** In FishLC50, FishNOEC, MoaEpa e ZebrafishCoral Lombok 1.18.44 ha scope `compile`; in FatheadEPA non ha
scope, quindi è comunque compile. Così il jar di Lombok finisce nel classpath di runtime della GUI.

**Soluzione.** `<scope>provided</scope>` (come negli altri pom padre).

**Verifica.** Lombok assente da `dependency:tree -Dscope=runtime`.

## 16. BASSA: jpmml 1.5.6 e Core-Aux legato a Core 1.3.21
**Problema.** Core-Aux 1.0 porta jpmml 1.5.6 (2020) e dipende da `Core:1.3.21` con molte exclusion. È un legame di
versione nascosto, e una delle cause dell'anomalia del punto 10.

**Soluzione.** In Core-Aux 1.1:
- dichiarare Core come `provided`, così prende quello del modello;
- valutare l'aggiornamento di jpmml (verificando che i modelli PMML diano gli stessi risultati);
- in alternativa, spostare le due classi di Core-Aux nel core.

**Verifica.** Test di regressione dei moduli che usano PMML (AromataseIRFMN, NRF2Up, PPARa/PPARg/PXR_up,
SteroidogenesisEDScreen, TissueBloodIneris) verdi.

---

## Cose verificate e a posto
- **Librerie interne raggiungibili su Nexus:** Core-Aux 1.0, Coral 2.2, NRMEA 1.0, ToxTreeWrapper 1.0, PadelAdapter 0.3,
  PadelFullLib 1.1, VegaPythonEnv-Global 1.0.0, weka 3.5.8.
- **Nei sorgenti dei moduli non ci sono:**
  - dipendenze `system` o `systemPath`;
  - range di versione;
  - jar nel repository git;
  - `Runtime.exec`, `ProcessBuilder`, JNI o `Class.forName` nel codice dei modelli.
- **I pom padre pubblicati su Nexus (1.0 e 1.1)** non hanno dipendenze strane, a parte il `LATEST` del punto 14.

## Ordine di esecuzione consigliato
1. Punto 1, insieme ai commit della migrazione PaDEL (domattina).
2. Punto 3: completare la migrazione PaDEL fino alla GUI.
3. Punto 2: build con `.m2` pulito. Rivela il quadro reale e può cambiare le priorità dei punti 6 e 7.
4. Punti 4 e 5 (ToxTree), che sono collegati.
5. Punti 6, 7, 10, 11, 12, 14, 15: correzioni piccole sui pom, da fare insieme ai rilasci.
6. Punti 8, 9 e 16: progetti a sé, da pianificare.

---

## Problema noto (non di dipendenze): instabilità di PaDEL in TotalHLQsarins
Per alcune molecole i descrittori PaDEL non sono riproducibili. Il caso documentato è la molecola **1068** del
training set di TotalHLQsarins: `O=C3C4=CC=2C(N=C1C=CC=CC1=2)=C(N4(C(O)=CN3C))C6CC=5OCOC=5CC6`.

Due esiti possibili:
- **A:** TopoPSA 76.29, minHsOH 0.36988, gmax 12.95627, AATS7p 1.35145;
- **B:** TopoPSA 71.36, minHsOH 0.35988, gmax 12.96553, AATS7p 1.34188.

La differenza di 4.93 in TopoPSA corrisponde a un azoto aromatico riconosciuto oppure no. Attraverso la MLR del
modello diventa LogHLt 0.746 contro 0.729, cioè un tempo di dimezzamento di 5.575 h contro 5.354 h. **Lo stesso
programma può quindi dare due predizioni diverse per la stessa molecola.**

**Causa.** L'ordine di iterazione di mappe e insiemi basati sugli identity hash nella standardizzazione PaDEL e nella
percezione dell'aromaticità del vecchio CDK (`cdk1`, dentro PadelFullLib). Non dipende dal timeout della ricerca
degli anelli: la standardizzazione dura 40–86 ms, contro un limite di 5000.

**Misure del 19/11/2026, attraverso PadelService:**
- 200 calcoli ripetuti nello stesso processo: 140 volte A, 60 volte B. Con altre molecole intercalate: 237 contro 63.
- `-XX:hashCode=3` (hash sequenziali): la distribuzione è identica fra processi diversi (151/49 due volte). La causa
  sono quindi gli identity hash.
- `-XX:hashCode=2` (hash tutti uguali): il vecchio CDK fallisce su ogni molecola ("loops not allowed"). Non è una
  soluzione.
- La molecola BENCH-086 (SludgeCombaseClass), che con l'adapter in-process ogni tanto dava ERROR, nel servizio è
  stabile su 500 calcoli.

**Effetto sui test.** Nei test di regressione, attraverso il servizio, TotalHLQsarins è risultato verde 3 volte su 3:
il servizio riceve sempre la stessa sequenza di richieste e parte da uno stato pulito. Per questo i riferimenti sono
stati committati. Se il test diventa rosso solo sulla molecola 1068 del ts, si tratta di questo problema. In VEGA,
dove l'ordine delle molecole cambia, la predizione resta instabile.

**Possibile soluzione (da valutare).** Correggerebbe il problema solo un intervento nel codice di PadelFullLib, di cui
non abbiamo i sorgenti: per esempio decompilare e sostituire le strutture basate sugli identity hash con strutture
ordinate. In alternativa, un CDK moderno per quei descrittori, ma con valori diversi.
