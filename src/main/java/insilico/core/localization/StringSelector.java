package insilico.core.localization;

import java.util.Locale;
import java.util.ResourceBundle;

public class StringSelector {

    // languages
    public static final String IT = "it";
    public static final String ZH = "zh";

    private static final String DEFAULT_LANGUAGE = "";

    private static Locale locale = new Locale(DEFAULT_LANGUAGE);
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("bundle", locale);


    public static void SetLanguage(String language){
        locale = new Locale(language);
        resourceBundle = ResourceBundle.getBundle("bundle", locale);
    }

    public static String getString(String key){
        return resourceBundle.getString(key);
    }


}
