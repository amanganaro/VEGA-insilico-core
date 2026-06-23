package insilico.core.auxiliary.protoqsar.algae;

import insilico.core.auxiliary.protoqsar.algae.weight.Mass;
import insilico.core.auxiliary.protoqsar.algae.weight.VertexDegree;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.blocks.weights.other.WeightsQuantumNumber;
import insilico.core.exception.DescriptorNotFoundException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.matrix.TopoDistanceMatrix;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Alberto
 */
public class AlgaeDescriptors {
    
    private final static int MAX_TOPO_DISTANCE = 10;

    private final static String AtomCouples[][] = {
        {"C", "C"},
        {"C", "N"},
        {"C", "O"},
        {"C", "S"},
        {"C", "P"},
        {"C", "F"},
        {"C", "Cl"},
        {"C", "Br"},
        {"C", "I"},
        {"N", "N"},
        {"N", "O"},
        {"N", "S"},
        {"N", "P"},
        {"N", "F"},
        {"N", "Cl"},
        {"N", "Br"},
        {"N", "I"},
        {"O", "O"},
        {"O", "S"},
        {"O", "P"},
        {"O", "F"},
        {"O", "Cl"},
        {"O", "Br"},
        {"O", "I"},
        {"S", "S"},
        {"S", "P"},
        {"S", "F"},
        {"S", "Cl"},
        {"S", "Br"},
        {"S", "I"},
        {"P", "P"},
        {"P", "F"},
        {"P", "Cl"},
        {"P", "Br"},
        {"P", "I"},
        {"F", "F"},
        {"F", "Cl"},
        {"F", "Br"},
        {"F", "I"},
        {"Cl", "Cl"},
        {"Cl", "Br"},
        {"Cl", "I"},
        {"Br", "Br"},
        {"Br", "I"},
        {"I", "I"}
    };    
    
    private final ArrayList<Descriptor> DescList;
    
    
    public AlgaeDescriptors() {
        
        DescList = new ArrayList<>();
        
        // Binary fp descriptors (B and F)
        for (int lag=1; lag<= MAX_TOPO_DISTANCE; lag++)
            for (int i=0; i<AtomCouples.length; i++) {
                Descriptor d = new Descriptor("B"+FormatLag(lag)+"[" + AtomCouples[i][0] + "-" + AtomCouples[i][1] + "]", "");
                d.setValue(Descriptor.MISSING_VALUE);
                DescList.add(d);
            }                
        for (int lag=1; lag<= MAX_TOPO_DISTANCE; lag++)
            for (int i=0; i<AtomCouples.length; i++) {
                Descriptor d = new Descriptor("F"+FormatLag(lag)+"[" + AtomCouples[i][0] + "-" + AtomCouples[i][1] + "]", "");
                d.setValue(Descriptor.MISSING_VALUE);
                DescList.add(d);
            }   
        
        // ACs
        Descriptor d = new Descriptor("ATS5m","");
        d.setValue(Descriptor.MISSING_VALUE);
        DescList.add(d);
        d = new Descriptor("MATS5m","");
        d.setValue(Descriptor.MISSING_VALUE);
        DescList.add(d);
        
        // Ring
        d = new Descriptor("nR10","");
        d.setValue(Descriptor.MISSING_VALUE);
        DescList.add(d);
        
        // Connectivity
        d = new Descriptor("X3","");
        d.setValue(Descriptor.MISSING_VALUE);
        DescList.add(d);
        d = new Descriptor("X3A","");
        d.setValue(Descriptor.MISSING_VALUE);
        DescList.add(d);
        d = new Descriptor("X3sol","");
        d.setValue(Descriptor.MISSING_VALUE);
        DescList.add(d);
        
        
    }
    
    
    private String FormatLag(int value) {
        String s;
        if (value < 10)
            s = "0" + value;
        else
            s = "" + value;
        return s;
    }
    
    
    public void Calculate(InsilicoMolecule mol) throws Exception {

        //// BINARY FINGERPRINTS
        
        IAtomContainer m;
        m = mol.GetStructure();
        int nSK = m.getAtomCount();

        // Gets matrices
        int[][] TopoMat = mol.GetMatrixTopologicalDistance();
        
        for (int d=0; d<AtomCouples.length; d++) {
        
            int[] descB = new int[MAX_TOPO_DISTANCE];
            int[] descF = new int[MAX_TOPO_DISTANCE];
            Arrays.fill(descB, 0);
            Arrays.fill(descF, 0);
            
            for (int i=0; i<nSK; i++) {
                if (m.getAtom(i).getSymbol().equalsIgnoreCase(AtomCouples[d][0])) {
                    for (int j=0; j<nSK; j++) {
                        if (i==j) continue;
                        if (m.getAtom(j).getSymbol().equalsIgnoreCase(AtomCouples[d][1])) {

                            // B (presence of pair) and F (number of couples)
                            if (TopoMat[i][j] <= MAX_TOPO_DISTANCE) {
                                descB[TopoMat[i][j]-1] = 1;
                                descF[TopoMat[i][j]-1]++;
                            }
                            
                        }                        
                    }
                }
            }      
            
            // Fix: if atoms are the same, resulting value is calculated twice
            if (AtomCouples[d][0].compareTo(AtomCouples[d][1]) == 0) {
                for (int i=0; i<descF.length; i++)
                    descF[i] /= 2;
            }
            
            for (int i=0; i<descB.length; i++)            
                SetByName("B"+FormatLag(i+1)+"[" + AtomCouples[d][0] + "-" + AtomCouples[d][1] + "]", descB[i]);
            for (int i=0; i<descF.length; i++)            
                SetByName("F"+FormatLag(i+1)+"[" + AtomCouples[d][0] + "-" + AtomCouples[d][1] + "]", descF[i]);
                
        } 
        
        
        
        //// RING

        double nR10 = 0;
        IRingSet allRings = mol.GetAllRings();
        Iterator<IAtomContainer> RingsIterator = allRings.atomContainers().iterator();
        while (RingsIterator.hasNext()) {
            IRing ring = (IRing)RingsIterator.next();
            if (ring.getAtomCount() == 10)
                nR10++;
        }

        SetByName("nR10", nR10);
        
        
        
        //// CONNECTIVITY
        
        int MaxPath = 3;
        
        // Gets matrices
        double[][] ConnAugMatrix = mol.GetMatrixConnectionAugmented();
        
        int[] VD = VertexDegree.getWeights(m, true);
        WeightsQuantumNumber QN = new WeightsQuantumNumber();
        int[] Qnumbers = QN.getWeights(m);
        double[] curDescX = new double[MaxPath+1];
        double[] curDescXA = new double[MaxPath+1];
        double[] curDescXsol = new double[MaxPath+1];
        int[] nPaths = new int[MaxPath+1];

        // checks for missing weights
        for (int i=0; i<Qnumbers.length; i++)
            if (Qnumbers[i] == -999) 
                throw new Exception("Missing Q numbers");
        
        // clears VD matrix from linked F
        // apparently this is only for D7 compatibility
//        for (int i=0; i<nSK; i++) 
//            for (int j=0; j<nSK; j++) {
//                if (i==j) continue;
//                if ((ConnAugMatrix[i][j]>0) && (ConnAugMatrix[j][j]==9))
//                    VD[i]--;
//            }
        
        for (int k=0; k<MaxPath; k++) { 
            curDescX[k] = 0; 
            curDescXA[k] = 0; 
            curDescXsol[k] = 0;
            nPaths[k] = 0;
        }
            
        for (int i=0; i<nSK; i++) {

            if (ConnAugMatrix[i][i] == 9) 
                continue; // F not taken into account
                        
            // path 0
            curDescX[0] += Math.pow(VD[i], -0.5);
            curDescXA[0] = curDescX[0];
            curDescXsol[0] += 0.5 * Qnumbers[i] * Math.pow(VD[i], -0.5);
            
            // path 1 - MaxPath
            for (int path=1; path<(MaxPath+1); path++) {
                
                if (curDescX[path] == -999) continue;
                
                IAtom at =  m.getAtom(i);
                List<List<IAtom>> CurPaths =  PathTools.getPathsOfLength(m, at, path);
                nPaths[path] += CurPaths.size();
                for (List<IAtom> curPath : CurPaths) {
                    double prodX = 1;
                    int prodQuantum = 1;
                    for (IAtom iAtom : curPath) {
                        int atIdx = m.indexOf(iAtom);
                        prodX *= VD[atIdx];
                        if (ConnAugMatrix[atIdx][atIdx] != 9)
                            prodQuantum *= Qnumbers[atIdx];
                    }
                    curDescX[path] += Math.pow(prodX, -0.5);
                    curDescXsol[path] += (1.00 / Math.pow(2.00, (double) (path + 1))) *
                            ((double) prodQuantum) * Math.pow(prodX, -0.5);
                }
                
            }
        }
        
        // descriptors with path>0 counted all paths twice
        for (int i=1; i<(MaxPath+1); i++) {
            curDescX[i] /= 2;
            curDescXA[i] = curDescX[i]==0 ? 0.0 : curDescX[i] / (nPaths[i]/2.0);
            curDescXsol[i] /= 2;
        }
        
        
        // Sets descriptors
        SetByName("X3", curDescX[3]);
        SetByName("X3A", curDescXA[3]);
        SetByName("X3sol", curDescXsol[3]);

        
        
        //// AUTOCORRELATIONS (on H depleted molecule - as in D6 ?)
        
        // Gets matrices
        int[][] TopoMatrix = TopoDistanceMatrix.getMatrix(m);
            
        double[] w = Mass.getWeights(m);

        // If one or more weights are not available, sets all to missing value
        boolean MissingWeight = false;
        for (int i=0; i<nSK; i++) 
            if (w[i] == Descriptor.MISSING_VALUE)
                MissingWeight = true;
        if (MissingWeight)        
            throw new Exception("missing value in mass weights");

        // Calculates weights averages
        double wA = 0;        
        for (int i=0; i<nSK; i++)
            wA += w[i];
        wA = wA / ((double) nSK);

        Integer lag = 5;

        double AC=0, MoranAC=0;
        double denom = 0, delta = 0;        

        for (int i=0; i<nSK; i++) {
            denom += Math.pow((w[i] - wA), 2);
            for (int j=0; j<nSK; j++) 
                if (TopoMatrix[i][j] == lag) {
                    AC += w[i] * w[j];
                    MoranAC += (w[i] - wA) * (w[j] - wA);
                    delta++;
                }
        }

        if (delta > 0) {
            if (denom == 0) {
                MoranAC = 1;
            } else {
                MoranAC = ((1 / delta) * MoranAC) / ((1 / ((double)nSK)) * denom);
            }
        }

        // AC transformed in log form
        AC /= 2.0;
        AC = Math.log(1 + AC);

        // Sets descriptors
        SetByName("ATS" + lag.toString() + "m", AC);
        SetByName("MATS" + lag.toString() + "m", MoranAC);
        
                
    }
    
    
    private boolean SetByName(String Name, double Value) {
        Descriptor d = null;
        int i;
        for (i=0; i<DescList.size(); i++) 
            if (DescList.get(i).getName().equalsIgnoreCase(Name)) {
                d = DescList.get(i);
                break;
            }
        if (d==null) {
            return false;
        } else {
            d.setValue(Value);
            DescList.set(i, d);
            return true;
        }
    }
    
    
    private Descriptor GetByName(String Name) throws DescriptorNotFoundException {
        for (int i=0; i<DescList.size(); i++) 
            if (DescList.get(i).getName().equalsIgnoreCase(Name)) {
                return DescList.get(i);
            }
        throw new DescriptorNotFoundException("Descriptor " + Name + " not found.");
    }
    
    
    public ArrayList<Descriptor> GetDescriptors() {
        return DescList;
    }
    
    
    public double[] GetDescriptorsForAlgaeClassificationModel() throws DescriptorNotFoundException {
        double[] desc = new double[14];
        desc[0] = GetByName("ATS5m").getValue();
        desc[1] = GetByName("B01[C-Cl]").getValue();
        desc[2] = GetByName("B01[C-O]").getValue();
        desc[3] = GetByName("B02[Cl-Cl]").getValue();
        desc[4] = GetByName("B02[N-O]").getValue();
        desc[5] = GetByName("B03[N-O]").getValue();
        desc[6] = GetByName("B09[C-C]").getValue();
        desc[7] = GetByName("B09[O-O]").getValue();
        desc[8] = GetByName("B10[C-O]").getValue();
        desc[9] = GetByName("F01[C-O]").getValue();
        desc[10] = GetByName("F02[C-O]").getValue();
        desc[11] = GetByName("F04[O-O]").getValue();
        desc[12] = GetByName("F10[C-N]").getValue();
        desc[13] = GetByName("X3A").getValue();
        return desc;
    }


    public double[] GetDescriptorsForAlgaeQuantitativeModel() throws DescriptorNotFoundException {
        double[] desc = new double[15];
        desc[0] = GetByName("B01[O-S]").getValue();
        desc[1] = GetByName("B02[N-Cl]").getValue();
        desc[2] = GetByName("B02[N-O]").getValue();
        desc[3] = GetByName("B03[O-Cl]").getValue();
        desc[4] = GetByName("B05[C-S]").getValue();
        desc[5] = GetByName("B06[N-Cl]").getValue();
        desc[6] = GetByName("B07[N-O]").getValue();
        desc[7] = GetByName("B09[C-C]").getValue();
        desc[8] = GetByName("F01[C-O]").getValue();
        desc[9] = GetByName("F05[O-O]").getValue();
        desc[10] = GetByName("MATS5m").getValue();
        desc[11] = GetByName("nR10").getValue();
        desc[12] = GetByName("X3A").getValue();
        desc[13] = GetByName("X3sol").getValue();
        desc[14] = -999; // LDA classification result, to be set from outside
        return desc;
    }
    
}
