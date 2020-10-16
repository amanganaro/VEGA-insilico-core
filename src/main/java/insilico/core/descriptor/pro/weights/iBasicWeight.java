package insilico.core.descriptor.pro.weights;

import org.openscience.cdk.interfaces.IAtomContainer;

public interface iBasicWeight {

    abstract public double[] getWeights(IAtomContainer mol);
    abstract public double getWeight(String AtomType);
}
