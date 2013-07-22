package net.minecraft.launcher.locale;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Energy on 13年7月4日.
 */
public class LocaleHelper {
    private static Locale currentLocale = new Locale("en", "US");
    private static ResourceBundle messages;
    private static Locale[] locales = new Locale[]{new Locale("zh", "HK"), new Locale("en", "US")};

    public static Locale[] getLocales() {
        return locales;
    }

    public void setLocales(Locale[] locales) {
        this.locales = locales;
    }

    static {

        messages = ResourceBundle.getBundle("res.locale.MessagesBundle", currentLocale);

    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static void setCurrentLocale(Locale currentLocale) {
        LocaleHelper.currentLocale = currentLocale;
        messages = ResourceBundle.getBundle("res.locale.MessagesBundle", currentLocale);
    }

    public static ResourceBundle getMessages() {
        return messages;
    }

    public static void setMessages(ResourceBundle messages) {
        LocaleHelper.messages = messages;
    }
}
