package insilico.core.molecule;

import insilico.core.alerts.Alert;
import insilico.core.alerts.AlertList;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.exception.MatrixNotSupportedException;
import insilico.core.molecule.acf.ACFItemList;
import insilico.core.molecule.matrix.*;
import insilico.core.similarity.SimilarityDescriptors;
import org.openscience.cdk.RingSet;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Basic molecule object
 * It stores basic information for the molecule structure and all its related messages
 */
public class InsilicoMolecule implements Serializable, Cloneable {

    Logger logger = LoggerFactory.getLogger(InsilicoMolecule.class);


    private static final long serialVersionUID = 1L;

    private boolean isValid;
    private boolean explicitHydrogen;

    private String Name;
    private String CAS;
    private String SMILES;

    private InsilicoMoleculeMessages errors, warnings;

    private InsilicoMoleculeCache MoleculeCache = new InsilicoMoleculeCache();

    /**
     * Constructor for empty molecule
     */
    public InsilicoMolecule() {
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
    public void ClearCache(){
        MoleculeCache.ClearCache();
    }

    /**
     * Clear all cached information except the molecule's structure and its ring sets
     */
    public final void ClearCachePreserveStructure() {
        MoleculeCache.ClearCachePreserveStructure();
    }

    public Object Clone() throws CloneNotSupportedException {
        InsilicoMolecule mol = (InsilicoMolecule) super.clone();
        mol.errors = (InsilicoMoleculeMessages) this.errors.Clone();
        mol.warnings = (InsilicoMoleculeMessages) this.warnings.Clone();
        mol.ClearCache();
        return mol;
    }

    // Getters and setters for all cached data

    /**
     *
     * @return
     * @throws InvalidMoleculeException
     */
    public IAtomContainer GetStructure() throws InvalidMoleculeException {

        if((!isValid) || (SMILES.isEmpty())){
            String err = "Requested CDK AtomContainer for an invalid molecule (SMILES: " + SMILES + ")";
            logger.warn(err);
            throw new InvalidMoleculeException();
        }

        return MoleculeCache.GetStructure(SMILES, explicitHydrogen);
    }


    public RingSet GetSSSR() throws InvalidMoleculeException {

        if((!isValid) || (SMILES.isEmpty())){
            String err = "Requested CDK AtomContainer for an invalid molecule (SMILES: " + SMILES + ")";
            logger.warn(err);
            throw new InvalidMoleculeException();
        }
        return MoleculeCache.GetSSSR(SMILES, explicitHydrogen);

    }

    public RingSet GetAllRings() throws InvalidMoleculeException{

        if((!isValid) || (SMILES.isEmpty())){
            String err = "Requested CDK AtomContainer for an invalid molecule (SMILES: " + SMILES + ")";
            logger.warn(err);
            throw new InvalidMoleculeException();
        }
        return MoleculeCache.GetAllRings(SMILES, explicitHydrogen, warnings, errors);
    }

    public boolean HasSimilarityDescriptors() {
        return MoleculeCache.HasSimilarityDescriptors();
    }

    public SimilarityDescriptors GetSimilarityDescriptors() {return MoleculeCache.GetSimilarityDescriptors();}

    public void SetSimilarityDescriptors(SimilarityDescriptors similarityDescriptors){
        MoleculeCache.SetSimilarityDescriptors(similarityDescriptors);
    }

    public boolean HasACF(){
        return MoleculeCache.HasACF();
    }
    public ACFItemList  GetACF(){
        return MoleculeCache.GetACF();
    }

    public void SetACF(ACFItemList ACFItems){
        MoleculeCache.SetACF(ACFItems);
    }

    public AlertList GetAlerts(){return MoleculeCache.GetAlerts();}

    public void PurgeAlerts(){
        MoleculeCache.PurgeAlerts();
    }

    public void AddAlert(Alert alert){
        MoleculeCache.AddAlert(alert);
    }

    public void AddAlert(AlertList alert) {
        AlertList alertList = MoleculeCache.GetAlerts();
        for (Alert a: alert.getSAList())
            alertList.add(a);
    }

    private MoleculeMatrix GetMatrix(Class MatrixClass) throws GenericFailureException {

        return MoleculeCache.GetMatrix(MatrixClass, SMILES, explicitHydrogen);

    }

    /**
     * Get Adjaceny Matrix
     * @return
     * @throws GenericFailureException
     */
    public int[][] GetMatrixAdjacency() throws GenericFailureException{
        try {
            return MoleculeCache.GetMatrix(AdjacencyMatrix.class, SMILES, explicitHydrogen).getBidimensionalIntMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + AdjacencyMatrix.class.getSimpleName();
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    /**
     * Get augmented Bonds matrix
     * @return
     * @throws GenericFailureException
     */
    public double[][] GetMatrixBondAugmented() throws GenericFailureException{
        try {
            return MoleculeCache.GetMatrix(BondAugMatrix.class, SMILES, explicitHydrogen).getBidimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + BondAugMatrix.class.getSimpleName();
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    /**
     * Get Burder Matrix
     * @return
     * @throws GenericFailureException
     */
    public double[][] GetMatrixBurden() throws GenericFailureException{
        try {
            return MoleculeCache.GetMatrix(BurdenMatrix.class, SMILES, explicitHydrogen).getBidimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + BurdenMatrix.class.getSimpleName();
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    /**
     * Get augmented connection matrix
     * @return
     * @throws GenericFailureException
     */
    public double[][] GetMatrixConnectionAugmented() throws GenericFailureException{
        try {
            return MoleculeCache.GetMatrix(ConnectionAugMatrix.class, SMILES, explicitHydrogen).getBidimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + ConnectionAugMatrix.class.getSimpleName();
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    /**
     * Get Distance/Detour matrix
     * @return
     * @throws GenericFailureException
     */
    public double[][] GetMatrixDistanceDetour() throws GenericFailureException{
        try {
            return MoleculeCache.GetMatrix(DistanceDetourMatrix.class, SMILES, explicitHydrogen).getBidimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + DistanceDetourMatrix.class.getSimpleName();
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    /**
     * Get Edge Adjacency and Edge Degree matrices
     * @return
     * @throws GenericFailureException
     */
    public double[][][] GetMatrixEdgeAdjacency() throws GenericFailureException{
        try {
            return MoleculeCache.GetMatrix(EdgeAdjacencyMatrix.class, SMILES, explicitHydrogen).getThreedimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + EdgeAdjacencyMatrix.class.getSimpleName();
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    /**
     * Get Topological Distance Matrix
     * @return
     * @throws GenericFailureException
     */
    public int[][] GetMatrixTopologicalDistance() throws GenericFailureException{
        try {
            return MoleculeCache.GetMatrix(TopoDistanceMatrix.class, SMILES, explicitHydrogen).getBidimensionalIntMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + TopoDistanceMatrix.class.getSimpleName();
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    /**
     * Get topologica distance matrix with H-filled structure
     * @return
     * @throws GenericFailureException
     */
    public double[][] GetMatrixTopologicalDistanceHFilled() throws GenericFailureException{
        try {
            return MoleculeCache.GetMatrix(TopoDistanceMatrixHFilled.class, SMILES, explicitHydrogen).getBidimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + TopoDistanceMatrixHFilled.class.getSimpleName();
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    /**
     * Get Laplace Matrix
     * @return
     * @throws GenericFailureException
     */
    public int[][] GetMatrixLaplace() throws GenericFailureException{
        try {
            return MoleculeCache.GetMatrix(LaplaceMatrix.class, SMILES, explicitHydrogen).getBidimensionalIntMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + LaplaceMatrix.class.getSimpleName();
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    /**
     * Get Barysz Matrix
     * @return
     * @throws GenericFailureException
     */
    public double[][][] GetMatrixBarysz() throws GenericFailureException{
        try {
            return MoleculeCache.GetMatrix(BaryszMatrix.class, SMILES, explicitHydrogen).getThreedimensionalDoubleMatrix();
        } catch (MatrixNotSupportedException ex){
            String msg = "Unable to convert matrix of class " + BaryszMatrix.class.getSimpleName();
            logger.warn(msg);
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
        MoleculeCache.ClearCache();
    }


    /**
     * Sets the SMILES for the molecule together with its given CDK structure..
     *
     *
     * @param newSMILES SMILES for the molecule
     * @param structure CDK structure for the molecule
     */
    public void SetSMILESAndStructure(String newSMILES, IAtomContainer structure) {
        this.SMILES = newSMILES;
        MoleculeCache.ClearCache();

        MoleculeCache.SetStructure(structure);
        if(newSMILES.contains("H"))
            this.explicitHydrogen = true;
//        MoleculeCache.SetStructure(newSMILES);
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
