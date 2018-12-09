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

    private ScannerEntry scannerEntry;

    private transient ArrayList<SongEntry> cachedResult;

    private String name;

    private boolean alwaysUpdateWhenLoad = false;

    public SongList(String name, ScannerEntry scannerEntry) {
        this.scannerEntry = scannerEntry;
        this.name = name;
    }

    public void setAlwaysUpdateWhenLoad(boolean alwaysUpdateWhenLoad) {
        this.alwaysUpdateWhenLoad = alwaysUpdateWhenLoad;
    }

    public boolean isAlwaysUpdateWhenLoad() {
        return alwaysUpdateWhenLoad;
    }

    public List<SongEntry> getCachedResult() {
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

    public void scan() throws Exception {
        if (isDirectList()) {
            loadFromCache();
        } else {
            cachedResult = new ArrayList<>(scannerEntry.createScanner().scanAsList());
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
}
