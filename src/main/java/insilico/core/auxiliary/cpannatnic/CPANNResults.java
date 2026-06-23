package insilico.core.auxiliary.cpannatnic;

/**
 *
 * @author Alberto Manganaro
 */
public class CPANNResults {

    private double Prediction;
    private int Neuron;
    private double Distance;
    private boolean isValid;
    
    public CPANNResults() {
        Prediction = 0;
        Neuron = 0;
        Distance = 0;
        isValid = false;
    }
    
    
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
     * @return the Neuron
     */
    public int getNeuron() {
        return Neuron;
    }

    /**
     * @param Neuron the Neuron to set
     */
    public void setNeuron(int Neuron) {
        this.Neuron = Neuron;
    }

    /**
     * @return the Distance
     */
    public double getDistance() {
        return Distance;
    }

    /**
     * @param Distance the Distance to set
     */
    public void setDistance(double Distance) {
        this.Distance = Distance;
    }    

    /**
     * @return the isValid
     */
    public boolean isIsValid() {
        return isValid;
    }

    /**
     * @param isValid the isValid to set
     */
    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }
    
}
