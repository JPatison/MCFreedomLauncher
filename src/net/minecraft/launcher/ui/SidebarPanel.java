package net.minecraft.launcher.ui;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.ui.sidebar.ProfileSelection;
import net.minecraft.launcher.ui.sidebar.StatusPanelForm;
import net.minecraft.launcher.ui.sidebar.login.LoginContainerForm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SidebarPanel extends JPanel {
    private final Launcher launcher;
    private final LoginContainerForm loginForm;
    private final ProfileSelection profileSelection;
    private final StatusPanelForm serverStatus;

    public SidebarPanel(Launcher launcher) {
        this.launcher = launcher;

        //setPreferredSize(new Dimension(250, 1));

        int border = 4;
        setBorder(new EmptyBorder(border, border, border, border));

        this.loginForm = new LoginContainerForm(launcher);
        this.profileSelection = new ProfileSelection(launcher);
        this.serverStatus = new StatusPanelForm(launcher);

        createInterface();
    }

    protected void createInterface() {
        setLayout(new BoxLayout(this, 1));
        add(this.profileSelection);
        add(this.serverStatus);

       // add(new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(2147483647, 2147483647)));
        add(new Box.Filler(new Dimension(0, 0), new Dimension(0, 32767), new Dimension(0, 32767)));
        add(this.loginForm);
    }

    public LoginContainerForm getLoginForm() {
        return this.loginForm;
    }

    public Launcher getLauncher() {
        return this.launcher;
    }
}


