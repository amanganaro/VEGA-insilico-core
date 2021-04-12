package insilico.core.model.runner;


import insilico.core.descriptor.DescriptorsEngine;
import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.model.InsilicoModel;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.iInsilicoModel;
import insilico.core.molecule.InsilicoMolecule;

import java.util.ArrayList;

public class InsilicoModelWrapper {

    private final iInsilicoModel model;
    private ArrayList<InsilicoModelOutput> result;
    private boolean FlagForOutput;

    /**
     * Reset Model Wrapper results
     */
    public void ResetResults() {
        result = new ArrayList<>();
    }

    public InsilicoModelWrapper(iInsilicoModel Model, boolean FlagForOutput){
        model = Model;
        result = new ArrayList<>();
        this.FlagForOutput = FlagForOutput;
    }

    public InsilicoModelWrapper(iInsilicoModel Model) {
        this((InsilicoModel) Model, true);
    }

    public void Process(InsilicoMolecule input, DescriptorsEngine DescEngine, boolean CalculateAlerts) throws GenericFailureException {
        try {
            result.add(model.Execute(input, DescEngine, CalculateAlerts));
        } catch (GenericFailureException e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("runner_consensus_exception"), model.getInfo().getName(),  e.getMessage()));
        }
    }



    /**
     * @return the model
     */
    public iInsilicoModel getModel() {
        return model;
    }


    /**
     * @return the result
     */
    public ArrayList<InsilicoModelOutput> getResult() {
        return result;
    }


    /**
     * @param result the result to set
     */
    public void setResult(ArrayList<InsilicoModelOutput> result) {
        this.result = result;
    }

    /**
     * @return the FlagForOutput
     */
    public boolean isFlagForOutput() {
        return FlagForOutput;
    }

    /**
     * @param FlagForOutput the FlagForOutput to set
     */
    public void setFlagForOutput(boolean FlagForOutput) {
        this.FlagForOutput = FlagForOutput;
    }


}
