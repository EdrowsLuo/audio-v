package com.edlplan.audiov.ui;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class UserStateListenerOverlay extends View {

    /**
     * 大概多久检查一次
     */
    private int checkDeltaTimeMS = 2000;

    /**
     * 用户大概多久不操作就判定为没有进行操作
     */
    private int noActionTime = 10000;

    private long latestOperateTime = -1;

    private boolean active = false;

    private boolean userOperating = false;

    private OnUserStateChangeListener onUserStateChangeListener;

    public UserStateListenerOverlay(Context context) {
        super(context);
    }

    public UserStateListenerOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UserStateListenerOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UserStateListenerOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnUserStateChangeListener(OnUserStateChangeListener onUserStateChangeListener) {
        this.onUserStateChangeListener = onUserStateChangeListener;
        if (!active) {
            active = true;
            latestOperateTime = SystemClock.uptimeMillis();
            userOperating = true;
            checkUserLeave();
        }
    }

    public void checkUserLeave() {
        postDelayed(() -> {
            if (userOperating && SystemClock.uptimeMillis() - latestOperateTime > noActionTime) {
                userOperating = false;
                if (onUserStateChangeListener != null) {
                    onUserStateChangeListener.onUserLeave(this);
                }
            }
            postDelayed(this::checkUserLeave, checkDeltaTimeMS);
        }, checkDeltaTimeMS);
    }

    public void userOperate() {
        latestOperateTime = SystemClock.uptimeMillis();
        if (!userOperating) {
            userOperating = true;
            if (onUserStateChangeListener != null) {
                onUserStateChangeListener.onUserArrival(this);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        userOperate();
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        userOperate();
        return super.dispatchKeyEvent(event);
    }

    public interface OnUserStateChangeListener {

        void onUserLeave(View view);

        void onUserArrival(View view);

    }
}
