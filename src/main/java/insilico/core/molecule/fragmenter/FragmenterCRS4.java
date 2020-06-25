package insilico.core.molecule.fragmenter;

import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smarts.SmartsPattern;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import java.util.*;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class FragmenterCRS4 {

    private static List<IAtomContainer> splitMolecule(IAtomContainer atomContainer, IBond bond) {

        // atomContainer è la molecola i-esima presa dal file SDF privata degli atomi di idrogeno
        // bond è il legame tra due atomi determinato nel metodo chiamante

        List<IAtomContainer> ret = new ArrayList<>();
        // Itera sugli atomi del bond dato in ingresso
        for (IAtom atom : bond.atoms()) {
            IAtom excludedAtom;

            // Se l'atomo corrente del bond è quello di indice 0, esclude quello di indice 1
            // altrimenti esclude quello di indice 0
            if (atom.equals(bond.getAtom(0))) excludedAtom = bond.getAtom(1);
            else excludedAtom = bond.getAtom(0);

            // Crea una lista vuota di bond
            List<IBond> part = new ArrayList<>();
            // Aggiunge il bond dato in ingresso alla suddetta lista
            part.add(bond);

            // Vedi descrizione metodo traverse
            // part è una lista di bond
            part = traverse(atomContainer, atom, part);


            IAtomContainer partContainer;
            //  Vedi descrizione metodo makeAtomContainer
            //  partContainer è un contenitore di atomi
            partContainer = makeAtomContainer(atom, part, excludedAtom);
            if (partContainer.getAtomCount() > 2 && partContainer.getAtomCount() != atomContainer.getAtomCount())
                ret.add(partContainer);
            part.remove(0);
            partContainer = makeAtomContainer(atom, part, excludedAtom);
            if (partContainer.getAtomCount() > 2 && partContainer.getAtomCount() != atomContainer.getAtomCount())
                ret.add(partContainer);
        }
        return ret;
    }

    private static IAtomContainer makeAtomContainer(IAtom atom, List<IBond> parts, IAtom excludedAtom) {
        // Crea un IAtomContainer
        IAtomContainer partContainer = DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
        // Aggiunge l'atomo in input all'IAtomContainer
        partContainer.addAtom(atom);
        //  Itera la lista di bond
        for (IBond aBond : parts) {

            // Per ciascun bond prende i suoi atomi e ci itera
            for (IAtom bondedAtom : aBond.atoms()) {
                //  Se l' atomo corrente (del bond) non è tra quelli esclusi e se (contemporaneamente) non si trova nell' IAtomContainer
                //  allora ce lo aggiunge
                if (!bondedAtom.equals(excludedAtom) && !partContainer.contains(bondedAtom))
                    partContainer.addAtom(bondedAtom);
            }
            // Se il bond corrente (su cui sta iterando) non contiene l'atomo escluso, allora aggiunge il bond stesso all'IAtomContainer
            if (!aBond.contains(excludedAtom)) partContainer.addBond(aBond);
        }
        return partContainer;
    }

    private static List<IBond> traverse(IAtomContainer atomContainer, IAtom atom, List<IBond> bondList) {

        //  Crea una lista di tutti i bond connessi all' atomo dato in input
        List<IBond> connectedBonds = atomContainer.getConnectedBondsList(atom);

        // Itera sui bond connessi all'atomo dato (cioè i bond della lista definita sopra)
        for (IBond aBond : connectedBonds) {
            if (bondList.contains(aBond))
                continue;
            // Se il bond corrente (tra quelli connessi all'atomo dato in input) non è nella lista di bond in input
            // allora lo aggiunge alla lista di bond in input
            bondList.add(aBond);

            //  Poi prende l'atomo connesso all'atomo dato in input
            IAtom nextAtom = aBond.getConnectedAtom(atom);

            // Se il numero di atomi connessi all'atomo precedente è diverso da 1 ricomincia (richiama se stessa)
            // altrimenti passa al bond successivo della lista di bond connessi all'atomo dato
            if (atomContainer.getConnectedAtomsCount(nextAtom) == 1)
                continue;
            traverse(atomContainer, nextAtom, bondList);
        }
        return bondList;
    }

    // ##### CCQ
    // The CCQ terminology means that single bonds connecting two carbon atoms (CC) are cleaved if at least one 
    // of those carbons is connected to a heteroatom (Q).
    // Does CCQ open rings?
    // It opens aliphatic rings, but not aromatic ones.
    public static List<IAtomContainer> getCCQfragments(IAtomContainer atomContainer) throws CDKException {

        List<IAtomContainer> Results = new ArrayList<>();

        for (int i = 0; i < atomContainer.getBondCount(); i ++){
            if (isCCQBond(atomContainer.getBond(i), atomContainer)){
                Bond bond = (Bond) atomContainer.getBond(i);
                List<IAtomContainer> candidates = splitMolecule(atomContainer, bond);
                for (int a = 0; a < candidates.size(); a ++){
                    Results.add(candidates.get(a));
                }
            }
        }

        return Results;
    }

    private static boolean isCCQBond(IBond bond, IAtomContainer atomContainer) throws CDKException {
        Atom a1 = (Atom) bond.getAtom(0);
        Atom a2 = (Atom) bond.getAtom(1);
        String symbol;
        for(int i = 0; i < atomContainer.getConnectedAtomsCount(a1); i ++){
            symbol = atomContainer.getConnectedAtomsList(a1).get(i).getSymbol();
            if ( symbol != "C" & symbol != "H" ){
                return true;
            }
        }
        for(int i = 0; i < atomContainer.getConnectedAtomsCount(a2); i ++){
            symbol = atomContainer.getConnectedAtomsList(a2).get(i).getSymbol();
            if ( symbol != "C" & symbol != "H" ){
                return true;
            }
        }
        return false;
    }

//    public static void getExhaustivefragments(IAtomContainer atomContainer) throws CDKException {
//            ExhaustiveFragmenter ef = new ExhaustiveFragmenter();
//            ef.generateFragments(atomContainer);
//            SmilesGenerator sg = new SmilesGenerator();
//            IAtomContainer[] res = ef.getFragmentsAsContainers();
//            for (int i = 0; i < res.length; i++) {
//                    sg.setUseAromaticityFlag(true);
//                    String smiles = sg.createSMILES(res[i]);
//                    System.out.println( atomContainer.getProperty(CDKConstants.TITLE) + " " + "EXHAUSTIVE" + " " + smiles );
//            }
//            return;
//    }

//	public static List<String> getMurckofragments(IAtomContainer atomContainer) throws CDKException {
//		MurckoFragmenter mf = new MurckoFragmenter();
//		mf.generateFragments(atomContainer);
//		List<String> results = new ArrayList<String>();
//		SmilesGenerator sg = new SmilesGenerator();
//		IAtomContainer[] res = mf.getFragmentsAsContainers();
//		for (int i = 0; i < res.length; i++) {
//			sg.setUseAromaticityFlag(true);
//			String smiles = sg.createSMILES(res[i]);
//			if (results.contains(smiles) == false){
//				results.add(smiles);
//				System.out.println( atomContainer.getProperty(CDKConstants.TITLE) + " " + "MURCKO" + " " + smiles );
//			}
//		}
//		return results;
//	}


    public static List<IAtomContainer> getROTATABLEfragments(IAtomContainer atomContainer) throws CDKException {

        List<IAtomContainer> Results = new ArrayList<>();

        SMARTSQueryTool rotata = new SMARTSQueryTool("[!$([NH]!@C(=O))&!D1&!$(*#*)]-&!@[!$([NH]!@C(=O))&!D1&!$(*#*)]",DefaultChemObjectBuilder.getInstance());
        List <List<Integer>> mappings;

        boolean status = ((SMARTSQueryTool) rotata).matches(atomContainer);
        if (status) {
            int nmatch = ((SMARTSQueryTool) rotata).countMatches();
            mappings = ((SMARTSQueryTool) rotata).getMatchingAtoms();
            //System.out.println( "ROTATA" + " " + nmatch );
            for (int i = 0; i < nmatch; i++) {
                List atomIndices = (List) mappings.get(i);
                Atom a1 = (Atom) atomContainer.getAtom((Integer) atomIndices.get(0));
                Atom a2 = (Atom) atomContainer.getAtom((Integer) atomIndices.get(1));
                Bond bond = (Bond) atomContainer.getBond(a1, a2);
                List<IAtomContainer> candidates = splitMolecule(atomContainer, bond);
                //System.out.println( "A" + " " + candidates.size() + " " + (Integer) atomIndices.get(0) + " " + (Integer) atomIndices.get(1));
                Results.addAll(candidates);
            }
        }

        return Results;
    }


    public static List<IAtomContainer> getRECAPfragments(IAtomContainer atomContainer) throws CDKException {

        List<IAtomContainer> Results = new ArrayList<>();

        // TODO: 25/06/2020 SMARTQUERYTOOL depcrecated
        // http://cdk.github.io/cdk/latest/docs/api/org/openscience/cdk/smarts/SmartsPattern.html
        // The class SMARTSQueryTool provides a easy to use wrapper around SMARTS matching functionality.
        // Qui fa un dizionario di classi SMARTSQueryTool		
        Map<String, SMARTSQueryTool> smarts = new Hashtable<String, SMARTSQueryTool>();
//        Map<String, SmartsPattern> smarts = new Hashtable<>();
        SMARTSQueryTool amide = new SMARTSQueryTool("[$([C;!$(C([#7])[#7])](=!@[O]))]!@[$([#7;+0;!D1])]", DefaultChemObjectBuilder.getInstance());
        smarts.put("amide", amide);
        SMARTSQueryTool ester = new SMARTSQueryTool("[$(C=!@O)]!@[$([O;+0])]", DefaultChemObjectBuilder.getInstance());
        smarts.put("ester", ester);
        SMARTSQueryTool amine = new SMARTSQueryTool("[$([N;!D1;+0;!$(N-C=[#7,#8,#15,#16])](-!@[*]))]-!@[$([*])]", DefaultChemObjectBuilder.getInstance());
        smarts.put("amine", amine);
        SMARTSQueryTool urea = new SMARTSQueryTool("[$(C(=!@O)([#7;+0;D2,D3])!@[#7;+0;D2,D3])]!@[$([#7;+0;D2,D3])]", DefaultChemObjectBuilder.getInstance());
        smarts.put("urea", urea);
        SMARTSQueryTool ether = new SMARTSQueryTool("[$([O;+0](-!@[#6!$(C=O)])-!@[#6!$(C=O)])]-!@[$([#6!$(C=O)])]", DefaultChemObjectBuilder.getInstance());
        smarts.put("ether", ether);
        SMARTSQueryTool olefin = new SMARTSQueryTool("C=!@C", DefaultChemObjectBuilder.getInstance());
        smarts.put("olefin", olefin);
        SMARTSQueryTool quaternaryN = new SMARTSQueryTool("[N;+1;D4]!@[#6]", DefaultChemObjectBuilder.getInstance());
        smarts.put("quaternaryN", quaternaryN);
        SMARTSQueryTool aromaticNaliphaticC = new SMARTSQueryTool("[$([n;+0])]-!@C", DefaultChemObjectBuilder.getInstance());
        smarts.put("aromaticNaliphaticC", aromaticNaliphaticC);
        SMARTSQueryTool lactamNaromaticC = new SMARTSQueryTool("[$([O]=[C]-@[N;+0])]-!@[$([C])]", DefaultChemObjectBuilder.getInstance());
        smarts.put("lactamNaromaticC", lactamNaromaticC);
        SMARTSQueryTool aromaticCaromaticC = new SMARTSQueryTool("c-!@c", DefaultChemObjectBuilder.getInstance());
        smarts.put("aromaticCaromaticC", aromaticCaromaticC);
        SMARTSQueryTool sulphonamide = new SMARTSQueryTool("[$([#7;+0;D2,D3])]-!@[$([S](=[O])=[O])]", DefaultChemObjectBuilder.getInstance());
        smarts.put("sulphonamide", sulphonamide);


        boolean status = false;
        Iterator<String> keys = smarts.keySet().iterator();
        String chiave;
        Object valore;
        List mappings;

        while(keys.hasNext()) {
            chiave = keys.next();
            valore = smarts.get(chiave);
            //  Perform a SMARTS match and check whether the query is present in the target molecule.
            status = ((SMARTSQueryTool) valore).matches(atomContainer);
            //System.out.println(status);
            if (status) {

                //  Returns the number of times the pattern was found in the target molecule.
                int nmatch = ((SMARTSQueryTool) valore).countMatches();
                mappings = ((SMARTSQueryTool) valore).getMatchingAtoms();

                for (int i = 0; i < nmatch; i++) {
                    List atomIndices = (List) mappings.get(i);
                    //System.out.println(atomIndices);
                    Atom a1 = (Atom) atomContainer.getAtom((Integer) atomIndices.get(0));
                    Atom a2 = (Atom) atomContainer.getAtom((Integer) atomIndices.get(1));

                    //  Returns the bond that connectes the two given atoms.
                    Bond bond = (Bond) atomContainer.getBond(a1, a2);


                    //  Taglia la molecola sul bond 
                    List<IAtomContainer> candidates = splitMolecule(atomContainer, bond);
                    //System.out.println( "A" + " " + candidates.size() + " " + (Integer) atomIndices.get(0) + " " + (Integer) atomIndices.get(1));
                    Results.addAll(candidates);
                }
            }
        }

        return Results;
    }


}
