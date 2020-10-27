package insilico.core.model.trainingset;

import insilico.core.exception.GenericFailureException;
import insilico.core.molecule.acf.ACFItemList;
import insilico.core.similarity.SimilarityDescriptors;

/**
 * Interface for TrainingSet objects.
 *
 * @author Alberto Manganaro <a.manganaro@kode-solutions.net>
 */
public interface iTrainingSet {

    /**
     * @return the number of molecules in the whole set
     */
    public int getMoleculesSize();

    /**
     * @return the number of molecules in the training set
     */
    public int getMoleculesTrainSize();

    /**
     * @return the number of molecules in the test set
     */
    public int getMoleculesTestSize();

    public int getId(int Index) throws GenericFailureException;

    public String getCAS(int Index) throws GenericFailureException;

    public String getSMILES(int Index) throws GenericFailureException;

    public String getUnitConversion();

    public boolean hasUnitConversion();

    /**
     * @param Index index of the molecule
     * @return the set of the given molecule
     */
    public short getMoleculeSet(int Index) throws GenericFailureException;


    /**
     * @param Index index of the molecule
     * @return the Experimental value of the molecule
     */
    public double getExperimentalValue(int Index) throws GenericFailureException;


    public String getExperimentalValueFormatted(int Index) throws GenericFailureException;


    public double getMolecularWeight(int Index) throws GenericFailureException;

    /**
     * @param Index index of the molecule
     * @return the Predicted value of the molecule
     */
    public double getPredictedValue(int Index) throws GenericFailureException;


    public String getPredictedValueFormatted(int Index) throws GenericFailureException;


    public int getDescriptorSize();

    public String GetDescriptorName(int Index) throws GenericFailureException;

    public double getDescriptorMax(int Index) throws GenericFailureException;

    public double getDescriptorMin(int Index) throws GenericFailureException;

    /**
     * @param Index the index of the reference molecule
     * @return the SimilarityDescriptor for the given index
     */
    public SimilarityDescriptors getSimilarityDescriptor(int Index) throws GenericFailureException;

    public String getAlerts(int Index) throws GenericFailureException;

    public ACFItemList getACF() throws GenericFailureException;

    /**
     * Returns the class label for a given value. Returns null if no label
     * is found.
     *
     * @param Value value to be converted
     * @return class label for the given value
     */
    public String getClassLabel(double Value) throws GenericFailureException;


    /**
     * Returns the units used by experimental (and predicted) values in this
     * dataset. If not available, returns a blank string.
     *
     * @return units used for values in current dataset
     */
    public String getUnits();


    /**
     * Returns true if the current dataset has units set for its values.
     *
     * @return true if units are available, false otherwise
     */
    public boolean hasUnits();


    public String FormatValue(double value);

}
