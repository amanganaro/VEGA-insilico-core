package insilico.database.object;

import insilico.core.exception.DatabaseException;
import insilico.database.DBConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Wrapper for DB datasets
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBDataset extends DBObject {

    public final static short STATUS_UNDEFINED = -1;
    public final static short STATUS_TRAINING = 0;
    public final static short STATUS_TEST = 1;
    

    /**
     * Constructor without parameters - not to be used.
     */
    public DBDataset() {
        this(null, -1);
    }
       
    
    /**
     * Constructor with only the DBConnector as parameter. To be used
     * when creating a new object in the db.
     * 
     * @param Conn the connector to the current DB
     */
    public DBDataset(DBConnector Conn) {
        this(Conn, -1);
    }
    
    
    /**
     * Constructor with the DBConnector and the db object id as parameters. To
     * be used when accessing to an existing db object.
     * 
     * @param Conn the connector to the current DB
     * @param Id database id of the current object
     */
    public DBDataset(DBConnector Conn, long Id) {
        super(Conn, Id);
        this.CreateSQLStatement = "INSERT INTO datasets (name) VALUES ('-')";
    }    
    

    
    
    //// Reference Experimental 
    
    public DBExperimental GetExperimental() throws DatabaseException {
        CheckStatus();
        long expId = DBConn.QueryInteger("SELECT id, experimental_id FROM datasets WHERE id='" + DBId + "'", "experimental_id");
        return  new DBExperimental(DBConn, expId);
    }
    
    public void SetExperimental(DBExperimental Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE datasets SET experimental_id='" + Value.GetId() + "' WHERE id='" + DBId + "'");
    }
    
    
    //// Name 
    
    public String GetName() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, name FROM datasets WHERE id='" + DBId + "'", "name");
    }
    
    public void SetName(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE datasets SET name='" + Value + "' WHERE id='" + DBId + "'");
    }
    
    
    //// Description 
    
    public String GetDescription() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, description FROM datasets WHERE id='" + DBId + "'", "description");
    }
    
    public void SetDescription(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE datasets SET description='" + Value + "' WHERE id='" + DBId + "'");
    }
    
    
////////////////////////////////////////////////////////////////////////////////    

    
    private int GetMoleculesNumber(Integer Status) throws DatabaseException {
        CheckStatus();

        String QueryStr = "SELECT COUNT(*) FROM datasets_link WHERE dataset_id='" + 
                DBId + "'";
        if (Status != null)
            QueryStr +=  " AND status='" + Status.toString() + "'";
        
        ResultSet rs = DBConn.QueryRS(QueryStr);
            
        int num = 0;
        try {
            if (rs.next()) {
                num = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return num;
    }
    

    public int GetMoleculesNumber() throws DatabaseException {
        return GetMoleculesNumber(null);
    }
    

    public int GetMoleculesNumber(short Status) throws DatabaseException {
        return GetMoleculesNumber(new Integer(Status));
    }
    
    
    private ArrayList<DBMolecule> GetMolecules(Integer Status) throws DatabaseException {
        CheckStatus();
        
        String QueryStr = "SELECT * FROM datasets_link WHERE dataset_id='" 
                + DBId + "'";
        if (Status != null)
            QueryStr +=  " AND status='" + Status.toString() + "'";
        
        ArrayList<DBMolecule> list = new ArrayList<DBMolecule>();
        ResultSet rs = DBConn.QueryRS(QueryStr);
        try {
            while (rs.next()) {
                long MolId = rs.getLong("molecule_id");
                DBMolecule curMol = new DBMolecule(DBConn, MolId);
                list.add(curMol);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }    
    
    
    public ArrayList<DBMolecule> GetMolecules() throws DatabaseException {
        return GetMolecules(null);
    }
    
    public ArrayList<DBMolecule> GetMolecules(short Status) throws DatabaseException {
        return GetMolecules(new Integer(Status));
    }

    
    public int GetMoleculeStatus(DBMolecule Value) throws DatabaseException {
        CheckStatus();
        
        return (int)DBConn.QueryInteger("SELECT * FROM datasets_link WHERE dataset_id='" + DBId + 
                "' AND molecule_id='" + Value.GetId() + "'", "status");
    }
    
    public void AddMolecule(DBMolecule Value) throws DatabaseException {
        AddMolecule(Value, STATUS_UNDEFINED);
    }
    
    public void AddMolecule(DBMolecule Value, int MolStatus) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("INSERT INTO datasets_link (molecule_id, dataset_id, status) VALUES ('" + 
                Value.GetId() + "','" + DBId +  "','" + MolStatus + "')");
    }
    
    public void RemoveMolecule(DBMolecule Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("DELETE FROM datasets_link WHERE molecule_id='" + Value.GetId() +
                "' AND dataset_id='" + DBId + "'");
    }    
        
}
