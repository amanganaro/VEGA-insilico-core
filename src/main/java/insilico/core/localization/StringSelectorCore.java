package insilico.core.localization;

import java.util.Locale;
import java.util.ResourceBundle;

public class StringSelectorCore {


    // languages
    public static final String IT = "it";
    public static final String ZH = "zh";
    private static Locale locale = new Locale("");
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("bundle-Core", locale);

    public static void SetLanguage(String language){
        locale = new Locale(language);
        resourceBundle = ResourceBundle.getBundle("bundle-Core", locale);
    }

    public void BackToDefault(){
        locale = new Locale("");
    }

    public static String getString(String key){
        return resourceBundle.getString(key);
    }

    public static String getLanguage(){return locale.getLanguage();}


}
