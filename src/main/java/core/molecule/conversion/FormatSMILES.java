package core.molecule.conversion;

import core.exception.GenericFailureException;
import core.molecule.tools.MoleculeNormalization;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class FormatSMILES {

    private static ILoggingTool logger = LoggingToolFactory.createLoggingTool(FormatSMILES.class);

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
        SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
        try {
            curStructure = sp.parseSmiles(SMILES);
        } catch (InvalidSmilesException e) {
            logger.warn("Error while parsing SMILES: " + SMILES + " - " + e.getMessage());
            throw new GenericFailureException("Error while parsing SMILES: " + SMILES + " - " + e.getMessage());
        }

        // normalize and check structure
        MoleculeNormalization.Normalize(curStructure);

        return curStructure;
    }

}
