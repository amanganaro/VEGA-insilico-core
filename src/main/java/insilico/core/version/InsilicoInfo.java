package insilico.core.version;

import insilico.core.exception.InitFailureException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataInputStream;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class InsilicoInfo {

    private String Name;

    private int Major;
    private int Minor;
    private int Revision;

    private static final String XMLSource = "/insilico/core/version/version.xml";

    /**
     * Constructor. Builds the info object by reading the data retrieved
     * in the XML file.
     *
     * @throws InitFailureException
     */
    public InsilicoInfo() throws InitFailureException {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new DataInputStream((getClass().getResource(XMLSource)).openStream()));
            doc.getDocumentElement().normalize();

            // Reads version
            NodeList nodes = doc.getElementsByTagName("Version");
            Element element = (Element) nodes.item(0);

            this.Name = getValue("Name", element);
            this.Major = Integer.valueOf(getValue("Major",element));
            this.Minor = Integer.valueOf(getValue("Minor",element));
            this.Revision = Integer.valueOf(getValue("Revision",element));

        } catch (Throwable e) {
            throw new InitFailureException("Unable to read core data from XML (" +
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


    /**
     * @return the Name
     */
    public String getName() {
        return Name;
    }

    /**
     * @return the Major
     */
    public int getMajor() {
        return Major;
    }

    /**
     * @return the Minor
     */
    public int getMinor() {
        return Minor;
    }

    /**
     * @return the Revision
     */
    public int getRevision() {
        return Revision;
    }

    /**
     * Provides the model's version as a unique string
     * @return the version
     */
    public String getVersion() {
        String s = String.valueOf(Major) + "." + String.valueOf(Minor) +
                "." + String.valueOf(Revision);
        return s;
    }
}
