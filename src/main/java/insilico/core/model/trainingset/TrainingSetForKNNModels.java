package insilico.core.model.trainingset;

import insilico.core.alert.AlertEncoding;
import insilico.core.alert.iAlertBlock;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

/**
 * Needed by KNN models, it reads the dataset without calculating the model.
 * Also, reads predictions directly from given file.
 *
 * @author Alberto Manganaro <a.manganaro@kode-solutions.net>
 */
public class TrainingSetForKNNModels extends TrainingSet {

    private Boolean CalculateDescriptors;

    public TrainingSetForKNNModels() {
        super();
        CalculateDescriptors = false;
    }

    public void SetCalculateDescriptors(boolean status) {
        this.CalculateDescriptors = status;
    }

    public void Build(String MolFilePath, iInsilicoModel Model, iAlertBlock SAs) {

        try {

            // Info retrieved from model info object
            if (Model.getInfo().hasClassValues()) {
                this.ClassValues = (HashMap<Double, String>) Model.getInfo().getClassValues().clone();
                this.hasClassValues = true;
            }
            this.Units = Model.getInfo().getUnits();

            // Reads the molecule file
            // Format: it must contain 5 columns:
            // Id, CAS, SMILES, Status (Training/Test), Experimental value, Predicted value

            DataInputStream in;
            BufferedReader br;
            URL TsURL = getClass().getResource(MolFilePath);
            in = new DataInputStream(TsURL.openStream());
            br = new BufferedReader(new InputStreamReader(in));

            // First line: header
            String[] parsedStr = br.readLine().split("\t");
            if (parsedStr.length < 5)
                throw new GenericFailureException("Wrong number of fields in table header");

            // Get number of descriptors and init descriptors names
            DescriptorSize = Model.getDescriptorsSize();
            DescriptorName = Model.getDescriptorsNames();

            // Check number of compounds and train/test numbers
            MoleculesSize = 0;
            MoleculesTrainSize = 0;
            MoleculesTestSize = 0;
            String str;
            while ((str = br.readLine()) != null) {
                str = GeneralUtilities.TrimString(str);
                if (str.isEmpty())
                    continue;
                MoleculesSize++;
                if (str.split("\t")[3].compareToIgnoreCase("Training") == 0)
                    MoleculesTrainSize++;
                else if (str.split("\t")[3].compareToIgnoreCase("Test") == 0)
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
            DescriptorMin = new float[DescriptorSize];
            DescriptorMax = new float[DescriptorSize];

            // Reset reader and starts from beginning
            in.close();
            in = new DataInputStream(TsURL.openStream());
            br = new BufferedReader(new InputStreamReader(in));

            // Reads each line and set values
            int idx = 0;
            br.readLine(); // skip header row
            while ((str = br.readLine()) != null) {
                parsedStr = GeneralUtilities.TrimString(str).split("\t");
                Id[idx] = Integer.valueOf(parsedStr[0]);
                CAS[idx] = insilico.core.molecule.conversion.CAS.NormalizeCAS(parsedStr[1]);
                SMILES[idx] = parsedStr[2];
                if (parsedStr[3].compareToIgnoreCase("Training") == 0)
                    Status[idx] = MOLECULE_TRAINING;
                else if (parsedStr[3].compareToIgnoreCase("Test") == 0)
                    Status[idx] = MOLECULE_TEST;
                else
                    Status[idx] = MOLECULE_UNKNOWN_SET;
                Experimental[idx] = Float.valueOf(parsedStr[4]);
                Prediction[idx] = Float.valueOf(parsedStr[5]);

                idx++;
            }
            in.close();


            // Converts SMILES and calculates needed additional info
            SimDescriptors = new SimilarityDescriptors[MoleculesSize];
            ACFList = new ACFItemList();

            SimilarityDescriptorsBuilder SimDescEngine = new SimilarityDescriptorsBuilder();

            ACFBuilder AcfEngine = new ACFBuilder(1);
            AcfEngine.DoNotSplitRings = false;

            boolean DescMinMaxSet = false;

            for (int i = 0; i < MoleculesSize; i++) {

                InsilicoMolecule mol = SmilesMolecule.Convert(SMILES[i]);
                if (!mol.IsValid()) {
                    SimDescriptors[i] = null;
                    continue;
                }

                // Similarity objects
                SimDescriptors[i] = SimDescEngine.Calculate(mol);

                // Calculate and set descriptors only if the option is set
                // this is needed by Opera KNN models
                if (this.CalculateDescriptors) {

                    // Descriptors and alerts from model calculation
                    InsilicoModelOutput Res = Model.Execute(mol);

                    for (int d = 0; d < DescriptorSize; d++)
                        this.Descriptors[i][d] = (float) Model.GetDescriptor(d);
                    if (!DescMinMaxSet) {
                        for (int d = 0; d < DescriptorSize; d++) {
                            DescriptorMin[d] = this.Descriptors[i][d];
                            DescriptorMax[d] = this.Descriptors[i][d];
                        }
                        DescMinMaxSet = true;
                    } else {
                        for (int d = 0; d < DescriptorSize; d++) {
                            DescriptorMin[d] = Math.min(this.Descriptors[i][d], DescriptorMin[d]);
                            DescriptorMax[d] = Math.max(this.Descriptors[i][d], DescriptorMax[d]);
                        }
                    }
                }

                // SA (if available, directly from AlertBlock)
                if (SAs != null)
                    this.Alerts[i] = AlertEncoding.MergeAlertIds(SAs.Calculate(mol));

                // ACF are calculated only for training set molecules
                if (!(Status[i] == MOLECULE_TRAINING))
                    continue;

                ACFItemList CurACF = AcfEngine.CreateList(mol);

                for (ACFItem acf : CurACF.getList()) {
                    boolean ToBeAdded = true;
                    for (int z = 0; z < ACFList.size(); z++)
                        if (acf.getACF().compareTo(ACFList.get(z).getACF()) == 0) {
                            ACFList.get(z).setFrequency(ACFList.get(z).getFrequency() + 1);
                            ToBeAdded = false;
                            break;
                        }
                    if (ToBeAdded)
                        ACFList.AddItem(new ACFItem(acf.getACF(), 1));
                }
            }


        } catch (Exception ex) {
            System.out.println(ex.getMessage());

        }
    }
}

