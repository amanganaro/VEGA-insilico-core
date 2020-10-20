package insilico.core.descriptor.pro;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.pro.weights.basic.*;
import insilico.core.descriptor.pro.weights.iBasicWeight;
import insilico.core.descriptor.pro.weights.iWeight;
import insilico.core.descriptor.pro.weights.other.WeightsIState;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.matrix.TopoDistanceMatrix;
import insilico.core.molecule.tools.Manipulator;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;

/**
 * Autocorrelation molecular descriptors.
 * Calculates Autocorrelation ATS, Moran Autocorrelations and Geary
 * Autocorrelations.
 *
 * Note: only ATS is given in log(1+val) form.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class AutoCorrelation extends DescriptorBlock {

    private final static long serialVersionUID = 1L;
    private final static String BlockName = "AutoCorrelation Descriptors";
    private final static int MAXLAG = 8;

    private ArrayList<iWeight> bWeights;


    /**
     * Constructor. This should not be used, no weight is specified. The
     * overloaded constructors should be used instead.
     */
    public AutoCorrelation() {
        super();
        this.Name = AutoCorrelation.BlockName;
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
        for (iWeight w : bWeights)
            for (int curLag=1; curLag<=MAXLAG; curLag++)
                Add("ATS" + curLag + w.getSymbol(), "Broto-Moreau autocorrelation of lag " + curLag + " (log function) weighted by " + w.getName());
        for (iWeight w : bWeights)
            for (int curLag=1; curLag<=MAXLAG; curLag++)
                Add("ATSC" + curLag + w.getSymbol(), "Centred Broto-Moreau autocorrelation of lag " + curLag + " (log function) weighted by " + w.getName());
        for (iWeight w : bWeights)
            for (int curLag=1; curLag<=MAXLAG; curLag++)
                Add("MATS" + curLag + w.getSymbol(), "Moran autocorrelation of lag " + curLag + " (log function) weighted by " + w.getName());
        for (iWeight w : bWeights)
            for (int curLag=1; curLag<=MAXLAG; curLag++)
                Add("GATS" + curLag + w.getSymbol(), "Geary autocorrelation of lag " + curLag + " (log function) weighted by " + w.getName());
        SetAllValues(Descriptor.MISSING_VALUE);
    }


    /**
     * Calculate descriptors for the given molecule.
     *
     * @param mol molecule to be calculated
     */
    @Override
    public void Calculate(InsilicoMolecule mol) {

        try {

            // Generate/clears descriptors
            GenerateDescriptors();

            // AC are calculated on H-filled molecules

            IAtomContainer m;
            try {
                IAtomContainer orig_m = mol.GetStructure();
                m = Manipulator.AddHydrogens(orig_m);
            } catch (InvalidMoleculeException | GenericFailureException e) {
                log.warn("Invalid structure, unable to calculate: " + this.Name);
                SetAllValues(Descriptor.MISSING_VALUE);
                return;
            }

            // Gets matrices
            int[][] TopoMatrix;
            try {
                TopoMatrix = TopoDistanceMatrix.getMatrix(m);
            } catch (Exception e) {
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

                // Calculates weights averages
                double wA = 0;
                for (int i=0; i<nSK; i++)
                    wA += w[i];
                wA = wA / ((double) nSK);

                // Calculates autocorrelations
                for (int lag=1; lag<=MAXLAG; lag++) {

                    double AC=0, ACS=0, MoranAC=0, GearyAC=0;
                    double denom = 0, delta = 0;

                    for (int i=0; i<nSK; i++) {

                        denom += Math.pow((w[i] - wA), 2);

                        for (int j=0; j<nSK; j++)
                            if (TopoMatrix[i][j] == lag) {
                                AC += w[i] * w[j];
                                ACS += Math.abs((w[i]-wA) * (w[j]-wA));
                                MoranAC += (w[i] - wA) * (w[j] - wA);
                                GearyAC += Math.pow((w[i] - w[j]), 2);
                                delta++;
                            }
                    }

                    if (delta > 0) {
                        if (denom == 0) {
                            MoranAC = 1;
                            GearyAC = 0;
                        } else {
                            MoranAC = ((1 / delta) * MoranAC) / ((1 / ((double)nSK)) * denom);
                            GearyAC = ((1 / (2 * delta)) * GearyAC) / ((1 / ((double)(nSK - 1))) * denom);
                        }
                    }

                    // AC transformed in log form
                    AC /= 2.0;
                    AC = Math.log(1 + AC);

                    ACS /= 2.0;

                    // Sets descriptors
                    SetByName("ATS" + lag + curWeight.getSymbol(), AC);
                    SetByName("ATSC" + lag + curWeight.getSymbol(), ACS);
                    SetByName("MATS" + lag + curWeight.getSymbol(), MoranAC);
                    SetByName("GATS" + lag + curWeight.getSymbol(), GearyAC);
                }
            }

        } catch (Throwable e) {
            log.warn("Unable to calculate: " + this.Name + " - " + e.getMessage());
            this.SetAllValues(Descriptor.MISSING_VALUE);
        }

    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        AutoCorrelation block = new AutoCorrelation();
        block.CloneDetailsFrom(this);
        return block;
    }


}
