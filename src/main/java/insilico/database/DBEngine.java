package insilico.database;

import insilico.core.exception.DatabaseException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.similarity.Similarity;
import insilico.core.similarity.SimilarityDescriptors;
import insilico.database.object.*;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class for retrieving lists of objects from DB
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBEngine {
 
    private DBConnector DBConn;
    

    public DBEngine(String DBFolder, short OpenType) throws DatabaseException {
        DBConn = new DBConnector(DBFolder, OpenType);
    }
    
    
    public DBConnector GetConnector() {
        return this.DBConn;
    }
    

    //// Molecules
    
    public InsilicoMolecule GetInsilicoMolecule(DBMolecule mol) {
        InsilicoMolecule m = null;
        
        try {
            String SMILES = null;//mol.GetSMILES().GetSMILES();
            m = SmilesMolecule.Convert(SMILES);
            m.SetId(mol.GetName());
            m.SetCAS(mol.GetCAS());
        } catch (Throwable e) { 
            m = null;
        }
        
        return m;
    }
    
    public int CountMolecules() throws DatabaseException {
        CheckStatus();
        
        ResultSet rs = DBConn.QueryRS("SELECT COUNT(*) FROM molecules");
        try {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DatabaseException("Unable to perform query.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
    }
    
    
    public ArrayList<DBMolecule> GetAllMolecules() throws DatabaseException {
        CheckStatus();
        
        ArrayList<DBMolecule> list = new ArrayList<DBMolecule>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM molecules ORDER BY id ASC");
        try {
            while (rs.next()) {
                long MolId = rs.getLong("id");
                DBMolecule curmol = new DBMolecule(DBConn, MolId);
                list.add(curmol);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    /**
     * Retrieves all molecule with at least one experimental record available
     * for all the given endpoints (passed as array of int containing the DB
     * id of the endpoints)
     * 
     * @param Endpoints
     * @return
     * @throws DatabaseException 
     */
    public ArrayList<DBMolecule> GetAllMoleculesWithExperimental(int[] Endpoints) 
            throws DatabaseException {
        CheckStatus();
        
        // Retrieves all experimentals for the given enpoints
        String WhereClause = "";
        for (int ep : Endpoints) {
            if (!WhereClause.isEmpty())
                WhereClause += " OR ";
            WhereClause += "endpoint_id='" + ep + "'";
        }
        ResultSet rs = DBConn.QueryRS("SELECT * FROM experimental WHERE " + WhereClause);
        
        // Retrieves unique molecules
        WhereClause = "";
        try {
            while (rs.next()) {
                if (!WhereClause.isEmpty())
                    WhereClause += " OR ";
                WhereClause += "experimental_id='" + rs.getInt("id") + "'";
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        
        ArrayList<DBMolecule> list = new ArrayList<DBMolecule>();
        rs = DBConn.QueryRS("SELECT DISTINCT(molecule_id) FROM exp_values WHERE " + WhereClause);
        try {
            while (rs.next()) {
                long MolId = rs.getLong("molecule_id");
                DBMolecule curmol = new DBMolecule(DBConn, MolId);
                list.add(curmol);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    public ArrayList<DBMolecule> GetMatchingMolecules(String CAS) throws DatabaseException {
        CheckStatus();
        
        ArrayList<DBMolecule> list = new ArrayList<DBMolecule>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM molecules WHERE cas='" + CAS + "' ORDER BY id ASC");
        try {
            while (rs.next()) {
                long MolId = rs.getLong("id");
                DBMolecule curmol = new DBMolecule(DBConn, MolId);
                list.add(curmol);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    public ArrayList<DBMolecule> GetMatchingMolecules(SimilarityDescriptors SimDesc) throws DatabaseException {
        CheckStatus();
        
        // Finds matching SMILES
        
        ArrayList<DBSmiles> listSmiles = new ArrayList<DBSmiles>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM smiles ORDER BY id ASC");
        Similarity SimEngine = new Similarity();
        
        try {
            while (rs.next()) {
                DBSmiles curSmiles = new DBSmiles(DBConn, rs.getLong("id"));
                SimilarityDescriptors TargetDesc = curSmiles.GetDescriptors();
                double S = SimEngine.Calculate(SimDesc, TargetDesc);
                if (S==1) {
                    listSmiles.add(curSmiles);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        
        // Checks if some SMILES lead to the same molecule (then removes it)
        
        ArrayList<DBMolecule> list = new ArrayList<DBMolecule>();
        for (int i=0; i<listSmiles.size(); i++) {
            DBMolecule curMol = listSmiles.get(i).GetMolecule();
            if (i==0) {
                list.add(curMol);
                continue;
            }
            boolean Found = false;
            for (int j=0; j<list.size(); j++) {
                if (list.get(j).GetId() == curMol.GetId()) {
                    Found = true;
                    break;
                }
            }
            if (!Found)
                list.add(curMol);
        }

        return list;
    }
    
    
    // Same as GetMatchingMolecules but performs exact structure check on similarity==1
    public ArrayList<DBMolecule> GetMatchingMoleculesExactCheck(SimilarityDescriptors SimDesc,
            IAtomContainer Mol) throws DatabaseException {

        ArrayList<DBMolecule> OrigList = GetMatchingMolecules(SimDesc);
        ArrayList<DBMolecule> list = new ArrayList<DBMolecule>();

        for (DBMolecule m : OrigList) {
            ArrayList<DBSmiles> listSmiles = null;//m.GetSMILESAll();
            for (int i=0; i<listSmiles.size(); i++) {
                InsilicoMolecule  curMol = SmilesMolecule.Convert(listSmiles.get(i).GetSMILES());
                try {
                    if (Similarity.CheckIsomorphism(Mol, curMol.GetStructure())) {
                        list.add(m);
                        break;
                    }
                } catch (InvalidMoleculeException ex) {
                    throw new DatabaseException();
                }
            }
        }
        
        return list;
    }
    
    
    
    //// Datasets
    
    public int CountDatasets() throws DatabaseException {
        CheckStatus();
        
        ResultSet rs = DBConn.QueryRS("SELECT COUNT(*) FROM datasets");
        try {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DatabaseException("Unable to perform query.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
    }

    
    public ArrayList<DBDataset> GetAllDatasets() throws DatabaseException {
        CheckStatus();
        
        ArrayList<DBDataset> list = new ArrayList<DBDataset>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM datasets ORDER BY id ASC");
        try {
            while (rs.next()) {
                long MolId = rs.getLong("id");
                DBDataset curDS = new DBDataset(DBConn, MolId);
                list.add(curDS);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    
    //// End-points
    
    public int CountEndpoints() throws DatabaseException {
        CheckStatus();
        
        ResultSet rs = DBConn.QueryRS("SELECT COUNT(*) FROM endpoints");
        try {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DatabaseException("Unable to perform query.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
    }

    
    public ArrayList<DBEndpoint> GetAllEndpoints() throws DatabaseException {
        CheckStatus();
        
        ArrayList<DBEndpoint> list = new ArrayList<DBEndpoint>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM endpoints ORDER BY id ASC");
        try {
            while (rs.next()) {
                long MolId = rs.getLong("id");
                DBEndpoint curEP = new DBEndpoint(DBConn, MolId);
                list.add(curEP);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    //// Experimentals
    
    public ArrayList<DBExperimental> GetAllExperimentals() throws DatabaseException {
        CheckStatus();
        
        ArrayList<DBExperimental> list = new ArrayList<DBExperimental>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM experimental ORDER BY id ASC");
        try {
            while (rs.next()) {
                long ExpId = rs.getLong("id");
                DBExperimental curEx = new DBExperimental(DBConn, ExpId);
                list.add(curEx);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    
////////////////////////////////////////////////////////////////////////////////    
    
    private void CheckStatus() throws DatabaseException {
        if (DBConn==null)
            throw new DatabaseException("Database connection not set");
    }
    
    public void Close() throws DatabaseException {
        if (DBConn != null)
            if (DBConn.CheckStatus())
                DBConn.Close();
    }
}
