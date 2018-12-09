package com.edlplan.audiov.scan;

import com.edlplan.audiov.GlobalVar;
import com.edlplan.audiov.core.utils.Consumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class JsonFileScanner implements ISongListScanner {

    public static final String KEY_FILE_PATH = "file";

    private String txtFilePath;

    private void initialFile(File file) throws JSONException, IOException {
        FileWriter writer = new FileWriter(file);
        writer.write(initialObject());
        writer.close();
    }

    private String initialObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("data", new JSONArray());
            return object.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setTxtFilePath(String txtFilePath) {
        this.txtFilePath = txtFilePath;
    }

    public static JSONObject asJSONObject(List<SongEntry> entryList) {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        for (SongEntry entry : entryList) {
            JSONObject e = new JSONObject();
            try {
                e.put("name", entry.getSongName());
                e.put("file", entry.getFilePath());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            array.put(e);
        }
        try {
            object.put("data", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public void scan(Consumer<SongEntry> consumer) throws Exception {
        File file = new File(txtFilePath);
        if (!file.exists()) {
            file.createNewFile();
            initialFile(file);
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String line = reader.readLine();
        if (line != null) {
            stringBuilder.append(line);
            while ((line = reader.readLine()) != null) {
                stringBuilder.append("\n").append(line);
            }
        } else {
            initialFile(file);
            stringBuilder.append(initialObject());
        }

        try {
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            JSONArray array = jsonObject.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                SongEntry entry = new SongEntry();
                JSONObject object = array.getJSONObject(i);
                entry.setSongName(object.getString("name"));
                entry.setFilePath(object.getString("file"));
                consumer.consume(entry);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initial(JSONObject data) throws Exception {
        txtFilePath = GlobalVar.parseValue(data.getString(KEY_FILE_PATH));
    }
}
