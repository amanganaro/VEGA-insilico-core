package insilico.database.object;

import insilico.core.exception.DatabaseException;
import insilico.database.DBConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Wrapper for DB endpoint
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBEndpoint extends DBObject {

    
    /**
     * Constructor without parameters - not to be used.
     */
    public DBEndpoint() {
        this(null, -1);
    }
       
    
    /**
     * Constructor with only the DBConnector as parameter. To be used
     * when creating a new object in the db.
     * 
     * @param Conn the connector to the current DB
     */
    public DBEndpoint(DBConnector Conn) {
        this(Conn, -1);
    }
    
    
    /**
     * Constructor with the DBConnector and the db object id as parameters. To
     * be used when accessing to an existing db object.
     * 
     * @param Conn the connector to the current DB
     * @param Id database id of the current object
     */
    public DBEndpoint(DBConnector Conn, long Id) {
        super(Conn, Id);
        this.CreateSQLStatement = "INSERT INTO endpoints (name) VALUES ('-')";
    }   
    

    
    //// Name 
    
    public String GetName() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, name FROM endpoints WHERE id='" + DBId + "'", "name");
    }
    
    public void SetName(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE endpoints SET name='" + Value + "' WHERE id='" + DBId + "'");
    }
    
    
    //// Description 
    
    public String GetDescription() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, description FROM endpoints WHERE id='" + DBId + "'", "description");
    }
    
    public void SetDescription(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE endpoints SET description='" + Value + "' WHERE id='" + DBId + "'");
    }
    
    
////////////////////////////////////////////////////////////////////////////////    

    //// Experimental
    
    public int GetExperimentalsNumber() throws DatabaseException {
        CheckStatus();
        
        ResultSet rs = DBConn.QueryRS("SELECT COUNT(*) FROM experimental WHERE endpoint_id='" + DBId + "'");
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
    
    public ArrayList<DBExperimental> GetExperimentals() throws DatabaseException {
        CheckStatus();
        
        ArrayList<DBExperimental> list = new ArrayList<DBExperimental>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM experimental WHERE endpoint_id='" + DBId + "'");
        try {
            while (rs.next()) {
                long ExpId = rs.getLong("id");
                DBExperimental curExp = new DBExperimental(DBConn, ExpId);
                list.add(curExp);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    
}
