package com.edlplan.audiov;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.audio.AudioService;
import com.edlplan.audiov.scan.SongEntry;

import java.util.ArrayList;
import java.util.List;

public class EdAudioService {

    private static AudioService audioService;

    private static List<SongEntry> songList = new ArrayList<>();

    private static int prePlayingIdx = 0;

    public static AudioService getAudioService() {
        if (audioService == null) {
            audioService = new AudioService();
            audioService.play();
        }
        return audioService;
    }

    public static int getPlayingIdx() {
        return prePlayingIdx;
    }

    public static void setSongList(List<SongEntry> newSongList) {
        if (newSongList.size() == 0) {
            return;
        }
        songList.clear();
        songList.addAll(newSongList);
        getAudioService().changeAudio(AudioVCore.createAudio(songList.get(0).getFilePath()), true);
    }

    public static List<SongEntry> getSongList() {
        return songList;
    }

    public static void playAtPosition(int pos) {
        if (songList.size() == 0) {
            return;
        }
        prePlayingIdx = floorMod(pos, songList.size());
        getAudioService().changeAudio(AudioVCore.createAudio(songList.get(prePlayingIdx).getFilePath()), true);
    }

    public static void nextSong() {
        if (getAudioService().getAudioEntry() == null) {
            playAtPosition(0);
        } else {
            playAtPosition(prePlayingIdx + 1);
        }
    }

    public static void previousSong() {
        if (getAudioService().getAudioEntry() == null) {
            playAtPosition(0);
        } else {
            playAtPosition(prePlayingIdx - 1);
        }
    }


    private static int floorMod(int x, int y) {
        int r = x - floorDiv(x, y) * y;
        return r;
    }

    private static int floorDiv(int x, int y) {
        int r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }
}
