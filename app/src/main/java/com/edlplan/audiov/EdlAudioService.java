package com.edlplan.audiov;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.audio.AudioService;
import com.edlplan.audiov.scan.SongEntry;
import com.edlplan.audiov.scan.SongList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EdlAudioService {

    private static AudioService audioService;

    private static SongList res;

    private static List<SongEntry> songList = new ArrayList<>();

    private static SongEntry playingEntry;

    private static int prePlayingIdx = 0;
    public static final OnListInitialBehavior PLAY_FROM_START = () -> playAtPosition(0);
    public static final OnListInitialBehavior PLAY_RANDOM = () -> {
        if (songList.size() == 0) {
            playAtPosition(0);
        } else {
            playAtPosition((new Random()).nextInt(songList.size()));
        }
    };
    private static OnListInitialBehavior onListInitialBehavior;

    public static void setOnListInitialBehavior(OnListInitialBehavior onListInitialBehavior) {
        EdlAudioService.onListInitialBehavior = onListInitialBehavior;
    }

    private static void onListInitial() {
        if (onListInitialBehavior != null) {
            onListInitialBehavior.onListInitial();
        } else {
            playAtPosition(0);
        }
    }

    public static AudioService getAudioService() {
        if (audioService == null) {
            audioService = new AudioService();
            audioService.play();
        }
        return audioService;
    }

    public static SongEntry getPlayingEntry() {
        return playingEntry;
    }

    public static int getPlayingIdx() {
        return prePlayingIdx;
    }

    public static SongList getRes() {
        return res;
    }

    public static void setSongList(SongList list) {
        res = list;
        if (list.getCachedResult().size() == 0) {
            songList.clear();
            getAudioService().pause();
            prePlayingIdx = -1;
            return;
        }
        songList.clear();
        songList.addAll(list.getCachedResult());
        onListInitial();
    }

    public static List<SongEntry> getSongList() {
        return songList;
    }

    public static void setSongList(List<SongEntry> newSongList) {
        res = null;
        if (newSongList.size() == 0) {
            songList.clear();
            getAudioService().pause();
            return;
        }
        songList.clear();
        songList.addAll(newSongList);
        onListInitial();
    }

    public static void playAtPosition(int pos) {
        if (songList.size() == 0) {
            playingEntry = null;
            getAudioService().pause();
            return;
        }
        prePlayingIdx = floorMod(pos, songList.size());
        playingEntry = songList.get(prePlayingIdx);
        if (!new File(playingEntry.getFilePath()).exists()) {
            playNextAvailable(pos, pos + 1);
            return;
        }
        getAudioService().changeAudio(AudioVCore.createAudio(playingEntry.getFilePath()), true);
    }

    private static void playNextAvailable(int start, int pos) {
        if (songList.size() == 0) {
            return;
        }
        pos = floorMod(pos, songList.size());
        if (start == pos) {
            return;
        } else {
            if (!new File(songList.get(pos).getFilePath()).exists()) {
                playNextAvailable(start, pos + 1);
            } else {
                playAtPosition(pos);
            }
        }
    }

    public static void nextSong() {
        if (getAudioService().getAudioEntry() == null) {
            onListInitial();
        } else {
            playAtPosition(prePlayingIdx + 1);
        }
    }

    public static void previousSong() {
        if (getAudioService().getAudioEntry() == null) {
            onListInitial();
        } else {
            playAtPosition(prePlayingIdx - 1);
        }
    }

    /**
     * 当SongList发生了改变的时候更新数据，当且尽当res不为null时有效
     */
    public static void notifySongListChange() {
        if (res != null) {
            songList.clear();
            songList.addAll(res.getCachedResult());
            if (playingEntry != null) {
                int idx = songList.indexOf(playingEntry);
                if (idx == -1) {
                    onListInitial();
                    audioService.pause();
                } else {
                    prePlayingIdx = idx;
                }
            }
        }
    }

    private static int floorMod(int x, int y) {
        int r = x - floorDiv(x, y) * y;
        return r;
    }

    private static int floorDiv(int x, int y) {
        int r = x / y;
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    public interface OnListInitialBehavior {
        void onListInitial();
    }
}
