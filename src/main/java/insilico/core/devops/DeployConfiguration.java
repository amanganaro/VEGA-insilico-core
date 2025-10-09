package insilico.core.devops;

import insilico.core.model.InsilicoModel;
import insilico.core.model.InsilicoModelPython;
import insilico.core.model.runner.iInsilicoModelRunnerMessenger;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

@Slf4j
public class DeployConfiguration {

    public static void main(String[] args) {

        InsilicoModel model = null;

        try {
            System.out.println(Arrays.toString(args));
            Class<?> clazz = Class.forName(args[0]);
            //se modello python aggiungere argomenti
            Object instance;
            if(InsilicoModelPython.class.isAssignableFrom(clazz)) {
//                if(args[1].equals("insilico.ontox_assay.ismOntoxAssay")){
//                    generateModelsForOntoxAssays(clazz);
//                }
                if(args[2].equals("VOID")) {
                    instance = clazz.getDeclaredConstructor(boolean.class, iInsilicoModelRunnerMessenger.class).newInstance(true, null);
                }else{
                    instance = clazz.getDeclaredConstructor( boolean.class, null, String.class).newInstance(true, null, args[2]);
                }
            }else
                instance = clazz.getDeclaredConstructor().newInstance();
            model = (InsilicoModel) instance;

        } catch (ClassNotFoundException e) {
            log.error("The class {} was not found", args[0]);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }

        if (model != null) {

            //make the training set only if specified in the pom file
            if(args[1].equalsIgnoreCase("true") ) {

                Path p;
                if(System.getProperty("user.dir").toLowerCase().endsWith(args[3].toLowerCase())){
                    p = Paths.get("src", "main", "resources", model.getInfo().getTrainingSetURL());
                }else{
                    p = Paths.get(args[3], "src", "main", "resources", model.getInfo().getTrainingSetURL());
                }
                ModelsDeployment.BuildDataset(model, p.toString());

            } else {
                log.error("The model could not be found");
            }
        }

    }

    private static void generateModelsForOntoxAssays(Class<?> clazz, String makeDataset, String modelFolder) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        String[] modelTags = {
         "ACE_ONTOX",
         "ACHE_ONTOX",
         "AHR_ONTOX",
         "AT1R_ONTOX",
         "BMP_ONTOX",
         "BSEP_ONTOX",
         "COX1_ONTOX",
         "CYP26_ONTOX",
         "FGFR1_ONTOX",
         "FGFR2_ONTOX",
         "FGFR3_ONTOX",
         "FGFR4_ONTOX",
         "GR_ONTOX",
         "HDEAC_ONTOX",
         "PXR_ONTOX",
         "NMDA_ONTOX",
         "OAT1_ONTOX",
         "PPARA_ONTOX",
         "PPARD_ONTOX",
         "PPARG_ONTOX",
         "THRA_ONTOX",
         "THRB_ONTOX",
         "TTR_ONTOX",
         "VGSC_ONTOX",
         "WNT_ONTOX"
        };


        for(String modelTag: modelTags) {

            Object instance = clazz.getDeclaredConstructor(boolean.class, null, String.class).newInstance(true, null, modelTag);
            InsilicoModel model = (InsilicoModel) instance;

            //make the training set only if specified in the pom file
            if (makeDataset.equalsIgnoreCase("true")) {

                Path p;
                if (System.getProperty("user.dir").toLowerCase().endsWith(modelFolder.toLowerCase())) {
                    p = Paths.get("src", "main", "resources", model.getInfo().getTrainingSetURL());
                } else {
                    p = Paths.get(modelFolder, "src", "main", "resources", model.getInfo().getTrainingSetURL());
                }
                ModelsDeployment.BuildDataset(model, p.toString());

            } else {
                log.error("The model could not be found");
            }
        }

    }

}
