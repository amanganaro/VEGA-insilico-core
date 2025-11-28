package insilico.core.tools.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * General utilities for computation and i/o purposes.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
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

    /**
     * Read and print into console the message from an input stream
     *
     * @param inputStream
     * @return output string from the stream
     * @throws IOException
     */
    public static StringBuilder readProcessOutput(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output;
    }

    /**
     * start a process builder to execute a command in system bash
     * @param envVariables
     * @param commands
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean executeCommandLine(Map<String, String> envVariables, String... commands) throws IOException, InterruptedException{
        ProcessBuilder processBuilder = new ProcessBuilder(commands);

        if(envVariables != null) {
            envVariables.forEach((key, value) ->
                    processBuilder.environment().put(key, String.valueOf(value)));
        }

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        String s = readProcessOutput(process.getInputStream()).toString();
        log.info("Process builder: {}",s);
        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    public static boolean executeCommandLine(Map<String, String> envVariables, List<String> commands) throws IOException, InterruptedException{
        ProcessBuilder processBuilder = new ProcessBuilder(commands);

        if(envVariables != null) {
            envVariables.forEach((key, value) ->
                    processBuilder.environment().put(key, String.valueOf(value)));
        }

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        String s = readProcessOutput(process.getInputStream()).toString();
        log.info("Process builder: {}",s);
        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    public static boolean executeCommandLineAndCheckResult(Map<String, String> envVariables, String expected, String... commands) throws IOException, InterruptedException{
        ProcessBuilder processBuilder = new ProcessBuilder();

        if(envVariables != null) {
            Map<String, String> env = processBuilder.environment();
            envVariables.forEach((key, variables) ->
                    env.compute(key, (k, currentPath) ->
                            variables + (currentPath != null ? currentPath : "")));
        }

        processBuilder.redirectErrorStream(true);
        processBuilder.command(commands);
        Process process = processBuilder.start();
        StringBuilder sb = readProcessOutput(process.getInputStream());
        log.info("Process builder: {}", sb.toString());
        int exitCode = process.waitFor();
        return exitCode == 0 && sb.indexOf(expected) != -1;
    }

}

