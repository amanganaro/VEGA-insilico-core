package insilico.core.descriptor.blocks.old.logp;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.Atom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * NC+NHET LogP.<p>
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class CHLogP extends DescriptorBlock {

    private static final long serialVersionUID = 1L;
    private static final String BlockName = "NC+NHET LogP";

    /**
     * Constructor. Sets by default MW calculation with scaled values.
     */
    public CHLogP() {
        super();
        this.Name = CHLogP.BlockName;
    }



    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        this.Add("CHLogP", "NC+NHET logP (Mannhold et al. equation)");

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
            SetAllValues(Descriptor.MISSING_VALUE);
            return;
        }

        try {

            int nSK = curMol.getAtomCount();

            int nC=0;
            int nHet=0;


            //// Counts on atoms

            for (int i=0; i<nSK; i++) {

                Atom CurAt = (Atom) curMol.getAtom(i);

                if (CurAt.getSymbol().equalsIgnoreCase("C"))
                    nC++;
                else
                    nHet++;
            }

            // Mannhold equation for LogP

            double logP = 1.46 + 0.11 * nC - 0.11 * nHet;
            this.SetByName("CHLogP", logP);

        } catch (Throwable e) {
            this.SetAllValues(Descriptor.MISSING_VALUE);
        }

    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    @Override
    public DescriptorBlock CreateClone()
            throws CloneNotSupportedException {
        CHLogP block = new CHLogP();
        block.CloneDetailsFrom(block);
        return block;
    }
}
