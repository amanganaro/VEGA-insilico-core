package insilico.core.alerts;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public abstract class AlertBlock implements iAlertBlock {

    // Index (unique id) of the block
    protected int BlockIndex;

    // Name of the block
    private final String Name;

    // Alerts in the block
    protected AlertList Alerts;

    // Current molecule to be checked
    protected InsilicoMolecule CurMol;

    public AlertBlock(int BlockIndex, String Name) throws InitFailureException {
        this.BlockIndex = BlockIndex;
        this.Name = Name;
        Alerts = new AlertList();
        BuildSAList();
    }

    /**
     * Method to be called in the constructor of the class
     * has to be OVERLOADED by inherited classes in order to build
     * the complete list of fragment for that class
     * @throws insilico.core.exception.InitFailureException
     */
    protected abstract void BuildSAList() throws InitFailureException;

    /**
     * Main method to be called for checking structural alerts
     * return true if checking has been performed, false if errors has
     * been encountered.
     *
     * @param mol
     * @return
     * @throws insilico.core.exception.InvalidMoleculeException
     * @throws insilico.core.exception.GenericFailureException
     */
    @Override
    public AlertList Calculate(InsilicoMolecule mol) throws InvalidMoleculeException, GenericFailureException {

        if (!mol.IsValid())
            throw new InvalidMoleculeException("Given molecule is not marked as valid");
        CurMol = mol;

        // Calls method for checking alerts
        return CalculateSAMatches();
    }


    /**
     * To be implemented only in needed blocks (for now, just for ToxRead usage)
     * Return the list of overlapping % for each alert; the value is the % of
     * atoms of the target mol matching with the alerts
     *
     * @param mol
     * @return
     * @throws InvalidMoleculeException
     * @throws GenericFailureException
     */
    public double[] getOverlapsPerc(InsilicoMolecule mol) throws InvalidMoleculeException, GenericFailureException {
        throw new GenericFailureException("not implemented in current alert block");
    }

    /**
     * Internal method to be overridden, inside should be put all code
     * for checking fragments
     *
     * @return
     * @throws insilico.core.exception.GenericFailureException
     */
    protected abstract AlertList CalculateSAMatches() throws GenericFailureException;


    /**
     * @return the alert list
     */
    @Override
    public AlertList getAlerts() {
        return Alerts;
    }

    /**
     * @return the Name
     */
    @Override
    public String getName() {
        return Name;
    }

    /**
     *
     * @return the block index (id)
     */
    @Override
    public int getId() {
        return BlockIndex;
    }


}
