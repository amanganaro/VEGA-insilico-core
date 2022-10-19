package insilico.core.descriptor.blocks.old.logp;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.descriptor.blocks.old.weight.GhoseCrippenWeights;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * ALogP and AMR descriptors.
 * Calculates GC ALogP and MR.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ALogP extends DescriptorBlock {

    private final static long serialVersionUID = 1L;
    private final static String BlockName = "ALogP and AMR";


    /**
     * Constructor. This should not be used, no weight is specified. The
     * overloaded constructors should be used instead.
     */
    public ALogP() {
        super();
        this.Name = ALogP.BlockName;
    }

    /**
     * Calculate descriptors for the given molecule.
     *
     * @param mol molecule to be calculated
     */
    @Override
    public void Calculate(InsilicoMolecule mol) {
        this.Calculate(mol, null);
    }


    /**
     * Overload of Calculate(), to be used if needed matrix is passed (to
     * fasten calculation)
     *
     * @param mol molecule to be calculated
//     * @param Matrices wrapper for calculated matrices
     * @param ACF Atom Centered Fragments descriptor block for the current
     * molecule
     */
    public void Calculate(InsilicoMolecule mol, DescriptorBlock ACF) {

        // Generate/clears descriptors
        GenerateDescriptors();

        IAtomContainer m;
        try {
            m = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Retrieves or calculates ACF
        DescriptorBlock CurACF;
        if (ACF != null)
            CurACF = ACF;
        else {
            CurACF = new AtomCenteredFragments();
            CurACF.Calculate(mol);
        }

        double LogP = 0;
        double MR = 0;
        double[] Frags = CurACF.GetAllValues();

        // Check if some fragments are missing values
        for (double d : Frags)
            if (d == Descriptor.MISSING_VALUE)
                return;

        for (int i=0; i<Frags.length; i++) {
            LogP += Frags[i] * GhoseCrippenWeights.GetHydrophobiticty(i);
            MR += Frags[i] * GhoseCrippenWeights.GetMolarRefractivity(i);
        }

        SetByName("ALogP", LogP);
        SetByName("AMR", MR);
    }

    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        Add("ALogP", "Ghose-Crippen ALogP");
        Add("AMR", "Ghose-Crippen Molar Refractivity");
        SetAllValues(Descriptor.MISSING_VALUE);
    }

    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        ALogP block = new ALogP();
        block.CloneDetailsFrom(this);
        return block;
    }


}
