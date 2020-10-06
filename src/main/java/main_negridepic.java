import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;

import java.awt.image.BufferedImage;

public class main_negridepic {

    public static void main(String[] args) throws Exception {

        final String[] SMILES = {
                "[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Cr+3].[Cr+3].[Cr+3].CC(O)=C(N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O)C(=O)NC1C=CC=CC=1.CC(O)=C(N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O)C(=O)NC1C=CC=CC=1.CC(O)=C(N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O)C(=O)NC1C=CC=CC=1.NC1=CC(O)=CC=C1N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O.NC1=CC=CC(O)=C1N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O.NC1C=C(O)C(=CC=1)N=NC1C=C2C=C(C(N=NC3=CC(=CC=C3[O-])S(N)(=O)=O)=C([O-])C2=CC=1)S([O-])(=O)=O",
                "COCCCNC1C=CC=C2C=1C(=O)C1C=CC=C(NCCCOC)C=1C2=O.COCCCNC1C=CC=C2C=1C(=O)C1C=CC=C(NCC(CCCC)CC)C=1C2=O.COCCCNC1C=CC=C2C=1C(=O)C1C=CC=C(NCCCOCC(CCCC)CC)C=1C2=O.CCCCC(CNC1C=CC=C2C=1C(=O)C1C=CC=C(NCC(CCCC)CC)C=1C2=O)CC.CCCCC(COCCCNC1C=CC=C2C=1C(=O)C1C=CC=C(NCC(CCCC)CC)C=1C2=O)CC.CCCCC(COCCCNC1C=CC=C2C=1C(=O)C1C=CC=C(NCCCOCC(CCCC)CC)C=1C2=O)CC",
                "[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Cu+2].[Cu+2].[O-][N+](=NC1C=C(C(C=CC2=CC=C(C=C2S([O-])(=O)=O)N=NC2C=C([O-])C(=CC=2)N=NC2=CC(=CC=C2[O-])S([O-])(=O)=O)=CC=1)S([O-])(=O)=O)C1C=C(C(C=CC2=CC=C(C=C2S([O-])(=O)=O)N=NC2C=C([O-])C(=CC=2)N=NC2=CC(=CC=C2[O-])S([O-])(=O)=O)=CC=1)S([O-])(=O)=O",
                "[zdK+].[K+].[K+].[Cu+2].CC1=CC(N=NC2=C(C=C3C=C(C=CC3=C2[O-])NC2C=CC=CC=2)S([O-])(=O)=O)=C([O-])C=C1N=NC1C=C2C(=CC=CC2=C(C=1)S([O-])(=O)=O)S([O-])(=O)=O",
                "[Na+].[Na+].[Na+].[Na+].CC1C=C(NC2=NC(=NC(NC3=CC=CC(N=NC4C=C5C(=CC(=CC5=CC=4)S([O-])(=O)=O)S([O-])(=O)=O)=C3OC)=N2)N(CCO)CCO)C(OC)=C(C=1)N=NC1C=C2C(=CC(=CC2=CC=1)S([O-])(=O)=O)S([O-])(=O)=O",
                "[Na+].[Na+].[Na+].[Na+].[K+].[K+].[K+].[Cu+2].[Cu+2].CN(C1N=C(NC2=CC(=CC(N=NC(=NNC3=CC(=CC=C3C([O-])=O)S([O-])(=O)=O)C3C=CC=CC=3)=C2[O-])S([O-])(=O)=O)N=C(Cl)N=1)C1=CC(=CC=C1S([O-])(=O)=O)NC1=NC(Cl)=NC(NC2=CC(=CC(N=NC(=NNC3=CC(=CC=C3C([O-])=O)S([O-])(=O)=O)C3C=CC=CC=3)=C2[O-])S([O-])(=O)=O)=N1",
                "[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].CC(CNC1=NC(F)=NC(NC2C=C3C=C(C(N=NC4=CC(=CC=C4S([O-])(=O)=O)S([O-])(=O)=O)=C(O)C3=CC=2)S([O-])(=O)=O)=N1)NC1=NC(F)=NC(NC2C=C3C=C(C(N=NC4=CC=C5C(=CC=CC5=C4S([O-])(=O)=O)S([O-])(=O)=O)=C(O)C3=CC=2)S([O-])(=O)=O)=N1.CC(CNC1=NC(F)=NC(NC2C=C3C=C(C(N=NC4=CC=C5C(=CC=CC5=C4S([O-])(=O)=O)S([O-])(=O)=O)=C(O)C3=CC=2)S([O-])(=O)=O)=N1)NC1=NC(F)=NC(NC2C=C3C=C(C(N=NC4=CC=C5C(=CC=CC5=C4S([O-])(=O)=O)S([O-])(=O)=O)=C(O)C3=CC=2)S([O-])(=O)=O)=N1.CC(CNC1=NC(F)=NC(NC2C=C3C=C(C(N=NC4=CC(=CC=C4S([O-])(=O)=O)S([O-])(=O)=O)=C(O)C3=CC=2)S([O-])(=O)=O)=N1)NC1=NC(F)=NC(NC2C=C3C=C(C(N=NC4=CC(=CC=C4S([O-])(=O)=O)S([O-])(=O)=O)=C(O)C3=CC=2)S([O-])(=O)=O)=N1.CC(CNC1=NC(F)=NC(NC2C=C3C=C(C(N=NC4=CC=C5C(=CC=CC5=C4S([O-])(=O)=O)S([O-])(=O)=O)=C(O)C3=CC=2)S([O-])(=O)=O)=N1)NC1=NC(F)=NC(NC2C=C3C=C(C(N=NC4=CC(=CC=C4S([O-])(=O)=O)S([O-])(=O)=O)=C(O)C3=CC=2)S([O-])(=O)=O)=N1",
                "[Na+].[K+].[K+].[K+].[K+].[K+].C=CS(=O)(=O)C1C=C(C(=CC=1)N=NC1=CC=C(C=C1NC(N)=O)NC1=NC(F)=CC(F)=N1)S([O-])(=O)=O.C=CS(=O)(=O)C1C=C(C(=CC=1)N=NC1=CC=C(C=C1NC(N)=O)NC1=CC(F)=NC(F)=N1)S([O-])(=O)=O.NC(=O)NC1=CC(=CC=C1N=NC1=CC=C(C=C1S([O-])(=O)=O)S(=O)(=O)CCOS([O-])(=O)=O)NC1=CC(F)=NC(F)=N1.NC(=O)NC1=CC(=CC=C1N=NC1=CC=C(C=C1S([O-])(=O)=O)S(=O)(=O)CCOS([O-])(=O)=O)NC1=NC(F)=CC(F)=N1",
                "[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[O-]S(=O)(=O)C1C(=CC=C2C=CC=CC2=1)N=NC1=C(C=C2C=C(C=C(NC3N=C(Cl)N=C(NCCNC4=NC(NC5C=C(C=C6C=C(C(N=NC7=CC=C8C=CC=CC8=C7S([O-])(=O)=O)=C(O)C6=5)S([O-])(=O)=O)S([O-])(=O)=O)=NC(Cl)=N4)N=3)C2=C1O)S([O-])(=O)=O)S([O-])(=O)=O.[O-]S(=O)(=O)C1C(=CC=C2C=CC=CC2=1)N=NC1C(=CC2=CC(=CC(NC3=NC(Cl)=NC(NCCNC4=NC(Cl)=NC(NC5=CC(=CC6=CC(=C(N=NC7=CC=C8C(=CC=CC8=C7S([O-])(=O)=O)S([O-])(=O)=O)C(O)=C65)S([O-])(=O)=O)S([O-])(=O)=O)=N4)=N3)=C2C=1O)S([O-])(=O)=O)S([O-])(=O)=O.[O-]S(=O)(=O)C1C(=CC=C2C(=CC=CC2=1)S([O-])(=O)=O)N=NC1C(=CC2=CC(=CC(NC3=NC(Cl)=NC(NCCNC4=NC(Cl)=NC(NC5=CC(=CC6=CC(=C(N=NC7=CC=C8C(=CC=CC8=C7S([O-])(=O)=O)S([O-])(=O)=O)C(O)=C65)S([O-])(=O)=O)S([O-])(=O)=O)=N4)=N3)=C2C=1O)S([O-])(=O)=O)S([O-])(=O)=O",
                "[Cu+2].COCC(O)=O.CN1CCNCC1.[O-]C(=O)C1C2C(C(CCl)=C(CCl)C=1CCl)=C1NC=2N=C2N=C(N=C3NC(=NC4=NC(=N1)C1=CC=CC=C41)C1=CC=CC=C31)C1=CC=CC=C21",
                "[Cu+2].COCC(O)=O.CN1CCNCC1.[O-]C(=O)C1C2C(C(CCl)=C(CCl)C=1CCl)=C1NC=2N=C2N=C(N=C3NC(=NC4=NC(=N1)C1=CC=CC=C41)C1=CC=CC=C31)C1=CC=CC=C21",
                "[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].CC1=CC(=CC=C1N=NC1=CC=C(N=NC2=CC=CC=C2S([O-])(=O)=O)C2=CC(=CC=C12)S([O-])(=O)=O)NC1=NC(NC2C=C(C=CC=2)S(=O)(=O)CCOS([O-])(=O)=O)=NC(NC2=CC(=CC=C2S([O-])(=O)=O)S([O-])(=O)=O)=N1.CC1=CC(=CC=C1N=NC1=CC=C(N=NC2=CC=CC=C2S([O-])(=O)=O)C2=CC=C(C=C12)S([O-])(=O)=O)NC1=NC(NC2C=C(C=CC=2)S(=O)(=O)CCOS([O-])(=O)=O)=NC(NC2=CC(=CC=C2S([O-])(=O)=O)S([O-])(=O)=O)=N1",
                "[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[Na+].[K+].[K+].[K+].[K+].[K+].[K+].[K+].[K+].NC1=CC=C2C(C=C(C(N=NC3C=CC(=CC=3)S(=O)(=O)CCOS([O-])(=O)=O)=C2O)S([O-])(=O)=O)=C1N=NC1C=CC(=CC=1)S(=O)(=O)CCOS([O-])(=O)=O.NC1=CC=C2C(C=C(C(N=NC3=CC=C(C=C3S([O-])(=O)=O)S(=O)(=O)CCOS([O-])(=O)=O)=C2O)S([O-])(=O)=O)=C1N=NC1=CC=C(C=C1S([O-])(=O)=O)S(=O)(=O)CCOS([O-])(=O)=O.NC1=CC=C2C(C=C(C(N=NC3=CC=C(C=C3S([O-])(=O)=O)S(=O)(=O)CCOS([O-])(=O)=O)=C2O)S([O-])(=O)=O)=C1N=NC1C=CC(=CC=1)S(=O)(=O)CCOS([O-])(=O)=O.NC1=CC=C2C(C=C(C(N=NC3C=CC(=CC=3)S(=O)(=O)CCOS([O-])(=O)=O)=C2O)S([O-])(=O)=O)=C1N=NC1=CC=C(C=C1S([O-])(=O)=O)S(=O)(=O)CCOS([O-])(=O)=O",
        };

        final int[] ids = {
                6, 23, 34, 44, 50, 102, 147, 151, 191, 38,40, 146, 154
        };

        int index = 0;
        for(String smiles : SMILES){
            SmilesMolecule.EXCLUDE_DISCONNECTED_STRUCTURES = false;
            InsilicoMolecule mol = SmilesMolecule.Convert(smiles);
            DepictionGenerator generator = new DepictionGenerator().withSize(4000,4000).withAtomColors()
                    .withAtomMapNumbers().withZoom(1.5).withFillToFit();


            Depiction depiction = generator.depict(mol.GetStructure());
            BufferedImage image = depiction.toImg();
            depiction.writeTo(Depiction.PNG_FMT, "mol " + ids[index] + ".png");
            index++;
        }


    }
}