package insilico.core.model;

import insilico.core.alerts.AlertList;
import insilico.core.alerts.AlertsEngine;
import insilico.core.constant.MessagesError;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.DescriptorsEngine;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.knn.InsilicoKnn;
import insilico.core.model.trainingset.TrainingSet;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.acf.ACFBuilder;
import insilico.core.similarity.SimilarityDescriptorsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

/**
 * Ancestor for all model classes. It implements the common structure of a
 * model (basic initialization, calculation pipeline).
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public abstract class InsilicoModel implements iInsilicoModel {

    Logger logger = LoggerFactory.getLogger(InsilicoModel.class);

    protected final static short MODEL_ERROR = -1;
    protected final static short MODEL_CALCULATED = 1;

    protected final static short DESCRIPTORS_ERROR = -2;
    protected final static short DESCRIPTORS_MISSING = -1;
    protected final static short DESCRIPTORS_CALCULATED = 1;

    protected final static short ALERTS_ERROR = -1;
    protected final static short ALERTS_NOT_NEEDED = 0;
    protected final static short ALERTS_CALCULATED = 1;

    protected final static short AD_ERROR = -1;
    protected final static short AD_CALCULATED = 1;


    protected InsilicoModelInfo Info;
    protected iTrainingSet TS;
    protected InsilicoMolecule CurMolecule;
    protected InsilicoModelOutput CurOutput;

    protected int DescriptorsSize = 0;
    protected String[] DescriptorsNames;
    protected double[] Descriptors;

    protected int ResultsSize = 0;
    protected String[] ResultsName;
    protected String[] ADItemsName;

    protected short DescriptorStatus;
    protected short AlertsStatus;
    protected short ModelStatus;
    protected short ADStatus;

    private boolean SkipADandTSLoading = false;

    protected DecimalFormat Format_2D;
    protected DecimalFormat Format_3D;
    protected DecimalFormat Format_4D;
    protected DecimalFormat Format_6D;



    /**
     * Constructor. Initialize the model by setting the TS (bound to the
     * database connector), creating the Info object (reading data
     * from the model.xml file that must be located in the given data path) and
     * loading ACF and SA (if available) serialized classes (located in the
     * given data path).
     *
     * @param ModelData path (ending with slash) where the data objects are
     * located (model.xml, serialized ACF and SA classes if available)
     * @throws InitFailureException
     */
    public InsilicoModel(String ModelData)
            throws InitFailureException {

        Info = new InsilicoModelInfo(getClass().getResource(ModelData));
        TS = null; // TS is initialized in execute()

        ResultsName = new String[0];
        ADItemsName = new String[0];

        // Init decimal format objects
        DecimalFormatSymbols InternationalSymbols =
                new DecimalFormatSymbols();
        InternationalSymbols.setDecimalSeparator('.');
        Format_2D = new DecimalFormat("0.##", InternationalSymbols);
        Format_3D = new DecimalFormat("0.###", InternationalSymbols);
        Format_4D = new DecimalFormat("0.####", InternationalSymbols);
        Format_6D = new DecimalFormat("0.######", InternationalSymbols);

    }


    @Override
    public ArrayList<DescriptorBlock> GetRequiredDescriptorBlocks() {
        // Default - return an empty list
        ArrayList<DescriptorBlock> blocks = new ArrayList<>();
        return blocks;
    }


    @Override
    public ArrayList<Integer> GetRequiredAlertBlocks() {
        // Default - return an empty list
        ArrayList<Integer> blocks = new ArrayList<>();
        return blocks;
    }


    @Override
    public AlertList GetCalculatedAlert() throws CloneNotSupportedException {
        // Default - return an empty list
        AlertList SA = new AlertList();
        return SA;
    }


    protected abstract short CalculateDescriptors(DescriptorsEngine DescEngine);


    protected abstract short CalculateModel();


    protected abstract short CalculateAD();


    protected abstract void CalculateAssessment();


    @Override
    public InsilicoModelOutput Execute(InsilicoMolecule mol)
            throws GenericFailureException {
        return Execute(mol, null, true);
    }

    /**
     * Execute current model with the given input. This method should not be
     * overridden, it already implements the complete calculation pipeline.
     * Descendant classes just have to implement methods for calculating
     * descriptors, model, AD and assessment.
     *
     * @param mol
     * @param DescEngine
     * @param CalculateAlerts
     * @return InsilicoModelOutput object with the results of calculation
     * @throws insilico.core.exception.GenericFailureException
     */
    @Override
    public InsilicoModelOutput Execute(InsilicoMolecule mol, DescriptorsEngine DescEngine, boolean CalculateAlerts)
            throws GenericFailureException {

        // Init TS if needed
        if (!SkipADandTSLoading)
            if (TS == null) {
                URL uTS = getClass().getResource(Info.getTrainingSetURL());
                TS = TrainingSet.ReadFromSerializedFile(uTS);
            }

        // Sets needed objects
        CurMolecule = mol;
        CurOutput = new InsilicoModelOutput();
        CurOutput.setMoleculeId(CurMolecule.GetId());
        CurOutput.setMoleculeSMILES(CurMolecule.GetSMILES());

        // Step 0 - Checks molecule and builds ACF if needed
        if (!CurMolecule.IsValid()) {
            CurOutput.setStatus(InsilicoModelOutput.OUTPUT_ERROR);
            CurOutput.setErrMessage(MessagesError.MODEL_INVALID_MOLECULE + (CurMolecule.GetErrors().GetSize()>0?(". " + CurMolecule.GetErrors().GetMessages()):""));
            CurOutput.setAssessment(MessagesError.MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE);
            CurOutput.setAssessmentVerbose(MessagesError.MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE);
            return CurOutput;
        }
        if (!CurMolecule.HasACF()) {
            try {
                ACFBuilder ACFBuild = new ACFBuilder(1);
                ACFBuild.DoNotSplitRings = false;
                CurMolecule.SetACF(ACFBuild.CreateList(CurMolecule));
            } catch (InvalidMoleculeException ex) {
                throw new GenericFailureException("Unable to calculate ACF");
            }
        }


        // Step 1 - Descriptors and similarity object calculation
        try {
            // If no DescriptorEngine has been provided, build it here
            // if it has been provided, descriptors have been calculated BEFORE
            // passing the engine to this method
            if (DescEngine == null) {
                DescriptorsEngine LocalDescEngine = new DescriptorsEngine();
                LocalDescEngine.AddDescriptorBlock(this.GetRequiredDescriptorBlocks());
                LocalDescEngine.CalculateDescriptors(CurMolecule);
                DescriptorStatus = CalculateDescriptors(LocalDescEngine);
                if (!CurMolecule.HasSimilarityDescriptors()) {
                    SimilarityDescriptorsBuilder SimBuild = new SimilarityDescriptorsBuilder();
                    // TODO: 15/06/2020 Corretto?
                    CurMolecule.SetSimilarityDescriptors(SimBuild.Calculate(CurMolecule, LocalDescEngine));
                }
            } else {
                if (!CurMolecule.HasSimilarityDescriptors()) {
                    SimilarityDescriptorsBuilder SimBuild = new SimilarityDescriptorsBuilder();
                    CurMolecule.SetSimilarityDescriptors(SimBuild.Calculate(CurMolecule, DescEngine));
                }
                DescriptorStatus = CalculateDescriptors(DescEngine);
            }
        } catch (Throwable e) {
            if (e.getClass()==OutOfMemoryError.class) throw new OutOfMemoryError(e.getMessage());
            logger.error("Descriptors calculation: " + e);
            throw new GenericFailureException("Unexpected error: " + e);
        }
        if (DescriptorStatus == InsilicoModel.DESCRIPTORS_CALCULATED) {
            for (Double Desc : Descriptors)
                if ((Desc == Descriptor.MISSING_VALUE) || (Desc.isNaN()) || (Desc.isInfinite()) )
                    DescriptorStatus = InsilicoModel.DESCRIPTORS_ERROR;
        }
        if (DescriptorStatus != InsilicoModel.DESCRIPTORS_CALCULATED) {
            CurOutput.setStatus(InsilicoModelOutput.OUTPUT_ERROR);
            CurOutput.setErrMessage(MessagesError.MODEL_DESCRIPTORS_MISSING);
            CurOutput.setAssessment(MessagesError.MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE);
            CurOutput.setAssessmentVerbose(MessagesError.MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE);
            return CurOutput;
        }


        // Step 2 - Alerts calculation
        if (Info.hasAlerts()) {

            // If alerts have been not previously calculated, they are reset
            // and calculated here
            if (CalculateAlerts) {
                CurMolecule.PurgeAlerts();
                AlertsEngine CurSAEngine = new AlertsEngine();
                try {
                    CurSAEngine.AddAlertsBlock(this.GetRequiredAlertBlocks());
                    CurMolecule.AddAlert(CurSAEngine.CalculateAlerts(CurMolecule));
                    AlertsStatus = ALERTS_CALCULATED;
                } catch (GenericFailureException|InvalidMoleculeException|InitFailureException e) {
                    AlertsStatus = ALERTS_ERROR;
                }
            } else {
                AlertsStatus = ALERTS_CALCULATED;
            }

        } else {
            AlertsStatus = ALERTS_NOT_NEEDED;
        }

        // Step 3 - Model calculation
        try {
            ModelStatus = CalculateModel();
        } catch (Throwable e) {
            if (e.getClass()==OutOfMemoryError.class) throw new OutOfMemoryError(e.getMessage());
            logger.error("Model calculation: " + e);
            throw new GenericFailureException("Unexpected error: " + e);
        }
        if (ModelStatus != InsilicoModel.MODEL_CALCULATED) {
            CurOutput.setStatus(InsilicoModelOutput.OUTPUT_ERROR);
            CurOutput.setErrMessage(MessagesError.MODEL_NOT_CALCULATED_ERROR);
            CurOutput.setAssessment(MessagesError.MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE);
            CurOutput.setAssessmentVerbose(MessagesError.MODEL_ASSESSMENT_FOR_NOT_CALCULATED_MOLECULE);
            return CurOutput;
        }

        // Step 4 - AD check
        if (!SkipADandTSLoading) {
            try {
                ADStatus = CalculateAD();
            } catch (Throwable e) {
                if (e.getClass()==OutOfMemoryError.class) throw new OutOfMemoryError(e.getMessage());
                logger.error("AD calculation: " + e);
                throw new GenericFailureException("Unexpected error: " + e);
            }
            if (ADStatus != InsilicoModel.AD_CALCULATED) {
                // Tries anyway to set assessment
                // (possibly the compound is just not predicted)
                try {
                    CalculateAssessment();
                } catch (Throwable e) {
                    if (e.getClass()==OutOfMemoryError.class) throw new OutOfMemoryError(e.getMessage());
                    logger.error("Assessment calculation: " + e);
                    throw new GenericFailureException("Unexpected error: " + e);
                }
                CurOutput.setErrMessage(MessagesError.MODEL_AD_NOT_CALCULATED);
                CurOutput.setStatus(InsilicoModelOutput.OUTPUT_OK_AD_MISSING);
                return CurOutput;
            }

            try {
                CalculateAssessment();
            } catch (Throwable e) {
                if (e.getClass()==OutOfMemoryError.class) throw new OutOfMemoryError(e.getMessage());
                logger.error("Assessment calculation: " + e);
                throw new GenericFailureException("Unexpected error: " + e);
            }
        }

        CurOutput.setStatus(InsilicoModelOutput.OUTPUT_OK);

        return CurOutput;
    }


    /**
     * @return the current TS
     */
    @Override
    public iTrainingSet GetTrainingSet() {
        if (TS == null) {
            URL uTS = getClass().getResource(Info.getTrainingSetURL());
            try {
                TS = TrainingSet.ReadFromSerializedFile(uTS);
            } catch (GenericFailureException ex) {
                TS = null;
            }
        }
        return TS;
    }


    @Override
    public void Purge() {
        this.TS = null;
    }

    /**
     * Returns a given descriptor value
     * @param Index index of the descriptor
     * @return descriptor value
     * @throws GenericFailureException
     */
    @Override
    public double GetDescriptor(int Index) throws GenericFailureException {
        if ((Index<0)||(Index>(Descriptors.length-1)))
            throw new GenericFailureException("array ot ouf bounds");
        return Descriptors[Index];
    }


    @Override
    public int GetResultsSize() {
        return this.ResultsSize;
    }

    /**
     * @return string array with the names of the results
     */
    @Override
    public String[] GetResultsName() {
        return ResultsName;
    }


    /**
     * @return the Info object of this model
     */
    @Override
    public InsilicoModelInfo getInfo() {
        return Info;
    }


    /**
     * @return the ADItemsName
     */
    public String[] GetADItemsName() {
        return ADItemsName;
    }

    @Override
    public int getDescriptorsSize() {
        return this.DescriptorsSize;
    }

    public String[] getDescriptorsNames() {
        return this.DescriptorsNames;
    }

    /**
     * @param SkipADandTSLoading the SkipADandTSLoading to set
     */
    @Override
    public void setSkipADandTSLoading(boolean SkipADandTSLoading) {
        this.SkipADandTSLoading = SkipADandTSLoading;
    }

    @Override
    public void ProcessTrainingSet() throws Exception {
        this.setSkipADandTSLoading(true);
        TrainingSet TS = new TrainingSet();
        String TSPath = this.getInfo().getTrainingSetURL();
        String[] buf = TSPath.split("/");
        String DatName = buf[buf.length-1];
        TSPath = TSPath.substring(0, TSPath.length()-3) + "txt";
        TS.Build(TSPath, this);
        TS.SerializeToFile(DatName);
    }
}
