package insilico.core.ad.reasoining;

import insilico.core.constant.InsilicoConstants;
import insilico.core.molecule.acf.ACFItem;
import insilico.core.molecule.acf.ACFItemList;

/**
 *
 * @author Alberto Manganaro <a.manganaro@kode-solutions.net>
 */
public class ACFAnalysis implements iReasoningItem {

    private final String[] Fragments;
    private final short[] FragmentsType;


    public ACFAnalysis(ACFItemList ACFRare, ACFItemList ACFMissing) {

        Fragments = new String[ACFRare.size() + ACFMissing.size()];
        FragmentsType = new short[ACFRare.size() + ACFMissing.size()];

        int idx = 0;
        for (ACFItem acf : ACFRare.getList()) {
            Fragments[idx] = acf.getACF();
            FragmentsType[idx] = InsilicoConstants.ACF_TYPE_RARE;
            idx++;
        }
        for (ACFItem acf : ACFMissing.getList()) {
            Fragments[idx] = acf.getACF();
            FragmentsType[idx] = InsilicoConstants.ACF_TYPE_MISSING;
            idx++;
        }
    }

    @Override
    public short getReasoningItemType() {
        return InsilicoConstants.REASONING_ACF_ANALYSIS;
    }

    /**
     * @return the Fragments
     */
    public String[] getFragments() {
        return Fragments;
    }

    /**
     * @return the FragmentsType
     */
    public short[] getFragmentsType() {
        return FragmentsType;
    }
}
