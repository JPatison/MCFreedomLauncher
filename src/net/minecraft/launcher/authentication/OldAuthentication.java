package net.minecraft.launcher.authentication;

import net.minecraft.launcher.Launcher;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Proxy;
import java.util.Random;

public class OldAuthentication {
    private final Proxy proxy;
    private final File lastLoginFile;
    private Response lastSuccessfulResponse;
    private boolean isAuthenticating;

    public OldAuthentication(Launcher launcher, Proxy proxy) {
        this.lastLoginFile = new File(launcher.getWorkingDirectory(), "lastlogin");
        this.proxy = proxy;
    }

    public Response login(String username, String password) throws IOException {
        this.lastSuccessfulResponse = new Response(username, null, "SessionID", username, "uuid");
        return this.lastSuccessfulResponse;
    }

    public StoredDetails getStoredDetails() {
        if (!this.lastLoginFile.isFile()) return null;
        try {
            Cipher cipher = getCipher(2, "passwordfile");
            DataInputStream dis;

            if (cipher != null)
                dis = new DataInputStream(new CipherInputStream(new FileInputStream(this.lastLoginFile), cipher));
            else {
                dis = new DataInputStream(new FileInputStream(this.lastLoginFile));
            }

            String username = dis.readUTF();
            String password = dis.readUTF();
            String displayName = username.length() > 0 ? username.split("@")[0] : "";
            StoredDetails result = new StoredDetails(username, password, displayName, null);
            dis.close();
            return result;
        } catch (Exception e) {
            Launcher.getInstance().println("Couldn't load old lastlogin file", e);
        }
        return null;
    }

    public String guessPasswordFromSillyOldFormat(String username) {
        StoredDetails details = getStoredDetails();

        if ((details != null) && (details.getUsername().equals(username))) {
            return details.getPassword();
        }

        return null;
    }

    private Cipher getCipher(int mode, String password) throws Exception {
        Random random = new Random(43287234L);
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

        SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(mode, pbeKey, pbeParamSpec);
        return cipher;
    }

    public Response getLastSuccessfulResponse() {
        return this.lastSuccessfulResponse;
    }

    public void setLastSuccessfulResponse(Response lastSuccessfulResponse) {
        this.lastSuccessfulResponse = lastSuccessfulResponse;
    }

    public void clearLastSuccessfulResponse() {
        this.lastSuccessfulResponse = null;
    }

    public synchronized boolean isAuthenticating() {
        return this.isAuthenticating;
    }

    public synchronized void setAuthenticating(boolean authenticating) {
        this.isAuthenticating = authenticating;
    }

    public static class StoredDetails {
        private final String username;
        private final String password;
        private final String displayName;
        private final String uuid;

        public StoredDetails(String username, String password, String displayName, String uuid) {
            this.username = username;
            this.password = password;
            this.displayName = displayName;
            this.uuid = uuid;
        }

        public String getUsername() {
            return this.username;
        }

        public String getPassword() {
            return this.password;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getUUID() {
            return this.uuid;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if ((o == null) || (getClass() != o.getClass())) return false;

            StoredDetails that = (StoredDetails) o;

            if (this.username != null ? !this.username.equals(that.username) : that.username != null) return false;
            if (this.uuid != null ? !this.uuid.equals(that.uuid) : that.uuid != null) return false;

            return true;
        }

        public int hashCode() {
            int result = this.username != null ? this.username.hashCode() : 0;
            result = 31 * result + (this.uuid != null ? this.uuid.hashCode() : 0);
            return result;
        }
    }

    public static class Response {
        private final String username;
        private final String errorMessage;
        private final String sessionId;
        private final String playerName;
        private final String uuid;

        public Response(String username, String errorMessage, String sessionId, String playerName, String uuid) {
            this.username = username;
            this.errorMessage = errorMessage;
            this.sessionId = sessionId;
            this.playerName = playerName;
            this.uuid = uuid;
        }

        public String getUsername() {
            return this.username;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }

        public String getSessionId() {
            return this.sessionId;
        }

        public String getPlayerName() {
            return this.playerName;
        }

        public String getUUID() {
            return this.uuid;
        }

        public boolean isOnline() {
            return this.sessionId != null;
        }
    }
}
