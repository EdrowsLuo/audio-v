package com.edlplan.audiov;

import android.os.Environment;

import com.edlplan.audiov.core.utils.Getter;

import java.util.HashMap;

public class GlobalVar {

    public static final String EXTERNAL_PATH = "external_path";

    public static final String INTERNAL_PATH = "internal_path";

    private static HashMap<String, Getter<String>> valueMap = new HashMap<>();

    private static HashMap<String, String> translationMap = new HashMap<>();

    static {
        registerValue(EXTERNAL_PATH, () -> {
            String p = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (p.endsWith("/")) {
                p = p.substring(0, p.length() - 1);
            }
            return p;
        });
    }

    public static void registerValue(String key, Getter<String> v) {
        valueMap.put(key, v);
    }

    public static String parseValue(String str) {

        if (str.startsWith("@")) {
            String body = str.substring(1);
            String get = translationMap.get(body);
            return get == null ? body : get;
        }

        int pos1 = str.indexOf('#');
        if (pos1 != -1) {
            int pos2 = str.indexOf('#', pos1 + 1);
            if (pos2 != -1) {
                String key = str.substring(pos1 + 1, pos2);
                if (valueMap.containsKey(key)) {
                    return parseValue(str.replace("#" + key + "#", valueMap.get(key).get()));
                } else {
                    return str;
                }
            } else {
                return str;
            }
        } else {
            return str;
        }
    }

}
