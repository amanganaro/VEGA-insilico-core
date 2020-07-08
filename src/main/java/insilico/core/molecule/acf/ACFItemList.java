package insilico.core.molecule.acf;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Wrapper for a list of ACF items.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ACFItemList implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private ArrayList<ACFItem> ACFList;

    public ACFItemList() {
        ACFList = new ArrayList<ACFItem>();
    }

    public void AddItem(ACFItem item) {
        ACFList.add(item);
    }

    public int size() {
        return ACFList.size();
    }

    public ACFItem get(int Index) {
        if ((Index<0)||(Index>(size()-1)))
            return null;
        return ACFList.get(Index);
    }

    public ArrayList<ACFItem> getList() {
        return ACFList;
    }

    public Object Clone() throws CloneNotSupportedException {
        ACFItemList newACF = new ACFItemList();
        newACF.ACFList = new ArrayList<>();
        for (ACFItem i : this.ACFList)
            newACF.ACFList.add((ACFItem)i.Clone());
        return (Object)newACF;
    }




}
