package net.minecraft.launcher.authentication;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Map;

public class AuthenticationSerializer
        implements JsonDeserializer<AuthenticationService>, JsonSerializer<AuthenticationService> {
    public AuthenticationService deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        AuthenticationService result = new SPAuthenticationService();
        if (json == null) return result;
        Map map = (Map) context.deserialize(json, Map.class);
        result.loadFromStorage(map);
        return result;
    }

    public JsonElement serialize(AuthenticationService src, Type typeOfSrc, JsonSerializationContext context) {
        Map map = src.saveForStorage();
        if ((map == null) || (map.isEmpty())) return null;

        return context.serialize(map);
    }
}
