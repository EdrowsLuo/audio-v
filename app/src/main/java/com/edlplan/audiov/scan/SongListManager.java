package com.edlplan.audiov.scan;

import com.edlplan.audiov.GlobalVar;
import com.edlplan.audiov.core.utils.Consumer;
import com.edlplan.audiov.core.utils.ListenerGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SongListManager {

    private static int VERSION = 101;

    private static SongListManager manager;

    private ListenerGroup<Consumer<SongListManager>> onSongListSateChangeListener =
            ListenerGroup.create(songListManagerConsumer -> songListManagerConsumer.consume(this));

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

    public ArrayList<SongList> getSongLists() {
        return songLists;
    }

    public int size() {
        return songLists.size();
    }

    public SongList getSongList(int i) {
        return songLists.get(i);
    }

    public boolean containsName(String name) {
        for (SongList list : songLists) {
            if (list.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void initialCacheFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        File ccd = new File(GlobalVar.parseValue("#internal_path#/songListCache/"));
        if (!ccd.exists()) {
            ccd.mkdirs();
        }
        for (File f : ccd.listFiles()) {
            if (f.isFile()) {
                f.delete();
            }
        }
        ArrayList<SongList> tmp = new ArrayList<>();
        {
            ScannerEntry entry = new ScannerEntry();
            JSONObject cfg = new JSONObject();
            try {
                cfg.put(FolderScanner.KEY_SCAN_DEPTH, 0 + "");
                cfg.put(FolderScanner.KEY_NAME_PATTERN, FolderScanner.DEFAULT_PATTERN);
                cfg.put(FolderScanner.KEY_FILE, "#external_path#/netease/cloudmusic/Music");
                cfg.put(SongList.EXT_DESCRIPTION, "扫描网易云目录的歌曲");
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
        }
        {
            ScannerEntry entry = new ScannerEntry();
            JSONObject cfg = new JSONObject();
            try {
                cfg.put(SongList.EXT_DESCRIPTION, "你默认的歌单");
                cfg.put(SongList.EXT_PINNED, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            entry.setInitialValue(cfg);
            entry.setScannerklass(DirctCacheScanner.class);

            SongList list = new SongList("默认收藏夹", entry);
            try {
                list.updateCache();
            } catch (Exception e) {
                e.printStackTrace();
            }

            tmp.add(list);
        }

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
                list.setEnable(true);
                if (list.isAlwaysUpdateWhenLoad() && !list.isDirectList()) {
                    try {
                        list.scan();
                        list.updateCache();
                    } catch (Exception e) {
                        e.printStackTrace();
                        list.setEnable(false);
                        list.setErrorMessage(e.toString());
                    }
                } else {
                    list.loadFromCache();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            initialCacheFile(file);
            loadFromCache();
            return;
        }
        infoListChange();
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
            onSongListSateChangeListener.handle();
        }
    }

    public void addSongList(SongList list) {
        if (!songLists.contains(list)) {
            songLists.add(list);
            updateCache();
            onSongListSateChangeListener.handle();
        }
    }

    public void registerOnChangeListener(Consumer<SongListManager> consumer) {
        onSongListSateChangeListener.register(consumer);
    }

    public void unregisterOnChangeListener(Consumer<SongListManager> consumer) {
        onSongListSateChangeListener.unregiser(consumer);
    }

    public void infoListChange() {
        onSongListSateChangeListener.handle();
    }
}
