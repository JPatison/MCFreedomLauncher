package net.minecraft.launcher;

import net.minecraft.hopper.Util;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Map;

public class Http {
    public static String buildQuery(Map<String, Object> query) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry entry : query.entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            try {
                builder.append(URLEncoder.encode((String) entry.getKey(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Launcher.getInstance().println("Unexpected exception building query", e);
            }

            if (entry.getValue() != null) {
                builder.append('=');
                try {
                    builder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    Launcher.getInstance().println("Unexpected exception building query", e);
                }
            }
        }

        return builder.toString();
    }

    public static String performPost(URL url, Map<String, Object> query, Proxy proxy) throws IOException {
        return Util.performPost(url, buildQuery(query), proxy, "application/x-www-form-urlencoded", false);
    }

  public static String performGet(URL url, Proxy proxy) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        connection.setConnectTimeout(15000);
    connection.setReadTimeout(60000);
    connection.setRequestMethod("GET");

    InputStream inputStream = connection.getInputStream();
        try {
      return IOUtils.toString(inputStream);
    } finally {
      IOUtils.closeQuietly(inputStream);
        }
    }

    public static URL concatenateURL(URL url, String args) throws MalformedURLException {
        if ((url.getQuery() != null) && (url.getQuery().length() > 0)) {
            return new URL(url.getProtocol(), url.getHost(), url.getFile() + "?" + args);
        }
        return new URL(url.getProtocol(), url.getHost(), url.getFile() + "&" + args);
    }
}
