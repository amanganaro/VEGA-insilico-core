package insilico.core.model.trainingset;

import insilico.core.alerts.AlertEncoding;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.model.InsilicoModelInfoUpdated;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.iInsilicoModel;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.acf.ACFBuilder;
import insilico.core.molecule.acf.ACFItem;
import insilico.core.molecule.acf.ACFItemList;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.similarity.SimilarityDescriptors;
import insilico.core.similarity.SimilarityDescriptorsBuilder;
import insilico.core.tools.utils.GeneralUtilities;
import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;

@Slf4j
public class TrainingSet implements Serializable, iTrainingSet {

    private static final long serialVersionUID = 1L;

    public static final short MOLECULE_UNKNOWN_SET = -1;
    public static final short MOLECULE_TRAINING =1;
    public static final short MOLECULE_TEST =2;

    protected String UnitConversion;

    protected int MoleculesSize;
    protected int MoleculesTrainSize;
    protected int MoleculesTestSize;
    protected int[] Id;
    protected String[] CAS;
    protected short[] Status;
    protected String[] SMILES;
    protected float[] Experimental;
    protected float[] Prediction;
    protected float[] MW;
    protected int DescriptorSize;
    protected String[] DescriptorName;
    protected float[] DescriptorMin;
    protected float[] DescriptorMax;
    protected float[][] Descriptors;



    protected String Units;

    protected SimilarityDescriptors[] SimDescriptors;
    protected String[] Alerts;
    protected ACFItemList ACFList;

    protected boolean hasClassValues;
    protected HashMap<Double, String> ClassValues;

    protected final DecimalFormat Format;


    // Constructor
    public TrainingSet () {

        DecimalFormatSymbols InternationalSymbols =
                new DecimalFormatSymbols();
        InternationalSymbols.setDecimalSeparator('.');
        Format = new DecimalFormat("0.###", InternationalSymbols);

        MoleculesSize = 0;
        MoleculesTrainSize = 0;
        MoleculesTestSize = 0;
        Id = null;
        CAS = null;
        Status = null;
        SMILES = null;
        UnitConversion = null;
        Experimental = null;
        Prediction = null;
        MW = null;
        DescriptorSize = 0;
        DescriptorName = null;
        DescriptorMin = null;
        DescriptorMax = null;
        Units = null;
        SimDescriptors = null;
        Alerts = null;
        ACFList = null;
        hasClassValues = false;
        ClassValues = null;
    }

    // Getters and setters, Interface Methods
    @Override
    public int getMoleculesSize() {
        return MoleculesSize;
    }

    @Override
    public int getMoleculesTrainSize() {
        return MoleculesTrainSize;
    }

    @Override
    public int getMoleculesTestSize() {
        return MoleculesTestSize;
    }

    public String getUnitConversion() {
        return UnitConversion;
    }

    public boolean hasUnitConversion() {
        return UnitConversion != null;
    }

    @Override
    public int getId(int Index) throws GenericFailureException {
        if((Id == null) || (Index < 0) || (Index >= Id.length)){
            String message = "Id object empty or wrong index in request to training set";
            log.error(message);
            throw new GenericFailureException(message);
        }
        return Id[Index];
    }

    @Override
    public String getCAS(int Index) throws GenericFailureException {
        if ((CAS == null) || (Index < 0) || (Index >= CAS.length)) {
            String message = "CAS object empty or wrong index in request to training set";
            log.error(message);
            throw new GenericFailureException(message);
        }
        return CAS[Index];
    }

    @Override
    public String getSMILES(int Index) throws GenericFailureException {
        if ((SMILES == null) || (Index < 0) || (Index >= SMILES.length)) {
            String message = "SMILES object empty or wrong index in request to training set";
            log.error(message);
            throw new GenericFailureException(message);
        }
        return SMILES[Index];
    }

    @Override
    public short getMoleculeSet(int Index) throws GenericFailureException {
        if ((Status == null) || (Index < 0) || (Index >= Status.length)) {
            String message = "Status object empty or wrong index in request to training set";
            log.error(message);
            throw new GenericFailureException(message);
        }
        return Status[Index];
    }

    @Override
    public double getExperimentalValue(int Index) throws GenericFailureException {
        if ((Experimental == null) || (Index < 0) || (Index >= Experimental.length)) {
            log.error("Experimental object empty or wrong index in request to training set");
            throw new GenericFailureException("Experimental object empty or wrong index in request to training set");
        }
        return Experimental[Index];
    }

    @Override
    public String getExperimentalValueFormatted(int Index) throws GenericFailureException {
        if ((Experimental == null) || (Index < 0) || (Index >= Experimental.length)) {
            log.error("Experimental object empty or wrong index in request to training set");
            throw new GenericFailureException("Experimental object empty or wrong index in request to training set");
        }

        double val = getExperimentalValue(Index);
        return (FormatValue(val));
    }

    @Override
    public double getPredictedValue(int Index) throws GenericFailureException {
        if ((Prediction == null) || (Index < 0) || (Index >= Prediction.length)) {
            log.error("Prediction object empty or wrong index in request to training set");
            throw new GenericFailureException("Prediction object empty or wrong index in request to training set");
        }
        return Prediction[Index];
    }

    @Override
    public String getPredictedValueFormatted(int Index) throws GenericFailureException {
        if ((Prediction == null) || (Index < 0) || (Index >= Prediction.length)) {
            log.error("Prediction object empty or wrong index in request to training set");
            throw new GenericFailureException("Prediction object empty or wrong index in request to training set");
        }

        double val = getPredictedValue(Index);
        return (FormatValue(val));
    }

    @Override
    public double getMolecularWeight(int Index) throws GenericFailureException {
        if ((MW == null) || (Index < 0) || (Index >= MW.length)) {
            log.error("MW object empty or wrong index in request to training set");
            throw new GenericFailureException("MW object empty or wrong index in request to training set");
        }

        return MW[Index];
    }

    @Override
    public int getDescriptorSize() {
        return DescriptorSize;
    }

    @Override
    public String GetDescriptorName(int Index) throws GenericFailureException {
        if ((Index<0)||(Index>(DescriptorName.length-1)))
            throw new GenericFailureException("array ot ouf bounds");
        return DescriptorName[Index];
    }

    @Override
    public double getDescriptorMax(int Index) throws GenericFailureException {
        if ((DescriptorMax == null) || (Index < 0) || (Index >= DescriptorMax.length)) {
            log.error("DescriptorMax object empty or wrong index in request to training set");
            throw new GenericFailureException("DescriptorMax object empty or wrong index in request to training set");
        }
        return DescriptorMax[Index];
    }

    @Override
    public double getDescriptorMin(int Index) throws GenericFailureException {
        if ((DescriptorMin == null) || (Index < 0) || (Index >= DescriptorMin.length)) {
            log.error("DescriptorMin object empty or wrong index in request to training set");
            throw new GenericFailureException("DescriptorMin object empty or wrong index in request to training set");
        }
        return DescriptorMin[Index];
    }

    @Override
    public double getDescriptor(int MolIndex, int DescriptorIndex) throws GenericFailureException {
        if ((Descriptors == null) || (MolIndex < 0) || (MolIndex >= Descriptors.length) ||
                (DescriptorIndex < 0) || (DescriptorIndex >= Descriptors[0].length)) {
            log.error("Descriptors object empty or wrong index in request to training set");
            throw new GenericFailureException("Descriptors object empty or wrong index in request to training set");
        }
        return Descriptors[MolIndex][DescriptorIndex];
    }

    @Override
    public SimilarityDescriptors getSimilarityDescriptor(int Index) throws GenericFailureException {
        if ((SimDescriptors == null) || (Index < 0) || (Index >= SimDescriptors.length)) {
            log.error("SimDescriptors object empty or wrong index in request to training set");
            throw new GenericFailureException("SimDescriptors object empty or wrong index in request to training set");
        }
        return SimDescriptors[Index];
    }

    @Override
    public String getAlerts(int Index) throws GenericFailureException {
        if ((Alerts == null) || (Index < 0) || (Index >= Alerts.length)) {
            log.error("Alerts object empty or wrong index in request to training set");
            throw new GenericFailureException("Alerts object empty or wrong index in request to training set");
        }
        return Alerts[Index];
    }

    @Override
    public ACFItemList getACF() throws GenericFailureException {
        if (ACFList == null) {
            log.error("ACFList object empty or wrong index in request to training set");
            throw new GenericFailureException("ACFList object empty or wrong index in request to training set");
        }
        return ACFList;
    }

    @Override
    public String getClassLabel(double Value) throws GenericFailureException {
        if (!hasClassValues) {
            log.error("Requested class label for a dataset without classification info in request to training set");
            throw new GenericFailureException("Requested class label for a dataset without classification info in request to training set");
        }
//        int value_to_check = ;
        if (!ClassValues.containsKey(Value)) {
            log.error("Class label not found (value = " + Value + ") in request to training set");
            throw new GenericFailureException("Class label not found (value = " + Value + ") in request to training set");
        }
        return ClassValues.get(Value);
    }

    @Override
    public String getUnits() {
        return Units;
    }

    @Override
    public boolean hasUnits() {
        return (Units != null);
    }

    @Override
    public String FormatValue(double value) {
        if (value == InsilicoConstants.MISSING_VALUE)
            return "-";
        else {
            if (hasClassValues)
                try {
                    return getClassLabel(value);
                } catch (GenericFailureException e) {
                    log.error("Unable to format value " + value + " in request to training ");
                    return "-";
                }
            else
                return Format.format(value);
        }
    }


    /**
     * Overload of the Build() method, to be used for normal models (predicted values are calculated directly
     * inside the method), while for KNN models the full method with the PredictionFromTxt parameter should be used.
     *
     * @param molFilePath
     * @param Model
     */
    public void Build(String molFilePath, iInsilicoModel Model) {
        Build(molFilePath, Model, false, false);
    }


    /**
     * Build the training set, reading the data from the text file provided as parameter.
     *
     * PredictionFromTxt parameter should be set to true for models that can not directly calculate predictions
     * on their own training set (for now, this is true just for KNN models).
     *
     * @param molFilePath full URI of the input text file
     * @param Model insilicoModel to be used
     * @param PredictionFromTxt true if predicted values are found in the text file, otherwise they are calculated
     */
    public void Build(String molFilePath, iInsilicoModel Model, boolean PredictionFromTxt, boolean CalculateDescriptors){

        try{

            if(Model.getInfo().hasClassValues()){
                this.ClassValues = Model.getInfo().getClassValues();
                this.hasClassValues = true;
            }

            if (Model.getInfo().hasConversion())
                this.UnitConversion = Model.getInfo().getConversion();

            // Read molecule format - 5 columns: Id, CAS, SMILES, Status (Training/Test), Experimental Value
            // Additionally, a 6th column can be present with the already calculated predicted value
            DataInputStream in;
            BufferedReader bufferedReader;
            URL tsURL = getClass().getResource(molFilePath);
            in = new DataInputStream(tsURL.openStream());
            bufferedReader = new BufferedReader(new InputStreamReader(in));

            // Check the header
            String[] parsedString = bufferedReader.readLine().split("\t");
            if(parsedString.length < 5)
                throw new GenericFailureException("Wrong number of fields in header");

            // Number of descriptors, initialization of descriptors names
            DescriptorSize = Model.getDescriptorsSize();
            DescriptorName = Model.getDescriptorsNames();

            // Check number of compounds and train/test numbers
            MoleculesSize = 0;
            MoleculesTrainSize = 0;
            MoleculesTestSize = 0;
            String string;
            while((string = bufferedReader.readLine()) != null){
                string = GeneralUtilities.TrimString(string);
                if(string.isEmpty())
                    continue;
                MoleculesSize++;
                if(string.split("\t")[3].compareToIgnoreCase("Training") == 0)
                    MoleculesTrainSize++;
                if(string.split("\t")[3].compareToIgnoreCase("Test") == 0)
                    MoleculesTestSize++;
            }

            // Init objects
            Id = new int[MoleculesSize];
            CAS = new String[MoleculesSize];
            Status = new short[MoleculesSize];
            Experimental = new float[MoleculesSize];
            Prediction = new float[MoleculesSize];
            SMILES = new String[MoleculesSize];
            Alerts = new String[MoleculesSize];
            DescriptorMin = new float[MoleculesSize];
            DescriptorMax = new float[MoleculesSize];
            if(CalculateDescriptors)
                Descriptors = new float[MoleculesSize][DescriptorSize];

            // reset reader
            in.close();
            in = new DataInputStream(tsURL.openStream());
            bufferedReader = new BufferedReader(new InputStreamReader(in));

            // Set values for each line
            int index = 0;
            bufferedReader.readLine();
            while ((string = bufferedReader.readLine()) != null) {
                parsedString = GeneralUtilities.TrimString(string).split("\t");
                Id[index] = Integer.parseInt(parsedString[0]);
                CAS[index] = insilico.core.molecule.conversion.CAS.NormalizeCAS(parsedString[1]);
                SMILES[index] = parsedString[2];
                if (parsedString[3].compareToIgnoreCase("Training") == 0)
                    Status[index] = MOLECULE_TRAINING;
                else if (parsedString[3].compareToIgnoreCase("Test") == 0)
                    Status[index] = MOLECULE_TEST;
                else
                    Status[index] = MOLECULE_UNKNOWN_SET;
                Experimental[index] = Float.parseFloat(parsedString[4]);
                if (PredictionFromTxt)
                    Prediction[index] = Float.parseFloat(parsedString[5]);
                index++;
            }
            in.close();

            // Smiles conversion, additional info
            SimDescriptors = new SimilarityDescriptors[MoleculesSize];
            MW = new float[MoleculesSize];
            ACFList = new ACFItemList();

            SimilarityDescriptorsBuilder similarityDescriptorsBuilder = new SimilarityDescriptorsBuilder();

            ACFBuilder acfBuilder = new ACFBuilder(1);
            acfBuilder.DoNotSplitRings = false;

            boolean descMinMaxSet = false;

            for (int i = 0; i < MoleculesSize; i++){

                InsilicoMolecule mol = SmilesMolecule.Convert(SMILES[i]);
                if(!mol.IsValid()){
                    SimDescriptors[i] = null;
                    continue;
                }

                // Similarity objects
                SimDescriptors[i] = similarityDescriptorsBuilder.Calculate(mol);

                MW[i] = (float) mol.GetMolecularWeight();

                InsilicoModelOutput Res = Model.Execute(mol);

                if (DescriptorSize > 0) {

                    if(CalculateDescriptors){
                        for (int d=0; d<DescriptorSize; d++)
                            this.Descriptors[i][d] = (float)Model.GetDescriptor(d);
                    }

                    float[] Descriptors = new float[DescriptorSize];
                    for (int d=0; d<DescriptorSize; d++)
                        Descriptors[d] = (float)Model.GetDescriptor(d);
                    if (!descMinMaxSet) {
                        for (int d=0; d<DescriptorSize; d++) {
                            DescriptorMin[d] = Descriptors[d];
                            DescriptorMax[d] = Descriptors[d];
                        }
                        descMinMaxSet = true;
                    } else {
                        for (int d=0; d<DescriptorSize; d++) {
                            DescriptorMin[d] = Math.min(Descriptors[d], DescriptorMin[d]);
                            DescriptorMax[d] = Math.max(Descriptors[d], DescriptorMax[d]);

                        }
                    }
                }


                this.Alerts[i] = AlertEncoding.MergeAlertIds(Model.GetCalculatedAlert());

                // prediction from calculation
                if (!PredictionFromTxt)
                    this.Prediction[i] = (float) Res.getMainResultValue();

                // ACF are calculated only for training set molecules
                if (!(Status[i] == MOLECULE_TRAINING))
                    continue;

                ACFItemList CurACF = acfBuilder.CreateList(mol);

                for (ACFItem acf : CurACF.getList()) {
                    boolean ToBeAdded = true;
                    for (int z=0; z<ACFList.size(); z++)
                        if (acf.getACF().compareTo(ACFList.get(z).getACF()) == 0) {
                            int fr = ACFList.get(z).getFrequency() + 1;
                            ACFList.get(z).setFrequency(fr);
                            ToBeAdded = false;
                            break;
                        }
                    if (ToBeAdded)
                        ACFList.AddItem(new ACFItem(acf.getACF(), 1));
                }

            }

            in.close();

        } catch (Exception e){
            log.error(e.getMessage());
            // TODO: 15/06/2020 sviluppare exception
        }



    }


    public void Print(PrintWriter Out, boolean IncludeAllInfo) {

        String Units = "";
        if (hasUnits())
            Units = " [" + getUnits() + "]";

        String headers = "Id\tCAS\tSMILES\tStatus\tExperimental value" + Units + "\tPredicted value" + Units;
        if (IncludeAllInfo) {
            headers += "\tAlerts";
            for (int d=0; d<this.DescriptorSize; d++)
                headers += "\t" + this.DescriptorName[d];
        }
        Out.println(headers);

        for (int i=0; i<this.MoleculesSize; i++) {
            String s = Integer.toString(this.Id[i]);
            s += "\t" + this.CAS[i];
            s += "\t" + this.SMILES[i];
            s += "\t" + (this.Status[i]==MOLECULE_TRAINING?"Training":(this.Status[i]==MOLECULE_TEST?"Test":"unknown"));

            String Exp = "\t" + this.Experimental[i];
            if (hasClassValues) {
                try {
                    Exp = "\t" + getClassLabel(this.Experimental[i]);
                } catch (Throwable ex) { }
            }
            s += Exp;

            String Pred = "\t" + this.Prediction[i];
            if (hasClassValues) {
                try {
                    Pred = "\t" + getClassLabel(this.Prediction[i]);
                } catch (Throwable ex) { }
            }
            s += Pred;

            if (IncludeAllInfo) {
                s += "\t" + this.Alerts[i];
            }
            Out.println(s);
            Out.flush();
        }
    }

    public void PrintToStdOut(boolean includeAllInfo){
        Print(new PrintWriter(System.out), includeAllInfo);
    }




    public void SerializeToFile(String DestFilePath) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DestFilePath));
        out.writeObject(this);
        out.close();
    }

    public static TrainingSet ReadFromSerializedFile(URL SourceFileURL) throws GenericFailureException {
        try {
            ObjectInputStream in = new ObjectInputStream(SourceFileURL.openStream());
            TrainingSet TS = (TrainingSet) in.readObject();
            in.close();
            return TS;
        } catch (IOException | ClassNotFoundException e) {
            throw new GenericFailureException("Unable to load training set - " + e.getMessage());
        }
    }
}
