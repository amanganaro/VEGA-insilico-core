package insilico.core.descriptor.pro;

import Jama.Matrix;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.pro.weights.other.WeightsVertexDegree;
import insilico.core.descriptor.weight.VertexDegree;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Topological Charge AutoCorrelation descriptors block.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class TopologicalChargeAutoCorrelation extends DescriptorBlock {

    private static final long serialVersionUID = 1L;
    private static final String BlockName = "Topological Charge Descriptors";
    private final static int MAXPATH = 8;



    /**
     * Constructor.
     */
    public TopologicalChargeAutoCorrelation() {
        super();
        this.Name = TopologicalChargeAutoCorrelation.BlockName;
    }
    

    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        for (int i=0; i<MAXPATH; i++)
            Add("GGI" + (i+1), "topological charge index of order " + (i+1));
        for (int i=0; i<MAXPATH; i++)
            Add("JGI" + (i+1), "mean topological charge index of order " + (i+1));

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

        IAtomContainer curMol;
        try {
            curMol = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            log.warn("Invalid structure, unable to calculate: " + this.Name);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        
        // Gets matrices
        int[][] TopoMat = null;
        try {
            TopoMat = mol.GetMatrixTopologicalDistance();
        } catch (GenericFailureException e) {
            log.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        try {

            int nSK = curMol.getAtomCount();
            int[][] AdjMat = AdjacencyMatrix.getMatrix(curMol);
            int[] VertexDegrees = WeightsVertexDegree.getWeights(curMol, false);

            Matrix mAdj = new Matrix(AdjMat.length, AdjMat[0].length);
            for (int i=0; i<AdjMat.length; i++)
                for (int j=0; j<AdjMat[0].length; j++) 
                    mAdj.set(i, j, (double)AdjMat[i][j]);

            Matrix mRecSqrDist = new Matrix(TopoMat.length, TopoMat[0].length);
            for (int i=0; i<TopoMat.length; i++)
                for (int j=0; j<TopoMat[0].length; j++) {
                    double CurVal = 0;
                    if (TopoMat[i][j] != 0)
                        CurVal = 1 / Math.pow((double)TopoMat[i][j], 2);
                    mRecSqrDist.set(i, j, CurVal);
                }

            Matrix multMat = mAdj.times(mRecSqrDist);

            double[][] CTMatrix = new double[nSK][nSK];
            for (int i=0; i<nSK; i++)
                for (int j=0; j<nSK; j++) {
                    if (i == j)
                        CTMatrix[i][j] = VertexDegrees[i];
                    else {
                        CTMatrix[i][j] = multMat.get(i, j) - multMat.get(j, i);
                    }
                }

            int[] PathCount = new int[MAXPATH];
            double[] GGIval = new double[MAXPATH];
            for (int i=0; i<MAXPATH; i++) {
                PathCount[i] = 0;
                GGIval[i] = 0;
            }

            for (int i=0; i<nSK; i++)
                for (int j=i; j<nSK; j++) {

                    int CurPath = TopoMat[i][j];
                    if  ((CurPath>0) && (CurPath<=MAXPATH)) {
                        PathCount[CurPath-1]++;
                        GGIval[CurPath-1] += Math.abs(CTMatrix[i][j]);
                    }
                }

            // Sets descriptors
            for (int i=0; i<MAXPATH; i++) {
                if (GGIval[i]>0) {
                    SetByName("GGI" + (i+1), GGIval[i]);
                    double JGIval = GGIval[i] / (double)PathCount[i];
                    SetByName("JGI" + (i+1), JGIval);
                } else {
                    SetByName("GGI" + (i+1), 0);
                    SetByName("JGI" + (i+1), 0);
                }
            }

        } catch (Throwable e) {
            this.SetAllValues(Descriptor.MISSING_VALUE);
        }
        
    }    

    
    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone()
            throws CloneNotSupportedException {
        TopologicalChargeAutoCorrelation block = new TopologicalChargeAutoCorrelation();
        block.CloneDetailsFrom(block);
        return block;
    }

    
}
