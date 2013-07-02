package net.minecraft.launcher.ui;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.ui.tabs.LauncherTabPanel;

import javax.swing.*;
import java.awt.*;

public class LauncherPanel extends JPanel {
    private final LauncherTabPanel tabPanel;
    private final SidebarPanel sidebar;
    private final JProgressBar progressBar;
    private final Launcher launcher;

    public LauncherPanel(Launcher launcher) {
        this.launcher = launcher;

        setLayout(new BorderLayout());

        this.progressBar = new JProgressBar();
        this.sidebar = new SidebarPanel(launcher);
        this.tabPanel = new LauncherTabPanel(launcher);
        createInterface();
    }

    protected void createInterface() {
        this.tabPanel.getBlog().setPage("http://mcupdate.tumblr.com");

        this.progressBar.setVisible(false);
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(100);

        add(this.tabPanel, "Center");
        add(this.sidebar, "East");
        add(this.progressBar, "South");
    }

    public LauncherTabPanel getTabPanel() {
        return this.tabPanel;
    }

    public SidebarPanel getSidebar() {
        return this.sidebar;
    }

    public JProgressBar getProgressBar() {
        return this.progressBar;
    }

    public Launcher getLauncher() {
        return this.launcher;
    }
}


