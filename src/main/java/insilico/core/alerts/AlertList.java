package insilico.core.alerts;

import java.io.Serializable;
import java.util.ArrayList;

public class AlertList implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private ArrayList<Alert> SAList;

    public AlertList() {
        SAList = new ArrayList<>();
    }

    public ArrayList<Alert> getSAList() {
        return SAList;
    }

    public void add(Alert item) {
        SAList.add(item);
    }

    public int size() {
        return SAList.size();
    }

    public Alert get(int Index) {
        if ((Index<0)||(Index>(size()-1)))
            return null;
        return SAList.get(Index);
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        AlertList s = (AlertList) super.clone();
        s.SAList = new ArrayList<>();
        for (Alert i : this.SAList)
            s.SAList.add((Alert)i.clone());
        return (Object)s;
    }


}
