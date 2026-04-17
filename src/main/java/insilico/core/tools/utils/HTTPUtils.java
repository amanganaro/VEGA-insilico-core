package insilico.core.tools.utils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
            FileOutputStream fos;
            if(file.exists()) {
                httpUrlConnection.setRequestProperty("Range", "bytes=" + file.length() + "-");
                fos = new FileOutputStream(file, true); //resume download, append to existing file
            }
            else{
                fos = new FileOutputStream(file);
            }
            BufferedInputStream in = new BufferedInputStream(httpUrlConnection.getInputStream());
            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);

            try{
                byte[] data = new byte[1024];
                int x = 0;
                while ((x = in.read(data, 0, 1024)) >= 0){
                    bout.write(data, 0, x);
                }
            }finally{
                if(bout!=null){
                    bout.flush();
                    bout.close();
                }
                if(fos!=null){
                    fos.flush();
                    fos.close();
                }
            }
            existingFileSize = file.length();
            
            System.out.println(existingFileSize);
            
            maxTries--;
        }

        System.out.println(fileLength);
        System.out.println(existingFileSize);
        System.out.println(maxTries);

        if(maxTries < 0){
            throw new IOException("Cannot download file");
        }
    }
}
