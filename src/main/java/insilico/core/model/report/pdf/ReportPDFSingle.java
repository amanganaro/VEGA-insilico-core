package insilico.core.model.report.pdf;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.runner.InsilicoModelConsensusWrapper;
import insilico.core.model.runner.InsilicoModelWrapper;
import insilico.core.molecule.InsilicoMolecule;

import java.util.ArrayList;

public class ReportPDFSingle extends ReportPDF {

    public ReportPDFSingle(boolean HiRes)
            throws InitFailureException {
        super(HiRes);
    }

    public byte[] CreateReport(ArrayList<InsilicoMolecule> inputMols, InsilicoModelWrapper ModelWrapper)
            throws InitFailureException, GenericFailureException {

        this.InitReport(ModelWrapper);

        this.AddCover();

        for (int i=0; i<inputMols.size(); i++) {

            // PAGE 1 - results
            this.WritePageResults(ModelWrapper, inputMols.get(i), i);

            // PAGE 1 bis - Classification bar representation
            if (ModelWrapper.getResult().get(i).getStatus() == InsilicoModelOutput.OUTPUT_OK)
                this.WritePageClassBar(ModelWrapper, inputMols.get(i), i);

            // PAGE 2 - similar compounds
            // only if some are available (it is printed also when AD is not calculated)
            if (!ModelWrapper.getResult().get(i).getSimilarMolecules().isEmpty())
                this.WritePageSimilarMolecules(ModelWrapper, inputMols.get(i), i);

            // if output is in error state, stops here
            if (ModelWrapper.getResult().get(i).getStatus() != InsilicoModelOutput.OUTPUT_OK)
//            || (ModelWrapper.getResult().get(i).getStatus() != InsilicoModelOutput.OUTPUT_OK_AD_MISSING)
//            || (ModelWrapper.getResult().get(i).getStatus() != InsilicoModelOutput.OUTPUT_OK_AD_NOT_APPLICABLE))
                continue;



//            {
//                if(ModelWrapper.getResult().get(i).getStatus() != InsilicoModelOutput.OUTPUT_OK_AD_MISSING)
//                    if(ModelWrapper.getResult().get(i).getStatus() != InsilicoModelOutput.OUTPUT_OK_AD_NOT_APPLICABLE)
//                        continue;
//            }

            // PAGE 3 - AD assessment
            this.WritePageAD(ModelWrapper, inputMols.get(i), i);

            // PAGE 4 - Fragment analysis
            this.WritePageSA(ModelWrapper, inputMols.get(i), i);

            // PAGE 4 bis - ACF analysis
            this.WritePageACF(ModelWrapper, inputMols.get(i), i);

            // PAGE 5 - Descriptors analysis
            this.WritePageDescriptors(ModelWrapper, inputMols.get(i), i);

        }

        return this.GenerateReport();
    }


    public byte[] CreateReport(ArrayList<InsilicoMolecule> inputMols, InsilicoModelConsensusWrapper ModelWrapper)
            throws InitFailureException, GenericFailureException {

        this.InitReport(ModelWrapper);

        this.AddCover();

        for (int i=0; i<inputMols.size(); i++) {

            // PAGE 1 - results
            this.WritePageResults(ModelWrapper, inputMols.get(i), i);

        }

        return this.GenerateReport();
    }
}
