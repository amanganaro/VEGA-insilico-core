package insilico.core.similarity;

import lombok.Data;

import java.io.Serializable;
import java.util.BitSet;

@Data
public class SimilarityDescriptors implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;


    public double[] Constitutional;
    public double[] HeteroAtoms;
    public double[] FunctionalGroups;
    public BitSet Fingerprint;


    /**
     * Constructor.
     */
    public SimilarityDescriptors() {
        Constitutional = null;
        HeteroAtoms = null;
        FunctionalGroups = null;
        Fingerprint = null;
    }


    public Object Clone() throws CloneNotSupportedException {
        SimilarityDescriptors newSim = (SimilarityDescriptors)super.clone();
        newSim.Fingerprint = (BitSet)this.Fingerprint.clone();
        return((Object)newSim);
    }
}
