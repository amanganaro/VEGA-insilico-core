package insilico.core.model;

import insilico.core.constant.MessagesError;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.runner.InsilicoModelWrapper;
import insilico.core.molecule.InsilicoMolecule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public abstract class InsilicoModelConsensus implements iInsilicoModelConsensus{

    Logger logger = LoggerFactory.getLogger(InsilicoModelConsensus.class);

    protected final static short MODEL_ERROR = -1;
    protected final static short MODEL_CALCULATED = 1;


    protected InsilicoModelInfo Info;
    protected InsilicoMolecule CurMolecule;
    protected int CurMoleculeIndex;
    protected ArrayList<InsilicoModelWrapper> CurModels;
    protected InsilicoModelConsensusOutput CurOutput;

    protected int ResultsSize = 0;
    protected String[] ResultsName;

    protected short ModelStatus;

    protected DecimalFormat Format_2D;
    protected DecimalFormat Format_3D;
    protected DecimalFormat Format_4D;

    /**
     * Constructor. Initialize the model by creating the Info object (reading data
     * from the model.xml file that must be located in the given data path).
     * Model.xml file is the same as normal models, but some info (like the TS)
     * will be missing as not used in the consensus models.
     *
     * @param ModelData path (ending with slash) where the data objects are
     * located (model.xml, serialized ACF and SA classes if available)
     * @throws InitFailureException
     */
    public InsilicoModelConsensus(String ModelData) throws InitFailureException {

        Info = new InsilicoModelInfo(getClass().getResource(ModelData));

        ResultsName = new String[0];

        // Init decimal format objects
        DecimalFormatSymbols InternationalSymbols = new DecimalFormatSymbols();
        InternationalSymbols.setDecimalSeparator('.');
        Format_2D = new DecimalFormat("0.##", InternationalSymbols);
        Format_3D = new DecimalFormat("0.###", InternationalSymbols);
        Format_4D = new DecimalFormat("0.####", InternationalSymbols);

    }


    protected abstract short CalculateModel();
    protected abstract void CalculateAssessment();


    /**
     * @return the Info object of this model
     */
    @Override
    public InsilicoModelInfo getInfo() {
        return Info;
    }



    /**
     * Execute current model with the given input. This method should not be
     * overridden, it already implements the complete calculation pipeline.
     * Descendant classes just have to implement methods for calculating
     * the consensus model and the assessment.
     *
     * @param mol
     * @param molIndex
     * @param ModelsResults
     * @return InsilicoModelConsensusOutput object with the results of calculation
     * @throws insilico.core.exception.GenericFailureException
     */
    @Override
    public InsilicoModelConsensusOutput Execute(InsilicoMolecule mol, int molIndex, ArrayList<InsilicoModelWrapper> ModelsResults) throws GenericFailureException {

        CurMolecule = mol;
        CurMoleculeIndex = molIndex;
        CurModels = ModelsResults;

        CurOutput = new InsilicoModelConsensusOutput();
        CurOutput.setMoleculeId(CurMolecule.GetId());
        CurOutput.setMoleculeSMILES(CurMolecule.GetSMILES());

        // 0 - Check Molecule
        if(!CurMolecule.IsValid()) {
            CurOutput.setStatus(InsilicoModelOutput.OUTPUT_ERROR);
            CurOutput.setErrMessage(MessagesError.MODEL_INVALID_MOLECULE + (CurMolecule.GetErrors().GetSize()>0?(". " + CurMolecule.GetErrors().GetMessages()):""));
            CurOutput.setAssessment(MessagesError.MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE);
            CurOutput.setAssessmentVerbose(MessagesError.MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE);
            return CurOutput;
        }

        // 1 - Model calculation
        try {
            ModelStatus = CalculateModel();
        } catch (Throwable ex){
            if (ex.getClass()==OutOfMemoryError.class) throw new OutOfMemoryError(ex.getMessage());
            logger.error("Model calculation: " + ex);
            throw new GenericFailureException("Unexpected error: " + ex);
        }
        if (ModelStatus != InsilicoModel.MODEL_CALCULATED) {
            CurOutput.setStatus(InsilicoModelOutput.OUTPUT_ERROR);
            CurOutput.setErrMessage(MessagesError.MODEL_NOT_CALCULATED_ERROR);
            CurOutput.setAssessment(MessagesError.MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE);
            CurOutput.setAssessmentVerbose(MessagesError.MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE);
            return CurOutput;
        }

        // 2 - Assessment
        try {
            CalculateAssessment();
        } catch (Throwable e) {
            if (e.getClass()==OutOfMemoryError.class) throw new OutOfMemoryError(e.getMessage());
            logger.error("Assessment calculation: " + e);
            throw new GenericFailureException("Unexpected error: " + e);
        }


        CurOutput.setStatus(InsilicoModelOutput.OUTPUT_OK);

        return CurOutput;
    }

    @Override
    public ArrayList<InsilicoModel> GetRequiredModels() throws InitFailureException {
        return null;
    }

    @Override
    public int GetResultsSize() {
        return this.ResultsSize;
    }

    @Override
    public String[] GetResultsName() {
        return ResultsName;
    }
}
