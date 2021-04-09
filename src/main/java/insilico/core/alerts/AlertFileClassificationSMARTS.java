package insilico.core.alerts;

import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelector;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

/**
 * Utility class for reading a file with SMARTS rules, to be used as alerts.
 * The file must containt the following fields, plus the header (ignored while
 * reading data):
 * no.  number of the fragment (just for the file, the info is ignored)
 * SMARTS   string for the SMARTS
 * Toxicity	toxicity (1=toxic, else non-toxic)
 * Accuracy	accuracy of the rule/fragment [1,0]
 * Fisher P	P-value for the Fisher test
 * Parents  parent fragments, as list of their number separed by comma
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class AlertFileClassificationSMARTS {
    
    private final int Size;
    private final String[] SMARTS;
    private final boolean[] Toxicity; 
    private final double[] Accuracy;
    private final double[] FisherPValue;
    private final ArrayList<Integer>[] ParentAlerts;
    
    
    public AlertFileClassificationSMARTS (InputStream SourceFile) throws IOException, GenericFailureException {

        DataInputStream in = new DataInputStream(SourceFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        ArrayList<String> Lines = new ArrayList<>();
        
        String CurLine = br.readLine();  // First line - just headers
        
        while ((CurLine = br.readLine()) != null) 
            Lines.add(CurLine);
        
        SMARTS = new String[Lines.size()];
        Toxicity = new boolean[Lines.size()];
        Accuracy = new double[Lines.size()];
        FisherPValue = new double[Lines.size()];
        ParentAlerts = new ArrayList[Lines.size()];
        
        for (int i=0; i< Lines.size(); i++) {
            try {
                String[] CurValues = Lines.get(i).split("\\t");
                // First column is just the number - not used
                SMARTS[i] = CurValues[1];
                Toxicity[i] = (Integer.parseInt(CurValues[2]) == 1);
                Accuracy[i] = Double.parseDouble(CurValues[3]);
                FisherPValue[i] = Double.parseDouble(CurValues[4]);
                ParentAlerts[i] = new ArrayList<>();
                if (CurValues.length > 5) {
                    String[] CurParents = CurValues[5].split(",");
                    for (String s : CurParents)
                        ParentAlerts[i].add(Integer.valueOf(s));
                }
            } catch (NumberFormatException e) {
                log.error(String.format(StringSelector.getString("sa_smarts_input_file_error"), i+1, e.getMessage()));
                throw new GenericFailureException(StringSelector.getString("sa_alert_numeric_conversion_error"));
            }
        }
        
        Size = Lines.size();
    }

    /**
     * @return the SMARTS
     */
    public String[] getSMARTS() {
        return SMARTS;
    }

    /**
     * @return the Accuracy
     */
    public double[] getAccuracy() {
        return Accuracy;
    }

    /**
     * @return the FisherPValue
     */
    public double[] getFisherPValue() {
        return FisherPValue;
    }

    /**
     * @return the ParentAlerts
     */
    public ArrayList<Integer>[] getParentAlerts() {
        return ParentAlerts;
    }

    /**
     * @return the Size
     */
    public int getSize() {
        return Size;
    }

    /**
     * @return the Toxicity
     */
    public boolean[] getToxicity() {
        return Toxicity;
    }
    
}
