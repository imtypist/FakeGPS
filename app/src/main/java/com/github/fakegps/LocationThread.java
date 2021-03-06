package com.github.fakegps;

import android.content.Context;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.Settings;

import com.github.fakegps.model.LocPoint;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import tiger.radio.loggerlibrary.Logger;

/**
 * LocationThread
 * Created by tiger on 7/21/16.
 */
public class LocationThread extends HandlerThread {

    private static final String TAG = "LocationThread";


    private Context mContext;
    private JoyStickManager mJoyStickManager;
    private LocationManager mLocationManager;

    private Handler mHandler;
    private LocPoint mLastLocPoint = new LocPoint(0, 0);

    private static Method mMethodMakeComplete;
    private static ILocationManager mILocationManager;

    public LocationThread(Context context, JoyStickManager joyStickManager) {
        super("LocationThread");
        mContext = context;
        mJoyStickManager = joyStickManager;

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mMethodMakeComplete == null) {
            try {
                mMethodMakeComplete = Location.class.getMethod("makeComplete", new Class[0]);
            } catch (NoSuchMethodException e) {
                Logger.e(TAG, "get Location.makeComplete method fail!", e);
            }
        }

        if (mILocationManager == null) {
            Field declaredField = null;
            try {
                declaredField = Class.forName(mLocationManager.getClass().getName()).getDeclaredField("mService");
                declaredField.setAccessible(true);
                mILocationManager = (ILocationManager) declaredField.get(mLocationManager);
            } catch (Exception e) {
                Logger.e(TAG, "get LocationManager mService fail!", e);
            }
        }


    }

    @Override
    public synchronized void start() {
        super.start();

        mHandler = new Handler(getLooper());
        mHandler.post(mUpdateLocation);
    }

    public void startThread() {
        start();
    }

    public void stopThread() {
        mHandler.removeCallbacksAndMessages(null);
        try {
            quit();
            interrupt();
        } catch (Exception e) {
            Logger.e(TAG, "stopThread fail!", e);
        }

        mJoyStickManager = null;
    }


    protected static boolean setMockLocation(int i, Context context) {
        Logger.d(TAG, "setMockLocation " + i);
        try {
            return Settings.Secure.putInt(context.getContentResolver(), "mock_location", i);
        } catch (Exception e) {
            return false;
        }
    }

    Runnable mUpdateLocation = new Runnable() {
        @Override
        public void run() {

            LocPoint locPoint = mJoyStickManager.getUpdateLocPoint();
            Logger.d(TAG, "UpdateLocation, " + locPoint);
            Location location = new Location("gps");
            try {
                location.setLatitude(locPoint.getLatitude());
                location.setLongitude(locPoint.getLongitude());
                location.setBearing(locPoint.getBearing());
                location.setAltitude(20);
                if (Build.VERSION.SDK_INT > 16) {
                    location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                }
                location.setAccuracy(1);
                if (mLastLocPoint.getLatitude() != locPoint.getLatitude()
                        || mLastLocPoint.getLongitude() != locPoint.getLongitude()) {
                    mLastLocPoint.setLatitude(locPoint.getLatitude());
                    mLastLocPoint.setLongitude(locPoint.getLongitude());
                    location.setSpeed(1);
                } else {
                    location.setSpeed(0);
                }
                location.setTime(System.currentTimeMillis());
                if (mMethodMakeComplete != null) {
                    try {
                        mMethodMakeComplete.invoke(location, new Object[0]);
                    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                mILocationManager.reportLocation(location, false);

            } catch (Exception e) {
                Logger.e(TAG, "add Location fail!", e);
            }

            mHandler.postDelayed(mUpdateLocation, 1000);
        }

    };

    public Handler getHandler() {
        return mHandler;
    }

}
       
