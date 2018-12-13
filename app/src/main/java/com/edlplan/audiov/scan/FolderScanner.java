package com.edlplan.audiov.scan;

import com.edlplan.audiov.GlobalVar;
import com.edlplan.audiov.core.utils.Consumer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FolderScanner implements ISongListScanner {

    public static final String DEFAULT_PATTERN = ".*\\.(mp3)"; //默认播放器只支持mp3

    public static final int SCAN_DEPTH_INF = -1;

    public static final String KEY_FILE = "file";

    public static final String KEY_NAME_PATTERN = "pattern";

    public static final String KEY_SCAN_DEPTH = "depth";

    private File file;

    private Pattern namePattern;

    private int scanDepth = SCAN_DEPTH_INF;

    public FolderScanner() {

    }

    public void setNamePattern(Pattern namePattern) {
        this.namePattern = namePattern;
    }

    public Pattern getNamePattern() {
        return namePattern;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    /**
     * 设置扫描的最大深度，-1时为无限制。
     * 例如设置为0时只扫描当前目录
     * @param scanDepth 目标深度
     */
    public void setScanDepth(int scanDepth) {
        this.scanDepth = scanDepth;
    }

    public int getScanDepth() {
        return scanDepth;
    }

    protected String parseNameOfFile(File file) {
        String name = file.getName();
        if (name.contains(".")) {
            return name.substring(0, name.lastIndexOf('.'));
        } else {
            return name;
        }
    }

    private void scan(Consumer<SongEntry> consumer, File file, int depth) {
        if (depth > scanDepth || file.isFile()) {
            return;
        }
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                scan(consumer, f, depth == SCAN_DEPTH_INF ? SCAN_DEPTH_INF : (depth + 1));
            } else {
                if (namePattern.matcher(f.getName()).matches()) {
                    SongEntry entry = new SongEntry();
                    entry.setFilePath(f.getAbsolutePath());
                    entry.setSongName(parseNameOfFile(f));
                    consumer.consume(entry);
                }
            }
        }
    }

    @Override
    public void scan(Consumer<SongEntry> consumer) {
        scan(consumer, file, scanDepth == SCAN_DEPTH_INF ? SCAN_DEPTH_INF : 0);
    }

    @Override
    public void initial(JSONObject data) throws JSONException {
        file = new File(GlobalVar.parseValue(data.getString(KEY_FILE)));
        namePattern = Pattern.compile(data.optString(KEY_NAME_PATTERN, DEFAULT_PATTERN));
        scanDepth = Integer.parseInt(data.optString(KEY_SCAN_DEPTH, "0"));
    }
}
