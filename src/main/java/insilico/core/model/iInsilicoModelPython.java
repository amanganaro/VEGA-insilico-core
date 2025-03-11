package insilico.core.model;

import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

public interface iInsilicoModelPython {


    String getCondaEnv();

    String getScriptName();

    public Map<String, String> calculatePythonModel(Path scriptPath, String... params) throws GenericFailureException;

    public boolean configureCondaEnv(String httpUrl) throws InitFailureException;

    void setDescriptorGenerator(Object descriptorGenerator);

    public boolean isUsingCdddDescriptor();
}
