package insilico.core.model;

import java.io.IOException;
import java.util.Map;

public interface iInsilicoModelPython {

    public String getCondaEnv();

    public Map<String, String> calculatePythonModel() throws IOException, InterruptedException;

    public boolean configureCondaEnv() throws InterruptedException, IOException;

    public void setCheckSetup();

    //TODO
    public boolean prepareInputData();
}
