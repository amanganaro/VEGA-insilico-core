import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.InsilicoModelInfoUpdated;

import insilico.core.model.report.pdf.ReportPDFUpdated;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
public class main {

    public static void main(String[]  args) throws IOException, InitFailureException, GenericFailureException {

        URL url = new File(System.getProperty("user.dir") + "/test_xml/prova_daphnia_demetra.xml").toURI().toURL();




        InsilicoModelInfoUpdated insilicoModelInfoUpdated = new InsilicoModelInfoUpdated(url);
//        ReportPDFUpdated report = new ReportPDFUpdated(true, url);
//        FileOutputStream stream = report.GenerateTempReport();
//
        Path tempFile = Files.createTempFile(null,null);

        Path filepath = Files.write(tempFile, url.getFile().getBytes());
        ReportPDFUpdated report = new ReportPDFUpdated(true, filepath.toUri().toURL());




//        System.out.println(tempFile);


//        System.out.println(insilicoModelInfoUpdated.getVersion());
//        System.out.println(insilicoModelInfoUpdated.getVega());
//        System.out.println(insilicoModelInfoUpdated.getGuide());
//        System.out.println(insilicoModelInfoUpdated.getReference());
//        System.out.println(insilicoModelInfoUpdated.getEndpoint());
//        System.out.println(insilicoModelInfoUpdated.getStats());



    }









}
