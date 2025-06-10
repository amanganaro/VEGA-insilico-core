package insilico.core.model;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import java.nio.file.Path;
import java.util.Map;

public interface iInsilicoModelPython {


    String getCondaEnv();

    String getScriptName();

    Map<String, String> calculatePythonModel(Path scriptPath, String... params) throws GenericFailureException;

    boolean configureCondaEnv(String httpUrl) throws InitFailureException;

    void setDescriptorGenerator(Object descriptorGenerator);

    boolean isUsingCdddDescriptor();
}
