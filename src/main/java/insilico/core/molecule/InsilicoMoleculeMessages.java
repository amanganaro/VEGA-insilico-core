package insilico.core.molecule;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Helper class for storing a set of related messages (errors etc.)
 *
 * @author Alberto Manganaro <a.manganaro@kode-solutions.net>
 */
public class InsilicoMoleculeMessages implements Serializable, Cloneable {

    private ArrayList<String> messages;

    public InsilicoMoleculeMessages() {
        messages = new ArrayList<>();
    }

    public void AddMessage(String msg){
        messages.add(msg);
    }

    public int GetSize(){
        return messages.size();
    }

    public String GetMessages(int Index){
        if((Index>=0) && (Index< GetSize())){
            return messages.get(Index);
        }
        else
            return null;
    }

    public String GetMessages(){
        StringBuilder msg = new StringBuilder();
        for (String m : messages){
            if(msg.length() > 0)
                msg.append("  ");
            msg.append(m);
        }
        return msg.toString();
    }

    @SuppressWarnings("unchecked")
    public Object Clone() throws CloneNotSupportedException {
        InsilicoMoleculeMessages m = (InsilicoMoleculeMessages)super.clone();
        m.messages = (ArrayList<String>)this.messages.clone();
        return m;
    }
}
