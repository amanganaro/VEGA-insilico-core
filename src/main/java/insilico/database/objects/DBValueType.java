package insilico.database.objects;

import insilico.core.exception.DatabaseException;
import insilico.database.DBConnector;

/**
 * Wrapper for DB value type
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBValueType extends DBObject {

    
    /**
     * Constructor without parameters - not to be used.
     */
    public DBValueType() {
        this(null, -1);
    }
       
    
    /**
     * Constructor with only the DBConnector as parameter. To be used
     * when creating a new object in the db.
     * 
     * @param Conn the connector to the current DB
     */
    public DBValueType(DBConnector Conn) {
        this(Conn, -1);
    }
    
    
    /**
     * Constructor with the DBConnector and the db object id as parameters. To
     * be used when accessing to an existing db object.
     * 
     * @param Conn the connector to the current DB
     * @param Id database id of the current object
     */
    public DBValueType(DBConnector Conn, long Id) {
        super(Conn, Id);
        this.CreateSQLStatement = "INSERT INTO value_type (name) VALUES ('-')";
    }    
    

    
    //// Name 
    
    public String GetName() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, name FROM value_type WHERE id='" + DBId + "'", "name");
    }
    
    public void SetName(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE value_type SET name='" + Value + "' WHERE id='" + DBId + "'");
    }
    
    
    //// Units
    
    public String GetUnits() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, units FROM value_type WHERE id='" + DBId + "'", "units");
    }
    
    public void SetUnits(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE value_type SET units='" + Value + "' WHERE id='" + DBId + "'");
    }
    
    
    //// Is Numeric
    
    public boolean GetIsNumeric() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryBoolean("SELECT id, is_numeric FROM value_type WHERE id='" + DBId + "'", "is_numeric");
    }
    
    public void SetIsNumeric(boolean Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE value_type SET is_numeric='" + Value + "' WHERE id='" + DBId + "'");
    }
    

    //// Is Experimental
    
    public boolean GetIsExperimental() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryBoolean("SELECT id, is_experimental FROM value_type WHERE id='" + DBId + "'", "is_experimental");
    }
    
    public void SetIsExperimental(boolean Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE value_type SET is_experimental='" + Value + "' WHERE id='" + DBId + "'");
    }
    

    //// Endpoint
    
    public DBEndpoint GetEndpoint() throws DatabaseException {
        CheckStatus();
        long EPId = DBConn.QueryInteger("SELECT id, endpoint_id FROM value_type WHERE id='" + DBId + "'", "endpoint_id");
        return (new DBEndpoint(DBConn, EPId));
    }
    
    public void SetEndpoint(DBEndpoint Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE value_type SET endpoint_id='" + Value.GetId() + "' WHERE id='" + DBId + "'");
    }
    

    //// Classes values

    public void SetClassForValue(int Value, String ClassName) throws DatabaseException {
        CheckStatus();
        try {
            
            long ValueClassId = DBConn.QueryInteger("SELECT * FROM value_classes WHERE value_type_id='" + DBId + 
                    "' AND value='" + Value +"'", "id");
            
            // value_classes_id found, just update
            DBConn.ExecSQL("UPDATE value_classes SET classname='" + ClassName + "' WHERE id='" + ValueClassId + "'");
            
        } catch (DatabaseException e) {
            // no value_classes_id found, insert new
            DBConn.ExecSQL("INSERT INTO value_classes (value, classname, value_type_id) VALUES ('" + 
                    Value + "', '" + ClassName + "', '" + DBId + "')");            
        }
    }

    public String GetClassForValue(double Value) throws DatabaseException {
        CheckStatus();
        
        String ClassName;
        try {
            ClassName = DBConn.QueryString("SELECT * FROM value_classes WHERE value_type_id='" + DBId + 
                    "' AND value='" + Value +"'", "classname");
        } catch (DatabaseException e) {
            ClassName = null;
        }
        return ClassName;
    }
    
    
}
