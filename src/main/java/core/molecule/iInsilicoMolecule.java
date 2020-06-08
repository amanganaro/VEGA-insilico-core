package core.molecule;

import core.exception.InvalidMoleculeException;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public interface iInsilicoMolecule {

    public boolean IsValid();
    public void SetIsValid(boolean Valid);

    public boolean IsExplicitHydrogen();
    public void SetExplicitHydrogen(boolean ExplicitHydrogen);

    public boolean IsDisconnected();
    public void SetIsDisconnected(boolean Disconnected);

    public boolean Is3D();
    public void SetIs3D(boolean ThreeD);

    public String GetName();
    public void SetName(String Name);

    public String GetCAS();
    public void SetCAS(String CAS);

    public String GetSMILES();
    public void SetSMILES(String SMILES);

    public IAtomContainer GetStructure() throws InvalidMoleculeException;
    public void SetStructure(IAtomContainer Structure, boolean UpdateSMILES);

    public InSilicoMoleculeCache GetCache();

    public ArrayList<String> GetWarnings();
    public String GetFormattedWarnings();
    public void AddWarning(String message);

}
