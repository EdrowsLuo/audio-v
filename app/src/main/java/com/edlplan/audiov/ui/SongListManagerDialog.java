package com.edlplan.audiov.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.edlplan.audiov.EdAudioService;
import com.edlplan.audiov.R;
import com.edlplan.audiov.core.utils.Consumer;
import com.edlplan.audiov.scan.SongList;
import com.edlplan.audiov.scan.SongListManager;

public class SongListManagerDialog extends Dialog {

    RecyclerView recyclerView;

    Consumer<SongListManager> onListChange;

    OnClickOverride onClickOverride;

    SMAdapter adapter;

    public SongListManagerDialog(@NonNull Context context) {
        super(context, R.style.Theme_Design_BottomSheetDialog);
        setContentView(R.layout.song_list_manager_dialog);

        recyclerView = findViewById(R.id.song_list_manager_body);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);

        recyclerView.setLayoutManager(layoutManager);

        layoutManager.setOrientation(OrientationHelper.VERTICAL);

        adapter = new SMAdapter();

        recyclerView.setAdapter(adapter);

        onListChange = songListManager -> adapter.notifyDataSetChanged();

        SongListManager.get().registerOnChangeListener(onListChange);

        findViewById(R.id.new_song_list).setOnClickListener(v -> {
            SongListEditDialog dialog = new SongListEditDialog(getContext(), null);
            dialog.show();
        });

    }

    public void setOnClickOverride(OnClickOverride onClickOverride) {
        this.onClickOverride = onClickOverride;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        SongListManager.get().unregisterOnChangeListener(onListChange);
    }

    public static class EntryHolder extends RecyclerView.ViewHolder{

        private View body;

        private TextView title;

        private TextView description;

        private ImageButton opbutton;

        public EntryHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_textview);
            description = itemView.findViewById(R.id.description_textview);
            body = itemView.findViewById(R.id.body);
            opbutton = itemView.findViewById(R.id.operate_button);
        }

    }


    public class SMAdapter extends RecyclerView.Adapter<EntryHolder> {

        @NonNull
        @Override
        public EntryHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new EntryHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_list_entry, viewGroup, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull EntryHolder entryHolder, int i) {
            SongList list = SongListManager.get().getSongList(i);

            entryHolder.title.setText(list.getName() + " (" + list.getCachedResult().size() + ")");
            String dsc = list.getDescription();
            if (dsc == null) {
                entryHolder.description.setVisibility(View.GONE);
            } else {
                entryHolder.description.setVisibility(View.VISIBLE);
                entryHolder.description.setText(dsc);
            }

            if (list.isEnable()) {
                entryHolder.body.setBackgroundColor(0xFF333333);
                entryHolder.opbutton.setBackgroundTintList(ColorStateList.valueOf(0xFF333333));
            } else {
                entryHolder.body.setBackgroundColor(0xFFDD1212);
                entryHolder.opbutton.setBackgroundTintList(ColorStateList.valueOf(0xFFDD1212));
                entryHolder.description.setVisibility(View.VISIBLE);
                entryHolder.description.setText((dsc == null ? "" : (dsc + "\n") + "Error: " + list.getErrorMessage()));
            }

            //if (list.isEnable()) {
                if (onClickOverride == null) {
                    entryHolder.body.setOnClickListener(v -> {
                        OperationDialog dialog = new OperationDialog(getContext());
                        dialog.setTitle(list.getName());
                        OperationDialog.OperationBuilder operationBuilder = dialog.operationBuilder();

                        operationBuilder.addOperation("播放当前歌单", () -> {
                            EdAudioService.setSongList(list.getCachedResult());
                            dialog.dismiss();
                        });

                        if (!list.isDirectList()) {
                            operationBuilder.addOperation("刷新数据", () -> {
                                LoadingDialog loadingDialog = new LoadingDialog(getContext());
                                (new Thread(() -> {
                                    try {
                                        list.scan();
                                        list.updateCache();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        list.setEnable(false);
                                        list.setErrorMessage(e.getMessage());
                                        recyclerView.post(SongListManager.get()::infoListChange);
                                    }
                                    recyclerView.post(() -> {
                                        loadingDialog.dismiss();
                                        Toast.makeText(getContext(), "刷新成功", Toast.LENGTH_SHORT).show();
                                    });
                                })).start();
                                loadingDialog.show();
                            });
                        }

                        if (i != 0) {
                            operationBuilder.addOperation("歌单上移", () -> {
                                SongList pre = SongListManager.get().getSongList(i - 1);
                                SongListManager.get().getSongLists().set(i, pre);
                                SongListManager.get().getSongLists().set(i - 1, list);
                                SongListManager.get().updateCache();
                                SongListManager.get().infoListChange();
                                dialog.dismiss();
                            });
                        }

                        if (i != SongListManager.get().size() - 1) {
                            operationBuilder.addOperation("歌单下移", () -> {
                                SongList pre = SongListManager.get().getSongList(i + 1);
                                SongListManager.get().getSongLists().set(i, pre);
                                SongListManager.get().getSongLists().set(i + 1, list);
                                SongListManager.get().updateCache();
                                SongListManager.get().infoListChange();
                                dialog.dismiss();
                            });
                        }

                        operationBuilder.addOperation("编辑原始数据", () -> {
                            SongListEditDialog editDialog = new SongListEditDialog(getContext(), list);
                            editDialog.show();
                        });

                        if (!list.isPinned()) {
                            operationBuilder.addOperation("删除", () -> {
                                OperationDialog sure = new OperationDialog(getContext());
                                sure.setTitle("删除歌单");
                                sure.operationBuilder()
                                        .addOperation("确认？", ()->{
                                            SongListManager.get().deleteSongList(list);
                                            recyclerView.post(SongListManager.get()::infoListChange);
                                            sure.dismiss();
                                            dialog.dismiss();
                                            dismiss();
                                        })
                                        .build();
                                sure.show();
                            });
                        }


                        operationBuilder.build();

                        dialog.show();
                    });
                } else {
                    entryHolder.body.setOnClickListener(v -> onClickOverride.onClick(entryHolder, list));
                }

            //}
        }

        @Override
        public int getItemCount() {
            return SongListManager.get().size();
        }
    }

    public interface OnClickOverride {
        void onClick(EntryHolder holder, SongList list);
    }

}

