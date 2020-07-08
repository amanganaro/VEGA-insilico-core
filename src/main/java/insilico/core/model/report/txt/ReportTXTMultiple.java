package insilico.core.model.report.txt;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.InsilicoModelConsensusOutput;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.runner.InsilicoModelConsensusWrapper;
import insilico.core.model.runner.InsilicoModelWrapper;
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
public class ReportTXTMultiple {

    static Logger logger = LoggerFactory.getLogger(ReportTXTMultiple.class);

    /**
     * Writes the multiple report from the model wrapper to std output
     *
     * @param ModelsWrapper
     * @param ModelsConsWrapper
     * @throws insilico.core.exception.GenericFailureException
     */
    public static void PrintReport(ArrayList<InsilicoModelWrapper> ModelsWrapper,
                                   ArrayList<InsilicoModelConsensusWrapper> ModelsConsWrapper)
            throws GenericFailureException {
        PrintWriter pw = new PrintWriter(System.out);
        PrintReport(ModelsWrapper, ModelsConsWrapper, pw);
    }


    /**
     * Overload when no consensus model is passed
     *
     * @param ModelsWrapper
     * @throws GenericFailureException
     */
    public static void PrintReport(ArrayList<InsilicoModelWrapper> ModelsWrapper)
            throws GenericFailureException {
        ArrayList<InsilicoModelConsensusWrapper> ModelsConsWrapper = new ArrayList<>();
        PrintReport(ModelsWrapper, ModelsConsWrapper);
    }


    /**
     * Writes the multiple report from the model wrapper to a new text file
     *
     * @param ModelsWrapper
     * @param ModelsConsWrapper
     * @param FileName
     * @throws java.io.IOException
     * @throws insilico.core.exception.GenericFailureException
     */
    public static void PrintReport(ArrayList<InsilicoModelWrapper> ModelsWrapper,
                                   ArrayList<InsilicoModelConsensusWrapper> ModelsConsWrapper, String FileName)
            throws IOException, GenericFailureException {
        PrintWriter pw = new PrintWriter(new File(FileName));
        PrintReport(ModelsWrapper, ModelsConsWrapper, pw);
        pw.close();
    }


    /**
     * Overload when no consensus model is passed
     *
     * @param ModelsWrapper
     * @param FileName
     * @throws IOException
     * @throws GenericFailureException
     */
    public static void PrintReport(ArrayList<InsilicoModelWrapper> ModelsWrapper, String FileName)
            throws IOException, GenericFailureException {
        ArrayList<InsilicoModelConsensusWrapper> ModelsConsWrapper = new ArrayList<>();
        PrintReport(ModelsWrapper, ModelsConsWrapper, FileName);
    }


    /**
     * Writes the multiple report from the model wrapper to the given PrintWriter
     * object. Overloaded methods should be used instead of this one.
     *
     * @param ModelsWrapper
     * @param ModelsConsWrapper
     * @param Out
     * @throws insilico.core.exception.GenericFailureException
     */
    public static void PrintReport(ArrayList<InsilicoModelWrapper> ModelsWrapper,
                                   ArrayList<InsilicoModelConsensusWrapper> ModelsConsWrapper, PrintWriter Out)
            throws GenericFailureException {

        // Models info
        Out.print("Prediction and Applicability Domain analysis for models:" + System.lineSeparator());
        for (InsilicoModelConsensusWrapper curModel : ModelsConsWrapper)
            Out.print(curModel.getModel().getInfo().getName() + " (version " + curModel.getModel().getInfo().getVersion() + ")" + System.lineSeparator());
        for (InsilicoModelWrapper curModel : ModelsWrapper)
            if (curModel.isFlagForOutput())
                Out.print(curModel.getModel().getInfo().getName() + " (version " + curModel.getModel().getInfo().getVersion() + ")" + System.lineSeparator());
        try {
            InsilicoInfo icv = new InsilicoInfo();
            Out.print("(calculation core version: " + icv.getVersion() + ")" + System.lineSeparator());
        } catch (InitFailureException ex) {
            logger.warn("unable to retrieve core information - " + ex.getMessage());
        }

        Out.print(System.lineSeparator());

        if ((ModelsWrapper.size() + ModelsConsWrapper.size())==0)
            return;

        // Check molecules matching
        int nMolecules;
        if (!ModelsWrapper.isEmpty())
            nMolecules = ModelsWrapper.get(0).getResult().size();
        else
            nMolecules = ModelsConsWrapper.get(0).getResult().size();
        for (InsilicoModelWrapper curModel : ModelsWrapper)
            if (curModel.getResult().size() != nMolecules)
                throw new GenericFailureException("Number of molecules in models do not match");
        for (InsilicoModelConsensusWrapper curModel : ModelsConsWrapper)
            if (curModel.getResult().size() != nMolecules)
                throw new GenericFailureException("Number of molecules in models do not match");

        // Headers
        StringBuilder report_txt = new StringBuilder("No.\tId\tSMILES");
        for (InsilicoModelConsensusWrapper curModel : ModelsConsWrapper) {
            report_txt.append("\t").append(curModel.getModel().getInfo().getName()).append(" - assessment");
            String curUnits = ((curModel.getModel().getInfo().getUnits()!=null)&&(!curModel.getModel().getInfo().getUnits().isEmpty()))?
                    " [" + curModel.getModel().getInfo().getUnits() + "]":"";
            report_txt.append("\t").append(curModel.getModel().getInfo().getName()).append(" - prediction").append(curUnits);
        }
        for (InsilicoModelWrapper curModel : ModelsWrapper) {
            if (!curModel.isFlagForOutput())
                continue;
            report_txt.append("\t").append(curModel.getModel().getInfo().getName()).append(" - assessment");
            String curUnits = ((curModel.getModel().getInfo().getUnits()!=null)&&(!curModel.getModel().getInfo().getUnits().isEmpty()))?
                    " [" + curModel.getModel().getInfo().getUnits() + "]":"";
            report_txt.append("\t").append(curModel.getModel().getInfo().getName()).append(" - prediction").append(curUnits);
        }
        Out.print(report_txt + System.lineSeparator());

        // Results
        String[] ResId = new String[nMolecules];
        String[] ResSMILES = new String[nMolecules];
        if (!ModelsWrapper.isEmpty()) {
            for (int i = 0; i < nMolecules; i++) {
                ResId[i] = ModelsWrapper.get(0).getResult().get(i).getMoleculeId();
                ResSMILES[i] = ModelsWrapper.get(0).getResult().get(i).getMoleculeSMILES();
            }
        } else {
            for (int i = 0; i < nMolecules; i++) {
                ResId[i] = ModelsConsWrapper.get(0).getResult().get(i).getMoleculeId();
                ResSMILES[i] = ModelsConsWrapper.get(0).getResult().get(i).getMoleculeSMILES();
            }
        }

        for (int i = 0; i < nMolecules; i++) {

            report_txt = new StringBuilder(String.valueOf(i + 1).toString());
            report_txt.append("\t").append(ResId[i]);
            report_txt.append("\t").append(ResSMILES[i]);

            for (InsilicoModelConsensusWrapper curModel : ModelsConsWrapper) {
                InsilicoModelConsensusOutput curResult = curModel.getResult().get(i);
                if (curResult.getMoleculeId().compareTo(ResId[i]) != 0)
                    throw new GenericFailureException("Molecules in models do not match");

                if (curResult.getStatus() < InsilicoModelOutput.OUTPUT_OK) {
                    report_txt.append("\t[ERRROR]\t-");
                } else {
                    report_txt.append("\t").append(curResult.getAssessment());
                    report_txt.append("\t").append(curResult.getResults()[0]);
                }
            }
            for (InsilicoModelWrapper curModel : ModelsWrapper) {
                if (!curModel.isFlagForOutput())
                    continue;
                InsilicoModelOutput curResult = curModel.getResult().get(i);
                if (curResult.getMoleculeId().compareTo(ResId[i]) != 0)
                    throw new GenericFailureException("Molecules in models do not match");

                if (curResult.getStatus() < InsilicoModelOutput.OUTPUT_OK) {
                    report_txt.append("\t[ERRROR]\t-");
                } else {
                    report_txt.append("\t").append(curResult.getAssessment());
                    report_txt.append("\t").append(curResult.getResults()[0]);
                }
            }

            Out.print(report_txt + System.lineSeparator());
            Out.flush();

        }

    }


    /**
     * Overload when no consensus models are passed
     *
     * @param ModelsWrapper
     * @param Out
     * @throws GenericFailureException
     */
    public static void PrintReport(ArrayList<InsilicoModelWrapper> ModelsWrapper, PrintWriter Out)
            throws GenericFailureException {
        ArrayList<InsilicoModelConsensusWrapper> ModelsConsWrapper = new ArrayList<>();
        PrintReport(ModelsWrapper, ModelsConsWrapper, Out);
    }

}
