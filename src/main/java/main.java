import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.pro.Constitutional;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.InsilicoModelInfoUpdated;

import insilico.core.model.report.pdf.ReportPDFUpdated;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.InsilicoMoleculeNormalization;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static insilico.core.model.InsilicoModelInfoUpdated.Guide_AD_SimilarMols;


@Slf4j
public class main {

    public static void main(String[]  args) throws IOException, InitFailureException, GenericFailureException {

        URL url = new File(System.getProperty("user.dir") + "/test_xml/prova_daphnia_demetra.xml").toURI().toURL();
        InsilicoModelInfoUpdated insilicoModelInfoUpdated = new InsilicoModelInfoUpdated(url);
        ReportPDFUpdated report = new ReportPDFUpdated(true, url);
        report.GenerateReport();
//
//        InsilicoMoleculeNormalization.DRAGON7_COMPLIANT_NORMALIZATION = true;
//
////        String SMI = "CN1C=NC(N)=C2N=CN=C12";
//        String SMI = "COC(=O)C1C2CCC(CC1OC(=O)C1=CC=CC=C1)N2C";
//        InsilicoMolecule mol = SmilesMolecule.Convert(SMI);
//        System.out.println(mol.GetSMILES());
//
//        DescriptorBlock b = new Constitutional();
//        b.Calculate(mol);
//        for (Descriptor d : b.GetAllDescriptors())
//            System.out.println(d.getName() + "\t" + d.getFormattedValue());



    }









}
