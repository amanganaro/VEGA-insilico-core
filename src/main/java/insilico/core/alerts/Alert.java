package insilico.core.alerts;

import insilico.core.propertycontainer.PropertyContainer;
import insilico.core.similarity.SimilarMolecule;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Data
public class Alert extends PropertyContainer implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private final int Block;
    private final String Id;
    private String Name;
    private String Description;
    private String ImageURL;
    private String ParentAlerts;
    private ArrayList<SimilarMolecule> SimilarMols;

    public Alert(int Block, String Id) {
        super();
        this.Block = Block;
        this.Id = Id;
        Name = "";
        Description = "";
        ImageURL = null;
        ParentAlerts = "";
        SimilarMols = new ArrayList<>();
    }

    public void AddSimilarMolecule(SimilarMolecule molecule) {
        if (this.SimilarMols == null)
            SimilarMols = new ArrayList<>();
        SimilarMols.add(molecule);
    }

    public ArrayList<SimilarMolecule> GetSimilarMolecules() {
        return SimilarMols;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Alert s = (Alert) super.clone();
        s.SimilarMols = new ArrayList<>();
        for (SimilarMolecule m : this.SimilarMols)
            s.SimilarMols.add((SimilarMolecule)m.clone());
        return (Object)s;
    }




}
