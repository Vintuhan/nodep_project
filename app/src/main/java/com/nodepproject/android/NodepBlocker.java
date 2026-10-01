package com.nodepproject.android;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NodepBlocker {
    private final SharedPreferences prefs;
    private final Set<String> base = new HashSet<>();

    public NodepBlocker(Context context) {
        prefs = context.getSharedPreferences("nodep", Context.MODE_PRIVATE);
        loadBase(context);
    }

    private void loadBase(Context context) {
        try {
            String json;
            try (BufferedReader r = new BufferedReader(new InputStreamReader(
                    context.getAssets().open("nodep/data/blocklist.json"), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder(); String line;
                while ((line = r.readLine()) != null) sb.append(line);
                json = sb.toString();
            }
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) addBase(arr.optString(i));
        } catch (Exception ignored) {}
    }

    private void addBase(String host) {
        String h = normalize(host);
        if (!h.isEmpty()) base.add(h);
    }

    public boolean isBlocked(String hostname) {
        String h = normalize(hostname);
        if (h.isEmpty() || isLocal(h) || isWhitelisted(h)) return false;
        Set<String> custom = getCustom();
        String[] labels = h.split("\\.");
        for (int i = 0; i < labels.length - 1; i++) {
            String d = join(labels, i);
            if (base.contains(d) || custom.contains(d)) return true;
        }
        return false;
    }

    private boolean isWhitelisted(String host) {
        // The full whitelist is enforced inside the injected JS from the user's original data.
        // A small protocol-level safety whitelist is kept here to avoid blocking local/private hosts.
        if (host.equals("localhost") || host.endsWith(".localhost")) return true;
        return host.endsWith(".gov") || host.endsWith(".edu") || host.endsWith(".mil");
    }

    private boolean isLocal(String h) { return h.equals("127.0.0.1") || h.equals("0.0.0.0") || h.equals("::1"); }

    public void addCustom(String host) {
        String h = normalize(host); if (h.isEmpty()) return;
        Set<String> set = getCustom(); set.add(h); saveCustom(set);
    }

    public Set<String> getCustom() {
        return new HashSet<>(prefs.getStringSet("custom", new HashSet<>()));
    }

    private void saveCustom(Set<String> set) { prefs.edit().putStringSet("custom", set).apply(); }

    public void trustForDay(String host) {
        String h = normalize(host); if (h.isEmpty()) return;
        Set<String> trusted = new HashSet<>(prefs.getStringSet("trusted", new HashSet<>()));
        trusted.add(h + "|" + (System.currentTimeMillis() + 24L * 60 * 60 * 1000));
        prefs.edit().putStringSet("trusted", trusted).apply();
    }

    public boolean isTrustedForDay(String host) {
        String h=normalize(host); long now=System.currentTimeMillis();
        for(String item:new HashSet<>(prefs.getStringSet("trusted",new HashSet<>()))) {
            String[] p=item.split("\\|"); if(p.length==2 && p[0].equals(h)) { try { return Long.parseLong(p[1])>now; } catch(Exception ignored){} }
        }
        return false;
    }

    private static String normalize(String s) {
        String h = String.valueOf(s == null ? "" : s).toLowerCase().trim();
        if (h.startsWith("https://")) h=h.substring(8);
        else if(h.startsWith("http://")) h=h.substring(7);
        int slash=h.indexOf('/'); if(slash>=0)h=h.substring(0,slash);
        if(h.startsWith("www."))h=h.substring(4);
        int colon=h.indexOf(':'); if(colon>=0)h=h.substring(0,colon);
        return h.replaceAll("\\.$","");
    }

    private static String join(String[] a,int from){StringBuilder s=new StringBuilder();for(int i=from;i<a.length;i++){if(i>from)s.append('.');s.append(a[i]);}return s.toString();}
}
