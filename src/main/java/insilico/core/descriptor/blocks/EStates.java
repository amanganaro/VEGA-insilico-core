package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.weight.EState;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.tools.logger.InsilicoLogger;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;


/**
 * E-States descriptors.
 * 
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class EStates extends DescriptorBlock {

    private static final long serialVersionUID = 1L;
    private final static String BlockName = "E-States Descriptors";
    
    
    /**
     * Constructor. This should not be used, no weight is specified. The 
     * overloaded constructors should be used instead.
     */    
    public EStates() {
        super();
        this.Name = EStates.BlockName;
    }
    

    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        Add("SsCl","");
        Add("SsBr","");
        Add("SsF","");
        Add("SsI","");
        Add("SdssC","");
        Add("SsssN","");
        Add("StN","");
        Add("SsSH","");
        Add("SdssNp","");  
        Add("StsC", "");
        Add("SsOm", "");
        
        Add("Ss", "sum of e-states (no H)");
        Add("Ms", "mean of e-states (no H)");
        
        Add("SHssNH","");

        Add("Hmax",""); // max HE-state
        Add("Hmin",""); // min HE-state
        Add("Gmax",""); // max E-state
        Add("Gmin",""); // min E-state
        
        Add("Qs", ""); // Qs from TEST software
        Add("Qv", ""); // Qv from TEST software
        Add("Qsv", ""); // Qsv from TEST software
        
        SetAllValues(Descriptor.MISSING_VALUE);
    }


    /**
     * Calculate descriptors for the given molecule.
     * 
     * @param mol molecule to be calculated
     */
    @Override
    public void Calculate(InsilicoMolecule mol) {

        // Generate/clear descriptors
        GenerateDescriptors();

        IAtomContainer m;
        try {
            m = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        int nSK = m.getAtomCount();
        
        // Get EStates weights
        EState es;
        try {
            es = new EState(mol.GetStructure());
        } catch (Exception e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }
        
        // Calculation
        double SsCl=0, SsF=0, SsBr=0, SsI=0;
        double StN=0, SsssN=0, SsSH=0;
        double SdssC=0, SHssNH=0, SdssNp=0;
        double StsC=0, SsOm=0;
        
        double Hmax= Descriptor.MISSING_VALUE, Hmin= Descriptor.MISSING_VALUE;
        double Gmax= Descriptor.MISSING_VALUE, Gmin= Descriptor.MISSING_VALUE;
        
        double Ss = 0, Ms = 0;

        for (int at=0; at<m.getAtomCount(); at++) {
            
            IAtom curAt = m.getAtom(at);
            
            // Count H
            int nH = 0;
            try {
                nH = curAt.getImplicitHydrogenCount();
            } catch (Exception e) {
                InsilicoLogger.getLogger().warn("unable to get H count");
            }
            
            // formal charge
            int Charge;
            try {
                Charge = curAt.getFormalCharge();
            } catch (Exception e) {
                Charge = 0;
            }                    
            
            // Count bonds
            int nBnd=0, nSng = 0, nDbl = 0, nTri = 0, nAr=0;
            for (IBond b : m.getConnectedBondsList(curAt)) {
                if (b.getFlag(CDKConstants.ISAROMATIC)) {
                    nAr++;
                    nBnd++;
                    continue;
                }
                if (b.getOrder() == IBond.Order.SINGLE) {
                    nSng++;
                    nBnd++;
                }
                if (b.getOrder() == IBond.Order.DOUBLE) {
                    nDbl++;
                    nBnd++;
                }
                if (b.getOrder() == IBond.Order.TRIPLE) {
                    nTri++;
                    nBnd++;
                }
            }            
            
            // Sum of e-states
            Ss += es.getEState()[at];
            
            // Maximum and minimum Estate/HEstate
            Gmax = (Gmax== Descriptor.MISSING_VALUE) ? es.getEState()[at] : (Math.max(es.getEState()[at], Gmax));
            Gmin = (Gmin== Descriptor.MISSING_VALUE) ? es.getEState()[at] : (Math.min(es.getEState()[at], Gmin));
            if (nH>0) {
                Hmax = (Hmax== Descriptor.MISSING_VALUE) ? es.getHEState()[at] : (Math.max(es.getHEState()[at], Hmax));
                Hmin = (Hmin== Descriptor.MISSING_VALUE) ? es.getHEState()[at] : (Math.min(es.getHEState()[at], Hmin));
            }
                            
            
            // Halo atoms
            if (curAt.getSymbol().equalsIgnoreCase("Cl")) {
                SsCl += es.getEState()[at];
            }
            
            if (curAt.getSymbol().equalsIgnoreCase("Br")) {
                SsBr += es.getEState()[at];
            }
            
            if (curAt.getSymbol().equalsIgnoreCase("F")) {
                SsF += es.getEState()[at];
            }
            
            if (curAt.getSymbol().equalsIgnoreCase("I")) {
                SsI += es.getEState()[at];
            }

            
            // C Groups
            if (curAt.getSymbol().equalsIgnoreCase("C")) {
   
                if ((nBnd == 3) && (nDbl == 1) && (nSng == 2))
                    SdssC += es.getEState()[at];
                
                if ((nBnd == 2) && (nTri == 1) && (nSng == 1))
                    StsC += es.getEState()[at];
                
            }
            
            
            // C Groups
            if (curAt.getSymbol().equalsIgnoreCase("O")) {
   
                if ((nBnd == 1) && (nSng == 1) && (Charge == -1))
                    SsOm += es.getEState()[at];
                
            }
            
            
            // N Groups
            if (curAt.getSymbol().equalsIgnoreCase("N")) {
   
                if ((nBnd == 1) && (nTri == 1))
                    StN += es.getEState()[at];
                
                if ((nBnd == 3) && (nSng == 3))
                    SsssN += es.getEState()[at];
                
                if ((nBnd == 2) && (nSng == 2) && (nH == 1))
                    SHssNH += es.getHEState()[at];
                
                if ((nBnd == 3) && (nSng == 2) && (nDbl == 1) && (Charge == 1))
                    SdssNp += es.getEState()[at];                
            }

            
            // S groups
            if (curAt.getSymbol().equalsIgnoreCase("S")) {
                
                if ((nBnd == 1) && (nSng == 1) && (nH == 1))
                    SsSH += es.getEState()[at];
            }

        }            
          
        
        // Qs, Qv and Qsv from TEST software
        
        double []IS = es.getIS();
        double sumIalk = 0.0D, sumI = 0.0D, sumImax = 0.0D;
        for (int i = 0; i < nSK; i++) {
     
            double DV = m.getConnectedBondsCount(i);
            double D = DV;
      
            sumIalk += (DV + 1.0D) / D;
            sumI += IS[i];
            if (DV == 1.0D) {
                sumImax += 8.0D;
            } else if (DV == 2.0D) {
                sumImax += 3.5D;
            } else if (DV == 3.0D) {
                sumImax += 2.0D;
            } else if (DV == 4.0D) {
                sumImax += 1.25D;
            } else {
                sumImax += IS[i];
            }
        }
        
        double Qs = (Math.pow(nSK / sumI, 2.0D) * sumIalk);
        double Qv = (sumIalk * sumImax / (sumI * sumI));
        double sumIave = (sumIalk + sumImax) / 2.0D;
        double Qsv = (sumIave * sumIalk / (sumI * sumI));
 
        
        Ms = Ss / m.getAtomCount();
        SetByName("Ss", Ss);
        SetByName("Ms", Ms);

        SetByName("SsCl", SsCl);
        SetByName("SsBr", SsBr);
        SetByName("SsF", SsF);
        SetByName("SsI", SsI);
        SetByName("SdssC", SdssC);
        SetByName("SsSH", SsSH);
        SetByName("SsssN", SsssN);
        SetByName("StN", StN);
        SetByName("SdssNp", SdssNp);
        SetByName("StsC", StsC);
        SetByName("SsOm", SsOm);

        SetByName("SHssNH", SHssNH);

        SetByName("Gmax", Gmax);
        SetByName("Gmin", Gmin);
        SetByName("Hmax", Hmax);
        SetByName("Hmin", Hmin);

        SetByName("Qs", Qs);
        SetByName("Qv", Qv);
        SetByName("Qsv", Qsv);
        
    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException 
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        EStates block = new EStates();
        block.CloneDetailsFrom(this);
        return block;
    }

}
