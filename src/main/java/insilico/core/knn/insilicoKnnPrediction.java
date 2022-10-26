package insilico.core.knn;

import insilico.core.descriptor.Descriptor;
import insilico.core.similarity.SimilarMolecule;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class insilicoKnnPrediction {

    public final static short KNN_MISSING_EQUAL_CLASSESS = -3;
    public final static short KNN_MISSING_RANGE = -2;
    public final static short KNN_MISSING_NO_MOLECULES = -1;
    public final static short KNN_ERROR = 0;
    public final static short KNN_EXPERIMENTAL = 1;
    public final static short KNN_NORMAL_PREDICTION = 2;

    private double Prediction;
    private ArrayList<SimilarMolecule> Neighbours;
    private short Status;

    // CONSTRUCTOR
    public insilicoKnnPrediction() {
        Prediction = Descriptor.MISSING_VALUE;
        Status = KNN_ERROR;
        Neighbours = new ArrayList<>();
    }

    // GETTERS AND SETTERS
    /**
     * @return the Prediction
     */
    public double getPrediction() {
        return Prediction;
    }

    /**
     * @param Prediction the Prediction to set
     */
    public void setPrediction(double Prediction) {
        this.Prediction = Prediction;
    }

    /**
     * @return the Neighbours
     */
    public ArrayList<SimilarMolecule> getNeighbours() {
        return Neighbours;
    }

    /**
     * @param Neighbours the Neighbours to set
     */
    public void setNeighbours(ArrayList<SimilarMolecule> Neighbours) {
        this.Neighbours = Neighbours;
    }

    /**
     * @return the Status
     */
    public short getStatus() {
        return Status;
    }

    /**
     * @param Status the Status to set
     */
    public void setStatus(short Status) {
        this.Status = Status;
    }
}
