package insilico.core.model.old;

import insilico.core.exception.InitFailureException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class InsilicoModelInfoOLD {

    private final String Name;
    private final String Key;
    private final String Description;
    private final String DescriptionLong;
    private final String Version;

    private final String TrainingSetURL;
    private final String TrainingSetPngURL;
    private final String GuideURL;
    private final String QMRFLink;
    private final String QMRFLocalURL;
    private final boolean Alerts;
    private final String Units;
    private final HashMap<Double, String> ClassValues;

    /**
     * Constructor. Builds the info object by reading the data retrieved
     * in the XML file.
     *
     * @param XMLSource path (as resource) for the XML file
     * @throws InitFailureException
     */
    public InsilicoModelInfoOLD(URL XMLSource) throws InitFailureException {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new DataInputStream(XMLSource.openStream()));
            doc.getDocumentElement().normalize();

            // Reads version
            NodeList nodes = doc.getElementsByTagName("Version");
            Element element = (Element) nodes.item(0);
            this.Name = getValue("Name", element);
            this.Key = getValue("Key", element);
            this.Description = getValue("Description", element);
            this.DescriptionLong = getValue("DescriptionLong", element);
            this.Version = getValue("Version", element);

            // Reads model objects info
            nodes = doc.getElementsByTagName("Model");
            element = (Element) nodes.item(0);
            this.TrainingSetURL = getValue("TS", element);
            this.TrainingSetPngURL = hasTag("TSPNG", element) ? getValue("TSPNG", element) : null;
            this.GuideURL = hasTag("Guide", element) ? getValue("Guide", element) : null;
            this.QMRFLink = hasTag("QMRF", element) ? getValue("QMRF", element) : null;
            this.QMRFLocalURL =  hasTag("LocalQMRF", element) ? getValue("LocalQMRF", element) : null;
//            this.Alerts = hasTag("HasAlerts", element) ? getValue("HasAlerts", element).equalsIgnoreCase("true") : false;
            this.Alerts = hasTag("HasAlerts", element) && getValue("HasAlerts", element).equalsIgnoreCase("true");
            this.Units = hasTag("Units", element) ? getValue("Units", element) : null;

            // Class values (if available)
            this.ClassValues = new HashMap<>();
            if (hasTag("ClassValues", element)) {
                NodeList ClassValuesNode = element.getElementsByTagName("ClassValues");
                Element node = (Element) ClassValuesNode.item(0);
                NodeList Classes = node.getElementsByTagName("Class");
                for (int i=0; i<Classes.getLength(); i++) {
                    Element n = (Element) Classes.item(i);
                    double ClassValue = Double.parseDouble(getValue("Value", n));
                    String ClassLabel = getValue("Label", n);
                    this.ClassValues.put(ClassValue, ClassLabel);
                }
            }

        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new InitFailureException("Unable to read model data from XML (" +
                    e.getMessage() + ")");
        }

    }

    /**
     * Private method to retrieve node values
     *
     * @param tag tag to be searched
     * @param element root element
     * @return the value of the node
     */
    private static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }


    private static boolean hasTag(String tag, Element element) {
        NodeList curNodes = element.getElementsByTagName(tag);
        return !(curNodes.getLength() == 0);
    }


    /**
     * @return the Name
     */
    public String getName() {
        return Name;
    }

    /**
     * @return the Key
     */
    public String getKey() {
        return Key;
    }

    /**
     * @return the Description
     */
    public String getDescription() {
        return Description;
    }

    /**
     * @return the DescriptionLong
     */
    public String getDescriptionLong() {
        return DescriptionLong;
    }

    /**
     * @return the TrainingSetURL
     */
    public String getTrainingSetURL() {
        return TrainingSetURL;
    }


    public boolean hasTrainingSetPngURL() {
        return !(TrainingSetPngURL == null);
    }

    /**
     * @return the TrainingSetPngURL
     */
    public String getTrainingSetPngURL() {
        return TrainingSetPngURL;
    }

    /**
     * @return the Alerts
     */
    public boolean hasAlerts() {
        return Alerts;
    }

    /**
     * @return the units
     */
    public String getUnits() {
        return Units;
    }

    /**
     * @return the Version
     */
    public String getVersion() {
        return Version;
    }


    /**
     * @return the ClassValues
     */
    public HashMap<Double, String> getClassValues() {
        return ClassValues;
    }


    public boolean hasClassValues() {
        return !this.ClassValues.isEmpty();
    }

    /**
     * @return the GuideURL
     */
    public String getGuideURL() {
        return GuideURL;
    }

    public boolean hasGuideURL() {
        return !(GuideURL == null);
    }

    public String getQMRFLink() {
        return QMRFLink;
    }

    public boolean hasQMRFLink() {
        return !(QMRFLink == null);
    }

    public String getQMRFLocalURL() {
        return QMRFLocalURL;
    }

    public boolean hasQMRFLocalURL() {
        return !(QMRFLocalURL == null);
    }





}
