package com.edlplan.audiov.ui;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.edlplan.audiov.R;
import com.edlplan.audiov.scan.DirctCacheScanner;
import com.edlplan.audiov.scan.ISongListScanner;
import com.edlplan.audiov.scan.ScannerEntry;
import com.edlplan.audiov.scan.ScannerTypeMapper;
import com.edlplan.audiov.scan.SongList;
import com.edlplan.audiov.scan.SongListManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 编辑歌单，有2个模式，一个是新建歌单，一个是编辑现有歌单
 */
public class SongListEditDialog extends Dialog {

    private boolean isNewList;

    private SongList songList;


    public SongListEditDialog(@NonNull Context context, SongList list) {
        super(context, R.style.Theme_Design_BottomSheetDialog);
        setContentView(R.layout.song_list_edit_dialog);
        this.isNewList = (list == null);
        if (isNewList) {
            ScannerEntry entry = new ScannerEntry();
            entry.setInitialValue(new JSONObject());
            entry.setScannerklass(DirctCacheScanner.class);
            songList = new SongList("", entry);
        } else {
            songList = list;
        }

        ((EditText) findViewById(R.id.song_list_name)).setText(songList.getName());

        if (songList.isDirectList()) {
            ((RadioButton) findViewById(R.id.normal_song_list)).setChecked(true);
            findViewById(R.id.custom_values).setVisibility(View.GONE);
        } else {
            ((RadioButton) findViewById(R.id.custom_song_list)).setChecked(true);
            findViewById(R.id.custom_values).setVisibility(View.VISIBLE);
        }

        RadioGroup type = findViewById(R.id.song_list_type);
        type.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.normal_song_list:{
                    findViewById(R.id.custom_values).setVisibility(View.GONE);
                }break;
                case R.id.custom_song_list:{
                    findViewById(R.id.custom_values).setVisibility(View.VISIBLE);
                }break;
            }
        });

        ((AutoCompleteTextView) findViewById(R.id.song_list_type_class)).setText(songList.getScannerEntry().getScannerklass());
        try {
            ((EditText) findViewById(R.id.song_list_ini_value)).setText(songList.getScannerEntry().getInitialValue().toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
            ((EditText) findViewById(R.id.song_list_ini_value)).setText(e.toString());
        }

        findViewById(R.id.copy_to_board).setOnClickListener(v -> {
            checkData(((name, klass, inivalue) -> {
                JSONObject object = new JSONObject();
                try {
                    object.put("name", name);
                    object.put("klass", klass.getName());
                    object.put("inivalue", inivalue);
                    ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText(null, object.toString());
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(getContext(), "粘贴到剪贴板", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "粘贴到剪贴板失败", Toast.LENGTH_SHORT).show();
                }
            }));
        });

        findViewById(R.id.load_from_board).setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                String txt = clipData.getItemAt(0).getText().toString();
                try {
                    JSONObject object = new JSONObject(txt);
                    ((RadioButton) findViewById(R.id.custom_song_list)).setChecked(true);
                    findViewById(R.id.custom_values).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.song_list_name)).setText(object.getString("name"));
                    ((AutoCompleteTextView) findViewById(R.id.song_list_type_class)).setText(object.getString("klass"));
                    try {
                        ((EditText) findViewById(R.id.song_list_ini_value)).setText(object.getJSONObject("inivalue").toString(4));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((EditText) findViewById(R.id.song_list_ini_value)).setText(e.toString());
                    }
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "解析失败x", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "剪切板没有东西x", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_cancel).setOnClickListener(v -> dismiss());
        findViewById(R.id.button_save).setOnClickListener(v -> {
            checkData(((name, klass, inivalue) -> {
                if (isNewList) {
                    if (SongListManager.get().containsName(name)) {
                        Toast.makeText(getContext(), String.format("歌单\"%s\"已存在", name), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                songList.setName(name);
                songList.getScannerEntry().setScannerklass(klass);
                songList.getScannerEntry().setInitialValue(inivalue);
                if (isNewList) {
                    SongListManager.get().addSongList(songList);
                    Toast.makeText(getContext(), "添加成功", Toast.LENGTH_LONG).show();
                    dismiss();
                } else {
                    SongListManager.get().updateCache();
                    SongListManager.get().infoListChange();
                    dismiss();
                }
            }));
        });
    }

    private boolean checkData(OnGetData onGetData) {
        String name = ((EditText) findViewById(R.id.song_list_name)).getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "忘记填歌单名称了？", Toast.LENGTH_LONG).show();
            return false;
        }

        Class klass = null;
        try {
            klass = Class.forName(
                    ScannerTypeMapper.map(((AutoCompleteTextView) findViewById(R.id.song_list_type_class))
                            .getText().toString()));
            if (!ISongListScanner.class.isAssignableFrom(klass)) {
                Toast.makeText(getContext(), "找不到类型：" + ((AutoCompleteTextView) findViewById(R.id.song_list_type_class))
                        .getText().toString(), Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "错误的类型：" + e.toString(), Toast.LENGTH_LONG).show();
            return false;
        }

        JSONObject inivalue = null;
        try {
            inivalue = new JSONObject(((EditText) findViewById(R.id.song_list_ini_value)).getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "错误的初始化数据：" + e.toString(), Toast.LENGTH_LONG).show();
            return false;
        }
        onGetData.onGetData(name, klass, inivalue);
        return true;
    }

    private interface OnGetData {
        void onGetData(String name, Class klass, JSONObject inivalue);
    }

}
