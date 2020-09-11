package insilico.core.molecule.tools.qmcustomclasses;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.matchers.smarts.SMARTSAtom;

import java.util.Iterator;
import java.util.List;


public class CustomHydrogenAtom extends SMARTSAtom {

    private IAtomContainer atomContainer;

    public CustomHydrogenAtom() {
        super(DefaultChemObjectBuilder.getInstance());
    }

    public boolean matches(IAtom atom) {
        if (!atom.getSymbol().equals("H")) {
            return false;
        } else if (atom.getFormalCharge() == 1) {
            return true;
        } else {
            List<IAtom> list = this.atomContainer.getConnectedAtomsList(atom);
            Iterator i$ = list.iterator();

            IAtom connAtom;
            do {
                if (!i$.hasNext()) {
                    if (atom.getFormalNeighbourCount() > 1) {
                        return true;
                    }

                    if (atom.getMassNumber() != null) {
                        if (this.getMassNumber() == atom.getMassNumber()) {
                            return true;
                        }
                    } else if (this.getMassNumber() == 1) {
                        return true;
                    }

                    return false;
                }

                connAtom = (IAtom)i$.next();
            } while(!connAtom.getSymbol().equals("H"));

            return true;
        }
    }

    public IAtomContainer getAtomContainer() {
        return this.atomContainer;
    }

    public void setAtomContainer(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
    }

}
