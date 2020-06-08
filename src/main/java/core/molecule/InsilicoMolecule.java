package core.molecule;

import core.exception.InvalidMoleculeException;
import lombok.Data;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Data
public class InsilicoMolecule implements Serializable, Cloneable, iInsilicoMolecule {

    protected static final long serialVersionUID = 1L;

    protected boolean IsValid;
    protected boolean IsDisconnected;
    protected boolean ExplicitHydrogen;

    protected String Name;
    protected String CAS;
    protected String SMILES;

    protected ArrayList<String> Warnings;

    transient protected IAtomContainer Structure;
    transient protected InSilicoMoleculeCache Cache;

    public InsilicoMolecule() {
        IsValid = false;
        IsDisconnected = false;
        ExplicitHydrogen = false;
        Name = "";
        CAS = "";
        SMILES = "";
        Warnings = new ArrayList();
        Structure = null;
        Cache = new InSilicoMoleculeCache();
    }

    @Override
    public boolean IsValid() {
        return this.IsValid;
    }

    @Override
    public void SetIsValid(boolean Valid) {
        this.IsValid = Valid;
    }

    @Override
    public boolean IsExplicitHydrogen() {
        return this.IsExplicitHydrogen();
    }

    @Override
    public void SetExplicitHydrogen(boolean ExplicitHydrogen) {
        this.ExplicitHydrogen = ExplicitHydrogen;
    }

    @Override
    public boolean IsDisconnected() {
        return this.IsDisconnected;
    }

    @Override
    public void SetIsDisconnected(boolean Disconnected) {
        this.IsDisconnected = Disconnected;
    }

    @Override
    public boolean Is3D() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void SetIs3D(boolean ThreeD) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String GetName() {
        return this.Name;
    }

    @Override
    public void SetName(String Name) {
        this.Name = Name;
    }

    @Override
    public String GetCAS() {
        return this.CAS;
    }

    @Override
    public void SetCAS(String CAS) {
        this.CAS = CAS;
    }

    @Override
    public String GetSMILES() {
        return this.SMILES;
    }

    @Override
    public void SetSMILES(String SMILES) {
        this.SMILES = SMILES;
    }

    @Override
    public IAtomContainer GetStructure() throws InvalidMoleculeException {
        if (this.Structure == null)
            throw new InvalidMoleculeException("Missing molecular structure");
        return this.Structure;
    }

    @Override
    public void SetStructure(IAtomContainer Structure, boolean UpdateSMILES) {
        this.Structure = Structure;
    }

    @Override
    public InSilicoMoleculeCache GetCache() {
        if (this.Cache == null)
            Cache = new InSilicoMoleculeCache();
        return this.Cache;
    }

    @Override
    public ArrayList<String> GetWarnings() {
        return this.Warnings;
    }

    @Override
    public String GetFormattedWarnings() {
        String warn = "";
        for (String s : Warnings) {
            if (!warn.isEmpty()) warn += "; ";
            warn += s;
        }
        return warn;
    }

    @Override
    public void AddWarning(String message) {
        this.Warnings.add(message);
    }
}
