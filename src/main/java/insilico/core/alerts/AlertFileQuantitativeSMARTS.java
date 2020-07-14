package insilico.core.alerts;

import insilico.core.exception.GenericFailureException;
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
 * Mean value   mean value of property
 * StdDev   standard deviation of property
 * Hits the number of hits (matches)
 * Parents  parent fragments, as list of their number separed by comma
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class AlertFileQuantitativeSMARTS {

    private Logger logger = LoggerFactory.getLogger(AlertFileQuantitativeSMARTS.class);


    private final int Size;
    private final String[] SMARTS;
    private final double[] Mean; 
    private final double[] Stdev;
    private final int[] Hits;
    private final ArrayList<Integer>[] ParentAlerts;
    private final String [] Description;
    
    
    public AlertFileQuantitativeSMARTS (InputStream SourceFile) throws IOException, GenericFailureException {
        DataInputStream in = new DataInputStream(SourceFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        ArrayList<String> Lines = new ArrayList<>();
        
        String CurLine = br.readLine();  // First line - just headers
        
        while ((CurLine = br.readLine()) != null) 
            Lines.add(CurLine);
        
        SMARTS = new String[Lines.size()];
        Mean = new double[Lines.size()];
        Stdev = new double[Lines.size()];
        Hits = new int[Lines.size()];
        ParentAlerts = new ArrayList[Lines.size()];
        Description = new String[Lines.size()];
        
        for (int i=0; i< Lines.size(); i++) {
            try {
                String[] CurValues = Lines.get(i).split("\\t");
                // First column is just the number - not used
                SMARTS[i] = CurValues[1];
                Mean[i] = Double.parseDouble(CurValues[2]);
                Stdev[i] = Double.parseDouble(CurValues[3]);
                Hits[i] = Integer.parseInt(CurValues[4]);
                ParentAlerts[i] = new ArrayList<>();
                if (CurValues.length > 5) 
                    if (CurValues[5].length()>0) {
                        String[] CurParents = CurValues[5].split(",");
                        for (String s : CurParents)
                            ParentAlerts[i].add(Integer.valueOf(s));
                    }
                if (CurValues.length > 6) 
                    Description[i] = CurValues[6];
                else
                    Description[i] = "";
            } catch (NumberFormatException e) {
                logger.error("Unable to read value from alert input file at line " + (i+1) + " - " + e.getMessage());
                throw new GenericFailureException("Error in numeric conversion from alerts file");
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
    public double[] getMean() {
        return Mean;
    }

    /**
     * @return the FisherPValue
     */
    public double[] getStdDev() {
        return Stdev;
    }

    /**
     * @return the numer of hits
     */
    public int[] getHits() {
        return Hits;
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
     * @return the Description
     */
    public String[] getDescription() {
        return Description;
    }
    
}
