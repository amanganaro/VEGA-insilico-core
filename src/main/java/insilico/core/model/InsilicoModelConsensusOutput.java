package insilico.core.model;

public class InsilicoModelConsensusOutput {

    private static final long serialVersionUID = 1L;

    public final static short OUTPUT_OK = 1;
    public final static short OUTPUT_NOT_CALCULATED = 0;
    public final static short OUTPUT_ERROR = -1;

    public final static short ASSESS_GRAY = 0;
    public final static short ASSESS_RED = 1;
    public final static short ASSESS_GREEN = 2;
    public final static short ASSESS_YELLOW = 3;
    public final static short ASSESS_ORANGE = 4;


    private short Status;               // Status of the processing

    private String MoleculeId;
    private String MoleculeSMILES;

    private double MainResultValue;     // numerical main result
    protected boolean ExperimentalBased;
    private int UsedModels;
    private String[] Results;           // array of resulting values

    private String Assessment;          // final assessment
    private String AssessmentVerbose;   // final assessment in verbose form
    private short AssessmentStatus;     // final assessment status

    private String ErrMessage;          // Error/warning message


    public InsilicoModelConsensusOutput() {
        this.Status = OUTPUT_NOT_CALCULATED;

        this.MoleculeId = "-";
        this.MoleculeSMILES = "-";

        this.MainResultValue = 0;
        this.Results = null;
        this.UsedModels = 0;
        this.ExperimentalBased = false;

        this.ErrMessage = "";

        this.AssessmentStatus = ASSESS_GRAY;
        this.Assessment = null;
        this.AssessmentVerbose = null;
    }



    /**
     * @return the UsedModels
     */
    public int getUsedModels() {
        return UsedModels;
    }


    /**
     * @param UsedModels the UsedModels to set
     */
    public void setUsedModels(int UsedModels) {
        this.UsedModels = UsedModels;
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

    /**
     * @return the ExperimentalBased
     */
    public boolean isExperimentalBased() {
        return ExperimentalBased;
    }

    /**
     * @param ExperimentalBased the ExperimentalBased to set
     */
    public void setExperimentalBased(boolean ExperimentalBased) {
        this.ExperimentalBased = ExperimentalBased;
    }

}
