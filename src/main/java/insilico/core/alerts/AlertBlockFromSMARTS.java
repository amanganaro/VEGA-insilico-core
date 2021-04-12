package insilico.core.alerts;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.molecule.InsilicoMolecule;

public abstract class AlertBlockFromSMARTS extends AlertBlock {

    // Matcher tool
//    protected CustomQueryMatcher Matcher;

    // Flag for initialization of SMARTS matchers
    protected boolean IsInitialized;



    public AlertBlockFromSMARTS(int BlockIndex, String Name) throws InitFailureException {
        super(BlockIndex, Name);
//        Matcher = null;
        IsInitialized = false;
    }

    /**
     * Method to be called in the constructor of the class
     * has to be OVERLOADED by inherited classes in order to build
     * the complete list of fragment for that class
     * @throws insilico.core.exception.InitFailureException
     */
    @Override
    protected abstract void BuildSAList() throws InitFailureException;


    protected abstract void InitSMARTS() throws InitFailureException;





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
            throw new InvalidMoleculeException(StringSelectorCore.getString("sa_molecule_invalid_marked"));
        CurMol = mol;

        // Init
        try {
//            Matcher = new CustomQueryMatcher(CurMol);
            if (!IsInitialized) {
                InitSMARTS();
                IsInitialized = true;
            }
        } catch (Exception e) {
            throw new GenericFailureException(String.format(StringSelectorCore.getString("sa_invalid_molecule_err"), e.getMessage()));
        }

        // Calls method for checking alerts
        return CalculateSAMatches();
    }
}
