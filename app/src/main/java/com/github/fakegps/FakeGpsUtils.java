package com.github.fakegps;

import android.content.ClipboardManager;
import android.content.Context;
import android.widget.EditText;

import com.github.fakegps.model.LocPoint;

import tiger.radio.loggerlibrary.Logger;

/**
 * Created by tiger on 7/23/16.
 */
public final class FakeGpsUtils {
    private static final String TAG = "FakeGpsUtils";

    private FakeGpsUtils() {
    }

    public static void copyToClipboard(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    public static LocPoint getLocPointFromInput(Context context, EditText editText) {
        LocPoint point = null;
        String text = editText.getText().toString().replace("(", "").replace(")", "");
        String[] split = text.split(",");
        if (split.length == 2) {
            try {
                double lat = Double.parseDouble(split[0].trim());
                double lon = Double.parseDouble(split[1].trim());
                point = new LocPoint(lat, lon);
            } catch (NumberFormatException e) {
                Logger.e(TAG, "Parse loc point error!", e);
            }
        }
        return point;
    }

    public static LocPoint[] getLocPointsFromInput(Context context, EditText editText){
        LocPoint[] point = null;
        String text = editText.getText().toString();
        String[] split0 = text.split(";");
        for(int i = 0;i < split0.length;i++){
            String[] split = split0[i].split(",");
            if(split.length == 2) {
                try {
                    double lat = Double.parseDouble(split[0].trim());
                    double lon = Double.parseDouble(split[1].trim());
                    point = appendElement(point, new LocPoint(lat,lon));
                } catch (NumberFormatException e) {
                    Logger.e(TAG, "Parse loc point error!", e);
                }
            }
        }
        return point;
    }

    private static LocPoint[] appendElement(LocPoint[] original, LocPoint element) {
        int length;
        if(original == null){
            length = 0;
        }else{
            length = original.length;
        }
        LocPoint[] destination = new LocPoint[length + 1];
        if(original != null){
            System.arraycopy(original, 0, destination, 0, length);
        }
        destination[length] = element;
        return destination;
    }

    public static double getMoveStepFromInput(Context context, EditText editText) {
        double step = 0;
        String stepStr = editText.getText().toString().trim();
        try {
            step = Double.valueOf(stepStr);
        } catch (NumberFormatException e) {
            Logger.e(TAG, "Parse move step error!", e);
        }

        return step;
    }

    public static int getIntValueFromInput(Context context, EditText editText) {
        int value = 0;
        String stepStr = editText.getText().toString().trim();
        try {
            value = Integer.valueOf(stepStr);
        } catch (NumberFormatException e) {
            Logger.e(TAG, "Parse move step error!", e);
        }

        return value;
    }

    public static int[] getIntValuesFromInput(Context context, EditText editText) {
        int[] value = null;
        String stepStr = editText.getText().toString();
        String[] split = stepStr.split(",");
        for (int i = 0;i < split.length;i++) {
            try {
                value = appendElement(value, Integer.valueOf(split[i]));
            } catch (NumberFormatException e) {
                Logger.e(TAG, "Parse move step error!", e);
            }
        }

        return value;
    }

    private static int[] appendElement(int[] original, int element) {
        int length;
        if(original == null){
            length = 0;
        }else{
            length = original.length;
        }
        int[] destination = new int[length + 1];
        if(original != null){
            System.arraycopy(original, 0, destination, 0, length);
        }
        destination[length] = element;
        return destination;
    }

}
