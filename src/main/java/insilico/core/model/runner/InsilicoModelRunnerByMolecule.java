package insilico.core.model.runner;

import insilico.core.alerts.AlertsEngine;
import insilico.core.descriptor.DescriptorsEngine;
import insilico.core.descriptor.blocks.Constitutional;
import insilico.core.descriptor.blocks.FunctionalGroups;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;

import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class InsilicoModelRunnerByMolecule extends InsilicoModelRunner {

    public InsilicoModelRunnerByMolecule(){}

    @Override
    public void Run(ArrayList<InsilicoMolecule> Mols) throws GenericFailureException {

        // Create descriptor and alert engine
        if (Messenger != null)
            Messenger.SendMessage("Initializing molecular descriptors engine...");
        DescriptorsEngine descriptorsEngine;
        AlertsEngine alertsEngine;

        try{
            descriptorsEngine = new DescriptorsEngine();
            alertsEngine = new AlertsEngine();

            for (InsilicoModelWrapper wrapper : ModelWrappers){
                descriptorsEngine.AddDescriptorBlock(wrapper.getModel().GetRequiredDescriptorBlocks());
                alertsEngine.AddAlertsBlock(wrapper.getModel().GetRequiredAlertBlocks());
            }

            // Add dependencies for similarity calculation
            descriptorsEngine.AddDescriptorBlock(new Constitutional());
            descriptorsEngine.AddDescriptorBlock(new FunctionalGroups());

        } catch (InitFailureException | CloneNotSupportedException ex) {
            throw new GenericFailureException("Unable to initialize descriptor blocks - " + ex.getMessage());
        }

        // Reset results in each model wrapper
        for (InsilicoModelWrapper wrapper : ModelWrappers)
            wrapper.ResetResults();
        for(InsilicoModelConsensusWrapper wrapper : ModelConsensusWrappers)
            wrapper.ResetResult();

        // Calculate descriptors and models on each molecule
        if (Messenger != null)
            Messenger.SendMessage("Running models...");

        int moleculeIndex = 0;
        for(InsilicoMolecule molecule : Mols){

            if(Messenger != null)
                Messenger.UpdateProgress();

            // Calculate current descriptor
            descriptorsEngine.CalculateDescriptors(molecule);

            // Set similarity descriptors, ACF and alerts
            if(molecule.IsValid()){
                try {
                    molecule.SetACF(ACFBuild.CreateList(molecule));
                } catch (InvalidMoleculeException ex) {
                    throw new GenericFailureException("Unable to calculace ACF - " + ex.getMessage());
                }
                molecule.SetSimilarityDescriptors(SimBuild.Calculate(molecule));
                try {
                    molecule.PurgeAlerts();
                    molecule.AddAlert(alertsEngine.CalculateAlerts(molecule));
                } catch (GenericFailureException | InvalidMoleculeException ex) {
                    throw new GenericFailureException("Unable to calculate alerts - " + ex.getMessage());
                }
            }

            // Calculate all models on the molecule
            for(InsilicoModelWrapper wrapper : ModelWrappers) {
                try {
                    wrapper.Process(molecule, descriptorsEngine, false);
                } catch (GenericFailureException ex){
                    throw new GenericFailureException(ex.getMessage());
                }

                // Release memory used by loaded model TS after execution
                if (PurgeModels)
                    wrapper.getModel().Purge();
            }

            // Calculate consensus models
            if(!ModelConsensusWrappers.isEmpty())
                for(InsilicoModelConsensusWrapper wrapper : ModelConsensusWrappers) {
                    try {
                        wrapper.Process(molecule, moleculeIndex, ModelWrappers);

                    } catch (GenericFailureException ex){
                        throw new GenericFailureException(ex.getMessage());
                    }
                }

            moleculeIndex++;

        }
    }

    @Override
    public void Run(InsilicoMolecule Mol) throws GenericFailureException {
        ArrayList<InsilicoMolecule> Mols = new ArrayList<>();
        Mols.add(Mol);
        this.Run(Mols);
    }


}
