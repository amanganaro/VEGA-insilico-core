package insilico.core.molecule.acf;

import java.io.Serializable;

/**
 * Wrapper for info on a single ACF item.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ACFItem implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private String ACF;
    private int frequency;

    public ACFItem(){
        this.ACF = "";
        this.frequency = 0;
    }

    public ACFItem(String fragment, int frequency) {
        this.ACF = fragment;
        this.frequency = frequency;
    }

    /**
     * @return the ACF
     */
    public String getACF() {
        return ACF;
    }

    /**
     * @param ACF the ACF to set
     */
    public void setACF(String ACF) {
        this.ACF = ACF;
    }

    /**
     * @return the Frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @param Frequency the Frequency to set
     */
    public void setFrequency(int Frequency) {
        this.frequency = frequency;
    }

    public Object Clone() throws CloneNotSupportedException {
        return(super.clone());
    }
}
