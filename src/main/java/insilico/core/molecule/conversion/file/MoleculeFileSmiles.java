package insilico.core.molecule.conversion.file;

import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Reader for SMILES file. The reader can process text files where each
 * line corresponds to a SMILES string, with some other tab-separated fields
 * such as molecule's name (id) and its CAS number.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MoleculeFileSmiles extends MoleculeFile {

    Logger logger = LoggerFactory.getLogger(MoleculeFileSmiles.class);

    private int SmilesField;
    private int CASField;
    private int IdField;


    /**
     * Constructor.
     */
    public MoleculeFileSmiles() {
        super();
        SmilesField = -1;
        CASField = -1;
        IdField = -1;
    }


    /**
     * Reads and return next molecule found in file. Returns an IOException if
     * the file has not been already opened. It always return the result of
     * parsed lines, if they contain invalid SMILES this will be eventually
     * seen with the isValid() method of the returned molecule.
     *
     * @return the next parsed molecule in the file
     * @throws java.io.IOException
     */
    @Override
    public InsilicoMolecule ReadNext() throws IOException {

        if (!isFileOpen)
            throw new IOException("File is not open");

        String CurLine = null;
        boolean Proceed = true;

        try {
            while (Proceed) {
                CurLine = reader.readLine();

                if (CurLine == null)
                    return null;
                if ( (CurLine.equalsIgnoreCase("")) || (CurLine.equalsIgnoreCase("\n")))
                    continue;
                Proceed = false;
            }

            Count++;

            InsilicoMolecule m = SmilesMolecule.Convert(CurLine, SmilesField, CASField, IdField);
            if (m.GetId().isEmpty())
                m.SetId("Molecule " + Count);
            return m;
        } catch (IOException e) {
            logger.error("Error while reading file " + this.FileName + " (" + e.getMessage() + ")");
            throw(e);
        }
    }


    /**
     * Reads all the molecules in the file and returns them as an ArrayList.
     * Note: this method will start reading from the current file mark, thus
     * if some molecules have been already parsed with the ReadNext() method,
     * this method will return all the molecules starting from that point.
     *
     * @return array of parsed molecules
     * @throws IOException
     */
    @Override
    public ArrayList<InsilicoMolecule> ReadAll() throws IOException {

        ArrayList<InsilicoMolecule> Mols = new ArrayList<>();
        InsilicoMolecule mol;
        while ((mol=this.ReadNext()) != null)
            Mols.add(mol);
        return Mols;

    }


    /**
     * Sets the field number for the SMILES string in each line parsed from
     * the file.
     * @param SmilesField the SmilesField to set
     */
    public void setSmilesField(int SmilesField) {
        this.SmilesField = SmilesField;
    }

    /**
     * Sets the field number for the CAS number in each line parsed from
     * the file.
     * @param CASField the CASField to set
     */
    public void setCASField(int CASField) {
        this.CASField = CASField;
    }

    /**
     * Sets the field number for the Id string in each line parsed from
     * the file.
     * @param IdField the IdField to set
     */
    public void setIdField(int IdField) {
        this.IdField = IdField;
    }

}
