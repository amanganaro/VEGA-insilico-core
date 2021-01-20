package insilico.core.molecule.conversion;


import insilico.core.exception.InitFailureException;
import insilico.core.exception.MoleculeConversionException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.InsilicoMoleculeMessages;
import insilico.core.molecule.conversion.custom.CustomSmilesWriter;
import insilico.core.molecule.tools.InsilicoMoleculeNormalization;
import insilico.core.molecule.tools.Normalizer;
import insilico.core.tools.utils.GeneralUtilities;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conversion from SMILES format to Insilico Molecule
 */
public class SmilesMolecule {

    static Logger logger = LoggerFactory.getLogger(MDLMolecule.class);

    public static boolean EXCLUDE_DISCONNECTED_STRUCTURES = true;

    final static private char charTab = 9;
    final static String ERR_HEADER = "Conversion from SMILES: ";

    /**
     * Converts a given SMILES string to a InsilicoMolecule Object, it creates its CDK MOlecule Structure and its inisilico canonical SMILES String
     * Input string can also contain the molecule's identifier and its CAS
     * number, separated by a tab (chr 9) character.
     * @param SMILESString input String to be converted
     * @param SMILESField index of the field (starting from 0) containing the SMILES
     * @param CASField index of the field containing the CAS
     * @param IdField index of the field containing the Id
     * @return converted InsilicoMolecule Object
     */
    public static InsilicoMolecule Convert(String SMILESString, int SMILESField, int CASField, int IdField){

        InsilicoMolecule isMol = new InsilicoMolecule();
        logger.debug("Starting conversion for SMILES: " + SMILESString);

        try {
            SMILESString = GeneralUtilities.TrimString(SMILESString);
            String[] bufStr = SMILESString.split(String.valueOf(charTab));

            if (SMILESField == -1)
                SMILESField = 0;

            if (bufStr.length == 1){
                SMILESField = 0;
                CASField = -1;
                IdField = -1;
            } else if (bufStr.length == 2){
                // if there are two fields and no cas or id is set
                // assumes that the second field is the id
                if((CASField == -1) && (IdField == -1)){
                    CASField = -1;
                    IdField = -1;
                }
            } else {
                if ( (SMILESField >= bufStr.length) || (CASField >= bufStr.length) || (IdField >= bufStr.length) )
                    throw new MoleculeConversionException(ERR_HEADER + "unable to match fields in string");
            }

            isMol.SetSMILES(GeneralUtilities.TrimString(bufStr[SMILESField]));

            if(CASField != -1)
                isMol.SetCAS(CAS.NormalizeCAS(bufStr[CASField]));

            if (IdField != -1) {
                if (IdField == CASField)
                    isMol.SetId(isMol.GetCAS());
                else
                    isMol.SetId(bufStr[IdField]);
            } else {
                // if only cas but no id is given, cas is taken as id
                if (CASField != -1)
                    isMol.SetId(isMol.GetCAS());
            }

            // Check for disconnected structures
            if (EXCLUDE_DISCONNECTED_STRUCTURES)
                if (IsDisconnected(isMol.GetSMILES()))
                    throw new MoleculeConversionException(ERR_HEADER + "molecule has disconnected structures");

            // Check for particular SMILES leading to problems
            if (isMol.GetSMILES().compareTo("-") == 0)
                throw new MoleculeConversionException("Invalid SMILES");

            // Parses SMILES and creates normalized Molecule object
            IAtomContainer mol = CreateCDKMolecule(isMol.GetSMILES(), isMol.GetWarnings());

            // Generates the SMILES for the molecule
            String SMI = SmilesMolecule.GenerateSmiles(mol);

            // Mark as valid molecule
            isMol.SetSMILESAndStructure(SMI, mol);
            isMol.MarkAsValid();



        } catch (MoleculeConversionException | NumberFormatException ex){
            logger.error("Molecule conversion failed for SMILES " + isMol.GetSMILES() + " (" + ex.getMessage() + ")");
            if (ex.getClass() == MoleculeConversionException.class)
                for (String s : ((MoleculeConversionException)ex).getMessageList())
                    isMol.AddError(s);
            else
                isMol.AddError(ex.getMessage());
            isMol.MarkAsInvalid();
        }

        logger.debug("SMILES molecule converted");
        return isMol;
    }

    /**
     * Constructor to be used when no info are available about other field (ID and CAS) in the SMILES String
     * @param SMILES
     * @return
     */
    public static InsilicoMolecule Convert(String SMILES){
        return Convert(SMILES, -1, -1, -1);
    }

    /**
     * Converts a single (trimmed and already checked for disconnection) SMILES string into a CDK Molecule object
     * @param SMILES String - SMILES to be converted
     * @param warnings
     * @return AtomContainer CDK Object (Molecule object)
     */
    public static IAtomContainer CreateCDKMolecule(String SMILES, InsilicoMoleculeMessages warnings)
            throws MoleculeConversionException {

        IAtomContainer mol;

        // Parses SMILES and creates AtomContainer object
        try {
            SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//            smilesParser.setPreservingAromaticity(true);
            mol = smilesParser.parseSmiles(SMILES);

            if (logger.isDebugEnabled()) {
                logger.debug("SMILES converted with CDK SmilesParser");
                for (int at=0; at<mol.getAtomCount(); at++)
                    logger.debug("Atom no. " + (at+1) + ": " + mol.getAtom(at).getSymbol() +
                            ", aromatic=" + mol.getAtom(at).getFlag(CDKConstants.ISAROMATIC));
            }

        } catch (InvalidSmilesException  e) {
            String err = ERR_HEADER + "unable to parse SMILES string " + SMILES;
            logger.error(err + " (" + e.getMessage() + ")");
            throw new MoleculeConversionException(err);
        }

        // Configures generated structure
        try {
//            Normalizer normalizer = new Normalizer();
//            mol = normalizer.ConfigureMolecule(mol, new InsilicoMoleculeMessages());
            mol = InsilicoMoleculeNormalization.Normalize(mol);
        } catch (InitFailureException ex)
        {
            String err = ERR_HEADER + "unable to init normalizer for SMILES string " + SMILES;
            logger.error(err + " (" + ex.getMessage() + ")");
            throw new MoleculeConversionException(err);
        } catch (Exception e)
        {
            String err = ERR_HEADER + "unable to normalize SMILES string " + SMILES + " (" + e.getMessage() + ")";
            logger.error(err);
            throw new MoleculeConversionException(err);
        }

        return  mol;
    }


    /**
     * Converts a CDK AtomContainer Object (Molecule) to SMILES
     * @param mol CDK Atom Container molecule object
     * @return
     * @throws MoleculeConversionException
     */
    public static String GenerateSmiles(IAtomContainer mol)
            throws MoleculeConversionException{

        try {
            CustomSmilesWriter smilesWriter = new CustomSmilesWriter();
            smilesWriter.setUseAromaticityFlag(true);
            return smilesWriter.createSMILES(mol);
        } catch (CDKException ex){
            String err = ERR_HEADER + "unable to generate SMILES string";
            if (!ex.getMessage().isEmpty())
                err += " (" + ex.getMessage() + ")";
            logger.error(err);
            throw new MoleculeConversionException(err);
        }

    }

    /**
     * Checks if SMILES string contains disconnected structures
     * @param SMILES String
     * @return
     */
    public static Boolean IsDisconnected(String SMILES){
        Boolean res = false;
        if (SMILES.contains("."))
            res = true;
        return res;
    }

    /**
     * Splits disconnected structure in a SMILES string
     * @param SMILES SMILES String
     * @return
     */
    public static String[] SplitDisconnected(String SMILES){
        String disconnectedChar = "\\.";
        String[] res = SMILES.split(disconnectedChar);
        return res;
    }


}

