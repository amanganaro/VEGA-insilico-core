package core.molecule.tools;

import core.exception.GenericFailureException;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MoleculeNormalization {

    public static void Normalize(IAtomContainer structure) throws GenericFailureException{

        int nAt = structure.getAtomCount();

        // Molecule Aromaticity
        Aromaticity AR = new Aromaticity(ElectronDonation.daylight(), Cycles.all());
        try {
            AR.apply(structure);
        } catch (CDKException ex){
            throw new GenericFailureException(ex.getMessage());
        }

        // Atom Typing
        try {
            CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(DefaultChemObjectBuilder.getInstance());
            for (int i = 0; i < nAt; i++){
                IAtomType at = matcher.findMatchingAtomType(structure, structure.getAtom(i));

                structure.getAtom(i).setAtomTypeName(at.getAtomTypeName());
                structure.getAtom(i).setHybridization(at.getHybridization());
                structure.getAtom(i).setValency(at.getValency());

            }
        } catch (CDKException ex) {
            throw new GenericFailureException(ex.getMessage());
        }


    }

}
