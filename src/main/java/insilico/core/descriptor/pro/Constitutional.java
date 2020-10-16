package insilico.core.descriptor.pro;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.pro.weights.basic.*;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

/**
 * Constitutional descriptors block.<p>
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class Constitutional extends DescriptorBlock {

    private static final long serialVersionUID = 1L;
    private static final String BlockName = "Constitutional Descriptors";



    /**
     * Constructor. Sets by default MW calculation with scaled values.
     */
    public Constitutional() {
        super();
        this.Name = Constitutional.BlockName;
    }



    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        this.Add("MW", "molecular weight");
        this.Add("AMW", "average molecular weight");
        this.Add("Sv", "sum of atomic van der Waals volumes (scaled on Carbon atom)");
        this.Add("Mv", "mean atomic van der Waals volume (scaled on Carbon atom)");
        this.Add("Sp", "sum of atomic polarizabilities (scaled on Carbon atom)");
        this.Add("Mp", "mean atomic polarizability (scaled on Carbon atom)");
        this.Add("Se", "sum of atomic Sanderson electronegativities (scaled on Carbon atom)");
        this.Add("Me", "mean atomic Sanderson electronegativity (scaled on Carbon atom)");
        this.Add("Si", "sum of atomic ionic potential (scaled on Carbon atom)");
        this.Add("Mi", "mean atomic ionic potential (scaled on Carbon atom)");

        this.Add("nAt", "number of atoms");
        this.Add("nSk", "number of non-H atoms");

        this.Add("nBt", "number of bonds (total)");
        this.Add("nBo", "number of non-H bonds");
        this.Add("nBm", "number of multiple bonds");
        this.Add("nDB", "number of double bonds");
        this.Add("nTB", "number of triple bonds");
        this.Add("nAB", "number of aromatic bonds");
        this.Add("SCBO", "sum of conventional bond orders (H-depleted)");

        this.Add("nH", "number of Hydrogen atoms");
        this.Add("nC", "number of Carbon atoms");
        this.Add("nN", "number of Nitrogen atoms");
        this.Add("nO", "number of Oxygen atoms");
        this.Add("nP", "number of Phosphorous atoms");
        this.Add("nS", "number of Sulfur atoms");
        this.Add("nF", "number of Fluorine atoms");
        this.Add("nCl", "number of Chlorine atoms");
        this.Add("nBr", "number of Bromine atoms");
        this.Add("nI", "number of Iodine atoms");
        this.Add("nB", "number of Boron atoms");

        this.Add("HPerc", "percentage of H atoms");
        this.Add("CPerc", "percentage of C atoms");
        this.Add("NPerc", "percentage of N atoms");
        this.Add("OPerc", "percentage of O atoms");
        this.Add("XPerc", "percentage of halogen atoms");

        this.Add("nHet", "number of heteroatoms");
        this.Add("nX", "number of halogen atoms");

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

        try {

            int nSK = curMol.getAtomCount();
            int nBO = curMol.getBondCount();
            int[] H = new int[nSK];

            int nTotH=0;
            int nC=0, nN=0, nO=0, nP=0, nS=0;
            int nI=0, nF=0, nCl=0, nBr=0, nB=0;
            int nHet=0;


            //// Counts on atoms

            for (int i=0; i<nSK; i++) {

                IAtom CurAt = curMol.getAtom(i);

                // Hydrogens
                H[i] = 0;
                try {
                    H[i] = CurAt.getImplicitHydrogenCount();
                } catch (Exception e) { }
                nTotH += H[i];


                if (CurAt.getSymbol().equalsIgnoreCase("C"))
                    nC++;
                else
                    nHet++;

                if (CurAt.getSymbol().equalsIgnoreCase("N"))
                    nN++;
                if (CurAt.getSymbol().equalsIgnoreCase("O"))
                    nO++;
                if (CurAt.getSymbol().equalsIgnoreCase("P"))
                    nP++;
                if (CurAt.getSymbol().equalsIgnoreCase("S"))
                    nS++;
                if (CurAt.getSymbol().equalsIgnoreCase("F"))
                    nF++;
                if (CurAt.getSymbol().equalsIgnoreCase("Cl"))
                    nCl++;
                if (CurAt.getSymbol().equalsIgnoreCase("Br"))
                    nBr++;
                if (CurAt.getSymbol().equalsIgnoreCase("I"))
                    nI++;
                if (CurAt.getSymbol().equalsIgnoreCase("B"))
                    nB++;

            }

            this.SetByName("nAt", nSK + nTotH);
            this.SetByName("nSk", nSK);

            this.SetByName("nH", nTotH);
            this.SetByName("nC", nC);
            this.SetByName("nN", nN);
            this.SetByName("nO", nO);
            this.SetByName("nP", nP);
            this.SetByName("nS", nS);
            this.SetByName("nF", nF);
            this.SetByName("nCl", nCl);
            this.SetByName("nBr", nBr);
            this.SetByName("nI", nI);
            this.SetByName("nB", nB);

            this.SetByName("HPerc", (nTotH/(double)(nSK + nTotH))*100);
            this.SetByName("CPerc", (nC/(double)(nSK + nTotH))*100);
            this.SetByName("NPerc", (nN/(double)(nSK + nTotH))*100);
            this.SetByName("OPerc", (nO/(double)(nSK + nTotH))*100);
            this.SetByName("XPerc", ((nI + nF + nCl + nBr)/(double)(nSK + nTotH))*100);

            this.SetByName("nHet", nHet);
            this.SetByName("nX", nI + nF + nCl + nBr);


            //// Counts on bonds

            int nArBonds=0, nDblBonds=0, nTrpBonds=0, nMulBonds=0;
            double scbo=0;

            for (int i=0; i<nBO; i++) {

                IBond CurBo = curMol.getBond(i);

                if (CurBo.getFlag(CDKConstants.ISAROMATIC)) {
                    nArBonds++;
                    nMulBonds++;
                    scbo += 1.5;
                } else {
                    if (CurBo.getOrder() == IBond.Order.SINGLE) {
                        scbo++;
                    } else {
                        nMulBonds++;
                        if (CurBo.getOrder() == IBond.Order.DOUBLE) {
                            nDblBonds++;
                            scbo += 2;
                        }
                        if (CurBo.getOrder() == IBond.Order.TRIPLE) {
                            nTrpBonds++;
                            scbo += 3;
                        }
                    }
                }

            }

            this.SetByName("nBt", nBO + nTotH);
            this.SetByName("nBo", nBO);
            this.SetByName("nBm", nMulBonds);
            this.SetByName("nDB", nDblBonds);
            this.SetByName("nTB", nTrpBonds);
            this.SetByName("nAB", nArBonds);
            this.SetByName("SCBO", scbo);


            for (int w=0; w<5; w++) {

                iBasicWeight ws;
                switch (w) {
                    case 0:
                        ws = new WeightsMass();
                        break;
                    case 1:
                        ws = new WeightsVanDerWaals();
                        break;
                    case 2:
                        ws = new WeightsPolarizability();
                        break;
                    case 3:
                        ws = new WeightsElectronegativity();
                        break;
                    case 4:
                        ws = new WeightsIonizationPotential();
                        break;
                    default:
                        throw new Exception("Weight not found");
                }

                double[] weights = ws.getWeights(curMol);
                double weightH = ws.getWeight("H");
                double weightC = ws.getWeight("C");

                double sum = 0;
                for (int i=0; i<nSK; i++) {
                    if (weights[i] == Descriptor.MISSING_VALUE) {
                        sum = Descriptor.MISSING_VALUE;
                        break;
                    } else {
                        if (w == 0) {
                            // weight mass -> uses original values
                            sum += weights[i];
                            if (H[i] > 0)
                                sum += weightH * H[i];
                        } else {
                            // all other weights -> use carbon-scaled values
                            sum += weights[i] / weightC;
                            if (H[i] > 0)
                                sum += (weightH / weightC) * H[i];
                        }
                    }
                }

                double ave = Descriptor.MISSING_VALUE;
                if (sum != Descriptor.MISSING_VALUE)
                    ave = sum/(nSK + nTotH);

                switch (w) {
                    case 0:
                        this.SetByName("MW", sum);
                        this.SetByName("AMW", ave);
                        break;
                    case 1:
                        this.SetByName("Sv", sum);
                        this.SetByName("Mv", ave);
                        break;
                    case 2:
                        this.SetByName("Sp", sum);
                        this.SetByName("Mp", ave);
                        break;
                    case 3:
                        this.SetByName("Se", sum);
                        this.SetByName("Me", ave);
                        break;
                    case 4:
                        this.SetByName("Si", sum);
                        this.SetByName("Mi", ave);
                        break;
                    default:
                        throw new Exception("Weight not found");
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
    public DescriptorBlock CreateClone()
            throws CloneNotSupportedException {
        Constitutional block = new Constitutional();
        block.CloneDetailsFrom(block);
        return block;
    }
}
