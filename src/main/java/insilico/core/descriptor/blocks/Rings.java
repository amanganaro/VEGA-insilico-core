package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;

import java.util.Iterator;

/**
 * Rings descriptors block.<p>
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class Rings extends DescriptorBlock {
    
    private static final long serialVersionUID = 1L;
    private static final String BlockName = "Rings Descriptors";

    private static final int RING_SIZE_MIN = 3;
    private static final int RING_SIZE_MAX = 11;
    
    
    /**
     * Constructor. 
     */
    public Rings() {
        super();
        this.Name = Rings.BlockName;
    }
    
        
    
    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        for (int i=RING_SIZE_MIN; i<=RING_SIZE_MAX; i++)
            this.Add("nR" + i, "");
        SetAllValues(Descriptor.MISSING_VALUE);
    }
    
    
    /**
     * Calculate descriptors for the given molecule.
     * 
     * Note: rings are counted on all-rings perception, and not on SSSR
     * 
     * @param mol molecule to be calculated
     */
    @Override
    public void Calculate(InsilicoMolecule mol) {

        // Generate/clears descriptors
        GenerateDescriptors();
        
        try {
        
            int nSizes = RING_SIZE_MAX - RING_SIZE_MIN + 1;
            int[] RingCount = new int[nSizes];
            int[] RingSize = new int[nSizes];
            for (int i=0; i<nSizes; i++) {
                RingSize[i] = RING_SIZE_MIN + i;
                RingCount[i] = 0;
            }
            
            IRingSet allRings = mol.GetAllRings();
            Iterator<IAtomContainer> RingsIterator = allRings.atomContainers().iterator();
            while (RingsIterator.hasNext()) {
                IRing ring = (IRing)RingsIterator.next();
                for (int i=0; i<nSizes; i++) {
                    if (ring.getAtomCount() == RingSize[i])
                        RingCount[i]++;
                }
            }
            
            for (int i=0; i<nSizes; i++) 
                SetByName("nR" + RingSize[i], RingCount[i]);
            
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
        Rings block = new Rings();
        block.CloneDetailsFrom(block);
        return block;
    }

    
}
