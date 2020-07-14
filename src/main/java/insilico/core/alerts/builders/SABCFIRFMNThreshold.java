package insilico.core.alerts.builders;

import insilico.core.alerts.Alert;
import insilico.core.alerts.AlertBlock;
import insilico.core.alerts.AlertEncoding;
import insilico.core.alerts.AlertList;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class SABCFIRFMNThreshold extends AlertBlock {

    private Double MW;
    private Double LogP;
    
    
    public SABCFIRFMNThreshold() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_BCF_IRFMN_THRESHOLD, "IRFMN property-based rule set for BCF");
    }

    @Override
    protected void BuildSAList() throws InitFailureException {

        // No. 01
        
        Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, 1));
        curSA.setName("MNPB" + 1);
        curSA.setDescription("IRFMN property-based alert n. 1 for BCF: " + 
                "compounds with logP < 3 have BCF < 3");

        // Threshold value of alerts
        curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 3.0);
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_UPPER_THRESHOLD, true);
        
        Alerts.add(curSA);

        
        // No. 02
        
        curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, 2));
        curSA.setName("MNPB" + 2);
        curSA.setDescription("IRFMN property-based alert n. 2 for BCF: " + 
                "compounds with MW > 600 have BCF < 3.3");

        // Threshold value of alerts
        curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_MEAN, 3.3);
        curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_IS_UPPER_THRESHOLD, true);
        
        Alerts.add(curSA);
        
    }

    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        if ((MW == null) || (LogP == null))
            throw new GenericFailureException("Logp or MW not set");
        
        AlertList Res = new AlertList();
        
        try {
    
            // No. 1
            if (LogP < 3)
                Res.add((Alert)Alerts.get(0).clone());
            
            // No. 2
            if (MW > 600)
                Res.add((Alert)Alerts.get(1).clone());
            
        } catch (CloneNotSupportedException e) {
            return null;
        }
        
        return Res;
    }

    /**
     * @return the MW
     */
    public Double getMW() {
        return MW;
    }

    /**
     * @param MW the MW to set
     */
    public void setMW(Double MW) {
        this.MW = MW;
    }

    /**
     * @return the LogP
     */
    public Double getLogP() {
        return LogP;
    }

    /**
     * @param LogP the LogP to set
     */
    public void setLogP(Double LogP) {
        this.LogP = LogP;
    }
    
}
