package insilico.core.molecule.tools;

import org.openscience.cdk.interfaces.IChemObject;

import java.awt.*;

public class DepicSub {

    private Iterable<IChemObject> substructure;
    private Color colorSub;

    public DepicSub(Iterable<IChemObject> substructure, Color colorSub) {
        this.substructure = substructure;
        this.colorSub = colorSub;
    }

    public Iterable<IChemObject> getSubstructure() {
        return substructure;
    }

    public void setSubstructure(Iterable<IChemObject> substructure) {
        this.substructure = substructure;
    }

    public Color getColorSub() {
        return colorSub;
    }

    public void setColorSub(Color colorSub) {
        this.colorSub = colorSub;
    }
}
