package com.github.fakegps;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.github.fakegps.model.LocPoint;
import com.github.fakegps.ui.BookmarkActivity;
import com.github.fakegps.ui.FlyToActivity;
import com.github.fakegps.ui.JoyStickView;
import com.github.fakegps.ui.MainActivity;

import tiger.radio.loggerlibrary.Logger;

/**
 * Created by tiger on 7/22/16.
 */
public class JoyStickManager implements IJoyStickPresenter {

    private static final String TAG = "JoyStickManager";

    public static double STEP_DEFAULT = 0.00002;

    private static JoyStickManager INSTANCE = new JoyStickManager();

    private Context mContext;
    private LocationThread mLocationThread;
    private boolean mIsStarted = false;
    private double mMoveStep = STEP_DEFAULT;

    private LocPoint mCurrentLocPoint;

    private LocPoint mTargetLocPoint;
    private int mFlyTime;
    private int mFlyTimeIndex;
    private double deltaLatitude;
    private double deltaLongitude;
    private boolean mIsFlyMode = false;

    private JoyStickView mJoyStickView;

    private JoyStickManager() {
    }


    public void init(Context context) {
        mContext = context;
    }

    public static JoyStickManager get() {
        return INSTANCE;
    }

    public void start(@NonNull LocPoint locPoint) {
        mCurrentLocPoint = locPoint;
        if (mLocationThread == null || !mLocationThread.isAlive()) {
            mLocationThread = new LocationThread(mContext.getApplicationContext(), this);
            mLocationThread.startThread();
        }
        showJoyStick();
        mIsStarted = true;
    }

    public void stop() {
        if (mLocationThread != null) {
            mLocationThread.stopThread();
            mLocationThread = null;
        }

        hideJoyStick();
        mIsStarted = false;
    }

    public boolean isStarted() {
        return mIsStarted;
    }

    public void showJoyStick() {
        if (mJoyStickView == null) {
            mJoyStickView = new JoyStickView(mContext);
            mJoyStickView.setJoyStickPresenter(this);
        }

        if (!mJoyStickView.isShowing()) {
            mJoyStickView.addToWindow();
        }
    }

    public void hideJoyStick() {
        if (mJoyStickView != null && mJoyStickView.isShowing()) {
            mJoyStickView.removeFromWindow();
        }
    }

    public LocPoint getCurrentLocPoint() {
        return mCurrentLocPoint;
    }

    public LocPoint getUpdateLocPoint() {
        if (!mIsFlyMode || mFlyTimeIndex > mFlyTime) {
            return mCurrentLocPoint;
        } else {
            double lat = mCurrentLocPoint.getLatitude() + deltaLatitude / mFlyTime;
            double lon = mCurrentLocPoint.getLongitude() + deltaLongitude / mFlyTime;
            mCurrentLocPoint.setLatitude(lat);
            mCurrentLocPoint.setLongitude(lon);
            mFlyTimeIndex++;
            return mCurrentLocPoint;
        }
    }

    public void jumpToLocation(@NonNull LocPoint location) {
        mIsFlyMode = false;
        mCurrentLocPoint = location;
    }

    public void flyToLocation(@NonNull LocPoint location, int flyTime) {
        mTargetLocPoint = location;
        mFlyTime = flyTime;
        mFlyTimeIndex = 0;
        mIsFlyMode = true;
        deltaLatitude = mTargetLocPoint.getLatitude() - mCurrentLocPoint.getLatitude();
        deltaLongitude = mTargetLocPoint.getLongitude() - mCurrentLocPoint.getLongitude();
        mCurrentLocPoint.setBearing(calBearing(mCurrentLocPoint, mTargetLocPoint));
    }

    public void flyToLocation(@NonNull LocPoint[] location, int[] flyTime) throws InterruptedException {
        int length = location.length;
        for(int i = 0;i < length;i++){
            mTargetLocPoint = location[i];
            mFlyTime = flyTime[i];
            mFlyTimeIndex = 0;
            mIsFlyMode = true;
            deltaLatitude = mTargetLocPoint.getLatitude() - mCurrentLocPoint.getLatitude();
            deltaLongitude = mTargetLocPoint.getLongitude() - mCurrentLocPoint.getLongitude();
            mCurrentLocPoint.setBearing(calBearing(mCurrentLocPoint, mTargetLocPoint));
            while(true){
                if(mFlyTimeIndex > mFlyTime){
                    break;
                }
                Thread.currentThread().sleep(1000);
            }
        }
    }

    private float calBearing(LocPoint st, LocPoint ed){
        double x = ed.getLatitude() - st.getLatitude();
        double y = ed.getLongitude() - st.getLongitude();
        float angle = 0;
        if(y != 0){
            angle = (float)(Math.atan(x/y)/Math.PI*180);
        }else{
            if(x > 0){
                angle = 90;
            }else if(x < 0){
                angle = 270;
            }
        }
        return angle;
    }

    public boolean isFlyMode() {
        return mIsFlyMode;
    }

    public void stopFlyMode() {
        mIsFlyMode = false;
    }

    public void setMoveStep(double moveStep) {
        mMoveStep = moveStep;
    }

    public double getMoveStep() {
        return mMoveStep;
    }


    @Override
    public void onSetLocationClick() {
        Logger.d(TAG, "onSetLocationClick");
        MainActivity.startPage(mContext);
    }

    @Override
    public void onFlyClick() {
        Logger.d(TAG, "onFlyClick");
        if (mIsFlyMode) {
            stopFlyMode();
            Toast.makeText(mContext, "Stop Fly", Toast.LENGTH_SHORT).show();
        } else {
            FlyToActivity.startPage(mContext);
        }

    }

    @Override
    public void onBookmarkLocationClick() {
        Logger.d(TAG, "onBookmarkLocationClick");
        if (mCurrentLocPoint != null) {
            LocPoint locPoint = new LocPoint(mCurrentLocPoint);
            BookmarkActivity.startPage(mContext, "Bookmark", locPoint);
            Toast.makeText(mContext, "Current location is copied!" + "\n" + locPoint, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, "Service is not start!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCopyLocationClick() {
        Logger.d(TAG, "onCopyLocationClick");
        if (mCurrentLocPoint != null) {
            FakeGpsUtils.copyToClipboard(mContext, mCurrentLocPoint.toString());
            Toast.makeText(mContext, "Current location is copied!" + "\n" + mCurrentLocPoint, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onArrowUpClick() {
        Logger.d(TAG, "onArrowUpClick");
        mCurrentLocPoint.setLatitude(mCurrentLocPoint.getLatitude() + mMoveStep);
    }

    @Override
    public void onArrowDownClick() {
        Logger.d(TAG, "onArrowDownClick");
        mCurrentLocPoint.setLatitude(mCurrentLocPoint.getLatitude() - mMoveStep);
    }

    @Override
    public void onArrowLeftClick() {
        Logger.d(TAG, "onArrowLeftClick");
        mCurrentLocPoint.setLongitude(mCurrentLocPoint.getLongitude() - mMoveStep);
    }

    @Override
    public void onArrowRightClick() {
        Logger.d(TAG, "onArrowRightClick");
        mCurrentLocPoint.setLongitude(mCurrentLocPoint.getLongitude() + mMoveStep);
    }

}
