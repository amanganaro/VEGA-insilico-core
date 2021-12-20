package insilico.database.objects;

import insilico.core.exception.DatabaseException;
import insilico.database.DBConnector;


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
    
    
}
