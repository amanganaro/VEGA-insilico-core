package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.weight.QuantumNumber;
import insilico.core.descriptor.weight.ValenceVertexDegree;
import insilico.core.descriptor.weight.VertexDegree;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.openscience.cdk.Atom;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Connectivity molecular descriptors.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ConnectivityIndices extends DescriptorBlock {
    
    private static final long serialVersionUID = 1L;

    Logger logger = LoggerFactory.getLogger(ConnectivityIndices.class);

    private final static String BlockName = "Connectivity Descriptors";

    public final static String PARAMETER_MAX_PATH_00 = "mp00";
    public final static String PARAMETER_MAX_PATH_01 = "mp01";
    public final static String PARAMETER_MAX_PATH_02 = "mp02";
    public final static String PARAMETER_MAX_PATH_03 = "mp03";
    public final static String PARAMETER_MAX_PATH_04 = "mp04";
    public final static String PARAMETER_MAX_PATH_05 = "mp05";
    
    

    /**
     * Constructor. Sets the default value for the max path (5)
     */    
    public ConnectivityIndices() {
        super();
        this.Name = ConnectivityIndices.BlockName;
    }
    

    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        int MaxPath = RetrieveMaxPath();
        for (int i=0; i<(MaxPath+1); i++) {
            Add("X" + i, "");
            Add("X" + i + "v", "");
            Add("X" + i + "sol", "");
        }
        SetAllValues(Descriptor.MISSING_VALUE);
    }
    
    
    private int RetrieveMaxPath() {
        int MaxPath = -1;
        if (getBoolProperty(PARAMETER_MAX_PATH_00))
            MaxPath = 0;
        if (getBoolProperty(PARAMETER_MAX_PATH_01))
            MaxPath = 1;
        if (getBoolProperty(PARAMETER_MAX_PATH_02))
            MaxPath = 2;
        if (getBoolProperty(PARAMETER_MAX_PATH_03))
            MaxPath = 3;
        if (getBoolProperty(PARAMETER_MAX_PATH_04))
            MaxPath = 4;
        if (getBoolProperty(PARAMETER_MAX_PATH_05))
            MaxPath = 5;
        return MaxPath;
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
        
        // Retrieves parameter
        int MaxPath = RetrieveMaxPath();

        IAtomContainer m;
        try {
            m = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        
        // Gets matrices
        double[][] ConnAugMatrix;
        try {
            ConnAugMatrix = mol.GetMatrixConnectionAugmented();
        } catch (GenericFailureException e) {
            InsilicoLogger.getLogger().warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
                
        int nSK = m.getAtomCount();
        
        int[] VD = VertexDegree.getWeights(m, true);
        double[] ValenceVD = ValenceVertexDegree.getWeights(m);
        int[] Qnumbers = QuantumNumber.getWeights(m);
        double[] curDescX = new double[MaxPath+1];
        double[] curDescXv = new double[MaxPath+1];
        double[] curDescXsol = new double[MaxPath+1];

        // checks for missing weights
        for (int qnumber : Qnumbers)
            if (qnumber == -999)
                return;
        for (double v : ValenceVD)
            if (v == -999)
                return;
        
        // clears VD matrix from linked F
        for (int i=0; i<nSK; i++) 
            for (int j=0; j<nSK; j++) {
                if (i==j) continue;
                if ((ConnAugMatrix[i][j]>0) && (ConnAugMatrix[j][j]==9))
                    VD[i]--;
            }
        
        
        for (int k=0; k<MaxPath; k++) { 
            curDescX[k] = 0; 
            curDescXv[k] = 0; 
            curDescXsol[k] = 0;
        }
            
        for (int i=0; i<nSK; i++) {

            if (ConnAugMatrix[i][i] == 9) 
                continue; // F not taken into account
                        
            // path 0
            curDescX[0] += Math.pow(VD[i], -0.5);
            curDescXv[0] += Math.pow(ValenceVD[i], -0.5);
            curDescXsol[0] += 0.5 * Qnumbers[i] * Math.pow(VD[i], -0.5);
            
            // path 1 - MaxPath
            for (int path=1; path<(MaxPath+1); path++) {
                
                if (curDescX[path] == -999) continue;
                
                Atom at = (Atom) m.getAtom(i);
                List<List<IAtom>> CurPaths =  PathTools.getPathsOfLength(m, at, path);
                for (List<IAtom> curPath : CurPaths) {
                    double prodX = 1;
                    double prodXv = 1;
                    int prodQuantum = 1;
                    for (IAtom iAtom : curPath) {
                        int atIdx = m.indexOf(iAtom);
//                        if (ConnMatrix[atIdx][atIdx] == 9) 
//                            continue; // F not taken into account
                        prodX *= VD[atIdx];
                        prodXv *= ValenceVD[atIdx];
                        prodQuantum *= Qnumbers[atIdx];
                    }
                    curDescX[path] += Math.pow(prodX, -0.5);
                    curDescXv[path] += Math.pow(prodXv, -0.5);
                    curDescXsol[path] += (1.00 / Math.pow(2.00, (double) (path + 1))) *
                            ((double) prodQuantum) * Math.pow(prodX, -0.5);
                }
                
            }
        }
        
        // descriptors with path>0 counted all paths twice
        for (int i=1; i<(MaxPath+1); i++) {
            curDescX[i] /= 2;
            curDescXv[i] /= 2;
            curDescXsol[i] /= 2;
        }
        
        // Sets descriptors
        for (int i=0; i<(MaxPath+1); i++) {
            SetByName("X" + i,  curDescX[i]);
            SetByName("X" + i + "v", curDescXv[i]);
            SetByName("X" + i + "sol", curDescXsol[i]);
        }

    }

    
    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        ConnectivityIndices block = new ConnectivityIndices();
        block.CloneDetailsFrom(this);
        return block;
    }

}
