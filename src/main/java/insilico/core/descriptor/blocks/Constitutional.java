package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.blocks.weights.basic.WeightsMass;
import insilico.core.descriptor.blocks.weights.iBasicWeight;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelectorCore;
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
//    private static final String BlockName = ;


    /**
     * Constructor. Sets by default MW calculation with scaled values.
     */
    public Constitutional() {
        super();
        this.Name = StringSelectorCore.getString("descriptors_constitutional_name");
    }

    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        this.Add("MW", StringSelectorCore.getString("descriptors_constitutional_mw"));
        this.Add("AMW", StringSelectorCore.getString("descriptors_constitutional_amw"));
        this.Add("Sv", StringSelectorCore.getString("descriptors_constitutional_sv"));
        this.Add("Mv", StringSelectorCore.getString("descriptors_constitutional_mv"));
        this.Add("Sp", StringSelectorCore.getString("descriptors_constitutional_sp"));
        this.Add("Mp", StringSelectorCore.getString("descriptors_constitutional_mp"));
        this.Add("Se", StringSelectorCore.getString("descriptors_constitutional_se"));
        this.Add("Me", StringSelectorCore.getString("descriptors_constitutional_me"));
        this.Add("Si", StringSelectorCore.getString("descriptors_constitutional_si"));
        this.Add("Mi", StringSelectorCore.getString("descriptors_constitutional_mi"));

        this.Add("nAt", StringSelectorCore.getString("descriptors_constitutional_nAt"));
        this.Add("nSk", StringSelectorCore.getString("descriptors_constitutional_nSk"));

        this.Add("nBt", StringSelectorCore.getString("descriptors_constitutional_nBt"));
        this.Add("nBo", StringSelectorCore.getString("descriptors_constitutional_nBo"));
        this.Add("nBm", StringSelectorCore.getString("descriptors_constitutional_nBm"));
        this.Add("nDB", StringSelectorCore.getString("descriptors_constitutional_nDB"));
        this.Add("nTB", StringSelectorCore.getString("descriptors_constitutional_nTB"));
        this.Add("nAB", StringSelectorCore.getString("descriptors_constitutional_nAB"));
        this.Add("SCBO", StringSelectorCore.getString("descriptors_constitutional_SCBO"));

        this.Add("nH", StringSelectorCore.getString("descriptors_constitutional_nH"));
        this.Add("nC", StringSelectorCore.getString("descriptors_constitutional_nC"));
        this.Add("nN", StringSelectorCore.getString("descriptors_constitutional_nN"));
        this.Add("nO", StringSelectorCore.getString("descriptors_constitutional_nO"));
        this.Add("nP", StringSelectorCore.getString("descriptors_constitutional_nP"));
        this.Add("nS", StringSelectorCore.getString("descriptors_constitutional_nS"));
        this.Add("nF", StringSelectorCore.getString("descriptors_constitutional_nF"));
        this.Add("nCl", StringSelectorCore.getString("descriptors_constitutional_nCl"));
        this.Add("nBr", StringSelectorCore.getString("descriptors_constitutional_nBr"));
        this.Add("nI", StringSelectorCore.getString("descriptors_constitutional_nI"));
        this.Add("nB", StringSelectorCore.getString("descriptors_constitutional_nB"));

        this.Add("HPerc", StringSelectorCore.getString("descriptors_constitutional_Hperc"));
        this.Add("CPerc", StringSelectorCore.getString("descriptors_constitutional_CPerc"));
        this.Add("NPerc", StringSelectorCore.getString("descriptors_constitutional_NPerc"));
        this.Add("OPerc", StringSelectorCore.getString("descriptors_constitutional_OPerc"));
        this.Add("XPerc", StringSelectorCore.getString("descriptors_constitutional_XPerc"));

        this.Add("nHet", StringSelectorCore.getString("descriptors_constitutional_NHet"));
        this.Add("nX", StringSelectorCore.getString("descriptors_constitutional_nX"));

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
            log.warn(String.format(StringSelectorCore.getString("descriptors_invalid_structure"), this.Name));
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
                        ws = new insilico.core.descriptor.blocks.weights.basic.WeightsVanDerWaals();
                        break;
                    case 2:
                        ws = new insilico.core.descriptor.blocks.weights.basic.WeightsPolarizability();
                        break;
                    case 3:
                        ws = new insilico.core.descriptor.blocks.weights.basic.WeightsElectronegativity();
                        break;
                    case 4:
                        ws = new insilico.core.descriptor.blocks.weights.basic.WeightsIonizationPotential();
                        break;
                    default:
                        throw new Exception(StringSelectorCore.getString("descriptors_weight_not_found"));
                }

                double[] weights = ws.getScaledWeights(curMol);
                double weightH = ws.getScaledWeight("H");

                double sum = 0;
                for (int i=0; i<nSK; i++) {
                    if (weights[i] == Descriptor.MISSING_VALUE) {
                        sum = Descriptor.MISSING_VALUE;
                        break;
                    } else {
                        // all values INCLUDING MW are scaled on carbon
                        sum += weights[i];
                        if (H[i] > 0)
                            sum += weightH * H[i];
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
                        throw new Exception(StringSelectorCore.getString("descriptors_weight_not_found"));
                }
            }

        } catch (Throwable e) {
            log.warn(String.format(StringSelectorCore.getString("descriptors_unable_calculate"), this.Name, e.getMessage()));
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
