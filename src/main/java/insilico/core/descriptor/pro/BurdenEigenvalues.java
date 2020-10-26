package insilico.core.descriptor.pro;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.pro.weights.basic.*;
import insilico.core.descriptor.pro.weights.iBasicWeight;
import insilico.core.descriptor.pro.weights.iWeight;
import insilico.core.descriptor.pro.weights.other.WeightsIState;
import insilico.core.descriptor.weight.Electronegativity;
import insilico.core.descriptor.weight.Mass;
import insilico.core.descriptor.weight.Polarizability;
import insilico.core.descriptor.weight.VanDerWaals;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Burden eigenvalue (on H-Filled molecule) molecular descriptors.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class BurdenEigenvalues extends DescriptorBlock {

    private static final long serialVersionUID = 1L;
    private final static String BlockName = "Burden Eigenvalue Descriptors";
    private final static int MAXEIG = 8;

    private ArrayList<iWeight> bWeights;


    /**
     * Constructor. This should not be used, no weight is specified. The
     * overloaded constructors should be used instead.
     */
    public BurdenEigenvalues() {
        super();
        this.Name = BurdenEigenvalues.BlockName;
    }


    @Override
    protected final void GenerateDescriptors() {

        bWeights = new ArrayList<>();
        bWeights.add(new WeightsMass());
        bWeights.add(new WeightsVanDerWaals());
        bWeights.add(new WeightsElectronegativity());
        bWeights.add(new WeightsPolarizability());
        bWeights.add(new WeightsIonizationPotential());
        bWeights.add(new WeightsIState());

        DescList.clear();
        for (iWeight curWeight : bWeights) {
            for (int i=0; i<MAXEIG; i++)
                Add("SpMax" + (i+1) + "_Bh(" + curWeight.getSymbol() + ")", "largest eigenvalue n. " + (i+1) + " of Burden matrix weighted by " + curWeight.getName());
            for (int i=0; i<MAXEIG; i++)
                Add("SpMin" + (i+1) + "_Bh(" + curWeight.getSymbol() + ")", "smallest eigenvalue n. " + (i+1) + " of Burden matrix weighted by " + curWeight.getName());
        }
        SetAllValues(Descriptor.MISSING_VALUE);
    }


    /**
     * Calculate descriptors for the given molecule.
     *
     * @param mol molecule to be calculated
     */
    @Override
    public void Calculate(InsilicoMolecule mol) {

        // Generate/clears descriptors
        GenerateDescriptors();

        // Burden matrix is calculated on H-filled molecules
        InsilicoMolecule HMol;
        IAtomContainer m;
        try {
            HMol = (InsilicoMolecule) mol.Clone();
            HMol.SetExplicitHydrogen(true);
            m = HMol.GetStructure();
        } catch (CloneNotSupportedException | InvalidMoleculeException e) {
            log.warn("Invalid structure, unable to calculate: " + this.Name);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Gets matrix
        double[][] BurdenMat;
        try {
            BurdenMat = HMol.GetMatrixBurden();
        } catch (GenericFailureException e) {
            log.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        int nSK = m.getAtomCount();

        // Cycle for all found weighting schemes
        for (iWeight curWeight : bWeights) {

            // Sets needed weights
            double[] w;

            if (curWeight.getClass() == WeightsIState.class) {

                // I-States
                w = ((WeightsIState)curWeight).getWeights(m, true);

                // correction for compatibility with D7
                // H I-state is always 1
                for (int i=0; i<nSK; i++) {
                    if (m.getAtom(i).getSymbol().equalsIgnoreCase("H"))
                        w[i] = 1;
                }

            } else {

                // All other weights are basic weights (scaled values)
                w = ((iBasicWeight) curWeight).getScaledWeights(m);
            }

            // If one or more weights are not available, sets all to missing value
            boolean MissingWeight = false;
            for (int i=0; i<nSK; i++)
                if (w[i] == Descriptor.MISSING_VALUE)
                    MissingWeight = true;
            if (MissingWeight)
                continue;

            // Builds the weighted matrix
            for (int i=0; i<nSK; i++) {
                BurdenMat[i][i] = w[i];
            }

            // Calculates eigenvalues
            Matrix DataMatrix = new Matrix(BurdenMat);
            double[] eigenvalues;
            EigenvalueDecomposition ed = new EigenvalueDecomposition(DataMatrix);
            eigenvalues = ed.getRealEigenvalues();
            Arrays.sort(eigenvalues);

            for (int i=0; i<MAXEIG; i++) {
                double valH, valL;
                if (i>(eigenvalues.length-1)) {
                    valH = 0;
                    valL = 0;
                } else {
                    if (eigenvalues[eigenvalues.length-1-i] > 0)
                        valH = eigenvalues[eigenvalues.length-1-i];
                    else
                        valH = 0;
                    if (eigenvalues[i] < 0)
                        valL = Math.abs(eigenvalues[i]);
                    else
                        valL = 0;
                }
                SetByName("SpMax" + (i+1) + "_Bh(" + curWeight.getSymbol() + ")", valH);
                SetByName("SpMin" + (i+1) + "_Bh(" + curWeight.getSymbol() + ")", valL);
            }
        }
    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        BurdenEigenvalues block = new BurdenEigenvalues();
        block.CloneDetailsFrom(this);
        return block;
    }


}
