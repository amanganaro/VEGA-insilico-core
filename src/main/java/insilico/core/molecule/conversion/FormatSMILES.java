package insilico.core.molecule.conversion;

import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.molecule.tools.InsilicoMoleculeNormalization;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class FormatSMILES {


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
            log.warn(String.format(StringSelectorCore.getString("conversion_smiles_parse_error"), SMILES, e.getMessage()));
            throw new GenericFailureException(String.format(StringSelectorCore.getString("conversion_smiles_parse_error"), SMILES, e.getMessage()));
        }

        // normalize and check structure
//        MoleculeNormalization.Normalize(curStructure);
        try {
//            Normalizer normalizer = new Normalizer();
//            curStructure = normalizer.ConfigureMolecule(curStructure, new InsilicoMoleculeMessages());
            curStructure = InsilicoMoleculeNormalization.Normalize(curStructure);
//            InsilicoMoleculeNormalization.Normalize(curStructure);
        } catch (Exception ex){
            log.warn(ex.getMessage());
        }

        return curStructure;
    }

}
