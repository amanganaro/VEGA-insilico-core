package insilico.core.model.runner;

import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.model.InsilicoModelConsensus;
import insilico.core.model.InsilicoModelConsensusOutput;
import insilico.core.model.iInsilicoModelConsensus;
import insilico.core.molecule.InsilicoMolecule;

import java.util.ArrayList;

public class InsilicoModelConsensusWrapper {

    private final iInsilicoModelConsensus model;
    private ArrayList<InsilicoModelConsensusOutput> result;

    // CONSTRUCTOR
    public InsilicoModelConsensusWrapper(iInsilicoModelConsensus Model){
        model = Model;
        result = new ArrayList<>();
    }

    // GETTERS AND SETTERS
    /**
     * @return the model
     */
    public iInsilicoModelConsensus getModel() {
        return model;
    }


    /**
     * @return the result
     */
    public ArrayList<InsilicoModelConsensusOutput> getResult() {
        return result;
    }


    /**
     * @param result the result to set
     */
    public void setResult(ArrayList<InsilicoModelConsensusOutput> result) {
        this.result = result;
    }


    public void ResetResult() {
        result = new ArrayList<>();
    }

    public void Process(InsilicoMolecule mol, int molIndex, ArrayList<InsilicoModelWrapper> ModelResults) throws GenericFailureException{
        try {
            result.add(model.Execute(mol, molIndex, ModelResults));
        } catch  (GenericFailureException ex){
            throw new GenericFailureException(String.format(StringSelectorCore.getString("runner_consensus_exception"),model.getInfo().getName(), ex.getMessage() ));
        }
    }

}
