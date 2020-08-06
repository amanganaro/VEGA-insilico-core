package insilico.core.alerts;

import insilico.core.propertycontainer.PropertyContainer;
import insilico.core.similarity.SimilarMolecule;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Base class for a Structural Alert (SA)
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Data
public class Alert extends PropertyContainer implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    // Keys for default additional properties of the alert
    // To be used with the proper method inherited from PropertyContainer
    public final static String TEXT_PROPERTY_PARENT_ALERTS = "parent_alerts";
    public final static String NUMERIC_PROPERTY_ACCURACY = "alert_acc";
    public final static String NUMERIC_PROPERTY_P_VALUE = "alert_p_value";
    public final static String BOOL_PROPERTY_ACTIVITY = "alert_activity";


    // Id of the alert
    private final int Block;
    private final String Id; // Use methods in AlertsEngine to code / decode alert id

    // Basic properties of the alert
    private String Name;
    private String Description;
    private String ImageURL;

    // Used to store similar molecules (needed in the output of VEGA models)
    private ArrayList<SimilarMolecule> SimilarMols;


    public Alert(int Block, String Id) {
        super();
        this.Block = Block;
        this.Id = Id;
        Name = "";
        Description = "";
        ImageURL = "";
        SimilarMols = new ArrayList<>();
    }


    /**
     * @return the Block
     */
    public int getBlock() {
        return Block;
    }

    /**
     * @return the Id
     */
    public String getId() {
        return Id;
    }

    /**
     * @return the Name
     */
    public String getName() {
        return Name;
    }

    /**
     * @param Name the Name to set
     */
    public void setName(String Name) {
        this.Name = Name;
    }

    /**
     * @return the Description
     */
    public String getDescription() {
        return Description;
    }

    /**
     * @param Description the Description to set
     */
    public void setDescription(String Description) {
        this.Description = Description;
    }

    /**
     * @return the ImageURL
     */
    public String getImageURL() {
        return ImageURL;
    }

    /**
     * @param ImageURL the ImageURL to set
     */
    public void setImageURL(String ImageURL) {
        this.ImageURL = ImageURL;
    }

    /**
     *
     * @param molecule
     */
    public void AddSimilarMolecule(SimilarMolecule molecule) {
        if (this.SimilarMols == null)
            SimilarMols = new ArrayList<>();
        SimilarMols.add(molecule);
    }

    /**
     *
     * @return
     */
    public ArrayList<SimilarMolecule> GetSimilarMolecules() {
        return SimilarMols;
    }

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Alert s = (Alert) super.clone();
        s.SimilarMols = new ArrayList<>();
        for (SimilarMolecule m : this.SimilarMols)
            s.SimilarMols.add((SimilarMolecule)m.clone());
        return (Object)s;
    }




}
