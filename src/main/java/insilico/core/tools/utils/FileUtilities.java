package insilico.core.tools.utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelectorCore;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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

    /***
     * Method to read the header and a specified row (index start from 0) and pair them into a Map
     * with key value items, as key the header row and as value the correspondent selected row value
     * @param file
     * @param delimiter
     * @param rowToRead
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws CsvValidationException
     */
    public static Map<String, String> readSelectedRowAndHeaderFromFile(String file, char delimiter, int rowToRead) throws URISyntaxException, IOException, CsvValidationException {

        Path path = Paths.get(file);
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
                Map<String, String> rowMap = new HashMap<>();
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
        File sourceFile = new File(source);
        File destFolder = new File(dest);

        if (sourceFile.isFile()) {
            Files.copy(
                    sourceFile.toPath(),
                    new File(destFolder, sourceFile.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } else if (sourceFile.isDirectory()) {
            /*Files.walk(Paths.get(source)).forEach(subItem -> {
                Path destination = Paths.get(dest, subItem.toString().substring(source.length()));
                try {
                    File f=new File(destination.toString());
                    if(!f.exists())
                        Files.copy(subItem, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });*/

            if (!destFolder.exists()) {
                destFolder.mkdirs();
            }

            Files.walkFileTree(sourceFile.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetDir = destFolder.toPath().resolve(sourceFile.toPath().relativize(dir));
                    if (!Files.exists(targetDir)) {
                        Files.createDirectory(targetDir);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = destFolder.toPath().resolve(sourceFile.toPath().relativize(file));
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        }


        return true;
    }

    public static boolean copyExternalDataV2(String source, String dest) throws IOException, InterruptedException {
        Path destinationDirectory = Paths.get(dest);
        Files.createDirectories(destinationDirectory);

        // Check if source exists as a file system path
        try {
            Path sourcePath = Paths.get(source); // Throws exception if source is not a file system path
            Files.walk(sourcePath).forEach((subItem) -> {
                Path destination = destinationDirectory.resolve(sourcePath.relativize(subItem));
                try {
                    Files.copy(subItem, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            // If source is not a file system path (e.g., inside JAR), copy manually
            try (InputStream is = FileUtilities.class.getResourceAsStream(source)) {
                if (is == null) {
                    throw new IOException("Resource not found: " + source);
                }
                Path destFile = destinationDirectory.resolve(new File(source).getName());
                Files.copy(is, destFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return true;

    }

}
