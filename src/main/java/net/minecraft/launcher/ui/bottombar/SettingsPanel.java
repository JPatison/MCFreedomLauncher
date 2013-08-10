/*
 * Created by JFormDesigner on Sat Aug 10 23:55:34 CST 2013
 */

package net.minecraft.launcher.ui.bottombar;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.locale.LocaleHelper;
import org.hopto.energy.InstallDirSettings;
import org.hopto.energy.LangSelection;
import org.hopto.energy.Main;
import org.hopto.energy.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Energy
 */
public class SettingsPanel extends JPanel {
    private final ResourceBundle resourceBundle = LocaleHelper.getMessages();
    private final Launcher launcher;

    public SettingsPanel(Launcher launcher) {
        initComponents();
        this.launcher = launcher;
        comboBoxLanguage.setSelectedItem(LocaleHelper.getCurrentLocale());
        labelWorkingDirectory.setEditable(false);
        labelWorkingDirectory.setText(launcher.getWorkingDirectory().toPath().toString());

    }

    private void buttonChangeWDActionPerformed(ActionEvent e) {
        InstallDirSettings.changeDir(Launcher.getInstance().getFrame(), Launcher.getInstance().getWorkingDirectory());
        Launcher.getInstance().getFrame().getWindowListeners()[0].windowClosing(null);
        try {
            Main.main(new String[0]);
        } catch (IOException ex) {
            Logger.getLogger(SettingsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buttonChangeLangActionPerformed(ActionEvent e) {
        //LangSelection.selectLang();
       LangSelection.setLang((Locale)comboBoxLanguage.getSelectedItem());
        try {
            Util.restartApplication();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = LocaleHelper.getMessages();
        labelLanguage = new JLabel();
        comboBoxLanguage = new JComboBox(LocaleHelper.getLocales());
        buttonChangeLang = new JButton();
        labelDirectory = new JLabel();
        labelWorkingDirectory = new JTextField();
        buttonChangeWD = new JButton();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 40, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

        //---- labelLanguage ----
        labelLanguage.setText(bundle.getString("SettingsPanel.labelLanguage.text"));
        add(labelLanguage, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));
        add(comboBoxLanguage, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 5), 0, 0));

        //---- buttonChangeLang ----
        buttonChangeLang.setText(bundle.getString("SettingsPanel.buttonChangeLang.text"));
        buttonChangeLang.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonChangeLangActionPerformed(e);
            }
        });
        add(buttonChangeLang, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 5, 0), 0, 0));

        //---- labelDirectory ----
        labelDirectory.setText(bundle.getString("SettingsPanel.labelDirectory.text"));
        add(labelDirectory, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- labelWorkingDirectory ----
        labelWorkingDirectory.setText(bundle.getString("SettingsPanel.labelWorkingDirectory.text"));
        add(labelWorkingDirectory, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0));

        //---- buttonChangeWD ----
        buttonChangeWD.setText(bundle.getString("SettingsPanel.buttonChangeWD.text"));
        buttonChangeWD.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonChangeWDActionPerformed(e);
            }
        });
        add(buttonChangeWD, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel labelLanguage;
    private JComboBox comboBoxLanguage;
    private JButton buttonChangeLang;
    private JLabel labelDirectory;
    private JTextField labelWorkingDirectory;
    private JButton buttonChangeWD;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
