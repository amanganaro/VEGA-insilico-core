package insilico.core.model.report.txt;

import insilico.core.ad.item.iADIndex;
import insilico.core.exception.InitFailureException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.iInsilicoModel;
import insilico.core.model.runner.InsilicoModelWrapper;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.tools.utils.ModelUtilities;
import insilico.core.version.InsilicoInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class ReportTXTSingle {


    /**
     * Writes the single report from the model wrapper to std output
     *
     * @param ModelWrapper
     */
    public static void PrintReport(ArrayList<InsilicoMolecule> inputMols, InsilicoModelWrapper ModelWrapper) {
        PrintWriter pw = new PrintWriter(System.out);
        PrintReport(inputMols, ModelWrapper, pw);
    }

    /**
     * Writes the single report from the model wrapper to the given PrintWriter
     * object. Overloaded methods should be used instead of this one.
     *
     * @param ModelWrapper
     * @param Out
     */
    public static void PrintReport(ArrayList<InsilicoMolecule> inputMols, InsilicoModelWrapper ModelWrapper, PrintWriter Out) {

        iInsilicoModel Model = ModelWrapper.getModel();
        ArrayList<InsilicoModelOutput> Results = ModelWrapper.getResult();

        // Model info
        Out.print(StringSelectorCore.getString("report_txt_intro") + System.lineSeparator());
        Out.print(String.format(StringSelectorCore.getString("report_txt_version"), Model.getInfo().getName(), Model.getInfo().getVersion()) + System.lineSeparator());
        try {
            InsilicoInfo icv = new InsilicoInfo();
            Out.print(String.format(StringSelectorCore.getString("report_txt_core_version"), icv.getVersion()) + System.lineSeparator());
        } catch (InitFailureException ex) {
            log.warn(String.format(StringSelectorCore.getString("report_txt_init_exception"), ex.getMessage()));
        }

        Out.print(System.lineSeparator());

        if (Results.isEmpty())
            return;


        // Headers
        StringBuilder report_txt = new StringBuilder(String.format(StringSelectorCore.getString("report_txt_header"), "\t","\t","\t"));
        for (String curResultName : Model.GetResultsName()) {
            report_txt.append("\t").append(curResultName);
        }
        report_txt.append("\t").append(StringSelectorCore.getString("report_txt_experimental"));
        if (Model.GetTrainingSet().hasUnits())
            report_txt.append(" [").append(Model.GetTrainingSet().getUnits()).append("]");
        if (Model.getInfo().hasAlerts())
            report_txt.append("\t").append(StringSelectorCore.getString("report_txt_struct_alerts"));
        report_txt.append("\t").append(StringSelectorCore.getString("report_txt_adi"));
        for (String curADIName : Model.GetADItemsName()) {
            report_txt.append("\t").append(curADIName);
        }
        report_txt.append("\t").append(StringSelectorCore.getString("report_txt_struct_remarks")).append(System.lineSeparator());
        Out.print(report_txt);

        // Results
        for (int i = 0; i < Results.size(); i++) {

            InsilicoModelOutput R = Results.get(i);
            report_txt = new StringBuilder(String.valueOf(i + 1).toString());
            report_txt.append("\t").append(R.getMoleculeId());
            report_txt.append("\t").append(R.getMoleculeSMILES());

            if (R.getStatus() < InsilicoModelOutput.OUTPUT_OK) {

                report_txt.append("\t").append("[").append(StringSelectorCore.getString("report_txt_struct_error")).append("]");
                int nCols = Model.GetResultsName().length + 1 + (Model.getInfo().hasAlerts()?1:0) + 1 + Model.GetADItemsName().length;
                for (int j=0; j<nCols; j++)
                    report_txt.append("\t-");

            } else {

                report_txt.append("\t").append(R.getAssessment());
                for (int j = 0; j < R.getResults().length; j++)
                    report_txt.append("\t").append(R.getResults()[j]);
                report_txt.append("\t").append(R.getExperimentalFormatted());
                if (Model.getInfo().hasAlerts())
                    report_txt.append("\t").append(ModelUtilities.BuildSANameList(R.getSAList().getSAList()));

                if (R.getStatus() != InsilicoModelOutput.OUTPUT_OK_AD_MISSING) {
                    report_txt.append("\t").append(R.getADI().GetIndexValueFormatted());
                    for (iADIndex a : R.getADIndex()) {
                        report_txt.append("\t").append(a.GetIndexValueFormatted());
                    }
                } else {
                    int nCols = 1 + Model.GetADItemsName().length;
                    for (int j=0; j<nCols; j++)
                        report_txt.append("\t-");
                }

            }

            StringBuilder msg = new StringBuilder();
            for (int j=0; j<inputMols.get(i).GetWarnings().GetSize(); j++)
                msg.append("[").append(StringSelectorCore.getString("report_txt_molecule_warning")).append("]").append(inputMols.get(i).GetWarnings().GetMessages(j)).append(". ");
            for (int j=0; j<inputMols.get(i).GetErrors().GetSize(); j++)
                msg.append("[").append(StringSelectorCore.getString("report_txt_molecule_error")).append("]").append(inputMols.get(i).GetErrors().GetMessages(j)).append(". ");
            if (!R.getErrMessage().isEmpty())
                msg.append("[").append(StringSelectorCore.getString("report_txt_model")).append("]").append(R.getErrMessage()).append(".");
            if (msg.length() == 0)
                msg = new StringBuilder("-");

            report_txt.append("\t").append(msg);

            Out.print(report_txt + System.lineSeparator());
            Out.flush();

        }

    }


}
