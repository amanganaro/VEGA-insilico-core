package insilico.core.molecule.tools;

import insilico.core.exception.InitFailureException;
import insilico.core.molecule.conversion.file.MoleculeFileSmiles;
import org.openscience.cdk.config.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * Provides atomic numbers and atomic symbols.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class AtomicNumber {

    /**
     * Gives the atomic number for a given element symbol. Returns -999 if the
     * symbol is not found.
     * @param ElementSymbol element symbol of the atom
     * @return atomic number
     */
    public int GetAtomicNumber(String ElementSymbol) {
        return Elements.ofString(ElementSymbol).toIElement().getAtomicNumber();
    }


    /**
     * Gives the element symbol for a given atomic number. Returns a blank
     * string if the atomic number is not found.
     * @param AtomicNumber atomic number of the element
     * @return element symbol
     */
    public String GetElementSymbol(int AtomicNumber) {
        return Elements.ofNumber(AtomicNumber).toIElement().getSymbol();
    }

}
