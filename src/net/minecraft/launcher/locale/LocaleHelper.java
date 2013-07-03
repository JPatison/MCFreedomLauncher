package net.minecraft.launcher.locale;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Energy on 13年7月4日.
 */
public class LocaleHelper {
    private static Locale currentLocale;
    private static ResourceBundle messages;

    static {
        currentLocale = new Locale("zh", "HK");
        messages = ResourceBundle.getBundle("res.locale.MessagesBundle", currentLocale);

    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static void setCurrentLocale(Locale currentLocale) {
        LocaleHelper.currentLocale = currentLocale;
    }

    public static ResourceBundle getMessages() {
        return messages;
    }

    public static void setMessages(ResourceBundle messages) {
        LocaleHelper.messages = messages;
    }
}
