package net.minecraft.launcher.authentication;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.exceptions.AuthenticationException;
import net.minecraft.launcher.authentication.exceptions.InvalidCredentialsException;
import net.minecraft.launcher.events.AuthenticationChangedListener;
import org.apache.commons.lang3.StringUtils;
import org.hopto.energy.HashUtil;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Created by Energy on 13年7月2日.
 */
public class SPAuthenticationService implements AuthenticationService {
    private static final String LEGACY_LASTLOGIN_PASSWORD = "passwordfile";
    private static final int LEGACY_LASTLOGIN_SEED = 43287234;
    private final List<AuthenticationChangedListener> listeners = new ArrayList();
    private String sessionToken = "";
    private String username = "";
    private String password = "";
    private String uuid = "";
    private GameProfile selectedProfile = null;
    private boolean shouldRememberMe = true;

    public static String[] getStoredDetails(File lastLoginFile) {
        if (!lastLoginFile.isFile()) return null;
        try {
            Cipher cipher = getCipher(2, "passwordfile");
            DataInputStream dis;

            if (cipher != null)
                dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLoginFile), cipher));
            else {
                dis = new DataInputStream(new FileInputStream(lastLoginFile));
            }

            String username = dis.readUTF();
            String password = dis.readUTF();
            dis.close();
            return new String[]{username, password};
        } catch (Exception e) {
            Launcher.getInstance().println("Couldn't load old lastlogin file", e);
        }
        return null;
    }

    private static Cipher getCipher(int mode, String password) throws Exception {
        Random random = new Random(43287234L);
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

        SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(mode, pbeKey, pbeParamSpec);
        return cipher;
    }

    @Override
    public boolean canLogIn() {
        return !canPlayOnline();
    }

    @Override
    public void logIn() throws AuthenticationException {

        Map args = new HashMap();
        args.put("user", getUsername());
        args.put("password", getPassword());
        args.put("version", Integer.valueOf(14));
        uuid = HashUtil.getMD5(getUsername());
        String response = "0:0:" + getUsername() + ":0:" + uuid;
        /*try { response = Http.performPost(AUTHENTICATION_URL, args, Launcher.getInstance().getProxy()).trim();
        } catch (IOException e) {
            throw new AuthenticationException("Authentication server is not responding", e);
        }*/

        String[] split = response.split(":");

        if (split.length == 5) {
            //  String profileId = Long.toString(System.currentTimeMillis());
            String profileId = split[4];
            String profileName = split[2];
            String sessionToken = split[3];

            if ((StringUtils.isBlank(profileId)) || (StringUtils.isBlank(profileName)) || (StringUtils.isBlank(sessionToken))) {
                throw new AuthenticationException("Unknown response from authentication server: " + response);
            }

            setSelectedProfile(new GameProfile(profileId, profileName));
            this.sessionToken = sessionToken;
            fireAuthenticationChangedEvent();
        } else {
            throw new InvalidCredentialsException(response);
        }
    }

    @Override
    public void logOut() {
        this.password = null;
        setSelectedProfile(null);
        this.sessionToken = null;
        fireAuthenticationChangedEvent();
    }

    @Override
    public boolean isLoggedIn() {
        return getSelectedProfile() != null;
    }

    @Override
    public boolean canPlayOnline() {
        return (isLoggedIn()) && (getSelectedProfile() != null) && (getSessionToken() != null);
    }

    @Override
    public GameProfile[] getAvailableProfiles() {
        if (getSelectedProfile() != null) {
            return new GameProfile[]{getSelectedProfile()};
        }
        return new GameProfile[0];
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public GameProfile getSelectedProfile() {
        return this.selectedProfile;
    }

    public void setSelectedProfile(GameProfile selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    @Override
    public void selectGameProfile(GameProfile paramGameProfile) throws AuthenticationException {

    }

    @Override
    public void loadFromStorage(Map<String, String> credentials) {
        logOut();

        if (credentials.containsKey("rememberMe")) {
            setRememberMe(Boolean.getBoolean((String) credentials.get("rememberMe")));
        }

        setUsername((String) credentials.get("username"));

        if ((credentials.containsKey("displayName")) && (credentials.containsKey("uuid")))
            setSelectedProfile(new GameProfile((String) credentials.get("uuid"), (String) credentials.get("displayName")));
    }

    public Map<String, String> saveForStorage() {
        Map result = new HashMap();

        if (!shouldRememberMe()) {
            result.put("rememberMe", Boolean.toString(false));
            return result;
        }


        if (getUsername() != null) {
            result.put("username", getUsername());
        }

        if (getSelectedProfile() != null) {
            result.put("displayName", getSelectedProfile().getName());
            result.put("uuid", getSelectedProfile().getId());
        }

        return result;
    }

    public boolean shouldRememberMe() {
        return this.shouldRememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.shouldRememberMe = rememberMe;
    }


    @Override
    public String getSessionToken() {
        return this.sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    protected String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    public void addAuthenticationChangedListener(AuthenticationChangedListener listener) {
        this.listeners.add(listener);
    }

    public void removeAuthenticationChangedListener(AuthenticationChangedListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public String guessPasswordFromSillyOldFormat(File file) {
        String[] details = getStoredDetails(file);

        if ((details != null) &&
                (details[0].equals(getUsername()))) {
            return details[1];
        }

        return null;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(getClass().getSimpleName());
        result.append("{");

        if (isLoggedIn()) {
            result.append("Logged in as ");
            result.append(getUsername());

            if (getSelectedProfile() != null) {
                result.append(" / ");
                result.append(getSelectedProfile());
                result.append(" - ");

                if (canPlayOnline()) {
                    result.append("Online with session token '");
                    result.append(getSessionToken());
                    result.append("'");
                } else {
                    result.append("Offline");
                }
            }
        } else {
            result.append("Not logged in");
        }

        result.append("}");

        return result.toString();
    }

    protected void fireAuthenticationChangedEvent() {
        final List listeners = new ArrayList(this.listeners);

        for (Iterator iterator = listeners.iterator(); iterator.hasNext(); ) {
            AuthenticationChangedListener listener = (AuthenticationChangedListener) iterator.next();

            if (!listener.shouldReceiveEventsInUIThread()) {
                listener.onAuthenticationChanged(this);
                iterator.remove();
            }
        }

        if (!listeners.isEmpty())
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for (AuthenticationChangedListener listener : (ArrayList<AuthenticationChangedListener>) listeners)
                        listener.onAuthenticationChanged(SPAuthenticationService.this);
                }
            });
    }


}