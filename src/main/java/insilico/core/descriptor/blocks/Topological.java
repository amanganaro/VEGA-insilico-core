package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.weight.VertexDegree;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Topological descriptors block.<p>
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class Topological extends DescriptorBlock {
    
    private static final long serialVersionUID = 1L;

    Logger logger = LoggerFactory.getLogger(Topological.class);

    private static final String BlockName = "Topological Descriptors";

    
    
    /**
     * Constructor.
     */
    public Topological() {
        super();
        this.Name = Topological.BlockName;
    }
    
        
    
    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        this.Add("W", "");
        this.Add("WA", "");

        this.Add("SNar", "");
        this.Add("HNar", "");
        this.Add("GNar", "");

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

        IAtomContainer curMol;
        try {
            curMol = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        
        // Gets matrices
        int[][] TopoMat = null;
        try {
            TopoMat = mol.GetMatrixTopologicalDistance();
        } catch (GenericFailureException e) {
            logger.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        
        try {

            int nSK = curMol.getAtomCount();
            int[] VD = VertexDegree.getWeights(curMol, true);

            // Wiener index
            double W = 0;
            for (int i=0; i<nSK; i++)
                for (int j=0; j<nSK; j++) {
                    if (i==j) continue;
                    W += TopoMat[i][j];
                }
            this.SetByName("W",  W/2);
            this.SetByName("WA",  W/(nSK*(nSK-1)));


            // Narumi indices
            double SNar=1, HNar=0, GNar=1;
            for (int i=0; i<nSK; i++) {
                SNar *= VD[i];
                HNar += 1.00/VD[i];
            }        
            HNar = nSK / HNar;
            GNar = Math.pow(SNar, 1.00/nSK);

            this.SetByName("SNar",  Math.log(SNar));
            this.SetByName("HNar", HNar);
            this.SetByName("GNar", GNar);

        } catch (Throwable e) {
            this.SetAllValues(Descriptor.MISSING_VALUE);
        }
        
    }    

    
    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone()
            throws CloneNotSupportedException {
        Topological block = new Topological();
        block.CloneDetailsFrom(block);
        return block;
    }

    
}
