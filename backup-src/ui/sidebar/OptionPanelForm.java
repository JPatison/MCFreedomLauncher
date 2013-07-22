package net.minecraft.launcher.ui.sidebar;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.locale.LocaleHelper;
import org.hopto.energy.LangSelection;
import org.hopto.energy.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Energy on 13年7月4日.
 */
public class OptionPanelForm  extends SidebarGridForm{
    private final Launcher launcher;
    private static ResourceBundle resourceBundle = LocaleHelper.getMessages();

    private  JComboBox<Locale> langList =new JComboBox<Locale>(LocaleHelper.getLocales());
    private JButton btnChangeLang=new JButton(resourceBundle.getString("change.language"));


    public OptionPanelForm(Launcher launcher) {
        super(resourceBundle.getString("options"));

        this.launcher = launcher;

        langList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                Locale locale = (Locale)cb.getSelectedItem();
                updateLocale(locale);
            }
        });
        langList.setSelectedItem(LocaleHelper.getCurrentLocale());

        btnChangeLang.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LangSelection.selectLang();
                //Launcher.getInstance().getFrame().getRootPane().updateUI();
                //SwingUtilities.updateComponentTreeUI(Launcher.getInstance().getFrame());
               // Launcher.getInstance().getFrame().getWindowListeners()[0].windowClosing(null);



            /*    try {
                    Main.main(new String[0]);
                } catch (IOException ex) {
                    //Logger.getLogger(OptionPanelForm.class.getName()).log(Level.SEVERE, null, ex);
                }*/

                try {
                    Util.restartApplication();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });


        createInterface();

        applyOptions();
    }

    private void applyOptions() {


    }

    @Override
    protected void populateGrid(GridBagConstraints constraints) {
        add(new JLabel(resourceBundle.getString("language"), 2), constraints, 0, 0, 0, 1, 17);

        add(this.langList, constraints, 1, 0, 1, 1);

        add(btnChangeLang,constraints,0,1,0,1,17);



    }



    private void updateLocale(Locale locale) {
        LocaleHelper.setCurrentLocale(locale);
    }
}
