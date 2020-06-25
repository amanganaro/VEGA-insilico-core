package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.weight.VertexDegree;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.tools.logger.InsilicoLogger;
import insilico.core.tools.utils.MoleculeUtilities;
import org.openscience.cdk.Atom;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Information Content descriptors.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class InformationContent extends DescriptorBlock {
    
    private static final long serialVersionUID = 1L;
    private final static String BlockName = "Information Content";

    public final static String PARAMETER_MAX_LAG_01 = "ml01";
    public final static String PARAMETER_MAX_LAG_02 = "ml02";
    public final static String PARAMETER_MAX_LAG_03 = "ml03";
    public final static String PARAMETER_MAX_LAG_04 = "ml04";
    public final static String PARAMETER_MAX_LAG_05 = "ml05";
    
    

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
        int MaxPath = RetrieveMaxLag();
        Add("IDE","");
        Add("IVDE","");
        Add("IVDEM",""); // (implementation from TEST software)
        Add("IDWBAR","");
        for (int i=1; i<(MaxPath+1); i++) {
            Add("IC" + i, "");
            Add("CIC" + i, "");
            Add("BIC" + i, "");
        }
        SetAllValues(Descriptor.MISSING_VALUE);
    }
    
    
    private int RetrieveMaxLag() {
        int MaxLag = 1;
        if (getBoolProperty(PARAMETER_MAX_LAG_01))
            MaxLag = 1;
        if (getBoolProperty(PARAMETER_MAX_LAG_02))
            MaxLag = 2;
        if (getBoolProperty(PARAMETER_MAX_LAG_03))
            MaxLag = 3;
        if (getBoolProperty(PARAMETER_MAX_LAG_04))
            MaxLag = 4;
        if (getBoolProperty(PARAMETER_MAX_LAG_05))
            MaxLag = 5;
        return MaxLag;
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
        
        // Retrieves parameter
        int MaxPath = RetrieveMaxLag();

        IAtomContainer m;
        try {
            m = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        
        // Gets matrices
        double[][] ConnMat;
        int[][] TopoDistMat;
        try {
            ConnMat = mol.GetMatrixConnectionAugmented();
            TopoDistMat = mol.GetMatrixTopologicalDistance();
        } catch (GenericFailureException e) {
            InsilicoLogger.getLogger().warn(e);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
                
        int nSK = m.getAtomCount();
        
        int[] VertexDeg = VertexDegree.getWeights(m, true);
        
        
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
        
        
        // Neighborhood indices
        
        double bic_denom = 0;
        for (IBond bnd : m.bonds())
            bic_denom += MoleculeUtilities.Bond2Double(bnd);
        bic_denom = Math.log(bic_denom) / Math.log(2);
        
        for (int CurLag=1; CurLag<=MaxPath; CurLag++) {

            // Create belonging class for each atom(vertex)
            ArrayList<ArrayList<String>> NeigList = new ArrayList<>(nSK);
            for (int i=0; i<nSK; i++) {
                Atom atStart = (Atom) m.getAtom(i);
                ArrayList<String> CurNeig = new ArrayList<>();
                for (int j=0; j<nSK; j++) {
                    if (i==j) continue;
                    if (TopoDistMat[i][j] == CurLag) {
                        Atom atEnd = (Atom) m.getAtom(j);
                        List<IAtom> sp = PathTools.getShortestPath(m, atStart, atEnd);
                        String bufPath = "" + sp.get(0).getAtomicNumber();  
                        for (int k=0; k<(sp.size()-1); k++) {
                            int a = m.getAtomNumber(sp.get(k));
                            int b = m.getAtomNumber(sp.get(k+1));
                            if (ConnMat[a][b] == 1)
                                bufPath += "s";
                            if (ConnMat[a][b] == 2)
                                bufPath += "d";
                            if (ConnMat[a][b] == 3)
                                bufPath += "t";
                            if (ConnMat[a][b] == 1.5)
                                bufPath += "a";
                            bufPath += sp.get(k+1).getAtomicNumber();                            
                            bufPath += "(" + VertexDeg[m.getAtomNumber(sp.get(k+1))] + ")";  
                        }
                        CurNeig.add(bufPath);
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
            
            double cic = (Math.log(nSK) / Math.log(2)) - ic;
            
            double bic = bic_denom==0 ? 0 : ic / bic_denom;
            
            SetByName("IC" + CurLag, ic);
            SetByName("CIC" + CurLag, cic);
            SetByName("BIC" + CurLag, bic);
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
