package insilico.database.object;

import insilico.core.exception.DatabaseException;
import insilico.core.similarity.SimilarityDescriptors;
import insilico.database.DBConnector;

/**
 * Wrapper for DB SMILES
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBSmiles extends DBObject {


    /**
     * Constructor without parameters - not to be used.
     */
    public DBSmiles() {
        this(null, -1);
    }
       
    
    /**
     * Constructor with only the DBConnector as parameter. To be used
     * when creating a new object in the db.
     * 
     * @param Conn the connector to the current DB
     */
    public DBSmiles(DBConnector Conn) {
        this(Conn, -1);
    }
    
    
    /**
     * Constructor with the DBConnector and the db object id as parameters. To
     * be used when accessing to an existing db object.
     * 
     * @param Conn the connector to the current DB
     * @param Id database id of the current object
     */
    public DBSmiles(DBConnector Conn, long Id) {
        super(Conn, Id);
        this.CreateSQLStatement = "INSERT INTO smiles (smiles) VALUES ('-')";
    }    
        
    
    
    
    //// Molecule
    
    public DBMolecule GetMolecule() throws DatabaseException {
        CheckStatus();
        long Mol_id = DBConn.QueryInteger("SELECT id, molecule_id FROM smiles WHERE id='" + DBId + "'", "molecule_id");
        return new DBMolecule(DBConn, Mol_id);
   }
    
    public void SetMolecule(DBMolecule Mol) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE smiles SET molecule_id='" + Mol.GetId() + "' WHERE id='" + DBId + "'");
    }
    
    
    //// SMILES
    
    public String GetSMILES() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, smiles FROM smiles WHERE id='" + DBId + "'", "smiles");
    }
    
    public void SetSMILES(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE smiles SET smiles='" + DBConnector.FixSpecialChars(Value) + "' WHERE id='" + DBId + "'");
    }
    
    
    //// Similarity Descriptors
    
    public SimilarityDescriptors GetDescriptors() throws DatabaseException {
        CheckStatus();
        return (SimilarityDescriptors) DBConn.QueryObject("SELECT id, descriptors FROM smiles WHERE id='" + DBId + "'", "descriptors");
    }
    
    public void SetDescriptors(SimilarityDescriptors Value) throws DatabaseException {
        CheckStatus();
        SimilarityDescriptors[] bufArray = new SimilarityDescriptors[1];
        bufArray[0] = Value;
        DBConn.ExecSQLWithParam("UPDATE smiles SET descriptors=? WHERE id='" + DBId + "'", bufArray);
    }

    
    
}
