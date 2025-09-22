package insilico.core;

//import insilico.carcinogenicity_isscancgx.ismCarcinogenicityIsscanCgx;
//import insilico.bcf_caesar.ismBCFCaesar;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorsEngine;
import insilico.core.descriptor.blocks.FunctionalGroups;
import insilico.core.knn.insilicoKnnPrediction;
import insilico.core.knn.insilicoKnnQualitative;
import insilico.core.knn.insilicoKnnQuantitative;
import insilico.core.model.InsilicoModel;
import insilico.core.model.InsilicoModelPython;
import insilico.core.model.guide.GuidePDFGenerator;
import insilico.core.model.qmrf.QMRFDocument;
import insilico.core.model.qmrf.QMRFMSDoc;
import insilico.core.model.report.pdf.ReportPDF;
//import insilico.core.model.report.pdf.ReportPDFMultiple;
//import insilico.core.model.report.pdf.ReportPDFSingle;
import insilico.core.model.runner.InsilicoModelRunner;
import insilico.core.model.runner.InsilicoModelRunnerByMolecule;
import insilico.core.model.runner.InsilicoModelWrapper;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.fragmenter.FragmenterCRS4;
//import insilico.core.molecule.tools.Depiction;
//import insilico.tpo_oberon.ismTpoOberon;
//import insilico.fish_knn.ismFishKnn;
//import insilico.mutagenicity_knn.ismMutagenicityKnn;
import insilico.core.python.CdddDescriptors;
import insilico.core.tools.utils.FileUtilities;
import insilico.core.tools.utils.HTTPUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.smarts.SmartsPattern;
//import insilico.daphnia_demetra.ismDaphniaDemetra;
//import insilico.mutagenicity_bb.ismMutagenicityBB;
//import insilico.mutagenicity_bb.ismMutagenicityBB;
//import insilico.daphnia_demetra.ismDaphniaDemetra;
//import insilico.mutagenicity_bb.ismMutagenicityBB;

import javax.imageio.ImageIO;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class main {


    public static void main(String[] args) throws Exception {

//        System.out.println("Insilico Model");
//        try {
//            File tempFile = File.createTempFile("DILI_ONTOX", ".zip");
//            HTTPUtils.downloadFile("https://amcc.it/vega/dili-bayer.zip", tempFile.getAbsolutePath());
//
//        }catch(Exception e){
//            System.out.println(e.getMessage());
//        }
//
//
//        if(1==1){
//            return;
//        }
        CdddDescriptors cd = new CdddDescriptors(
                List.of("O=C(NC4CCN(CCCCC2(C(=O)NCC(F)(F)F)(c3ccccc3(c1ccccc12)))CC4)c6ccccc6(c5ccc(cc5)C(F)(F)F)"),
                false, null);

        if (1==1) return;

        InsilicoMolecule mu = SmilesMolecule.Convert("O=C(NC4CCN(CCCCC2(C(=O)NCC(F)(F)F)(c3ccccc3(c1ccccc12)))CC4)c6ccccc6(c5ccc(cc5)C(F)(F)F)");
        Descriptor de = mu.GetBasicDescriptorByName("MW_da");
        System.out.println(de.getValue());
        if (1==1) return;

        QMRFMSDoc qm = new QMRFMSDoc(main.class.getResource("/QMRF_BCF_CAESAR.docx"));
        qm.SaveXMLtoFile("qmrf_bcf_caesar.xml");
        if (1==1) return;

        ////

        String src = "/qmrf_hepa_nrf2.xml";

        URL u = main.class.getResource(src);
        QMRFDocument doc = new QMRFDocument(u);
//        doc.PrintToScreen(true, true);
        byte[] bos = doc.CreatePDF();

        try (FileOutputStream fos = new FileOutputStream("prova.pdf")) {
            fos.write(bos);
        }

    }
}
