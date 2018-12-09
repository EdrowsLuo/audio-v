package com.edlplan.audiov;

import android.animation.TimeInterpolator;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.audio.IAudioEntry;
import com.edlplan.audiov.platform.android.AndroidPlugin;
import com.edlplan.audiov.platform.bass.BassPlugin;
import com.edlplan.audiov.scan.FolderScanner;
import com.edlplan.audiov.scan.ISongListScanner;
import com.edlplan.audiov.scan.ScannerEntry;
import com.edlplan.audiov.scan.SongEntry;
import com.edlplan.audiov.scan.SongListManager;
import com.edlplan.audiov.ui.SongListDialog;
import com.edlplan.audiov.ui.UserStateListenerOverlay;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class AudioVMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,UserStateListenerOverlay.OnUserStateChangeListener {

    private boolean isSongProgressBarTouched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalVar.registerValue(GlobalVar.INTERNAL_PATH, () -> {
            String p = getFilesDir().getAbsolutePath();
            if (p.endsWith("/")) {
                p = p.substring(0, p.length() - 1);
            }
            return p;
        });

        AndroidPlugin.initial(this);
        AudioVCore.initial(AndroidPlugin.INSTANCE, BassPlugin.INSTANCE);

        setContentView(R.layout.activity_audio_v_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SeekBar bar = findViewById(R.id.song_progress);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean preHandled = true;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    IAudioEntry entry = EdAudioService.getAudioService().getAudioEntry();
                    if (entry != null) {
                        if (preHandled) {
                            preHandled = false;
                            EdAudioService.getAudioService().post(
                                    audioService -> {
                                        if (audioService.getAudioEntry() == entry) {
                                            entry.seekTo(progress * entry.length() / bar.getMax());
                                        }
                                        preHandled = true;
                                    }
                            );
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSongProgressBarTouched = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSongProgressBarTouched = false;
            }
        });


        ImageButton playStopButton = findViewById(R.id.button_play_stop);
        playStopButton.setOnClickListener(v -> {
            IAudioEntry entry = EdAudioService.getAudioService().getAudioEntry();
            if (entry != null) {
                if (entry.isPlaying()) {
                    EdAudioService.getAudioService().pause();
                    //Toast.makeText(AudioVMainActivity.this, "pause", Toast.LENGTH_SHORT).show();
                } else {
                    EdAudioService.getAudioService().play();
                    //Toast.makeText(AudioVMainActivity.this, "play", Toast.LENGTH_SHORT).show();
                }
            }
        });


        EdAudioService.getAudioService().registerOnAudioProgressListener((audioEntry, ms) -> {
            runOnUiThread(()->{
                if (!isSongProgressBarTouched) {
                    bar.setProgress((int) (bar.getMax() * ms / audioEntry.length()));
                }
            });
        });

        findViewById(R.id.button_next_song).setOnClickListener(v -> EdAudioService.nextSong());
        findViewById(R.id.button_pre_song).setOnClickListener(v -> EdAudioService.previousSong());
        findViewById(R.id.open_song_list).setOnClickListener(v -> new SongListDialog(this).show());

        EdAudioService.getAudioService().registerOnAudioCompleteListener(audioEntry -> {
            EdAudioService.nextSong();
        });

        EdAudioService.getAudioService().registerOnAudioChangeListener((pre, next) -> {
            runOnUiThread(() -> {
                ((TextView)findViewById(R.id.song_name)).setText(
                        EdAudioService.getSongList().size()>0?
                                EdAudioService.getSongList().get(EdAudioService.getPlayingIdx()).getSongName():
                                "404"
                );
            });
        });


        try {
            //SongListManager.get().getSongList(0).scan();
            //SongListManager.get().getSongList(0).updateCache();
            EdAudioService.setSongList(SongListManager.get().getSongList(0).getCachedResult());
            EdAudioService.playAtPosition((new Random()).nextInt(EdAudioService.getSongList().size()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((UserStateListenerOverlay) findViewById(R.id.user_state_listener_overlay)).setOnUserStateChangeListener(this);
    }

    public void hideUi() {
        findViewById(R.id.app_bar_layout).animate().cancel();
        findViewById(R.id.app_bar_layout)
                .animate()
                .setDuration(1000)
                .alpha(0)
                .withEndAction(() -> findViewById(R.id.app_bar_layout).setVisibility(View.GONE))
                .start();

        findViewById(R.id.button_field).animate().cancel();
        findViewById(R.id.button_field)
                .animate()
                .setDuration(1000)
                .alpha(0)
                .withEndAction(() -> findViewById(R.id.button_field).setVisibility(View.GONE))
                .start();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void showUi() {
        findViewById(R.id.app_bar_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.app_bar_layout).animate().cancel();
        findViewById(R.id.app_bar_layout)
                .animate()
                .setDuration(300)
                .alpha(1)
                .start();

        findViewById(R.id.button_field).setVisibility(View.VISIBLE);
        findViewById(R.id.button_field).animate().cancel();
        findViewById(R.id.button_field)
                .animate()
                .setDuration(300)
                .alpha(1)
                .start();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.audio_vmain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onUserLeave(View view) {
        hideUi();
    }

    @Override
    public void onUserArrival(View view) {
        showUi();
    }
}
