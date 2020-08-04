package insilico.core.molecule.conversion;

import insilico.core.tools.utils.logger.InsilicoLogger;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

/**
 * Search for tags inside a MDL (SDF) file
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MDLTagReader {

    Logger logger = LoggerFactory.getLogger(MDLMolecule.class);

    private final static int DefaultMaxMolNum = 20;
    private int MaxMolNum;
    private ArrayList<String> Tags;



    public MDLTagReader() {
        MaxMolNum = DefaultMaxMolNum;
        Tags = new ArrayList<String>();
    }


    public MDLTagReader(int NumMaxMol) {
        MaxMolNum = NumMaxMol;
        Tags = new ArrayList<String>();
    }

    public ArrayList<String> SearchTags(byte[] SDF) {

        Tags = new ArrayList<String>();

        try {
            ByteArrayInputStream bain = new ByteArrayInputStream(SDF);
            BufferedReader br = new BufferedReader(new InputStreamReader(bain));

            String strLine = null;
            int MolNum = 0;

            while ((strLine = br.readLine()) != null) {

                // to make it shorter, just check the first MaxMolNum molecules
                if (strLine.compareTo("$$$$") != 0) {
                    MolNum++;
                    if (MolNum < MaxMolNum)
                        continue;
                    else
                        break;
                }

                int pos = strLine.indexOf("<");
                if (pos > -1) {
                    int pos2 = strLine.indexOf(">", pos+1);
                    if ((pos2 > -1) && (pos2 > pos)) {
                        String curTag = strLine.substring(pos+1, pos2);
                        AddTag(curTag);
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            InsilicoLogger.getLogger().warn(e.getMessage());
        }

        return Tags;
    }

    public ArrayList<String> SearchTags(File SDF) {

        Tags = new ArrayList<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(SDF));

            String strLine = null;
            int MolNum = 0;

            while ((strLine = br.readLine()) != null) {

                // to make it shorter, just check the first MaxMolNum molecules
                if (strLine.compareTo("$$$$") == 0) {
                    MolNum++;
                    if (MolNum < MaxMolNum)
                        continue;
                    else
                        break;
                }

                int pos = strLine.indexOf("<");
                if (pos > -1) {
                    int pos2 = strLine.indexOf(">", pos+1);
                    if ((pos2 > -1) && (pos2 > pos)) {
                        String curTag = strLine.substring(pos+1, pos2);
                        AddTag(curTag);
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            InsilicoLogger.getLogger().warn(e.getMessage());
        }

        return Tags;
    }


    private void AddTag(String CurTag) {

        if (Tags.isEmpty())
            Tags.add(CurTag);
        else {
            boolean found = false;
            for (String tag : Tags)
                if (tag.compareToIgnoreCase(CurTag) == 0) {
                    found = true;
                    break;
                }
            if (!found)
                Tags.add(CurTag);
        }
    }

}
