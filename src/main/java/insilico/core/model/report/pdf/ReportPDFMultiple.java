package insilico.core.model.report.pdf;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.runner.InsilicoModelConsensusWrapper;
import insilico.core.model.runner.InsilicoModelWrapper;
import insilico.core.molecule.InsilicoMolecule;

import java.util.ArrayList;

public class ReportPDFMultiple extends ReportPDF {

    public ReportPDFMultiple(boolean HiRes)
            throws InitFailureException {
        super(HiRes);
    }


    public byte[] CreateReportByModel(ArrayList<InsilicoMolecule> inputMols, ArrayList<InsilicoModelWrapper> ModelsWrapper)
            throws InitFailureException, GenericFailureException {
        ArrayList<InsilicoModelConsensusWrapper> ModelsConsWrapper = new ArrayList<>();
        return CreateReportByModel(inputMols, ModelsWrapper, ModelsConsWrapper);
    }


    public byte[] CreateReportByModel(ArrayList<InsilicoMolecule> inputMols, ArrayList<InsilicoModelWrapper> ModelsWrapper,
                                      ArrayList<InsilicoModelConsensusWrapper> ModelsConsWrapper)
            throws InitFailureException, GenericFailureException {

        this.InitReport(ModelsWrapper, ModelsConsWrapper);

        this.AddCover();

        for (InsilicoModelConsensusWrapper curModel : ModelsConsWrapper) {

            for (int i=0; i<inputMols.size(); i++) {

                // PAGE 1 - results
                this.WritePageResults(curModel, inputMols.get(i), i);

            }

        }

        for (InsilicoModelWrapper curModel : ModelsWrapper) {

            if (!curModel.isFlagForOutput())
                continue;

            for (int i=0; i<inputMols.size(); i++) {

                // PAGE 1 - results
                this.WritePageResults(curModel, inputMols.get(i), i);

                // PAGE 1 bis - Classification bar representation
                if (curModel.getResult().get(i).getStatus() == InsilicoModelOutput.OUTPUT_OK)
                    this.WritePageClassBar(curModel, inputMols.get(i), i);

                // PAGE 2 - similar compounds
                if (!curModel.getResult().get(i).getSimilarMolecules().isEmpty())
                    this.WritePageSimilarMolecules(curModel, inputMols.get(i), i);

                // if output is in error state, stops here
                if (curModel.getResult().get(i).getStatus() != InsilicoModelOutput.OUTPUT_OK)
                    continue;

                // PAGE 3 - AD assessment
                this.WritePageAD(curModel, inputMols.get(i), i);

                // PAGE 4 - Fragment analysis
                this.WritePageSA(curModel, inputMols.get(i), i);

                // PAGE 4 bis - ACF analysis
                this.WritePageACF(curModel, inputMols.get(i), i);

                // PAGE 5 - Descriptors analysis
                this.WritePageDescriptors(curModel, inputMols.get(i), i);

            }
        }

        this.AddReferencePage();

        return this.GenerateReport();
    }


    public byte[] CreateReportByMolecule(ArrayList<InsilicoMolecule> inputMols, ArrayList<InsilicoModelWrapper> ModelsWrapper)
            throws InitFailureException, GenericFailureException {
        ArrayList<InsilicoModelConsensusWrapper> ModelsConsWrapper = new ArrayList<>();
        return CreateReportByMolecule(inputMols, ModelsWrapper, ModelsConsWrapper);
    }


    public byte[] CreateReportByMolecule(ArrayList<InsilicoMolecule> inputMols, ArrayList<InsilicoModelWrapper> ModelsWrapper,
                                         ArrayList<InsilicoModelConsensusWrapper> ModelsConsWrapper)
            throws InitFailureException, GenericFailureException {

        this.InitReport(ModelsWrapper, ModelsConsWrapper);

        this.AddCover();

        for (int i=0; i<inputMols.size(); i++) {

            for (InsilicoModelConsensusWrapper curModel : ModelsConsWrapper) {

                // PAGE 1 - results
                this.WritePageResults(curModel, inputMols.get(i), i);

            }

            for (InsilicoModelWrapper curModel : ModelsWrapper) {

                if (!curModel.isFlagForOutput())
                    continue;

                // PAGE 1 - results
                this.WritePageResults(curModel, inputMols.get(i), i);

                // if output is in error state, just print the first page
                if (curModel.getResult().get(i).getStatus() != InsilicoModelOutput.OUTPUT_OK)
                    continue;

                // PAGE 1 bis - Classification bar representation
                this.WritePageClassBar(curModel, inputMols.get(i), i);

                // PAGE 2 - similar compounds
                this.WritePageSimilarMolecules(curModel, inputMols.get(i), i);

                // PAGE 3 - AD assessment
                this.WritePageAD(curModel, inputMols.get(i), i);

                // PAGE 4 - Fragment analysis
                this.WritePageSA(curModel, inputMols.get(i), i);

                // PAGE 4 bis - ACF analysis
                this.WritePageACF(curModel, inputMols.get(i), i);

                // PAGE 5 - Descriptors analysis
                this.WritePageDescriptors(curModel, inputMols.get(i), i);

            }
        }

        this.AddReferencePage();

        return this.GenerateReport();
    }

}
