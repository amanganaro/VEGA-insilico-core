package insilico.core.alert;


import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class AlertEncoding {

    private static String EncodeIndex(int Index) {
        String res = "";

        int FirstDigit = Index / 36;
        int SecondDigit = Index % 36;

        if (FirstDigit < 10)
            res += (char) (48 + FirstDigit); // ASCII 0-9
        else
            res += (char) (97 + (FirstDigit-10)); // ASCII a-z

        if (SecondDigit < 10)
            res += (char) (48 + SecondDigit); // ASCII 0-9
        else
            res += (char) (97 + (SecondDigit-10)); // ASCII a-z

        return res;
    }

    private static int DecodeId(String Id) {
        int val = 0;
        char FirstChar = Id.charAt(0);
        if ((FirstChar >= 48) && (FirstChar < 58))
            val += ((int)FirstChar - 48) * 36;
        else
            val += ((int)FirstChar - 97 + 10) * 36;
        char SecondChar = Id.charAt(1);
        if ((SecondChar >= 48) && (SecondChar < 58))
            val += ((int)SecondChar - 48);
        else
            val += ((int)SecondChar - 97 + 10);
        return val;
    }


    public static final String BuildAlertId(int BlockIndex, int AlertIndex) {
        return EncodeIndex(BlockIndex) + EncodeIndex(AlertIndex);
    }


    public static final int GetBlockIndex(String AlertId) {
        String BlockId = AlertId.substring(0, 2);
        return DecodeId(BlockId);
    }


    public static final int GetAlertIndex(String AlertId) {
        String AlertItemId = AlertId.substring(2, 4);
        return DecodeId(AlertItemId)-1;
    }


    public static final String MergeAlertIds(AlertList SAs) {
        String res = "";
        for (Alert curSA : SAs.getSAList()) {
            if (res.length() > 0) res += ".";
            res += curSA.getId();
        }
        return res;
    }

    public static final String MergeAlertIds(AlertList SAs, String PreviousSAs) {
        String curSA = MergeAlertIds(SAs);
        if ( (PreviousSAs == null) || (PreviousSAs.isEmpty()) )
            return curSA;
        else
            return PreviousSAs + "." + curSA;
    }

    public static final String MergeAlertIds(String SAs, String PreviousSAs) {
        if ( (PreviousSAs == null) || (PreviousSAs.isEmpty()) )
            return SAs;
        else if (SAs.isEmpty())
            return PreviousSAs;
        else
            return PreviousSAs + "." + SAs;
    }

    public static final String MergeAlertIds(ArrayList<AlertList> SAs) {
        String res = "";
        for (AlertList curSAList : SAs) {
            for (Alert curSA : curSAList.getSAList()) {
                if (res.length() > 0) res += ".";
                res += curSA.getId();
            }
        }
        return res;
    }

    public static final String[] UnpackAlertIds(String SAs) {
        return SAs.split("\\.");
    }

    public static final boolean ContainsAlert(String SAList, String SA) {
        String[] CurAlerts = UnpackAlertIds(SAList);
        for (String alert : CurAlerts)
            if (alert.equalsIgnoreCase(SA))
                return true;
        return false;
    }
}
