package org.hopto.energy;

import net.minecraft.launcher.Launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {
    public static final String APPLICATION_NAME = "minecraft";
    public static String propFileLocation = "./FreeLauncher.properties";

    public static String getProperties(String key){
        String value="";
       File propFile= new File(propFileLocation);
        Properties prop=new Properties();
        if (!propFile.exists()) {
            return value;
        } else {
            try {
                prop.load(new FileInputStream(propFile));
            } catch (IOException ex) {
                Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
            }
            value = prop.getProperty(key);
        }

        return value;
    }

    public static Object setProperties(String key,String value){
        Object finalValue="";
        File propFile= new File(propFileLocation);
        Properties prop=new Properties();
        if (!propFile.exists()) {
            return finalValue;
        } else {
            try {
                prop.load(new FileInputStream(propFile));
            } catch (IOException ex) {
                Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
            }
            finalValue = prop.setProperty(key, value);
            try {
                prop.store(new FileOutputStream(propFile),"MCFreeLauncher");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return finalValue;
    }

    public static OS getPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) return OS.WINDOWS;
        if (osName.contains("mac")) return OS.MACOS;
        if (osName.contains("linux")) return OS.LINUX;
        if (osName.contains("unix")) return OS.LINUX;
        return OS.UNKNOWN;
    }

    public static File getWorkingDirectory() {
        String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch (getPlatform().ordinal()) {
            case 1:
            case 2:
                workingDirectory = new File(userHome, ".minecraft/");
                break;
            case 3:
                String applicationData = System.getenv("APPDATA");
                String folder = applicationData != null ? applicationData : userHome;

                workingDirectory = new File(folder, ".minecraft/");
                break;
            case 4:
                workingDirectory = new File(userHome, "Library/Application Support/minecraft");
                break;
            default:
                workingDirectory = new File(userHome, "minecraft/");
        }

        return workingDirectory;
    }

    public static enum OS {
        WINDOWS, MACOS, SOLARIS, LINUX, UNKNOWN;
    }
}
