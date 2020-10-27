package insilico.core.molecule.conversion;

import insilico.core.descriptor.Descriptor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provide conversion of exp/predicted values between different units.
 *
 */
@Slf4j
public class ValueConversion {

    public final static String NO_CONVERSION = "no conversion";
    public final static String MINUS_LOG_MOL_LITER_TO_MG_LITER = "-log(mol/l) -> mg/l";


    public static double Convert(String ConversionType, double Value, double MW) {

        if (ConversionType.equals(NO_CONVERSION)) {
            return Value;
        }

        if (ConversionType.equals(MINUS_LOG_MOL_LITER_TO_MG_LITER)) {
            return Math.pow(10, (-1 * Value)) * 1000 * MW;
        }

        return Descriptor.MISSING_VALUE;
    }


    public static String GetConvertedUnit(String ConversionType) {

        if (ConversionType.equals(MINUS_LOG_MOL_LITER_TO_MG_LITER)) {
            return "mg/l";
        }

        return "";
    }

}
