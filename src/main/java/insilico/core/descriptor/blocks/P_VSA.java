package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.blocks.weights.basic.*;
import insilico.core.descriptor.blocks.weights.other.WeightsHydrophobicityGC;
import insilico.core.descriptor.blocks.weights.other.WeightsIState;
import insilico.core.descriptor.blocks.weights.other.WeightsMolarRefractivityGC;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.acf.GhoseCrippenACF;
import insilico.core.molecule.tools.Manipulator;
import insilico.core.tools.utils.MoleculeUtilities;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;

/**
 * P_VSA descriptors.
 * Calculates P_VSA descriptors
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class P_VSA extends DescriptorBlock {

    private final static long serialVersionUID = 1L;
    private final static String BlockName = "P_VSA Descriptors";

    private final static String[] W_LOGP = { "LogP", "LogP" };
    private final static String[] W_MOLAR_REFRACTIVITY = { "MR", "molar refractivity"};
    private final static String[] W_MASS = {(new WeightsMass()).getSymbol(), (new WeightsMass()).getName()};
    private final static String[] W_VDW = {(new WeightsVanDerWaals()).getSymbol(), (new WeightsVanDerWaals()).getName()};
    private final static String[] W_ELECTRONEGATIVITY = {(new WeightsElectronegativity()).getSymbol(), (new WeightsElectronegativity()).getName()};
    private final static String[] W_POLARIZABILITY = {(new WeightsPolarizability()).getSymbol(), (new WeightsPolarizability()).getName()};
    private final static String[] W_IONIZATION = {(new WeightsIonizationPotential()).getSymbol(), (new WeightsIonizationPotential()).getName()};
    private final static String[] W_I_STATE = {(new WeightsIState()).getSymbol(), (new WeightsIState()).getName()};

    private final static String[][] WEIGHTS = {
            W_LOGP,
            W_MOLAR_REFRACTIVITY,
            W_MASS,
            W_IONIZATION
    };

    private final static Object[][] RefBondLengths = {
        {"Br","Br",2.54},
        {"Br","C",1.97},
        {"Br","Cl",2.36},
        {"Br","F",1.85},
        {"Br","H",1.44},
        {"Br","I",2.65},
        {"Br","N",1.84},
        {"Br","O",1.58},
        {"Br","P",2.37},
        {"Br","S",2.21},
        {"C","C",1.54},
        {"C","Cl",1.8},
        {"C","F",1.35},
        {"C","H",1.06},
        {"C","I",2.12},
        {"C","N",1.47},
        {"C","O",1.43},
        {"C","P",1.85},
        {"C","S",1.81},
        {"Cl","Cl",2.31},
        {"Cl","F",1.63},
        {"Cl","H",1.22},
        {"Cl","I",2.56},
        {"Cl","N",1.74},
        {"Cl","O",1.41},
        {"Cl","P",2.01},
        {"Cl","S",2.07},
        {"F","F",1.28},
        {"F","H",0.87},
        {"F","I",2.04},
        {"F","N",1.41},
        {"F","O",1.32},
        {"F","P",1.5},
        {"F","S",1.64},
        {"H","I",1.63},
        {"H","N",1.01},
        {"H","O",0.97},
        {"H","P",1.41},
        {"H","S",1.31},
        {"I","I",2.92},
        {"I","N",2.26},
        {"I","O",2.14},
        {"I","P",2.49},
        {"I","S",2.69},
        {"N","N",1.45},
        {"N","O",1.46},
        {"N","P",1.6},
        {"N","S",1.76},
        {"O","O",1.47},
        {"O","P",1.57},
        {"O","S",1.57},
        {"P","P",2.26},
        {"P","S",2.07},
        {"S","S",2.05}        
    };
    
    
    
    /**
     * Constructor. This should not be used, no weight is specified. The 
     * overloaded constructors should be used instead.
     */    
    public P_VSA() {
        super();
        this.Name = P_VSA.BlockName;
    }

    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        for (String[] curWeight : WEIGHTS) {
            int bins = GetBinSize(curWeight[0]);
            for (int i=1; i<= bins; i++)
                Add("P_VSA_" + curWeight[0] + "_" + i, "P_VSA-like on " + curWeight[1] + ", bin " + i);
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

        // P_VSA are calculated on H filled molecules
        IAtomContainer m;
        try {
            IAtomContainer orig_m = mol.GetStructure();
            m = Manipulator.AddHydrogens(orig_m);
        } catch (InvalidMoleculeException | GenericFailureException e) {
            log.warn("Invalid structure, unable to calculate: " + this.Name);
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
            
        int nSK = m.getAtomCount();

        GhoseCrippenACF GC = new GhoseCrippenACF(m, true);
        int[] ACF = GC.GetACF();
        
        // Calculate VSA
        double[] VSA = new double[nSK];
        
        for (int i=0; i<nSK; i++) {

            IAtom at = m.getAtom(i);

            // R - van der waals radius
            double vdwR = GetVdWRadius(m, at);
            if (vdwR == Descriptor.MISSING_VALUE) continue;

            double coef = 0;
            for (IAtom connAt : m.getConnectedAtomsList(at)) {

                double connAt_vdwR = GetVdWRadius(m, connAt);
                if (connAt_vdwR == Descriptor.MISSING_VALUE) continue;

                // refR - reference bond length
                double refR = this.GetRefBondLength(at, connAt);
                if (refR == Descriptor.MISSING_VALUE) continue;

                // c - correction
                double c = 0;
                double bnd = MoleculeUtilities.Bond2Double(m.getBond(at, connAt));
                if (bnd == 1.5) c = 0.1;
                if (bnd == 2) c = 0.2;
                if (bnd == 3) c = 0.3;

                double g_1 = Math.max(  Math.abs(vdwR - connAt_vdwR) , GetRefBondLength(at, connAt) - c ) ;
                double g_2 = vdwR + connAt_vdwR;
                double g  = Math.min(g_1, g_2) ;

                coef += ( Math.pow(connAt_vdwR,2) - Math.pow( (vdwR - g), 2) ) / (g) ;
            }

            VSA[i] = ( 4.0 * Math.PI * Math.pow(vdwR,2) ) - Math.PI * vdwR * coef ;
        }


        // Cycle for all found weighting schemes
        for (String[] curWeight : WEIGHTS) {

            // Sets needed weights
            double[] w = null;

            if (curWeight[0].equalsIgnoreCase(W_LOGP[0]))
                w = (new WeightsHydrophobicityGC()).getWeightsForFragmentId(ACF);
            else  if (curWeight[0].equalsIgnoreCase(W_MOLAR_REFRACTIVITY[0]))
                w = (new WeightsMolarRefractivityGC()).getWeightsForFragmentId(ACF);
            else if (curWeight[0].equalsIgnoreCase(W_MASS[0]))
                w = (new WeightsMass()).getScaledWeights(m);
            else if (curWeight[0].equalsIgnoreCase(W_IONIZATION[0]))
                w = (new WeightsIonizationPotential()).getScaledWeights(m);

            int bins = GetBinSize(curWeight[0]);
            
            for (int b=1; b<=bins; b++) {
                
                double PVSA = 0;
                for (int i=0; i<nSK; i++) {
                    if (w[i] == Descriptor.MISSING_VALUE) continue;
                    if ( b == CalculateBin(curWeight[0], w[i]))
                        PVSA += VSA[i];
                }

                SetByName("P_VSA_" + curWeight[0] + "_" + b, PVSA);
            }
        }
    }


    private int GetBinSize(String curWeight) {

        if (curWeight.equalsIgnoreCase(W_LOGP[0]))
            return 8;
        if (curWeight.equalsIgnoreCase(W_MOLAR_REFRACTIVITY[0]))
            return 8;
        if (curWeight.equalsIgnoreCase(W_MASS[0]))
            return 5;
        if (curWeight.equalsIgnoreCase(W_IONIZATION[0]))
            return 4;
        return 0;
    }


    private int CalculateBin(String curWeight, double value) {

        if (curWeight.equalsIgnoreCase(W_LOGP[0])) {
            // LOGP
            if (value < -1.5) return 1;
            if (value < -0.5) return 2;
            if (value < -0.25) return 3;
            if (value < 0) return 4;
            if (value < 0.25) return 5;
            if (value < 0.52) return 6;
            if (value < 0.75) return 7;
            return 8;
        }

        if (curWeight.equalsIgnoreCase(W_MOLAR_REFRACTIVITY[0])) {
            // Molar Refractivity
            if (value < 0.9) return 1;
            if (value < 1.5) return 2;
            if (value < 2.0) return 3;
            if (value < 2.5) return 4;
            if (value < 3.0) return 5;
            if (value < 4.0) return 6;
            if (value < 6.0) return 7;
            return 8;
        }

        if (curWeight.equalsIgnoreCase(W_MASS[0])) {
            // Mass
            if (value < 1) return 1;
            if (value < 1.2) return 2;
            if (value < 1.6) return 3;
            if (value < 3) return 4;
            return 5;
        }

        if (curWeight.equalsIgnoreCase(W_IONIZATION[0])) {
            // Ionization potential
            if (value < 1) return 1;
            if (value < 1.15) return 2;
            if (value < 1.25) return 3;
            return 4;
        }

        return 0;
    }

    
    private boolean AtomCouple (IAtom at1, IAtom at2, String symbol1, String symbol2) {
        if ( (at1.getSymbol().equalsIgnoreCase(symbol1)) && (at2.getSymbol().equalsIgnoreCase(symbol2)))
            return true;
        if ( (at1.getSymbol().equalsIgnoreCase(symbol2)) && (at2.getSymbol().equalsIgnoreCase(symbol1)))
            return true;
        return false;
    }
    
    
    private double GetRefBondLength(IAtom at1, IAtom at2) {
        double len = Descriptor.MISSING_VALUE;
        for (int i=0; i<RefBondLengths.length; i++) {
            if (AtomCouple(at1, at2, (String)RefBondLengths[i][0], (String)RefBondLengths[i][1])) {
                len = (Double)RefBondLengths[i][2];
                break;
            }
        }
        return len;
    }
    
    
    private double GetVdWRadius(IAtomContainer m, IAtom at) {
        
        String s = at.getSymbol();
        if (s.equalsIgnoreCase("C")) 
            return 1.950;
        if (s.equalsIgnoreCase("N")) 
            return 1.950;
        if (s.equalsIgnoreCase("F")) 
            return  1.496;
        if (s.equalsIgnoreCase("P")) 
            return  2.287;
        if (s.equalsIgnoreCase("S")) 
            return  2.185;
        if (s.equalsIgnoreCase("Cl")) 
            return  2.044;
        if (s.equalsIgnoreCase("Br")) 
            return  2.166;
        if (s.equalsIgnoreCase("I")) 
            return 2.358;
        
        if (s.equalsIgnoreCase("O")) {
            
            // oxide
            if (m.getConnectedAtomsList(at).size() == 1)
                return 1.810;
            
            // acid
            if (Manipulator.CountImplicitHydrogens(at) > 0)
                return 2.152;
            
            return 1.779;
        }
        
        if (s.equalsIgnoreCase("H")) {
            IAtom connAt = m.getConnectedAtomsList(at).get(0);
            if (connAt.getSymbol().equalsIgnoreCase("0"))
                return 0.8;
            if (connAt.getSymbol().equalsIgnoreCase("N"))
                return 0.7;
            if (connAt.getSymbol().equalsIgnoreCase("P"))
                return 0.7;
            return 1.485;
        }
        
        return Descriptor.MISSING_VALUE;
    }
    
    
    
    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        P_VSA block = new P_VSA();
        block.CloneDetailsFrom(this);
        return block;
    }

    
}
