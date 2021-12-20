package insilico.database.objects;

import insilico.core.exception.DatabaseException;
import insilico.database.DBConnector;

/**
 * Wrapper for DB experimental value
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBValue extends DBObject {

    
    /**
     * Constructor without parameters - not to be used.
     */
    public DBValue() {
        this(null, -1);
    }
       
    
    /**
     * Constructor with only the DBConnector as parameter. To be used
     * when creating a new object in the db.
     * 
     * @param Conn the connector to the current DB
     */
    public DBValue(DBConnector Conn) {
        this(Conn, -1);
    }
    
    
    /**
     * Constructor with the DBConnector and the db object id as parameters. To
     * be used when accessing to an existing db object.
     * 
     * @param Conn the connector to the current DB
     * @param Id database id of the current object
     */
    public DBValue(DBConnector Conn, long Id) {
        super(Conn, Id);
        this.CreateSQLStatement = "INSERT INTO value_values (value) VALUES ('0')";
    }    
    
        
        
    //// Experimental values 
    
    public double GetValue() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryDouble("SELECT id, value FROM value_values WHERE id='" + DBId + "'", "value");
    }
    
    
    public void SetValue(double Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE value_values SET value='" + Value + "' WHERE id='" + DBId + "'");
    }

    
    //// Value Type
    
    public DBValueType GetValueType() throws DatabaseException {
        CheckStatus();
        long VTId = DBConn.QueryInteger("SELECT id, value_type_id FROM value_values WHERE id='" + DBId + "'", "value_type_id");
        return (new DBValueType(DBConn, VTId));
    }
    
    public void SetValueType(DBValueType Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE value_values SET value_type_id='" + Value.GetId() + "' WHERE id='" + DBId + "'");
    }
    
    
    //// Molecule
    
    public DBMolecule GetMolecule() throws DatabaseException {
        CheckStatus();
        long MId = DBConn.QueryInteger("SELECT id, molecules_id FROM value_values WHERE id='" + DBId + "'", "molecules_id");
        return (new DBMolecule(DBConn, MId));
    }
    
    public void SetMolecule(DBMolecule Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE value_values SET molecules_id='" + Value.GetId() + "' WHERE id='" + DBId + "'");
    }

    
    //// Output as formatted value
    
    public String GetFormattedValue() throws DatabaseException {
        String res = "";
        
        DBValueType curValType = this.GetValueType();
        double curVal = this.GetValue();
        
        if (curValType.GetIsNumeric()) {
            
            // Numerical format
            res = new Double(curVal).toString();
            String curUnits = curValType.GetUnits();
            if (curUnits != null)
                if (curUnits.length() > 0)
                    res += " " + curUnits;
            
        } else {
            
            // Qualitative format
            String curClass = curValType.GetClassForValue(curVal);
            res = new Double(curVal).toString();
            if (curClass != null)
                if (curClass.length() > 0)
                    res = curClass;
        }
        
        return res;
    }
    
}
