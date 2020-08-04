package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.openscience.cdk.Atom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.Arrays;

/**
 * Topological distances descriptors.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class Cats2D extends DescriptorBlock {

    Logger logger = LoggerFactory.getLogger(Cats2D.class);


    private final static long serialVersionUID = 1L;
    private final static String BlockName = "CATS 2D Descriptors";
    
    private final static int MAX_CATS_DISTANCE = 10;
    
    private final static String TYPE_A = "A";
    private final static String TYPE_P = "P";
    private final static String TYPE_N = "N";
    private final static String TYPE_L = "L";
    private final static String TYPE_D = "D";

    private final static String[][] AtomCouples = {
        {TYPE_P, TYPE_N},
        {TYPE_A, TYPE_P},
        {TYPE_D, TYPE_A},
        {TYPE_A, TYPE_L},
        {TYPE_D, TYPE_L},
        {TYPE_N, TYPE_L}, 
    };
    
    private double[][] ConnAugMatrix;
    
    
    /**
     * Constructor. This should not be used, no weight is specified. The 
     * overloaded constructors should be used instead.
     */    
    public Cats2D() {
        super();
        this.Name = Cats2D.BlockName;
    }

    
    
    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        for (int lag=0; lag< MAX_CATS_DISTANCE; lag++)
            for (int i=0; i<AtomCouples.length; i++) 
                Add("CATS2D_"+lag+"_" + AtomCouples[i][0] + AtomCouples[i][1], "CATS 2D "+ AtomCouples[i][0] + "-" + AtomCouples[i][1] + " at topological distance " + lag);
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
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        int nSK = m.getAtomCount();

        // Gets matrices
        int[][] TopoMat = null;
        try {
            TopoMat = mol.GetMatrixTopologicalDistance();
        } catch (GenericFailureException e) {
            InsilicoLogger.getLogger().warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        try {
            ConnAugMatrix = mol.GetMatrixConnectionAugmented();
        } catch (GenericFailureException e) {
            InsilicoLogger.getLogger().warn(e.getMessage());
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Gets CATS types
        ArrayList<String>[] CatsTypes = setCatsAtomType(m);
        
        
        for (int d=0; d<AtomCouples.length; d++) {
        
            int descT = 0;
            int[] desc = new int[MAX_CATS_DISTANCE];
            Arrays.fill(desc, 0);
            
            for (int i=0; i<nSK; i++) {
                if ( isIn(CatsTypes[i], AtomCouples[d][0]) ) {
                    for (int j=0; j<nSK; j++) {
                        if (i==j) continue;
                        if ( isIn(CatsTypes[j], AtomCouples[d][1]) ) {

                            if (TopoMat[i][j] < MAX_CATS_DISTANCE) 
                                desc[TopoMat[i][j]]++;
                            
                        }                        
                    }
                }
            }

            for (int i=0; i<desc.length; i++)            
                SetByName("CATS2D_"+i+"_" + AtomCouples[d][0] + AtomCouples[d][1], desc[i]);
                
        }

    }

    
    private boolean isIn(ArrayList<String> list, String s) {
        for (String ss : list)
            if (ss.equalsIgnoreCase(s))
                return true;
        return false;
    }
    
    /**
     * Sets CATS 2D atom types for each atom, as a list of string containing
     * all matching types for each atom.
    **/
    private ArrayList<String>[] setCatsAtomType(IAtomContainer m) {
        
        int nSK = m.getAtomCount();
        ArrayList<String>[] AtomTypes = new ArrayList[nSK];
        
        for (int i=0; i<nSK; i++) {
        
            AtomTypes[i] = new ArrayList<>();
            Atom CurAt = (Atom) m.getAtom(i);
            
            boolean tN=false, tP=false, tA=false, tD=false, tL=false;
            
            // Definition of CATS types
            //
            // A: O, N without H
            // N: [+], NH2
            // P: [-], COOH, POOH, SOOH

            // Hydrogens
            int H = 0;
            try {
                H = CurAt.getImplicitHydrogenCount();
            } catch (Exception e) { }

            // [+]
            if (CurAt.getFormalCharge() > 0) {
                
                boolean NpOm = false;
                if (ConnAugMatrix[i][i] == 7) { 
                    for (int j=0; j<nSK; j++) {
                        if (j==i) continue;
                        if (ConnAugMatrix[i][j]==1) {
                            if (ConnAugMatrix[j][j] == 8) {
                                Atom Oxy = (Atom) m.getAtom(j);
                                if (Oxy.getFormalCharge()!=0)
                                    NpOm = true;
                            }
                        }
                    }
                }

                if (!NpOm)
                    tP = true;
            }

            // [-]
            if (CurAt.getFormalCharge() < 0)
                tN = true;
            
            // O
            if (CurAt.getSymbol().equalsIgnoreCase("O")) {
                    tA = true;
                
                if ( (CurAt.getFormalCharge() == 0) && (H == 1))
                    tD = true;
                
            }
                
            // N (NH2 and N without H)
            if (CurAt.getSymbol().equalsIgnoreCase("N")) {

                int nSglBnd = 0, nOtherBnd = 0;
                for (int j=0; j<nSK; j++) {
                    if (j==i) continue;
                    if (ConnAugMatrix[i][j]>0) {
                        if (ConnAugMatrix[i][j] == 1)
                            nSglBnd++;
                        else
                            nOtherBnd++;
                    }
                }
                
                if ( (CurAt.getFormalCharge() == 0) &&
                     (H == 2) &&
                     (nSglBnd == 1) &&
                     (nOtherBnd == 0) )   
                    tP = true;
                
                if (H == 0)
                    tA = true;
                
                if  ( (CurAt.getFormalCharge() == 0) &&( (H == 1) || (H ==2) ) )
                    tD = true;
                
            }
            
            // COOH, POOH, SOOH
            if ( ( (CurAt.getSymbol().equalsIgnoreCase("C")) ||
                   (CurAt.getSymbol().equalsIgnoreCase("S")) ||
                   (CurAt.getSymbol().equalsIgnoreCase("P")) ) &&
                  (CurAt.getFormalCharge() == 0) )  {
                
                int nSglBnd = 0, nDblO = 0, nSglOH = 0, nOtherBnd = 0;
                for (int j=0; j<nSK; j++) {
                    if (j==i) continue;
                    if (ConnAugMatrix[i][j]>0) {
                        if (ConnAugMatrix[i][j] == 1) {
                            nSglBnd++;
                            if (ConnAugMatrix[j][j] == 8) {
                                int Obonds = 0;
                                for (int k=0; k<nSK; k++) {
                                    if (k == j) continue;
                                    if (ConnAugMatrix[k][j]>0) Obonds++;
                                }
                                if (Obonds == 1) nSglOH++;
                            }
                        } else {
                            if ( (ConnAugMatrix[i][j] == 2) && (ConnAugMatrix[j][j] == 8) )
                                nDblO++;
                            else
                                nOtherBnd++;
                        }
                    }
                }
                
                if ( (nSglBnd == 2) && (nSglOH == 1) && (nDblO == 1) && (nOtherBnd == 0) )
                    tN = true;
            }
            
            if (CurAt.getSymbol().equalsIgnoreCase("Cl")) 
                tL = true;
            
            if (CurAt.getSymbol().equalsIgnoreCase("Br")) 
                tL = true;

            if (CurAt.getSymbol().equalsIgnoreCase("I")) 
                tL = true;
            
            if (CurAt.getSymbol().equalsIgnoreCase("C")) {
                boolean connOnlyToSingleC = true;
                for (int j=0; j<nSK; j++) {
                    if (j==i) continue;
                    if (ConnAugMatrix[i][j]>0) {
                        if ( (ConnAugMatrix[j][j] != 6) || (ConnAugMatrix[i][j] > 1.5) ) {
                            connOnlyToSingleC = false;
                            break;
                        }
                    }
                }                
                if (connOnlyToSingleC)
                    tL = true;
            }
            
            if (CurAt.getSymbol().equalsIgnoreCase("S")) {
                boolean connOnlyToSingleC = true;
                int nSingleC = 0;
                for (int j=0; j<nSK; j++) {
                    if (j==i) continue;
                    if (ConnAugMatrix[i][j]>0) {
                        if ( (ConnAugMatrix[j][j] != 6) || (ConnAugMatrix[i][j] != 1) ) {
                            connOnlyToSingleC = false;
                            break;
                        } else {
                            nSingleC++;
                        }
                    }
                }                
                if ( (connOnlyToSingleC) && (nSingleC == 2) )
                    tL = true;
            }
            
            
            // Sets final types
            if (tA) AtomTypes[i].add(TYPE_A);
            if (tN) AtomTypes[i].add(TYPE_N);
            if (tP) AtomTypes[i].add(TYPE_P);
            if (tD) AtomTypes[i].add(TYPE_D);
            if (tL) AtomTypes[i].add(TYPE_L);
            
        }
        
        return AtomTypes;
    }

    
    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        Cats2D block = new Cats2D();
        block.CloneDetailsFrom(this);
        return block;
    }

    
}
