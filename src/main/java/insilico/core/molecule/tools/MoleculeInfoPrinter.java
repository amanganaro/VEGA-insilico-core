package insilico.core.molecule.tools;

import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.molecule.InsilicoMolecule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MoleculeInfoPrinter {


    public static void PrintInfo(InsilicoMolecule Molecule, OutputStream out){

        PrintWriter writer = new PrintWriter(out);

        writer.println(StringSelectorCore.getString("tool_infoprinter_valid_mol") + Molecule.IsValid());
        writer.println(StringSelectorCore.getString("tool_infoprinter_warn") + Molecule.GetWarnings());

        if (!Molecule.IsValid()) {
            writer.flush();
            writer.close();
            return;
        }

        IAtomContainer structure;

        try {
            structure = Molecule.GetStructure();
        } catch (InvalidMoleculeException ex) {
            writer.println(StringSelectorCore.getString("tool_infoprinter_retrieve_struct_fail"));
            writer.flush();
            writer.close();
            return;
        }

        writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_smiles"), Molecule.GetSMILES()));
        writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_atoms"), structure.getAtomCount()));
        writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_bonds"), structure.getBondCount()));


        for (int i=0; i<structure.getAtomCount(); i++) {
            IAtom at = structure.getAtom(i);
            writer.println();
            writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_atom_no"),i+1));
            writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_atomic_no"),at.getAtomicNumber()));
            writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_symbol"),at.getSymbol()));
            writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_type"),at.getAtomTypeName()));
            writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_hybrid"),at.getHybridization().toString()));
            writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_valency"),at.getValency()));
            writer.println(StringSelectorCore.getString("tool_infoprinter_aromatic") + at.isAromatic());
        }

        for (int i=0; i<structure.getBondCount(); i++) {
            IBond bo = structure.getBond(i);
            writer.println();
            writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_bond_no"),i+1));
            writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_order"),bo.getOrder().toString()));
            writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_atom"),1, (structure.indexOf(bo.getAtom(0)) + 1)));
            writer.println(String.format(StringSelectorCore.getString("tool_infoprinter_atom"),2, (structure.indexOf(bo.getAtom(0)) + 1)));
            writer.println(StringSelectorCore.getString("tool_infoprinter_aromatic") + bo.isAromatic());
        }

        writer.flush();
        writer.close();



    }


}
