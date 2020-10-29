package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.acf.GhoseCrippenACF;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * Atom Centered Fragments (ACF) descriptors block.<p>
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class AtomCenteredFragments extends DescriptorBlock {

    private static final long serialVersionUID = 1L;
    private static final String BlockName = "Atom Centered Fragments";


    /**
     * Constructor.
     */
    public AtomCenteredFragments() {
        super();
        this.Name = AtomCenteredFragments.BlockName;
    }


    @Override
    protected void GenerateDescriptors() {
        DescList.clear();
        for (String[] s : GhoseCrippenACF.ACF_NAMES) {
            boolean Undefined = false;
            for (String undSymbol : GhoseCrippenACF.ACF_UNDEFINED_NAMES)
                if (undSymbol.equalsIgnoreCase(s[1]))
                    Undefined = true;
            if (Undefined)
                continue;
            Add(s[1], s[2]);

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

        IAtomContainer CurMol;
        try {
            CurMol = mol.GetStructure();
        } catch (InvalidMoleculeException e) {
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        // Gets fragments
        GhoseCrippenACF GC = new GhoseCrippenACF(CurMol, false);
        int[] Frags = GC.GetACF();

        // Count fragments to fill descriptors
        int[] FragDescriptors = new int[GhoseCrippenACF.ACF_NAMES.length];
        for (int i : FragDescriptors)
            i = 0;
        for (int frag : Frags) {
            if (frag == Descriptor.MISSING_VALUE)
                continue;
            FragDescriptors[frag]++;
        }

        // Count fragments not mapped directly into atoms (H)
        for (int key : GC.getNotMappedFragCount().keySet())
            FragDescriptors[key] = GC.getNotMappedFragCount().get(key);

        // Set descriptors
        for (int i=0; i<GhoseCrippenACF.ACF_NAMES.length; i++)
            SetByName(GhoseCrippenACF.ACF_NAMES[i][1], FragDescriptors[i]);
    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    @Override
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        AtomCenteredFragments block = new AtomCenteredFragments();
        block.CloneDetailsFrom(this);
        return block;
    }

}
