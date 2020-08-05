package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.weight.EState;
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
public class TopologicalEState extends DescriptorBlock {
    
    private static final long serialVersionUID = 1L;

    Logger logger = LoggerFactory.getLogger(TopologicalEState.class);

    private static final String BlockName = "Topological E-State Descriptors";

    
    
    /**
     * Constructor.
     */
    public TopologicalEState() {
        super();
        this.Name = TopologicalEState.BlockName;
    }
    
        
    
    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        this.Add("MAXDN", "");
        this.Add("MAXDP", "");

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
        
        // Get weights
        EState ES;
        try {
            ES = new EState(mol.GetStructure());
        } catch (Exception e) {
            logger.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        double[] IStates = ES.getIS();
        
        // Get matrices
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
            double maxDN = 0, maxDP = 0;

            for (int i=0; i<nSK; i++) {
                double Delta = 0;
                for (int j=0; j<nSK; j++) {
                    if (i==j) continue;
                    if ( (IStates[i]!= Descriptor.MISSING_VALUE) && (IStates[j]!= Descriptor.MISSING_VALUE) )
                        Delta += (IStates[i]-IStates[j]) / Math.pow(TopoMat[i][j]+1, 2);
                }
                if (Delta<0) {
                    if (Delta<maxDN)
                        maxDN = Delta;
                } else {
                    if (Delta>maxDP)
                        maxDP = Delta;
                }
            }
            
            

            this.SetByName("MAXDN", Math.abs(maxDN) );
            this.SetByName("MAXDP", maxDP );

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
        TopologicalEState block = new TopologicalEState();
        block.CloneDetailsFrom(block);
        return block;
    }

    
}
