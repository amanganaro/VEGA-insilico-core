package insilico.database.objects;

import insilico.core.exception.DatabaseException;
import insilico.database.DBConnector;

/**
 * Ancestor for Molecule DB objects
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBObject {

    protected long DBId;
    protected String CreateSQLStatement;
    protected DBConnector DBConn;
    
    
    //// constructors
    
    public DBObject() {
        this(null, -1);
    }
       
    public DBObject(DBConnector Conn) {
        this(Conn, -1);
    }
    
    public DBObject(DBConnector Conn, long Id) {
        DBConn = Conn;
        DBId = Id;
    }

    
    
    public void CreateIntoDB() throws DatabaseException {
        if ((DBId>-1)||(DBConn==null))
            throw new DatabaseException("Unable to create db object");
        this.DBId = DBConn.ExecSQLWithAutogenerate(CreateSQLStatement, "id");
    }
    
    
    public final long GetId() {
        return this.DBId;
    }
    
    
    protected final void CheckStatus() throws DatabaseException {
        if (DBConn==null)
            throw new DatabaseException("Database connection not set");
        if (DBId<0)
            throw new DatabaseException("Data object not set");
    }
    
    
    public DBConnector GetDBConnector() {
        return DBConn;
    }
            
}
