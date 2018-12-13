package com.edlplan.audiov.ui;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.edlplan.audiov.R;

public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.Theme_Design_BottomSheetDialog);
        setContentView(R.layout.loading_dialog);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        return false;
    }
}
