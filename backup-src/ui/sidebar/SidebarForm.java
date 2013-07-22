package net.minecraft.launcher.ui.sidebar;

import javax.swing.*;

public abstract class SidebarForm extends JPanel {
    public SidebarForm(String name) {
        setBorder(BorderFactory.createTitledBorder(name));
    }

    protected abstract void createInterface();
}


