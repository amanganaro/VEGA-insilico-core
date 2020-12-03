package insilico.core.descriptor.blocks.pro;

import Jama.Matrix;
import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IBond.Order;

import java.util.List;

/**
 * Walk And Path molecular descriptors.
 * Calculates MWC, MPC, PW, SRW
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class WalkAndPath extends DescriptorBlock {

    private final static long serialVersionUID = 1L;
    private final static String BlockName = "Walk and Path Descriptors";
    private final static int MAX_PATH = 10;
    private final static int MAX_PATH_PW = 5;

    /**
     * Constructor. This should not be used, no weight is specified. The 
     * overloaded constructors should be used instead.
     */    
    public WalkAndPath() {
        super();
        this.Name = WalkAndPath.BlockName;
    }

    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        for (int i = 2; i <= MAX_PATH_PW; i++)
            Add("PW" + i, "path/walk " + i +" - Randic shape index");  // in Dragon PW descriptors are in the topological block
        for (int i = 1; i <= MAX_PATH; i++)
            Add("MWC" + i, "molecular walk count of order " + i);
        for (int i = 1; i <= MAX_PATH; i++)
            Add("SRW" + i, "self-returning walk count of order " + i);
        for (int i = 1; i <= MAX_PATH; i++)
            Add("MPC" + i, "molecular path count of order " + i);
        for (int i = 1; i <= MAX_PATH; i++)
            Add("piPC" + i, "molecular multiple path count of order " + i);
        Add("piID", "conventional bond order ID number");
        Add("TPC", "total path count");
        Add("PCR", "ratio of multiple path count over path count");
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

        IAtomContainer m;
        try {
            m = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            log.warn("Invalid structure, unable to calculate: " + this.Name);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Gets matrices
        int[][] AdjMat = null;
        try {
            AdjMat = mol.GetMatrixAdjacency();
        } catch (GenericFailureException e) {
            log.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        double[][] AdjMatDbl = new double[AdjMat.length][AdjMat[0].length];
        for (int i=0; i<AdjMat.length; i++)
            for (int j=0; j<AdjMat[0].length; j++)
                AdjMatDbl[i][j] = AdjMat[i][j];

        int nSK = m.getAtomCount();
        double piID=0, TPC=0;
                
        // Cycle for all found path schemes
        for (int curPath = 1; curPath<= MAX_PATH; curPath++) {
            
            double CurPath=0, CurWalk=0, CurPW=0, CurMPC=0;
            int[] AtomWalk = GetAtomsWalks(curPath, AdjMatDbl);
            double[][] AtomPath = GetAtomsPaths(curPath, m);
            
            for (int i=0; i<nSK; i++) {
                CurWalk += AtomWalk[i];
                CurPath += AtomPath[i][0];
                CurMPC += AtomPath[i][1];
                CurPW += (double)AtomPath[i][0] / (double)AtomWalk[i];
            }
            
//            if (curPath == 1) {
//                CurWalk /= 2;
//            } else {
                CurWalk = Math.log(1 + CurWalk);
//            }
            CurPath /= 2;
            TPC += CurPath;
            CurMPC /= 2.0;
            piID += CurMPC;
            CurMPC = Math.log(1+CurMPC);
            CurPW /= nSK;
            
            
            SetByName("MWC" + curPath, CurWalk);
            SetByName("piPC" + curPath, CurMPC);
            SetByName("MPC" + curPath, Math.log(1+CurPath));
            SetByName("PW" + curPath, CurPW);
        }

        piID = Math.log(1+piID+nSK);
        TPC = Math.log(1+TPC+nSK);

        double PCR = piID / TPC;
        SetByName("piID", piID);
        SetByName("TPC", TPC);
        SetByName("PCR", PCR);

        int[] MolSRW = GetSRWToLag(MAX_PATH, AdjMatDbl);
        for (int i = 1; i<= MAX_PATH; i++)
            SetByName("SRW" + i, Math.log(1+MolSRW[i]));

    }

    
    
    private int[] GetAtomsWalks(int WalksOrder, double[][] AdjMatrix) {
        
        int[] walks = new int[AdjMatrix.length];
        
        Matrix mAdj = new Matrix(AdjMatrix);
        Matrix mWalks = new Matrix(AdjMatrix);
        
        for (int k=1; k<WalksOrder; k++) {
            mWalks = mWalks.times(mAdj);
        }
        
        for (int i=0; i<AdjMatrix.length; i++) {
            int CurSum = 0;
            for (int j=0; j<AdjMatrix[0].length; j++)
                CurSum += mWalks.get(i, j);
            walks[i] = CurSum;
        }
        
        return walks;
    }

    // index 0 = count of atom paths
    // index 1 = sum of paths weighted by bond order
    private double[][] GetAtomsPaths(int PathsOrder, IAtomContainer Mol) {
        
        int nSK = Mol.getAtomCount();
        double[][] paths = new double[nSK][2];
        
        for (int i=0; i<nSK; i++) {
            IAtom a = Mol.getAtom(i);
            int PathNum = 0;
            double totBO = 0;
            for (int j=0; j<nSK; j++) {
                if (i==j)
                    continue;
                IAtom b =  Mol.getAtom(j);
                List<List<IAtom>> list= PathTools.getAllPaths(Mol, a, b);
                for (int k=0; k<list.size(); k++) {
                    if (list.get(k).size() == (PathsOrder+1)) {
                        PathNum++;
                        
                        double curBO=1;
                        for (int at_idx=0; at_idx<list.get(k).size()-1; at_idx++) {
                            IAtom at_1 = list.get(k).get(at_idx);
                            IAtom at_2 = list.get(k).get(at_idx+1);
                            IBond curBond = Mol.getBond(at_2, at_1);
                            if (curBond.getFlag(CDKConstants.ISAROMATIC))
                                curBO *= 1.5;
                            else {
                                if (curBond.getOrder() == Order.SINGLE) curBO *= 1;
                                if (curBond.getOrder() == Order.DOUBLE) curBO *= 2;
                                if (curBond.getOrder() == Order.TRIPLE) curBO *= 3;
                                if (curBond.getOrder() == Order.QUADRUPLE) curBO *= 4;
                            }
                        }
                        totBO+=curBO;
                    }
                }
                paths[i][0] = PathNum;
                paths[i][1] = totBO;
            }
        }
        
        return paths;
    }    
    
    
    private int[] GetSRWToLag(int Lag, double[][] AdjMatrix) {
        
        int nSK = AdjMatrix.length;
        int[] MolSRW = new int[Lag+1];
        
        Matrix mAdj = new Matrix(AdjMatrix);
        Matrix mWalks = new Matrix(AdjMatrix);
        
        for (int k=1; k<(Lag+1); k++) {
            
            MolSRW[k] = 0;
            for (int i=0; i<nSK; i++) {
                MolSRW[k] += mWalks.get(i, i);
            }
            
            mWalks = mWalks.times(mAdj);
        }

        return MolSRW;
    }
    
    

    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        WalkAndPath block = new WalkAndPath();
        block.CloneDetailsFrom(this);
        return block;
    }

    
}
