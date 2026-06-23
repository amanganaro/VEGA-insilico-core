package insilico.core.auxiliary.opera;

import insilico.core.model.trainingset.iTrainingSet;

import java.util.ArrayList;

/**
 *
 * @author Alberto
 */
public class OperaModel {
    
    protected final iTrainingSet TS;
    
    protected final int nDescriptors;
    protected final int K;
    protected final short DistType;
    
    protected boolean SkipExactMatch;
    
    
    public OperaModel(iTrainingSet ModelTS, int nDescriptors, int K, short DistanceType){
        this.TS = ModelTS;
        this.nDescriptors = nDescriptors;
        this.K = K;
        this.DistType = DistanceType;
        this.SkipExactMatch = false;
    }
    
    
    public void SetSkipExactMatch(boolean status) {
        this.SkipExactMatch = status;
    }
    
    
    public double Calculate(double[] Descriptors) throws Exception {
        
        if (Descriptors.length != nDescriptors)
            throw new Exception("Wrong number of descriptors");
        
        // Autoscaling not needed - descriptors MUST be already scaled
        // (both for the target molecule and for the molecules in the TS)       
        
        // Calculate distances
        double[] Distances = new double[TS.getMoleculesSize()];
        for (int molIdx=0; molIdx < TS.getMoleculesSize(); molIdx++) {
            double[] curDesc = new double[nDescriptors];

            for (int j=0; j<nDescriptors; j++)
                curDesc[j] = TS.getDescriptor(molIdx, j);
            Distances[molIdx] = OperaDistance.CalculateDistance(DistType, Descriptors, curDesc);
        }
        
        // Extract the K similar molecules
        ArrayList<Integer> MostSim = new ArrayList<>();
        boolean[] Used = new boolean[TS.getMoleculesSize()];
        for (int i=0; i<TS.getMoleculesSize(); i++)
            Used[i] = false;
        for (int i=0; i<K; i++) {
            double LowestDistance = 1000000;
            int LowestDistIndex = 0;
            for (int j=0; j<TS.getMoleculesSize(); j++) {
                if (SkipExactMatch)
                    if (Distances[j] < 0.001)
                        continue;
                if (!Used[j])
                    if ( Distances[j] < LowestDistance ) {
                        LowestDistIndex = j;
                        LowestDistance = Distances[j];
                    }
            }
            MostSim.add(LowestDistIndex);
            Used[LowestDistIndex] = true;
        }
        
        
        // Calculate weights
        double sumWeights = 0;
        double[] Weights = new double[K];
        for (int Kcount=0; Kcount<K; Kcount++) {
            Weights[Kcount] = 1.0 / (0.05 + Distances[MostSim.get(Kcount)]);
            sumWeights += Weights[Kcount];
        }
        for (int Kcount=0; Kcount<K; Kcount++)
            Weights[Kcount] /= sumWeights;
        
        // Calculate the final predicted value (weighted mean)
        double Prediction = 0;
        for (int Kcount=0; Kcount<K; Kcount++)
            Prediction += Weights[Kcount] * TS.getExperimentalValue(MostSim.get(Kcount));
        
        return Prediction;
        
    }
    
}