package insilico.core.model.guide;

import insilico.core.model.information.InsilicoModelInformation;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Alberto
 */
public class ModelGuideWriter {
    
    private final static String HTML_RES = "/insilico/core/model/guide/res.zip";

    
    /**
     * Create and save in the given folder the HTML for the guide based 
     * on the given info object.
     * 
     * @param Info
     * @param path
     * @throws Exception 
     */
    public final static void GenerateHTMLtoFile(InsilicoModelInformation Info,
                                                String path) throws Exception {
        
        String html = GenerateHTML(Info);
        
        // copy all the base HTML files in the dest directory
        URL src = ModelGuideWriter.class.getResource(HTML_RES);
        unzip(src.openStream(), new File(path));
        
        // save html file
        PrintWriter pw = new PrintWriter(new FileWriter(path + "/vega_model_guide_" + Info.getKey() + ".html"));
        pw.write(html);
        pw.flush();
        pw.close();
    }
    
    
    /**
     * Create and return the HTML for the guide based on the given info object.
     * 
     * @param Info
     * @return 
     */
    public final static String GenerateHTML(InsilicoModelInformation Info) {
        String s = "";
        
        s += "<html>\n";
        s += "<head>\n";
        s += "  <meta charset=\"utf-8\">\n";
        s += "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n";
        s += "  <link rel=\"stylesheet\" href=\"./res/simple-grid.css\">\n";
        s += "  <title>" + Info.getName() + " model for " + Info.getVegaEndpoint() + "</title>\n";
        s += "</head>\n";
        s += "<body>\n";
        s += "\n";
        s += "    <div class=\"container\">";
        s += "<div class=\"row\"><div class=\"col-12 center\">";
        s += "<img height=65px src=\"./res/vega.png\">";
        s += "</div></div>\n";
        
        
        // Title and summary
        s += "<div class=\"row\"><div class=\"col-12 center\">";
        s += "<h1>Guide to " + Info.getName() + " model for " + 
                Info.getVegaEndpoint() + "<br>version " + Info.getVersion();
        s += "</div></div>\n";


        s += "<div class=\"row\"><div class=\"col-12\">";
        s += "<h2>1. Model Explanation</h2>";
        s += "</div></div>\n";

        // Introduction
        s += "<div class=\"row\"><div class=\"col-12\">";
        s += "<h3>1.1 Introduction</h3>";
        s += TextToHTML(Info.getDescription());
        s += "</div></div>\n";
        
        // Model details
        s += "<div class=\"row\"><div class=\"col-12\">";
        s += "<h3>1.2 Model Details</h3>";
        s += TextToHTML(Info.getDetails());
        s += "</div></div>\n";
        
        // References
        s += "<div class=\"row\"><div class=\"col-12\">";
        s += "<h3>1.3 References</h3>";
        String ref = "";
        if (Info.getReferences().isEmpty())
            ref += ModelGuideConstants.NULL_STRING;
        else {
            for (InsilicoModelInformation.Reference e : Info.getReferences()) {
                if (!ref.isEmpty())
                    ref += "<br>";
                ref += TextToHTML(e.Title) + "<br>";
                if ( (e.Link != null) && (!e.Link.isEmpty()))
                    ref += TextToLink(e.Link) + "<br>";
            }
        }
        s += ref;
        s += "</div></div>\n";

        // Model details
        s += "<div class=\"row\"><div class=\"col-12\">";
        s += "<h3>1.4 Dataset</h3>";
        s += TextToHTML(Info.getData());
        s += "</div></div>\n";
        
        // Applicability domain
        s += "<div class=\"row\"><div class=\"col-12\">";
        s += "<h3>1.5 Applicability Domain</h3>";
        s += TextToHTML(ModelGuideConstants.AD_INTRO) + "<br>";
        for (InsilicoModelInformation.ADIndex e : Info.getADIndices()) {
            s += "<br><b>- " + e.Name + ".</b> ";
            s += TextToHTML(e.Description);
            s += ModelGuideConstants.AD_INTERVALS;
            s += "<table>";
            for (InsilicoModelInformation.ADIndexRange ee : e.Ranges) {
                s += "<tr><td class=\"min\">" + ee.Range;
                s += "</td><td>" + ee.Description + "</td></tr>";
            }
            s += "</table>";
        }
        s += "</div></div>\n";
        
        // Additional model details
        int DetailsIdx = 6;
        if (!Info.getAdditionalDetails().isEmpty()) {
            for (InsilicoModelInformation.Section e : Info.getAdditionalDetails()) {
                s += "<div class=\"row\"><div class=\"col-12\">";
                s += "<h3>1." + DetailsIdx++ + " " + e.Title + "</h3>";
                s += TextToHTML(e.Description);
                s += "</div></div>\n";
            }            
        }
        
        // Model statistics
        s += "<div class=\"row\"><div class=\"col-12\">";
        s += "<h3>1." + DetailsIdx++ + " Model statistics</h3>";
        s += TextToHTML(ModelGuideConstants.STATISTICS_INTRO);
        String stats = "";
        if (Info.getStatsTrainingSize() != null)
            stats += "<tr><td class=\"min\">Training set size</td><td>" + Info.getStatsTrainingSize() + "</td></tr>";
        if (Info.getStatsTrainingR2() != null)
            stats += "<tr><td class=\"min\">R<sup>2</sup> on training set</td><td>" + Info.getStatsTrainingR2() + "</td></tr>";
        if (Info.getStatsTrainingR2adj() != null)
            stats += "<tr><td class=\"min\">R<sup>2</sup>adj on training set</td><td>" + Info.getStatsTrainingR2adj() + "</td></tr>";
        if (Info.getStatsTrainingSDEP() != null)
            stats += "<tr><td class=\"min\">RMSE on training set</td><td>" + Info.getStatsTrainingSDEP() + "</td></tr>";
        if (Info.getStatsTrainingAccuracy() != null)
            stats += "<tr><td class=\"min\">Accuracy on training set</td><td>" + Info.getStatsTrainingAccuracy() + "</td></tr>";
        if (Info.getStatsTrainingSensitivity() != null)
            stats += "<tr><td class=\"min\">Specificity on training set</td><td>" + Info.getStatsTrainingSensitivity() + "</td></tr>";
        if (Info.getStatsTrainingSpecificity() != null)
            stats += "<tr><td class=\"min\">Sensitivity on training set</td><td>" + Info.getStatsTrainingSpecificity() + "</td></tr>";
        if (Info.getStatsTrainingMCC() != null)
            stats += "<tr><td class=\"min\">MCC on training set</td><td>" + Info.getStatsTrainingMCC() + "</td></tr>";
        if (Info.getStatsTestSize() != null)
            stats += "<tr><td class=\"min\">Test set size</td><td>" + Info.getStatsTestSize() + "</td></tr>";
        if (Info.getStatsTestR2() != null)
            stats += "<tr><td class=\"min\">R<sup>2</sup> on test set</td><td>" + Info.getStatsTestR2() + "</td></tr>";
        if (Info.getStatsTestR2adj() != null)
            stats += "<tr><td class=\"min\">R<sup>2</sup>adj on test set</td><td>" + Info.getStatsTestR2adj() + "</td></tr>";
        if (Info.getStatsTestSDEP() != null)
            stats += "<tr><td class=\"min\">RMSE on test set</td><td>" + Info.getStatsTestSDEP() + "</td></tr>";
        if (Info.getStatsTestAccuracy() != null)
            stats += "<tr><td class=\"min\">Accuracy on test set</td><td>" + Info.getStatsTestAccuracy() + "</td></tr>";
        if (Info.getStatsTestSensitivity() != null)
            stats += "<tr><td class=\"min\">Specificity on test set</td><td>" + Info.getStatsTestSensitivity() + "</td></tr>";
        if (Info.getStatsTestSpecificity() != null)
            stats += "<tr><td class=\"min\">Sensitivity on test set</td><td>" + Info.getStatsTestSpecificity() + "</td></tr>";
        if (Info.getStatsTestMCC() != null)
            stats += "<tr><td class=\"min\">MCC on test set</td><td>" + Info.getStatsTestMCC() + "</td></tr>";
        if (!stats.isEmpty())
            s += "<table>" + stats + "</table>";
        s += "</div></div>\n";
        
        
        s += "<div class=\"row\"><div class=\"col-12\">";
        s += "<h2>2. Model usage</h2>";
        s += "</div></div>\n";
        
        // Model usage - input
        s += "<div class=\"row\"><div class=\"col-12\">";
        s += "<h3>2.1 Input</h3>";
        s += TextToHTML(ModelGuideConstants.INPUT_GENERAL);
        s += "</div></div>\n";
        
        // Model usage - output
        s += "<div class=\"row\"><div class=\"col-12\">";
        s += "<h3>2.2 Input</h3>";
        s += TextToHTML(ModelGuideConstants.OUTPUT_GENERAL);
        
        s += "<br><br><i>1 - Prediction summary</i><br>";
        s += "Here is reported a depiction of the compound and the final assessment of the prediction (i.e. the prediction made"
                + " together with the analysis of the applicability domain). Following, all information related to the prediction "
                + "are reported:<br>";
        for (String res : Info.getResultOutputs())
            s += "- " + res + "<br>";                
        s += "<br>Note that if some problems were encountered while processing the molecule "
                + "structure, some warning are reported in the last field (Remarks).<br><br>\n";
        
        s += "A graphical representation of the evaluation of the prediction and of its "
                + "reliability is also provided, using the following elements:";
        if (!Info.getPredictionColors().isEmpty()) {
            s += "<table>";
            for (String color : Info.getPredictionColors().keySet()) {            
                s += "<tr><td><img height=40px src=\"./res/" + ColorToImageLink(color) + "\"></td>";
                s += "<td>" + Info.getPredictionColors().get(color) + "</td></tr>";
            }
            s += "</table>";
        }
        s += "<table>";
        s += "<tr><td><img height=40px src=\"./res/rel_1.png\"></td>";
        s += "<td>Prediction has low reliability (compound out of the AD)</td></tr>";
        s += "<tr><td><img height=40px src=\"./res/rel_2.png\"></td>";
        s += "<td>Prediction has moderate reliability (compound could be out of the AD)</td></tr>";
        s += "<tr><td><img height=40px src=\"./res/rel_3.png\"></td>";
        s += "<td>Prediction has high reliability (compound into the AD)</td></tr>";
        s += "</table>";
        
        InsilicoModelInformation.Section sec = GetSection(Info, "2");
        if (sec != null) {
            s += "<br><i>" + sec.Number + " - " + sec.Title + "</i><br>";
            s += TextToHTML(sec.Description) + "<br>\n";            
        }
        
        s += "<br><i>3.1 - Applicability Domain: Similar compounds, with predicted and experimental values</i><br>";
        s += TextToHTML(ModelGuideConstants.OUTPUT_3_1) + "<br>\n";
        
        s += "<br><i>3.2 - Applicability Domain: Measured Applicability Domain scores</i><br>";
        s += TextToHTML(ModelGuideConstants.OUTPUT_3_2) + "<br>\n";
        
        s += "<br><i>4.1 - Reasoning: Relevant chemical fragments and moieties</i><br>";
        s += TextToHTML(ModelGuideConstants.OUTPUT_4_1) + "<br>\n";
        
        sec = GetSection(Info, "4.2");
        if (sec != null) {
            s += "<br><i>" + sec.Number + " - " + sec.Title + "</i><br>";
            s += TextToHTML(sec.Description) + "<br>\n";            
        }
        
        
        s += "</div></div>\n";
        
        // Whats new

        // Credits
        s += "<div class=\"row\"><div class=\"col-12 center\">\n" +
            "<br>\n" +
            "<hr>\n" +
            "For information and support about VEGA and about this QSAR model, please contact:<br> \n" +
            "<a href=\"mailto:chm@kode-solutions.net\">chm@kode-solutions.net</a><br>\n" +
            "<img src=\"./res/kode_chm_200.png\" style=\"height:50px; padding:10px\" >\n" +
            "</div></div>";
                
        s += "</div>\n";
        s += "</body>\n";
        s += "</html>";
        
        return s;
    }
    
    
    //// Internal methods
    
    private static String TextToHTML(String s) {
        if (s == null) return ModelGuideConstants.NULL_STRING;
        if (s.isEmpty()) return ModelGuideConstants.NULL_STRING;
        String ss = s.replace("\n", "<br>");
        return ss;
    }
    
    
    private static String TextToLink(String s) {
        String ss = "<a href='" + s + "' target='_blank'>";
        ss += s;
        ss += "</a>";
        return ss;
    }
    
    
    private static String ColorToImageLink(String s) {
        if (s.equalsIgnoreCase("green"))
            return "green.png";
        if (s.equalsIgnoreCase("yellow"))
            return "yellow.png";
        if (s.equalsIgnoreCase("orange"))
            return "orange.png";
        if (s.equalsIgnoreCase("red"))
            return "red.png";
        return "gray.png";
    }
    
    
    private static InsilicoModelInformation.Section GetSection(InsilicoModelInformation info, String Number) {
        for (InsilicoModelInformation.Section s : info.getOutputSections())
            if (s.Number.equalsIgnoreCase(Number))
                return s;
        return null;
    }
    
   
    private static void unzip(InputStream source, File target) throws IOException {
        final ZipInputStream zipStream = new ZipInputStream(source);
        ZipEntry nextEntry;
        while ((nextEntry = zipStream.getNextEntry()) != null) {
            final String name = nextEntry.getName();
            // only extract files
            if (!name.endsWith("/")) {
                final File nextFile = new File(target, name);

                // create directories
                final File parent = nextFile.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                // write file
                try (OutputStream targetStream = new FileOutputStream(nextFile)) {
                    copy(zipStream, targetStream);
                }
            }
        }
    }

    private static void copy(final InputStream source, final OutputStream target) throws IOException {
        final int bufferSize = 4 * 1024;
        final byte[] buffer = new byte[bufferSize];

        int nextCount;
        while ((nextCount = source.read(buffer)) >= 0) {
            target.write(buffer, 0, nextCount);
        }
    }    
    

}
