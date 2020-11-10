package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.Arrays;

/**
 * Topological distances / 2D atom pairs descriptors.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class AtomPairs2D extends DescriptorBlock {

    private final static long serialVersionUID = 1L;

    private final static String BlockName = "2D Atom Pairs Descriptors";
    private final static int MAX_TOPO_DISTANCE = 10;

    private final static String[][] ATOM_COUPLES = {
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
    
    
    
    /**
     * Constructor. This should not be used, no weight is specified. The 
     * overloaded constructors should be used instead.
     */    
    public AtomPairs2D() {
        super();
        this.Name = AtomPairs2D.BlockName;
    }

    
    
    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        for (int i = 0; i< ATOM_COUPLES.length; i++) {
            // sum of topo distances are not calculated on pairs involving C atom
            if ( (ATOM_COUPLES[i][0].equalsIgnoreCase("C")) || (ATOM_COUPLES[i][1].equalsIgnoreCase("C"))  )
                continue;
            Add("T(" + ATOM_COUPLES[i][0] + ".." + ATOM_COUPLES[i][1] + ")", "sum of topological distances between " + ATOM_COUPLES[i][0] + "-" + ATOM_COUPLES[i][1]) ;
        }
        for (int lag=1; lag<= MAX_TOPO_DISTANCE; lag++)
            for (int i = 0; i< ATOM_COUPLES.length; i++) {
                Add("B" + (lag<10 ? ("0"+lag) : lag) + "[" + ATOM_COUPLES[i][0] + "-" + ATOM_COUPLES[i][1] + "]", "presence/absence of "+ ATOM_COUPLES[i][0] + "-" + ATOM_COUPLES[i][1] + " at topological distance " + lag);
            }
        for (int lag=1; lag<= MAX_TOPO_DISTANCE; lag++)
            for (int i = 0; i< ATOM_COUPLES.length; i++) {
                Add("F" + (lag<10 ? ("0"+lag) : lag) + "[" + ATOM_COUPLES[i][0] + "-" + ATOM_COUPLES[i][1] + "]", "frequency of "+ ATOM_COUPLES[i][0] + "-" + ATOM_COUPLES[i][1] + " at topological distance " + lag);
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

        IAtomContainer m;
        try {
            m = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        int nSK = m.getAtomCount();

        // Gets matrices
        int[][] TopoMat = null;
        try {
            TopoMat = mol.GetMatrixTopologicalDistance();
        } catch (GenericFailureException e) {
            log.warn("Invalid structure, unable to calculate: " + this.Name);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        
        for (int d = 0; d< ATOM_COUPLES.length; d++) {
        
            int descT = 0;
            int[] descB = new int[MAX_TOPO_DISTANCE];
            int[] descF = new int[MAX_TOPO_DISTANCE];
            Arrays.fill(descB, 0);
            Arrays.fill(descF, 0);
            
            for (int i=0; i<nSK; i++) {
                if (m.getAtom(i).getSymbol().equalsIgnoreCase(ATOM_COUPLES[d][0])) {
                    for (int j=0; j<nSK; j++) {
                        if (i==j) continue;
                        if (m.getAtom(j).getSymbol().equalsIgnoreCase(ATOM_COUPLES[d][1])) {

                            // T (sum of topo distances)
                            if (TopoMat[i][j] > 2) // DA VEDERE PERCHE MAGGIORE DI 2
                                descT += TopoMat[i][j];
                            
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
            if (ATOM_COUPLES[d][0].compareTo(ATOM_COUPLES[d][1]) == 0) {
                descT /= 2;
                for (int i=0; i<descF.length; i++)
                    descF[i] /= 2;
            }

            SetByName("T(" + ATOM_COUPLES[d][0] + ".." + ATOM_COUPLES[d][1] + ")", descT);
            for (int i=0; i<descB.length; i++) {
                int lag = i+1;
                SetByName("B" + (lag<10 ? ("0"+lag) : lag) + "[" + ATOM_COUPLES[d][0] + "-" + ATOM_COUPLES[d][1] + "]", descB[i]);
                SetByName("F" + (lag<10 ? ("0"+lag) : lag) + "[" + ATOM_COUPLES[d][0] + "-" + ATOM_COUPLES[d][1] + "]", descF[i]);
            }


        }

    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        AtomPairs2D block = new AtomPairs2D();
        block.CloneDetailsFrom(this);
        return block;
    }

    
}
