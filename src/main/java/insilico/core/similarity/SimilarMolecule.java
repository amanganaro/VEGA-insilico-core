package insilico.core.similarity;

import lombok.Data;

import java.io.Serializable;

/**
 * Simply wrapper for similar molecules, encapsulates the index of a molecule
 * (from a certain TrainingSet) and its calculated similarity against a
 * target molecule.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Data
public class SimilarMolecule implements Comparable<SimilarMolecule>, Cloneable, Serializable  {

    private static final long serialVersionUID = 1L;

    private long index;
    private double similarity;

    /**
     * Default Constructor
     */
    public SimilarMolecule() {
        this.index = -1;
        this.similarity = 0;
    }

    /**
     * Constructor.
     *
     * @param index molecule's index
     */
    public SimilarMolecule(long index){
        this.index = index;
        this.similarity = 0;
    }

    /**
     * Constructor
     * @param index molecule's index
     * @param similarity calculated molecule's similarity
     */
    public SimilarMolecule(long index, double similarity){
        this.index = index;
        this.similarity = similarity;
    }


    /**
     * Default comparator based on similarity values.<p>
     * Note: the sorting is reversed, so that when using Array.sort a
     * descending order is obtained (low indexes = high similarity)
     *
     * @param moleculeToCompare SimilarMolecule to compare
     * @return comparing result
     */
    @Override
    public int compareTo(SimilarMolecule moleculeToCompare) {
        if (this.getSimilarity() < moleculeToCompare.getSimilarity())
            return +1;
        else if (this.getSimilarity() == moleculeToCompare.getSimilarity())
            return 0;
        else
            return -1;
    }

    /**
     * Clones the object.
     *
     * @return a cloned SimilarMolecule object
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
