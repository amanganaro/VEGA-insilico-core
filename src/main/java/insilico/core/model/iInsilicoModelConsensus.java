package insilico.core.model;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.old.InsilicoModelInfoOLD;
import insilico.core.model.runner.InsilicoModelWrapper;
import insilico.core.molecule.InsilicoMolecule;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Interface for all implemented Model classes.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public interface iInsilicoModelConsensus extends Serializable {

    public InsilicoModelInfo getInfo();
    public InsilicoModelConsensusOutput Execute(InsilicoMolecule mol, int molIndex, ArrayList<InsilicoModelWrapper> ModelsResults) throws GenericFailureException;
    public ArrayList<InsilicoModel> GetRequiredModels() throws InitFailureException;
    public int GetResultsSize();
    public String[] GetResultsName();
}
