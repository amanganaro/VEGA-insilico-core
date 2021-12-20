package insilico.database.objects;

import insilico.core.exception.DatabaseException;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.similarity.ScreeningFingerprint;
import insilico.core.similarity.Similarity;
import insilico.core.similarity.SimilarityDescriptors;
import insilico.core.similarity.SimilarityDescriptorsBuilder;
import insilico.database.DBConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DBAccessTools {

    
//// Counters //////////////////////////////////////////////////////////////////
    
    
    /**
     * Private method for all queries for counting elements in a table. All
     * queries must be in the form "SELECT COUNT(*) FROM table_name"
     * 
     * @param DBConn
     * @param Query
     * @return
     * @throws DatabaseException 
     */
    private static int CountQuery(DBConnector DBConn, String Query) throws DatabaseException {
        ResultSet rs = DBConn.QueryRS(Query);
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
    
    
    /**
     * Counts all molecules
     * 
     * @param DBConn
     * @return
     * @throws DatabaseException 
     */
    public static int CountMolecules(DBConnector DBConn) throws DatabaseException {
        return CountQuery(DBConn, "SELECT COUNT(*) FROM molecules");
    }
    
    
    /**
     * Count all value types
     * 
     * @param DBConn
     * @return
     * @throws DatabaseException 
     */
    public static int CountValueTypes(DBConnector DBConn) throws DatabaseException {
        return CountQuery(DBConn, "SELECT COUNT(*) FROM value_type");
    }
    

    /**
     * Count molecules with a data available for the given ValuteType
     * 
     * @param DBConn
     * @param ValueType
     * @return
     * @throws DatabaseException 
     */
    public static int CountMoleculesWithAvailableValue(DBConnector DBConn, 
            DBValueType ValueType) throws DatabaseException {
        return CountQuery(DBConn, "SELECT COUNT(DISTINCT molecules_id) FROM (SELECT * FROM value_values WHERE value_type_id='" + ValueType.GetId() + "')");
    }
    
    

//// Getters ///////////////////////////////////////////////////////////////////
    
    
    public static ArrayList<DBMolecule> GetAllMolecules(DBConnector DBConn) throws DatabaseException {
        
        ArrayList<DBMolecule> list = new ArrayList<>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM molecules ORDER BY id ASC");
        try {
            while (rs.next()) {
                long curId = rs.getLong("id");
                list.add(new DBMolecule(DBConn, curId));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    public static ArrayList<DBValueType> GetAllValueTypes(DBConnector DBConn) throws DatabaseException {
        
        ArrayList<DBValueType> list = new ArrayList<>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM value_type ORDER BY id ASC");
        try {
            while (rs.next()) {
                long curId = rs.getLong("id");
                list.add(new DBValueType(DBConn, curId));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    
//// Searching /////////////////////////////////////////////////////////////////    

    
    /**
     * Returns the matching (exactly equal) molecule if found. If no set of 
     * molecules is given as parameter, the target molecule is compared with all
     * available molecules in the database.
     * 
     * @param DBConn
     * @param Target
     * @param Molecules
     * @return 
     * @throws insilico.core.exception.DatabaseException 
     */
    public static DBMolecule SearchMatchingMolecule(DBConnector DBConn, InsilicoMolecule Target, 
            ArrayList<DBMolecule> Molecules) throws DatabaseException {
    
        // Calculates screening FP
        long SFP;
        try {
            SFP = ScreeningFingerprint.Calculate(Target.GetStructure());
        } catch (GenericFailureException|InvalidMoleculeException e) {
            throw new DatabaseException("unable to calculate screening FP");
        }
        
        // Create molecule list - only molecules with the same screening FP
        ArrayList<DBMolecule> MolList = new ArrayList<>();
        if (Molecules == null) {
            // Get full list from DB
            ResultSet rs = DBConn.QueryRS("SELECT * FROM molecules WHERE screeningfp= '" + SFP + "' ORDER BY id ASC");
            try {
                while (rs.next()) {
                    long curId = rs.getLong("id");
                    MolList.add(new DBMolecule(DBConn, curId));
                }
            } catch (SQLException e) {
                throw new DatabaseException("Unable to perform query: " + e.getMessage());
            }
        } else {
            // Clean up user provided list
            for (DBMolecule curMol : Molecules) 
                if (curMol.GetScreeningFP() == SFP)
                    MolList.add(curMol);
        }

        // Check list
        if (MolList.isEmpty())
            return null;
        
        // Calculates descriptors
        SimilarityDescriptorsBuilder Sim = new SimilarityDescriptorsBuilder();
        Similarity SimCalculator = new Similarity();
        SimilarityDescriptors Descriptors = Sim.Calculate(Target);
        
        // Calculates matching
        for (DBMolecule Mol : MolList) {
            InsilicoMolecule m = SmilesMolecule.Convert(Mol.GetSMILES());
            double sim;
            try {
                sim = SimCalculator.CalculateExactMatches(Descriptors, Mol.GetDescriptors(),
                        Target.GetStructure(), m.GetStructure());
            } catch (InvalidMoleculeException ex) {
                throw new DatabaseException();
            }
            if (sim == 1)
                return Mol;
        }
        
        return null;        

///////        
        
        // Calculate descriptors for target molecule
//        SimilarityDescriptorsBuilder Sim = new SimilarityDescriptorsBuilder();
//        Similarity SimCalculator = new Similarity();
//        SimilarityDescriptors Descriptors = Sim.Calculate(Target);
//        
//        int nAtoms = (int)Descriptors.Constitutional[9]; //nSk is the 9th descriptor
//        
//        // Create molecule list - only molecules with the same nAtoms
//        ArrayList<DBMolecule> MolList = new ArrayList<>();
//        if (Molecules == null) {
//            // Get full list from DB
//            ResultSet rs = DBConn.QueryRS("SELECT * FROM molecules WHERE atoms= '" + nAtoms + "' ORDER BY id ASC");
//            try {
//                while (rs.next()) {
//                    long curId = rs.getLong("id");
//                    MolList.add(new DBMolecule(DBConn, curId));
//                }
//            } catch (SQLException e) {
//                throw new DatabaseException("Unable to perform query: " + e.getMessage());
//            }
//        } else {
//            // Clean up user provided list
//            for (DBMolecule curMol : Molecules) 
//                if (curMol.GetAtoms() == nAtoms)
//                    MolList.add(curMol);
//        }
//        
//        // Calculates matching
//        for (DBMolecule Mol : MolList) {
//            InsilicoMolecule m = SmilesMolecule.Convert(Mol.GetSMILES());
//            double sim = SimCalculator.CalculateExactMatches(Descriptors, Mol.GetDescriptors(),
//                    Target.GetCDKMolecule(), m.GetCDKMolecule());
//            if (sim == 1)
//                return Mol;
//        }
//        
//        return null;
    }


    /**
     * 
     * @param DBConn
     * @param CAS
     * @return
     * @throws DatabaseException 
     */
    public static ArrayList<DBMolecule> SearchMoleculesByCAS(DBConnector DBConn, String CAS) 
            throws DatabaseException {
        
        ArrayList<DBMolecule> list = new ArrayList<>();
        ResultSet rs = DBConn.QueryRS("SELECT * FROM molecules WHERE cas='" + CAS + "' ORDER BY id ASC");
        try {
            while (rs.next()) {
                long curId = rs.getLong("id");
                list.add(new DBMolecule(DBConn, curId));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
        
    }
    
    
    public static ArrayList<DBMolecule> SearchSimilarMolecules(DBConnector DBConn, InsilicoMolecule Target, 
            ArrayList<DBMolecule> Molecules, int SimNumber) throws DatabaseException {
        
        // Check molecules parameter
        if (Molecules == null) 
            Molecules = GetAllMolecules(DBConn);
        
        // Calculates descriptors
        SimilarityDescriptorsBuilder Sim = new SimilarityDescriptorsBuilder();
        Similarity SimCalculator = new Similarity();
        SimilarityDescriptors Descriptors = Sim.Calculate(Target);
        
        // Calculates similarity
        double[] SimValues = new double[Molecules.size()];
        int idx = 0;
        for (DBMolecule Mol : Molecules) {
            SimValues[idx] = SimCalculator.Calculate(Descriptors, Mol.GetDescriptors());
            idx++;
        }
        
        // Finds similar molecules
        int SimMolecules[] = new int[SimNumber];
        for (int i=0; i<SimNumber; i++) {
            
            SimMolecules[i] = -1;
            
            for (int j=0; j<SimValues.length; j++) {
                if ((SimMolecules[i] == -1) && (SimValues[j] != -1)) {
                    SimMolecules[i] = j;
                    continue;
                }
                if (SimValues[j] > SimValues[SimMolecules[i]])
                    SimMolecules[i] = j;
            }
            
            SimValues[SimMolecules[i]] = -1;
        }
        
        ArrayList<DBMolecule> Res = new ArrayList<>();
        for (int i=0; i<SimNumber; i++) 
            Res.add(Molecules.get(SimMolecules[i]));
        
        return Res;
    }
    
    
////////////////////////////////////////////////////////////////////////////////
    
    
    public static long InsertMolecule(DBConnector DBConn, InsilicoMolecule mol) throws DatabaseException {
        
        DBMolecule dbmol = new DBMolecule(DBConn);
        dbmol.CreateIntoDB();
        dbmol.SetCAS(mol.GetCAS());
        dbmol.SetName(mol.GetId());
        dbmol.SetSMILES(mol.GetSMILES());
        
        SimilarityDescriptorsBuilder SimDescEngine = new SimilarityDescriptorsBuilder();
        SimilarityDescriptors desc = SimDescEngine.Calculate(mol);
        dbmol.SetDescriptors(desc);
        
        return dbmol.GetId();
        
    }

    

    
    
    public static ArrayList<DBMolecule> GetMoleculesWithAvailableValues(DBConnector DBConn, 
            ArrayList<DBValueType> ValueTypes) throws DatabaseException {
        
        String ValueTypeClause = "";
        if (ValueTypes != null) 
            if (ValueTypes.size() > 0) {
                for (DBValueType curValType : ValueTypes) {
                    if (ValueTypeClause.length() == 0)
                        ValueTypeClause += " WHERE ";
                    else
                        ValueTypeClause += " OR ";
                    ValueTypeClause += "value_type_id='" + curValType.GetId() + "'";
                }        
            }
        
        ArrayList<DBMolecule> list = new ArrayList<DBMolecule>();
        
        String CurQuery = "SELECT DISTINCT molecules_id FROM ( SELECT * FROM value_values";
        if (ValueTypeClause.length()>0)
            CurQuery += ValueTypeClause;
        CurQuery += ") ORDER BY molecules_id ASC";
        ResultSet rs = DBConn.QueryRS(CurQuery);
        try {
            while (rs.next()) {
                long MolId = rs.getLong("molecules_id");
                DBMolecule curmol = new DBMolecule(DBConn, MolId);
                list.add(curmol);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        return list;
    }
    
    
    public static ArrayList<DBValue> GetValues(DBConnector DBConn, DBMolecule mol, 
            ArrayList<DBValueType> ValueTypes) throws DatabaseException {
        
        String ValueTypeClause = "";
        if (ValueTypes != null) 
            if (ValueTypes.size() > 0) {
                for (DBValueType curValType : ValueTypes) {
                    if (ValueTypeClause.length() > 0)
                        ValueTypeClause += " OR ";
                    ValueTypeClause += "value_type_id='" + curValType.GetId() + "'";
                }        
            }

        ArrayList<DBValue> list = new ArrayList<DBValue>();
        String CurQuery = "SELECT * FROM value_values WHERE molecules_id='" + mol.GetId() + "'";
        if (ValueTypeClause.length()>0)
            CurQuery += " AND (" + ValueTypeClause + ")";
        CurQuery += " ORDER BY id ASC";
        ResultSet rs = DBConn.QueryRS(CurQuery);
        try {
            while (rs.next()) {
                long ValId = rs.getLong("id");
                DBValue curval = new DBValue(DBConn, ValId);
                list.add(curval);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Unable to perform query: " + e.getMessage());
        }
        
        return list;
    }
    
}
