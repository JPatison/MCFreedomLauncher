package net.minecraft.launcher.ui.tabs;

import net.minecraft.hopper.HopperService;
import net.minecraft.hopper.PublishResponse;
import net.minecraft.hopper.SubmitResponse;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class CrashReportTab extends JPanel {
    private final Launcher launcher;
    private final CompleteVersion version;
    private final File reportFile;
    private final String report;
    //private final HopperService hopper = new HopperService();
    private final JEditorPane reportEditor = new JEditorPane();
    private final JScrollPane scrollPane = new JScrollPane(this.reportEditor);
    private final CrashInfoPane crashInfoPane;
    private final boolean isModded;
    private SubmitResponse hopperServiceResponse;

    public CrashReportTab(final Launcher launcher, final CompleteVersion version, File reportFile, final String report) {
        super(true);
        this.launcher = launcher;
        this.version = version;
        this.reportFile = reportFile;
        this.report = report;
        this.crashInfoPane = new CrashInfoPane(launcher);

        if ((report.contains("Is Modded: Probably not")) || (report.contains("Is Modded: Unknown")))
            this.isModded = (!report.contains("Suspicious classes: No suspicious classes found."));
        else {
            this.isModded = true;
        }

        setLayout(new BorderLayout());
        createInterface();

        if (launcher.getProfileManager().getSelectedProfile().getUseHopperCrashService())
            launcher.getVersionManager().getExecutorService().submit(new Runnable() {
                public void run() {
                    try {
                        Map environment = new HashMap();
                        environment.put("launcher.version", LauncherConstants.VERSION_NAME);
                        environment.put("launcher.title", launcher.getFrame().getTitle());
                        environment.put("bootstrap.version", String.valueOf(launcher.getBootstrapVersion()));
                        CrashReportTab.this.hopperServiceResponse = HopperService.submitReport(launcher.getProxy(), report, "Minecraft", version.getId(), environment);
                        launcher.println("Reported crash to Mojang (ID " + CrashReportTab.this.hopperServiceResponse.getReport().getId() + ")");

                        if (CrashReportTab.this.hopperServiceResponse.getProblem() != null)
                            CrashReportTab.this.showKnownProblemPopup();
                        else if (CrashReportTab.this.hopperServiceResponse.getReport().canBePublished())
                            CrashReportTab.this.showPublishReportPrompt();
                    } catch (IOException e) {
                        launcher.println("Couldn't report crash to Mojang", e);
                    }
                }
            });
    }

    private void showPublishReportPrompt() {
        String[] options = {"Publish Crash Report", "Cancel"};
        JLabel message = new JLabel();

        message.setText("<html><p>Sorry, but it looks like the game crashed and we don't know why.</p><p>Would you mind publishing this report so that " + (this.isModded ? "the mod authors" : "Mojang") + " can fix it?</p></html>");

        int result = JOptionPane.showOptionDialog(this, message, "Uhoh, something went wrong!", 0, 1, null, options, options[0]);

        if (result == 0)
            try {
                PublishResponse localPublishResponse = HopperService.publishReport(this.launcher.getProxy(), this.hopperServiceResponse.getReport());
            } catch (IOException e) {
                this.launcher.println("Couldn't publish report " + this.hopperServiceResponse.getReport().getId(), e);
            }
    }

    private void showKnownProblemPopup() {
        if (this.hopperServiceResponse.getProblem().getUrl() == null) {
            JOptionPane.showMessageDialog(this, this.hopperServiceResponse.getProblem().getDescription(), this.hopperServiceResponse.getProblem().getTitle(), 1);
        } else {
            String[] options = {"Fix The Problem", "Cancel"};
            int result = JOptionPane.showOptionDialog(this, this.hopperServiceResponse.getProblem().getDescription(), this.hopperServiceResponse.getProblem().getTitle(), 0, 1, null, options, options[0]);

            if (result == 0)
                try {
                    OperatingSystem.openLink(new URI(this.hopperServiceResponse.getProblem().getUrl()));
                } catch (URISyntaxException e) {
                    this.launcher.println("Couldn't open help page ( " + this.hopperServiceResponse.getProblem().getUrl() + "  ) for crash", e);
                }
        }
    }

    protected void createInterface() {
        add(this.crashInfoPane, "North");
        add(this.scrollPane, "Center");

        this.reportEditor.setText(this.report);

        this.crashInfoPane.createInterface();
    }

    private class CrashInfoPane extends JPanel
            implements ActionListener {
        public static final String INFO_NORMAL = "<html><div style='width: 100%'><p><b>Uhoh, it looks like the game has crashed! Sorry for the inconvenience :(</b></p><p>Using magic and love, we've managed to gather some details about the crash and we will investigate this as soon as we can.</p><p>You can see the full report below.</p></div></html>";
        public static final String INFO_MODDED = "<html><div style='width: 100%'><p><b>Uhoh, it looks like the game has crashed! Sorry for the inconvenience :(</b></p><p>We think your game may be modded, and as such we can't accept this crash report.</p><p>However, if you do indeed use mods, please send this to the mod authors to take a look at!</p></div></html>";
        private final JButton submitButton = new JButton("Report to Mojang");
        private final JButton openFileButton = new JButton("Open report file");

        protected CrashInfoPane(Launcher launcher) {
            this.submitButton.addActionListener(this);
            this.openFileButton.addActionListener(this);
        }

        protected void createInterface() {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();

            constraints.anchor = 13;
            constraints.fill = 2;
            constraints.insets = new Insets(2, 2, 2, 2);

            constraints.gridx = 1;
            add(this.submitButton, constraints);
            constraints.gridy = 1;
            add(this.openFileButton, constraints);

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 1.0D;
            constraints.weighty = 1.0D;
            constraints.gridheight = 2;
            // add(new JLabel(CrashReportTab.this.isModded ? "<html><div style='width: 100%'><p><b>Uhoh, it looks like the game has crashed! Sorry for the inconvenience :(</b></p><p>We think your game may be modded, and as such we can't accept this crash report.</p><p>However, if you do indeed use mods, please send this to the mod authors to take a look at!</p></div></html>" : "<html><div style='width: 100%'><p><b>Uhoh, it looks like the game has crashed! Sorry for the inconvenience :(</b></p><p>Using magic and love, we've managed to gather some details about the crash and we can hopefully try to fix it if you report it to us.</p><p>You can see the full report below, and we'd really appreciate it if you send it to us to take a look at.</p></div></html>"), constraints);
            add(new JLabel(CrashReportTab.this.isModded ? "<html><div style='width: 100%'><p><b>Uhoh, it looks like the game has crashed! Sorry for the inconvenience :(</b></p><p>We think your game may be modded, and as such we can't accept this crash report.</p><p>However, if you do indeed use mods, please send this to the mod authors to take a look at!</p></div></html>" : "<html><div style='width: 100%'><p><b>Uhoh, it looks like the game has crashed! Sorry for the inconvenience :(</b></p><p>Using magic and love, we've managed to gather some details about the crash and we will investigate this as soon as we can.</p><p>You can see the full report below.</p></div></html>"), constraints);

            if (CrashReportTab.this.isModded)
                this.submitButton.setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == this.submitButton) {
                if (CrashReportTab.this.hopperServiceResponse != null) {
                    if (CrashReportTab.this.hopperServiceResponse.getProblem() != null)
                        CrashReportTab.this.showKnownProblemPopup();
                    else if (CrashReportTab.this.hopperServiceResponse.getReport().canBePublished())
                        CrashReportTab.this.showPublishReportPrompt();
                } else
                    try {
                        Map args = new HashMap();

                        args.put("pid", Integer.valueOf(10400));
                        args.put("issuetype", Integer.valueOf(1));
                        args.put("description", "Put the summary of the bug you're having here\n\n*What I expected to happen was...:*\nDescribe what you thought should happen here\n\n*What actually happened was...:*\nDescribe what happened here\n\n*Steps to Reproduce:*\n1. Put a step by step guide on how to trigger the bug here\n2. ...\n3. ...");

                        args.put("environment", buildEnvironmentInfo());

                        OperatingSystem.openLink(URI.create("https://mojang.atlassian.net/secure/CreateIssueDetails!init.jspa?" + Http.buildQuery(args)));
                    } catch (Throwable ex) {
                        Launcher.getInstance().println("Couldn't open bugtracker", ex);
                    }
            } else if (e.getSource() == this.openFileButton)
                OperatingSystem.openLink(CrashReportTab.this.reportFile.toURI());
        }

        private String buildEnvironmentInfo() {
            StringBuilder result = new StringBuilder();

            result.append("OS: ");
            result.append(System.getProperty("os.name"));
            result.append(" (ver ");
            result.append(System.getProperty("os.version"));
            result.append(", arch ");
            result.append(System.getProperty("os.arch"));
            result.append(")\nJava: ");
            result.append(System.getProperty("java.version"));
            result.append(" (by ");
            result.append(System.getProperty("java.vendor"));
            result.append(")\nLauncher: ");
            result.append(LauncherConstants.VERSION_NAME);
            result.append(" (bootstrap ");
            result.append(Launcher.getInstance().getBootstrapVersion());
            result.append(")\nMinecraft: ");
            result.append(CrashReportTab.this.version.getId());
            result.append(" (updated ");
            result.append(CrashReportTab.this.version.getUpdatedTime());
            result.append(")");

            return result.toString();
        }
    }
}
