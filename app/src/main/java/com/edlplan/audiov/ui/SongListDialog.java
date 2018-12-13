package com.edlplan.audiov.ui;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.edlplan.audiov.EdAudioService;
import com.edlplan.audiov.R;
import com.edlplan.audiov.core.audio.IAudioEntry;
import com.edlplan.audiov.core.audio.OnAudioChangeListener;
import com.edlplan.audiov.scan.SongEntry;
import com.edlplan.audiov.scan.SongListManager;

public class SongListDialog extends Dialog implements OnAudioChangeListener{

    SongListAdapter adapter;

    OnAudioChangeListener onAudioChangeListener;

    RecyclerView recyclerView;

    public SongListDialog(@NonNull Context context) {
        super(context, R.style.Theme_MaterialComponents_BottomSheetDialog);
        setContentView(R.layout.song_list_dialog);

        findViewById(R.id.button).setOnClickListener(v -> {
            SongListManagerDialog dialog = new SongListManagerDialog(getContext());
            dialog.setOnClickOverride((holder, list) -> {
                if (list.getCachedResult().size() == 0) {
                    Toast.makeText(getContext(), "这个歌单好像是空的哎", Toast.LENGTH_SHORT).show();
                    return;
                }
                EdAudioService.setSongList(list.getCachedResult());
                dialog.dismiss();
            });
            dialog.show();
        });

        recyclerView = findViewById(R.id.song_list_body);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);

        recyclerView.setLayoutManager(layoutManager);

        layoutManager.setOrientation(OrientationHelper.VERTICAL);

        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setAdapter(adapter = new SongListAdapter());

        recyclerView.scrollToPosition(EdAudioService.getPlayingIdx());

        EdAudioService.getAudioService().registerOnAudioChangeListener(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        EdAudioService.getAudioService().unregisterOnAudioChangeListener(this);
    }

    @Override
    public void onAudioChange(IAudioEntry pre, IAudioEntry next) {
        recyclerView.post(adapter::notifyDataSetChanged);
    }

    private static class SongListEntryHolder extends RecyclerView.ViewHolder {

        private TextView songName;

        private View moreDetails;

        private View body;

        public SongListEntryHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.entry_name);
            moreDetails = itemView.findViewById(R.id.more_details);
            body = itemView.findViewById(R.id.list_entry_body);
        }
    }

    private class SongListAdapter extends RecyclerView.Adapter<SongListEntryHolder> {

        @NonNull
        @Override
        public SongListEntryHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new SongListEntryHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.entry_list_entry, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SongListEntryHolder songListEntryHolder, int i) {
            SongEntry entry = EdAudioService.getSongList().get(i);
            songListEntryHolder.songName.setText(entry.getSongName());
            if (i == EdAudioService.getPlayingIdx()) {
                songListEntryHolder.body.setBackgroundColor(0xFF999999);
            } else {
                songListEntryHolder.body.setBackgroundColor(0xFF333333);
            }
            songListEntryHolder.body.setOnClickListener(v -> EdAudioService.playAtPosition(i));
            songListEntryHolder.body.setOnLongClickListener(v -> {
                OperationDialog dialog = new OperationDialog(v.getContext());
                OnAudioChangeListener listener = (pre, next) -> dialog.dismiss();
                EdAudioService.getAudioService().registerOnAudioChangeListener(listener);

                dialog.setTitle(entry.getSongName());
                dialog.setOnDismissListener(
                        dialog1 -> EdAudioService.getAudioService().post(
                                audioService -> audioService.unregisterOnAudioChangeListener(listener)));

                dialog.operationBuilder()
                        .addOperation("播放", () -> EdAudioService.playAtPosition(i))
                        .addOperation("收藏至其他歌单",()->{
                            SongListManagerDialog managerDialog = new SongListManagerDialog(getContext());
                            managerDialog.setOnClickOverride((holder, list) -> {
                                if (list.isDirectList()) {
                                    OperationDialog operationDialog = new OperationDialog(getContext());
                                    operationDialog.setTitle(list.getName());
                                    OperationDialog.OperationBuilder operationBuilder = operationDialog.operationBuilder();
                                    if (list.containsSong(entry)) {
                                        operationBuilder
                                                .addOperation("从这个歌单里移除", () -> {
                                                    list.deleteSong(entry.copy());
                                                    operationDialog.dismiss();
                                                    SongListManager.get().infoListChange();
                                                    Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        operationBuilder
                                                .addOperation("添加到这个歌单里", () -> {
                                                    list.addSong(entry.copy());
                                                    operationDialog.dismiss();
                                                    SongListManager.get().infoListChange();
                                                    Toast.makeText(getContext(), "添加入歌单 " + list.getName(), Toast.LENGTH_SHORT).show();
                                                });
                                    }

                                    operationBuilder.build();

                                    operationDialog.show();
                                } else {
                                    Toast.makeText(getContext(), "扫描模式的歌单无法手动添加歌曲 " + list.getName(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            managerDialog.show();
                        })
                        .build();
                dialog.show();

                return true;
            });
        }

        @Override
        public int getItemCount() {
            return EdAudioService.getSongList().size();
        }
    }

}
