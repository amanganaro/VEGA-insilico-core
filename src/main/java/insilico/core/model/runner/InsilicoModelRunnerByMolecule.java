package insilico.core.model.runner;

import insilico.core.alerts.AlertsEngine;
import insilico.core.descriptor.DescriptorsEngine;
import insilico.core.descriptor.blocks.Constitutional;
import insilico.core.descriptor.blocks.FunctionalGroups;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.model.InsilicoModelPython;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.python.CdddDescriptors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            Messenger.SendMessage(StringSelectorCore.getString("runner_consensus_init_engine"));
        DescriptorsEngine descriptorsEngine;
        AlertsEngine alertsEngine;
        CdddDescriptors cdddDescriptors = null;

        try{
            descriptorsEngine = new DescriptorsEngine();
            alertsEngine = new AlertsEngine();

            boolean isTherePythonModel = false;
            boolean isTherePythonModelUsingCDDD = false;

            for (InsilicoModelWrapper wrapper : ModelWrappers){
                descriptorsEngine.AddDescriptorBlock(wrapper.getModel().GetRequiredDescriptorBlocks());
                alertsEngine.AddAlertsBlock(wrapper.getModel().GetRequiredAlertBlocks());
                if(InsilicoModelPython.class.isAssignableFrom(wrapper.getModel().getClass())){
                    isTherePythonModel = true;
                    if(((InsilicoModelPython) wrapper.getModel()).isUsingCdddDescriptor()){
                        isTherePythonModelUsingCDDD = true;
                    }
                }
            }

            // Initialize CDDD Descriptor class
            if(isTherePythonModelUsingCDDD){
                List<String> smilesList = Mols.stream().map(InsilicoMolecule::getInputSMILES).collect(Collectors.toList());
                cdddDescriptors = new CdddDescriptors(smilesList, false);
                if(!cdddDescriptors.calculateDescriptors()){
                    throw new GenericFailureException(String.format(StringSelectorCore
                            .getString("runner_consensus_exception_init_blocks"),
                                "CDDD descriptors failing execution"));
                }
            }

            // Add dependencies for similarity calculation
            descriptorsEngine.AddDescriptorBlock(new Constitutional());
            descriptorsEngine.AddDescriptorBlock(new FunctionalGroups());

        } catch (InitFailureException | CloneNotSupportedException | IOException | URISyntaxException |
                 InterruptedException ex) {
            throw new GenericFailureException(String.format(StringSelectorCore
                    .getString("runner_consensus_exception_init_blocks"), ex.getMessage()));
        }

        // Reset results in each model wrapper
        for (InsilicoModelWrapper wrapper : ModelWrappers) {
            wrapper.ResetResults();
            if(InsilicoModelPython.class.isAssignableFrom(wrapper.getModel().getClass()) &&
                    ((InsilicoModelPython) wrapper.getModel()).isUsingCdddDescriptor()){
                ((InsilicoModelPython) wrapper.getModel()).setDescriptorGenerator(cdddDescriptors);
            }
        }
        for(InsilicoModelConsensusWrapper wrapper : ModelConsensusWrappers)
            wrapper.ResetResult();

        // Calculate descriptors and models on each molecule
        if (Messenger != null)
            Messenger.SendMessage(StringSelectorCore.getString("runner_consensus_run_models"));

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
                    throw new GenericFailureException(String.format(StringSelectorCore.getString("runner_consensus_exception_acf"),ex.getMessage()));
                }
//                molecule.SetSimilarityDescriptors(SimBuild.Calculate(molecule));
                try {
                    molecule.PurgeAlerts();
                    molecule.AddAlert(alertsEngine.CalculateAlerts(molecule));
                } catch (GenericFailureException | InvalidMoleculeException ex) {
                    throw new GenericFailureException(String.format(StringSelectorCore.getString("runner_consensus_exception_alerts"),ex.getMessage()));
                }
            }

            // Calculate all models on the molecule
            for(InsilicoModelWrapper wrapper : ModelWrappers) {

                try {
                    if(InsilicoModelPython.class.isAssignableFrom(wrapper.getModel().getClass())){
                        wrapper.Process(molecule, null, false);
                    }else{
                        wrapper.Process(molecule, descriptorsEngine, false);
                    }
                } catch (GenericFailureException ex){
                    throw new GenericFailureException("model: " + wrapper.getModel().getInfo().getName() + " - "  + ex.getMessage());
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

        if(cdddDescriptors!=null){
            try {
                cdddDescriptors.dispose();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void Run(InsilicoMolecule Mol) throws GenericFailureException {
        ArrayList<InsilicoMolecule> Mols = new ArrayList<>();
        Mols.add(Mol);
        this.Run(Mols);
    }


}
