package insilico.core.tools.utils;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRing;

import java.util.List;

/**
 * General utilities for chemical/molecular purposes.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MoleculeUtilities {

    public static double Bond2Double(IBond bond) {

        double retVal = 0;
        IBond.Order bondOrder = bond.getOrder();

        if (bond.getFlag(CDKConstants.ISAROMATIC))
            retVal = 1.5;
        else {
            if(bondOrder == IBond.Order.SINGLE)
                retVal = 1;
            if(bondOrder == IBond.Order.DOUBLE)
                retVal = 2;
            if(bondOrder == IBond.Order.TRIPLE)
                retVal = 3;
            if(bondOrder == IBond.Order.QUADRUPLE)
                retVal = 4;
        }

        return retVal;
    }

    /**
     * Calculates the total bond order of a given atom. Aromatic bonds are
     * counted as 1.5 thus the value can be a floating point number.
     * @param atom IAtom object for the atom to be checked
     * @param molecule source Molecule object for the atom
     * @return the value of the total bond order
     */
    public static double GetTotalBondOrder(IAtom atom, IAtomContainer molecule){

        double bondOrder = 0;
        List<IBond> bonds = molecule.getConnectedBondsList(atom);

        for(IBond bond : bonds){
            if(bond.getFlag(CDKConstants.ISAROMATIC)){
                bondOrder += 1.5;
                continue;
            }
            if(bond.getOrder() == IBond.Order.SINGLE){
                bondOrder += 1;
                continue;
            }
            if(bond.getOrder() == IBond.Order.DOUBLE){
                bondOrder += 2;
                continue;
            }
            if(bond.getOrder() == IBond.Order.TRIPLE){
                bondOrder += 3;
                continue;
            }
            if(bond.getOrder() == IBond.Order.QUADRUPLE){
                bondOrder += 4;
            }
        }

        return bondOrder;
    }

    /**
     * Determines if a ring is aromatic (all of its atoms are set as aromatic).
     * @param ring CDK Ring object to be checked
     * @return True if all ring's atoms are aromatic
     */
    public static boolean IsRingAromatic(IRing ring){

        boolean isAromatic = true;
        for(IBond bond: ring.bonds()){
            if(!(bond.getFlag(CDKConstants.ISAROMATIC))){
                isAromatic = false;
                break;
            }
        }
        return isAromatic;
    }
}
