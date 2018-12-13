package com.edlplan.audiov.scan;

import java.io.Serializable;

public class SongEntry implements Serializable, Cloneable {

    private String filePath;

    private String songName;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongName() {
        return songName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SongEntry) {
            SongEntry o = (SongEntry) obj;
            return filePath.equals(o.filePath) && songName.equals(o.songName);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("{\n    path:%s\n    name:%s\n}", filePath, songName);
    }

    public SongEntry copy() {
        SongEntry entry = new SongEntry();
        entry.songName = songName;
        entry.filePath = filePath;
        return entry;
    }
}
