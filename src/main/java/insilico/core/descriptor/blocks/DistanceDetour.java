package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.tools.logger.InsilicoLogger;
import org.openscience.cdk.RingSet;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IRing;

import java.util.Arrays;

/**
 * Distance/Detour descriptors.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DistanceDetour extends DescriptorBlock {

    private final static long serialVersionUID = 1L;
    private final static String BlockName = "Distance/Detour Descriptors";

    private final static int MinRingSize = 3;
    private final static int MaxRingSize = 12;


    /**
     * Constructor. This should not be used, no weight is specified. The 
     * overloaded constructors should be used instead.
     */    
    public DistanceDetour() {
        super();
        this.Name = DistanceDetour.BlockName;
    }

    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        for (int i=MinRingSize; i<=MaxRingSize; i++) 
            Add("D/Dr" + i, "");
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
        double[][] DDrMatrix = null;
        try {
            DDrMatrix = mol.GetMatrixDistanceDetour();
        } catch (GenericFailureException e) {
            InsilicoLogger.getLogger().warn(e);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
                    
        double[] DDr = new double[MaxRingSize+1];
        
        try {
            RingSet MolRings = mol.GetAllRings();

            Arrays.fill(DDr, 0);

            for (int i=0; i< MolRings.getAtomContainerCount(); i++) {
                IRing r = (IRing) MolRings.getAtomContainer(i);
                int rSize = r.getAtomCount();
                
                if ((rSize >= MinRingSize) && (rSize <= MaxRingSize)) {
                    for (IAtom at : r.atoms()) {
                        int atNum = m.getAtomNumber(at);
                        double rowSum = 0;
                        for (int k=0; k<DDrMatrix[atNum].length; k++)
                            rowSum += DDrMatrix[atNum][k];
                        DDr[rSize]  += rowSum;    
                    }
                }

            }

            for (int i=MinRingSize; i<=MaxRingSize; i++) 
                SetByName("D/Dr" + i, DDr[i]);
            
        } catch (Exception e) {
            SetAllValues(Descriptor.MISSING_VALUE);
        }

    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        DistanceDetour block = new DistanceDetour();
        block.CloneDetailsFrom(this);
        return block;
    }

    
}
