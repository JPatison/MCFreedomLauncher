package net.minecraft.launcher.ui.tabs;

import net.minecraft.launcher.Launcher;

import javax.swing.*;
import java.awt.*;

public class LauncherTabPanel extends JTabbedPane {
    private final Launcher launcher;
    private final WebsiteTab blog;
    private final ConsoleTab console;
    private CrashReportTab crashReportTab;

    public LauncherTabPanel(Launcher launcher) {
        super(1);

        this.launcher = launcher;
        this.blog = new WebsiteTab(launcher);
        this.console = new ConsoleTab(launcher);

        createInterface();
    }

    protected void createInterface() {
        addTab("Update Notes", this.blog);
        addTab("Development Console", this.console);
        addTab("Profile Editor", new ProfileListTab(this.launcher));
        addTab("Local Version Editor (NYI)", new VersionListTab(this.launcher));
    }

    public Launcher getLauncher() {
        return this.launcher;
    }

    public WebsiteTab getBlog() {
        return this.blog;
    }

    public ConsoleTab getConsole() {
        return this.console;
    }

    public void showConsole() {
        setSelectedComponent(this.console);
    }

    public void setCrashReport(CrashReportTab newTab) {
        if (this.crashReportTab != null) removeTab(this.crashReportTab);
        this.crashReportTab = newTab;
        addTab("Crash Report", this.crashReportTab);
        setSelectedComponent(newTab);
    }

    protected void removeTab(Component tab) {
        for (int i = 0; i < getTabCount(); i++)
            if (getTabComponentAt(i) == tab) {
                removeTabAt(i);
                break;
            }
    }
}


