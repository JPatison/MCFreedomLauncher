package net.minecraft.launcher.ui;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.ui.bottombar.PlayButtonPanel;
import net.minecraft.launcher.ui.bottombar.PlayerInfoPanel;
import net.minecraft.launcher.ui.bottombar.ProfileSelectionPanel;
import net.minecraft.launcher.ui.bottombar.SettingsPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BottomBarPanel extends JPanel {
    private final Launcher launcher;
    private final ProfileSelectionPanel profileSelectionPanel;
    private final PlayerInfoPanel playerInfoPanel;
    private final PlayButtonPanel playButtonPanel;
    private  final SettingsPanel settingsPanel;

    public BottomBarPanel(Launcher launcher) {
        this.launcher = launcher;

        int border = 4;
        setBorder(new EmptyBorder(border, border, border, border));

        this.profileSelectionPanel = new ProfileSelectionPanel(launcher);
        this.playerInfoPanel = new PlayerInfoPanel(launcher);
        this.playButtonPanel = new PlayButtonPanel(launcher);
        this.settingsPanel = new SettingsPanel(launcher);



        createInterface();
    }

    protected void createInterface() {
        setLayout(new GridLayout(1, 4));

        add(wrapSidePanel(this.profileSelectionPanel, 17));


        add(this.settingsPanel);


        add(this.settingsPanel);

        add(this.playButtonPanel);

        add(wrapSidePanel(this.playerInfoPanel, 13));

    }

    protected JPanel wrapSidePanel(JPanel target, int side) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = side;
        constraints.weightx = 1.0D;
        constraints.weighty = 1.0D;


        wrapper.add(target, constraints);

        return wrapper;
    }

    public Launcher getLauncher() {
        return this.launcher;
    }

    public ProfileSelectionPanel getProfileSelectionPanel() {
        return this.profileSelectionPanel;
    }

    public PlayerInfoPanel getPlayerInfoPanel() {
        return this.playerInfoPanel;
    }

    public PlayButtonPanel getPlayButtonPanel() {
        return this.playButtonPanel;
    }
}