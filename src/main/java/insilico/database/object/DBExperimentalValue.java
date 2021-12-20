package insilico.database.object;

import insilico.core.exception.DatabaseException;
import insilico.database.DBConnector;

/**
 * Wrapper for DB experimental value
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBExperimentalValue extends DBObject {

    
    /**
     * Constructor without parameters - not to be used.
     */
    public DBExperimentalValue() {
        this(null, -1);
    }
       
    
    /**
     * Constructor with only the DBConnector as parameter. To be used
     * when creating a new object in the db.
     * 
     * @param Conn the connector to the current DB
     */
    public DBExperimentalValue(DBConnector Conn) {
        this(Conn, -1);
    }
    
    
    /**
     * Constructor with the DBConnector and the db object id as parameters. To
     * be used when accessing to an existing db object.
     * 
     * @param Conn the connector to the current DB
     * @param Id database id of the current object
     */
    public DBExperimentalValue(DBConnector Conn, long Id) {
        super(Conn, Id);
        this.CreateSQLStatement = "INSERT INTO exp_values (source) VALUES ('-')";
    }    
    

    
    //// Molecule
    
    public DBMolecule GetMolecule() throws DatabaseException {
        CheckStatus();
        long MId = DBConn.QueryInteger("SELECT id, molecule_id FROM exp_values WHERE id='" + DBId + "'", "molecule_id");
        return (new DBMolecule(DBConn, MId));
    }
    
    public void SetMolecule(DBMolecule Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE exp_values SET molecule_id='" + Value.GetId() + "' WHERE id='" + DBId + "'");
    }

    
    //// Experimental
    
    public DBExperimental GetExperimental() throws DatabaseException {
        CheckStatus();
        long EXId = DBConn.QueryInteger("SELECT id, experimental_id FROM exp_values WHERE id='" + DBId + "'", "experimental_id");
        return (new DBExperimental(DBConn, EXId));
    }
    
    public void SetExperimental(DBExperimental Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE exp_values SET experimental_id='" + Value.GetId() + "' WHERE id='" + DBId + "'");
    }

    
    //// Dataset
    
    public DBDataset GetDatasetl() throws DatabaseException {
        CheckStatus();
        long DSId = DBConn.QueryInteger("SELECT id, dataset_id FROM exp_values WHERE id='" + DBId + "'", "dataset_id");
        return (new DBDataset(DBConn, DSId));
    }
    
    public void SetDataset(DBDataset Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE exp_values SET dataset_id='" + Value.GetId() + "' WHERE id='" + DBId + "'");
    }

    
    //// Source 
    
    public String GetSource() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryString("SELECT id, source FROM exp_values WHERE id='" + DBId + "'", "source");
    }
    
    public void SetSource(String Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE exp_values SET source='" + Value + "' WHERE id='" + DBId + "'");
    }
    
    
    //// Experimental values 
    
    public double GetValueNumerical() throws DatabaseException {
        CheckStatus();
        return DBConn.QueryDouble("SELECT * FROM exp_values WHERE id='" + DBId + "'", "value_num");
    }
    
    
    public void SetValueNumerical(double Value) throws DatabaseException {
        CheckStatus();
        DBConn.ExecSQL("UPDATE exp_values SET value_num='" + Value + "' WHERE id='" + DBId + "'");
    }
    
    
    public String GetValueString() throws DatabaseException {
        CheckStatus();
        DBExperimental CurExp = GetExperimental();
        if (CurExp.GetNumeric())
            throw new DatabaseException("trying to get non-numeric value from numeric field");
        return CurExp.GetClassLabel(GetValueNumerical());
    }
    
}
