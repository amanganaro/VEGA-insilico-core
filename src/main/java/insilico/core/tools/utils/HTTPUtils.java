package insilico.core.tools.utils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class HTTPUtils {

    public static String downloadFile(String resourceUrl, String fileName) throws IOException {
        int maxTries = 5;

        URL url = new URL(resourceUrl);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("HEAD");

        long fileLength = httpConnection.getContentLengthLong();
        long existingFileSize = 0;

        File file = new File(fileName);
        if(file.exists()){
            existingFileSize = file.length();
        }
        System.out.println("Existing file size " + existingFileSize);
        while(existingFileSize < fileLength && maxTries > 0){
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            if(file.exists())
                httpUrlConnection.setRequestProperty("Range", "bytes=" + file.length() + "-");
            BufferedInputStream in = new BufferedInputStream(httpUrlConnection.getInputStream());
            FileOutputStream fos;
            if(file.exists())
                fos = new FileOutputStream(file, true); //resume download, append to existing file
            else
                fos = new FileOutputStream(file);

            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            try{
                byte[] data = new byte[1024];
                int x = 0;
                while ((x = in.read(data, 0, 1024)) >= 0){
                    bout.write(data, 0, x);
                }
            }catch(Exception e){
                throw e;
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
            maxTries--;
        }

        if(maxTries < 0){
            throw new IOException("Cannot download file");
        }

        return System.getProperty("user.dir") + File.separator + fileName;
    }
}
