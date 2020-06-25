package insilico.core.molecule;

import insilico.core.alerts.Alert;
import insilico.core.alerts.AlertList;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.exception.MatrixNotSupportedException;
import insilico.core.exception.MoleculeConversionException;
import insilico.core.molecule.acf.ACFItemList;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.matrix.*;
import insilico.core.molecule.tools.Manipulator;
import insilico.core.similarity.SimilarityDescriptors;
import insilico.core.tools.logger.InsilicoLogger;
import org.openscience.cdk.RingSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.ringsearch.AllRingsFinder;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Basic molecule object
 * It stores basic information for the molecule structure and all its related messages
 */
public class InsilicoMolecule implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private boolean isValid;
    private boolean explicitHydrogen;

    private String Name;
    private String CAS;
    private String SMILES;

    private InsilicoMoleculeMessages errors, warnings;

    transient private IAtomContainer structure;
    transient private RingSet SSSR;
    transient private RingSet allRings;
    transient private ArrayList<MoleculeMatrix> matrices;
    transient private AlertList structuralAlerts;
    transient private SimilarityDescriptors similarityDescriptors;
    transient private ACFItemList ACF;

    /**
     * Constructor for empty molecule
     */
    public InsilicoMolecule(){
        isValid = false;
        explicitHydrogen = false;
        Name = "";
        CAS = "";
        SMILES = "";
        errors = new InsilicoMoleculeMessages();
        warnings = new InsilicoMoleculeMessages();
        ClearCache();
    }

    /**
     * Clear all cached information
     */
    public final void ClearCache(){
        ClearCachePreserveStructure();
        structure = null;
        SSSR = null;
        allRings = null;
    }

    /**
     * Clear all cached information except the molecule's structure and its ring sets
     */
    public final void ClearCachePreserveStructure(){
        matrices = new ArrayList<>();
        structuralAlerts = new AlertList();
        similarityDescriptors = null;
        ACF = null;
    }

    public Object Clone() throws CloneNotSupportedException {
        InsilicoMolecule mol = (InsilicoMolecule) super.clone();
        mol.errors = (InsilicoMoleculeMessages) this.errors.Clone();
        mol.warnings = (InsilicoMoleculeMessages) this.warnings.Clone();
        mol.ClearCache();
        return mol;
    }

    // Getters and setters for all cached data

    public IAtomContainer GetStructure() throws InvalidMoleculeException {

        if((isValid == false) || (SMILES.isEmpty())){
            String err = "Requested CDK AtomContainer for an invalid molecule (SMILES: " + SMILES + ")";
            InsilicoLogger.getLogger().warn(err);
            throw new InvalidMoleculeException();
        }

        if (structure != null){
            return structure;
        }

        try {
            structure = SmilesMolecule.CreateCDKMolecule(SMILES, new InsilicoMoleculeMessages());
        } catch (MoleculeConversionException ex){
            structure = null;
            String err = "Failed SMILES conversion while requesting CDK Molecule for SMILES: "+ SMILES;
            InsilicoLogger.getLogger().warn(err);
            throw new InvalidMoleculeException(err);
        }

        if (explicitHydrogen)
            try {
                structure = Manipulator.AddHydrogens(structure);
            } catch (GenericFailureException ex){
                structure = null;
                String err = "Failed normalization while requesting CDK Molecule for SMILES: "+ this.SMILES;
                InsilicoLogger.getLogger().warn(err);
                throw new InvalidMoleculeException(err);
            }

        return structure;
    }

    public RingSet GetSSSR() throws InvalidMoleculeException {
        if (SSSR == null){
            Cycles cycles = Cycles.sssr(GetStructure());
            SSSR = (RingSet) cycles.toRingSet();
//            DEPRECATED
//            SSSRFinder rf = new SSSRFinder(GetStructure());
//            SSSR = (RingSet) rf.findSSSR();
        }
        return SSSR;
    }

    public RingSet GetAllRings() throws InvalidMoleculeException{
        if (allRings == null){
            AllRingsFinder ringsFinder = new AllRingsFinder();
            ringsFinder.setTimeout(5000);
            try {
                allRings = (RingSet) ringsFinder.findAllRings(GetStructure());
            } catch (CDKException ex){
                ringsFinder = new AllRingsFinder().setTimeout(15000);
                try {
                    allRings = (RingSet) ringsFinder.findAllRings(GetStructure(), 20);
                } catch (CDKException ex2) {
                    this.isValid = false;
                    this.AddError("Unable to perceive all rings");
                    InsilicoLogger.getLogger().warn("Unable to find all rings for molecule " + GetSMILES());
                    throw new InvalidMoleculeException("unable to find all rings");
                }

                this.AddWarning("All rings perceived only partially due to the complexity of the molecule");
                InsilicoLogger.getLogger().warn("unable to find all rings for molecule " + GetSMILES() + " - rings perceived only partially");
            }
        }
        return allRings;
    }

    public boolean HasSimilarityDescriptors() {
        return !(similarityDescriptors == null);
    }

    public SimilarityDescriptors GetSimilarityDescriptors() {return similarityDescriptors;}
    public void SetSimilarityDescriptors(SimilarityDescriptors similarityDescriptors){
        this.similarityDescriptors = similarityDescriptors;
    }

    public boolean HasACF(){return !(ACF == null);}
    public ACFItemList  GetACF(){return ACF;}
    public void SetACF(ACFItemList ACFItems){
        this.ACF = ACFItems;
    }

    public AlertList GetAlerts(){return structuralAlerts;}
    public void PurgeAlerts(){structuralAlerts = new AlertList();}
    public void AddAlert(Alert alert){structuralAlerts.add(alert);}
    public void AddAlert(AlertList alert){
        for (Alert a: alert.getSAList())
            structuralAlerts.add(a);
    }

    private MoleculeMatrix GetMatrix(Class MatrixClass) throws GenericFailureException {

        // Check if already cached
        for (MoleculeMatrix matrix: matrices)
            if (matrix.getMatrixClass() == MatrixClass) return matrix;

        MoleculeMatrix matrix = null;
        try {

            if (MatrixClass == AdjacencyMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass, AdjacencyMatrix.getMatrix(this.GetStructure()));
            if (MatrixClass == BondAugMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass, BondAugMatrix.getMatrix(this.GetStructure()));
            if (MatrixClass == BurdenMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass, BurdenMatrix.getMatrix(this.GetStructure()));
            if (MatrixClass == ConnectionAugMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass, ConnectionAugMatrix.getMatrix(this.GetStructure()));
            if (MatrixClass == DistanceDetourMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass,DistanceDetourMatrix.getMatrix(this.GetStructure()));
            if (MatrixClass == EdgeAdjacencyMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass,EdgeAdjacencyMatrix.getMatrix(this.GetStructure()));
            if (MatrixClass == TopoDistanceMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass, TopoDistanceMatrix.getMatrix(this.GetStructure()));
            if (MatrixClass == TopoDistanceMatrixHFilled.class)
                matrix = new MoleculeMatrix(MatrixClass, TopoDistanceMatrixHFilled.getMatrix(this.GetStructure()));
            if (MatrixClass == LaplaceMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass,LaplaceMatrix.getMatrix(this.GetStructure()));
            if (MatrixClass == BaryszMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass,BaryszMatrix.getMatrix(this.GetStructure()));

        } catch (InvalidMoleculeException ex){
            String msg = "Unable to build matrix " + MatrixClass.getName() + ", invalid molecule structure for " + this.GetSMILES();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }

        if (matrix != null) {
            this.matrices.add(matrix);
            return matrix;
        } else {
            String msg = "Unable to build matrix " + MatrixClass.getName() + " for molecule " + this.GetSMILES();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    public int[][] GetMatrixAdjacency() throws GenericFailureException{
        try {
            return this.GetMatrix(AdjacencyMatrix.class).getBidimensionalIntMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + AdjacencyMatrix.class.getSimpleName();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    public double[][] GetMatrixBondAugmented() throws GenericFailureException{
        try {
            return this.GetMatrix(BondAugMatrix.class).getBidimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + BondAugMatrix.class.getSimpleName();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    public double[][] GetMatrixBurden() throws GenericFailureException{
        try {
            return this.GetMatrix(BurdenMatrix.class).getBidimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + BurdenMatrix.class.getSimpleName();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    public double[][] GetMatrixConnectionAugmented() throws GenericFailureException{
        try {
            return this.GetMatrix(ConnectionAugMatrix.class).getBidimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + ConnectionAugMatrix.class.getSimpleName();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    public double[][] GetMatrixDistanceDetour() throws GenericFailureException{
        try {
            return this.GetMatrix(DistanceDetourMatrix.class).getBidimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + DistanceDetourMatrix.class.getSimpleName();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    public double[][][] GetMatrixEdgeAdjacency() throws GenericFailureException{
        try {
            return this.GetMatrix(EdgeAdjacencyMatrix.class).getThreedimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + EdgeAdjacencyMatrix.class.getSimpleName();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    public int[][] GetMatrixTopologicalDistance() throws GenericFailureException{
        try {
            return this.GetMatrix(TopoDistanceMatrix.class).getBidimensionalIntMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + TopoDistanceMatrix.class.getSimpleName();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    public double[][] GetMatrixTopologicalDistanceHFilled() throws GenericFailureException{
        try {
            return this.GetMatrix(TopoDistanceMatrixHFilled.class).getBidimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + TopoDistanceMatrixHFilled.class.getSimpleName();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    public int[][] GetMatrixLaplace() throws GenericFailureException{
        try {
            return this.GetMatrix(LaplaceMatrix.class).getBidimensionalIntMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + LaplaceMatrix.class.getSimpleName();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    public double[][][] GetMatrixBarysz() throws GenericFailureException{
        try {
            return this.GetMatrix(BaryszMatrix.class).getThreedimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + BaryszMatrix.class.getSimpleName();
            InsilicoLogger.getLogger().warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    // GETTERS AND SETTERS FOR INSILICO MOLECULE PROPERTIES
    /**
     * Marks the molecule as valid (i.e. it has been correctly parsed and
     * initialized)
     */
    public void MarkAsValid() {
        isValid = true;
    }


    /**
     * Marks the molecule as invalid (i.e. it has not been correctly parsed and
     * initialized)
     */
    public void MarkAsInvalid() {
        isValid = false;
    }


    /**
     * Returns the molecule Id or an empty string if it has not been set
     * @return Id of the molecule
     */
    public String GetId() {
        return Name;
    }


    /**
     * Returns the molecule CAS or an empty string if it has not been set
     * @return CAS of the molecule
     */
    public String GetCAS() {
        return CAS;
    }


    /**
     * Returns the molecule SMILES or an empty string if it has not been set
     * @return SMILES of the molecule
     */
    public String GetSMILES() {
        return SMILES;
    }


    /**
     * Sets the Id for the molecule
     * @param newId Id for the molecule
     */
    public void SetId(String newId) {
        this.Name = newId;
    }


    /**
     * Sets the CAS for the molecule
     * @param newCAS CAS for the molecule
     */
    public void SetCAS(String newCAS) {
        this.CAS = newCAS;
    }


    /**
     * Sets the SMILES for the molecule.
     * Cached data are discarded as not updated anymore.
     *
     * @param newSMILES SMILES for the molecule
     */
    public void SetSMILES(String newSMILES) {
        this.SMILES = newSMILES;
        this.ClearCache();
    }


    /**
     * Sets the SMILES for the molecule together with its given CDK structure..
     * Cached data are discarded as not updated anymore.
     *
     * @param newSMILES SMILES for the molecule
     * @param structure CDK structure for the molecule
     */
    public void SetSMILESAndStructure(String newSMILES, IAtomContainer structure) {
        this.SMILES = newSMILES;
        this.ClearCache();
        this.structure = structure;
    }


    /**
     * Returns if the molecule object has a valid structure or not.<p>
     * A false value means that the molecule has not been initialized or
     * that some conversion from a file format has failed to produce a
     * valid structure.
     *
     * @return true if the molecule has a valid structure, false otherwise
     */
    public boolean IsValid() {
        return isValid;
    }


    /**
     * Returns the error messages for this molecule.
     * @return the error messages for this molecule
     */
    public InsilicoMoleculeMessages GetErrors() {
        return errors;
    }


    /**
     * Returns the warning messages for this molecule.
     * @return the warning messages for this molecule
     */
    public InsilicoMoleculeMessages GetWarnings() {
        return warnings;
    }


    /**
     * Adds a new error message
     * @param message error as String
     */
    public void AddError(String message) {
        errors.AddMessage(message);
    }


    /**
     * Adds a new error warning
     * @param message warning as String
     */
    public void AddWarning(String message) {
        warnings.AddMessage(message);
    }


    /**
     * Returns the flag for explicit hydrogen CDK structure generation
     * @return the ExplicitHydrogen flag
     */
    public boolean IsExplicitHydrogen() {
        return explicitHydrogen;
    }


    /**
     * Sets the flag for explicit hydrogen CDK structure generation.
     * Cached data are discarded as not updated anymore.
     *
     * @param ExplicitHydrogen the ExplicitHydrogen flag to set
     */
    public void SetExplicitHydrogen(boolean ExplicitHydrogen) {
        this.explicitHydrogen = ExplicitHydrogen;
        this.ClearCache();
    }






}
