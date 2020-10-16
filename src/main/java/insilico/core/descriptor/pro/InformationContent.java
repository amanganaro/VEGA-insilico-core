package insilico.core.descriptor.pro;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.pro.weights.other.WeightsVertexDegree;
import insilico.core.descriptor.weight.VertexDegree;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.matrix.ConnectionAugMatrix;
import insilico.core.molecule.matrix.TopoDistanceMatrix;
import insilico.core.molecule.tools.Manipulator;
import insilico.core.tools.utils.MoleculeUtilities;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.graph.ShortestPaths;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Information Content descriptors.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class InformationContent extends DescriptorBlock {
    
    private static final long serialVersionUID = 1L;
    private final static String BlockName = "Information Content";
    private final static int MAXLAG = 5;
    

    /**
     * Constructor. Sets the default value for the max path (5)
     */    
    public InformationContent() {
        super();
        this.Name = InformationContent.BlockName;
    }
    
    
    
    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        Add("IDE","mean information content on the distance equality");  // H-depleted
        // IDET? H-depleted
        Add("IVDE",""); // H-depleted
        Add("IVDEM",""); // (implementation from TEST software)
        Add("IDWBAR","");
        for (int i=1; i<(MAXLAG+1); i++) {
            Add("IC" + i, "Information Content index (neighborhood symmetry of " + i + "-order)");  // H
            // MANCA TIC?
            Add("SIC" + i, "Structural Information Content index (neighborhood symmetry of " + i + "-order)"); // H
            Add("CIC" + i, "Complementary Information Content index (neighborhood symmetry of " + i + "-order)"); // H
            Add("BIC" + i, "Bond Information Content index (neighborhood symmetry of " + i + "-order)"); // H
        }
        SetAllValues(Descriptor.MISSING_VALUE);
    }

    
    /**
     * Calculate descriptors for the given molecule.
     * 
     * @param mol molecule to be calculated
     */
    @Override
    public void Calculate(InsilicoMolecule mol) {

        // Generate/clears descriptors
        GenerateDescriptors();


        //// Descriptors based on H-depleted structure

        IAtomContainer curMol;
        try {
            curMol = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            log.warn("Invalid structure, unable to calculate: " + this.Name);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Gets matrices
        int[][] TopoDistMat;
        try {
            TopoDistMat = mol.GetMatrixTopologicalDistance();
        } catch (GenericFailureException e) {
            log.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
                
        int nSK = curMol.getAtomCount();
        int[] VertexDeg = WeightsVertexDegree.getWeights(curMol, false);

        // Information content
        
        int[] TopoDistFreq = new int[nSK];  // frequencies of topological distances
        double TopoDistFreqSum = 0; 
        for (int i=0; i<nSK; i++)
            TopoDistFreq[i] = 0;
        for (int i=0; i<nSK; i++)
            for (int j=i+1; j<nSK; j++) {
                TopoDistFreq[TopoDistMat[i][j]]++;
                TopoDistFreqSum += TopoDistMat[i][j];
            }
        
        // IDE
        double IDE = 0;
        double denom = (double)nSK*(nSK-1)/2.00;
        for (int i=0; i<nSK; i++)
            if (TopoDistFreq[i]>0)
                IDE += ((double)TopoDistFreq[i]/denom) * (Math.log((double)TopoDistFreq[i]/denom));
        IDE = (-1.00 / Math.log(2)) * IDE;
        
        SetByName("IDE", IDE);

        // IVDE
        double IVDE = 0;
        
        int[] VerDegCount = new int[10];
        for (int i=0; i<10; i++) VerDegCount[i] = 0;
        for (int i=0; i<nSK; i++) {
            VerDegCount[VertexDeg[i]]++;
        }
        
        for (int i=0; i<10; i++) {
            if (VerDegCount[i]>0)
                IVDE = IVDE - ( ((double)VerDegCount[i]) / ((double)nSK)) * Math.log( ((double)VerDegCount[i]) / ((double)nSK) );
        }
        IVDE = (1.0 / Math.log(2)) * IVDE;
        
        SetByName("IVDE", IVDE);
        
        // IVDEM (implementation from TEST software)
        double IVDEM = 0;
        
        for (int i=0; i<10; i++) {
            if (VerDegCount[i]>0)
                IVDEM = IVDEM - ( ( (double)VerDegCount[i] / (double)nSK ) * Log(2, ( (double)VerDegCount[i] / (double)nSK ) ) );
        }
        
        SetByName("IVDEM", IVDEM);
        
        // Idwbar (Bonchev-Trinajstic mean information content)
        double partial=0;
        for (int j=1; j<nSK; j++) 
            partial += (TopoDistFreq[j]) * j * (Math.log(j)/Math.log(2));
        double idw = TopoDistFreqSum * (Math.log(TopoDistFreqSum)/Math.log(2)) - partial;
        double idwbar = idw / TopoDistFreqSum;

        SetByName("IDWBAR", idwbar);                    
        


        //// Neighborhood indices - on H-filled molecule

        try {
            IAtomContainer orig_m = mol.GetStructure();
            curMol = Manipulator.AddHydrogens(orig_m);
        } catch (InvalidMoleculeException | GenericFailureException e) {
            log.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Gets matrices
        double[][] ConnMat;
        try {
            ConnMat = ConnectionAugMatrix.getMatrix(curMol);
            TopoDistMat = TopoDistanceMatrix.getMatrix(curMol);
        } catch (Exception e) {
            log.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        nSK = curMol.getAtomCount();

        double bic_denom = 0;
        for (IBond bnd : curMol.bonds())
            bic_denom += MoleculeUtilities.Bond2Double(bnd);
        bic_denom = Math.log(bic_denom) / Math.log(2);

        for (int CurLag=1; CurLag<=MAXLAG; CurLag++) {

            // Create belonging class for each atom(vertex)
            ArrayList<ArrayList<String>> NeigList = new ArrayList<>(nSK);
            for (int i=0; i<nSK; i++) {
                IAtom atStart =  curMol.getAtom(i);
                ArrayList<String> CurNeig = new ArrayList<>();
                for (int j=0; j<nSK; j++) {
                    if (i==j) continue;
                    if (TopoDistMat[i][j] == CurLag) {
                        IAtom atEnd =  curMol.getAtom(j);
                        ShortestPaths shortestPaths = new ShortestPaths(curMol, atStart);
                        List<IAtom> sp = Arrays.asList(shortestPaths.atomsTo(atEnd));
                        StringBuilder bufPath = new StringBuilder("" + sp.get(0).getAtomicNumber());
                        for (int k=0; k<(sp.size()-1); k++) {
                            int a = curMol.indexOf(sp.get(k));
                            int b = curMol.indexOf(sp.get(k + 1));
                            if (ConnMat[a][b] == 1)
                                bufPath.append("s");
                            if (ConnMat[a][b] == 2)
                                bufPath.append("d");
                            if (ConnMat[a][b] == 3)
                                bufPath.append("t");
                            if (ConnMat[a][b] == 1.5)
                                bufPath.append("a");
                            bufPath.append(sp.get(k + 1).getAtomicNumber());
                            bufPath.append("(").append(VertexDeg[curMol.indexOf(sp.get(k + 1))]).append(")");
                        }
                        CurNeig.add(bufPath.toString());
                    }
                }
                Collections.sort(CurNeig);
                NeigList.add(CurNeig);
            }

            // Calculates equivalence classes
            ArrayList<ArrayList<String>> G = new ArrayList<>();
            ArrayList<Integer> Gn = new ArrayList<>();
            for (int i=0; i<nSK; i++) {
                ArrayList<String> CurNeig = NeigList.get(i);
                boolean foundMatch = false;
                for (int k=0; k<G.size(); k++) {
                    if (CompareNeigVector(CurNeig, G.get(k))) {
                        foundMatch = true;
                        int buf = Gn.get(k);
                        Gn.set(k, (buf+1));
                        break;
                    }
                }
                if (!foundMatch) {
                    G.add(CurNeig);
                    Gn.add(1);
                }
            }

            // Calculate IC and CIC indices

            double ic=0;
            for (int i=0; i<Gn.size(); i++)
                ic += ((double)Gn.get(i)/nSK) * (Math.log((double)Gn.get(i)/nSK));
            ic = (-1.00 / Math.log(2)) * ic;

            double diff = Math.log(nSK) / Math.log(2);

            double cic = diff - ic;

            double bic = bic_denom==0 ? 0 : ic / bic_denom;

            double sic = ic / diff;

            SetByName("IC" + CurLag, ic);
            SetByName("CIC" + CurLag, cic);
            SetByName("BIC" + CurLag, bic);
            SetByName("SIC" + CurLag, sic);
        }        

    }

    
    private boolean CompareNeigVector(ArrayList<String> A, ArrayList<String> B) {
        
        // note: integer in the vectors should be already sorted 
        
        if (A.size()!=B.size())
            return false;
        else {
            for (int i=0; i<A.size(); i++)
                if (!A.get(i).equalsIgnoreCase(B.get(i)))
                    return false;
        }
        
        return true;
    }
    

    private static double Log(int base,double x) {
        double Logbx = Math.log10(x)/Math.log10((double)base);
        return Logbx;
    }        
    
    
    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        InformationContent block = new InformationContent();
        block.CloneDetailsFrom(this);
        return block;
    }

}
