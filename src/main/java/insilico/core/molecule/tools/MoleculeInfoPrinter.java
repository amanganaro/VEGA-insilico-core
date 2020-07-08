package insilico.core.molecule.tools;

import insilico.core.exception.InvalidMoleculeException;
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

        writer.println("Valid molecule: " + Molecule.IsValid());
        writer.println("Warnings: " + Molecule.GetWarnings());

        if (!Molecule.IsValid()) {
            writer.flush();
            writer.close();
            return;
        }

        IAtomContainer structure;

        try {
            structure = Molecule.GetStructure();
        } catch (InvalidMoleculeException ex) {
            writer.println("Unable to retrieve structure");
            writer.flush();
            writer.close();
            return;
        }

        writer.println("SMILES: " + Molecule.GetSMILES());
        writer.println("no. of atoms: " + structure.getAtomCount());
        writer.println("no. of bonds: " + structure.getBondCount());

        for (int i=0; i<structure.getAtomCount(); i++) {
            IAtom at = structure.getAtom(i);
            writer.println();
            writer.println("* Atom no. " + (i+1));
            writer.println("Atomic Number: " + at.getAtomicNumber());
            writer.println("Symbol: " + at.getSymbol());
            writer.println("Type: " + at.getAtomTypeName());
            writer.println("Hybridization: " + at.getHybridization().toString());
            writer.println("Valency: " + at.getValency());
            writer.println("Aromatic: " + at.isAromatic());
        }

        for (int i=0; i<structure.getBondCount(); i++) {
            IBond bo = structure.getBond(i);
            writer.println();
            writer.println("* Bond no. " + (i+1));
            writer.println("Order: " + bo.getOrder().toString());
            writer.println("Atom 1: " + (structure.indexOf(bo.getAtom(0)) + 1));
            writer.println("Atom 2: " + (structure.indexOf(bo.getAtom(1)) + 1));
            writer.println("Aromatic: " + bo.isAromatic());
        }

        writer.flush();
        writer.close();



    }


}
