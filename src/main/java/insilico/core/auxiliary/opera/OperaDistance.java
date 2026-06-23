package insilico.core.auxiliary.opera;

/**
 *
 * @author Alberto
 */
public class OperaDistance {

    public final static short DIST_EUCLIDEAN = 1;
    
    
    public static double CalculateDistance(short DistType, double[] DescA, double[] DescB) {
        
        double Distance = -1;
        
        switch (DistType)  {
            case DIST_EUCLIDEAN:
                double sum = 0;
                for (int i=0; i<DescA.length; i++)
                    sum += Math.pow( (DescA[i] - DescB[i]), 2);
                Distance = Math.pow( sum, 0.5);
                break;
            default:
                Distance = -1;
        }
        
        return Distance;
    }
    
}
