package insilico.core.similarity;

import insilico.core.exception.GenericFailureException;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.ExtendedFingerprinter;
import org.openscience.cdk.fingerprint.IFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.BitSet;

public class ScreeningFingerprint {

    private static final int SIZE = 64;
    private static final IFingerprinter FP = new ExtendedFingerprinter(SIZE, 3);

    public static long Calculate(IAtomContainer structure) throws GenericFailureException {
        try {
            BitSet matchingFP = FP.getFingerprint(structure);
            long[] matchingFPLong = matchingFP.toLongArray();
            if(matchingFPLong.length != 1)
                throw new CDKException("Wrong size of fingerprint array");
            return matchingFPLong[0];
        } catch (CDKException ex){
            throw new GenericFailureException("Unable to calculate FP - " + ex.getMessage());
        }
    }

    public static double Tanimoto(long A, long B) {
        int matches = 0;
        int diff = 0;
        for (int i =0 ; i < SIZE; i++) {
            long a = A & (1L<<i);
            long b = B & (1L<<i);
            if ( (a != 0) && (b != 0) )
                matches++;
            else if (!((a == 0) && (b == 0)))
                diff++;
        }
        return (matches==0)?0:((double)matches/(double)(matches+diff));
    }
}
