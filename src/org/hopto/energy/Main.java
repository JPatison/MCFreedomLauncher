package org.hopto.energy;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.locale.LocaleHelper;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    Locale currentLocale;
    public static void main(String[] args)
            throws IOException {
        Launcher.setLookAndFeel();

        System.setProperty("java.net.preferIPv4Stack", "true");

        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();

        optionParser.accepts("help", "Show help").forHelp();
        optionParser.accepts("force", "Force updating");

        OptionSpec proxyHostOption = optionParser.accepts("proxyHost", "Optional").withRequiredArg();
        OptionSpec proxyPortOption = optionParser.accepts("proxyPort", "Optional").withRequiredArg().defaultsTo("8080", new String[0]).ofType(Integer.class);
        OptionSpec proxyUserOption = optionParser.accepts("proxyUser", "Optional").withRequiredArg();
        OptionSpec proxyPassOption = optionParser.accepts("proxyPass", "Optional").withRequiredArg();
        OptionSpec workingDirectoryOption = optionParser.accepts("workdir", "Optional").withRequiredArg().ofType(File.class).defaultsTo(Util.getWorkingDirectory(), new File[0]);
        OptionSpec nonOptions = optionParser.nonOptions();
        OptionSet optionSet;
        try {
            optionSet = optionParser.parse(args);
        } catch (OptionException e) {
            optionParser.printHelpOn(System.out);
            System.out.println("(to pass in arguments to minecraft directly use: '--' followed by your arguments");
            return;
        }

        if (optionSet.has("help")) {
            optionParser.printHelpOn(System.out);
            return;
        }

        String hostName = (String) optionSet.valueOf(proxyHostOption);
        Proxy proxy = Proxy.NO_PROXY;
        if (hostName != null) {
            try {
                proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(hostName, ((Integer) optionSet.valueOf(proxyPortOption)).intValue()));
            } catch (Exception ignored) {
            }
        }
        String proxyUser = (String) optionSet.valueOf(proxyUserOption);
        String proxyPass = (String) optionSet.valueOf(proxyPassOption);
        PasswordAuthentication passwordAuthentication = null;
        if ((!proxy.equals(Proxy.NO_PROXY)) && (stringHasValue(proxyUser)) && (stringHasValue(proxyPass))) {
            passwordAuthentication = new PasswordAuthentication(proxyUser, proxyPass.toCharArray());

            final PasswordAuthentication auth = passwordAuthentication;
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return auth;
                }

            });
        }

        File workingDirectory = (File) optionSet.valueOf(workingDirectoryOption);
        if ((workingDirectory.exists()) && (!workingDirectory.isDirectory()))
            throw new RuntimeException("Invalid working directory: " + workingDirectory);
        if ((!workingDirectory.exists()) && (!workingDirectory.mkdirs())) {
            throw new RuntimeException("Unable to create directory: " + workingDirectory);
        }

        List strings = optionSet.valuesOf(nonOptions);
        String[] remainderArgs = (String[]) strings.toArray(new String[strings.size()]);

        boolean force = optionSet.has("force");

        JFrame frame = new JFrame();

       System.out.println(LangSelection.getLocale());


        new Launcher(frame, workingDirectory, proxy, passwordAuthentication, args);
    }

    public static boolean stringHasValue(String string) {
        return (string != null) && (!string.isEmpty());
    }
}

/*
class LangSelectionDialog extends JFrame{
    static final String Select = "Select Language";
    static Locale locale=new Locale("en","US");

    public static Locale loadLocale() {
        Properties prop = new Properties();
        String fileLocation=InstallDirSettings.fileLocation;
        File file = new File(fileLocation);



        if (!file.exists()) {
          */
/*  if (ret == 0) {
                File dir = fileChooser.getSelectedFile();
                prop.setProperty("installation_dir", dir.getAbsolutePath());
                workingDirectory = dir;
            } else if (ret == 1) {
                prop.setProperty("installation_dir", workingDirectory.getAbsolutePath());
            }
            try {
                prop.store(new FileOutputStream(settingFile), "");
            } catch (IOException ex) {
                Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
            }*//*

            return locale;

        } else {
            try {
                prop.load(new FileInputStream(file));
            } catch (IOException ex) {
                Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
            }
            locale = new Locale(prop.getProperty("locale"));
        }
        return locale;
    }

    public LangSelectionDialog() {

     //   JButton button=new JButton("Add");

        Locale[] locales= LocaleHelper.getLocales();
        locale = (Locale) JOptionPane.showInputDialog(LangSelectionDialog.this,
                "Please select your language",Select, JOptionPane.INFORMATION_MESSAGE,
                null, locales,locales[0]);
        LocaleHelper.setCurrentLocale(locale);
        JOptionPane.showMessageDialog(null,"You have selected: "+locale);
        System.out.println(locale);
       // button.addActionListener(lst);
       // add(button);


    }



}
*/
