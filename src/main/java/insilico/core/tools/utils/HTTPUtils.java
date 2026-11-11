package insilico.core.tools.utils;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HTTPUtils {

    public static void downloadFile(String resourceUrl, String filePath) throws IOException, ConnectException {
        int maxTries = 5;

        URL url = new URL(resourceUrl);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("HEAD");

        long fileLength = httpConnection.getContentLengthLong();
        long existingFileSize = 0;

        // url not reachable maybe due a problem of firewall
        if(fileLength < 0){
            throw new ConnectException("Resource not reachable, it could be that firewall is blocking connection to the server.");
        }

        File file = new File(filePath);
        if(file.exists()){
            existingFileSize = file.length();
        }
        while(existingFileSize < fileLength && maxTries > 0){
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            boolean append = file.exists() && file.length() > 0;
            if(append) {
                httpUrlConnection.setRequestProperty("Range", "bytes=" + file.length() + "-");
            }

            int responseCode = httpUrlConnection.getResponseCode();
            if (append && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                // server ignored the Range header (e.g. 200 OK with full content) or rejected it:
                // appending would corrupt the file, so restart the download from scratch
                append = false;
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    httpUrlConnection.disconnect();
                    httpUrlConnection = (HttpURLConnection) url.openConnection();
                    responseCode = httpUrlConnection.getResponseCode();
                }
            }
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                throw new IOException("Unexpected HTTP response code " + responseCode + " while downloading " + resourceUrl);
            }

            // resume download (append to existing file) or start a new file
            try (BufferedInputStream in = new BufferedInputStream(httpUrlConnection.getInputStream());
                 BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(file, append), 1024)) {
                byte[] data = new byte[1024];
                int x = 0;
                while ((x = in.read(data, 0, 1024)) >= 0){
                    bout.write(data, 0, x);
                }
            }
            existingFileSize = file.length();
            maxTries--;
        }

        if(existingFileSize < fileLength && maxTries == 0){
            throw new IOException("Cannot download file: still incomplete after all retry attempts");
        }

        if(existingFileSize != fileLength){
            throw new IOException("File is not downloaded correctly");
        }


         String expectedSha256 = org.apache.commons.io.IOUtils
                 .toString(new URL(resourceUrl + ".sha256"), java.nio.charset.StandardCharsets.UTF_8)
                 .trim().split("\\s+")[0];
         String actualSha256 = sha256(file);
         if (!actualSha256.equalsIgnoreCase(expectedSha256)) {
             Files.deleteIfExists(file.toPath());
             throw new IOException("Checksum mismatch for " + filePath + ": expected " + expectedSha256 +
                     ", got " + actualSha256);
         }
    }

    public static String sha256(File file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
        }
        StringBuilder hex = new StringBuilder();
        for (byte b : digest.digest()) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
