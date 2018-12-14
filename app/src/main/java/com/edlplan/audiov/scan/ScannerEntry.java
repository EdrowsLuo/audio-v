package com.edlplan.audiov.scan;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ScannerEntry implements Externalizable {

    private String scannerklass;

    private JSONObject initialValue;

    public JSONObject getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(JSONObject initialValue) {
        this.initialValue = initialValue;
    }

    public String getScannerklass() {
        return scannerklass;
    }

    public void setScannerklass(Class<? extends ISongListScanner> scannerklass) {
        this.scannerklass = scannerklass.getCanonicalName();
    }

    public ISongListScanner createScanner() throws Exception {
        ISongListScanner songListScanner = (ISongListScanner) Class.forName(scannerklass).newInstance();
        songListScanner.initial(initialValue);
        return songListScanner;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(scannerklass);
        out.writeUTF(initialValue.toString());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        scannerklass = in.readUTF();
        try {
            initialValue = new JSONObject(in.readUTF());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
