package insilico.core.molecule.conversion;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.MoleculeConversionException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.custom.CustomMDLWriter;
import insilico.core.molecule.tools.InsilicoMoleculeNormalization;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Conversion from MDL (MOL/SDF) format to insilico Molecule.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class MDLMolecule {
    

    static public boolean EXCLUDE_DISCONNECTED_STRUCTURES = true;

    final static String ERR_HEADER = StringSelectorCore.getString("conversion_mdl_header_error");

    public static InsilicoMolecule Convert(byte[] MDL) {

        InsilicoMolecule isMol = new InsilicoMolecule();
        log.debug(StringSelectorCore.getString("conversion_mdl_header_start"));

        try {

            ByteArrayInputStream in = new ByteArrayInputStream(MDL);
            MDLV2000Reader reader = new MDLV2000Reader(in);
            IAtomContainer Mol = new AtomContainer();

            // Reads molecule from stream and parses it
            try {
                Mol = reader.read(Mol);
            } catch (CDKException e) {
                throw new MoleculeConversionException(ERR_HEADER + String.format(StringSelectorCore.getString("conversion_mdl_molecule_exception"), e.getMessage()));
            }

            // CAS
            isMol.SetCAS(CAS.MISSING_CAS);

            // Name/Id
            try {
                isMol.SetId((String) Mol.getProperties().values().toArray()[0]);
            } catch (ArrayIndexOutOfBoundsException e) {
                // do nothing
            }

            // Checks for disconnected structures
            if (EXCLUDE_DISCONNECTED_STRUCTURES)
                if (!ConnectivityChecker.isConnected(Mol))
                    throw new MoleculeConversionException(ERR_HEADER + StringSelectorCore.getString("conversion_mdl_molecule_exception_disc_structs"));

            // Configures generated structure
            try {
//                Normalizer normalizer = new Normalizer();
//                Mol = normalizer.ConfigureMolecule(Mol, new InsilicoMoleculeMessages());
                Mol = InsilicoMoleculeNormalization.Normalize(Mol);
            } catch (InitFailureException e) {
                String err = ERR_HEADER + StringSelectorCore.getString("conversion_mdl_molecule_normalizer_init");
                log.error(err + " (" + e.getMessage() + ")");
                throw new MoleculeConversionException(err);
            } catch (Exception e) {
                String err = ERR_HEADER + String.format(StringSelectorCore.getString("conversion_mdl_molecule_normalize"), e.getMessage());
                log.error(err);
                throw new MoleculeConversionException(err);
            }

            // Generates the SMILES for the molecule
            String SMI = SmilesMolecule.GenerateSmiles(Mol);

            // Mark as valid molecule
            isMol.SetSMILESAndStructure(SMI, Mol);
            isMol.MarkAsValid();

        } catch (MoleculeConversionException e) {
            log.error(String.format(StringSelectorCore.getString("conversion_mdl_molecule_conversion_fail"), e.getMessage()));
            for (String s : e.getMessageList())
                isMol.AddError(s);
            isMol.MarkAsInvalid();
        }

        log.debug(StringSelectorCore.getString("conversion_mdl_molecule_conversion"));
        return isMol;
    }

    /**
     * Overload of the {@link #Convert(byte[], java.lang.String, java.lang.String)} method
     * to be used when tags for Id and CAS are available in the MDL molecule.
     *
     * @param MDL MDL code (byte[]) to be parsed
     * @param CASTag Tag to be used to retrieve the CAS
     * @param IdTag Tag to be used to retrieve the Id
     * @return The converted InsilicoMolecule object
     */
    public static InsilicoMolecule Convert(byte[] MDL, String CASTag, String IdTag) {

        // First, the molecule is converted as usual
        InsilicoMolecule isMol = Convert(MDL);

        // If a tag has been provided, searches for the CAS in the MDL code
        if (CASTag != null)
            try {
                String curCAS = GetTag(MDL,CASTag);
                if (curCAS != null)
                    isMol.SetCAS(CAS.NormalizeCAS(curCAS));
                else
                    log.warn(ERR_HEADER + String.format(StringSelectorCore.getString("conversion_unable_find_cas"), CASTag));
            } catch (GenericFailureException e) {
                String Warn = ERR_HEADER + String.format(StringSelectorCore.getString("conversion_unable_find_cas_error"), CASTag);
                log.warn(Warn);
                isMol.AddWarning(Warn);
            }

        // If a tag has been provided, searches for the Name (Id) in the MDL code
        if (IdTag != null)
            try {
                String curId = GetTag(MDL,IdTag);
                if (curId != null)
                    isMol.SetId(curId);
                else
                    log.warn(ERR_HEADER + String.format(StringSelectorCore.getString("conversion_unable_find_id"), IdTag));
            } catch (GenericFailureException e) {
                String Warn = ERR_HEADER + String.format(StringSelectorCore.getString("conversion_unable_find_id_error"), IdTag);
                log.warn(Warn);
                isMol.AddWarning(Warn);
            }

        // If a no Id has been set but a CAS has been set, the Id is set as the CAS
        if (isMol.GetId().isEmpty())
            if ((!isMol.GetCAS().isEmpty())&&(!isMol.GetCAS().equalsIgnoreCase(CAS.MISSING_CAS)))
                isMol.SetId(isMol.GetCAS());

        return isMol;
    }

    /**
     * Retrieves the tag value in a MDL molecule code. Return null if no
     * matching tag has been found.
     *
     * @param MDL MDL code for the molecule
     * @param Tag name of the tag to be searched
     * @return the value of the tag found
     * @throws GenericFailureException
     */
    private static String GetTag(byte[] MDL, String Tag) throws GenericFailureException {

        ByteArrayInputStream in = new ByteArrayInputStream(MDL);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        Tag = "<" + Tag + ">";
        try {
            String line;
            while ((line = br.readLine())!=null)
                if (line.length()>0)
                    if (line.charAt(0)=='>')
                        if (line.contains(Tag)) {
                            line = br.readLine();
                            if (line != null)
                                return(line);
                        }
        } catch (IOException e) {
            throw new GenericFailureException(e.getMessage());
        }

        return null;
    }


    /**
     * Converts a CDK Molecule to MDL molecule (as byte[])
     *
     * @param Mol CDK molecule object to be converted
     * @return The MDL molecule as a byte[]
     * @throws MoleculeConversionException
     */
    public static byte[] GenerateMDL(IAtomContainer Mol)
            throws MoleculeConversionException {

        byte[] OutMol = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            CustomMDLWriter writer = new CustomMDLWriter(baos);
            writer.write(Mol);
            writer.close();
            OutMol = baos.toByteArray();
        } catch (Exception e) {
            log.error(ERR_HEADER + StringSelectorCore.getString("conversion_mdl_unable_to_generate"));
            throw new MoleculeConversionException(ERR_HEADER + StringSelectorCore.getString("conversion_mdl_unable_to_generate"));
        }

        return OutMol;
    }
}
