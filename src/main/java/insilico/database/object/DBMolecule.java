package insilico.database.object;

import insilico.core.exception.DatabaseException;
import insilico.core.similarity.SimilarityDescriptors;
import insilico.database.DBConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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

    
    
////////////////////////////////////////////////////////////////////////////////    
    
    
    public ArrayList<DBDataset> GetDatasets() throws DatabaseException {
        CheckStatus();
        
        ArrayList<DBDataset> list = new ArrayList<DBDataset>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM datasets_link WHERE molecule_id='" + DBId + "'");
        try {
            while (rs.next()) {
                long DSId = rs.getLong("dataset_id");
                DBDataset curDS = new DBDataset(DBConn, DSId);
                list.add(curDS);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
//    public void AddDataset(DBDataset Value) throws DatabaseException {
//        CheckStatus();
//        DBConn.ExecSQL("INSERT INTO datasets_link (molecule_id, dataset_id) VALUES ('" + 
//                DBId + "','" + Value.GetId() +  "')");
//    }
//    
//    public void RemoveDataset(DBDataset Value) throws DatabaseException {
//        CheckStatus();
//        DBConn.ExecSQL("DELETE FROM datasets_link WHERE molecule_id='" + DBId +
//                "' AND dataset_id='" + Value.GetId() + "'");
//    }
    

    //// Experimental
    
    public ArrayList<DBExperimentalValue> GetExperimentals() throws DatabaseException {
        CheckStatus();
        
        ArrayList<DBExperimentalValue> list = new ArrayList<DBExperimentalValue>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM exp_values WHERE molecule_id='" + DBId + "'");
        try {
            while (rs.next()) {
                long ExId = rs.getLong("id");
                DBExperimentalValue curEx = new DBExperimentalValue(DBConn, ExId);
                list.add(curEx);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    public DBExperimentalValue GetExperimental(DBDataset RefDataset) throws DatabaseException {
        CheckStatus();
        long EXId = DBConn.QueryInteger("SELECT * FROM exp_values WHERE molecule_id='" + 
                DBId + "' AND dataset_id='" + RefDataset.GetId() + "'", "id");
        return new DBExperimentalValue(DBConn, EXId);
    }
    
    
    //// Predicted
    
    public ArrayList<DBPredictedValue> GetPredicted() throws DatabaseException {
        CheckStatus();
        
        ArrayList<DBPredictedValue> list = new ArrayList<DBPredictedValue>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM pred_values WHERE molecule_id='" + DBId + "'");
        try {
            while (rs.next()) {
                long PrId = rs.getLong("id");
                DBPredictedValue curPred = new DBPredictedValue(DBConn, PrId);
                list.add(curPred);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    public DBPredictedValue GetPredicted(DBDataset RefDataset) throws DatabaseException {
        CheckStatus();
        long PrId = DBConn.QueryInteger("SELECT * FROM pred_values WHERE molecule_id='" + 
                DBId + "' AND dataset_id='" + RefDataset.GetId() + "'", "id");
        return new DBPredictedValue(DBConn, PrId);
    }
    
    
    
}
