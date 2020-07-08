package insilico.core.molecule.tools;

import insilico.core.exception.GenericFailureException;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Depiction {

    public static BufferedImage DepictMolecule(InsilicoMolecule mol, int width, int height)
            throws GenericFailureException {

        BufferedImage image;
        if(!mol.IsValid())
            throw new GenericFailureException("Molecule does not have a valid structure");

        // Generates structure
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        try {
            sdg.setMolecule(mol.GetStructure());
            sdg.generateCoordinates();
        } catch (Exception ex){
            throw new GenericFailureException("Unable to generate coordinates for molecule");
        }
        IAtomContainer currentMol = sdg.getMolecule();

        // Image generators
        List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
        generators.add(new BasicSceneGenerator());
        generators.add(new RingGenerator());
        generators.add(new BasicAtomGenerator());

        // Loading a specific font required from the render
        AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());

        // setup only needs to be called on the first draw
        Rectangle drawArea = new Rectangle(width, height);
        renderer.setup(currentMol, drawArea);

        Rectangle diagramBounds = renderer.calculateDiagramBounds(currentMol);
        renderer.setZoomToFit(drawArea.width, drawArea.height, diagramBounds.width, diagramBounds.height);

        // Build the image object
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setBackground(new Color(255,255,255));
        graphics.clearRect(0,0, width, height);

        renderer.paint(currentMol, new AWTDrawVisitor(graphics));

        graphics.dispose();
        return image;
    }

    public static BufferedImage DepictDoubleMolecule(InsilicoMolecule mol1, InsilicoMolecule mol2, int width, int height)
            throws GenericFailureException {

        BufferedImage image1 = DepictMolecule(mol1, width/2, height);
        BufferedImage image2 = DepictMolecule(mol2, width/2, height);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setBackground(new Color(255,255,255));
        graphics.clearRect(0, 0, width, height);

        graphics.dispose();
        return image;
    }

    public static void SaveImageAsPNG(BufferedImage image, String fileName) throws IOException {
        File file = new File(fileName);
        file.createNewFile();
        ImageIO.write(image, "png", file);
    }

//    public static void SaveTSMoleculesAsPNG(iTrainingSet ts, String root){
//        for (int i = 0; i < ts.getMoleculesSize(); i++){
//            try {
//                InsilicoMolecule molecule = SmilesMolecule.Convert(ts.getSMILES(i));
//                String id = root + "/" + ts.getId(i) + ".png";
//            } catch (Exception e){
//                System.out.println("Error in molecule n° " + (i+1));
//            }
//        }
//    }

}
