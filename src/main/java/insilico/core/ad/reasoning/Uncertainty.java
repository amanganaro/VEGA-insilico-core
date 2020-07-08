package insilico.core.ad.reasoning;

import insilico.core.constant.InsilicoConstants;

import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class Uncertainty implements iReasoningItem {

    protected final ArrayList<UncertaintyClassBar> Bars;


    public Uncertainty() {
        this.Bars = new ArrayList<>();
    }

    @Override
    public short getReasoningItemType() {
        return InsilicoConstants.REASONING_UNCERTAINTY;
    }


    /**
     * @return the Bars
     */
    public ArrayList<UncertaintyClassBar> getBars() {
        return Bars;
    }
}