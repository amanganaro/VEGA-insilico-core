package insilico.core.model;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.PythonEnvironemntFailedException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface iInsilicoModelPython {


    String getCondaEnv();

    String getPythonEnv();

    String getScriptName();

    Map<String, String> calculatePythonModel(Path scriptPath, String... params) throws GenericFailureException;

    boolean configureCondaEnv() throws InitFailureException;

    boolean configurePythonEnv() throws PythonEnvironemntFailedException;

    boolean removeCondaEnv() throws IOException, InterruptedException;

    boolean removePythonEnv() throws IOException, InterruptedException;

    void setSupportFiles() throws InitFailureException;

    void setDescriptorGenerator(Object descriptorGenerator);

    boolean isUsingCdddDescriptor();
}
