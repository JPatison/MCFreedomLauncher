package net.minecraft.launcher.authentication;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.yggdrasil.YggdrasilAuthenticationService;

import java.lang.reflect.Type;
import java.util.*;

public class AuthenticationDatabase {
    public static final String DEMO_UUID_PREFIX = "demo-";
    private final Map<String, AuthenticationService> authById;

    public AuthenticationDatabase() {
        this(new HashMap());
    }

    public AuthenticationDatabase(Map<String, AuthenticationService> authById) {
        this.authById = authById;
    }

    public AuthenticationService getByName(String name) {
        if (name == null) return null;

        for (Map.Entry entry : this.authById.entrySet()) {
            GameProfile profile = ((AuthenticationService) entry.getValue()).getSelectedProfile();

            if ((profile != null) && (profile.getName().equals(name)))
                return (AuthenticationService) entry.getValue();
            if ((profile == null) && (getUserFromDemoUUID((String) entry.getKey()).equals(name))) {
                return (AuthenticationService) entry.getValue();
            }
        }

        return null;
    }

    public AuthenticationService getByUUID(String uuid) {
        return (AuthenticationService) this.authById.get(uuid);
    }

    public Collection<String> getKnownNames() {
        List names = new ArrayList();

        for (Map.Entry entry : this.authById.entrySet()) {
            GameProfile profile = ((AuthenticationService) entry.getValue()).getSelectedProfile();

            if (profile != null)
                names.add(profile.getName());
            else {
                names.add(getUserFromDemoUUID((String) entry.getKey()));
            }
        }

        return names;
    }

    public void register(String uuid, AuthenticationService authentication) {
        this.authById.put(uuid, authentication);
    }

    public Set<String> getknownUUIDs() {
        return this.authById.keySet();
    }

    public void removeUUID(String uuid) {
        this.authById.remove(uuid);
    }

    public static String getUserFromDemoUUID(String uuid) {
        if ((uuid.startsWith("demo-")) && (uuid.length() > "demo-".length())) {
            return "Demo User " + uuid.substring("demo-".length());
        }
        return "Demo User";
    }

    public static class Serializer
            implements JsonDeserializer<AuthenticationDatabase>, JsonSerializer<AuthenticationDatabase> {
        public AuthenticationDatabase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            TypeToken<Map> token = new TypeToken<Map>() {
            };
            Map services = new HashMap();
            Map credentials = (Map) context.deserialize(json, token.getType());

            for (Map.Entry entry : ((Map<String, String>) credentials).entrySet()) {
                AuthenticationService service = Launcher.isSPMode() ? new SPAuthenticationService() : new YggdrasilAuthenticationService();
                service.loadFromStorage((Map) entry.getValue());
                services.put(entry.getKey(), service);
            }

            return new AuthenticationDatabase(services);
        }

        public JsonElement serialize(AuthenticationDatabase src, Type typeOfSrc, JsonSerializationContext context) {
            Map services = src.authById;
            Map credentials = new HashMap();

            for (Map.Entry entry : ((Map<String, String>) services).entrySet()) {
                credentials.put(entry.getKey(), ((AuthenticationService) entry.getValue()).saveForStorage());
            }

            return context.serialize(credentials);
        }
    }
}