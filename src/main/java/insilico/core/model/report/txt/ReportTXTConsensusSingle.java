package insilico.core.model.report.txt;

import insilico.core.exception.InitFailureException;
import insilico.core.model.InsilicoModelConsensusOutput;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.iInsilicoModelConsensus;
import insilico.core.model.runner.InsilicoModelConsensusWrapper;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.version.InsilicoInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ReportTXTConsensusSingle {

    static Logger logger = LoggerFactory.getLogger(ReportTXTConsensusSingle.class);

    /**
     * Writes the single report from the consensus model wrapper to std output
     *
     * @param ModelWrapper
     */
    public static void PrintReport(ArrayList<InsilicoMolecule> inputMols, InsilicoModelConsensusWrapper ModelWrapper) {
        PrintWriter pw = new PrintWriter(System.out);
        PrintReport(inputMols, ModelWrapper, pw);
    }

    /**
     * Writes the single report from the consensus model wrapper to a new text file
     *
     * @param ModelWrapper
     * @param FileName
     * @throws java.io.IOException
     */
    public static void PrintReport(ArrayList<InsilicoMolecule> inputMols, InsilicoModelConsensusWrapper ModelWrapper, String FileName) throws IOException {
        PrintWriter pw = new PrintWriter(new File(FileName));
        PrintReport(inputMols, ModelWrapper, pw);
        pw.close();
    }


    /**
     * Writes the single report from the consensus model wrapper to the given PrintWriter
     * object. Overloaded methods should be used instead of this one.
     *
     * @param ModelWrapper
     * @param Out
     */
    public static void PrintReport(ArrayList<InsilicoMolecule> inputMols, InsilicoModelConsensusWrapper ModelWrapper, PrintWriter Out) {

        iInsilicoModelConsensus Model = ModelWrapper.getModel();
        ArrayList<InsilicoModelConsensusOutput> Results = ModelWrapper.getResult();

        // Model info
        Out.print("Prediction for the consensus model:" + System.lineSeparator());
        Out.print(Model.getInfo().getName() + " (version " + Model.getInfo().getVersion() + ")" + System.lineSeparator());
        try {
            InsilicoInfo icv = new InsilicoInfo();
            Out.print("(calculation core version: " + icv.getVersion() + ")" + System.lineSeparator());
        } catch (InitFailureException ex) {
            logger.warn("unable to retrieve core information - " + ex.getMessage());
        }

        Out.print(System.lineSeparator());

        if (Results.isEmpty())
            return;


        // Headers
        StringBuilder report_txt = new StringBuilder("No.\tId\tSMILES\tAssessment");
        report_txt.append("\tUsed models");
        for (String curResultName : Model.GetResultsName()) {
            report_txt.append("\t").append(curResultName);
        }
        report_txt.append("\tRemarks").append(System.lineSeparator());
        Out.print(report_txt);

        // Results
        for (int i = 0; i < Results.size(); i++) {

            InsilicoModelConsensusOutput R = Results.get(i);
            report_txt = new StringBuilder(String.valueOf(i + 1).toString());
            report_txt.append("\t").append(R.getMoleculeId());
            report_txt.append("\t").append(R.getMoleculeSMILES());

            if (R.getStatus() < InsilicoModelOutput.OUTPUT_OK) {

                report_txt.append("\t[ERROR]");
                int nCols = Model.GetResultsName().length + 2;
                for (int j=0; j<nCols; j++)
                    report_txt.append("\t-");

            } else {

                report_txt.append("\t").append(R.getAssessment());
                report_txt.append("\t").append(R.getUsedModels());
                for (int j = 0; j < R.getResults().length; j++)
                    report_txt.append("\t").append(R.getResults()[j]);

            }

            String msg = "";
            for (int j=0; j<inputMols.get(i).GetWarnings().GetSize(); j++)
                msg += "[Molecule warning] " + inputMols.get(i).GetWarnings().GetMessages(j) + ". ";
            for (int j=0; j<inputMols.get(i).GetErrors().GetSize(); j++)
                msg += "[Molecule error] " + inputMols.get(i).GetErrors().GetMessages(j) + ". ";
            if (!R.getErrMessage().isEmpty())
                msg += "[Model] " + R.getErrMessage() + ".";
            if (msg.length() == 0)
                msg = "-";

            report_txt.append("\t").append(msg);

            Out.print(report_txt + System.lineSeparator());
            Out.flush();

        }

    }


}
