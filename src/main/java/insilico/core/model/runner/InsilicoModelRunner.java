package insilico.core.model.runner;

import insilico.core.exception.GenericFailureException;
import insilico.core.model.InsilicoModel;
import insilico.core.model.iInsilicoModel;
import insilico.core.model.iInsilicoModelConsensus;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.acf.ACFBuilder;
import insilico.core.similarity.SimilarityDescriptorsBuilder;

import java.util.ArrayList;

/**
 * Main class for running multiple models
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public abstract class InsilicoModelRunner {

    protected final ArrayList<InsilicoModelWrapper> ModelWrappers;
    protected final ArrayList<InsilicoModelConsensusWrapper> ModelConsensusWrappers;
    protected final SimilarityDescriptorsBuilder SimBuild;
    protected final ACFBuilder ACFBuild;

    protected iInsilicoModelRunnerMessenger Messenger;
    protected boolean PurgeModels;

    public InsilicoModelRunner() {
        ModelWrappers = new ArrayList<>();
        ModelConsensusWrappers = new ArrayList<>();
        Messenger = null;
        PurgeModels = false;

        // Builder objects for similarity descriptors and ACF
        SimBuild = new SimilarityDescriptorsBuilder();
        ACFBuild = new ACFBuilder(1);
        ACFBuild.DoNotSplitRings = false;
    }

    // GETTERS AND SETTERS
    /**
     * @return the Messenger
     */
    public iInsilicoModelRunnerMessenger getMessenger() {
        return Messenger;
    }

    /**
     * @param Messenger the Messenger to set
     */
    public void setMessenger(iInsilicoModelRunnerMessenger Messenger) {
        this.Messenger = Messenger;
    }

    /**
     * @return the PurgeModels
     */
    public boolean isPurgeModels() {
        return PurgeModels;
    }

    /**
     * @param PurgeModels the PurgeModels to set
     */
    public void setPurgeModels(boolean PurgeModels) {
        this.PurgeModels = PurgeModels;
    }

    public ArrayList<InsilicoModelWrapper> GetModelWrappers() {
        return ModelWrappers;
    }


    public InsilicoModelWrapper GetModelWrapper(Class ModelClass) throws GenericFailureException {
        for (InsilicoModelWrapper model : ModelWrappers)
            if (model.getModel().getClass()== ModelClass)
                return model;
        throw new GenericFailureException("Model wrapper not found");
    }


    public ArrayList<InsilicoModelConsensusWrapper> GetModelConsensusWrappers() {
        return ModelConsensusWrappers;
    }


    public InsilicoModelConsensusWrapper GetModelConsensusWrapper(Class ModelClass) throws GenericFailureException {
        for (InsilicoModelConsensusWrapper model : ModelConsensusWrappers)
            if (model.getModel().getClass()== ModelClass)
                return model;
        throw new GenericFailureException("Model wrapper not found");
    }

    // ABSTRACT METHODS
    public abstract void Run(ArrayList<InsilicoMolecule> Mols) throws GenericFailureException;
    public abstract void Run(InsilicoMolecule Mol) throws GenericFailureException;

    // METHODS
    public void AddModel(InsilicoModel model){
        boolean found = false;
        for(InsilicoModelWrapper modelWrapper : ModelWrappers){
            if (modelWrapper.getModel().getInfo().getKey().equals(model.getInfo().getKey())) {
                found = true;
                break;
            }
        }
        if (!found){
            ModelWrappers.add(new InsilicoModelWrapper(model));
        }
    }

    public void AddModel(iInsilicoModel model, boolean FlagForOutput) {
        boolean found = false;
        for (InsilicoModelWrapper modelWrapper : ModelWrappers)
            if (modelWrapper.getModel().getInfo().getKey().equals(model.getInfo().getKey())) {
                found = true;
                break;
            }
        if (!found)
            ModelWrappers.add(new InsilicoModelWrapper(model, FlagForOutput));
    }

    public void AddModel(iInsilicoModelConsensus model) {
        boolean found = false;
        for (InsilicoModelConsensusWrapper mw : ModelConsensusWrappers)
            if (mw.getModel().getInfo().getKey().equals(model.getInfo().getKey())) {
                found = true;
                break;
            }
        if (!found)
            ModelConsensusWrappers.add(new InsilicoModelConsensusWrapper(model));
    }

}
