package insilico.core.tools.utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.python.CdddDescriptors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * General utilities for file handling/saving/etc
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class FileUtilities {

    private static final Logger log = LoggerFactory.getLogger(FileUtilities.class);

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

    private static boolean copyFilesRecursively(final File toCopy, final File destDir) {
        assert destDir.isDirectory();

        if (!toCopy.isDirectory()) {
            return copyFile(toCopy, new File(destDir, toCopy.getName()));
        } else {
            final File newDestDir = new File(destDir, toCopy.getName());
            if (!newDestDir.exists() && !newDestDir.mkdir()) {
                return false;
            }
            for (final File child : toCopy.listFiles()) {
                if (!copyFilesRecursively(child, newDestDir)) {
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
                final String filename = entry.getName().substring(entry.getName().lastIndexOf(File.separator) + 1);//StringUtils.removeStart(entry.getName(), jarConnection.getEntryName());
                final File f = new File(destDir, filename);
                if (!entry.isDirectory()) {
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

    private static boolean copyStream(final InputStream is, final File f) {
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

    private static boolean ensureDirectoryExists(final File f) {
        return f.exists() || f.mkdir();
    }
}
