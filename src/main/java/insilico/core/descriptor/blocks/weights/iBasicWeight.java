package insilico.core.descriptor.blocks.weights;

import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Interface for basic weighting schemes, that don't need a initial calculation on the structure and provide
 * scaled (on Carbon) values
 */
public interface iBasicWeight extends iWeight {

    abstract public double[] getWeights(IAtomContainer mol);
    abstract public double getWeight(String AtomType);

    abstract public double[] getScaledWeights(IAtomContainer mol);
    abstract public double getScaledWeight(String AtomType);
}
