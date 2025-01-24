package insilico.core.model;

import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.GenericFailureException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

public interface iInsilicoModelPython {


    String getCondaEnv();

    String getScriptName();

    public Map<String, String> calculatePythonModel(Path scriptPath, String... params) throws IOException, InterruptedException, CsvValidationException, URISyntaxException;

    boolean configureCondaEnv(URL urlSourceEnv, URL urlSourceAppFile) throws InterruptedException, IOException, URISyntaxException;

    void setDescriptorGenerator(Object descriptorGenerator);

    public boolean isUsingCdddDescriptor();
}
