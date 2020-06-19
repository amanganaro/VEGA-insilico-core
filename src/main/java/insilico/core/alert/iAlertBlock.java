package insilico.core.alert;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public interface iAlertBlock {

    public int getId();
    public String getName();
    public AlertList getAlerts();

    public AlertList Calculate(InsilicoMolecule mol) throws InvalidMoleculeException, GenericFailureException;

    public double[] getOverlapsPerc(InsilicoMolecule mol) throws InvalidMoleculeException, GenericFailureException;
}
