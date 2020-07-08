package insilico.core.model.information;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class InsilicoModelInformation {

    public final class Reference {
        public String Title;
        public String Link;
    }

    public final class ADIndexRange {
        public String Range;
        public String Description;
    }

    public final class ADIndex {
        public String Name;
        public String Description;
        public ArrayList<ADIndexRange> Ranges;
    }

    public final class Section {
        public String Number;
        public String Title;
        public String Description;
    }


    private final String Name;
    private final String Key;
    private final String Description;
    private final String Version;
    private final String Author;
    private final String VegaEndpoint;
    private final String VegaGroup;
    private final String VegaSection;


    private final String ModelJavaClass;
    private final String TrainingSetURL;
    private final String TrainingSetPngURL;
    private final String QmrfURL;
    private final String QmrfXmlURL;
    private final String QmrfExtLink;

    private final boolean Alerts;
    private final String Units;
    private final HashMap<Double, String> ClassValues;

    private final String EndpointLocation;
    private final String EndpointSubLocation;
    private final String EndpointName;

    private final String TestName;
    private final String TestType;
    private final String TestDuration;
    private final String TestDurationUnits;
    private final String TestSpeciesKingdom;
    private final String TestSpeciesPhylum;
    private final String TestSpeciesClass;
    private final String TestSpeciesTestOrganism;
    private final String TestSpeciesSex;
    private final String TestAdministrationRoute;
    private final String TestTargetOrgan;
    private final String TestGene;
    private final String TestStrain;
    private final String TestMetabolicActivation;
    private final String TestSpecificity;
    private final String TestCondition;
    private final String TestTypeMethod;
    private final String TestAssayProvider;
    private final String TestGuideline;

    private final String StatsTrainingR2;
    private final String StatsTrainingR2adj;
    private final String StatsTrainingSDEP;
    private final String StatsTrainingAccuracy;
    private final String StatsTrainingSensitivity;
    private final String StatsTrainingSpecificity;
    private final String StatsTrainingMCC;
    private final Integer StatsTrainingSize;
    private final String StatsTestR2;
    private final String StatsTestR2adj;
    private final String StatsTestQ2;
    private final String StatsTestSDEP;
    private final String StatsTestAccuracy;
    private final String StatsTestSensitivity;
    private final String StatsTestSpecificity;
    private final String StatsTestMCC;
    private final Integer StatsTestSize;
    private final String StatsNotes;

    private final String Details;
    private final String Data;
    private final ArrayList<Reference> References;
    private final ArrayList<ADIndex> ADIndices;
    private final ArrayList<Section> AdditionalDetails;
    private final ArrayList<String> ResultOutputs;
    private final LinkedHashMap<String, String> PredictionColors;
    private final ArrayList<Section> OutputSections;




    /**
     * Constructor. Builds the info object by reading the data retrieved
     * in the XML file.
     *
     * @param XMLSource path (as resource) for the XML file
     * @throws InitFailureException
     */
    public InsilicoModelInformation(URL XMLSource) throws InitFailureException {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new DataInputStream(XMLSource.openStream()));
            doc.getDocumentElement().normalize();

            // Version section
            NodeList nodes = doc.getElementsByTagName("Version");
            Element element = (Element) nodes.item(0);
            this.Name = getValue("Name", element);
            this.Key = getValue("Key", element);
            this.Description = getValue("Description", element);
            this.Version = getValue("Version", element);
            this.Author = getValue("Author", element);
            this.VegaEndpoint = getValue("VegaEndpoint", element);
            this.VegaGroup = getValue("VegaGroup", element);
            this.VegaSection = getValue("VegaSection", element);

            // Resources section
            nodes = doc.getElementsByTagName("Resources");
            element = (Element) nodes.item(0);
            this.ModelJavaClass = getValue("Class", element);
            this.TrainingSetURL = getValue("TS", element);
            this.TrainingSetPngURL = getValue("TSPNG", element);
            this.QmrfURL = getValue("QMRF", element);
            this.QmrfXmlURL = getValue("QMRFXML", element);
            this.QmrfExtLink = getValue("ExtQMRF", element);

            // Model info section
            nodes = doc.getElementsByTagName("Model");
            element = (Element) nodes.item(0);
            this.Alerts = hasTag("HasAlerts", element) ? getValue("HasAlerts", element).equalsIgnoreCase("true") : false;
            this.Units = getValue("Units", element);
            this.ClassValues = new HashMap<>();
            if (hasTag("ClassValues", element)) {
                NodeList ClassValuesNode = element.getElementsByTagName("ClassValues");
                Element node = (Element) ClassValuesNode.item(0);
                NodeList Classes = node.getElementsByTagName("Class");
                for (int i=0; i<Classes.getLength(); i++) {
                    Element n = (Element) Classes.item(i);
                    double ClassValue = Double.valueOf(getValue("Value", n));
                    String ClassLabel = getValue("Label", n);
                    this.ClassValues.put(ClassValue, ClassLabel);
                }
            }

            // Endpoint section
            nodes = doc.getElementsByTagName("Endpoint");
            element = (Element) nodes.item(0);
            this.EndpointLocation = getValue("EpLocation", element);
            this.EndpointSubLocation = getValue("EpSubLocation", element);
            this.EndpointName = getValue("Ep", element);
            this.TestName = getValue("TestName", element);
            this.TestType = getValue("TestType", element);
            this.TestDuration = getValue("DurationValue", element);
            this.TestDurationUnits = getValue("DurationUnit", element);
            this.TestSpeciesKingdom = getValue("SpeciesKingdom", element);
            this.TestSpeciesPhylum = getValue("SpeciesPhylum", element);
            this.TestSpeciesClass = getValue("SpeciesClass", element);
            this.TestSpeciesTestOrganism = getValue("SpeciesTest", element);
            this.TestSpeciesSex = getValue("SpeciesSex", element);
            this.TestAdministrationRoute = getValue("Route", element);
            this.TestTargetOrgan = getValue("Organ", element);
            this.TestGene = getValue("GeneName", element);
            this.TestStrain = getValue("Strain", element);
            this.TestMetabolicActivation = getValue("MetabolicActivation", element);
            this.TestSpecificity = getValue("TestSpecificity", element);
            this.TestCondition = getValue("TestCondition", element);
            this.TestTypeMethod = getValue("TypeMethod", element);
            this.TestAssayProvider = getValue("AssayProvider", element);
            this.TestGuideline = getValue("Guideline", element);


            // Statistics section
            nodes = doc.getElementsByTagName("Stats");
            element = (Element) nodes.item(0);
            this.StatsTrainingR2 = getValue("TrainR2", element);
            this.StatsTrainingR2adj = getValue("TrainR2adj", element);
            this.StatsTrainingSDEP = getValue("TrainStdev", element);
            this.StatsTrainingAccuracy = getValue("TrainAccuracy", element);
            this.StatsTrainingSensitivity = getValue("TrainSensitivity", element);
            this.StatsTrainingSpecificity = getValue("TrainSpecificity", element);
            this.StatsTrainingMCC = getValue("TrainMCC", element);
            this.StatsTrainingSize = getIntValue("TrainSize", element);
            this.StatsTestR2 = getValue("TestR2", element);
            this.StatsTestR2adj = getValue("TestR2adj", element);
            this.StatsTestQ2 = getValue("TestQ2", element);
            this.StatsTestSDEP = getValue("TestStdev", element);
            this.StatsTestAccuracy = getValue("TestAccuracy", element);
            this.StatsTestSensitivity = getValue("TestSensitivity", element);
            this.StatsTestSpecificity = getValue("TestSpecificity", element);
            this.StatsTestMCC = getValue("TestMCC", element);
            this.StatsTestSize = getIntValue("TestSize", element);
            this.StatsNotes = getValue("Notes", element);

            // Guide section
            nodes = doc.getElementsByTagName("Guide");
            element = (Element) nodes.item(0);
            this.Details = getValue("Details", element);
            this.Data = getValue("Data", element);
            this.References = new ArrayList<>();
            if (hasTag("References", element)) {
                NodeList SubNode = element.getElementsByTagName("References");
                Element node = (Element) SubNode.item(0);
                NodeList foundNodes = node.getElementsByTagName("Reference");
                for (int i=0; i<foundNodes.getLength(); i++) {
                    Element n = (Element) foundNodes.item(i);
                    Reference Ref = new Reference();
                    Ref.Title = getValue("Title", n);
                    Ref.Link = getValue("Link", n);
                    this.References.add(Ref);
                }
            }
            this.ADIndices = new ArrayList<>();
            if (hasTag("AD", element)) {
                NodeList SubNode = element.getElementsByTagName("AD");
                Element node = (Element) SubNode.item(0);
                NodeList foundNodes = node.getElementsByTagName("ADIndex");
                for (int i=0; i<foundNodes.getLength(); i++) {
                    Element n = (Element) foundNodes.item(i);
                    ADIndex AD = new ADIndex();
                    AD.Name = getValue("Name", n);
                    AD.Description = getValue("Description", n);
                    AD.Ranges = new ArrayList<>();
                    if (hasTag("Ranges", n)) {
                        NodeList SubSubNode = n.getElementsByTagName("Ranges");
                        Element node2 = (Element) SubSubNode.item(0);
                        NodeList foundNodes2 = node2.getElementsByTagName("Range");
                        for (int j=0; j<foundNodes2.getLength(); j++) {
                            Element nn = (Element) foundNodes2.item(j);
                            ADIndexRange Range = new ADIndexRange();
                            Range.Range = getValue("Values", nn);
                            Range.Description = getValue("Description", nn);
                            AD.Ranges.add(Range);
                        }
                    }
                    this.ADIndices.add(AD);
                }
            }
            this.AdditionalDetails = new ArrayList<>();
            if (hasTag("AdditionalDetails", element)) {
                NodeList SubNode = element.getElementsByTagName("AdditionalDetails");
                Element node = (Element) SubNode.item(0);
                NodeList foundNodes = node.getElementsByTagName("Section");
                for (int i=0; i<foundNodes.getLength(); i++) {
                    Section o = new Section();
                    Element n = (Element) foundNodes.item(i);
                    o.Title = getValue("Title", n);
                    o.Description = getValue("Description", n);
                    this.AdditionalDetails.add(o);
                }
            }
            this.ResultOutputs = new ArrayList<>();
            if (hasTag("Results", element)) {
                NodeList SubNode = element.getElementsByTagName("Results");
                Element node = (Element) SubNode.item(0);
                NodeList foundNodes = node.getElementsByTagName("Value");
                for (int i=0; i<foundNodes.getLength(); i++) {
                    Element n = (Element) foundNodes.item(i);
                    String s = n.getChildNodes().item(0).getNodeValue();
                    this.ResultOutputs.add(s);
                }
            }
            this.PredictionColors = new LinkedHashMap<>();
            if (hasTag("PredictionColors", element)) {
                NodeList SubNode = element.getElementsByTagName("PredictionColors");
                Element node = (Element) SubNode.item(0);
                NodeList foundNodes = node.getElementsByTagName("Item");
                for (int i=0; i<foundNodes.getLength(); i++) {
                    Element n = (Element) foundNodes.item(i);
                    String col = getValue("Color", n);
                    String desc = getValue("Description", n);
                    this.PredictionColors.put(col, desc);
                }
            }
            this.OutputSections = new ArrayList<>();
            if (hasTag("Output", element)) {
                NodeList SubNode = element.getElementsByTagName("Output");
                Element node = (Element) SubNode.item(0);
                NodeList foundNodes = node.getElementsByTagName("Section");
                for (int i=0; i<foundNodes.getLength(); i++) {
                    Section o = new Section();
                    Element n = (Element) foundNodes.item(i);
                    o.Number = getValue("Number", n);
                    o.Title = getValue("Title", n);
                    o.Description = getValue("Description", n);
                    this.OutputSections.add(o);
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
        if (element == null)
            return null;
        if (!hasTag(tag, element))
            return null;
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }


    private static Integer getIntValue(String tag, Element element) throws NumberFormatException {
        if (element == null)
            return null;
        String val = getValue(tag, element);
        if (val == null)
            return null;
        return Integer.valueOf(val);
    }

    private static boolean hasTag(String tag, Element element) {
        if (element == null)
            return false;
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
     * @return the Version
     */
    public String getVersion() {
        return Version;
    }

    /**
     * @return the Author
     */
    public String getAuthor() {
        return Author;
    }


    /**
     * @return the VegaEndpoint
     */
    public String getVegaEndpoint() {
        return VegaEndpoint;
    }

    /**
     * @return the VegaGroup
     */
    public String getVegaGroup() {
        return VegaGroup;
    }

    /**
     * @return the VegaSection
     */
    public String getVegaSection() {
        return VegaSection;
    }

    /**
     * @return the ModelJavaClass
     */
    public String getModelJavaClass() {
        return ModelJavaClass;
    }

    /**
     * @return the TrainingSetURL
     */
    public String getTrainingSetURL() {
        return TrainingSetURL;
    }

    /**
     * @return the TrainingSetPngURL
     */
    public String getTrainingSetPngURL() {
        return TrainingSetPngURL;
    }

    /**
     * @return the QmrfURL
     */
    public String getQmrfURL() {
        return QmrfURL;
    }

    /**
     * @return the QmrfXmlURL
     */
    public String getQmrfXmlURL() {
        return QmrfXmlURL;
    }

    /**
     * @return the QmrfExtLink
     */
    public String getQmrfExtLink() {
        return QmrfExtLink;
    }

    /**
     * @return the Alerts
     */
    public boolean isAlerts() {
        return Alerts;
    }

    /**
     * @return the Units
     */
    public String getUnits() {
        return Units;
    }

    /**
     * @return the ClassValues
     */
    public HashMap<Double, String> getClassValues() {
        return ClassValues;
    }

    /**
     * @return the EndpointLocation
     */
    public String getEndpointLocation() {
        return EndpointLocation;
    }

    /**
     * @return the EndpointSubLocation
     */
    public String getEndpointSubLocation() {
        return EndpointSubLocation;
    }

    /**
     * @return the EndpointName
     */
    public String getEndpointName() {
        return EndpointName;
    }

    /**
     * @return the TestName
     */
    public String getTestName() {
        return TestName;
    }

    /**
     * @return the TestType
     */
    public String getTestType() {
        return TestType;
    }

    /**
     * @return the TestDuration
     */
    public String getTestDuration() {
        return TestDuration;
    }

    /**
     * @return the TestDurationUnits
     */
    public String getTestDurationUnits() {
        return TestDurationUnits;
    }

    /**
     * @return the TestSpeciesKingdom
     */
    public String getTestSpeciesKingdom() {
        return TestSpeciesKingdom;
    }

    /**
     * @return the TestSpeciesPhylum
     */
    public String getTestSpeciesPhylum() {
        return TestSpeciesPhylum;
    }

    /**
     * @return the TestSpeciesClass
     */
    public String getTestSpeciesClass() {
        return TestSpeciesClass;
    }

    /**
     * @return the TestSpeciesTestOrganism
     */
    public String getTestSpeciesTestOrganism() {
        return TestSpeciesTestOrganism;
    }

    /**
     * @return the TestSpeciesSex
     */
    public String getTestSpeciesSex() {
        return TestSpeciesSex;
    }

    /**
     * @return the TestAdministrationRoute
     */
    public String getTestAdministrationRoute() {
        return TestAdministrationRoute;
    }

    /**
     * @return the TestTargetOrgan
     */
    public String getTestTargetOrgan() {
        return TestTargetOrgan;
    }

    /**
     * @return the TestGene
     */
    public String getTestGene() {
        return TestGene;
    }

    /**
     * @return the TestStrain
     */
    public String getTestStrain() {
        return TestStrain;
    }

    /**
     * @return the TestMetabolicActivation
     */
    public String getTestMetabolicActivation() {
        return TestMetabolicActivation;
    }

    /**
     * @return the TestSpecificity
     */
    public String getTestSpecificity() {
        return TestSpecificity;
    }

    /**
     * @return the TestCondition
     */
    public String getTestCondition() {
        return TestCondition;
    }

    /**
     * @return the TestTypeMethod
     */
    public String getTestTypeMethod() {
        return TestTypeMethod;
    }

    /**
     * @return the TestAssayProvider
     */
    public String getTestAssayProvider() {
        return TestAssayProvider;
    }

    /**
     * @return the TestGuideline
     */
    public String getTestGuideline() {
        return TestGuideline;
    }

    /**
     * @return the StatsTrainingR2
     */
    public String getStatsTrainingR2() {
        return StatsTrainingR2;
    }

    /**
     * @return the StatsTrainingR2adj
     */
    public String getStatsTrainingR2adj() {
        return StatsTrainingR2adj;
    }

    /**
     * @return the StatsTrainingSDEP
     */
    public String getStatsTrainingSDEP() {
        return StatsTrainingSDEP;
    }

    /**
     * @return the StatsTrainingAccuracy
     */
    public String getStatsTrainingAccuracy() {
        return StatsTrainingAccuracy;
    }

    /**
     * @return the StatsTrainingSensitivity
     */
    public String getStatsTrainingSensitivity() {
        return StatsTrainingSensitivity;
    }

    /**
     * @return the StatsTrainingSpecificity
     */
    public String getStatsTrainingSpecificity() {
        return StatsTrainingSpecificity;
    }

    /**
     * @return the StatsTrainingMCC
     */
    public String getStatsTrainingMCC() {
        return StatsTrainingMCC;
    }

    /**
     * @return the StatsTrainingSize
     */
    public Integer getStatsTrainingSize() {
        return StatsTrainingSize;
    }

    /**
     * @return the StatsTestR2
     */
    public String getStatsTestR2() {
        return StatsTestR2;
    }

    /**
     * @return the StatsTestR2adj
     */
    public String getStatsTestR2adj() {
        return StatsTestR2adj;
    }

    /**
     * @return the StatsTestQ2
     */
    public String getStatsTestQ2() {
        return StatsTestQ2;
    }

    /**
     * @return the StatsTestSDEP
     */
    public String getStatsTestSDEP() {
        return StatsTestSDEP;
    }

    /**
     * @return the StatsTestAccuracy
     */
    public String getStatsTestAccuracy() {
        return StatsTestAccuracy;
    }

    /**
     * @return the StatsTestSensitivity
     */
    public String getStatsTestSensitivity() {
        return StatsTestSensitivity;
    }

    /**
     * @return the StatsTestSpecificity
     */
    public String getStatsTestSpecificity() {
        return StatsTestSpecificity;
    }

    /**
     * @return the StatsTestMCC
     */
    public String getStatsTestMCC() {
        return StatsTestMCC;
    }

    /**
     * @return the StatsTestSize
     */
    public Integer getStatsTestSize() {
        return StatsTestSize;
    }

    /**
     * @return the StatsNotes
     */
    public String getStatsNotes() {
        return StatsNotes;
    }

    /**
     * @return the Details
     */
    public String getDetails() {
        return Details;
    }

    /**
     * @return the Data
     */
    public String getData() {
        return Data;
    }

    /**
     * @return the References
     */
    public ArrayList<Reference> getReferences() {
        return References;
    }

    /**
     * @return the ADIndices
     */
    public ArrayList<ADIndex> getADIndices() {
        return ADIndices;
    }

    /**
     * @return the AdditionalDetails
     */
    public ArrayList<Section> getAdditionalDetails() {
        return AdditionalDetails;
    }

    /**
     * @return the ResultOutputs
     */
    public ArrayList<String> getResultOutputs() {
        return ResultOutputs;
    }

    /**
     * @return the PredictionColors
     */
    public HashMap<String, String> getPredictionColors() {
        return PredictionColors;
    }

    /**
     * @return the OutputSections
     */
    public ArrayList<Section> getOutputSections() {
        return OutputSections;
    }
}
