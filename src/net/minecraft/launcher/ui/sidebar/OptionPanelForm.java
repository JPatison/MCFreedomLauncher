package net.minecraft.launcher.ui.sidebar;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.locale.LocaleHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

/**
 * Created by Energy on 13年7月4日.
 */
public class OptionPanelForm  extends SidebarGridForm implements ActionListener{
    private final Launcher launcher;

    private  JComboBox<Locale> langList =new JComboBox<Locale>(LocaleHelper.getLocales());


    public OptionPanelForm(Launcher launcher) {
        super("Options");

        this.launcher = launcher;

        langList.addActionListener(this);
        langList.setSelectedItem(LocaleHelper.getCurrentLocale());


        createInterface();

        applyOptions();
    }

    private void applyOptions() {


    }

    @Override
    protected void populateGrid(GridBagConstraints constraints) {
        add(new JLabel("Language:", 2), constraints, 0, 0, 0, 1, 17);

        add(this.langList, constraints, 1, 0, 1, 1);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        JComboBox cb = (JComboBox)e.getSource();
       Locale locale = (Locale)cb.getSelectedItem();
        updateLocale(locale);
    }

    private void updateLocale(Locale locale) {
        LocaleHelper.setCurrentLocale(locale);
    }
}
