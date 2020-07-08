package insilico.core.molecule.conversion.file;

import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.MDLMolecule;
import insilico.core.tools.logger.InsilicoLogger;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Reader for SDF file. The reader can recognize tags for molecule's id and
 * CAS number, if provided.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MoleculeFileSDF extends MoleculeFile {

    private String CASTag;
    private String IdTag;


    /**
     * Constructor.
     */
    public MoleculeFileSDF() {
        super();
        CASTag = null;
        IdTag = null;
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

        try {
            // Parses SDF and separates each single MDL molecule
            String MDLMol = "";
            boolean Proceed = true;

            while (Proceed) {
                String CurLine = reader.readLine();

                if ((CurLine == null) || (CurLine.compareTo("$$$$") == 0)) {
                    Proceed = false;
                } else {
                    MDLMol += CurLine + "\n";
                }
            }

            if (MDLMol.isEmpty())
                return null;

            Count++;

            byte[] bytes = MDLMol.getBytes();
            InsilicoMolecule m = MDLMolecule.Convert(bytes, CASTag, IdTag);
            if (m.GetId().isEmpty())
                m.SetId("Molecule " + Count);
            return m;
        } catch (IOException e) {
            InsilicoLogger.getLogger().error("Error while reading file " + this.FileName + " (" + e.getMessage() + ")");
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
     * @param CASTag the CASTag to set
     */
    public void setCASTag(String CASTag) {
        this.CASTag = CASTag;
    }

    /**
     * @param IdTag the IdTag to set
     */
    public void setIdTag(String IdTag) {
        this.IdTag = IdTag;
    }
}
