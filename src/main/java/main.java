import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import test.descriptors.TestDescriptorsRunner;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class main {



    public static void main(String[]  args) throws Exception {

//        TestDescriptorsRunner.RunSingleBlock("kode_dataset","Constitutional");
//        TestDescriptorsRunner.RunSingleBlock("kode_dataset","FunctionalGroups");
//        TestDescriptorsRunner.RunSingleBlock("kode_dataset","AutoCorrelation");

        List<String[]> list = new ArrayList<>();

//        list.add("[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Cr+3].[Cr+3].[Cr+3].CC(O)=C(N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O)C(=O)NC1C=CC=CC=1.CC(O)=C(N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O)C(=O)NC1C=CC=CC=1.CC(O)=C(N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O)C(=O)NC1C=CC=CC=1.NC1=CC(O)=CC=C1N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O.NC1=CC=CC(O)=C1N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O.NC1C=C(O)C(=CC=1)N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O", 6)

        String smiles = "[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].NC1C=C(N)C(=CC=1)N=NC1C=C2C(C=C(C(N=NC3C=C(C(=CC=3)NC3C=CC(=CC=3)N=NC3=CC=C(O)C=C3O)S([O-])(=O)=O)=C2O)S([O-])(=O)=O)=CC=1.NC1C=C(N)C(=CC=1)N=NC1C=C2C(C=C(C(N=NC3C=CC(=CC=3)NC3=CC=C(C=C3S([O-])(=O)=O)N=NC3=C(C=C4C=CC(=CC4=C3O)N=NC3=CC=C(O)C=C3O)S([O-])(=O)=O)=C2O)S([O-])(=O)=O)=CC=1.NC1C=C(N)C(=CC=1)N=NC1C=C2C(C=C(C(N=NC3C=C(C(=CC=3)NC3C=CC(=CC=3)N=NC3=C(C=C4C=CC(=CC4=C3O)N=NC3=CC=C(O)C=C3O)S([O-])(=O)=O)S([O-])(=O)=O)=C2O)S([O-])(=O)=O)=CC=1.NC1C=C(N)C(=CC=1)N=NC1C=C2C(C=C(C(N=NC3C=CC(=CC=3)NC3=CC=C(C=C3S([O-])(=O)=O)N=NC3=C(C=C4C=CC(=CC4=C3O)N=NC3=CC=C(N)C=C3N)S([O-])(=O)=O)=C2O)S([O-])(=O)=O)=CC=1.NC1C=C(N)C(=CC=1)N=NC1C=CC(=CC=1)NC1=CC=C(C=C1S([O-])(=O)=O)N=NC1=C(C=C2C=CC(=CC2=C1O)N=NC1=CC=C(N)C=C1N)S([O-])(=O)=O.NC1C=C(N)C(=CC=1)N=NC1C=CC(=CC=1)NC1=CC=C(C=C1S([O-])(=O)=O)N=NC1=C(C=C2C=CC(=CC2=C1O)N=NC1=CC=C(O)C=C1O)S([O-])(=O)=O.[O-]S(=O)(=O)C1=CC(=CC=C1NC1C=CC(=CC=1)N=NC1=C(C=C2C=CC(=CC2=C1O)N=NC1=CC=C(O)C=C1O)S([O-])(=O)=O)N=NC1=C(C=C2C=CC(=CC2=C1O)N=NC1=CC=C(O)C=C1O)S([O-])(=O)=O.[O-]S(=O)(=O)C1=CC(=CC=C1NC1C=CC(=CC=1)N=NC1=CC=C(O)C=C1O)N=NC1=C(C=C2C=CC(=CC2=C1O)N=NC1=CC=C(O)C=C1O)S([O-])(=O)=O";
//        String smiles = "CCCC";
        SmilesMolecule.EXCLUDE_DISCONNECTED_STRUCTURES = false;
        InsilicoMolecule mol = SmilesMolecule.Convert(smiles);
        DepictionGenerator generator = new DepictionGenerator().withSize(4000,4000).withAtomColors()
                .withAtomMapNumbers().withZoom(1.5).withFillToFit();


        Depiction depiction = generator.depict(mol.GetStructure());
        BufferedImage image = depiction.toImg();
        depiction.writeTo(Depiction.PNG_FMT, "mol.png");

//
//
//        }
    }









}
