package insilico.core.alerts;

import insilico.core.exception.AlertBlockNotFoundException;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;

import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class AlertsEngine {

    private final ArrayList<iAlertBlock> Alerts;


    public AlertsEngine() {
        Alerts = new ArrayList<>();
    }


    public iAlertBlock GetAlertBlock(int AlertBlockId) throws AlertBlockNotFoundException {
        for (iAlertBlock a : Alerts)
            if (a.getId() == AlertBlockId)
                return a;
        throw new AlertBlockNotFoundException("Alert block with id " + AlertBlockId + " not found");
    }

    public boolean hasAlertBlock(int AlertBlockId) {
        for (iAlertBlock a : Alerts)
            if (a.getId() == AlertBlockId)
                return true;
        return false;
    }

    public void AddAlertsBlock(ArrayList<Integer> AlertBlockIds) throws InitFailureException {
        for (Integer id: AlertBlockIds) {
//            AddAlertBlock(id);
        }
    }

    // TODO: 12/06/2020 AddAlertBlock + alerts/builder(s)


    public AlertList CalculateAlerts(InsilicoMolecule Mol) throws InvalidMoleculeException, GenericFailureException {

        AlertList CalculatedAlerts = new AlertList();

        if (Mol == null)
            throw new InvalidMoleculeException();
        if (!Mol.IsValid())
            throw new InvalidMoleculeException();

        for (iAlertBlock ab : Alerts) {
            AlertList CurAlerts = ab.Calculate(Mol);
            for (Alert a : CurAlerts.getSAList())
                CalculatedAlerts.add(a);
        }
        return CalculatedAlerts;

    }


}
