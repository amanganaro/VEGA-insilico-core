package insilico.database.objects;

import insilico.core.exception.DatabaseException;
import insilico.core.similarity.SimilarityDescriptors;
import insilico.database.DBConnector;

/**
 * Wrapper for DB molecules.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBMolecule extends DBObject {


    /**
     * Constructor without parameters - not to be used.
     */
    public DBMolecule() {
        this(null, -1);
    }
       
    
    /**
     * Constructor with only the DBConnector as parameter. To be used
     * when creating a new object in the db.
     * 
     * @param Conn the connector to the current DB
     */
    public DBMolecule(DBConnector Conn) {
        this(Conn, -1);
    }
    
    
    /**
     * Constructor with the DBConnector and the db object id as parameters. To
     * be used when accessing to an existing db object.
     * 
     * @param Conn the connector to the current DB
     * @param Id database id of the current object
     */
    public DBMolecule(DBConnector Conn, long Id) {
        super(Conn, Id);
        this.CreateSQLStatement = "INSERT INTO molecules (name) VALUES ('-')";
    }
    

    /**
     * Overridden method for creation into db. It calls the super CreateIntoDB
     * method and then creates the related row in the smiles table.
     * 
     * @throws DatabaseException 
     */
    @Override
    public void CreateIntoDB() throws DatabaseException {
        long smiles_id;
        
        super.CreateIntoDB();
        smiles_id = DBConn.ExecSQLWithAutogenerate("INSERT INTO smiles (smiles) VALUES ('-')", "id");
        DBConn.ExecSQL("UPDATE smiles SET molecule_id='" + DBId + "' WHERE id='" + smiles_id + "'");
    }    
    
    
    //// Name 
    
    public String GetName() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, name FROM molecules WHERE id='" + DBId + "'", "name");
    }
    
    public void SetName(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE molecules SET name='" + DBConnector.FixSpecialChars(Value) + "' WHERE id='" + DBId + "'");
    }

    
    //// CAS
    
    public String GetCAS() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, cas FROM molecules WHERE id='" + DBId + "'", "cas");
    }
    
    public void SetCAS(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE molecules SET cas='" + DBConnector.FixSpecialChars(Value) + "' WHERE id='" + DBId + "'");
    }
    
    
    //// SMILES
    
    public String GetSMILES() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, smiles FROM smiles WHERE molecule_id='" + DBId + "'", "smiles");
    }

    public void SetSMILES(String SMILES) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE smiles SET smiles='" + SMILES + "' WHERE molecule_id='" + DBId + "'");
    }
    
    
    //// Atoms (no. of atoms)
    
//    public int GetAtoms() throws DatabaseException {
//        CheckStatus();
//        return (int)DBConn.QueryInteger("SELECT id, atoms FROM molecules WHERE id='" + DBId + "'", "atoms");
//    }
//    
//    public void SetAtoms(int Value) throws DatabaseException {
//        CheckStatus();
//        DBConn.ExecSQL("UPDATE molecules SET atoms='" + Value + "' WHERE id='" + DBId + "'");
//    }

    
    //// Screening FP
    
    public long GetScreeningFP() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryInteger("SELECT id, screeningfp FROM molecules WHERE id='" + DBId + "'", "screeningfp");
    }
    
    public void SetScreeningFP(long Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE molecules SET screeningfp='" + Value + "' WHERE id='" + DBId + "'");
    }
    

    //// Similarity Descriptors
    
    public SimilarityDescriptors GetDescriptors() throws DatabaseException {
        CheckStatus();
        return (SimilarityDescriptors) DBConn.QueryObject("SELECT id, sim_descriptors FROM smiles WHERE molecule_id='" + DBId + "'", "sim_descriptors");
    }
    
    public void SetDescriptors(SimilarityDescriptors Value) throws DatabaseException {
        CheckStatus();
        SimilarityDescriptors[] bufArray = new SimilarityDescriptors[1];
        bufArray[0] = Value;
        DBConn.ExecSQLWithParam("UPDATE smiles SET sim_descriptors=? WHERE molecule_id='" + DBId + "'", bufArray);
        
        // When descriptors are set, also no. of atoms are set
//        int nAtoms = (int)Value.Constitutional[9];  //nSK is the 9th descriptor
//        DBConn.ExecSQL("UPDATE molecules SET atoms='" + nAtoms + "' WHERE id='" + DBId + "'");
    }    
    

    //// Alerts 
    
    public String GetAlerts() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, alerts FROM molecules WHERE id='" + DBId + "'", "alerts");
    }
    
    public void SetAlerts(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE molecules SET alerts='" + DBConnector.FixSpecialChars(Value) + "' WHERE id='" + DBId + "'");
    }

 
}
