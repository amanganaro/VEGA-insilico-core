package insilico.core.alerts;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;

import java.util.ArrayList;

/**
 * Interface for a block of Structural Alerts
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public interface iAlertBlock {

    public int getId();
    public String getName();
    public ArrayList<Alert> getAllAlerts();

    public ArrayList<Alert> Calculate(InsilicoMolecule mol) throws InvalidMoleculeException, GenericFailureException;

    public double[] getOverlapsPerc(InsilicoMolecule mol) throws InvalidMoleculeException, GenericFailureException;
}
