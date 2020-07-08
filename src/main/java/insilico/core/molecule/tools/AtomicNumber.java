package insilico.core.molecule.tools;

import insilico.core.exception.InitFailureException;
import insilico.core.molecule.conversion.file.MoleculeFileSmiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Provides atomic numbers and atomic symbols.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class AtomicNumber {

    Logger logger = LoggerFactory.getLogger(AtomicNumber.class);


    final static private char CharTAB = 9;

    private ArrayList<Integer> Z;
    private ArrayList<String> Symbol;

    /**
     * Constructor. Init Z data from embedded resource file.
     * @throws InitFailureException
     */
    public AtomicNumber() throws InitFailureException {

        Z = new ArrayList<>();
        Symbol = new ArrayList<>();

        // Loads data into the arraylists
        try {
            URL uData = getClass().getResource(System.getProperty("user.dir") + "/src/main/java/insilico.core/molecule/tools/Z.dat");
            DataInputStream in = new DataInputStream(uData.openStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String CurLine;
            while ((CurLine = br.readLine())!=null) {
                String[] BufStr = CurLine.split(String.valueOf(CharTAB).toString());
                Z.add(new Integer(BufStr[0]));
                Symbol.add(BufStr[1]);
            }
            in.close();
        } catch (IOException | NumberFormatException e) {
            logger.error("Error while initializing atomic number handler (" + e.getMessage() + ")");
            throw new InitFailureException("Unable to init Z data from embedded file.");
        }
    }


    /**
     * Gives the atomic number for a given element symbol. Returns -999 if the
     * symbol is not found.
     * @param ElementSymbol element symbol of the atom
     * @return atomic number
     */
    public int GetAtomicNumber(String ElementSymbol) {
        int val = -999;

        for (int i=0; i<Symbol.size(); i++)
            if (ElementSymbol.equalsIgnoreCase(Symbol.get(i))) {
                val = Z.get(i);
                break;
            }

        return val;
    }


    /**
     * Gives the element symbol for a given atomic number. Returns a blank
     * string if the atomic number is not found.
     * @param AtomicNumber atomic number of the element
     * @return element symbol
     */
    public String GetElementSymbol(int AtomicNumber) {
        String val = "";

        for (int i=0; i<Symbol.size(); i++)
            if (AtomicNumber == Z.get(i)) {
                val = Symbol.get(i);
                break;
            }

        return val;
    }

}
