package com.edlplan.audiov.scan;

import java.util.HashMap;

public class ScannerTypeMapper {

    private static HashMap<String, String> map = new HashMap<>();

    static {
        map.put(FolderScanner.class.getSimpleName(), FolderScanner.class.getName());
        map.put(DirectCacheScanner.class.getSimpleName(), DirectCacheScanner.class.getName());
    }

    public static String map(String raw) {
        if (map.containsKey(raw)) {
            return map.get(raw);
        } else {
            return raw;
        }
    }

}
