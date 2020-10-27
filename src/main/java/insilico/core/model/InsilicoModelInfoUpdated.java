package insilico.core.model;

import insilico.core.exception.InitFailureException;
import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class InsilicoModelInfoUpdated {

    // VERSION
    private final HashMap<String, String> Version = new HashMap<>();
    public final static String Version_Name = "Name";
    public final static String Version_Key = "Key";
    public final static String Version_Summary = "Summary";
    public final static String Version_Version = "Version";

    public String getName() {
        return Version.get(Version_Name) == null ? "" : Version.get(Version_Name);
    }

    public String getKey() {
        return Version.get(Version_Key) == null ? "" : Version.get(Version_Key);
    }

    public String getSummary() {
        return Version.get(Version_Summary) == null ? "" : Version.get(Version_Summary);
    }

    public String getVersion() {
        return Version.get(Version_Version) == null ? "" : Version.get(Version_Version);
    }



    // VEGA
    private final HashMap<String, String> Vega = new HashMap<>();
    private final HashMap<Integer, String> ClassValues = new HashMap<>();
    public final static String Vega_TS = "TS";
    public final static String Vega_QMRF = "QMRF";
    public final static String Vega_HasAlerts = "HasAlerts";
    public final static String Vega_Units = "Units";
    public final static String Vega_Conversion = "UnitConversion";
    public final static String Vega_ClassValues = "ClassValues";

    public String getTrainingSetURL() {
        return Vega.get(Vega_TS) == null ? "" : Vega.get(Vega_TS);
    }

    public String getQMRF() {
        return Vega.get(Vega_QMRF) == null ? "" : Vega.get(Vega_QMRF);
    }

    public boolean hasAlerts() {
        if (!Vega.containsKey(Vega_HasAlerts))
            return false;
        return Boolean.valueOf(Vega.get(Vega_HasAlerts));
    }

    public String getUnits() {
        return Vega.get(Vega_Units) == null ? "" : Vega.get(Vega_Units);
    }

    public boolean hasConversion() {
        return Vega.get(Vega_Conversion) == null;
    }

    public String getConversion() {
        return Vega.get(Vega_Conversion) == null ? "" : Vega.get(Vega_Conversion);
    }

    public boolean hasClassValues() {
        return !this.ClassValues.isEmpty();
    }

    public HashMap<Integer, String> getClassValues() {
        return ClassValues;
    }



    // GUIDE
    public final HashMap<String, String> Guide = new HashMap<>();
    public final static String Guide_Description = "Description";
    public final static String Guide_Model = "Model";
    public final static String Guide_Applicability_Domain = "Applicability_Domain";
    public final static String Guide_Descriptors = "Descriptors";
    public final static String Guide_Alerts = "Alerts";
    public final static String Guide_Output = "Output";

    // APPLICABILITY DOMAIN INSIDE GUIDE
    public final List<HashMap<String, String>> Applicability_Domain = new ArrayList<>();
    public final static String Guide_AD_Name = "Name";
    public final static String Guide_AD_RangeTop = "RangeTop";
    public final static String Guide_AD_RangeMid = "RangeMid";
    public final static String Guide_AD_RangeBottom = "RangeBottom";
    public final static String Guide_AD_DescriptionRangeTop = "DescriptionRangeTop";
    public final static String Guide_AD_DescriptionRangeMid = "DescriptionRangeMid";
    public final static String Guide_AD_DescriptionRangeBottom = "DescriptionRangeBottom";


    // REFERENCE
    public final ModelReference Reference = new ModelReference();
    public final static String Reference_SingleRef = "Ref";
    public final static String Reference_QMRF_Link = "QMRF_Link";
    public final static String Reference_ReferenceName = "ReferenceName";
    public final static String Reference_ReferenceLink = "ReferenceLink";

    // ENDPOINT
    public final HashMap<String, String> Endpoint = new HashMap<>();
    public final static String Endpoint_Unit_original = "Unit_original";
    public final static String Endpoint_Unit = "Unit";
    public final static String Endpoint_UnitFamily = "UnitFamily";
    public final static String Endpoint_Lambda = "Lambda";
    public final static String Endpoint_Classes = "Classes";
    public final static String Endpoint_ClassesGUID = "ClassesGUID";
    public final static String Endpoint_Endpoint_location1 = "Endpoint_location1";
    public final static String Endpoint_Endpoint_location2 = "Endpoint_location2";
    public final static String Endpoint_Endpoint = "Endpoint";
    public final static String Endpoint_Endpoint_comment = "Endpoint_comment";
    public final static String Endpoint_Test_type = "Test_type";
    public final static String Endpoint_Duration_value = "Duration_value";
    public final static String Endpoint_Duration_unit = "Duration_unit";
    public final static String Endpoint_Effect = "Effect";
    public final static String Endpoint_Kingdom = "Kingdom";
    public final static String Endpoint_Phylum = "Phylum";
    public final static String Endpoint_Class = "Class";
    public final static String Endpoint_Test_organisms_species = "Test_organisms_species";
    public final static String Endpoint_Sex = "Sex";
    public final static String Endpoint_Route_of_administration = "Route_of_administration";
    public final static String Endpoint_Organ = "Organ";
    public final static String Endpoint_Gene_name = "Gene_name";
    public final static String Endpoint_Strain = "Strain";
    public final static String Endpoint_Metabolic_activation = "Metabolic_activation";
    public final static String Endpoint_Test_specificity = "Test_specificity";
    public final static String Endpoint_Test_condition = "Test_condition";
    public final static String Endpoint_Type_of_method = "Type_of_method";
    public final static String Endpoint_Assay_provider = "Assay_provider";
    public final static String Endpoint_Test_guideline = "Test_guideline";


    // STATS
    public final HashMap<String, String> Stats = new HashMap<>();
    public final static String Stats_n_Train = "n_Train";
    public final static String Stats_R2_Train = "R2_Train";
    public final static String Stats_RMSE_Train = "RMSE_Train";
    public final static String Stats_R2adj_Train = "R2adj_Train";
    public final static String Stats_Q2_Train = "Q2_Train";
    public final static String Stats_Fisher_Train = "Fisher_Train";
    public final static String Stats_S_Train = "S_Train";
    public final static String Stats_Sdev_Train = "Sdev_Train";
    public final static String Stats_SSR_Train = "SSR_Train";
    public final static String Stats_n_Invisible_training = "n_Invisible_training";
    public final static String Stats_R2_Invisible_training = "R2_Invisible_training";
    public final static String Stats_RMSE_Invisible_training = "RMSE_Invisible_training";
    public final static String Stats_Q2_Invisible_training = "Q2_Invisible_training";
    public final static String Stats_Fisher_Invisible_training = "Fisher_Invisible_training";
    public final static String Stats_S_Invisible_training = "S_Invisible_training";
    public final static String Stats_n_Calibration = "n_Calibration";
    public final static String Stats_R2_Calibration = "R2_Calibration";
    public final static String Stats_RMSE_Calibration = "RMSE_Calibration";
    public final static String Stats_Q2_Calibration = "Q2_Calibration";
    public final static String Stats_Fisher_Calibration = "Fisher_Calibration";
    public final static String Stats_S_calibration = "S_calibration";
    public final static String Stats_n_Test = "n_Test";
    public final static String Stats_R2_Test = "R2_Test";
    public final static String Stats_RMSE_Test = "RMSE_Test";
    public final static String Stats_R2adj_Test = "R2adj_Test";
    public final static String Stats_Fisher_Test = "Fisher_Test";
    public final static String Stats_S_Test = "S_Test";
    public final static String Stats_Sdev_Test = "Sdev_Test";
    public final static String Stats_SSR_Test = "SSR_Test";
    public final static String Stats_Accuracy_Train = "Accuracy_Train";
    public final static String Stats_Specificity_Train = "Specificity_Train";
    public final static String Stats_Sensitivity_Train = "Sensitivity_Train";
    public final static String Stats_Accuracy_Test = "Accuracy_Test";
    public final static String Stats_Specificity_Test = "Specificity_Test";
    public final static String Stats_Sensitivity_Test = "Sensitivity_Test";



    public InsilicoModelInfoUpdated(URL XMLSource) throws InitFailureException {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new DataInputStream(XMLSource.openStream()));

//            doc.getDocumentElement().normalize();

            // VERSION TAGS
            System.out.println(doc.getDocumentURI());
            NodeList nodes = doc.getElementsByTagName("Version");
            Element element = (Element) nodes.item(0);


            if (hasTag(Version_Name, element)) {
                Version.put(Version_Name, getValue(Version_Name, element));
            } else {
                Version.put(Version_Name, null);
            }

            if (hasTag(Version_Key, element)) {
                Version.put(Version_Key, getValue(Version_Key,element));
            } else {
                Version.put(Version_Key, null);
            }

            if (hasTag(Version_Summary, element)) {
                Version.put(Version_Summary, getValue(Version_Summary,element));
            } else {
                Version.put(Version_Summary, null);
            }

            if (hasTag(Version_Version, element)) {
                Version.put(Version_Version, getValue(Version_Version,element));
            } else {
                Version.put(Version_Version, null);
            }

            // VEGA TAGS

            nodes = doc.getElementsByTagName("Vega");
            element = (Element) nodes.item(0);

            if (hasTag(Vega_TS, element)) {
                Vega.put(Vega_TS, getValue(Vega_TS,element));
            } else {
                Vega.put(Vega_TS, null);
            }


            if (hasTag(Vega_QMRF, element)) {
                Vega.put(Vega_QMRF, getValue(Vega_QMRF,element));
            } else {
                Vega.put(Vega_QMRF, null);
            }

            if (hasTag(Vega_HasAlerts, element)) {
                Vega.put(Vega_HasAlerts, getValue(Vega_HasAlerts,element));
            } else {
                Vega.put(Vega_HasAlerts, null);
            }

            if (hasTag(Vega_Units, element)) {
                Vega.put(Vega_Units, getValue(Vega_Units,element));
            } else {
                Vega.put(Vega_Units, null);
            }

            if (hasTag(Vega_Conversion, element)) {
                Vega.put(Vega_Conversion, getValue(Vega_Conversion,element));
            } else {
                Vega.put(Vega_Conversion, null);
            }

            if (hasTag(Vega_ClassValues, element)) {
                NodeList ClassValuesNode = element.getElementsByTagName("ClassValues");
                Element node = (Element) ClassValuesNode.item(0);
                NodeList Classes = node.getElementsByTagName("Class");
                for (int i=0; i<Classes.getLength(); i++) {
                    Element n = (Element) Classes.item(i);
                    String ClassValue = getValue("Value", n);
                    String ClassLabel = getValue("Label", n);
                    ClassValues.put(Integer.valueOf(ClassValue), ClassLabel);
                }
            }


            // GUIDE TAGS
            nodes = doc.getElementsByTagName("Guide");
            element = (Element) nodes.item(0);

            if (hasTag(Guide_Description, element)) {
                Guide.put(Guide_Description, getValue(Guide_Description,element));
            } else {
                Guide.put(Guide_Description, null);
            }

            if (hasTag(Guide_Model, element)) {
                Guide.put(Guide_Model, getValue(Guide_Model,element));
            } else {
                Guide.put(Guide_Model, null);
            }

            if (hasTag(Guide_Descriptors, element)) {
                Guide.put(Guide_Descriptors, getValue(Guide_Descriptors,element));
            } else {
                Guide.put(Guide_Descriptors, null);
            }

            if (hasTag(Guide_Output, element)) {
                Guide.put(Guide_Output, getValue(Guide_Output,element));
            } else {
                Guide.put(Guide_Output, null);
            }

            // todo AD work in progress

            if (hasTag(Guide_Applicability_Domain, element)) {
                NodeList ADNode = element.getElementsByTagName(Guide_Applicability_Domain);
                Element node = (Element) ADNode.item(0);
                NodeList ADSingleNodes = node.getElementsByTagName("AD");
                for (int i=0; i<ADSingleNodes.getLength(); i++) {

                    HashMap<String, String> currentAD = new HashMap<>();
                    Element n = (Element) ADSingleNodes.item(i);

                    String name = getValue(Guide_AD_Name, n);
                    currentAD.put(Guide_AD_Name, name);

                    if(hasTag(Guide_AD_RangeTop, n)) {
                        String rangeTop = getValue(Guide_AD_RangeTop, n);
                        String descriptionRangeTop = getValue(Guide_AD_DescriptionRangeTop, n);
                        currentAD.put(Guide_AD_RangeTop, rangeTop);
                        currentAD.put(Guide_AD_DescriptionRangeTop, descriptionRangeTop);
                    }


                    if (hasTag(Guide_AD_RangeMid, n)) {
                        String rangeMid = getValue(Guide_AD_RangeMid, n);
                        String descriptionRangeMid = getValue(Guide_AD_DescriptionRangeMid, n);
                        currentAD.put(Guide_AD_RangeMid, rangeMid);
                        currentAD.put(Guide_AD_DescriptionRangeMid, descriptionRangeMid);
                    }

                    if (hasTag(Guide_AD_RangeBottom, n)){
                        String rangeBottom = getValue(Guide_AD_RangeBottom, n);
                        String descriptionRangeBottom = getValue(Guide_AD_DescriptionRangeBottom, n);
                        currentAD.put(Guide_AD_RangeBottom, rangeBottom);
                        currentAD.put(Guide_AD_DescriptionRangeBottom, descriptionRangeBottom);
                    }

                    Applicability_Domain.add(currentAD);
                }

//                Guide.put(Guide_Description, getValue(,element));
            } else {
                Guide.put(Guide_Applicability_Domain, null);
            }

            // todo alerts, si lasciano così?
            if (hasTag(Guide_Alerts, element)) {
                Guide.put(Guide_Alerts, getValue(Guide_Alerts,element));
            } else {
                Guide.put(Guide_Alerts, null);
            }


            // REFERENCE TAGS
            nodes = doc.getElementsByTagName("Reference");
            element = (Element) nodes.item(0);

            if(hasTag(Reference_QMRF_Link, element)){
                Reference.setQMRFLink(getValue(Reference_QMRF_Link, element));
            }

            List<HashMap<String, String>> referenceList = new ArrayList<>();

            if(hasTag(Reference_SingleRef, element)) {
                NodeList RefNode = element.getElementsByTagName(Reference_SingleRef);
                for(int i = 0; i < RefNode.getLength(); i++){

                    HashMap<String, String> currentRef = new HashMap<>();
                    Element n = (Element) RefNode.item(i);

                    if(hasTag(Reference_ReferenceName, n)){
                        currentRef.put(Reference_ReferenceName, getValue(Reference_ReferenceName, n));
                    } else {
                        currentRef.put(Reference_ReferenceName, null);
                    }

                    if(hasTag(Reference_ReferenceLink, n)){
                        currentRef.put(Reference_ReferenceLink, getValue(Reference_ReferenceLink, n));
                    } else {
                        currentRef.put(Reference_ReferenceLink, null);
                    }

                    referenceList.add(currentRef);
                }

                Reference.setReferenceList(referenceList);
            }


            // ENDPOINT TAGS
            nodes = doc.getElementsByTagName("Endpoint");
            element = (Element) nodes.item(0);

            if (hasTag(Endpoint_Unit_original, element)) {
                Endpoint.put(Endpoint_Unit_original, getValue(Endpoint_Unit_original,element));
            } else {
                Endpoint.put(Endpoint_Unit_original, null);
            }
            
            if (hasTag(Endpoint_Unit, element)) {
                Endpoint.put(Endpoint_Unit, getValue(Endpoint_Unit,element));
            } else {
                Endpoint.put(Endpoint_Unit, null);
            }
            
            if (hasTag(Endpoint_UnitFamily, element)) {
                Endpoint.put(Endpoint_UnitFamily, getValue(Endpoint_UnitFamily,element));
            } else {
                Endpoint.put(Endpoint_UnitFamily, null);
            }
            
            if (hasTag(Endpoint_Lambda, element)) {
                Endpoint.put(Endpoint_Lambda, getValue(Endpoint_Lambda,element));
            } else {
                Endpoint.put(Endpoint_Lambda, null);
            }
            
            if (hasTag(Endpoint_Classes, element)) {
                Endpoint.put(Endpoint_Classes, getValue(Endpoint_Classes,element));
            } else {
                Endpoint.put(Endpoint_Classes, null);
            }
            
            if (hasTag(Endpoint_ClassesGUID, element)) {
                Endpoint.put(Endpoint_ClassesGUID, getValue(Endpoint_ClassesGUID,element));
            } else {
                Endpoint.put(Endpoint_ClassesGUID, null);
            }
            
            if (hasTag(Endpoint_Endpoint_location1, element)) {
                Endpoint.put(Endpoint_Endpoint_location1, getValue(Endpoint_Endpoint_location1,element));
            } else {
                Endpoint.put(Endpoint_Endpoint_location1, null);
            }
            
            if (hasTag(Endpoint_Endpoint_location2, element)) {
                Endpoint.put(Endpoint_Endpoint_location2, getValue(Endpoint_Endpoint_location2, element));
            } else {
                Endpoint.put(Endpoint_Endpoint_location2, null);
            }
            
            if (hasTag(Endpoint_Endpoint, element)) {
                Endpoint.put(Endpoint_Endpoint, getValue(Endpoint_Endpoint,element));
            } else {
                Endpoint.put(Endpoint_Endpoint, null);
            }
            
            if (hasTag(Endpoint_Endpoint_comment, element)) {
                Endpoint.put(Endpoint_Endpoint_comment, getValue(Endpoint_Endpoint_comment,element));
            } else {
                Endpoint.put(Endpoint_Endpoint_comment, null);
            }
            
            if (hasTag(Endpoint_Test_type, element)) {
                Endpoint.put(Endpoint_Test_type, getValue(Endpoint_Test_type,element));
            } else {
                Endpoint.put(Endpoint_Test_type, null);
            }
            
            if (hasTag(Endpoint_Duration_value, element)) {
                Endpoint.put(Endpoint_Duration_value, getValue(Endpoint_Duration_value,element));
            } else {
                Endpoint.put(Endpoint_Duration_value, null);
            }

            if (hasTag(Endpoint_Duration_unit, element)) {
                Endpoint.put(Endpoint_Duration_unit, getValue(Endpoint_Duration_unit,element));
            } else {
                Endpoint.put(Endpoint_Duration_unit, null);
            }

            if (hasTag(Endpoint_Effect, element)) {
                Endpoint.put(Endpoint_Effect, getValue(Endpoint_Effect,element));
            } else {
                Endpoint.put(Endpoint_Effect, null);
            }

            if (hasTag(Endpoint_Kingdom, element)) {
                Endpoint.put(Endpoint_Kingdom, getValue(Endpoint_Kingdom,element));
            } else {
                Endpoint.put(Endpoint_Kingdom, null);
            }

            if (hasTag(Endpoint_Phylum, element)) {
                Endpoint.put(Endpoint_Phylum, getValue(Endpoint_Phylum,element));
            } else {
                Endpoint.put(Endpoint_Phylum, null);
            }

            if (hasTag(Endpoint_Class, element)) {
                Endpoint.put(Endpoint_Class, getValue(Endpoint_Class,element));
            } else {
                Endpoint.put(Endpoint_Class, null);
            }

            if (hasTag(Endpoint_Test_organisms_species, element)) {
                Endpoint.put(Endpoint_Test_organisms_species, getValue(Endpoint_Test_organisms_species,element));
            } else {
                Endpoint.put(Endpoint_Test_organisms_species, null);
            }

            if (hasTag(Endpoint_Sex, element)) {
                Endpoint.put(Endpoint_Sex, getValue(Endpoint_Sex,element));
            } else {
                Endpoint.put(Endpoint_Sex, null);
            }

            if (hasTag(Endpoint_Route_of_administration, element)) {
                Endpoint.put(Endpoint_Route_of_administration, getValue(Endpoint_Route_of_administration,element));
            } else {
                Endpoint.put(Endpoint_Route_of_administration, null);
            }

            if (hasTag(Endpoint_Organ, element)) {
                Endpoint.put(Endpoint_Organ, getValue(Endpoint_Organ,element));
            } else {
                Endpoint.put(Endpoint_Organ, null);
            }

            if (hasTag(Endpoint_Gene_name, element)) {
                Endpoint.put(Endpoint_Gene_name, getValue(Endpoint_Gene_name,element));
            } else {
                Endpoint.put(Endpoint_Gene_name, null);
            }

            if (hasTag(Endpoint_Strain, element)) {
                Endpoint.put(Endpoint_Strain, getValue(Endpoint_Strain,element));
            } else {
                Endpoint.put(Endpoint_Strain, null);
            }

            if (hasTag(Endpoint_Metabolic_activation, element)) {
                Endpoint.put(Endpoint_Metabolic_activation, getValue(Endpoint_Metabolic_activation,element));
            } else {
                Endpoint.put(Endpoint_Metabolic_activation, null);
            }

            if (hasTag(Endpoint_Test_specificity, element)) {
                Endpoint.put(Endpoint_Test_specificity, getValue(Endpoint_Test_specificity,element));
            } else {
                Endpoint.put(Endpoint_Test_specificity, null);
            }

            if (hasTag(Endpoint_Test_condition, element)) {
                Endpoint.put(Endpoint_Test_condition, getValue(Endpoint_Test_condition,element));
            } else {
                Endpoint.put(Endpoint_Test_condition, null);
            }

            if (hasTag(Endpoint_Type_of_method, element)) {
                Endpoint.put(Endpoint_Type_of_method, getValue(Endpoint_Type_of_method,element));
            } else {
                Endpoint.put(Endpoint_Type_of_method, null);
            }

            if (hasTag(Endpoint_Assay_provider, element)) {
                Endpoint.put(Endpoint_Assay_provider, getValue(Endpoint_Assay_provider,element));
            } else {
                Endpoint.put(Endpoint_Assay_provider, null);
            }

            if (hasTag(Endpoint_Test_guideline, element)) {
                Endpoint.put(Endpoint_Test_guideline, getValue(Endpoint_Test_guideline,element));
            } else {
                Endpoint.put(Endpoint_Test_guideline, null);
            }

            // STATS
            nodes = doc.getElementsByTagName("Stats");
            element = (Element) nodes.item(0);

            if (hasTag(Stats_n_Train, element)) {
                Stats.put(Stats_n_Train, getValue(Stats_n_Train,element));
            } else {
                Stats.put(Stats_n_Train, null);
            }

            if (hasTag(Stats_R2_Train, element)) {
                Stats.put(Stats_R2_Train, getValue(Stats_R2_Train,element));
            } else {
                Stats.put(Stats_R2_Train, null);
            }

            if (hasTag(Stats_RMSE_Train, element)) {
                Stats.put(Stats_RMSE_Train, getValue(Stats_RMSE_Train,element));
            } else {
                Stats.put(Stats_RMSE_Train, null);
            }

            if (hasTag(Stats_R2adj_Train, element)) {
                Stats.put(Stats_R2adj_Train, getValue(Stats_R2adj_Train,element));
            } else {
                Stats.put(Stats_R2adj_Train, null);
            }

            if (hasTag(Stats_Q2_Train, element)) {
                Stats.put(Stats_Q2_Train, getValue(Stats_Q2_Train,element));
            } else {
                Stats.put(Stats_Q2_Train, null);
            }

            if (hasTag(Stats_Fisher_Train, element)) {
                Stats.put(Stats_Fisher_Train, getValue(Stats_Fisher_Train,element));
            } else {
                Stats.put(Stats_Fisher_Train, null);
            }

            if (hasTag(Stats_S_Train, element)) {
                Stats.put(Stats_S_Train, getValue(Stats_S_Train,element));
            } else {
                Stats.put(Stats_S_Train, null);
            }

            if (hasTag(Stats_Sdev_Train, element)) {
                Stats.put(Stats_Sdev_Train, getValue(Stats_Sdev_Train,element));
            } else {
                Stats.put(Stats_Sdev_Train, null);
            }

            if (hasTag(Stats_SSR_Train, element)) {
                Stats.put(Stats_SSR_Train, getValue(Stats_SSR_Train,element));
            } else {
                Stats.put(Stats_SSR_Train, null);
            }

            if (hasTag(Stats_n_Invisible_training, element)) {
                Stats.put(Stats_n_Invisible_training, getValue(Stats_n_Invisible_training,element));
            } else {
                Stats.put(Stats_n_Invisible_training, null);
            }

            if (hasTag(Stats_R2_Invisible_training, element)) {
                Stats.put(Stats_R2_Invisible_training, getValue(Stats_R2_Invisible_training,element));
            } else {
                Stats.put(Stats_R2_Invisible_training, null);
            }

            if (hasTag(Stats_RMSE_Invisible_training, element)) {
                Stats.put(Stats_RMSE_Invisible_training, getValue(Stats_RMSE_Invisible_training,element));
            } else {
                Stats.put(Stats_RMSE_Invisible_training, null);
            }

            if (hasTag(Stats_Q2_Invisible_training, element)) {
                Stats.put(Stats_Q2_Invisible_training, getValue(Stats_Q2_Invisible_training,element));
            } else {
                Stats.put(Stats_Q2_Invisible_training, null);
            }

            if (hasTag(Stats_Fisher_Invisible_training, element)) {
                Stats.put(Stats_Fisher_Invisible_training, getValue(Stats_Fisher_Invisible_training,element));
            } else {
                Stats.put(Stats_Fisher_Invisible_training, null);
            }

            if (hasTag(Stats_S_Invisible_training, element)) {
                Stats.put(Stats_S_Invisible_training, getValue(Stats_S_Invisible_training,element));
            } else {
                Stats.put(Stats_S_Invisible_training, null);
            }

            if (hasTag(Stats_n_Calibration, element)) {
                Stats.put(Stats_n_Calibration, getValue(Stats_n_Calibration,element));
            } else {
                Stats.put(Stats_n_Calibration, null);
            }

            if (hasTag(Stats_R2_Calibration, element)) {
                Stats.put(Stats_R2_Calibration, getValue(Stats_R2_Calibration,element));
            } else {
                Stats.put(Stats_R2_Calibration, null);
            }

            if (hasTag(Stats_RMSE_Calibration, element)) {
                Stats.put(Stats_RMSE_Calibration, getValue(Stats_RMSE_Calibration,element));
            } else {
                Stats.put(Stats_RMSE_Calibration, null);
            }

            if (hasTag(Stats_Q2_Calibration, element)) {
                Stats.put(Stats_Q2_Calibration, getValue(Stats_Q2_Calibration,element));
            } else {
                Stats.put(Stats_Q2_Calibration, null);
            }

            if (hasTag(Stats_Fisher_Calibration, element)) {
                Stats.put(Stats_Fisher_Calibration, getValue(Stats_Fisher_Calibration,element));
            } else {
                Stats.put(Stats_Fisher_Calibration, null);
            }

            if (hasTag(Stats_S_calibration, element)) {
                Stats.put(Stats_S_calibration, getValue(Stats_S_calibration,element));
            } else {
                Stats.put(Stats_S_calibration, null);
            }


            if (hasTag(Stats_n_Test, element)) {
                Stats.put(Stats_n_Test, getValue(Stats_n_Test,element));
            } else {
                Stats.put(Stats_n_Test, null);
            }

            if (hasTag(Stats_R2_Test, element)) {
                Stats.put(Stats_R2_Test, getValue(Stats_R2_Test,element));
            } else {
                Stats.put(Stats_R2_Test, null);
            }

            if (hasTag(Stats_RMSE_Test, element)) {
                Stats.put(Stats_RMSE_Test, getValue(Stats_RMSE_Test,element));
            } else {
                Stats.put(Stats_RMSE_Test, null);
            }
            
            if (hasTag(Stats_R2adj_Test, element)) {
                Stats.put(Stats_R2adj_Test, getValue(Stats_R2adj_Test,element));
            } else {
                Stats.put(Stats_R2adj_Test, null);
            }
            
            if (hasTag(Stats_Fisher_Test, element)) {
                Stats.put(Stats_Fisher_Test, getValue(Stats_Fisher_Test,element));
            } else {
                Stats.put(Stats_Fisher_Test, null);
            }
            
            if (hasTag(Stats_S_Test, element)) {
                Stats.put(Stats_S_Test, getValue(Stats_S_Test,element));
            } else {
                Stats.put(Stats_S_Test, null);
            }

            if (hasTag(Stats_Sdev_Test, element)) {
                Stats.put(Stats_Sdev_Test, getValue(Stats_Sdev_Test,element));
            } else {
                Stats.put(Stats_Sdev_Test, null);
            }

            if (hasTag(Stats_SSR_Test, element)) {
                Stats.put(Stats_SSR_Test, getValue(Stats_Sdev_Test,element));
            } else {
                Stats.put(Stats_SSR_Test, null);
            }

            if (hasTag(Stats_Accuracy_Train, element)) {
                Stats.put(Stats_Accuracy_Train, getValue(Stats_Accuracy_Train,element));
            } else {
                Stats.put(Stats_Accuracy_Train, null);
            }

            if (hasTag(Stats_Specificity_Train, element)) {
                Stats.put(Stats_Specificity_Train, getValue(Stats_Specificity_Train,element));
            } else {
                Stats.put(Stats_Specificity_Train, null);
            }

            if (hasTag(Stats_Sensitivity_Train, element)) {
                Stats.put(Stats_Sensitivity_Train, getValue(Stats_Sensitivity_Train,element));
            } else {
                Stats.put(Stats_Sensitivity_Train, null);
            }

            if (hasTag(Stats_Accuracy_Test, element)) {
                Stats.put(Stats_Accuracy_Test, getValue(Stats_Accuracy_Test,element));
            } else {
                Stats.put(Stats_Accuracy_Test, null);
            }

            if (hasTag(Stats_Specificity_Test, element)) {
                Stats.put(Stats_Specificity_Test, getValue(Stats_Specificity_Test,element));
            } else {
                Stats.put(Stats_Specificity_Test, null);
            }

            if (hasTag(Stats_Sensitivity_Test, element)) {
                Stats.put(Stats_Sensitivity_Test, getValue(Stats_Sensitivity_Test,element));
            } else {
                Stats.put(Stats_Sensitivity_Test, null);
            }

        } catch (Exception e) {
            throw new InitFailureException("Unable to read model data from XML (" + e.getMessage() + ")");
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

}
