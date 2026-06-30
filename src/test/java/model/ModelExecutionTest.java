package model;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.InsilicoModel;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.InsilicoModelPython;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.python.CdddDescriptors;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ModelExecutionTest {

    protected static ArrayList<InsilicoMolecule> dataset;
    protected static List<Map<String, String>> groundTruth;

    protected abstract InsilicoModel getModel() throws InitFailureException, GenericFailureException;

    @BeforeAll
    void loadDataset() throws IOException, CsvValidationException {

        dataset = new ArrayList<>();
        groundTruth = new ArrayList<>();

        InputStream is = ModelExecutionTest.class.getClassLoader().getResourceAsStream("groundTruth.csv");

        if (is == null) {
            throw new IllegalStateException("groundTruth.csv not found in src/test/resources/");
        }

        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(is, StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator('\t')
                        .build())
                .build()) {

            String[] headers = reader.readNext();
            int nameIndex = Arrays.asList(headers).indexOf("NAME");
            int smilesIndex = Arrays.asList(headers).indexOf("SMILES");

            String[] lineArray;
            while ((lineArray = reader.readNext()) != null) {
                InsilicoMolecule molecule = SmilesMolecule.Convert(lineArray[smilesIndex]);
                molecule.SetId(lineArray[nameIndex]);
                dataset.add(molecule);

                Map<String, String> line = new HashMap<>();
                for(int i = 0; i < lineArray.length; i++){
                    line.put(headers[i], lineArray[i]);
                }
                groundTruth.add(line);
            }
        }
    }

    @Test
    public void testModelExecution() throws InitFailureException, GenericFailureException, IOException {

        InsilicoModel model = getModel();
        CdddDescriptors cdddDescriptors=null;

        if(InsilicoModelPython.class.isAssignableFrom(model.getClass())){
            cdddDescriptors = new CdddDescriptors(dataset.stream().map(molecule -> {
                return molecule.getInputSMILES();
            }).toList(), false, null);

            ((InsilicoModelPython) model).setDescriptorGenerator(cdddDescriptors);
            boolean descriptorOK = cdddDescriptors.calculateDescriptors();

            if(descriptorOK){
                calculateModel(model);
                cdddDescriptors.dispose();
            }

        }else{
            calculateModel(model);
        }

    }

    private void calculateModel(InsilicoModel model) throws GenericFailureException {

        SoftAssertions softly = new SoftAssertions();

        for (InsilicoMolecule molecule : dataset) {

            Map<String, String> baseline = groundTruth.stream()
                    .filter(row -> row.get("NAME").equals(molecule.GetId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No ground truth entry for molecule: " + molecule.GetId()));

            InsilicoModelOutput out = model.Execute(molecule);
            System.out.println("----" + molecule.getInputSMILES() + "----");

            for (int i = 0; i < model.GetResultsName().length; i++) {
                String resultName = model.getInfo().getKey() + "-" + model.GetResultsName()[i];
                String expected = baseline.get(resultName);
                String actual   = out.getResults()[i];

                System.out.println("PREDICTED: " + model.GetResultsName()[i]
                        +": "+ actual);

                System.out.println("IN DATABASE: " + model.GetResultsName()[i]
                        +": "+ expected);

                softly.assertThat(actual)
                        .as("%s | molecule: %s | metric: %s",
                                model.getInfo().getName(),
                                molecule.GetId(),
                                resultName)
                        .isEqualTo(expected);
            }
        }

        softly.assertAll();
    }
}
