package insilico.core.descriptor.blocks.pro;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.blocks.weights.other.WeightsIState;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * E-States descriptors.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class EStateIndices extends DescriptorBlock {

    private static final long serialVersionUID = 1L;
    private final static String BlockName = "E-States Descriptors";

    // for internal use (store sum and count values)
    private HashMap<String, Double> S;
    private HashMap<String, Integer> N;

    private final static String DEF_URL = "/descriptor/EStatesIndices_definition.txt";

    private class FragDefinition {
        public String Name = "";
        public String AtomType = "";
        public int nSingle = 0;
        public int nDouble = 0;
        public int nTriple = 0;
        public int nArom = 0;
        public int nH = 0;
        public int Charge = 0;
    }

    private ArrayList<FragDefinition> Fragments;


    /**
     * Constructor. This should not be used, no weight is specified. The
     * overloaded constructors should be used instead.
     */
    public EStateIndices() {
        super();
        this.Name = EStateIndices.BlockName;
    }


    @Override
    protected final void GenerateDescriptors() {

        // Build definition of fragments from local resource
        Fragments = new ArrayList<>();
        try {
            DataInputStream in;
            BufferedReader bufferedReader;
            URL tsURL = EStateIndices.class.getResource(DEF_URL);
            in = new DataInputStream(tsURL.openStream());
            bufferedReader = new BufferedReader(new InputStreamReader(in));

            String line;
            line = bufferedReader.readLine(); // first line - headers
            while((line = bufferedReader.readLine()) != null) {
                String[] buf =line.split("\t");
                FragDefinition f = new FragDefinition();
                f.Name = buf[0];
                f.AtomType = buf[1];
                f.nSingle = Integer.parseInt(buf[2]);
                f.nDouble = Integer.parseInt(buf[3]);
                f.nTriple = Integer.parseInt(buf[4]);
                f.nArom = Integer.parseInt(buf[5]);
                f.nH = Integer.parseInt(buf[6]);
                f.Charge = Integer.parseInt(buf[7]);
                Fragments.add(f);
            }

        } catch (Exception e) {
            log.warn("unable to init EStates fragment definition from local resource - " + e.getMessage());
        }

        DescList.clear();

        // Frags
        for (FragDefinition frag : Fragments)
            Add("S" + frag.Name, "Sum of " + frag.Name + " E-states");
        for (FragDefinition frag : Fragments)
            Add("N" + frag.Name, "Number of atoms of type " + frag.Name);

        // Indices
        Add("Ss", "sum of E-states (without hydrogen)");
        Add("Ms", "mean of E-states (without hydrogen)");
        Add("Gmax","maximum E-State value"); // max E-state
        Add("Gmin","minimum E-State value"); // min E-state

        SetAllValues(Descriptor.MISSING_VALUE);
    }


    /**
     * Calculate descriptors for the given molecule.
     *
     * @param mol molecule to be calculated
     */
    @Override
    public void Calculate(InsilicoMolecule mol) {

        // Generate/clear descriptors
        GenerateDescriptors();

        IAtomContainer m;
        try {
            m = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            log.warn("Invalid structure, unable to calculate: " + this.Name);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        int[][] TopoMatrix;
        try {
            TopoMatrix =  mol.GetMatrixTopologicalDistance();
        } catch (Exception e) {
            log.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        int nSK = m.getAtomCount();

        // Get I-States weights
        WeightsIState w_istate = new WeightsIState();
        double[] w_is = w_istate.getWeights(m, false);
        for (double val : w_is)
            if (val == Descriptor.MISSING_VALUE) {
                log.warn("Unable to calculate all EStates for molecule");
                SetAllValues(Descriptor.MISSING_VALUE);
                return;
            }

        // Calculate E-States
        double[] w_es = new double[nSK];
        for (int at = 0; at<nSK; at++) {
            double sumDeltaI = 0;

            for (int j = 0; j < nSK; j++)
                if (at != j)
                    sumDeltaI += (w_is[at] - w_is[j]) / Math.pow((double) TopoMatrix[at][j] + 1.0, 2.0);

            w_es[at] = w_is[at] + sumDeltaI;
        }


        // Calculation
        S = new HashMap<>();
        N = new HashMap<>();

        double Gmax= Descriptor.MISSING_VALUE, Gmin= Descriptor.MISSING_VALUE;
        double Ss = 0, Ms = 0;

        for (int at=0; at<m.getAtomCount(); at++) {

            IAtom curAt = m.getAtom(at);

            // Count H
            int nH = 0;
            try {
                nH = curAt.getImplicitHydrogenCount();
            } catch (Exception e) {
                log.warn("unable to get H count");
            }

            // formal charge
            int Charge;
            try {
                Charge = curAt.getFormalCharge();
            } catch (Exception e) {
                Charge = 0;
            }

            // Count bonds
            int nBnd=0, nSng = 0, nDbl = 0, nTri = 0, nAr=0;
            for (IBond b : m.getConnectedBondsList(curAt)) {
                if (b.getFlag(CDKConstants.ISAROMATIC)) {
                    nAr++;
                    nBnd++;
                    continue;
                }
                if (b.getOrder() == IBond.Order.SINGLE) {
                    nSng++;
                    nBnd++;
                }
                if (b.getOrder() == IBond.Order.DOUBLE) {
                    nDbl++;
                    nBnd++;
                }
                if (b.getOrder() == IBond.Order.TRIPLE) {
                    nTri++;
                    nBnd++;
                }
            }

            // Sum of e-states
            Ss += w_es[at];

            // Maximum and minimum Estate/HEstate
            Gmax = (Gmax== Descriptor.MISSING_VALUE) ? w_es[at] : (Math.max(w_es[at], Gmax));
            Gmin = (Gmin== Descriptor.MISSING_VALUE) ? w_es[at] : (Math.min(w_es[at], Gmin));

            //// Groups count

            for (FragDefinition frag : Fragments) {
                if  (frag.AtomType.equalsIgnoreCase(curAt.getSymbol())) {
                    if ( (frag.nSingle == nSng) && (frag.nDouble == nDbl) &&
                            (frag.nTriple == nTri) && (frag.nArom == nAr) &&
                            (frag.nH == nH) && (frag.Charge == Charge)) {
                        UpdateGroup(frag.Name, w_es[at]);

                    }
                }
            }

        }

        Ms = Ss / m.getAtomCount();

        SetByName("Gmax", Gmax);
        SetByName("Gmin", Gmin);
        SetByName("Ss", Ss);
        SetByName("Ms", Ms);

        for (FragDefinition frag : Fragments) {
            if (S.containsKey(frag.Name)) {
                SetByName("S" + frag.Name, S.get(frag.Name));
                SetByName("N" + frag.Name, N.get(frag.Name));
            } else {
                SetByName("S" + frag.Name, 0);
                SetByName("N" + frag.Name, 0);
            }
        }

    }


    private void UpdateGroup(String group, double sumVal) {
        if (S.containsKey(group)) {
            double bufVal = S.get(group);
            bufVal += sumVal;
            S.put(group, bufVal);
            int bufCount = N.get(group);
            bufCount++;
            N.put(group, bufCount);
        } else {
            S.put(group, sumVal);
            N.put(group, 1);
        }
    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        EStateIndices block = new EStateIndices();
        block.CloneDetailsFrom(this);
        return block;
    }

}
