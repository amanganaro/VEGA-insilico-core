package insilico.core.model;

import lombok.Data;
import org.dmg.pmml.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class ModelReference implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<HashMap<String, String>> referenceList;
    private String QMRFLink;

    public ModelReference() {
        this.QMRFLink = "";
        this.referenceList = new ArrayList<>();
    }

}
