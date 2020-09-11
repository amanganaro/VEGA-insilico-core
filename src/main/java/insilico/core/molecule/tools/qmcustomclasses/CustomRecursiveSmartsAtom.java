package insilico.core.molecule.tools.qmcustomclasses;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.matchers.IQueryAtom;
import org.openscience.cdk.isomorphism.matchers.IQueryAtomContainer;
import org.openscience.cdk.isomorphism.matchers.smarts.RecursiveSmartsAtom;
import org.openscience.cdk.isomorphism.matchers.smarts.SMARTSAtom;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import java.util.*;

public class CustomRecursiveSmartsAtom extends SMARTSAtom {

    private static final long serialVersionUID = 1L;
    private static final ILoggingTool logger = LoggingToolFactory.createLoggingTool(RecursiveSmartsAtom.class);
    private IAtomContainer atomContainer = null;
    private IQueryAtomContainer recursiveQuery = null;
    private BitSet bitSet = null;

    public CustomRecursiveSmartsAtom(IQueryAtomContainer query) {
        super(DefaultChemObjectBuilder.getInstance());
        this.recursiveQuery = query;
    }

    public boolean matches(IAtom atom) {
        if (this.recursiveQuery.getAtomCount() == 1) {
            return ((IQueryAtom)this.recursiveQuery.getAtom(0)).matches(atom);
        } else if (this.atomContainer == null) {
            logger.error("In RecursiveSmartsAtom, atomContainer can't be null! You must set it before matching");
            return false;
        } else {
            if (this.bitSet == null) {
                try {
                    this.initilizeBitSets();
                } catch (CDKException var3) {
                    logger.error("Error found when matching recursive smarts: " + var3.getMessage());
                    return false;
                }
            }

            int atomNumber = this.atomContainer.getAtomNumber(atom);
            return this.bitSet.get(atomNumber);
        }
    }

    private void initilizeBitSets() throws CDKException {
        List<List<RMap>> bondMappings = null;
        UniversalIsomorphismTester tester = new UniversalIsomorphismTester();
        bondMappings = tester.getSubgraphMaps(this.atomContainer, this.recursiveQuery);
        this.bitSet = new BitSet(this.atomContainer.getAtomCount());
        Iterator i$ = bondMappings.iterator();

        while(true) {
            while(true) {
                while(true) {
                    while(true) {
                        while(i$.hasNext()) {
                            List<RMap> bondMapping = (List)i$.next();
                            Collections.sort(bondMapping, new Comparator<RMap>() {
                                public int compare(RMap r1, RMap r2) {
                                    if (r1.getId2() > r2.getId2()) {
                                        return 1;
                                    } else {
                                        return r1.getId2() == r2.getId2() ? 0 : -1;
                                    }
                                }
                            });
                            RMap rmap0 = (RMap)bondMapping.get(0);
                            IBond bond0 = this.atomContainer.getBond(rmap0.getId1());
                            IAtom atom0 = bond0.getAtom(0);
                            IAtom atom1 = bond0.getAtom(1);
                            IBond qbond0 = this.recursiveQuery.getBond(rmap0.getId2());
                            IQueryAtom qatom0 = (IQueryAtom)qbond0.getAtom(0);
                            IQueryAtom qatom1 = (IQueryAtom)qbond0.getAtom(1);
                            if (qatom0.matches(atom0) && qatom1.matches(atom1) && qatom0.matches(atom1) && qatom1.matches(atom0)) {
                                if (bondMapping.size() > 1) {
                                    IBond bond1 = this.atomContainer.getBond(((RMap)bondMapping.get(1)).getId1());
                                    IBond qbond1 = this.recursiveQuery.getBond(((RMap)bondMapping.get(1)).getId2());
                                    if (this.recursiveQuery.getAtomNumber(qatom0) == 0) {
                                        if (qbond1.contains(qatom0) && bond1.contains(atom0)) {
                                            this.bitSet.set(this.atomContainer.getAtomNumber(atom0), true);
                                        } else if (qbond1.contains(qatom0) && bond1.contains(atom1)) {
                                            this.bitSet.set(this.atomContainer.getAtomNumber(atom1), true);
                                        } else if (!qbond1.contains(qatom0) && bond1.contains(atom0)) {
                                            this.bitSet.set(this.atomContainer.getAtomNumber(atom1), true);
                                        } else {
                                            this.bitSet.set(this.atomContainer.getAtomNumber(atom0), true);
                                        }
                                    } else if (qbond1.contains(qatom1) && bond1.contains(atom0)) {
                                        this.bitSet.set(this.atomContainer.getAtomNumber(atom0), true);
                                    } else if (qbond1.contains(qatom1) && bond1.contains(atom1)) {
                                        this.bitSet.set(this.atomContainer.getAtomNumber(atom1), true);
                                    } else if (!qbond1.contains(qatom1) && bond1.contains(atom1)) {
                                        this.bitSet.set(this.atomContainer.getAtomNumber(atom0), true);
                                    } else {
                                        this.bitSet.set(this.atomContainer.getAtomNumber(atom1), true);
                                    }
                                } else {
                                    this.bitSet.set(this.atomContainer.getAtomNumber(atom1), true);
                                    this.bitSet.set(this.atomContainer.getAtomNumber(atom0), true);
                                }
                            } else if (this.recursiveQuery.getAtomNumber(qatom0) == 0) {
                                if (qatom0.matches(atom0) && qatom1.matches(atom1)) {
                                    this.bitSet.set(this.atomContainer.getAtomNumber(atom0), true);
                                } else {
                                    this.bitSet.set(this.atomContainer.getAtomNumber(atom1), true);
                                }
                            } else if (qatom0.matches(atom1) && qatom1.matches(atom0)) {
                                this.bitSet.set(this.atomContainer.getAtomNumber(atom0), true);
                            } else {
                                this.bitSet.set(this.atomContainer.getAtomNumber(atom1), true);
                            }
                        }

                        return;
                    }
                }
            }
        }
    }

    public IQueryAtomContainer getRecursiveQuery() {
        return this.recursiveQuery;
    }

    public void setRecursiveQuery(IQueryAtomContainer query) {
        this.recursiveQuery = query;
    }

    public IAtomContainer getAtomContainer() {
        return this.atomContainer;
    }

    public void setAtomContainer(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
        this.bitSet = null;
    }
}
