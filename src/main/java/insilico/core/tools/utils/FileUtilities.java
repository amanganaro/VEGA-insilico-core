package insilico.core.tools.utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelectorCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

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

    public static Map<String, String> readSelectedRowAndHeaderFromFile(String file, char delimiter, int rowToRead) throws URISyntaxException, IOException, CsvValidationException {

        Path path = Paths.get(ClassLoader.getSystemResource(file).toURI());
        try(Reader reader = Files.newBufferedReader(path)) {
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(delimiter)
                    .withIgnoreQuotations(true)
                    .build();

            try (CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(parser)
                    .build()) {

                String[] headers = csvReader.readNext();

                for (int i = 1; i < rowToRead; i++) {
                    csvReader.readNext();
                }

                String[] rowValues = csvReader.readNext();
                Map<String, String> rowMap = new HashMap<String,String>();
                if (headers != null && rowValues != null) {
                    for (int i = 0; i < headers.length; i++) {
                        rowMap.put(headers[i], i < rowValues.length ? rowValues[i] : null);
                    }
                }

                return rowMap;
            }
        }

    }

    public static boolean copyExternalData(String source, String dest) throws IOException, InterruptedException {
        Path destinationDirectory = Paths.get(dest);
        Files.createDirectories(destinationDirectory);
        Files.walk(Paths.get(source)).forEach(subItem -> {
            Path destination = Paths.get(dest, subItem.toString().substring(source.length()));
            try {
                File f=new File(destination.toString());
                if(!f.exists())
                    Files.copy(subItem, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return true;
    }

}
