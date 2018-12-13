package com.edlplan.audiov.scan;

import com.edlplan.audiov.GlobalVar;
import com.edlplan.audiov.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SongList implements Serializable {

    public static final String EXT_DESCRIPTION = "description";

    public static final String EXT_PINNED = "pinned";

    public static final String EXT_FULLY_EDITABLE = "fullyEditable";

    private ScannerEntry scannerEntry;

    private transient ArrayList<SongEntry> cachedResult;

    private transient boolean enable = true;

    private transient String errorMessage = null;

    private String name;

    private boolean alwaysUpdateWhenLoad = false;

    public SongList(String name, ScannerEntry scannerEntry) {
        this.scannerEntry = scannerEntry;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getName() {
        return name;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setAlwaysUpdateWhenLoad(boolean alwaysUpdateWhenLoad) {
        this.alwaysUpdateWhenLoad = alwaysUpdateWhenLoad;
    }

    public boolean isAlwaysUpdateWhenLoad() {
        return alwaysUpdateWhenLoad;
    }

    public List<SongEntry> getCachedResult() {
        if (cachedResult == null) {
            try {
                loadFromCache();
            } catch (Exception e) {
                e.printStackTrace();
                cachedResult = new ArrayList<>();
            }
        }
        return cachedResult;
    }

    private File getCacheFile() {
        File file = new File(GlobalVar.parseValue("#internal_path#/songListCache/" + name + ".javaobj"));
        Utils.checkFile(file);
        return file;
    }

    public void loadFromCache() throws Exception {
        File file = getCacheFile();
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            Object o = inputStream.readObject();
            if (o != null) {
                cachedResult = (ArrayList<SongEntry>) o;
            } else {
                cachedResult = new ArrayList<>();
            }
        } catch (Exception e) {
            cachedResult = new ArrayList<>();
        }
    }

    public void updateCache() throws Exception{
        File file = getCacheFile();
        ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(file));
        writer.writeObject(cachedResult);
        writer.close();
    }

    public ScannerEntry getScannerEntry() {
        return scannerEntry;
    }

    public void scan() throws Exception {
        try {
            enable = true;
            if (isDirectList()) {
                loadFromCache();
            } else {
                cachedResult = new ArrayList<>(scannerEntry.createScanner().scanAsList());
            }
        } catch (Exception e) {
            enable = false;
            throw e;
        }

    }

    public boolean isDirectList() {
        return DirctCacheScanner.class.getCanonicalName().equals(scannerEntry.getScannerklass());
    }

    private void checkDirectList() {
        if (!isDirectList()) {
            throw new IllegalStateException("错误的操作一个非Direct的SongList");
        }
    }

    public boolean containsSong(SongEntry entry) {
        for (SongEntry entryx : cachedResult) {
            if (entryx.equals(entry)) {
                return true;
            }
        }
        return false;
    }

    public void deleteSong(SongEntry entry) {
        checkDirectList();
        cachedResult.remove(entry);
        try {
            updateCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addSong(SongEntry entry) {
        checkDirectList();
        SongEntry copy = new SongEntry();
        copy.setFilePath(entry.getFilePath());
        copy.setSongName(entry.getSongName());
        cachedResult.add(copy);
        try {
            updateCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeSong(SongEntry entry) {
        checkDirectList();
        Iterator<SongEntry> iterator = cachedResult.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(entry)) {
                iterator.remove();
            }
        }
        try {
            updateCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCache() {
        getCacheFile().delete();
    }

    public String getDescription() {
        return scannerEntry.getInitialValue().optString(EXT_DESCRIPTION, null);
    }

    public boolean isPinned() {
        return scannerEntry.getInitialValue().optBoolean(EXT_PINNED, false);
    }

}
