package insilico.core.alerts.builders;

import insilico.core.alerts.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.tools.CustomQueryMatcher;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.ringsearch.RingPartitioner;
import org.openscience.cdk.smarts.SmartsPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Benigni/Bossa alerts, original set (SA 1-31) taken from ToxTree 2.6.13
 * implementation.
 * 
 * @author User
 */
@Slf4j
public class SABenigniBossa extends AlertBlockFromSMARTS implements iAlertBlock {

    public static String KEY_BBSA_IS_MUTAGEN = "bb_muta";
    public static String KEY_BBSA_IS_CARCINOGEN = "bb_carc";

    private static final double[] AlertAccuracy = {
            0.83,
            0.00,
            0.50,
            0.70,
            0.84,
            0.80,
            0.76,
            0.70,
            0.69,
            0.49,
            0.43,
            0.76,
            0.70,
            0.76,
            0.46,
            0.68,
            0.35,
            0.86,
            0.86,
            0.09,
            0.90,
            0.97,
            0.91,
            0.91,
            0.89,
            0.56,
            0.87,
            0.76,
            0.64,
            0.64,
            0.68,
            0.49,
            0.48,
            0.65,
            0.43
    };

    private static final double[] AlertFisherPValue = {
            0.002237,
            1.000000,
            1.000000,
            0.363187,
            0.000532,
            0.384933,
            0.000000,
            0.000000,
            0.318652,
            0.014885,
            0.029569,
            0.000000,
            0.019526,
            0.049735,
            0.586747,
            0.166236,
            0.048369,
            0.000000,
            0.000000,
            0.000000,
            0.000000,
            0.000000,
            0.015221,
            0.000003,
            0.000001,
            1.000000,
            0.000000,
            0.000000,
            0.045781,
            0.037194,
            0.002609,
            0.539260,
            0.007347,
            0.117820,
            0.708950,
    };

    public class BBAlert {
        private final ArrayList<String> SMARTS;
        private final ArrayList<String> PreSMARTS;
        private final ArrayList<Pattern> ParsedSMARTS;
        private final ArrayList<Pattern> ParsedPreSMARTS;
        private String Id;
        private String Name;
        private String Description;
        private boolean Mutagen;
        private boolean Carcinogen;

        public BBAlert() {
            SMARTS = new ArrayList<>();
            ParsedSMARTS = new ArrayList<>();
            PreSMARTS = new ArrayList<>();
            ParsedPreSMARTS = new ArrayList<>();
            Mutagen = false;
            Carcinogen = false;
        }

        public void InitSMARTS() throws InitFailureException {
            for (String curSMARTS : SMARTS) {
                try {
                    ParsedSMARTS.add(SmartsPattern.create(curSMARTS, DefaultChemObjectBuilder.getInstance()).setPrepare(false));
                } catch (Exception ex) {
                    log.warn("SMARTS: unable to initialize " + Id + ": " + curSMARTS);
                    throw new InitFailureException("unable to initialize " + Id + ": " + curSMARTS);
                }
            }
            for (String curSMARTS : PreSMARTS) {
                try {
                    ParsedPreSMARTS.add(SmartsPattern.create(curSMARTS, DefaultChemObjectBuilder.getInstance()).setPrepare(false));
                } catch (Exception ex) {
                    log.warn("PRE SMARTS: unable to initialize " + Id + ": " + curSMARTS);
                    log.warn(ex.getMessage());
                    throw new InitFailureException("unable to initialize " + Id + ": " + curSMARTS);
                }
            }
        }

        public void addSMARTS(String value) {
            this.SMARTS.add(value);
        }

        public ArrayList<String> getSMARTS() {
            return SMARTS;
        }

        public ArrayList<Pattern> getParsedSMARTS() {
            return ParsedSMARTS;
        }

        public void addPreSMARTS(String value) {
            this.PreSMARTS.add(value);
        }

        public ArrayList<String> getPreSMARTS() {
            return PreSMARTS;
        }

        public ArrayList<Pattern> getParsedPreSMARTS() {
            return ParsedPreSMARTS;
        }

        public String getId() {
            return Id;
        }

        public void setId(String Id) {
            this.Id = Id;
        }

        public String getName() {
            return Name;
        }

        public void setName(String Name) {
            this.Name = Name;
        }

        public String getDescription() {
            return Description;
        }

        public void setDescription(String Description) {
            this.Description = Description;
        }

        public boolean isMutagen() {
            return Mutagen;
        }

        public void setMutagen(boolean Mutagen) {
            this.Mutagen = Mutagen;
        }

        public boolean isCarcinogen() {
            return Carcinogen;
        }

        public void setCarcinogen(boolean Carcinogen) {
            this.Carcinogen = Carcinogen;
        }
    }


    private ArrayList<BBAlert> BBAlertList;


    public SABenigniBossa() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MUTAGEN_BENIGNI_BOSSA, "Benigni/Bossa (from ToxTree 2.6) rule set");
    }


    @Override
    protected void BuildSAList() throws InitFailureException {

        BBAlertList = CreateAlerts();

        Alert curSA;

        for (int i=0; i<BBAlertList.size(); i++) {
            BBAlert BB = BBAlertList.get(i);
            curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
            curSA.setName(BB.getId() + " " + BB.getName());
            curSA.setDescription(BB.getDescription());
            curSA.setImageURL("/insilico/core/alerts/png/benignibossa/" + BB.getId() + ".png");

            curSA.setBoolProperty(KEY_BBSA_IS_CARCINOGEN, BB.isCarcinogen());
            curSA.setBoolProperty(KEY_BBSA_IS_MUTAGEN, BB.isMutagen());

            // All alerts are toxic
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_TOXIC, true);

            // Sets accuracy of each alert
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, AlertAccuracy[i]);

            // Sets Fisher test p-value of each alert
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_FISHER, AlertFisherPValue[i]);

            Alerts.add(curSA);
        }

    }


    @Override
    protected void InitSMARTS() throws InitFailureException {

        for (BBAlert BB : BBAlertList)
            BB.InitSMARTS();

    }


    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {

        AlertList Res = new AlertList();

        try {

            // Code for SA 18-19-20 (ring based)

            boolean polycyclic_aromatic = false;
            boolean heterocyclic_aromatic = false;
            boolean polyhalogenated_cycloalkanes = false;

            // Detects rings
            IRingSet sssrings;
            try {
                sssrings = CurMol.GetSSSR();
            } catch (InvalidMoleculeException ex) {
                throw new GenericFailureException("Invalid molecule, unable to calculate SSSR");
            }
            int nrings = sssrings.getAtomContainerCount();

            if (nrings > 2) {

                List<?> ringsets = RingPartitioner.partitionRings(sssrings);
                // This partitions ring into fused rings sets

                for (int ii = 0; ii < ringsets.size(); ii++) {
                    IRingSet ringset = (IRingSet) ringsets.get(ii);

                    if (ringset.getAtomContainerCount() < 3)
                        continue;

                    int heteroaromatic_ring_count = 0;
                    int aromatic_ring_count = 0;

                    for (int j = 0; j < ringset.getAtomContainerCount(); j++) {

                        IRing ring = (IRing) ringset.getAtomContainer(j);

                        int ar=0, har=0;
                        for (int k = 0; k < ring.getAtomCount(); k++) {
                            IAtom a = ring.getAtom(k);
                            if (a.getFlag(CDKConstants.ISAROMATIC)) {
                                ar++;
                                if (!(a.getSymbol().equals("C")))
                                    har++;
                            }
                        }

                        if (ar == ring.getAtomCount()) {
                            aromatic_ring_count++;
                            if (har > 0)
                                heteroaromatic_ring_count++;
                        }
                    }

                    if ((aromatic_ring_count>2) && (heteroaromatic_ring_count==0))
                        polycyclic_aromatic = true;

                    if ((aromatic_ring_count>2) && (heteroaromatic_ring_count>0))
                        heterocyclic_aromatic = true;
                }
            }

            if (nrings > 0) {
                for (int i=0; i<sssrings.getAtomContainerCount(); i++) {
                    IRing ring = (IRing) sssrings.getAtomContainer(i);
                    int nHalo = 0;
                    boolean cycloalkane = true;
                    for (int j=0; j<ring.getAtomCount(); j++) {
                        IAtom a = ring.getAtom(j);
                        if (!(a.getSymbol().equals("C"))) {
                            cycloalkane = false;
                            break;
                        }
                        List<IAtom> alist;
                        try {
                            alist = CurMol.GetStructure().getConnectedAtomsList(a);
                        } catch (InvalidMoleculeException ex) {
                            throw new GenericFailureException("Invalid molecule, unable to calculate connected atoms list");
                        }
                        for (int k=0; k<alist.size(); k++) {
                            IAtom b = alist.get(k);
                            if ((b.getSymbol().equals("Cl")) ||
                                    (b.getSymbol().equals("Br")) ||
                                    (b.getSymbol().equals("F")) ||
                                    (b.getSymbol().equals("I")) ) {
                                nHalo++;
                            }
                        }
                    }
                    if (cycloalkane) {
                        for (int j=0; j<ring.getBondCount(); j++) {
                            IBond b = ring.getBond(j);
                            if (b.getOrder() != IBond.Order.SINGLE) {
                                cycloalkane = false;
                                break;
                            }
                        }
                        for (int k = 0; k < ring.getAtomCount(); k++) {
                            IAtom a = ring.getAtom(k);
                            if (a.getFlag(CDKConstants.ISAROMATIC)) {
                                cycloalkane = false;
                                break;
                            }
                        }
                    }

                    if ((cycloalkane) && (nHalo > 2)) {
                        polyhalogenated_cycloalkanes = true;
                        break;
                    }
                }
            }


            // Cycle on all alerts
            // Normal matching for all alerts except:
            // SA18, SA19, SA20

            for (int i=0; i<BBAlertList.size(); i++) {

                if (i==17)
                    if (polycyclic_aromatic) {
                        Res.add((Alert)Alerts.get(17).clone());
                        continue;
                    }

                if (i==18)
                    if (heterocyclic_aromatic) {
                        Res.add((Alert)Alerts.get(18).clone());
                        continue;
                    }

                if (i==19)
                    if (polyhalogenated_cycloalkanes) {
                        Res.add((Alert)Alerts.get(19).clone());
                        continue;
                    }


                // Normal matching

                BBAlert BB = BBAlertList.get(i);

                boolean PreScreen = false;
                if (BB.getParsedPreSMARTS().isEmpty())
                    PreScreen = true;
                else
                    for (Pattern q : BB.getParsedPreSMARTS())
                        if ((q.matches(CurMol.GetStructure()))) {
                            PreScreen = true;
                            break;
                        }

                if (PreScreen)
                    for (Pattern q : BB.getParsedSMARTS())
                        if (q.matches(CurMol.GetStructure())) {
                            Res.add((Alert)Alerts.get(i).clone());
                            break;
                        }

            }

        } catch (Throwable e) {
            return null;
        }

        return Res;
    }


    @Override
    public double[] getOverlapsPerc(InsilicoMolecule mol) throws InvalidMoleculeException, GenericFailureException {

        if (!mol.IsValid())
            throw new InvalidMoleculeException("Given molecule is not marked as valid");
        CurMol = mol;

        // Init
        try {
//            Matcher = new CustomQueryMatcher(mol);
            if (!IsInitialized) {
                InitSMARTS();
                IsInitialized = true;
            }
        } catch (Exception e) {
            throw new GenericFailureException("Unable to init matcher: " + e.getMessage());
        }

        // Code for SA 18-19-20 (ring based)
        boolean polycyclic_aromatic = false;
        boolean heterocyclic_aromatic = false;
        boolean polyhalogenated_cycloalkanes = false;

        try {

            // Detects rings
            IRingSet sssrings;
            try {
                sssrings = mol.GetSSSR();
            } catch (InvalidMoleculeException ex) {
                throw new GenericFailureException("Invalid molecule, unable to calculate SSSR");
            }
            int nrings = sssrings.getAtomContainerCount();

            if (nrings > 2) {

                List<?> ringsets = RingPartitioner.partitionRings(sssrings);
                // This partitions ring into fused rings sets

                for (int ii = 0; ii < ringsets.size(); ii++) {
                    IRingSet ringset = (IRingSet) ringsets.get(ii);

                    if (ringset.getAtomContainerCount() < 3)
                        continue;

                    int heteroaromatic_ring_count = 0;
                    int aromatic_ring_count = 0;

                    for (int j = 0; j < ringset.getAtomContainerCount(); j++) {

                        IRing ring = (IRing) ringset.getAtomContainer(j);

                        int ar=0, har=0;
                        for (int k = 0; k < ring.getAtomCount(); k++) {
                            IAtom a = ring.getAtom(k);
                            if (a.getFlag(CDKConstants.ISAROMATIC)) {
                                ar++;
                                if (!(a.getSymbol().equals("C")))
                                    har++;
                            }
                        }

                        if (ar == ring.getAtomCount()) {
                            aromatic_ring_count++;
                            if (har > 0)
                                heteroaromatic_ring_count++;
                        }
                    }

                    if ((aromatic_ring_count>2) && (heteroaromatic_ring_count==0))
                        polycyclic_aromatic = true;

                    if ((aromatic_ring_count>2) && (heteroaromatic_ring_count>0))
                        heterocyclic_aromatic = true;
                }
            }

            if (nrings > 0) {
                for (int i=0; i<sssrings.getAtomContainerCount(); i++) {
                    IRing ring = (IRing) sssrings.getAtomContainer(i);
                    int nHalo = 0;
                    boolean cycloalkane = true;
                    for (int j=0; j<ring.getAtomCount(); j++) {
                        IAtom a = ring.getAtom(j);
                        if (!(a.getSymbol().equals("C"))) {
                            cycloalkane = false;
                            break;
                        }
                        List<IAtom> alist;
                        try {
                            alist = mol.GetStructure().getConnectedAtomsList(a);
                        } catch (InvalidMoleculeException ex) {
                            throw new GenericFailureException("Invalid molecule, unable to calculate connected atoms list");
                        }
                        for (int k=0; k<alist.size(); k++) {
                            IAtom b = alist.get(k);
                            if ((b.getSymbol().equals("Cl")) ||
                                    (b.getSymbol().equals("Br")) ||
                                    (b.getSymbol().equals("F")) ||
                                    (b.getSymbol().equals("I")) ) {
                                nHalo++;
                            }
                        }
                    }
                    if (cycloalkane) {
                        for (int j=0; j<ring.getBondCount(); j++) {
                            IBond b = ring.getBond(j);
                            if (b.getOrder() != IBond.Order.SINGLE) {
                                cycloalkane = false;
                                break;
                            }
                        }
                        for (int k = 0; k < ring.getAtomCount(); k++) {
                            IAtom a = ring.getAtom(k);
                            if (a.getFlag(CDKConstants.ISAROMATIC)) {
                                cycloalkane = false;
                                break;
                            }
                        }
                    }

                    if ((cycloalkane) && (nHalo > 2)) {
                        polyhalogenated_cycloalkanes = true;
                        break;
                    }
                }
            }

        } catch (Throwable e) {
            //
        }

        // Calculate overlaps
        double[] Res = new double[BBAlertList.size()];

        try {
            for (int i=0; i<BBAlertList.size(); i++) {

                if (i==17)
                    if (polycyclic_aromatic) {
                        Res[17] = 1;
                        continue;
                    }

                if (i==18)
                    if (heterocyclic_aromatic) {
                        Res[18] = 1;
                        continue;
                    }

                if (i==19)
                    if (polyhalogenated_cycloalkanes) {
                        Res[19] = 1;
                        continue;
                    }

                // all other normal alerts
                BBAlert BB = BBAlertList.get(i);
                double curBBMax = 0;
                for (Pattern q : BB.getParsedSMARTS()) {
                    Iterable<IAtomContainer> matches = q.matchAll(CurMol.GetStructure()).toSubstructures();
//                    List<IAtomContainer> matches = UniversalIsomorphismTester.getOverlaps(mol.GetStructure(), q);
                    int max = 0;
                    for (IAtomContainer ac : matches)
                        if (ac.getAtomCount() > max)
                            max = ac.getAtomCount();
                    double buf = (double)max / (double)CurMol.GetStructure().getAtomCount();
                    if (buf>curBBMax) curBBMax = buf;
                }
                Res[i] = curBBMax;

            }
        } catch (InvalidMoleculeException  e) {
            throw new GenericFailureException("Error during matching: " + e.getMessage());
        }

        return Res;

    }


    public ArrayList<BBAlert> CreateAlerts() {

        ArrayList<BBAlert> BBAlerts = new ArrayList<>();
        BBAlert curAlert;

        //// SA 1 (idx 0)

        curAlert = new BBAlert();
        curAlert.setId("SA1");
        curAlert.setName("Acyl halides");
        curAlert.setDescription("Acyl halide RC(=O)[Br,Cl,F,I], where R is not OH or SH.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[!$([OH1,SH1])]C(=O)[Br,Cl,F,I]");

        BBAlerts.add(curAlert);


        //// SA 2 (idx 1)

        // SMARTS creation has been fully rewritten

        String CD1 = "[$([CH3;D1]),$([CH2;D2][Cl,Br,I,F]),$([CH1;D3]([Cl,Br,I,F])[Cl,Br,I,F]),$([CH0;D4]([Cl,Br,I,F])([Cl,Br,I,F])[Cl,Br,I,F])]";
        String CD2 = "[$([CH2;D2]),$([CH1;D3][Cl,Br,I,F]),$([CH0;D4]([Cl,Br,I,F])[Cl,Br,I,F])]";
        String CD3 = "[$([CH1;D3]),$([CH0;D4][Cl,Br,I,F])]";

        String[][] SA2_substituents_mod = {
                {"methyl-Hal", CD1},
                {"propyl", CD2 + CD1},
                {"isopropyl", CD2 + CD2 + CD1},
                {"isopropyl-1", CD3 + "(" + CD1+ ")" + CD1},
                {"butyl-2", CD2 + CD2 + CD2 + CD1},
                {"butyl-3", CD2 + CD3 + "(" + CD1 + ")" + CD1},
                {"butyl-4", "C" + "(" + CD1 + ")" + "(" + CD1 + ")" + CD1},
                {"butyl-5", CD3 + "(" + CD1 + ")" + CD2 + CD1},
                {"benzyl","[CH2;D2]c1ccccc1"}
        };

        curAlert = new BBAlert();
        curAlert.setId("SA2");
        curAlert.setName("Alkyl (C<5) or benzyl ester of sulphonic or phosphonic acid");
        curAlert.setDescription("Methyl, ethyl, propyl, butyl or benzyl esters of sulphonic or phosphonic acid. <br>P(=O)(O)(O)R or S(=O)(O)(O)R where R is not S or O <br> The alkyl chains can have halogen substituents.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);

        curAlert.addPreSMARTS("S([!$([OH1,SH1])])(=O)(=O)O");
        for (int sub=0; sub<SA2_substituents_mod.length; sub++) {
            String Sub = SA2_substituents_mod[sub][1];
            String Sulphonic = "S([!$([OH1,SH1])])(=O)(=O)O" + Sub;
            curAlert.addSMARTS(Sulphonic);
        }

        curAlert.addPreSMARTS("P(=O)([!$([OH1,SH1])])(O)O");
        for (int sub1=0; sub1<SA2_substituents_mod.length; sub1++)
            for (int sub2=0; sub2<SA2_substituents_mod.length; sub2++) {
                String Sub1 = SA2_substituents_mod[sub1][1];
                String Sub2 = SA2_substituents_mod[sub2][1];
                String Phosphonic = "P(=O)([!$([OH1,SH1])])(O" + Sub1 + ")O" + Sub2;
                curAlert.addSMARTS(Phosphonic);
            }

        BBAlerts.add(curAlert);



        //// SA 3 (idx 2)

        curAlert = new BBAlert();
        curAlert.setId("SA3");
        curAlert.setName("N-methylol derivatives");
        curAlert.setDescription("N-methylol derivatives");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[CX4H2](N)([OX2H1])");

        BBAlerts.add(curAlert);



        //// SA 4 (idx 3)

        // Original SMARTS modified due to #1
        // String SA4_smarts = "[CX3]([!Cl;!Br;!F;!I;!$(C=O)])(!@[#1,CX4])=[CX3]([Cl,F,Br,I])([#1,CX4])";

        String[] SA4_left = {
                "[C;D1]",
                "[C;D2]([!Cl;!Br;!F;!I;!$(C=O)])",
                "[C;D3]([!Cl;!Br;!F;!I;!$(C=O)])([C;!$(C=*);!$(C#*)])"
        };
        String[] SA4_right = {
                "[C;D2]([Cl,F,Br,I])",
                "[C;D3]([Cl,F,Br,I])([C;!$(C=*);!$(C#*)])",
        };

        curAlert = new BBAlert();
        curAlert.setId("SA4");
        curAlert.setName("Monohaloalkene");
        curAlert.setDescription("This alert contains halogenated olefins where at least one hydrogen or alkyl group is attached to each carbon atom.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        for (String s_left : SA4_left)
            for (String s_right : SA4_right)
                curAlert.addSMARTS(s_left + "=" + s_right);

        BBAlerts.add(curAlert);


        //// SA 5 (idx 4)

        String SA5_smarts = "[F,Cl,Br,I][CX4H2][CX4H2][N,S][CX4H2][CX4H2][F,Cl,Br,I]";
        curAlert = new BBAlert();
        curAlert.setId("SA5");
        curAlert.setName("S or N mustard");
        curAlert.setDescription("S or N mustard");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS(SA5_smarts);

        BBAlerts.add(curAlert);


        //// SA 6 (idx 5)

        curAlert = new BBAlert();
        curAlert.setId("SA6");
        curAlert.setName("Propiolactones and propiosultones");
        curAlert.setDescription("Propiolactones and propiosultones");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[O,S]=C1[O,S]CC1");
        curAlert.addSMARTS("O=S1(=O)(CCCO1)");

        BBAlerts.add(curAlert);


        //// SA 7 (idx 6)

        curAlert = new BBAlert();
        curAlert.setId("SA7");
        curAlert.setName("Epoxides and aziridines");
        curAlert.setDescription("Epoxides and aziridines");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("C1[O,N]C1");

        BBAlerts.add(curAlert);


        //// SA8 (idx 7)

        StringBuilder b8 = new StringBuilder();
        b8.append("[");
        b8.append("$([CX4!H0;R0])");
        b8.append(";");
        b8.append("!$(C([#1,C])=[O,C])");
        b8.append(";");
        b8.append("!$([CX4H2]([F,Cl,Br,I])[CX4H2][N,S][CX4H2][CX4H2][F,Cl,Br,I])");
        StringBuffer C= new StringBuffer();

        for (int i=0; i < 6; i++) {
            C.append("C");
            b8.append(";");
            b8.append("!$(");
            b8.append(C);
            b8.append("OP(O)(=O)");
            b8.append(")");

            b8.append(";");
            b8.append("!$(");
            b8.append(C);
            b8.append("OS(=O)(=O)");
            b8.append(")");
        }

        b8.append("]");
        b8.append("[Cl,Br,I]");

        curAlert = new BBAlert();
        curAlert.setId("SA8");
        curAlert.setName("Aliphatic halogens");
        curAlert.setDescription("This alert contains non tertiary aliphatic halogens. Substances fired by Alerts SA2, SA4, SA5 and SA20 should be also excluded.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS(b8.toString());

        BBAlerts.add(curAlert);


        //// SA 9 (idx 8)

        curAlert = new BBAlert();
        curAlert.setId("SA9");
        curAlert.setName("Alkyl nitrite");
        curAlert.setDescription("Alkyl nitrite");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("O=[NX2]OC");

        BBAlerts.add(curAlert);


        //// SA 10 (idx 9)

        curAlert = new BBAlert();
        curAlert.setId("SA10");
        curAlert.setName("alfa, beta unsaturated carbonyls");
        curAlert.setDescription("alfa, beta unsaturated carbonyls");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);

        // Original SMARTS modified, also code for C6 missing
        // curAlert.addSMARTS("[!a,#1;!$(C1(=O)C=CC(=O)C=C1)][#6]([!a,#1;!$(C1(=O)C=CC(=O)C=C1)])!:;=[#6][#6](=O)[!O;!$([#6]1:,=[#6][#6](=O)[#6]:,=[#6][#6](=O)1)]");

        // vecchia smart mia
        //curAlert.addSMARTS("[$([#6]);!$([#6;D2](=*)(=*));!$([#6]a);!$([#6]C1(=O)C=CC(=O)C=C1);!$([#6]CCCCCC)]!:;=[#6][#6](=O)[!O;!$(C1(=O)C=CC(=O)C=C1)]");

        curAlert.addSMARTS("[$([#6]);!$([#6]1C(=O)[#6]:,=[#6]C(=O)[#6]:,=1);!$([#6]C!=;-[C;R]!=;-[C;R]!=;-[C;R]!=;-[C;R]!=;-[C;R])]!:;=[$([#6]);!$(C=C[a])][$([#6]);!$([#6]-O);!$(C1(=O)[#6]:,=[#6]C(=O)[#6]:,=[#6]1)](=O)");

        BBAlerts.add(curAlert);


        //// SA 11 (idx 10)

        curAlert = new BBAlert();
        curAlert.setId("SA11");
        curAlert.setName("Simple aldehyde");
        curAlert.setDescription("Aliphatic and aromatic aldehydes. The alfa,beta-unsaturated aldehydes are excluded");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[#6][$([CH;D2]);!$(CC=C)](=O)");

        BBAlerts.add(curAlert);


        //// SA 12 (idx 11)

        curAlert = new BBAlert();
        curAlert.setId("SA12");
        curAlert.setName("Quinones");
        curAlert.setDescription("Quinones");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("O=[#6]1[#6]=,:[#6][#6](=O)[#6]=,:[#6]1");
        curAlert.addSMARTS("O=[#6]1[#6]=,:[#6][#6]=,:[#6][#6]1(=O)");

        BBAlerts.add(curAlert);


        //// SA 13 (idx 12)

        curAlert = new BBAlert();
        curAlert.setId("SA13");
        curAlert.setName("Hydrazine");
        curAlert.setDescription("This applies to molecules that contain a NN group not in a ring, and not NN=O. Chemicals fired by alert SA22 should be excluded from this alert. Chemicals with a quaternary protonated nitrogen, should be excluded.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[N+0]!@;-[N+0](=[!O;!N])");

        // original SMARTS corrected due to #1
        // curAlert.addSMARTS("[N+0]([#1,*])!@;-[N+0]([#1,*])");

        curAlert.addSMARTS("[$([N+0;D1]),$([N+0;D2](-*)(-N)),$([N+0;D3](-*)(-*)(-N))]!@;-[$([N+0;D1]),$([N+0;D2](-*)(-N)),$([N+0;D3](-*)(-*)(-N))]");

        BBAlerts.add(curAlert);


        //// SA 14 (idx 13)

        curAlert = new BBAlert();
        curAlert.setId("SA14");
        curAlert.setName("Aliphatic azo and azoxy");
        curAlert.setDescription("Aliphatic azo and azoxy. Chemicals fired by alert SA22 should be excluded from this alert.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);

        // original SMARTS corrected due to #1
        // curAlert.addSMARTS("[C,#1]N=[NX2][C,#1]");

        curAlert.addSMARTS("[$(NC),$([NH])]=[$([NH;D1]),$([N;D2]C)]");

        curAlert.addSMARTS("[$(C=[N+]=[N-]);!$(C=[N+]=[N-]=N);!$(C=[N+]=[N-]N)]");
        curAlert.addSMARTS("C=[$(N=N);!$(N=N=N);!$(N=NN)]");
        curAlert.addSMARTS("CN=NO");

        BBAlerts.add(curAlert);


        //// SA 15 (idx 14)

        curAlert = new BBAlert();
        curAlert.setId("SA15");
        curAlert.setName("Isocyanate and isothiocyanate groups");
        curAlert.setDescription("Isocyanate and isothiocyanate groups");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[NX2]=C=[O,S]");

        BBAlerts.add(curAlert);


        //// SA 16 (idx 15)

        curAlert = new BBAlert();
        curAlert.setId("SA16");
        curAlert.setName("Alkyl carbamate and thiocarbamate");
        curAlert.setDescription("Alkyl carbamate and thiocarbamate");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);

        // original SMARTS corrected due to #1
        // curAlert.addSMARTS("[NX3]([CX4,#1])([CX4,#1])C(=[O,S])[O,S][CX4]");

        String[] SA16_N = {
                "[NH2;D1]",
                "[NH;D2]([C;!$(C=*);!$(C#*)])",
                "[N;D3]([C;!$(C=*);!$(C#*)])([C;!$(C=*);!$(C#*)])"
        };
        for (String N : SA16_N)
            curAlert.addSMARTS(N + "C(=[O,S])[O,S][CX4]");

        BBAlerts.add(curAlert);


        //// SA 17 (idx 16)

        curAlert = new BBAlert();
        curAlert.setId("SA17");
        curAlert.setName("Thiocarbonyl (Nongenotoxic carcinogens)");
        curAlert.setDescription("Thiocarbonyl (Nongenotoxic carcinogens)");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[#7X3][#6](=[SX1])[!$([O,S][CX4])!$([OH,SH])!$([O-,S-])]");

        BBAlerts.add(curAlert);


        //// SA 18 (idx 17)

        curAlert = new BBAlert();
        curAlert.setId("SA18");
        curAlert.setName("Polycyclic Aromatic Hydrocarbons");
        curAlert.setDescription("Polycyclic Aromatic Hydrocarbons, with three or more fused rings. Does not include heterocyclic compounds");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        // No SMARTS, checked through code

        BBAlerts.add(curAlert);


        //// SA 19 (idx 18)

        curAlert = new BBAlert();
        curAlert.setId("SA19");
        curAlert.setName("Heterocyclic Polycyclic Aromatic Hydrocarbons");
        curAlert.setDescription("Heterocyclic Polycyclic Aromatic Hydrocarbons (3 or more fused rings).");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        // No SMARTS, checked through code

        BBAlerts.add(curAlert);


        //// SA 20 (idx 19)

        curAlert = new BBAlert();
        curAlert.setId("SA20");
        curAlert.setName("(Poly) Halogenated Cycloalkanes (Nongenotoxic carcinogens)");
        curAlert.setDescription("(Poly) Halogenated Cycloalkanes (Nongenotoxic carcinogens)");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        // No SMARTS, checked through code

        BBAlerts.add(curAlert);


        //// SA 21 (idx 20)

        curAlert = new BBAlert();
        curAlert.setId("SA21");
        curAlert.setName("Alkyl and aryl N-nitroso groups");
        curAlert.setDescription("Alkyl and aryl N-nitroso groups");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);

        // original SMARTS corrected due to v3 (?)
        // curAlert.addSMARTS("[C,c]N[NX2;v3]=O");

        curAlert.addSMARTS("[C,c]N[N+0;D2]=O");

        BBAlerts.add(curAlert);


        //// SA 22 (idx 21)

        curAlert = new BBAlert();
        curAlert.setId("SA22");
        curAlert.setName("Azide and triazene groups");
        curAlert.setDescription("Azide and triazene groups");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[N]=[N]-[N]");
        curAlert.addSMARTS("[N]=[N]=[N]");

        BBAlerts.add(curAlert);


        //// SA 23 (idx 22)

        curAlert = new BBAlert();
        curAlert.setId("SA23");
        curAlert.setName("Aliphatic N-nitro");
        curAlert.setDescription("Aliphatic N-nitro. The possibility to have an aromatic substituent on the nitrogen should be excluded.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[C!r][NH1]N(=O)O");
        curAlert.addSMARTS("[C!r]N(A)N(=O)O");

        BBAlerts.add(curAlert);


        //// SA 24 (idx 23)

        curAlert = new BBAlert();
        curAlert.setId("SA24");
        curAlert.setName("alfa,beta unsaturated alkoxy");
        curAlert.setDescription("alfa,beta unsaturated alkoxy");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);

        // Original SMARTS modified
        // curAlert.addSMARTS("[!$([#6](=O)[!O]),#1][C!H0;!R]([!$([#6](=O)[!O]),#1])!@;=[C!H0;!R]O[#6]");

        curAlert.addSMARTS("[CH](C)!@;=[CH]O[C,c]");

        BBAlerts.add(curAlert);


        //// SA 25 (idx 24)

        Object[][] sa28_exclusion_rules = {
                {"Ortho-disubstitution","a(a[A;!#1])(a[A;!#1])","[H]C=1C([H])=C(C)C(=C(C)C=1([H]))N([H])OC=O","", false},
                {"Carboxylic acid substituent at ortho position","aa[CX3](=O)[OX2H1]","O=C(O)C1=CC=CC=C1(N)","", false},
                {"-SO3H on the same ring","aa[SX4](=[OX1])(=[OX1])([O])","NC=1C=CC=CC=1S(=O)(=O)[O-]","", false},
                {"-SO3H on the same ring","aaa[SX4](=[OX1])(=[OX1])([O])","NC=1C=CC=C(C=1)S(=O)(=O)[O-]","", false},
                {"-SO3H on the same ring","aaaa[SX4](=[OX1])(=[OX1])([O])","O=S(=O)([O-])C1=CC=C(N)C=C1","", false},
                {"-SO3H on the same ring","aaaaa[SX4](=[OX1])(=[OX1])([O])","","", false},
                {"-SO3H on the same ring","aaaaaa[SX4](=[OX1])(=[OX1])([O])","","", false},
        };

        StringBuilder b25 = new StringBuilder();
        b25.append("[a");
        for (int i=0; i < sa28_exclusion_rules.length;i++) {
            b25.append(";!$(");
            b25.append(sa28_exclusion_rules[i][1]);
            b25.append(")");
        }
        b25.append("]!@[");
        b25.append("$(");
        b25.append("[NX2]=O");
        b25.append(")");
        b25.append("]");

        StringBuilder e25 = new StringBuilder();
        e25.append("Aromatic nitroso group. However, the following structures should be excluded:");
        Object old = "";
        for (int i=0; i < sa28_exclusion_rules.length;i++) {
            if (old.equals(sa28_exclusion_rules[i][0])) continue;
            if (i>0) e25.append(", ");
            e25.append(sa28_exclusion_rules[i][0]);
            old = sa28_exclusion_rules[i][0];
        }

        curAlert = new BBAlert();
        curAlert.setId("SA25");
        curAlert.setName("Aromatic nitroso group");
        curAlert.setDescription(e25.toString());
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addPreSMARTS("[NX2]=O");
        curAlert.addSMARTS(b25.toString());

        BBAlerts.add(curAlert);


        //// SA 26 (idx 25)

        curAlert = new BBAlert();
        curAlert.setId("SA26");
        curAlert.setName("Aromatic ring N-oxide");
        curAlert.setDescription("Aromatic ring N-oxide");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[n+]!@[O-]");

        BBAlerts.add(curAlert);


        //// SA 27 (idx 26)

        String[][] nitro = {
                {"Nitro charged","[N+]([O-])=O"},
                // {"Nitro uncharged","[N](=O)=O"} // VEGA normalizes nitro groups to [N+]([O-])=O
        };
        Object[][] sa27_exclusion_rules = {
                {"Ortho-disubstitution","a(a[A;!#1;!H])(a[A;!#1;!H])", false},
                {"Carboxylic acid substituent at ortho position","aa[CX3](=O)[OX2H1]", false},
                {"-SO3H on the same ring","aa[SX4](=[OX1])(=[OX1])([OX2H1])", false},
                {"-SO3H on the same ring","aaa[SX4](=[OX1])(=[OX1])([OX2H1])", false},
                {"-SO3H on the same ring","aaaa[SX4](=[OX1])(=[OX1])([OX2H1])", false},
                {"-SO3H on the same ring","aaaaa[SX4](=[OX1])(=[OX1])([OX2H1])", false},
                {"-SO3H on the same ring","aaaaaa[SX4](=[OX1])(=[OX1])([OX2H1])", false}
        };

        StringBuilder b27 = new StringBuilder();
        b27.append("[a");
        for (int i=0; i < sa27_exclusion_rules.length;i++) {
            b27.append(";");
            if (!((Boolean)sa27_exclusion_rules[i][2]).booleanValue())
                b27.append("!");
            b27.append("$(");
            b27.append(sa27_exclusion_rules[i][1]);
            b27.append(")");
        }
        b27.append("]([");
        for (int i=0; i < nitro.length;i++) {
            if (i>0) b27.append(',');
            b27.append("$(");
            b27.append(nitro[i][1]);
            b27.append(")");
        }
        b27.append("])");

        StringBuilder e27 = new StringBuilder();
        e27.append("Nitro aromatic. However: ");
        e27.append("Aromatic nitro groups with ortho-disubstitution or with a carboxylic acid substituent in ortho position should be excluded. ");
        e27.append("Please note that a molecule like this <b>CC1=CC=CC(=C1[N+](=O)[O-])[N+](=O)[O-]</b> should be included in the alert: one of the two nitro groups is ortho disubstituted, but the other one is ortho-monosubstituted. ");
        e27.append("Also the following molecule <b>CC2=CC=CC(CCC1=CC=CC(=C1)[N+](=O)[O-])=C2[N+](=O)[O-]</b> Should fire the alert (one nitro group is ortho disubstituted, but the other is not). ");
        e27.append("If a sulfonic acid group (-SO3H) is present on the ring that contains also the nitro group, the substance should be excluded. ");

        curAlert = new BBAlert();
        curAlert.setId("SA27");
        curAlert.setName("Nitro aromatic");
        curAlert.setDescription(e27.toString());
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addPreSMARTS("[N+][O-]");
        curAlert.addSMARTS(b27.toString());

        BBAlerts.add(curAlert);


        //// SA 28 (idx 27)

// Original SMARTS modified        
//        String[][] sa28_amines = {
//            {"Primary amine","[NX3;v3]([#1])([#1])"},
//            {"Hydroxyl amine","[NX3;v3]([OX2H])([#1,CX4,CX3])"},
//            {"Hydroxyl amine ester","[NX3;v3]([#1,CX4])OC=O"}
//        };	

        String[][] sa28_amines = {
                {"Primary amine","[N+0;H2;D1]"},
                {"Hydroxyl amine","[N+0;H1;D2][OH;D1]"},
                {"Hydroxyl amine","[N+0;H0;D3]([OH;D1])C"},
                {"Hydroxyl amine ester","[N+0;H1;D2]OC=O"},
                {"Hydroxyl amine ester","[N+0;H0;D3](C)OC=O"}
        };
        // Note: sa28_exclusion_rules array has been already defined above

        StringBuilder b28 = new StringBuilder();
        b28.append("[a");
        for (int i=0; i < sa28_exclusion_rules.length;i++) {
            b28.append(";!$(");
            b28.append(sa28_exclusion_rules[i][1]);
            b28.append(")");
        }
        b28.append("]!@[");
        for (int i=0; i < sa28_amines.length;i++) {
            if (i>0) b28.append(',');
            b28.append("$(");
            b28.append(sa28_amines[i][1]);
            b28.append(")");
        }
        b28.append("]");

        StringBuilder e28 = new StringBuilder();
        e28.append("Primary aromatic amine, hydroxyl amine and its derived esters (with restrictions). However: ");
        e28.append("Aromatic amino groups with ortho disubstitutions or with a carboxylic acid substituent in ortho position are excluded. ");
        e28.append("If a sulfonic acid group (-SO3H) is present on the ring that contains also the amino group, the substance should be excluded from the alert. ");
        e28.append("The following structures should also be included: O=C=NC1=CC=CC=C1 and C([H])([H])=NC1=CC=CC=C1. ");
        e28.append("The possibility that the Nitrogen atom of hydroxyl amine is part of a cycle, should be excluded.");

        curAlert = new BBAlert();
        curAlert.setId("SA28");
        curAlert.setName("Primary aromatic amine, hydroxyl amine and its derived esters (with restrictions)");
        curAlert.setDescription(e28.toString());
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS(b28.toString());
        curAlert.addSMARTS("aN=C=O");
        curAlert.addSMARTS("aN=[CH2]");

        BBAlerts.add(curAlert);


        //// SA 28bis (idx 28)

// Original SMARTS modified        
//        String[][] amines = {
//            {"Aromatic mono- and dialkylamine","[NX3;v3]([#1,CH3])([CH3])"},
//            {"Aromatic mono- and dialkylamine","[NX3;v3]([#1,CH3])([CH2][CH3])"},
//            {"Aromatic mono- and dialkylamine","[NX3;v3]([CH2][CH3])([CH2][CH3])"},
//        };      
        String[][] amines = {
                {"Aromatic mono- and dialkylamine","[N+0;H1;D2][CH3]"},
                {"Aromatic mono- and dialkylamine","[N+0;H0;D3]([CH3])([CH3])"},
                {"Aromatic mono- and dialkylamine","[N+0;H1;D2][CH2][CH3]"},
                {"Aromatic mono- and dialkylamine","[N+0;H0;D3]([CH3])([CH2][CH3])"},
                {"Aromatic mono- and dialkylamine","[N+0;H0;D3]([CH2][CH3])([CH2][CH3])"},
        };
        // Note: sa28_exclusion_rules array has been already defined above

        StringBuilder b28bis = new StringBuilder();
        b28bis.append("[a");
        for (int i=0; i < sa28_exclusion_rules.length;i++) {
            b28bis.append(";!$(");
            b28bis.append(sa28_exclusion_rules[i][1]);
            b28bis.append(")");
        }
        b28bis.append("]!@[");
        for (int i=0; i < amines.length;i++) {
            if (i>0) b28bis.append(',');
            b28bis.append("$(");
            b28bis.append(amines[i][1]);
            b28bis.append(")");
        }
        b28bis.append("]");

        StringBuilder e28bis = new StringBuilder();
        e28bis.append("Mono- or di- methyl or ethyl aromatic amines, are included. However:");
        e28bis.append("Aromatic amino groups with ortho-disubstitution or with a carboxylic acid substituent in ortho position should be excluded. ");
        e28bis.append("If a sulfonic acid group (-SO3H) is present on the ring that contains also the amino group, the substance should be excluded from the alert.");

        curAlert = new BBAlert();
        curAlert.setId("SA28bis");
        curAlert.setName("Aromatic mono- and dialkylamine");
        curAlert.setDescription(e28bis.toString());
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS(b28bis.toString());

        BBAlerts.add(curAlert);


        //// SA 28ter (idx 29)

        // Note: sa28_exclusion_rules array has been already defined above
        StringBuilder b28ter = new StringBuilder();
        b28ter.append("[a");
        for (int i=0; i < sa28_exclusion_rules.length;i++) {
            b28ter.append(";!$(");
            b28ter.append(sa28_exclusion_rules[i][1]);
            b28ter.append(")");
        }
        b28ter.append("]!@[");
//        b28ter.append("$([NX3;v3]([#1,CH3])C(=O)([#1,CH3]))");

        b28ter.append("$([N+0;H1;D2]C(=O)([CH3])),");
        b28ter.append("$([N+0;H0;D3]([CH3])C(=O)([CH3])),");
        b28ter.append("$([N+0;H1;D2][CH1](=O)),");
        b28ter.append("$([N+0;H0;D3]([CH3])[CH1](=O))");
        b28ter.append("]");

        StringBuilder e28ter = new StringBuilder();
        e28ter.append("Aromatic N-acyl amine. However:");
        e28ter.append("Aromatic amino groups with ortho-disubstitution or with a carboxylic acid substituent in ortho position should be excluded. ");
        e28ter.append("If a sulfonic acid group (-SO3H) is present on the ring that contains also the amino group, the substance should be excluded from the alert.");

        curAlert = new BBAlert();
        curAlert.setId("SA28ter");
        curAlert.setName("Aromatic N-acyl amine");
        curAlert.setDescription(e28ter.toString());
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS(b28ter.toString());

        BBAlerts.add(curAlert);


        //// SA 29 (idx 30)

        curAlert = new BBAlert();
        curAlert.setId("SA29");
        curAlert.setName("Aromatic diazo");
        curAlert.setDescription("Aromatic diazo. If a sulfonic acid group (-SO3H) is present on each of the rings that contain the diazo group, the substance should be not classified.");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);

        // Original SMARTS modified for SO3 exclusion groups
        // curAlert.addSMARTS("a[N]=[N]a");
        // curAlert.addSMARTS("[$(a:a(S(=[OX1])(=[OX1])([O-,OX2H1]))),$(a:a:a(S(=[OX1])(=[OX1])([O-,OX2H1]))),$(a:a:a:a(S(=[OX1])(=[OX1])([O-,OX2H1]))),$(a:a:a:a:a(S(=[OX1])(=[OX1])([O-,OX2H1])))][N]=[N][$(a:a(S(=[OX1])(=[OX1])([O-,OX2H1]))),$(a:a:a(S(=[OX1])(=[OX1])([O-,OX2H1]))),$(a:a:a:a(S(=[OX1])(=[OX1])([O-,OX2H1]))),$(a:a:a:a:a(S(=[OX1])(=[OX1])([O-,OX2H1])))]");

        String Sulfonic_Group = "S(=[OD1])(=[OD1])[OD1]";
        StringBuilder b29 = new StringBuilder();
        b29.append("[$([N](a)=[N]a)");
        String s_ar1 = "";
        for (int ar1=0; ar1<6; ar1++) {
            s_ar1 += ":a";
            String s_ar2 = "";
            for (int ar2=0; ar2<6; ar2++) {
                s_ar2 += ":a";
                b29.append(";!$([N](a");
                b29.append(s_ar1);
                b29.append(Sulfonic_Group);
                b29.append(")=[N]a");
                b29.append(s_ar2);
                b29.append(Sulfonic_Group);
                b29.append(")");
            }
        }
        b29.append("]");
        curAlert.addSMARTS(b29.toString());

        BBAlerts.add(curAlert);


        //// SA 30 (idx 31)

        curAlert = new BBAlert();
        curAlert.setId("SA30");
        curAlert.setName("Coumarins and Furocoumarins");
        curAlert.setDescription("Coumarins and Furocoumarins");
        curAlert.setMutagen(true);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("O=c1ccc2ccccc2(o1)");
        curAlert.addSMARTS("O=C1C=Cc2ccccc2O1");

        BBAlerts.add(curAlert);


        //// SA 31a (idx 32)

        // Some exclusion rules removed, they were only in ToxTree (not in the
        // original paper) and correspond to previous alerts

        String hydroxyl = "3 or more hydroxyl groups";
        String[][] exclusion_rules_Hal = {
                //{title, smarts, example}
                {"Structures with 2 halogens ortho","[Cl,Br,I,F]cc[Cl,Br,I,F]","FC1=CC=CC=C1Cl"},
                {"Structures with 2 halogens meta","[Cl,Br,I,F]ccc[Cl,Br,I,F]","C=1C=C(C=C(C=1)Br)Cl"},
                {hydroxyl,"[Cl,Br,I,F]c1c([OX2H])c([OX2H])c([OX2H])cc1",""},
                {hydroxyl,"[Cl,Br,I,F]c1c([OX2H])c([OX2H])cc([OX2H])c1",""},
                {hydroxyl,"[Cl,Br,I,F]c1c([OX2H])c([OX2H])ccc1([OX2H])",""},
                {hydroxyl,"[Cl,Br,I,F]c1c([OX2H])cc([OX2H])c([OX2H])c1",""},
                {hydroxyl,"[Cl,Br,I,F]c1c([OX2H])cc([OX2H])cc1([OX2H])",""},
                {hydroxyl,"[Cl,Br,I,F]c1cc([OX2H])c([OX2H])c([OX2H])c1",""}
        };
        Object[][] sa31a_exclusion_rules = {
                //{title, smarts, example,rulename, result}
//            {"Nitro aromatic","c[N+](=O)[O-]","O=[N+]([O-])C=1C=CC=C(C=1)Cl","SA27_gen", false},
//            {"Primary aromatic amine","c[N]([#1,C])([#1,C])","CNC=1C([H])=C([H])C([H])=C(Cl)C=1([H])","SA28_gen", false},
//            {"Hydroxyl amine","cN([OX2H])([#1,C])","[H]ONC1=CC=CC=C1Cl","SA28_gen", false},
//            {"Hydroxyl amine ester","cN([#1,C])OC=O","CCCN(OC=O)C1=CC=CC=C1(Cl)","SA28_gen", false},
//            {"Aromatic mono- and dialkylamine","c[NX3v3]([#1,CH3])([#1,CH3])","CNC=1C=CC=C(Cl)C=1","SA28bis_gen", false},        
//            {"Aromatic mono- and dialkylamine","c[NX3v3]([#1,CH3])([CH2][CH3])","CCN(C)C=1C=CC=C(C=1)Cl","", false},
//            {"Aromatic mono- and dialkylamine","c[NX3v3]([CH2][CH3])([CH2][CH3])","CCN(CC)C=1C=CC=C(Cl)C=1","",false}, 
//            {"Aromatic N-acyl amine","cNC(=O)[#1,CH3]","CC(=O)NC=1C=CC=C(C=1)Cl","SA28ter_gen", false},
//            {"Aromatic diazo","cN=[N]a","C1=CC=C(C=C1)N=NC=2C=CC=C(C=2)Cl","SA29_gen", false},
                {"Biphenyls","c!@[cR1r6]1ccccc1","C1=CC=C(C=C1)C2=CC=CC=C2Cl","", false},
                {"Diphenyls","c!@*!@c1ccccc1","c1c(Cl)c(ccc1Cc2ccc(cc2))","", false},
                {"Not in fused rings","[R2]","C=1C=CC=2C(C=1)=CC=CC=2Cl","", false}
        };

        StringBuilder b31a = new StringBuilder();
        for (int i=0; i < 5; i++) {
            b31a.append("[");
            b31a.append("c");
            if (i<5)
                for (int j=0; j < sa31a_exclusion_rules.length;j++) {
                    b31a.append(";");
                    if (!((Boolean)sa31a_exclusion_rules[j][4]).booleanValue())
                        b31a.append("!");
                    b31a.append("$(");
                    b31a.append(sa31a_exclusion_rules[j][1]);
                    b31a.append(")");
                }
            b31a.append("]");
            if (i==0) b31a.append("1");
        }
        b31a.append("c1");
        b31a.append("(");
        b31a.append("[");
        b31a.append("Cl,Br,F,I");
        for (int i=0; i < exclusion_rules_Hal.length;i++) {
            b31a.append(";");
            b31a.append("!$(");
            b31a.append(exclusion_rules_Hal[i][1]);
            b31a.append(")");
        }
        b31a.append("]");
        b31a.append(")");

        StringBuilder e31a = new StringBuilder();
        e31a.append("Halogenated benzene  (Nongenotoxic carcinogens). ");
        e31a.append("The rule applies only to halogenated benezenes (not naphtalenes, etc.), but it should allow for the presence of other rings in the same molecule. ");
        e31a.append("However, the following structures should be excluded:");
        for (int i=0; i < exclusion_rules_Hal.length;i++) {
            if (i>0) e31a.append(", ");
            e31a.append(exclusion_rules_Hal[i][0]);
        }
        e31a.append(" with 3 or more hydroxyl groups. ");

        old = "";
        for (int i=0; i < sa31a_exclusion_rules.length;i++) {
            if (old.equals(sa31a_exclusion_rules[i][0])) continue;
            if (i>0) e31a.append(", ");
            e31a.append(sa31a_exclusion_rules[i][0]);
            old = sa31a_exclusion_rules[i][0];
        }

        curAlert = new BBAlert();
        curAlert.setId("SA31a");
        curAlert.setName("Halogenated benzene  (Nongenotoxic carcinogens)");
        curAlert.setDescription(e31a.toString());
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS(b31a.toString());

        BBAlerts.add(curAlert);


        //// SA 31b (idx 33)

        curAlert = new BBAlert();
        curAlert.setId("SA31b");
        curAlert.setName("Halogenated PAH (naphthalenes, biphenyls, diphenyls)  (Nongenotoxic carcinogens)");
        curAlert.setDescription("Halogenated PAH (naphthalenes, biphenyls, diphenyls)  (Nongenotoxic carcinogens)");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("[Cl,Br,F,I]c1ccc2ccccc2(c1)");
        curAlert.addSMARTS("[Cl,Br,F,I]c1ccc(cc1)!@c2ccc(cc2)[Cl,Br,F,I]");
        curAlert.addSMARTS("c1cc(ccc1[!R]c2ccc(cc2)[Cl,Br,F,I])[Cl,Br,F,I]");

        BBAlerts.add(curAlert);


        //// SA 31c (idx 34)

        curAlert = new BBAlert();
        curAlert.setId("SA31c");
        curAlert.setName("Halogenated dibenzodioxins  (Nongenotoxic carcinogens)");
        curAlert.setDescription("Halogenated dibenzodioxins  (Nongenotoxic carcinogens). Only the chemicals with at least one halogen in one of the four lateral positions should fire.");
        curAlert.setMutagen(false);
        curAlert.setCarcinogen(true);
        curAlert.addSMARTS("c1ccc2Oc3cc(ccc3(Oc2(c1)))[Cl,Br,F,I]");

        BBAlerts.add(curAlert);





        return BBAlerts;

    }

}