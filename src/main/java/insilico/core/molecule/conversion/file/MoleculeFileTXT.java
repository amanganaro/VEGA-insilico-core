package insilico.core.molecule.conversion.file;

import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class MoleculeFileTXT extends MoleculeFile{


    public MoleculeFileTXT(){
        super();
    }


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

            InsilicoMolecule mol = SmilesMolecule.Convert(CurLine);
            if (mol.GetId().isEmpty())
                mol.SetId("Molecule " + Count);
            return mol;
        } catch (IOException e) {
            log.error("Error while reading file " + this.FileName + " (" + e.getMessage() + ")");
            throw(e);
        }
    }

    @Override
    public ArrayList<InsilicoMolecule> ReadAll() throws IOException {
        ArrayList<InsilicoMolecule> Mols = new ArrayList<>();
        InsilicoMolecule mol;
        while ((mol=this.ReadNext()) != null)
            Mols.add(mol);
        return Mols;
    }
}
