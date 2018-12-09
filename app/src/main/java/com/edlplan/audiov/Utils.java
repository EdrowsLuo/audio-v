package com.edlplan.audiov;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static void checkFile(File file) {
        checkDir(file.getParentFile());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void checkDir(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

}
