package com.edlplan.audiov.scan;

import android.support.annotation.Keep;

import com.edlplan.audiov.GlobalVar;
import com.edlplan.audiov.core.utils.Consumer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

@Keep
public class DroidBeatmapScanner implements ISongListScanner {

    private String songsPath;

    private SongEntry parse(File file) throws IOException {
        String filename = null;
        String title = null;
        String artist = null;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            int idx = line.indexOf(':');
            if (line.startsWith("[D")) break;
            if (idx != -1) {
                String key = line.substring(0, idx).trim();
                switch (key) {
                    case "AudioFilename": {
                        filename = line.substring(idx + 1, line.length()).trim();
                    }
                    break;
                    case "Title": {
                        title = line.substring(idx + 1, line.length()).trim();
                    }
                    break;
                    case "Artist": {
                        artist = line.substring(idx + 1, line.length()).trim();
                    }
                    break;
                    default:
                        continue;
                }
            }
        }
        reader.close();

        if (filename == null || title == null || artist == null) {
            return null;
        }

        SongEntry entry = new SongEntry();
        entry.setFilePath(new File(file.getParentFile(), filename).getAbsolutePath());
        entry.setSongName(String.format("%s - %s", artist, title));
        return entry;
    }

    @Override
    public void scan(Consumer<SongEntry> consumer) throws Exception {
        HashSet<String> entryMap = new HashSet<>();
        for (File osd : new File(songsPath).listFiles()) {
            if (osd.isDirectory()) {
                for (File osu : osd.listFiles()) {
                    if (osu.isFile() && osu.getName().endsWith(".osu")) {
                        SongEntry entry = parse(osu);
                        if (entry != null) {
                            if (!entryMap.contains(entry.getFilePath())) {
                                entryMap.add(entry.getFilePath());
                                consumer.consume(entry);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void initial(JSONObject data) throws Exception {
        songsPath = GlobalVar.parseValue(data.optString("songsPath", "#external_path#/osu!droid/Songs"));
    }
}
