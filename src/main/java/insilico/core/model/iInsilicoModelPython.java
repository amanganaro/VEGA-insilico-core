package insilico.core.model;

import java.io.IOException;

public interface iInsilicoModelPython {

    public String getCondaEnv();

    public double calculatePythonModel() throws IOException, InterruptedException;

    public boolean configureCondaEnv() throws InterruptedException, IOException;

    //TODO
    public boolean prepareInputData();
}
