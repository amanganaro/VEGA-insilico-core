package insilico.core.descriptor.blocks.old;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.blocks.old.weight.VertexDegree;
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
 * Information Content descriptors. Version on molecule with HYDROGENS
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class InformationContentWithH extends DescriptorBlock {
    
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
    public InformationContentWithH() {
        super();
        this.Name = InformationContentWithH.BlockName;
    }
    
    
    
    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        int MaxPath = RetrieveMaxLag();
        for (int i=1; i<(MaxPath+1); i++) {
            Add("IC" + i, "");
            Add("CIC" + i, "");
            Add("BIC" + i, "");
            Add("SIC" + i, "");
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
            IAtomContainer orig_m = mol.GetStructure();
            m = Manipulator.AddHydrogens(orig_m);
        } catch (InvalidMoleculeException | GenericFailureException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        
        // Gets matrices
        double[][] ConnMat;
        int[][] TopoDistMat;
        try {
            ConnMat = ConnectionAugMatrix.getMatrix(m);
            TopoDistMat = TopoDistanceMatrix.getMatrix(m);
        } catch (Exception e) {
            log.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
                
        int nSK = m.getAtomCount();
        
        int[] VertexDeg = VertexDegree.getWeights(m, true);
        
        
                   
        
        
        // Neighborhood indices
        
        double bic_denom = 0;
        for (IBond bnd : m.bonds())
            bic_denom += MoleculeUtilities.Bond2Double(bnd);
        bic_denom = Math.log(bic_denom) / Math.log(2);
        
        for (int CurLag=1; CurLag<=MaxPath; CurLag++) {

            // Create belonging class for each atom(vertex)
            ArrayList<ArrayList<String>> NeigList = new ArrayList<>(nSK);
            for (int i=0; i<nSK; i++) {
                IAtom atStart =  m.getAtom(i);
                ArrayList<String> CurNeig = new ArrayList<>();
                for (int j=0; j<nSK; j++) {
                    if (i==j) continue;
                    if (TopoDistMat[i][j] == CurLag) {
                        IAtom atEnd =  m.getAtom(j);
                        ShortestPaths shortestPaths = new ShortestPaths(m, atStart);
                        List<IAtom> sp = Arrays.asList(shortestPaths.atomsTo(atEnd));
                        // DEPRECATED METHOD
//                        List<IAtom> sp = PathTools.getShortestPath(m, atStart, atEnd);

                        StringBuilder bufPath = new StringBuilder("" + sp.get(0).getAtomicNumber());
                        for (int k=0; k<(sp.size()-1); k++) {
                            int a = m.indexOf(sp.get(k));
                            int b = m.indexOf(sp.get(k + 1));
                            if (ConnMat[a][b] == 1)
                                bufPath.append("s");
                            if (ConnMat[a][b] == 2)
                                bufPath.append("d");
                            if (ConnMat[a][b] == 3)
                                bufPath.append("t");
                            if (ConnMat[a][b] == 1.5)
                                bufPath.append("a");
                            bufPath.append(sp.get(k + 1).getAtomicNumber());
                            bufPath.append("(").append(VertexDeg[m.indexOf(sp.get(k + 1))]).append(")");
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
    
    
    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        InformationContentWithH block = new InformationContentWithH();
        block.CloneDetailsFrom(this);
        return block;
    }

}
