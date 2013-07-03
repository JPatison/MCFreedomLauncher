package net.minecraft.launcher.ui.sidebar;

import net.minecraft.launcher.Launcher;

import java.awt.*;

/**
 * Created by Energy on 13年7月4日.
 */
public class OptionPanelForm  extends SidebarGridForm{
    private final Launcher launcher;

    public OptionPanelForm(Launcher launcher) {
        super("Options");

        this.launcher = launcher;


        createInterface();

        applyOptions();
    }

    private void applyOptions() {

    }

    @Override
    protected void populateGrid(GridBagConstraints paramGridBagConstraints) {

    }
}
