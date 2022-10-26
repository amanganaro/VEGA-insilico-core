package insilico.core;

//import insilico.carcinogenicity_isscancgx.ismCarcinogenicityIsscanCgx;
//import insilico.bcf_caesar.ismBCFCaesar;
import insilico.core.descriptor.blocks.FunctionalGroups;
import insilico.core.knn.insilicoKnnPrediction;
import insilico.core.knn.insilicoKnnQualitative;
import insilico.core.knn.insilicoKnnQuantitative;
import insilico.core.model.InsilicoModel;
import insilico.core.model.guide.GuidePDFGenerator;
import insilico.core.model.report.pdf.ReportPDF;
import insilico.core.model.report.pdf.ReportPDFMultiple;
import insilico.core.model.report.pdf.ReportPDFSingle;
import insilico.core.model.runner.InsilicoModelRunner;
import insilico.core.model.runner.InsilicoModelRunnerByMolecule;
import insilico.core.model.runner.InsilicoModelWrapper;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.fragmenter.FragmenterCRS4;
import insilico.core.molecule.tools.Depiction;
//import insilico.tpo_oberon.ismTpoOberon;
//import insilico.fish_knn.ismFishKnn;
//import insilico.mutagenicity_knn.ismMutagenicityKnn;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class main {


    public static void main(String[] args) throws Exception {

//        InsilicoMolecule mol = SmilesMolecule.Convert("CCCCCc1cc(O)c2c(c1)OC(C)(C)C1CCC(C)=CC21                  \n \n \t");
////
//        InsilicoModel model = new ismMutagenicityKnn();
//
//        insilicoKnnQualitative pred = new insilicoKnnQualitative();
//        insilicoKnnPrediction res = pred.Calculate(SmilesMolecule.Convert("O=[N+]([O-])c1cc(cc(c1N(CCC)CCC)[N+](=O)[O-])S(=O)(=O)C"), model.GetTrainingSet());
//        System.out.println();
//        SmartsPattern pattern1 = SmartsPattern.create("[R2][R2]", DefaultChemObjectBuilder.getInstance()).setPrepare(false);


//        SmartsPattern pattern2 = SmartsPattern.create("[OX2H]-c1ccccc1", DefaultChemObjectBuilder.getInstance()).setPrepare(false);
//        SmartsPattern pattern3 = SmartsPattern.create("[R2][R2]", DefaultChemObjectBuilder.getInstance()).setPrepare(false);
//        SmartsPattern pattern3 = SmartsPattern.create("c1(-[OX2]-[C;H])c(-[OX2]-[C;H])cccc1", DefaultChemObjectBuilder.getInstance()).setPrepare(false);
//        SmartsPattern pattern4 = SmartsPattern.create("c1(-[OX2]-[C;H])ccc(-[OX2]-[C;H])cc1", DefaultChemObjectBuilder.getInstance()).setPrepare(false);
//        SmartsPattern pattern5 = SmartsPattern.create("c1(-[OX2]-[C;H])c(-[OX2H])cccc1", DefaultChemObjectBuilder.getInstance()).setPrepare(false);
//        SmartsPattern pattern6 = SmartsPattern.create("c1(-[OX2H])ccc(-[OX2]-[C;H])cc1", DefaultChemObjectBuilder.getInstance()).setPrepare(false);


//        Pattern pattern1 = SmartsPattern.findSubstructure(SmilesMolecule.Convert("[OX2H]-c1ccccc1").GetStructure());
//        Pattern pattern2 = SmartsPattern.findSubstructure(SmilesMolecule.Convert("c1(-[OX2H])ccc(-[OX2H])cc1").GetStructure());
//        Pattern pattern3 = SmartsPattern.findSubstructure(SmilesMolecule.Convert("c1(-[OX2]-[C;H])c(-[OX2]-[C;H])cccc1").GetStructure());
//        Pattern pattern4 = SmartsPattern.findSubstructure(SmilesMolecule.Convert("c1(-[OX2]-[C;H])ccc(-[OX2]-[C;H])cc1").GetStructure());
//        Pattern pattern5 = SmartsPattern.findSubstructure(SmilesMolecule.Convert("c1(-[OX2]-[C;H])c(-[OX2H])cccc1").GetStructure());
//        Pattern pattern6 = SmartsPattern.findSubstructure(SmilesMolecule.Convert("c1(-[OX2H])ccc(-[OX2]-[C;H])cc1").GetStructure());

//        pattern2.matchAll(mol.GetStructure()).uniqueAtoms().toChemObjects();
//        System.out.println(pattern1.matches(mol.GetStructure()));
//        System.out.println(pattern2.matches(mol.GetStructure()));

//        Iterable<IChemObject> asd = pattern2.matchAll(mol.GetStructure()).toChemObjects();
//        Iterable<IChemObject> asd2 = pattern3.matchAll(mol.GetStructure()).toChemObjects();
//
//        List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
//        generators.add(new BasicSceneGenerator());
//        generators.add(new RingGenerator());
//        generators.add(new BasicAtomGenerator());

//        DepictionGenerator generator = new DepictionGenerator().withSize(300,300).withAtomColors()
//                .withAtomMapNumbers().withAromaticDisplay().withHighlight(asd, Color.YELLOW).withHighlight(asd2, Color.CYAN).withOuterGlowHighlight(3);


//        System.out.println(pattern3.matches(mol.GetStructure()));
//        System.out.println(pattern4.matches(mol.GetStructure()));
//        System.out.println(pattern5.matches(mol.GetStructure()));
//        System.out.println(pattern6.matches(mol.GetStructure()));


//        BufferedImage bufferedImage = Depiction.DepictMoleculeWith2Substructures(mol, 280, 280, asd, asd2, Color.YELLOW, Color.CYAN, 4);
//        File outputfile = new File("image.jpg");

//        ImageIO.write(bufferedImage, "jpg", outputfile);



//        mol.GetBasicDescriptors();
//        mol.GetMolecularWeight();
//        System.out.println();
//        FragmenterCRS4.getCCQfragments(SmilesMolecule.Convert("CCCCCC").GetStructure());
//        FragmenterCRS4.getRECAPfragments(SmilesMolecule.Convert("CCCCCC").GetStructure());
//        FragmenterCRS4.getROTATABLEfragments(SmilesMolecule.Convert("CCCCCC").GetStructure());

//        FunctionalGroups block = new FunctionalGroups();
//        block.Calculate();
//        SABenigniBossa benigniBossa = new SABenigniBossa();
//
//
//        ModelBuilder3D mb3d = ModelBuilder3D.getInstance(DefaultChemObjectBuilder.getInstance());
//
//        InsilicoMolecule mol = SmilesMolecule.Convert("CNC(=O)c1nccc2cccn12");
//
//
//        IAtomContainer coordinates  = mb3d.generate3DCoordinates(mol.GetStructure(), false);
//
//        List<Point3d> pointList = new ArrayList<>();
//        for(int i = 0; i < coordinates.getAtomCount(); i++){
//            IAtom atom = coordinates.getAtom(i);
//            pointList.add(atom.getPoint3d());
//        }

//        return;
//
//        InsilicoModel model = new ismBCFCaesar();
//////
//        InsilicoModelWrapper wrappersList = new InsilicoModelWrapper(model ,true);
////
//        ArrayList<InsilicoMolecule> moleculeArrayList = new ArrayList<>();
//        moleculeArrayList.add(SmilesMolecule.Convert("O=C1OCCOCCOC(=O)CCCCC(=O)OC(=O)CCCC1"));
////
////
//        InsilicoModelRunnerByMolecule runner = new InsilicoModelRunnerByMolecule();
////
//        runner.AddModel((InsilicoModel) wrappersList.getModel());
//        runner.Run(moleculeArrayList);
////////////
////        byte[] bytePdf = report.CreateReportByModel(moleculeArrayList, runner.GetModelWrappers());
////        Files.write(Path.of("report.pdf"), bytePdf);
//        ReportPDFSingle reportPDFSingle = new ReportPDFSingle(true);
//        byte[] bytePdf = reportPDFSingle.CreateReport(moleculeArrayList , runner.GetModelWrappers().get(0));
//        Files.write(Path.of("report.pdf"), bytePdf);
//        guidePDFGenerator.CreateGuide(new ismMutagenicityBB().getInfo(), "guide.pdf");



    }
}
