package insilico.core.model;

import insilico.core.alert.AlertList;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.DescriptorsEngine;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InitFailureException;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;

import java.io.Serializable;
import java.util.ArrayList;

public interface iInsilicoModel extends Serializable {

    public InsilicoModelInfo getInfo();

    public ArrayList<DescriptorBlock> GetRequiredDescriptorBlocks() throws InitFailureException;
    public ArrayList<Integer> GetRequiredAlertBlocks();
    public AlertList GetCalculatedAlert() throws CloneNotSupportedException;

    public InsilicoModelOutput Execute(InsilicoMolecule mol) throws GenericFailureException;
    public InsilicoModelOutput Execute(InsilicoMolecule mol, DescriptorsEngine DescEngine, boolean CalculateAlerts) throws GenericFailureException;

    public void Purge();

    public iTrainingSet GetTrainingSet();
    public double GetDescriptor(int Index) throws GenericFailureException ;
    public int GetResultsSize();
    public String[] GetResultsName();
    public String[] GetADItemsName();

    public int getDescriptorsSize();
    public String[] getDescriptorsNames();
    public void setSkipADandTSLoading(boolean SkipADandTSLoading);
    public void ProcessTrainingSet() throws Exception;


}
