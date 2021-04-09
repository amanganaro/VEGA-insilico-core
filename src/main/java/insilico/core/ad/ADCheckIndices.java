package insilico.core.ad;

import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelector;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.similarity.SimilarMolecule;
import insilico.core.similarity.Similarity;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Ancestor class for Applicability Domain calculation. Provides some
 * basic variables and objects, and common methods for similarity
 * calculation.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class ADCheckIndices {

    
    /** Number of molecules to be showed to user in final report */
    protected int MoleculesToShowSize;

    /** Number of molecules to be used for the calculation of index */
    protected int MoleculesForIndexSize;
    
    /** Number of molecules to be showed for fragment similarity */    
    protected int MoleculesForFragmentsSize;

    /** Reference training set to be used */
    protected iTrainingSet TrainSet;
    
    /** List of the TrainingSet molecules ordered by their similarity */
    protected SimilarMolecule[] SimilarMols;
    
    /** Flag for calculation of indices only on the training set (test set skipped) */
    protected boolean OnlyFromTraining;
    
    /** Flag for skipping exact matches */
    private boolean SkipExperimental;

    
    /**
     * Constructor. Takes as input the TrainingSet object of the model to be
     * processed.
     * 
     * @param ModelTrainingSet Training Set to be used as reference
     */ 
    public ADCheckIndices(iTrainingSet ModelTrainingSet) {
        
        // Defaults
        MoleculesToShowSize = 6;
        MoleculesForIndexSize = 3;
        MoleculesForFragmentsSize = 3;
        OnlyFromTraining = false;
        SkipExperimental = false;

        TrainSet = ModelTrainingSet;
        SimilarMols = null;
    }
    
    
    /**
     * Calculates the similarity index for the given reference molecule against
     * all the molecule from the training set. Similarity is stored in the
     * private array SimilarMols.
     * @param Mol reference molecule
     * @throws insilico.core.exception.GenericFailureException
     */
    protected final void CalculateSimilarity(InsilicoMolecule Mol) throws GenericFailureException {
        
        if ((TrainSet == null)||(TrainSet.getMoleculesSize()==0)) {
            SimilarMols = null;
            log.warn(StringSelector.getString("ad_checkindices_logwarn"));
            throw new GenericFailureException(StringSelector.getString("ad_checkindices_exception"));
        }
        
        Similarity SIM = new Similarity();
        SimilarMols = new SimilarMolecule[TrainSet.getMoleculesSize()];
        
        // Calculates similarity for all molecules
        for (int idx=0; idx<TrainSet.getMoleculesSize(); idx++) {
            double curSim;
            try {
                curSim = SIM.Calculate(Mol.GetSimilarityDescriptors(),
                        TrainSet.getSimilarityDescriptor(idx));
                
                // If similarity is equal to 1, checks if mols are really identical
                if (curSim == 1.0) {
                    IAtomContainer A = Mol.GetStructure();
                    IAtomContainer B = SmilesMolecule.Convert(TrainSet.getSMILES(idx)).GetStructure();
                    boolean AreIsomorph = Similarity.CheckIsomorphism(A, B);
                    if (!AreIsomorph)
                        curSim = 0.999;
                }

                // Adjust low similarities to avoid problems in some following indices
                if (curSim < 0.38)
                    curSim = 0.38;
                
            } catch (Throwable e) {
//                log.warn("AD similarity calculation: unable to calculate for training set molecule "
//                        + idx + ": " + TrainSet.getSMILES(idx));
                curSim = 0;
            }
            SimilarMols[idx] = new SimilarMolecule(idx, curSim);
        }

        // Sorts the array of similar molecules (index=0 means most similar molecule)
        Arrays.sort(SimilarMols);
        
    }
    
    
    /**
     * Retrieves the wanted number of most similar molecules from the 
     * calculated array of similar molecules.
     * 
     * @param Size number of molecules to be retrieved
     * @return array of most similar molecules
     */
    protected final SimilarMolecule[] GetMostSimilarMolecules(int Size) {
        
        if (SimilarMols == null)
            return null;
        
        ArrayList<SimilarMolecule> list = new ArrayList<>();

        for (SimilarMolecule similarMol : SimilarMols) {

            if (OnlyFromTraining) {
                try {
                    short curSet = TrainSet.getMoleculeSet((int) similarMol.getIndex());
                    if (curSet == InsilicoConstants.MOLECULE_TRAINING)
                        continue;
                } catch (GenericFailureException e) {
                    continue;
                }
            }

            if (SkipExperimental) {
                if (similarMol.getSimilarity() == 1.0)
                    continue;
            }

            list.add(similarMol);
            if (list.size() == Size)
                break;
        }
        
//        return list.toArray(new SimilarMolecule[list.size()]);
        return list.toArray(new SimilarMolecule[0]);
    }
    
    
    /**
     * @return the MoleculesToShowSize
     */
    public int getMoleculesToShowSize() {
        return MoleculesToShowSize;
    }

    /**
     * @param MoleculesToShowSize the MoleculesToShowSize to set
     */
    public void setMoleculesToShowSize(int MoleculesToShowSize) {
        this.MoleculesToShowSize = MoleculesToShowSize;
    }

    /**
     * @return the MoleculesForIndexSize
     */
    public int getMoleculesForIndexSize() {
        return MoleculesForIndexSize;
    }

    /**
     * @param MoleculesForIndexSize the MoleculesForIndexSize to set
     */
    public void setMoleculesForIndexSize(int MoleculesForIndexSize) {
        this.MoleculesForIndexSize = MoleculesForIndexSize;
    }

    /**
     * @return the MoleculesForFragmentsSize
     */
    public int getMoleculesForFragmentsSize() {
        return MoleculesForFragmentsSize;
    }

    /**
     * @param MoleculesForFragmentsSize the MoleculesForFragmentsSize to set
     */
    public void setMoleculesForFragmentsSize(int MoleculesForFragmentsSize) {
        this.MoleculesForFragmentsSize = MoleculesForFragmentsSize;
    }
    
    /**
     * @return the OnlyFromTraining
     */
    public boolean isOnlyFromTraining() {
        return OnlyFromTraining;
    }

    /**
     * @param OnlyFromTraining the OnlyFromTraining to set
     */
    public void setOnlyFromTraining(boolean OnlyFromTraining) {
        this.OnlyFromTraining = OnlyFromTraining;
    }

    
    /**
     * @return the list of TS molecules ordered by their calculated similarity
     */
    public SimilarMolecule[] GetSimilarMolecules() {
        return SimilarMols;
    }

    /**
     * @return the SkipExperimental
     */
    public boolean isSkipExperimental() {
        return SkipExperimental;
    }

    /**
     * @param SkipExperimental the SkipExperimental to set
     */
    public void setSkipExperimental(boolean SkipExperimental) {
        this.SkipExperimental = SkipExperimental;
    }
}
