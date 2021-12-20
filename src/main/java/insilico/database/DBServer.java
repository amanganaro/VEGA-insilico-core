package insilico.database;

import insilico.core.exception.DatabaseException;
import org.hsqldb.Server;

import java.io.PrintWriter;

/**
 * Class for Molecule DB server.<p>
 * Implements methods to start the local HSQLDB as server.
 * The connection is based on HSQLDB libraries.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBServer extends Thread {
        
    private String DBFolder;
    private boolean Silent;
    private Server server;
    
    
    /**
     * Constructor - starts DB server.
     * 
//     * @param DBFolder full path of the HSQLDB database folder
//     * @throws DatabaseException
     */     
    public DBServer(String DatabaseBFolder, boolean SilentMode) {
        super();
        DBFolder = DatabaseBFolder;
        Silent = SilentMode;
    }
    
    
    @Override
    public void start() {
        
        super.start();
        
        // Check for drivers
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (Exception e) {
            System.out.println(this + ".setUp() error: " + e.getMessage());
        }

        // Builds the correct final dir for DB files
        if (DBFolder.charAt(DBFolder.length()-1) != System.getProperty("file.separator").charAt(0)) {
            DBFolder += System.getProperty("file.separator");
        }
        DBFolder += "dbdata";

        // Starts the server
        try {

            server = new Server();

            server.setSilent(Silent);                
            server.setPort(9001);
            server.setDatabaseName(0, "VEGA");
            server.setDatabasePath(0, "file:" + DBFolder);
            server.setLogWriter(null); 
            server.setErrWriter(null);
            server.setNoSystemExit(false);

            server.start();

        } catch (Throwable e) {
            System.out.println("*** Error: unable to start DB - " + e.getMessage());
        }            
    }


    public synchronized void ToggleSilent() {
        if (server == null)
            return;
        boolean SilentStatus = server.isSilent();
        if (SilentStatus) {
            server.setLogWriter(new PrintWriter(System.out)); 
            server.setErrWriter(new PrintWriter(System.out));
        } else {
            server.setLogWriter(null); 
            server.setErrWriter(null);
        }    
        server.setSilent(!SilentStatus);
        System.out.println("* Server silent mode: " + server.isSilent());
    }
    
    
    @Override
    public void interrupt() {
        server.stop();
        super.interrupt();
    }    
    
}
