package insilico.core.molecule.conversion;


import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

/**
 * Parsing and normalization for CAS numbers
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class CAS {

    private static ILoggingTool logger = LoggingToolFactory.createLoggingTool(CAS.class);

    /** Default string for invalid CAS numbers ({@value #MISSING_CAS}) */
    public final static String MISSING_CAS = "N.A.";

    /**
     * Parses the given CAS number and returns a normalized CAS with
     * its three parts separated by "-".<p>
     * If the input String is not a valid CAS number, it returns the default
     * string specified in {@link #MISSING_CAS}.
     *
     * @param CAS Input String to be parsed
     * @return Normalized CAS number as String
     */
    public static String NormalizeCAS(String CAS){

        // CAS = A - B - C
        String A, B, C;
        int buf;
        String res;

        try {
            int idx = CAS.length()-1;

            // C
            C = "" + CAS.charAt(idx);
            buf = Integer.parseInt(C);
            idx--;

            // B
            if (idx < 2){
                throw new Exception();
            }
            if(!Character.isDigit(CAS.charAt(idx)))
                idx--;
            B = "" + CAS.charAt(idx-1) + CAS.charAt(idx);
            buf = Integer.parseInt(B);
            idx -=2;

            // A
            if (idx<1)
                throw new Exception();
            if (!Character.isDigit(CAS.charAt(idx)))
                idx--;
            A = "";
            for (int i=idx; i>=0; i--) {
                A = "" + CAS.charAt(i) + A;
            }
            buf = Integer.parseInt(A);

            // Builds string
            res = A + "-" + B + "-" + C;

        } catch (Exception e) {
            logger.warn("Unable to parse CAS string " + CAS);
            res = MISSING_CAS;
        }

        return res;
    }


}
