package net.minecraft.launcher.ui.bottombar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.locale.LocaleHelper;
import net.minecraft.launcher.updater.LowerCaseEnumTypeAdapterFactory;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class StatusPanelForm extends SidebarGridForm {
    private static ResourceBundle resourceBundle = LocaleHelper.getMessages();
    private static final String SERVER_WEBSITE = "minecraft.net";
    private static final String SERVER_ACCOUNT = "account.mojang.com";
    private static final String SERVER_AUTH = "auth.mojang.com";
    private static final String SERVER_SKINS = "skins.minecraft.net";

    private static final String SERVER_SESSION = "session.minecraft.net";
    private static final String SERVER_LOGIN = "login.minecraft.net";
    private final Launcher launcher;
    private final JLabel sessionStatus = new JLabel("???");
    private final JLabel loginStatus = new JLabel("???");
    private final JLabel websiteStatus = new JLabel("???");
    private final JLabel accountStatus = new JLabel("???");
    private final JLabel authStatus = new JLabel("???");
    private final JLabel skinsStatus = new JLabel("???");
    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory()).create();


    public StatusPanelForm(Launcher launcher) {
        this.launcher = launcher;

        createInterface();
        refreshStatuses();
    }

    protected void populateGrid(GridBagConstraints constraints) {
        add(new JLabel(resourceBundle.getString("multiplayer"), 2), constraints, 0, 0, 0, 1, 17);
        add(this.sessionStatus, constraints, 1, 0, 1, 1);

        add(new JLabel(resourceBundle.getString("login"), 2), constraints, 0, 1, 0, 1, 17);
        add(this.loginStatus, constraints, 1, 1, 1, 1);

        add(new JLabel(resourceBundle.getString("website"), 2), constraints, 0, 2, 0, 1, 17);
        add(this.websiteStatus, constraints, 1, 2, 1, 1);

        add(new JLabel(resourceBundle.getString("account"), 2), constraints, 0, 3, 0, 1, 17);
        add(this.accountStatus, constraints, 1, 3, 1, 1);

        add(new JLabel(resourceBundle.getString("auth"), 2), constraints, 0, 4, 0, 1, 17);
        add(this.authStatus, constraints, 1, 4, 1, 1);

        add(new JLabel(resourceBundle.getString("skins"), 2), constraints, 0, 5, 0, 1, 17);
        add(this.skinsStatus, constraints, 1, 5, 1, 1);
    }

    public JLabel getSessionStatus() {

        return this.sessionStatus;

    }

    public JLabel getLoginStatus() {

        return this.loginStatus;

    }

    public JLabel getWebsiteStatus() {
        return websiteStatus;
    }

    public JLabel getAccountStatus() {
        return accountStatus;
    }

    public JLabel getAuthStatus() {
        return authStatus;
    }

    public JLabel getSkinsStatus() {
        return skinsStatus;
    }

    public void refreshStatuses() {
        this.launcher.getVersionManager().getExecutorService().submit(new Runnable() {

            public void run() {
                try {
                    TypeToken<List<Map>> token = new TypeToken<List<Map>>() {
                    };
                    List<Map> statuses = (List<Map>) StatusPanelForm.this.gson.fromJson(Http.performGet(new URL("http://status.mojang.com/check"), StatusPanelForm.this.launcher.getProxy()), token.getType());

                    for (Map serverStatusInformation : (List<Map>) statuses)
                        if (serverStatusInformation.containsKey(SERVER_LOGIN))
                            StatusPanelForm.this.loginStatus.setText(ServerStatus.valueOf(serverStatusInformation.get(SERVER_LOGIN).toString().toUpperCase()).title);
                        else if (serverStatusInformation.containsKey(SERVER_SESSION))
                            StatusPanelForm.this.sessionStatus.setText(ServerStatus.valueOf(serverStatusInformation.get(SERVER_SESSION).toString().toUpperCase()).title);
                        else if (serverStatusInformation.containsKey(SERVER_WEBSITE))
                            StatusPanelForm.this.websiteStatus.setText(ServerStatus.valueOf(serverStatusInformation.get(SERVER_WEBSITE).toString().toUpperCase()).title);
                        else if (serverStatusInformation.containsKey(SERVER_ACCOUNT))
                            StatusPanelForm.this.accountStatus.setText(ServerStatus.valueOf(serverStatusInformation.get(SERVER_ACCOUNT).toString().toUpperCase()).title);
                        else if (serverStatusInformation.containsKey(SERVER_AUTH))
                            StatusPanelForm.this.authStatus.setText(ServerStatus.valueOf(serverStatusInformation.get(SERVER_AUTH).toString().toUpperCase()).title);
                        else if (serverStatusInformation.containsKey(SERVER_SKINS))
                            StatusPanelForm.this.skinsStatus.setText(ServerStatus.valueOf(serverStatusInformation.get(SERVER_SKINS).toString().toUpperCase()).title);
                } catch (Exception e) {
                    Launcher.getInstance().println("Couldn't get server status", e);
                }
            }
        });
    }

    public static enum ServerStatus {
        GREEN(resourceBundle.getString("online.no.problems.detected")),
        YELLOW(resourceBundle.getString("may.be.experiencing.issues")),
        RED(resourceBundle.getString("offline.experiencing.problems"));

        private final String title;

        private ServerStatus(String title) {
            this.title = title;
        }
    }
}