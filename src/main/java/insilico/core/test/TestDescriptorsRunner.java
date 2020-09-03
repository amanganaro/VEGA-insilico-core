package insilico.core.test;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.blocks.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.ILoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

public class TestDescriptorsRunner {

    public TestDescriptorsRunner(){}

    private static String[] descriptorNames = {
      "Constitutional",
      "AtomCenteredFragments",
      "AutoCorrelation",
      "AutoCorrelationHFilled",
      "AutoCorrelationHFilledWithCorrectIState",
      "BurdenEigenvalue",
      "BurdenEigenvalueHFilled",
      "Cats2D",
      "ConnectivityIndices",
      "DistanceDetour",
      "DistanceEdge",
      "EdgeAdjacency",
      "EdgeAdjacencyAugmentedCorrected",
      "EdgeAdjacencyCorrected",
      "EigenvalueBased",
      "EStates",
      "InformationContent",
      "InformationContentWithH",
      "PVSA",
      "Rings",
      "Topological",
      "TopologicalCharge",
      "TopologicalDistances",
      "TopologicalEState",
      "WalkAndPath"
    };



    public void RunAllBlocks(String datasetName) throws Exception {

        File folder = new File("descriptors_csv/" + datasetName + "/");
        String destString = "C:\\Users\\Alessio Sommovigo\\Desktop\\Kode\\Projects\\insilicoCoreOld\\descriptors_csv\\" + datasetName + "\\new\\";


        if (!folder.exists()){
            folder.mkdirs();
        }


        for(String descriptor : descriptorNames){
            RunDescriptorsBlock(descriptor, folder + "/" + datasetName + " - " + descriptor, datasetName);
            File source = new File(folder + "/" + datasetName + " - " + descriptor + ".csv");
            File dest = new File (destString + datasetName + " - " + descriptor + ".csv");
            FileUtils.copyFile(source, dest);
        }
    }

    public void RunSingleBlock(String datasetName, String descriptorName) throws Exception {
        File folder = new File("descriptors_csv/" + datasetName + "/");
        String destString = "C:\\Users\\Alessio Sommovigo\\Desktop\\Kode\\Projects\\insilicoCoreOld\\descriptors_csv\\" + datasetName + "\\new\\";

        if (!folder.exists()){
            folder.mkdirs();
        }

        RunDescriptorsBlock(descriptorName, folder + "/" + datasetName + " - " + descriptorName, datasetName);
        File source = new File(folder + "/" + datasetName + " - " + descriptorName + ".csv");
        File dest = new File (destString + datasetName + " - " + descriptorName + ".csv");
        FileUtils.copyFile(source, dest);

    }


    private void RunDescriptorsBlock(String descriptorName, String descriptorFileName, String datasetName) throws Exception {
        switch (descriptorName) {
            case "Constitutional":
                TestDescriptors.Run(new Constitutional(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "AtomCenteredFragments":
                TestDescriptors.Run(new AtomCenteredFragments(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "AutoCorrelation":
                TestDescriptors.Run(new AutoCorrelation(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "AutoCorrelationHFilled":
                TestDescriptors.Run(new AutoCorrelationHFilled(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "AutoCorrelationHFilledWithCorrectIState":
                TestDescriptors.Run(new AutoCorrelationHFilledWithCorrectIState(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "BurdenEigenvalue":
                TestDescriptors.Run(new BurdenEigenvalue(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "BurdenEigenvalueHFilled":
                TestDescriptors.Run(new BurdenEigenvalueHFilled(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "Cats2D":
                TestDescriptors.Run(new Cats2D(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "ConnectivityIndices":
                TestDescriptors.Run(new ConnectivityIndices(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "DistanceDetour":
                TestDescriptors.Run(new DistanceDetour(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "DistanceEdge":
                TestDescriptors.Run(new DistanceEdge(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "EdgeAdjacency":
                TestDescriptors.Run(new EdgeAdjacency(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "EdgeAdjacencyAugmentedCorrected":
                TestDescriptors.Run(new EdgeAdjacencyAugmentedCorrected(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "EdgeAdjacencyCorrected":
                TestDescriptors.Run(new EdgeAdjacencyCorrected(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "EigenvalueBased":
                TestDescriptors.Run(new EigenvalueBased(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "EStates":
                TestDescriptors.Run(new EStates(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "InformationContent":
                TestDescriptors.Run(new InformationContent(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "InformationContentWithH":
                TestDescriptors.Run(new InformationContentWithH(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "PVSA":
                TestDescriptors.Run(new PVSA(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "Rings":
                TestDescriptors.Run(new Rings(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "Topological":
                TestDescriptors.Run(new Topological(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "TopologicalCharge":
                TestDescriptors.Run(new TopologicalCharge(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "TopologicalDistances":
                TestDescriptors.Run(new TopologicalDistances(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "TopologicalEState":
                TestDescriptors.Run(new TopologicalEState(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
            case "WalkAndPath":
                TestDescriptors.Run(new WalkAndPath(), datasetName, new PrintStream(new FileOutputStream(descriptorFileName + ".csv")), System.out);
                break;
        }
    }



}
