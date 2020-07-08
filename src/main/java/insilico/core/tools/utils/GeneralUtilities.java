package insilico.core.tools.utils;

/**
 * General utilities for computation and i/o purposes.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class GeneralUtilities {

    /**
     * Trims the given string from space, newline and tab characters.
     *
     * @param str - String to trim
     * @return String trimmed 
     */
    public static String TrimString(String str) {

        int idx = str.length()-1;
        while (idx>0) {
            char c = str.charAt(idx);
            if ((c=='\t') || (c=='\n') || (c==' '))
                idx--;
            else
                break;
        }
        if (idx!=str.length()-1)
            str = str.substring(0, idx+1);

        return str;
    }

}

