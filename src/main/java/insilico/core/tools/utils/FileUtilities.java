package insilico.core.tools.utils;

import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelectorCore;

import java.io.File;
import java.io.FileOutputStream;

/**
 * General utilities for file handling/saving/etc
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class FileUtilities {

    /**
     * Saves a byte[] object to file.
     *
     * @param FileDest path+filename of the file
     * @param data array of byte to be saved
     * @throws GenericFailureException
     */
    public static void WriteByteArrayToFile(String FileDest, byte[] data)
            throws GenericFailureException {
        try {
            File Script = new File(FileDest);
            Script.createNewFile();
            FileOutputStream outFileStream = new FileOutputStream(Script);
            outFileStream.write(data);
            outFileStream.flush();
            outFileStream.close();
        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("fileutilities_unable_to_save"), e.getMessage()));
        }
    }

    public static String AppendBeforeExtension(String Source, String AppendStr) {
        int idx = Source.length()-1;
        while (idx>0) {
            if (Source.charAt(idx)=='.')
                break;
            idx--;
        }
        String Ret;
        if (idx>0) {
            Ret = Source.substring(0, idx);
            Ret += "_" + AppendStr;
            Ret += Source.substring(idx);
        } else {
            Ret = Source + "_" + AppendStr;
        }
        return Ret;
    }

}
