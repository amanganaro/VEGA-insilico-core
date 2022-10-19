package insilico.core.molecule.tools;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObject;
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

    public static BufferedImage DepictMoleculeWithSubstructure(InsilicoMolecule mol, int width, int height, Iterable<IChemObject> substructure, Color colorSub, double highlightWidth) throws GenericFailureException, CDKException, InvalidMoleculeException {

        if(!mol.IsValid())
            throw new GenericFailureException(StringSelectorCore.getString("tool_depicture_generic_fail"));


        // Image generators
        List<IGenerator<IAtomContainer>> generators = new ArrayList<>();
        generators.add(new BasicSceneGenerator());
        generators.add(new RingGenerator());
        generators.add(new BasicAtomGenerator());

        DepictionGenerator generator = new DepictionGenerator().withSize(width,height).withAtomColors()
                .withAtomMapNumbers().withAromaticDisplay().withHighlight(substructure, colorSub).withOuterGlowHighlight(highlightWidth);


        //
        BufferedImage bufferedImage = generator.depict(mol.GetStructure()).toImg();

        return getBufferedImage(width, height, bufferedImage);
    }

    public static BufferedImage DepictMoleculeWith2Substructures(InsilicoMolecule mol, int width, int height,
                                                                 Iterable<IChemObject> substructure1, Iterable<IChemObject> substructure2, Color colorSub1, Color colorSub2, double highlightWidth) throws GenericFailureException, CDKException, InvalidMoleculeException {

        if(!mol.IsValid())
            throw new GenericFailureException(StringSelectorCore.getString("tool_depicture_generic_fail"));


        // Image generators
        List<IGenerator<IAtomContainer>> generators = new ArrayList<>();
        generators.add(new BasicSceneGenerator());
        generators.add(new RingGenerator());
        generators.add(new BasicAtomGenerator());

        DepictionGenerator generator = new DepictionGenerator().withSize(width,height).withAtomColors()
                .withAtomMapNumbers().withAromaticDisplay().withHighlight(substructure1, colorSub1).withHighlight(substructure2, colorSub2).withOuterGlowHighlight(highlightWidth);


        // Build the image object
        BufferedImage bufferedImage = generator.depict(mol.GetStructure()).toImg();

        return getBufferedImage(width, height, bufferedImage);
    }

    public static BufferedImage DepictMolecule(InsilicoMolecule mol, int width, int height)
            throws GenericFailureException, CDKException, InvalidMoleculeException {

        if(!mol.IsValid())
            throw new GenericFailureException(StringSelectorCore.getString("tool_depicture_generic_fail"));



        // Image generators
        List<IGenerator<IAtomContainer>> generators = new ArrayList<>();
        generators.add(new BasicSceneGenerator());
        generators.add(new RingGenerator());
        generators.add(new BasicAtomGenerator());

        DepictionGenerator generator = new DepictionGenerator().withSize(width,height).withAtomColors()
                .withAtomMapNumbers().withAromaticDisplay();

        BufferedImage bufferedImage = generator.depict(mol.GetStructure()).toImg();


        return getBufferedImage(width, height, bufferedImage);
    }


    /**
     * Overload to use directly a IAtomContainer (no check for validity is performed)
     *
     * @param mol
     * @param width
     * @param height
     * @return
     * @throws GenericFailureException
     * @throws CDKException
     */
    public static BufferedImage DepictMolecule(IAtomContainer mol, int width, int height)
            throws GenericFailureException, CDKException {



        // Image generators
        List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
        generators.add(new BasicSceneGenerator());
        generators.add(new RingGenerator());
        generators.add(new BasicAtomGenerator());

        DepictionGenerator generator = new DepictionGenerator().withSize(width,height).withAtomColors()
                .withAtomMapNumbers().withAromaticDisplay();



        BufferedImage bufferedImage = generator.depict(mol).toImg();


        return getBufferedImage(width, height, bufferedImage);
    }

    private static BufferedImage getBufferedImage(int width, int height, BufferedImage bufferedImage) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();

        graphics2D.drawImage(bufferedImage, 0, 0, width, height, null);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        graphics2D.dispose();
        return resizedImage;
    }


    public static BufferedImage DepictDoubleMolecule(InsilicoMolecule mol1, InsilicoMolecule mol2, int width, int height)
            throws GenericFailureException, CDKException, InvalidMoleculeException {

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

    public static void SaveTSMoleculesAsPNG(iTrainingSet ts, String root){
        for (int i = 0; i < ts.getMoleculesSize(); i++){
            try {
                InsilicoMolecule molecule = SmilesMolecule.Convert(ts.getSMILES(i));
                String id = root + "/" + ts.getId(i) + ".png";
            } catch (Exception e){
                System.out.println(String.format(StringSelectorCore.getString("tool_depicture_molecule_error"), i +1));
            }
        }
    }

}
