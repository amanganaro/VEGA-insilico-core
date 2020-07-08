package insilico.core.model;

import insilico.core.ad.item.iADIndex;
import insilico.core.ad.reasoning.iReasoningItem;
import insilico.core.alerts.AlertList;
import insilico.core.similarity.SimilarMolecule;

import java.io.Serializable;
import java.util.ArrayList;

public class InsilicoModelOutput implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static short OUTPUT_OK_AD_MISSING = 2;
    public final static short OUTPUT_OK = 1;
    public final static short OUTPUT_NOT_CALCULATED = 0;
    public final static short OUTPUT_ERROR = -1;

    public final static short ASSESS_GRAY = 0;
    public final static short ASSESS_RED = 1;
    public final static short ASSESS_GREEN = 2;
    public final static short ASSESS_YELLOW = 3;
    public final static short ASSESS_ORANGE = 4;

    // Status of the processing
    private short Status;

    private String MoleculeId;
    private String MoleculeSMILES;

    // numerical main result and array of resulting values
    private double MainResultValue;
    private String[] Results;

    // experimental value (if available) in numerical and string form
    private Double Experimental;
    private String ExperimentalFormatted;

    // ADI index and list of indices
    private iADIndex ADI;
    private ArrayList<iADIndex> ADList;

    // Additional reasoning items
    private ArrayList<iReasoningItem> ReasoningList;

    // list of most similar molecules from TS
    private ArrayList<SimilarMolecule> SimilarMols;

    // list of SA
    private AlertList SAList;

    // Final assessment, verbose form, assessment status
    private String Assessment;
    private String AssessmentVerbose;
    private short AssessmentStatus;

    // Error/warning message
    private String ErrMessage;


    /**
     * Constructor.
     */
    public InsilicoModelOutput() {
        this.Status = OUTPUT_NOT_CALCULATED;

        this.MoleculeId = "-";
        this.MoleculeSMILES = "-";

        this.MainResultValue = 0;
        this.Results = null;

        this.Experimental = null;
        this.ExperimentalFormatted = "-";

        this.ADI = null;
        this.ADList = new ArrayList<>();
        this.ReasoningList = new ArrayList<>();
        this.SimilarMols = new ArrayList<>();
        this.SAList = new AlertList();

        this.ErrMessage = "";

        this.AssessmentStatus = ASSESS_GRAY;
        this.Assessment = null;
        this.AssessmentVerbose = null;
    }

    /**
     * @return the Status
     */
    public short getStatus() {
        return Status;
    }


    /**
     * @param Status the Status to set
     */
    public void setStatus(short Status) {
        this.Status = Status;
    }


    /**
     * @return the MainResultValue
     */
    public double getMainResultValue() {
        return MainResultValue;
    }


    /**
     * @param MainResultValue the MainResultValue to set
     */
    public void setMainResultValue(double MainResultValue) {
        this.MainResultValue = MainResultValue;
    }


    /**
     * @return the Results
     */
    public String[] getResults() {
        return Results;
    }


    /**
     * @param Results the Results to set
     */
    public void setResults(String[] Results) {
        this.Results = Results;
    }


    /**
     * Reports if an experimental value is available or not
     * @return true if experimental value is available
     */
    public boolean HasExperimental() {
        if (this.Experimental != null)
            return true;
        else
            return false;
    }


    /**
     * @return the Experimental
     */
    public double getExperimental() {
        return Experimental.doubleValue();
    }


    /**
     * @param Experimental the Experimental to set
     */
    public void setExperimental(double Value) {
        this.Experimental = new Double(Value);
    }


    /**
     * @return the Experimental as (formatted) string
     */
    public String getExperimentalFormatted() {
        return ExperimentalFormatted;
    }


    /**
     * @param Value the Experimental value to set
     */
    public void setExperimentalFormatted(String Value) {
        this.ExperimentalFormatted = Value;
    }


    /**
     * @return the Assessment
     */
    public String getAssessment() {
        return Assessment;
    }


    /**
     * @param Assessment the Assessment to set
     */
    public void setAssessment(String Assessment) {
        this.Assessment = Assessment;
    }


    /**
     * @return the AssessmentVerbose
     */
    public String getAssessmentVerbose() {
        return AssessmentVerbose;
    }


    /**
     * @param AssessmentVerbose the AssessmentVerbose to set
     */
    public void setAssessmentVerbose(String AssessmentVerbose) {
        this.AssessmentVerbose = AssessmentVerbose;
    }


    /**
     * @return the AssessmentStatus
     */
    public short getAssessmentStatus() {
        return AssessmentStatus;
    }


    /**
     * @param AssessmentStatus the AssessmentStatus to set
     */
    public void setAssessmentStatus(short AssessmentStatus) {
        this.AssessmentStatus = AssessmentStatus;
    }


    public int getADIndexSize() {
        return ADList.size();
    }


    public iADIndex getADIndex(int Index) {
        if ((Index<0) || (Index>(ADList.size()-1)))
            return null;
        return ADList.get(Index);
    }


    public iADIndex getADIndex(Class IndexClass) {
        for (iADIndex item : ADList) {
            if (item.getClass() == IndexClass)
                return item;
        }
        return null;
    }


    public ArrayList<iADIndex> getADIndex() {
        return ADList;
    }


    public void addADIndex(iADIndex item) {
        ADList.add(item);
    }


    public ArrayList<iReasoningItem> getReasoningItem() {
        return ReasoningList;
    }

    public void addReasoningItem(iReasoningItem item) {
        ReasoningList.add(item);
    }

    public ArrayList<SimilarMolecule> getSimilarMolecules() {
        return SimilarMols;
    }


    public void addSimilarMolecule(SimilarMolecule item) {
        SimilarMols.add(item);
    }

    public AlertList getSAList() {
        return SAList;
    }


    public void setSAList(AlertList item) {
        SAList = item;
    }


    /**
     * @return the ADI
     */
    public iADIndex getADI() {
        return ADI;
    }

    /**
     * @param ADI the ADI to set
     */
    public void setADI(iADIndex ADI) {
        this.ADI = ADI;
    }

    /**
     * @return the ErrMessage
     */
    public String getErrMessage() {
        return ErrMessage;
    }

    /**
     * @param ErrMessage the ErrMessage to set
     */
    public void setErrMessage(String ErrMessage) {
        this.ErrMessage = ErrMessage;
    }

    /**
     * @return the MoleculeId
     */
    public String getMoleculeId() {
        return MoleculeId;
    }

    /**
     * @param MoleculeId the MoleculeId to set
     */
    public void setMoleculeId(String MoleculeId) {
        this.MoleculeId = MoleculeId;
    }

    /**
     * @return the MoleculeSMILES
     */
    public String getMoleculeSMILES() {
        return MoleculeSMILES;
    }

    /**
     * @param MoleculeSMILES the MoleculeSMILES to set
     */
    public void setMoleculeSMILES(String MoleculeSMILES) {
        this.MoleculeSMILES = MoleculeSMILES;
    }




}
