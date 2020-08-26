package insilico.core.molecule.conversion;

import insilico.core.exception.GenericFailureException;
import insilico.core.molecule.InsilicoMoleculeMessages;
import insilico.core.molecule.tools.InsilicoMoleculeNormalization;
import insilico.core.molecule.tools.MoleculeNormalization;
import insilico.core.molecule.tools.Normalizer;
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class FormatSMILES {

    private static Logger logger = LoggerFactory.getLogger(FormatSMILES.class);

    public final static short SMI_FIELD_SMILES = 1;
    public final static short SMI_FIELD_NAME = 2;
    public final static short SMI_FIELD_CAS = 3;

    /**
     *
     * @param SMILES
     * @return
     * @throws GenericFailureException
     */
    public static IAtomContainer StructureFromSMILES(String SMILES)
            throws GenericFailureException {

        // create structure
        IAtomContainer curStructure = null;
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        try {
            curStructure = sp.parseSmiles(SMILES);
        } catch (InvalidSmilesException e) {
            logger.warn("Error while parsing SMILES: " + SMILES + " - " + e.getMessage());
            throw new GenericFailureException("Error while parsing SMILES: " + SMILES + " - " + e.getMessage());
        }

        // normalize and check structure
//        MoleculeNormalization.Normalize(curStructure);
        try {
//            Normalizer normalizer = new Normalizer();
//            curStructure = normalizer.ConfigureMolecule(curStructure, new InsilicoMoleculeMessages());
            curStructure = InsilicoMoleculeNormalization.Normalize(curStructure);
//            InsilicoMoleculeNormalization.Normalize(curStructure);
        } catch (Exception ex){
            logger.warn(ex.getMessage());
        }

        return curStructure;
    }

}
