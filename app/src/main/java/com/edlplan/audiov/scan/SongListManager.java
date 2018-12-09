package com.edlplan.audiov.scan;

import com.edlplan.audiov.GlobalVar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class SongListManager {

    private static int VERSION = 100;

    private static SongListManager manager;

    public static SongListManager get() {
        if (manager == null) {
            manager = new SongListManager();
            manager.loadFromCache();
        }
        return manager;
    }

    private ArrayList<SongList> songLists;

    public File getListManagerCacheFile() {
        File file = new File(GlobalVar.parseValue("#internal_path#/listManager.javaobj"));
        if (!file.exists()) {
            initialCacheFile(file);
        }
        return file;
    }

    public int size() {
        return songLists.size();
    }

    public SongList getSongList(int i) {
        return songLists.get(i);
    }

    private void initialCacheFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ArrayList<SongList> tmp = new ArrayList<>();

        ScannerEntry entry = new ScannerEntry();
        JSONObject cfg = new JSONObject();
        try {
            cfg.put(FolderScanner.KEY_SCAN_DEPTH, 0 + "");
            cfg.put(FolderScanner.KEY_NAME_PATTERN, FolderScanner.DEFAULT_PATTERN);
            cfg.put(FolderScanner.KEY_FILE, "#external_path#/netease/cloudmusic/Music");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        entry.setInitialValue(cfg);
        entry.setScannerklass(FolderScanner.class);

        SongList wyyList = new SongList("网易云", entry);
        wyyList.setAlwaysUpdateWhenLoad(true);
        try {
            wyyList.scan();
            wyyList.updateCache();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tmp.add(wyyList);

        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeInt(VERSION);
            objectOutputStream.writeObject(tmp);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromCache() {
        File file = getListManagerCacheFile();
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            if (inputStream.readInt() != VERSION) {
                initialCacheFile(file);
                loadFromCache();
                return;
            }
            songLists = (ArrayList<SongList>) inputStream.readObject();
            for (SongList list : songLists) {
                if (list.isAlwaysUpdateWhenLoad()) {
                    list.scan();
                    list.updateCache();
                } else {
                    list.loadFromCache();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            initialCacheFile(file);
            loadFromCache();
        }
    }

    public void updateCache() {
        File file = getListManagerCacheFile();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeInt(VERSION);
            outputStream.writeObject(songLists);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteSongList(SongList list) {
        int idx = songLists.indexOf(list);
        if (idx != -1) {
            songLists.remove(list);
            updateCache();
        }
    }

    public void addSongList(SongList list) {
        if (!songLists.contains(list)) {
            songLists.add(list);
            updateCache();
        }
    }

}
