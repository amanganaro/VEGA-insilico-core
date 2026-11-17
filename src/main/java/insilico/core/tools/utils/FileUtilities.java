package insilico.core.tools.utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelectorCore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * General utilities for file handling/saving/etc
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
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
            // No escape character: the default one is the backslash, which is a meaningful character in a SMILES
            // (C/C=C\C), and would otherwise be silently removed from every value that contains it.
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(delimiter)
                    .withIgnoreQuotations(true)
                    .withEscapeChar(CSVParser.NULL_CHARACTER)
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

    /***
     * Method to read the header and every data row of a CSV file, pairing each row with the header into a Map
     * with the header values as keys. It is the multi-row counterpart of
     * {@link #readSelectedRowAndHeaderFromFile(String, char, int)}, needed to read the output of a Python script
     * executed in batch mode, where one row corresponds to one molecule.
     * <p>
     * The returned list preserves the file order, and each Map preserves the column order, so that the caller can
     * map every row back to the molecule it was computed for.
     *
     * @param file path of the CSV file to read
     * @param delimiter the column separator
     * @return one Map per data row, in file order; an empty list if the file has no data rows
     * @throws IOException on read errors
     * @throws CsvValidationException on malformed CSV content
     */
    public static List<Map<String, String>> readAllRowsAndHeaderFromFile(String file, char delimiter)
            throws IOException, CsvValidationException {

        Path path = Paths.get(file);
        List<Map<String, String>> rows = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(path)) {
            // No escape character: the default one is the backslash, which is a meaningful character in a SMILES
            // (C/C=C\C), and would otherwise be silently removed from every value that contains it.
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(delimiter)
                    .withIgnoreQuotations(true)
                    .withEscapeChar(CSVParser.NULL_CHARACTER)
                    .build();

            try (CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(parser)
                    .build()) {

                String[] headers = csvReader.readNext();
                if (headers == null) {
                    return rows;
                }

                String[] rowValues;
                while ((rowValues = csvReader.readNext()) != null) {
                    // a LinkedHashMap keeps the column order of the file
                    Map<String, String> rowMap = new LinkedHashMap<>();
                    for (int i = 0; i < headers.length; i++) {
                        rowMap.put(headers[i], i < rowValues.length ? rowValues[i] : null);
                    }
                    rows.add(rowMap);
                }
            }
        }

        return rows;
    }

    public static void deleteFolder(String path) throws IOException {
        FileUtils.deleteDirectory(new File(path));
    }



    public static boolean copyResourcesRecursively(final URL originUrl, final File destination) {
        try {
            final URLConnection urlConnection = originUrl.openConnection();
            if (urlConnection instanceof JarURLConnection) {
                return copyJarResourcesRecursively(destination,
                        (JarURLConnection) urlConnection);
            } else {
                return copyFilesRecursively(new File(originUrl.getPath()),
                        destination);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean copyFile(final File toCopy, final File destFile) {
        try {
            if(destFile.createNewFile())
                return copyStream(new FileInputStream(toCopy), new FileOutputStream(destFile));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private static boolean copyFilesRecursively(final File toCopy, final File destDir) throws IOException {
        assert destDir.isDirectory();

        if (!toCopy.isDirectory()) {
            return copyFile(toCopy, new File(destDir, toCopy.getName()));
        } else {
            Files.createDirectories(destDir.toPath());
            //final File newDestDir = new File(destDir, toCopy.getName());
            if (!destDir.exists()) {
                return false;
            }
            for (final File child : toCopy.listFiles()) {
                if (!copyFilesRecursively(child, destDir)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean copyJarResourcesRecursively(final File destDir, final JarURLConnection jarConnection) throws IOException {

        final JarFile jarFile = jarConnection.getJarFile();

        for (final Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
            final JarEntry entry = e.nextElement();
            if (entry.getName().startsWith(jarConnection.getEntryName())) {
                final String filename = entry.getName().substring(entry.getName().lastIndexOf("/") + 1);//StringUtils.removeStart(entry.getName(), jarConnection.getEntryName());
                final File f = new File(destDir, filename);
                if (!entry.isDirectory()) {
                    Files.createDirectories(f.getParentFile().toPath());
                    final InputStream entryInputStream = jarFile.getInputStream(entry);
                    if(!copyStream(entryInputStream, f)){
                        return false;
                    }
                    entryInputStream.close();
                } else {
                    if (!ensureDirectoryExists(f)) {
                        throw new IOException("Could not create directory: "
                                + f.getAbsolutePath());
                    }
                }
            }
        }
        return true;
    }

    public static boolean copyStream(final InputStream is, final File f) {
        try {
            if(f.createNewFile())
                return copyStream(is, new FileOutputStream(f));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private static boolean copyStream(final InputStream is, final OutputStream os) {
        try {
            final byte[] buf = new byte[1024];

            int len = 0;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            is.close();
            os.close();
            return true;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean ensureDirectoryExists(final File f) throws IOException {
        Files.createDirectories(f.toPath());
        return f.exists() || f.mkdir() ;
    }

    public static void extractFilesFromZip(String zipFile, String pathFolderIntoCopy) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFileFromZip(new File(pathFolderIntoCopy), zipEntry);
            if (zipEntry.isDirectory()) {
                Files.createDirectories(newFile.toPath());
                if (!newFile.isDirectory() && !newFile.exists()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zis.closeEntry();
            zipEntry = zis.getNextEntry();
        }
        zis.close();
    }

    public static File newFileFromZip(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
