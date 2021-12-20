package insilico.database;

import insilico.core.exception.DatabaseException;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Class for Molecule DB connection.<p>
 * Implements basic methods to build and connect to DB and to perform queries.
 * The connection is based on HSQLDB libraries.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBConnector {
    
    // Flags for DB connection
    public final static short OPEN_EXISTING_LOCAL = 10;  // Open existing local file DB
    public final static short OPEN_EXISTING_REMOTE = 20; // Open existing remote DB
    public final static short OPEN_NEW = 30;             // Open new DB, target dir should NOT be existing
    public final static short OPEN_NEW_OVERWRITE = 31;   // Open new DB, delete target dir if already exists
    
    
    // Connection
    private Connection Conn;
    private final short ConnType;
    
    
    /**
     * Constructor - creates DB object and connect it
     * 
     * @param DbURL full path of the HSQLDB database folder or DB address
     * if opening a remote connection
     * @param OpenType flag for connection type (open existing db, new db or 
     * new db overwriting existing folder), use public constants available
     * in this class to set the flag
     * @throws DatabaseException
     */     
    public DBConnector(String DbURL, short OpenType) 
            throws DatabaseException {

        ConnType = OpenType;
        
        if (OpenType == OPEN_EXISTING_REMOTE) {

            // connects to an existing DB
            try {
                this.ConnectRemote(DbURL);
            } catch (SQLException e) {
                throw new DatabaseException("Unable to connect to existing DB (" + 
                        e.getMessage() + ")");
            }
            return;
        }
        
        
        // Builds the correct final dir for DB files
        if (DbURL.charAt(DbURL.length()-1) != System.getProperty("file.separator").charAt(0)) {
            DbURL += System.getProperty("file.separator");
        }
        DbURL += "dbdata";


        if (OpenType == OPEN_EXISTING_LOCAL) {

            // connects to an existing DB
            try {
                this.ConnectLocal(DbURL);
            } catch (SQLException e) {
                throw new DatabaseException("Unable to connect to existing DB (" + 
                        e.getMessage() + ")");
            }
            return;
        }
        
        // New db - checks if folder already exists
        File destFolder = new File(DbURL);
        
        if (destFolder.exists()) {
            
            if (OpenType == OPEN_NEW)
                throw new DatabaseException("DB destination folder already exists");

            if (OpenType == OPEN_NEW_OVERWRITE) {
                try {
                    delete(destFolder);
                } catch (IOException e) {
                    throw new DatabaseException("Unable to remove existing folder");
                }
            }
            
        }
        
        // Connects and creates DB
        try {
            this.ConnectLocal(DbURL);
            Statement st = Conn.createStatement();
            st.execute("DROP SCHEMA PUBLIC CASCADE");
            this.CreateDBStructure();
        } catch (SQLException e) {
            throw new DatabaseException("Unable to create and connect to a new DB (" + 
                    e.getMessage() + ")");
        }
        
    }
    
    
    
////////////////////////////////////////////////////////////////////////////////
// General DB methods
////////////////////////////////////////////////////////////////////////////////
    
    
    /**
     * Connects to a local HSQL DB in file mode, given its folder.
     * 
     * @param DBFolder path of the DB folder
     * @throws SQLException 
     */
    private void ConnectLocal(String DBFolder) throws SQLException {
        
        // Connect to DB
        Conn = DriverManager.getConnection("jdbc:hsqldb:file:" + DBFolder);
        
    }
    
    
    /**
     * Connects to a HSQL DB server.
     * 
     * @param DBURL URL of the DB server
     * @throws SQLException 
     */
    private void ConnectRemote(String DBURL) throws SQLException {
        
        // Connect to DB
        Conn = DriverManager.getConnection("jdbc:hsqldb:hsql://" + DBURL);
        
    }
    

    /**
     * Closes current DB connection. Before closing, executes a SHUTDOWN
     * command to correctly dispose the HSQL DB.
     * 
     * @throws DatabaseException 
     */
    public void Close() throws DatabaseException {
        try {
            Conn.commit();
            if (ConnType != OPEN_EXISTING_REMOTE) {
                Statement st = Conn.createStatement();
                st.execute("SHUTDOWN");
            }
            Conn.close();
        } catch (SQLException e) {
            throw new DatabaseException("Unable close DB (" + 
                    e.getMessage() + ")");
        }
    }
    
    
    /**
     * Creates the structure (tables) in the current connected DB.
     * 
     * @throws SQLException 
     */
    private void CreateDBStructure() throws SQLException {
        
        // MOLECULES table
        update("CREATE MEMORY TABLE molecules ("+
                "id BIGINT IDENTITY," +
                "name VARCHAR(512) ," +
                "cas VARCHAR(16) ," +
                "screeningfp BIGINT ," +
                "alerts VARCHAR(4096) " +
                ");");

        // SMILES table
        update("CREATE CACHED TABLE smiles ("+
                "id BIGINT IDENTITY," +
                "molecule_id BIGINT ," +
                "smiles VARCHAR(2048) ," +
                "sim_descriptors OTHER ," +
                "CONSTRAINT fk_smiles_molecules " +
                "  FOREIGN KEY (molecule_id) " +
                "  REFERENCES molecules (id) " +
                "  ON DELETE CASCADE " +
                "  ON UPDATE NO ACTION " +
                ");");

        // DATASETS table
        update("CREATE MEMORY TABLE datasets ("+
                "id BIGINT IDENTITY," +
                "name VARCHAR(128)," +
                "description VARCHAR(1024)" +
                ");");
        
        // DATASETS MOLECULES table
        update("CREATE CACHED TABLE datasets_molecules ("+
                "status SMALLINT," +  // 0 = na, 1 = train, 2 = test
                "molecule_id BIGINT," +
                "dataset_id BIGINT," +
                "PRIMARY KEY (dataset_id, molecule_id) ," +
//                "INDEX fk_datasets_link_molecules_idx (molecule_id ASC) ," +
//                "INDEX fk_datasets_link_datasets_idx (dataset_id ASC) ," +
                "CONSTRAINT fk_datasets_link_molecules" +
                "  FOREIGN KEY (molecule_id)" +
                "  REFERENCES molecules (id)" +
                "  ON DELETE CASCADE" +
                "  ON UPDATE NO ACTION," +
                "CONSTRAINT fk_datasets_link_datasets" +
                "  FOREIGN KEY (dataset_id)" +
                "  REFERENCES datasets (id)" +
                "  ON DELETE CASCADE" +
                "  ON UPDATE NO ACTION" +
                ");");
        
        // END-POINTS table
        update("CREATE MEMORY TABLE endpoints ("+
                "id BIGINT IDENTITY," +
                "name VARCHAR(128)," +
                "description VARCHAR(1024)" +
                ");");

        // VALUE TYPE table
        update("CREATE MEMORY TABLE value_type ("+
                "id BIGINT IDENTITY," +
                "name VARCHAR(128) ," +
                "units VARCHAR(64) ," +
                "is_numeric BOOLEAN ," +
                "is_experimental BOOLEAN ," +
//                "value_classes_id BIGINT ," +
                "endpoint_id BIGINT ," +
//                "INDEX fk_value_type_endpoints_idx (endpoints_id ASC) ," +
                "CONSTRAINT fk_value_type_endpoints" +
                "  FOREIGN KEY (endpoint_id)" +
                "  REFERENCES endpoints (id)" +
                "  ON DELETE NO ACTION" +
                "  ON UPDATE NO ACTION" +
//                "CONSTRAINT fk_value_type_value_classes" +
//                "  FOREIGN KEY (value_classes_id)" +
//                "  REFERENCES value_classes (id)" +
//                "  ON DELETE NO ACTION" +
//                "  ON UPDATE NO ACTION" +
                ");");        

        // VALUE CLASSES table
        update("CREATE MEMORY TABLE value_classes ("+
                "id BIGINT IDENTITY," +
                "value INT ," +
                "classname VARCHAR(64) ," +
                "value_type_id BIGINT, " +
                "CONSTRAINT fk_value_type_value_classes" +
                "  FOREIGN KEY (value_type_id)" +
                "  REFERENCES value_type (id)" +
                "  ON DELETE CASCADE" +
                "  ON UPDATE NO ACTION" +
                ");");
        
        // VALUES table
        update("CREATE CACHED TABLE value_values ("+
                "id BIGINT IDENTITY," +
                "value DOUBLE ," +
                "value_type_id INT ," +
                "molecules_id BIGINT ," +
//                "INDEX fk_values_value_type_idx (value_type_id ASC) ," +
//                "INDEX fk_values_molecules_idx (molecules_id ASC) ," +
                "CONSTRAINT fk_values_value_type" +
                "  FOREIGN KEY (value_type_id)" +
                "  REFERENCES value_type (id)" +
                "  ON DELETE NO ACTION" +
                "  ON UPDATE NO ACTION," +
                "CONSTRAINT fk_values_molecules" +
                "  FOREIGN KEY (molecules_id)" +
                "  REFERENCES molecules (id)" +
                "  ON DELETE CASCADE" +
                "  ON UPDATE NO ACTION" +
                ");");
        
        // DATASETS VALUES table
        update("CREATE CACHED TABLE datasets_values ("+
                "dataset_id BIGINT ," +
                "value_id BIGINT NOT NULL ," +
//                "INDEX fk_datasets_values_datasets_idx (dataset_id ASC) ," +
//                "INDEX fk_datasets_values_values_idx (value_id ASC) ," +
                "PRIMARY KEY (value_id, dataset_id) ," +
                "CONSTRAINT fk_datasets_values_datasets" +
                "  FOREIGN KEY (dataset_id)" +
                "  REFERENCES datasets (id)" +
                "  ON DELETE CASCADE" +
                "  ON UPDATE NO ACTION," +
                "CONSTRAINT fk_datasets_values_values" +
                "  FOREIGN KEY (value_id)" +
                "  REFERENCES value_values (id)" +
                "  ON DELETE CASCADE" +
                "  ON UPDATE NO ACTION" +
                ");");
        
    }

    
    
////////////////////////////////////////////////////////////////////////////////
// Public methods for queries and DB handling
////////////////////////////////////////////////////////////////////////////////
    
    
    /**
     * Checks if DB connection is active.
     * 
     * @return true if connection is active, false otherwise
     */
    public boolean CheckStatus() {
        try {
            return (!Conn.isClosed());
        } catch (Throwable e) {
            return false;
        }
    }
    
    
    
    //// Queries /////////////
    
    // Performs given query (SQL) and returns the ResultSet
    public ResultSet QueryRS(String SQL) throws DatabaseException {
        
        ResultSet rs = null;
        
        try {
            Statement st = Conn.createStatement();         
            rs = st.executeQuery(SQL); 
        } catch (SQLException e) {
            throw new DatabaseException("DB Access error: " + e.getMessage());
        }
        return rs;
    }
    
    
    // Performs given query (SQL) and returns the String for given field 
    public String QueryString(String SQL, String FieldName) throws DatabaseException {
        
        ResultSet rs = this.QueryRS(SQL);
        
        try {
            if (!rs.next())
                throw new Exception("Empty result set");
            return rs.getString(FieldName);
        } catch (Throwable e) {
            throw new DatabaseException("DB Access error: " + e.getMessage());
        }
    }


    // Performs given query (SQL) and returns the String array for given field 
    public String[] QueryStringArray(String SQL, String FieldName) throws DatabaseException {
        
        ResultSet rs = this.QueryRS(SQL);
        
        try {
            ArrayList<String> res = new ArrayList<String>();
            while (rs.next()) {
                res.add(rs.getString(FieldName));
            }
            if (res.isEmpty())
                throw new Exception("Empty result set");
            return res.toArray(new String[res.size()]);
        } catch (Throwable e) {
            throw new DatabaseException("DB Access error: " + e.getMessage());
        }
    }


    // Performs given query (SQL) and returns the Boolean for given field 
    public boolean QueryBoolean(String SQL, String FieldName) throws DatabaseException {
        
        ResultSet rs = this.QueryRS(SQL);
        
        try {
            if (!rs.next())
                throw new Exception("Empty result set");
            return rs.getBoolean(FieldName);
        } catch (Throwable e) {
            throw new DatabaseException("DB Access error: " + e.getMessage());
        }
    }
    
    
    // Performs given query (SQL) and returns the Long Integer for given field 
    public long QueryInteger(String SQL, String FieldName) throws DatabaseException {
        
        ResultSet rs = this.QueryRS(SQL);
        
        try {
            if (!rs.next())
                throw new Exception("Empty result set");
            return rs.getLong(FieldName);
        } catch (Throwable e) {
            throw new DatabaseException("DB Access error: " + e.getMessage());
        }
    }
    
    
    // Performs given query (SQL) and returns the Long Integer for given field 
    public double QueryDouble(String SQL, String FieldName) throws DatabaseException {
        
        ResultSet rs = this.QueryRS(SQL);
        
        try {
            if (!rs.next())
                throw new Exception("Empty result set");
            return rs.getDouble(FieldName);
        } catch (Throwable e) {
            throw new DatabaseException("DB Access error: " + e.getMessage());
        }
    }
    
    
    // Performs given query (SQL) and returns the Object for given field 
    public Object QueryObject(String SQL, String FieldName) throws DatabaseException {
        
        ResultSet rs = this.QueryRS(SQL);
        
        try {
            if (!rs.next())
                throw new Exception("Empty result set");
            return rs.getObject(FieldName);
        } catch (Throwable e) {
            throw new DatabaseException("DB Access error: " + e.getMessage());
        }
    }
    
    
    
    //// Execute ////////////
    
    // Performs given query (should be an INSERT) and returns the value (long)
    // of the auto-generated field given as IdFieldName
    public long ExecSQLWithAutogenerate(String SQL, String IdFieldName) throws DatabaseException {

        try {
            Statement st = Conn.createStatement();         
            int RetCode = st.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS); 
            if (RetCode == -1)
                throw new SQLException("Error on statement (" + SQL + ")");
            ResultSet rs = st.getGeneratedKeys();
            if (!rs.next())
                throw new SQLException("Empty result set for autogenerated fields");
            return rs.getLong(IdFieldName);
        } catch (SQLException e) {
            throw new DatabaseException("DB Access error: " + e.getMessage());
        }
    }    
    
    
    // Performs given query (INSERT/UPDATE) without a returning RS
    public void ExecSQL(String SQL) throws DatabaseException {

        try {
            Statement st = Conn.createStatement();         
            int RetCode = st.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS); 
            if (RetCode == -1)
                throw new SQLException("Error on statement (" + SQL + ")");
        } catch (SQLException e) {
            throw new DatabaseException("DB Access error: " + e.getMessage());
        }
    }    
    
    
    // Performs given query (INSERT/UPDATE) without a returning RS
    // passing parameters - needed to store Object clasess in DB
    // Uses prepared statement, in SQL parameter are given as '?'
    public void ExecSQLWithParam(String SQL, Object[] paramObjs) throws DatabaseException {

        try {
            PreparedStatement pst = Conn.prepareStatement(SQL);
            for (int i=0; i<paramObjs.length; i++)
                pst.setObject((i+1), paramObjs[i]);
            int RetCode = pst.executeUpdate();
            if (RetCode == -1)
                throw new SQLException("Error on statement (" + SQL + ")");
        } catch (SQLException e) {
            throw new DatabaseException("DB Access error: " + e.getMessage());
        }
    }    
    
    
    
////////////////////////////////////////////////////////////////////////////////
// Utils
////////////////////////////////////////////////////////////////////////////////
    
    // Fixes special chars for SQL statements
    public static String FixSpecialChars(String s) {
        
        // Single quotes replaced with "''"
        String ret = s.replaceAll("'", "''");
        
        return ret;
    }
    
    
    
////////////////////////////////////////////////////////////////////////////////
// Private methods for handling DB
////////////////////////////////////////////////////////////////////////////////
    
    
    // use for SQL commands CREATE, DROP, INSERT and UPDATE
    private synchronized void update(String expression) throws SQLException {

        Statement st = Conn.createStatement(); 

        int RetCode = st.executeUpdate(expression); 

        if (RetCode == -1)
            throw new SQLException("Error on statement: " + expression);

        st.close();
    }   
        
    
    // remove file/directory
    private static void delete(File file) throws IOException {
 
    	if(file.isDirectory()){
 
            //directory is empty, then delete it
            if(file.list().length==0) {
               
                file.delete();
                
            } else {

                //list all the directory contents
                String files[] = file.list();
                for (String temp : files) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }

                //check the directory again, if empty then delete it
                if(file.list().length==0)
                    file.delete();
                
            }
 
        } else {
            //if file, then delete it
            file.delete();
    	}
    }    
    
    
}
