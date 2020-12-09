package insilico.core.molecule;


import insilico.core.alerts.Alert;
import insilico.core.alerts.AlertList;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.blocks.AtomCenteredFragments;
import insilico.core.descriptor.blocks.Constitutional;
import insilico.core.descriptor.blocks.Rings;
import insilico.core.descriptor.blocks.weights.basic.WeightsMass;
import insilico.core.descriptor.blocks.weights.iBasicWeight;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.exception.MoleculeConversionException;
import insilico.core.molecule.acf.ACFItemList;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.matrix.*;
import insilico.core.molecule.tools.Manipulator;
import insilico.core.similarity.SimilarityDescriptors;
import insilico.core.similarity.SimilarityDescriptorsBuilder;
import org.openscience.cdk.RingSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class to wrap InsilicoMolecule structures' cached data
 */
public class InsilicoMoleculeCache implements Serializable, Cloneable {

    private Logger logger = LoggerFactory.getLogger(InsilicoMoleculeCache.class);

    private static final long serialVersionUID = 1L;

    private IAtomContainer structure;
    private RingSet SSSR;
    private RingSet allRings;
    private ArrayList<MoleculeMatrix> matrices;
    private AlertList structuralAlerts;
    private SimilarityDescriptors similarityDescriptors;
    private ACFItemList ACF;
    private Double MW;
    private ArrayList<Descriptor> basicDescriptors;


    /**
     * Reset class for object's properties. Delete structure's data
     */
    public final void ClearCache() {
        ClearCachePreserveStructure();
        structure = null;
        SSSR = null;
        allRings = null;
    }

    /**
     * Reset class for object's properties. Doesn't delete structure's data
     */
    public final void ClearCachePreserveStructure() {
        matrices = new ArrayList<>();
        structuralAlerts = new AlertList();
        similarityDescriptors = null;
        ACF = null;
        MW = null;
        basicDescriptors = null;
    }

    public InsilicoMoleculeCache() {
        ClearCachePreserveStructure();
        ClearCache();
    }


    public void SetStructure(IAtomContainer newStructure){
        this.structure = newStructure;
    }

    public void SetStructure(String newSMILES) throws InvalidMoleculeException {
        this.structure = GetStructure(newSMILES, newSMILES.contains("H"));
    }

    public double GetMolecularWeight(String SMILES, boolean explicitHydrogen) throws InvalidMoleculeException {

        if (MW != null)
            return MW;

        IAtomContainer m = this.GetStructure(SMILES, explicitHydrogen);

        int nSK = m.getAtomCount();
        iBasicWeight ws = new WeightsMass();
        double[] weights = ws.getWeights(m);

        double sum = 0;
        for (int i=0; i<nSK; i++) {
            if (weights[i] == Descriptor.MISSING_VALUE) {
                logger.warn("Missing value for calculation of molecular weight");
                throw new InvalidMoleculeException("Missing value for calculation of molecular weight");
            }
            sum += weights[i];
        }

        // If H are implicit, calculate them
        if (!explicitHydrogen) {
            int nH = 0;
            for (int i=0; i<nSK; i++)
                nH += m.getAtom(i).getImplicitHydrogenCount();
            sum += nH * ws.getWeight("H");
        }

        this.MW = sum;
        return MW;
    }

    /**
     * Get stored IAtomContainer's object about structure.
     * @param SMILES
     * @param explicitHydrogen
     * @return
     * @throws InvalidMoleculeException
     */
    public IAtomContainer GetStructure(String SMILES, boolean explicitHydrogen) throws InvalidMoleculeException {

        if (structure != null) {
            return structure;
        }

        try {
            structure = SmilesMolecule.CreateCDKMolecule(SMILES, new InsilicoMoleculeMessages());
        } catch (MoleculeConversionException ex) {
            structure = null;
            String err = "Failed SMILES conversion while requesting CDK Molecule for SMILES: " + SMILES;
            logger.warn(err);
            throw new InvalidMoleculeException(err);
        }

        if (explicitHydrogen) {
            try {
                structure = Manipulator.AddHydrogens(structure);
            } catch (GenericFailureException ex) {
                structure = null;
                String err = "Failed normalization while requesting CDK Molecule for SMILES: " + SMILES;
                logger.warn(err);
                throw new InvalidMoleculeException(err);
            }
        }
        return structure;
    }


    public ArrayList<Descriptor> getBasicDescriptors(InsilicoMolecule mol) throws InvalidMoleculeException {
        if (basicDescriptors == null) {

            // Basic descriptors blocks
            ArrayList<DescriptorBlock> blocks = new ArrayList<>();
            blocks.add(new Constitutional());
            blocks.add(new Rings());
            blocks.add(new AtomCenteredFragments());

            for (DescriptorBlock b : blocks)
                b.Calculate(mol);

            basicDescriptors = new ArrayList<>();
            for (DescriptorBlock b : blocks)
                for (Descriptor d : b.GetAllDescriptors())
                    basicDescriptors.add(d);
        }
        return basicDescriptors;
    }


    /**
     * GET SSR
     * @param SMILES
     * @param explicitHydrogen
     * @return
     * @throws InvalidMoleculeException
     */
    public RingSet GetSSSR (String SMILES, boolean explicitHydrogen) throws InvalidMoleculeException {
        if (SSSR == null){
            Cycles cycles = Cycles.sssr(GetStructure(SMILES, explicitHydrogen));
            SSSR = (RingSet) cycles.toRingSet();
//            DEPRECATED
//            SSSRFinder rf = new SSSRFinder(GetStructure());
//            SSSR = (RingSet) rf.findSSSR();
        }
        return SSSR;
    }

    /**
     * Find all rings for given InsilicoMolecule object
     * @param SMILES InsilicoMolecule smiles
     * @param explicitHydrogen InsilicoMolecule flag for explicyt hydrogen
     * @param warnings InsilicoMolecule's warning messages
     * @param errors InsilicoMolecule's error messages
     * @return
     * @throws InvalidMoleculeException
     */
    public RingSet GetAllRings(String SMILES, boolean explicitHydrogen, InsilicoMoleculeMessages warnings, InsilicoMoleculeMessages errors) throws InvalidMoleculeException{
        if (allRings == null){
            AllRingsFinder ringsFinder = new AllRingsFinder();
            try {
                allRings = (RingSet) ringsFinder.findAllRings(GetStructure(SMILES, explicitHydrogen));
            } catch (CDKException ex) {

                errors.AddMessage("Unable to perceive all rings");
                logger.warn("Unable to find all rings for molecule " + SMILES);
                throw new InvalidMoleculeException("unable to find all rings");
            }
        }
        return allRings;
    }

    public boolean HasSimilarityDescriptors() {
        return !(similarityDescriptors == null);
    }

    public SimilarityDescriptors GetSimilarityDescriptors(InsilicoMolecule Mol) {
        if (similarityDescriptors == null) {
            SimilarityDescriptorsBuilder sb = new SimilarityDescriptorsBuilder();
            similarityDescriptors = sb.Calculate(Mol);
        }
        return similarityDescriptors;
    }

    public void SetSimilarityDescriptors(SimilarityDescriptors SimilarityDescriptors) {
        this.similarityDescriptors = SimilarityDescriptors;
    }

    public boolean HasACF(){return !(ACF == null);}
    public ACFItemList  GetACF(){return ACF;}
    public void SetACF(ACFItemList ACFItems){
        this.ACF = ACFItems;
    }

    public AlertList GetAlerts(){return structuralAlerts;}
    public void PurgeAlerts(){structuralAlerts = new AlertList();}

    public void AddAlert(Alert alert){structuralAlerts.add(alert);}
    public void AddAlert(AlertList alert) {
        for (Alert a: alert.getSAList())
            structuralAlerts.add(a);
    }

    /**
     * Calculate MoleculeMatrix object for given SMILES
     * @param MatrixClass
     * @param SMILES
     * @param explicitHydrogen
     * @return
     * @throws GenericFailureException
     */
    public MoleculeMatrix GetMatrix(Class MatrixClass, String SMILES, boolean explicitHydrogen) throws GenericFailureException {

        // Check if already cached
        for (MoleculeMatrix matrix: matrices)
            if (matrix.getMatrixClass() == MatrixClass) return matrix;

        MoleculeMatrix matrix = null;
        try {

            if (MatrixClass == AdjacencyMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass, AdjacencyMatrix.getMatrix(this.GetStructure(SMILES,explicitHydrogen)));
            if (MatrixClass == BondAugMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass, BondAugMatrix.getMatrix(this.GetStructure(SMILES,explicitHydrogen)));
            if (MatrixClass == BurdenMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass, BurdenMatrix.getMatrix(this.GetStructure(SMILES,explicitHydrogen)));
            if (MatrixClass == ConnectionAugMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass, ConnectionAugMatrix.getMatrix(this.GetStructure(SMILES,explicitHydrogen)));
            if (MatrixClass == DistanceDetourMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass,DistanceDetourMatrix.getMatrix(this.GetStructure(SMILES,explicitHydrogen)));
            if (MatrixClass == EdgeAdjacencyMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass,EdgeAdjacencyMatrix.getMatrix(this.GetStructure(SMILES,explicitHydrogen)));
            if (MatrixClass == TopoDistanceMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass, TopoDistanceMatrix.getMatrix(this.GetStructure(SMILES,explicitHydrogen)));
            if (MatrixClass == TopoDistanceMatrixHFilled.class)
                matrix = new MoleculeMatrix(MatrixClass, TopoDistanceMatrixHFilled.getMatrix(this.GetStructure(SMILES,explicitHydrogen)));
            if (MatrixClass == LaplaceMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass,LaplaceMatrix.getMatrix(this.GetStructure(SMILES,explicitHydrogen)));
            if (MatrixClass == BaryszMatrix.class)
                matrix = new MoleculeMatrix(MatrixClass,BaryszMatrix.getMatrix(this.GetStructure(SMILES,explicitHydrogen)));

        } catch (InvalidMoleculeException ex){
            String msg = "Unable to build matrix " + MatrixClass.getName() + ", invalid molecule structure for " + SMILES;
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }

        if (matrix != null) {
            this.matrices.add(matrix);
            return matrix;
        } else {
            String msg = "Unable to build matrix " + MatrixClass.getName() + " for molecule " + SMILES;
            logger.warn(msg);
            throw new GenericFailureException(msg);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
