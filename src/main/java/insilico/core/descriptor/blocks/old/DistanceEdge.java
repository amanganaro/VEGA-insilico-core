package insilico.core.descriptor.blocks.old;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.blocks.old.weight.VertexDegree;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Distance Edge descriptors.
 * Calculates MDE descriptors (originally from T.E.S.T daphnia model). 
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class DistanceEdge extends DescriptorBlock {

    private final static long serialVersionUID = 1L;

    Logger logger = LoggerFactory.getLogger(DistanceEdge.class);

    private final static String BlockName = "DistanceEdge descriptors";

    private boolean defaultDescriptors;

    public final static String PARAMETER_ATOM_C = "atomC";
    public final static String PARAMETER_ATOM_N = "atomN";
    public final static String PARAMETER_ATOM_O = "atomO";
    public final static String PARAMETER_VD_01 = "vd01";
    public final static String PARAMETER_VD_02 = "vd02";
    public final static String PARAMETER_VD_03 = "vd03";
    public final static String PARAMETER_VD_04 = "vd04";


    
    /**
     * Constructor. This should not be used, no weight is specified. The 
     * overloaded constructors should be used instead.
     */    
    public DistanceEdge() {
        super();
        this.Name = DistanceEdge.BlockName;
        this.defaultDescriptors = true;
    }

    public DistanceEdge(boolean defaultDescriptors) {
        super();
        this.Name = DistanceEdge.BlockName;
        this.defaultDescriptors = defaultDescriptors;
    }




    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        ArrayList<String> ATList = BuildAtomTypeList();
        ArrayList<Integer> VDList = BuildVDList();
        for (String curAtom : ATList) 
            for (Integer curVD : VDList) 
                for (Integer curVD2 : VDList) 
                    Add("MDE_" + curAtom + "_" + curVD + curVD2, "");
        SetAllValues(Descriptor.MISSING_VALUE);
    }
    
    
    private ArrayList<String> BuildAtomTypeList() {
        ArrayList<String> at = new ArrayList<>();
        if (defaultDescriptors) {
            at.add("C");
            at.add("N");
            at.add("O");
        } else {
            if (getBoolProperty(PARAMETER_ATOM_C)) at.add("C");
            if (getBoolProperty(PARAMETER_ATOM_N)) at.add("N");
            if (getBoolProperty(PARAMETER_ATOM_O)) at.add("O");
        }
        return at;
    }
    
    
    private ArrayList<Integer> BuildVDList() {
        ArrayList<Integer> vd = new ArrayList<>();
        if (defaultDescriptors) {
            vd.add(1);
            vd.add(2);
            vd.add(3);
            vd.add(4);
        } else {
            if (getBoolProperty(PARAMETER_VD_01)) vd.add(1);
            if (getBoolProperty(PARAMETER_VD_02)) vd.add(2);
            if (getBoolProperty(PARAMETER_VD_03)) vd.add(3);
            if (getBoolProperty(PARAMETER_VD_04)) vd.add(4);
        }
        return vd;
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
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Get matrices
        int[][] TopoMatrix;
        try {
            TopoMatrix = mol.GetMatrixTopologicalDistance();
        } catch (GenericFailureException e) {
            logger.warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Get VD
        int[] VD = VertexDegree.getWeights(m, true);
        
        int nSK = m.getAtomCount();
        ArrayList<String> ATList = BuildAtomTypeList();
        ArrayList<Integer> VDList = BuildVDList();
        for (String curAtom : ATList) 
            for (Integer curVD : VDList) 
                for (Integer curVD2 : VDList) {
                
                    double n = 0;
                    double d = 1;

                    for (int i=0; i<nSK; i++) 
                        if (curAtom.equalsIgnoreCase(m.getAtom(i).getSymbol())) 
                            for (int j=i+1; j<nSK; j++) 
                                if (curAtom.equalsIgnoreCase(m.getAtom(j).getSymbol())) 
                                    if  ( ((VD[i]==curVD) && (VD[j]==curVD2)) || ((VD[i]==curVD2) && (VD[j]==curVD)) ) {
                                        n++;
                                        d *= TopoMatrix[i][j];
                                    }

                    double res = 0;
                    if (n > 0) {
                        d = Math.pow(d,1.0/(2.0*n));		
                        res = n/(d*d);
                    }        

                    SetByName("MDE_" + curAtom + "_" + curVD + curVD2, res);
            }

    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        DistanceEdge block = new DistanceEdge();
        block.CloneDetailsFrom(this);
        return block;
    }

    
}
