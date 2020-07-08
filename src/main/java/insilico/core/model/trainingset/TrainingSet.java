package insilico.core.model.trainingset;

import insilico.core.alerts.AlertEncoding;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;

public class TrainingSet implements Serializable, iTrainingSet {

    private static final long serialVersionUID = 1L;
    
    Logger logger = LoggerFactory.getLogger(TrainingSet.class);

    public static final short MOLECULE_UNKNOWN_SET = -1;
    public static final short MOLECULE_TRAINING =1;
    public static final short MOLECULE_TEST =2;

    protected int MoleculesSize;
    protected int MoleculesTrainSize;
    protected int MoleculesTestSize;
    protected int[] Id;
    protected String[] CAS;
    protected short[] Status;
    protected String[] SMILES;
    protected float[] Experimental;
    protected float[] Prediction;

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
        Experimental = null;
        Prediction = null;
        DescriptorSize = 0;
        DescriptorName = null;
        DescriptorMin = null;
        DescriptorMax = null;
        Descriptors = null;
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

    @Override
    public int getId(int Index) throws GenericFailureException {
        if((Id == null) || (Index < 0) || (Index >= Id.length)){
            String message = "Id object empty or wrong index in request to training set";
            logger.error(message);
            throw new GenericFailureException(message);
        }
        return Id[Index];
    }

    @Override
    public String getCAS(int Index) throws GenericFailureException {
        if ((CAS == null) || (Index < 0) || (Index >= CAS.length)) {
            String message = "CAS object empty or wrong index in request to training set";
            logger.error(message);
            throw new GenericFailureException(message);
        }
        return CAS[Index];
    }

    @Override
    public String getSMILES(int Index) throws GenericFailureException {
        if ((SMILES == null) || (Index < 0) || (Index >= SMILES.length)) {
            String message = "SMILES object empty or wrong index in request to training set";
            logger.error(message);
            throw new GenericFailureException(message);
        }
        return SMILES[Index];
    }

    @Override
    public short getMoleculeSet(int Index) throws GenericFailureException {
        if ((Status == null) || (Index < 0) || (Index >= Status.length)) {
            String message = "Status object empty or wrong index in request to training set";
            logger.error(message);
            throw new GenericFailureException(message);
        }
        return Status[Index];
    }

    @Override
    public double getExperimentalValue(int Index) throws GenericFailureException {
        if ((Experimental == null) || (Index < 0) || (Index >= Experimental.length)) {
            logger.error("Experimental object empty or wrong index in request to training set");
            throw new GenericFailureException("Experimental object empty or wrong index in request to training set");
        }
        return Experimental[Index];
    }

    @Override
    public String getExperimentalValueFormatted(int Index) throws GenericFailureException {
        if ((Experimental == null) || (Index < 0) || (Index >= Experimental.length)) {
            logger.error("Experimental object empty or wrong index in request to training set");
            throw new GenericFailureException("Experimental object empty or wrong index in request to training set");
        }

        double val = getExperimentalValue(Index);
        return (FormatValue(val));
    }

    @Override
    public double getPredictedValue(int Index) throws GenericFailureException {
        if ((Prediction == null) || (Index < 0) || (Index >= Prediction.length)) {
            logger.error("Prediction object empty or wrong index in request to training set");
            throw new GenericFailureException("Prediction object empty or wrong index in request to training set");
        }
        return Prediction[Index];
    }

    @Override
    public String getPredictedValueFormatted(int Index) throws GenericFailureException {
        if ((Prediction == null) || (Index < 0) || (Index >= Prediction.length)) {
            logger.error("Prediction object empty or wrong index in request to training set");
            throw new GenericFailureException("Prediction object empty or wrong index in request to training set");
        }

        double val = getPredictedValue(Index);
        return (FormatValue(val));
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
            logger.error("DescriptorMax object empty or wrong index in request to training set");
            throw new GenericFailureException("DescriptorMax object empty or wrong index in request to training set");
        }
        return DescriptorMax[Index];
    }

    @Override
    public double getDescriptorMin(int Index) throws GenericFailureException {
        if ((DescriptorMin == null) || (Index < 0) || (Index >= DescriptorMin.length)) {
            logger.error("DescriptorMin object empty or wrong index in request to training set");
            throw new GenericFailureException("DescriptorMin object empty or wrong index in request to training set");
        }
        return DescriptorMin[Index];
    }

    @Override
    public double getDescriptor(int MolIndex, int DescriptorIndex) throws GenericFailureException {
        if ((Descriptors == null) || (MolIndex < 0) || (MolIndex >= Descriptors.length) ||
                (DescriptorIndex < 0) || (DescriptorIndex >= Descriptors[0].length)) {
            logger.error("Descriptors object empty or wrong index in request to training set");
            throw new GenericFailureException("Descriptors object empty or wrong index in request to training set");
        }
        return Descriptors[MolIndex][DescriptorIndex];
    }

    @Override
    public SimilarityDescriptors getSimilarityDescriptor(int Index) throws GenericFailureException {
        if ((SimDescriptors == null) || (Index < 0) || (Index >= SimDescriptors.length)) {
            logger.error("SimDescriptors object empty or wrong index in request to training set");
            throw new GenericFailureException("SimDescriptors object empty or wrong index in request to training set");
        }
        return SimDescriptors[Index];
    }

    @Override
    public String getAlerts(int Index) throws GenericFailureException {
        if ((Alerts == null) || (Index < 0) || (Index >= Alerts.length)) {
            logger.error("Alerts object empty or wrong index in request to training set");
            throw new GenericFailureException("Alerts object empty or wrong index in request to training set");
        }
        return Alerts[Index];
    }

    @Override
    public ACFItemList getACF() throws GenericFailureException {
        if (ACFList == null) {
            logger.error("ACFList object empty or wrong index in request to training set");
            throw new GenericFailureException("ACFList object empty or wrong index in request to training set");
        }
        return ACFList;
    }

    @Override
    public String getClassLabel(double Value) throws GenericFailureException {
        if (!hasClassValues) {
            logger.error("Requested class label for a dataset without classification info in request to training set");
            throw new GenericFailureException("Requested class label for a dataset without classification info in request to training set");
        }
        if (!ClassValues.containsKey(Value)) {
            logger.error("Class label not found (value = " + Value + ") in request to training set");
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
                    logger.error("Unable to format value " + value + " in request to training ");
                    return "-";
                }
            else
                return Format.format(value);
        }
    }

    public void Build(String molFilePath, iInsilicoModel Model){
        try{
            if(Model.getInfo().hasClassValues()){
                this.ClassValues = (HashMap<Double, String>) Model.getInfo().getClassValues().clone();
                this.hasClassValues = true;
            }

            // Read molecule format - 5 columns: Id, CAS, SMILES, Status (Training/Test), Experimental Value
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
            Descriptors = new float[MoleculesSize][DescriptorSize];
            DescriptorMin = new float[MoleculesSize];
            DescriptorMax = new float[MoleculesSize];

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
                index++;
            }
            in.close();

            // Smiles conversion, additional info
            SimDescriptors = new SimilarityDescriptors[MoleculesSize];
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

                InsilicoModelOutput Res = Model.Execute(mol);

                for (int d=0; d<DescriptorSize; d++)
                    this.Descriptors[i][d] = (float)Model.GetDescriptor(d);
                if (!descMinMaxSet) {
                    for (int d=0; d<DescriptorSize; d++) {
                        DescriptorMin[d] = this.Descriptors[i][d];
                        DescriptorMax[d] = this.Descriptors[i][d];
                    }
                    descMinMaxSet = true;
                } else {
                    for (int d=0; d<DescriptorSize; d++) {
//                        DescriptorMin[d] = this.Descriptors[i][d]<DescriptorMin[d]?this.Descriptors[i][d]:DescriptorMin[d];
//                        DescriptorMax[d] = this.Descriptors[i][d]>DescriptorMax[d]?this.Descriptors[i][d]:DescriptorMax[d];
                        DescriptorMin[d] = Math.min(this.Descriptors[i][d], DescriptorMin[d]);
                        DescriptorMax[d] = Math.max(this.Descriptors[i][d], DescriptorMax[d]);

                    }
                }

                this.Alerts[i] = AlertEncoding.MergeAlertIds(Model.GetCalculatedAlert());

                // prediction from calculation
                this.Prediction[i] = (float) Res.getMainResultValue();

                // ACF are calculated only for training set molecules
                if (!(Status[i] == MOLECULE_TRAINING))
                    continue;

                ACFItemList CurACF = acfBuilder.CreateList(mol);

                for (ACFItem acf : CurACF.getList()) {
                    boolean ToBeAdded = true;
                    for (int z=0; z<ACFList.size(); z++)
                        if (acf.getACF().compareTo(ACFList.get(z).getACF()) == 0) {
                            ACFList.get(z).setFrequency(ACFList.get(z).getFrequency() + 1);
                            ToBeAdded = false;
                            break;
                        }
                    if (ToBeAdded)
                        ACFList.AddItem(new ACFItem(acf.getACF(), 1));
                }

            }

            in.close();

        } catch (Exception e){
            logger.error(e.getMessage());
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
                for (int d=0; d<this.DescriptorSize; d++)
                    s += "\t" + this.Descriptors[i][d];
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
            TrainingSet TS = (TrainingSet)in.readObject();
            in.close();
            return TS;
        } catch (IOException | ClassNotFoundException e) {
            throw new GenericFailureException("Unable to load training set - " + e.getMessage());
        }
    }
}
