package insilico.core.model.qmrf;

import insilico.core.exception.InitFailureException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.DataInputStream;
import java.net.URL;
import java.util.ArrayList;

public class QMRFMSDoc {

    // number, name, XML tag
    final static private String[][] TAGS_SECTION = {
            {"1", "QSAR identifier", "QSAR_identifier"},
            {"2", "General information", "QSAR_General_information"},
            {"3", "Defining the endpoint - OECD Principle 1", "QSAR_Endpoint"},
            {"4", "Defining the algorithm - OECD Principle 2", "QSAR_Algorithm"},
            {"5", "Defining the applicability domain - OECD Principle 3", "QSAR_Applicability_domain"},
            {"6", "Internal validation - OECD Principle 4", "QSAR_Robustness"},
            {"7", "External validation - OECD Principle 4", "QSAR_Predictivity"},
            {"8", "Providing a mechanistic interpretation - OECD Principle 5", "QSAR_Interpretation"},
            {"9", "Miscellaneous information", "QSAR_Miscelaneous"},
            {"10", "Summary (JRC QSAR Model Database)", "QMRF_Summary"}
    };

    // number, name, XML tag, section
    final static private String[][] TAGS_CHAPTER = {
            {"1.1", "QSAR identifier (title)", "QSAR_title", "1"},
            {"1.2", "Other related models", "QSAR_models", "1"},
            {"1.3", "Software coding the model", "QSAR_software", "1"},
            {"2.1", "Date of QMRF", "qmrf_date", "2"},
            {"2.2", "QMRF author(s) and contact details", "qmrf_authors", "2"},
            {"2.3", "Date of QMRF update(s)", "qmrf_date_revision", "2"},
            {"2.4", "QMRF update(s)", "qmrf_revision", "2"},
            {"2.5", "Model developer(s) and contact details", "model_authors", "2"},
            {"2.6", "Date of model development and/or publication", "model_date", "2"},
            {"2.7", "Reference(s) to main scientific papers and/or software package", "references", "2"},
            {"2.8", "Availability of information about the model", "info_availability", "2"},
            {"2.9", "Availability of another QMRF for exactly the same model", "related_models", "2"},
            {"3.1", "Species", "model_species", "3"},
            {"3.2", "Endpoint", "model_endpoint", "3"},
            {"3.3", "Comment on endpoint", "endpoint_comments", "3"},
            {"3.4", "Endpoint units", "endpoint_units", "3"},
            {"3.5", "Dependent variable", "endpoint_variable", "3"},
            {"3.6", "Experimental protocol", "endpoint_protocol", "3"},
            {"3.7", "Endpoint data quality and variability", "endpoint_data_quality", "3"},
            {"4.1", "Type of model", "algorithm_type", "4"},
            {"4.2", "Explicit algorithm", "algorithm_explicit", "4"},
            {"4.3", "Descriptors in the model", "algorithms_descriptors", "4"},
            {"4.4", "Descriptor selection", "descriptors_selection", "4"},
            {"4.5", "Algorithm and descriptor generation", "descriptors_generation", "4"},
            {"4.6", "Software name and version for descriptor generation", "descriptors_generation_software", "4"},
            {"4.7", "Chemicals/Descriptors ratio", "descriptors_chemicals_ratio", "4"},
            {"5.1", "Description of the applicability domain of the model", "app_domain_description", "5"},
            {"5.2", "Method used to assess the applicability domain", "app_domain_method", "5"},
            {"5.3", "Software name and version for applicability domain assessment", "app_domain_software", "5"},
            {"5.4", "Limits of applicability", "applicability_limits", "5"},
            {"6.1", "Availability of the training set", "training_set_availability", "6"},
            {"6.2", "Available information for the training set", "training_set_data", "6"},
            {"6.3", "Data for each descriptor variable for the training set", "training_set_descriptors", "6"},
            {"6.4", "Data for the dependent variable for the training set", "dependent_var_availability", "6"},
            {"6.5", "Other information about the training set", "other_info", "6"},
            {"6.6", "Pre-processing of data before modelling", "preprocessing", "6"},
            {"6.7", "Statistics for goodness-of-fit", "goodness_of_fit", "6"},
            {"6.8", "Robustness - Statistics obtained by leave-one-out cross-validation", "loo", "6"},
            {"6.9", "Robustness - Statistics obtained by leave-many-out cross-validation", "lmo", "6"},
            {"6.10", "Robustness - Statistics obtained by Y-scrambling", "yscrambling", "6"},
            {"6.11", "Robustness - Statistics obtained by bootstrap", "bootstrap", "6"},
            {"6.12", "Robustness - Statistics obtained by other methods", "other_statistics", "6"},
            {"7.1", "Availability of the external validation set", "validation_set_availability", "7"},
            {"7.2", "Available information for the external validation set", "validation_set_data", "7"},
            {"7.3", "Data for each descriptor variable for the external validation set", "validation_set_descriptors", "7"},
            {"7.4", "Data for the dependent variable for the external validation set", "validation_dependent_var_availability", "7"},
            {"7.5", "Other information about the external validation set", "validation_other_info", "7"},
            {"7.6", "Experimental design of test set", "experimental_design", "7"},
            {"7.7", "Predictivity - Statistics obtained by external validation", "validation_predictivity", "7"},
            {"7.8", "Predictivity - Assessment of the external validation set", "validation_assessment", "7"},
            {"7.9", "Comments on the external validation of the model", "validation_comments", "7"},
            {"8.1", "Mechanistic basis of the model", "mechanistic_basis", "8"},
            {"8.2", "A priori or a posteriori mechanistic interpretation", "mechanistic_basis_comments", "8"},
            {"8.3", "Other information about the mechanistic interpretation", "mechanistic_basis_info", "8"},
            {"9.1", "Comments", "comments", "9"},
            {"9.2", "Bibliography", "bibliography", "9"},
            {"9.3", "Supporting information", "attachments", "9"},
            {"10.1", "QMRF number", "QMRF_number", "10"},
            {"10.2", "Publication date", "date_publication", "10"},
            {"10.3", "Keywords", "keywords", "10"},
            {"10.4", "Comments", "summary_comments", "10"}
    };

    private final XWPFDocument doc;
    private final String docText;

    public  QMRFMSDoc(URL source) throws InitFailureException {

        try {
            doc = new XWPFDocument(new DataInputStream(source.openStream()));
        } catch (Exception e) {
            throw new InitFailureException("Unable to open document - " + e.getMessage());
        }

        XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(doc);
        docText = xwpfWordExtractor.getText();
    }


    public String ParseAndConvertToXML() {

        String[] text = docText.split("\n");

        System.out.println("PARSED DOCUMENT:");
        System.out.println("-----------");
        for (int i=0; i<text.length; i++)
            System.out.println(i + "  " + text[i]);
        System.out.println("-----------");

        String XML = "";

        // headers
        XML += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";
        XML += "<!DOCTYPE QMRF PUBLIC \"http://qmrf.sourceforge.net/qmrf.dtd\" \"qmrf.dtd\">\n";
        XML += "<QMRF author=\"\" contact=\"J\" date=\"\" email=\"\" name=\"(Q)SAR Model Reporting Format\" schema_version=\"1.0\" url=\"\" version=\"\">\n";
        XML += "<QMRF_chapters>\n\n";

        String curChapNum = "0";
        String sectionClosing = null;

        for (int chapter=0; chapter<TAGS_CHAPTER.length; chapter++) {
            for (int i=0; i<text.length; i++) {
                int curChapter = IsChapter(text[i]);
                if (curChapter == chapter) {

                    // section if needed
                    if (!TAGS_CHAPTER[chapter][3].equalsIgnoreCase(curChapNum)) {
                        if (sectionClosing!=null)
                            XML += sectionClosing;
                        curChapNum = TAGS_CHAPTER[chapter][3];
                        for (String[] curSect : TAGS_SECTION) {
                            if (curSect[0].equalsIgnoreCase(curChapNum)) {
                                XML += "<" + curSect[2] + " chapter=\"" + curSect[0] + "\" "  +
                                        " help=\"\" name=\"" + curSect[1] + "\">\n";
                                sectionClosing = "</" + curSect[2] + ">\n\n";
                            }
                        }
                    }

                    // get content of the chapter
                    ArrayList<String> value = new ArrayList<>();
                    for (int j=i+1; j<text.length; j++) {
                        if (IsChapter(text[j])>-1) break;
                        if (IsSection(text[j])>-1) break;
                        value.add(text[j]);
                    }

                    // remove trailing white lines
                    for (int idx = value.size()-1; idx>=0; idx--) {
                        if (value.get(idx).isEmpty())
                            value.remove(idx);
                        else break;
                    }

                    // write xml
                    XML += "<" + TAGS_CHAPTER[chapter][2] + " chapter=\"" + TAGS_CHAPTER[chapter][0] + "\" "  +
                            " help=\"\" name=\"" + TAGS_CHAPTER[chapter][1] + "\">\n";
                    for (String s : value)
                        XML += s + "\n";
                    XML += "</" + TAGS_CHAPTER[chapter][2] + ">\n";
                }
            }
        }
        XML += sectionClosing;

        XML += "\n</QMRF_chapters>\n";
        XML += "</QMRF>";

        return XML;
    }


    private int IsChapter(String text) {
        for (int i=0; i<TAGS_CHAPTER.length; i++) {
            if ( (text.contains(TAGS_CHAPTER[i][0])) && (text.contains(TAGS_CHAPTER[i][1])) )
                return i;
        }
        return -1;
    }


    private int IsSection(String text) {
        for (int i=0; i<TAGS_SECTION.length; i++) {
            if ( (text.contains(TAGS_SECTION[i][0])) && (text.contains(TAGS_SECTION[i][1])) )
                return i;
        }
        return -1;
    }

}
