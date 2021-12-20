package insilico.database.object;

import insilico.core.exception.DatabaseException;
import insilico.database.DBConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Wrapper for DB experimental
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBExperimental extends DBObject {

    
    /**
     * Constructor without parameters - not to be used.
     */
    public DBExperimental() {
        this(null, -1);
    }
       
    
    /**
     * Constructor with only the DBConnector as parameter. To be used
     * when creating a new object in the db.
     * 
     * @param Conn the connector to the current DB
     */
    public DBExperimental(DBConnector Conn) {
        this(Conn, -1);
    }
    
    
    /**
     * Constructor with the DBConnector and the db object id as parameters. To
     * be used when accessing to an existing db object.
     * 
     * @param Conn the connector to the current DB
     * @param Id database id of the current object
     */
    public DBExperimental(DBConnector Conn, long Id) {
        super(Conn, Id);
        this.CreateSQLStatement = "INSERT INTO experimental (name) VALUES ('-')";
    }    
    

    
    //// Name 
    
    public String GetName() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, name FROM experimental WHERE id='" + DBId + "'", "name");
    }
    
    public void SetName(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE experimental SET name='" + Value + "' WHERE id='" + DBId + "'");
    }
    
    
    //// Units
    
    public String GetUnits() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, units FROM experimental WHERE id='" + DBId + "'", "units");
    }
    
    public void SetUnits(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE experimental SET units='" + Value + "' WHERE id='" + DBId + "'");
    }
    
    
    //// Numeric
    
    public boolean GetNumeric() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryBoolean("SELECT id, numeric FROM experimental WHERE id='" + DBId + "'", "numeric");
    }
    
    public void SetNumeric(boolean Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE experimental SET numeric='" + Value + "' WHERE id='" + DBId + "'");
    }
    

    //// Endpoint
    
    public DBEndpoint GetEndpoint() throws DatabaseException {
        CheckStatus();
        long EPId = DBConn.QueryInteger("SELECT id, endpoint_id FROM experimental WHERE id='" + DBId + "'", "endpoint_id");
        return (new DBEndpoint(DBConn, EPId));
    }
    
    public void SetEndpoint(DBEndpoint Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE experimental SET endpoint_id='" + Value.GetId() + "' WHERE id='" + DBId + "'");
    }
    


    public String GetClassLabel(double Value) throws DatabaseException {
        CheckStatus();
        if (this.GetNumeric())
            throw new DatabaseException("trying to get class label for numeric experimental data");
        return DBConn.QueryString("SELECT * FROM exp_classes WHERE experimental_id='" + DBId + 
                "' AND value='" + Integer.toString((int)Math.round(Value)) + "'", "class_str");
    }
    
    
    public void SetClassLabel(double Value, String LabelValue) throws DatabaseException {
        CheckStatus();
        if (this.GetNumeric())
            throw new DatabaseException("trying to store class label for numeric experimental data");
        int intValue = ((int)Math.round(Value));
        ResultSet rs = DBConn.QueryRS("SELECT * FROM exp_classes WHERE experimental_id='" + DBId + 
                "' AND value='" + Integer.toString(intValue) + "'");
        String sql;
        try {
            if (rs.next()) 
                sql = "UPDATE exp_classes SET class_str='" + LabelValue + "' WHERE experimental_id='" +
                        DBId + "' AND value='" + Integer.toString(intValue) + "'";
            else
                sql = "INSERT INTO exp_classes (experimental_id, value, class_str) VALUES ('" + 
                        DBId + "','" + Integer.toString(intValue) + "','" + LabelValue + "')";
            DBConn.ExecSQL(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
    

    
////////////////////////////////////////////////////////////////////////////////    

    //// Molecule
    
    public int GetMoleculesNumber() throws DatabaseException {
        CheckStatus();
        
        ResultSet rs = DBConn.QueryRS("SELECT COUNT(*) FROM exp_values WHERE experimental_id='" + DBId + "'");
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
    
    public ArrayList<DBMolecule> GetMolecules() throws DatabaseException {
        CheckStatus();
        
        ArrayList<DBMolecule> list = new ArrayList<DBMolecule>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM exp_values WHERE experimental_id='" + DBId + "'");
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
    
    public boolean HasExperimental(DBMolecule Mol) throws DatabaseException {
        CheckStatus();
        
        ResultSet rs = DBConn.QueryRS("SELECT COUNT(*) FROM exp_values WHERE molecule_id='" + Mol.GetId() + "' AND experimental_id='" + DBId + "'");
        int num = 0;
        try {
            if (rs.next()) {
                num = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        
        if (num > 0)
            return true;
        else
            return false;
    }
    
    
    
}
