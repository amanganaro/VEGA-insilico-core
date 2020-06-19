package insilico.core.ad.reasoining;

import insilico.core.constant.InsilicoConstants;

import java.io.Serializable;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public abstract class DescriptorAnalysis implements iReasoningItem, Serializable {

    private final int DescriptorIndex;
    private double DescriptorValue;


    public DescriptorAnalysis(int DescriptorIdx) {
        this.DescriptorIndex = DescriptorIdx;
        DescriptorValue = 0.0;
    }


    @Override
    public short getReasoningItemType() {
        return InsilicoConstants.REASONING_DESCRIPTOR_ANALYSIS;
    }


    /**
     * @return the DescriptorValue
     */
    public double getDescriptorValue() {
        return DescriptorValue;
    }

    /**
     * @param DescriptorValue the DescriptorValue to set
     */
    public void setDescriptorValue(double DescriptorValue) {
        this.DescriptorValue = DescriptorValue;
    }

    /**
     * @return
     */
    public int getDescriptorIndex() {
        return DescriptorIndex;
    }


    abstract public String getDescriptorName();
    abstract public String getExpName();
    abstract public String getDescription();

}
