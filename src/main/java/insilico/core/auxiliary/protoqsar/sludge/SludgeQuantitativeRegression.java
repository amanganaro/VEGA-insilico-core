package insilico.core.auxiliary.protoqsar.sludge;

/**
 *
 * @author Alberto
 */
public class SludgeQuantitativeRegression {

    public SludgeQuantitativeRegression() {
        
    }
    
    
    public double Predict(double[] Descriptors) {
        
        double res = 2.282507021808260000; // intercept
        res += (Descriptors[0] * 0.057930385582741000); // minHBint2
        res += (Descriptors[1] * -0.000166568447501542); // ATSC7v
        res += (Descriptors[2] * 0.005806461963101950); // VE3_DzZ
        res += (Descriptors[3] * 12.164992227275900000); // AATSC4e
        res += (Descriptors[4] * -0.132946990376867000); // BCUTp-1l
        
        return res;
    }
}
