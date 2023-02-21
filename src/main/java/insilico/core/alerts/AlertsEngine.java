package insilico.core.alerts;

import insilico.core.alerts.builders.*;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.AlertBlockNotFoundException;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelectorCore;
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
        throw new AlertBlockNotFoundException(String.format(StringSelectorCore.getString("sa_alert_not_found"),AlertBlockId));
    }

    public boolean hasAlertBlock(int AlertBlockId) {
        for (iAlertBlock a : Alerts)
            if (a.getId() == AlertBlockId)
                return true;
        return false;
    }

    public void AddAlertsBlock(ArrayList<Integer> AlertBlockIds) throws InitFailureException {
        for (Integer id: AlertBlockIds) {
            AddAlertBlock(id);
        }
    }

    public void AddAlertBlock(int AlertBlockId) throws InitFailureException {

        if (!this.hasAlertBlock(AlertBlockId)) {
            switch (AlertBlockId) {

                case InsilicoConstants.SA_BLOCK_MUTAGEN_BENIGNI_BOSSA:
                    Alerts.add(new SABenigniBossa());
                    break;
                case InsilicoConstants.SA_BLOCK_MUTAGEN_BENIGNI_BOSSA_ADDITIONAL:
                    Alerts.add(new SABenigniBossaAdditional());
                    break;

//                case InsilicoConstants.SA_BLOCK_MUTAGEN_CRS4:
//                    Alerts.add(new SAMutagenCRS4());
//                    break;
//                case InsilicoConstants.SA_BLOCK_MUTAGEN_IRFMN:
//                    Alerts.add(new SAMutagenIRFMN());
//                    break;
//                case InsilicoConstants.SA_BLOCK_BCF_IRFMN:
//                    Alerts.add(new SABCFIRFMN());
//                    break;
                case InsilicoConstants.SA_BLOCK_MOA_IRFMN:
                    Alerts.add(new SAMoaIRFMN());
                    break;
                case InsilicoConstants.SA_BLOCK_LOGP_MEYLAN:
                    Alerts.add(new SAMeylanLogPFragments());
                    break;
                case InsilicoConstants.SA_BLOCK_LOGP_MEYLAN_ADDITIONAL:
                    Alerts.add(new SAMeylanLogPAdditionalFragments());
                    break;
                case InsilicoConstants.SA_BLOCK_LOGP_MEYLAN_CORRECTION:
                    Alerts.add(new SAMeylanLogPCorrectionFragments());
                    break;
                case InsilicoConstants.SA_BLOCK_BCF_CAESAR:
                    Alerts.add(new SABCFCaesar());
                    break;
                case InsilicoConstants.SA_BLOCK_FISH_IRFMN:
                    Alerts.add(new SAFishIRFMN());
                    break;
                case InsilicoConstants.SA_BLOCK_READY_BIO_IRFMN:
                    Alerts.add(new SAReadyBioIRFMN());
                    break;
                case InsilicoConstants.SA_BLOCK_PERSISTENCE_SEDIMENT_IRFMN:
                    Alerts.add(new SAPersistenceSediment());
                    break;
                case InsilicoConstants.SA_BLOCK_PERSISTENCE_WATER_IRFMN:
                    Alerts.add(new SAPersistenceWater());
                    break;
                case InsilicoConstants.SA_BLOCK_SKIN_SENS_NCSTOX:
                    Alerts.add(new SASkinNcstox());
                    break;
                case InsilicoConstants.SA_BLOCK_PERSISTENCE_SOIL_IRFMN:
                    Alerts.add(new SAPersistenceSoil());
                    break;
                case InsilicoConstants.SA_BLOCK_CARC_ANTARES:
                    Alerts.add(new SACarcinogenicityAntares());
                    break;
                case InsilicoConstants.SA_BLOCK_CARC_ISSCANCGX:
                    Alerts.add(new SACarcinogenicityIsscanCgx());
                    break;
                case InsilicoConstants.SA_BLOCK_ESTROGEN_BIND_CERAPP:
                    Alerts.add(new SAEstrogenBindCerapp());
                    break;
                case InsilicoConstants.SA_BLOCK_HEPATOTOXICITY:
                    Alerts.add(new SAHepatotoxicity());
                    break;
                case InsilicoConstants.SA_BLOCK_MICRONUCLEUS_INVITRO_MODEL:
                    Alerts.add(new SAMicroNucleusModel());
                    break;
                case InsilicoConstants.SA_BLOCK_MICRONUCLEUS_INVIVO:
                    Alerts.add(new SAMicronucleusInVivo());
                    break;
                case InsilicoConstants.SA_BLOCK_MUTAGEN_SARPY_18K:
                    Alerts.add(new SAMutagenSarpy18K());
                    break;
                case InsilicoConstants.SA_BLOCK_MUTAGEN_SARPY:
                    Alerts.add(new SAMutagenSarpy());
                    break;
                case InsilicoConstants.SA_BLOCK_ANDROGEN_BIND_COMPARA_SARPY:
                    Alerts.add(new SAAndrogenBindComparaIRFMN());
                    break;
//                case InsilicoConstants.SA_BLOCK_ALGAE_COMBASE:
//                    Alerts.add(new SACombaseAlgae());
//                    break;
//                case InsilicoConstants.SA_BLOCK_MICROBES_COMBASE:
//                    Alerts.add(new SACombaseMicrobes());
//                    break;
//                case InsilicoConstants.SA_BLOCK_DAPHNIA_COMBASE:
//                    Alerts.add(new SACombaseDaphnia());
//                    break;
//                case InsilicoConstants.SA_BLOCK_FISH_COMBASE:
//                    Alerts.add(new SACombaseFish());
//                    break;
//                case InsilicoConstants.SA_BLOCK_REPROTOX_VERMEER:
//                    Alerts.add(new SAReprotoxVermeer());
//                    break;
//                case InsilicoConstants.SA_BLOCK_SKIN_SENS_VERMEER:
//                    Alerts.add(new SASkinVermeer());
//                    break;
//                case InsilicoConstants.SA_BLOCK_NEPHROTOX_VERMEER:
//                    Alerts.add(new SANephroVermeer());
//                    break;
                case InsilicoConstants.SA_BLOCK_SKIN_IRR_CONCERT:
                    Alerts.add(new SASkinConcert());
                    break;
                case InsilicoConstants.SA_BLOCK_DEVTOX_CONCERT:
                    Alerts.add(new SADevtoxConcert());
                    break;
                case InsilicoConstants.SA_BLOCK_SKIN_SENS_CONCERT:
                    Alerts.add(new SASkinSensitizationConcert());
                    break;
                case InsilicoConstants.SA_BLOCK_SKIN_IRRITATION_CONCERT_B3:
                    Alerts.add(new SASkinIrritationConcert());
                    break;
                case InsilicoConstants.SA_BLOCK_EYE_IRRITATION_CONCERT:
                    Alerts.add(new SAEyeIrritationConcert());
                    break;
                default:
                    throw new InitFailureException(String.format(StringSelectorCore.getString("sa_alert_not_available"), AlertBlockId));
            }
        }

    }


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
