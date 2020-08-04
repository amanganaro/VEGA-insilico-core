package insilico.core.molecule.conversion.file;

import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.custom.CustomMDLWriter;
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Ancestor abstract class for all molecule readers.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public abstract class MoleculeFile {

    Logger logger = LoggerFactory.getLogger(MoleculeFile.class);


    protected String FileName;
    protected BufferedReader reader;
    protected int Count;
    protected boolean isFileOpen;


    /**
     * Constructor.
     */
    public MoleculeFile() {
        FileName = "";
        reader = null;
        Count = 0;
        isFileOpen = false;
    }


    /**
     * Reads the next molecule. Shall return null if no more molecules are available.
     *
     * @return the read InsilicoMolecule object
     * @throws IOException
     */
    public abstract InsilicoMolecule ReadNext() throws IOException;


    /**
     * Reads all the molecule in the current file.
     *
     * @return array of the read InsilicoMolecule objects
     * @throws IOException
     */
    public abstract ArrayList<InsilicoMolecule> ReadAll() throws IOException;


    /**
     * Opens a new text file for reading.
     *
     * @param FileName file to be be opened
     * @throws FileNotFoundException
     */
    public void OpenFile(String FileName) throws FileNotFoundException {
        this.FileName = FileName;
        reader = new BufferedReader(new FileReader(FileName));
        Count = 0;
        isFileOpen = true;
    }


    /**
     * Closes current file.
     */
    public void CloseFile() {
        try {
            reader.close();
            isFileOpen = false;
        } catch (IOException e) {
            InsilicoLogger.getLogger().warn(e.getMessage());
        }
    }


    /**
     * Gets the current count of read molecules (note: that's not the total
     * number of molecules in the file, just the number that has been
     * processed when this method is invoked).
     *
     * @return number of molecules processed up to now
     */
    public int getCount() {
        return Count;
    }
}
