package insilico.core.molecule.acf;

import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.RingSet;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IRing;

import java.util.ArrayList;

/**
 * Builds the list of all atom centered fragments (ACF) of the user given lag.
 * Fragments are produced as SMILES string.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ACFBuilder {

    private int ACF_LAG;

    private IAtomContainer Mol;
    private RingSet MolRings;
    private double[][] ConnMatrix;

    public boolean DoNotSplitRings;

    /**
     * Constructor.
     */
    public ACFBuilder() {
        this(1); // Default Lag = 1
    }


    /**
     * Constructor.
     * @param LagOrder the lag for the ACF
     */
    public ACFBuilder(int LagOrder) {
        Mol = null;
        ConnMatrix = null;
        ACF_LAG = LagOrder;
        DoNotSplitRings = true;
    }

    /**
     * Creates the list of ACF for the given molecule
     *
     * @param mol molecule to be processed
     * @return the ACF list
     */
    public ACFItemList CreateList(InsilicoMolecule mol) throws InvalidMoleculeException {

        String[] CurACF = Calculate(mol);
        ArrayList<ACFItem> ACFList = new ArrayList<>();

        for (int k = 0; k < CurACF.length; k++) {
            boolean ToBeAdded = true;
            for (int z = 0; z < ACFList.size(); z++)
                if (CurACF[k].compareTo(ACFList.get(z).getACF()) == 0) {
                    ACFList.get(z).setFrequency(ACFList.get(z).getFrequency() + 1);
                    ToBeAdded = false;
                    break;
                }
            if (ToBeAdded)
                ACFList.add(new ACFItem(CurACF[k], 1));
        }

        ACFItemList FinalList = new ACFItemList();
        for (ACFItem ai : ACFList)
            FinalList.AddItem(ai);

        return FinalList;
    }

    private String[] Calculate(InsilicoMolecule mol) throws InvalidMoleculeException {

        Mol = mol.GetStructure();
        ArrayList <String>ACFList = new ArrayList<>();

        // Builds needed molecule objects matrix
//        ConnMatrix = ConnectionMatrix.getMatrix(Mol);
        try {
            ConnMatrix = mol.GetMatrixConnectionAugmented();
        } catch (GenericFailureException ex) {
            throw new InvalidMoleculeException("unable to build connection matrix");
        }
        MolRings = mol.GetSSSR();

        // Cycles upon all atoms
        for (IAtom atom : Mol.atoms()) {

            int A = Mol.getAtomNumber(atom);

            // Skips Hydrogen atoms
            if (atom.getSymbol().compareToIgnoreCase("H")==0)
                continue;

            // Skips atoms in ring
            if (DoNotSplitRings)
                if (MolRings.contains(atom))
                    continue;

            // Builds current ACF
            ACFList.add(RecursiveSearchACF(A, A, ACF_LAG));

        }

        String[] Results = new String[0];
        Results = ACFList.toArray(Results);
        return Results;

    }

    private String RecursiveSearchACF(int PrevAtom, int AtomIdx, int Lag) {

        String ACFChunk = "";


        //// Builds ACF string for current center atom
        IAtom CenterAtom = Mol.getAtom(AtomIdx);

        if (DoNotSplitRings)
            if (MolRings.contains(CenterAtom)) {
                ACFChunk += GenerateRingSMILESFromAtom(CenterAtom);
                return ACFChunk;
            }

        // Atom Type:
        if (CenterAtom.getFlag(CDKConstants.ISAROMATIC))
            ACFChunk += "[" + CenterAtom.getSymbol().toLowerCase();
        else
            ACFChunk += "[" + CenterAtom.getSymbol();
        ACFChunk += "]";


        //// Checks if is the last atom
        if (Lag == 0)
            return ACFChunk;


        //// Builds chunks for next atoms
        ArrayList <String>NextChunks = new ArrayList<String>();
        String CurBond="", CurChunk="";
        for (int i=0; i<Mol.getAtomCount(); i++) {
            if ( (i!=AtomIdx) && (i!=PrevAtom) && (ConnMatrix[AtomIdx][i]>0) )
                if ( Mol.getAtom(i).getSymbol().compareToIgnoreCase("H")!=0) {
                    CurChunk = RecursiveSearchACF(AtomIdx, i, Lag-1);
                    if (ConnMatrix[AtomIdx][i] == 1) CurBond = "-";
                    if (ConnMatrix[AtomIdx][i] == 2) CurBond = "=";
                    if (ConnMatrix[AtomIdx][i] == 3) CurBond = "#";
                    if (ConnMatrix[AtomIdx][i] == 4) CurBond = "*";
                    if (ConnMatrix[AtomIdx][i] == 1.5) CurBond = ":";
                    CurChunk = CurBond + CurChunk;
                    NextChunks.add(CurChunk);
                }
        }


        //// Checks if other bound atoms have been found
        if (NextChunks.isEmpty())
            return ACFChunk;


        //// Orders next chunks and builds the current ACF string
        String[] NextChunksStrings = new String[0];
        NextChunksStrings = NextChunks.toArray(NextChunksStrings);
        java.util.Arrays.sort(NextChunksStrings);
        for (String curChunk : NextChunksStrings) {
            ACFChunk += "(" + curChunk +")";
        }


        return ACFChunk;
    }


    private String GenerateRingSMILESFromAtom(IAtom atom) {

        String rSMILES = "";
        IRing CurRing = null;

        for (IAtomContainer r : MolRings.atomContainers()) {
            if (r.contains(atom)) {
                CurRing = (IRing) r;
                break;
            }
        }

        if (CurRing == null)
            return "";

        int AtIdx = 0;
        for (AtIdx=0; AtIdx<CurRing.getAtomCount(); AtIdx++)
            if (CurRing.getAtom(AtIdx).equals(atom))
                break;

        for (int i=0; i<CurRing.getAtomCount(); i++) {
            IAtom curAtom = CurRing.getAtom(AtIdx);

            if (curAtom.getFlag(CDKConstants.ISAROMATIC))
                rSMILES += curAtom.getSymbol().toLowerCase();
            else
                rSMILES += curAtom.getSymbol();

            if (i == 0)
                rSMILES += "1";

            AtIdx++;
            if (AtIdx == CurRing.getAtomCount())
                AtIdx = 0;
        }
        rSMILES += "1";


        return rSMILES;
    }




}
