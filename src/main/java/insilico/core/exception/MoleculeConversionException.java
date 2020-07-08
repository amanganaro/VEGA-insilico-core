package insilico.core.exception;

import java.util.ArrayList;

/**
 * Generic Exception thrown when conversion/manipulation of a molecule fails.
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class MoleculeConversionException extends Exception {

    private ArrayList<String> Messages;


    public MoleculeConversionException() {
        // TODO Auto-generated constructor stub
        Messages = new ArrayList<String>();
    }

    public MoleculeConversionException(String message) {
        super(message);
        Messages = new ArrayList<String>();
        Messages.add(message);
    }

    public MoleculeConversionException(String message, MoleculeConversionException prevException) {
        super(message);
        Messages = new ArrayList<String>();
        ArrayList<String> prevMessages = prevException.getMessageList();
        if ((prevMessages != null) && (prevMessages.size()>0))
            for (int i=0; i<prevMessages.size(); i++)
                Messages.add(prevMessages.get(i));
        Messages.add(message);
    }

    public MoleculeConversionException(MoleculeConversionException prevException) {
        super();
        Messages = new ArrayList<String>();
        ArrayList<String> prevMessages = prevException.getMessageList();
        if ((prevMessages != null) && (prevMessages.size()>0))
            for (int i=0; i<prevMessages.size(); i++)
                Messages.add(prevMessages.get(i));
    }

    public MoleculeConversionException(Throwable cause) {
        super(cause);
        Messages = new ArrayList<String>();
    }

    public MoleculeConversionException(String message, Throwable cause) {
        super(message, cause);
        Messages = new ArrayList<String>();
        Messages.add(message);
    }


    @Override
    public String getMessage() {
        String msg = "";
        if (Messages.size()>0)
            for (int i=0; i<Messages.size(); i++) {
                if (i>0)
                    msg += "; ";
                msg += (Messages.get(i));
            }
        return msg;
    }


    public ArrayList<String> getMessageList() {
        return Messages;
    }



}
