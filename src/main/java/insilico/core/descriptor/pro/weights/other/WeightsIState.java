package insilico.core.descriptor.pro.weights.other;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.pro.weights.iWeight;
import insilico.core.descriptor.weight.ValenceVertexDegree;
import insilico.core.exception.GenericFailureException;
import insilico.core.tools.utils.MoleculeUtilities;
import org.openscience.cdk.Atom;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

/**
 *  Intrinsic State.
 *  Calculated for each atom as I = ( (2/L)^2 * delta_v + 1 ) / delta
 *  where:
 *  L is the principal quantum number of the atom
 *  delta_v is the valence vertex degree of the atom
 *  delta is the number of sigma electrons (simple vertex degree) of the atom
 *
 */
public class WeightsIState implements iWeight {

    private static final String SYMBOL = "s";
    private static final String NAME = "I-state";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }


    public double[] getWeights(IAtomContainer mol, boolean HasExplicitH)  {

        int nSK = mol.getAtomCount();
        double[] w = new double[nSK];

        for (int i=0; i<nSK; i++)
            w[i] = getWeight(mol, i, HasExplicitH);

        return w;
    }


    public double getWeight(IAtomContainer mol, int atomIndex, boolean HasExplicitH) {

        IAtom a = mol.getAtom(atomIndex);

        //// Quantum number / period of the atom
        int L = (new WeightsQuantumNumber()).getWeight(a.getSymbol()) ;


        //// Vertex degree of the atom
        double D = 0;
        if (HasExplicitH) {
            int nH = 0;
            for (IAtom conn : mol.getConnectedAtomsList(a))
                if (a.getAtomicNumber() == 1)
                    nH++;
            D = mol.getConnectedBondsCount(a) - nH;
        } else {
            D = mol.getConnectedBondsCount(a);
        }


        //// Valence vertex degree of the atom
        double DeltaV = 0;

        // sum of bond order (including aromatic bonds as 1.5)
        double count = 0;
        for (IBond bond : mol.getConnectedBondsList(a))
            count += MoleculeUtilities.Bond2Double(bond);

        // Init deltaV (depending on presence of explicit H)
        if (HasExplicitH) {
            int nH = 0;
            for (IAtom conn : mol.getConnectedAtomsList(a))
                if (a.getAtomicNumber() == 1)
                    nH++;
            DeltaV = count - nH;
        } else {
            DeltaV = count;
        }

        String symbol = a.getSymbol();
        int charge = a.getFormalCharge();

        if (symbol.equals("C") || symbol.equals("Si")
                || symbol.equals("Pb") || symbol.equals("Sn"))
        {

            // TODO: I am not sure if following block is needed:
            if ((int) (D) != (int) (0.5 + DeltaV)) {

                //used for carbons with fractional aromatic bond order
                int ID = (int) D;

                switch (ID) {
                    case 1: { // 41
                        if ((int) (DeltaV + 0.5) != 2)
                            DeltaV = 3.0;
                        break;
                    }
                    case 2: { // 42
                        if ((int) (DeltaV + 0.5) != 3)
                            DeltaV = 4.0;
                        break;
                    }
                    case 3: { // 43
                        if (DeltaV >= 4)
                            DeltaV = 4.0;
                        break;
                    }
                }
            }

        } else if (symbol.equals("O")) { // 50 (oxygen)

            if (DeltaV > 1)
                DeltaV = 6.0;
            else
                DeltaV = 5.0;

        } else if (symbol.equals("N")) { // 60 (nitrogen)

            int ID = (int) DeltaV;
            switch (ID) {
                case 1: // 61-sp3
                {
                    DeltaV = DeltaV + 2.;
                    break;
                }
                case 2: // 62-sp2
                {
                    if ((int) (DeltaV + 0.5) == 2)
                        DeltaV = 4.;
                    else
                        DeltaV = 5.;
                    break;
                }
                case 3: // 63-sp
                {
                    if ((int) (DeltaV + 0.5) == 3 || (int) (DeltaV + 0.5) == 4)
                        DeltaV = DeltaV + 2.;
                    else
                        DeltaV = 5.;
                    break;
                }
            }

        } else if (symbol.equals("S")) { // 70 (sulfur)

            double h,Zv;
            Zv=6;
            if (DeltaV==1)
                h=1;
            else
                h=0;
            DeltaV=Zv-h;

        } else if (symbol.equals("P") || symbol.equals("As")) {

            double h,Zv;
            Zv=5;

            if (DeltaV==1)
                h=2;
            else if (DeltaV==2)
                h=1;
            else if (DeltaV==4)
                h=1;
            else
                h=0;
            DeltaV=Zv-h;

        } else { // F,Cl,Br,I, Hg

            double h = 0; // assume number of hydrogens = 0
            double Zv = ValenceVertexDegree.GetValenceElectronsNumber(symbol);
            if (Zv == Descriptor.MISSING_VALUE)
                return Descriptor.MISSING_VALUE;

            DeltaV = (Zv - h);

        }

        // modify based on charge
        DeltaV += -charge;


        //// I-State
        double IS = (Math.pow(2.0 / L, 2.0) * DeltaV + 1) / D;
        return IS;
    }
}
