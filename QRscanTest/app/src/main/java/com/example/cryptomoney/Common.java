package com.example.cryptomoney;

import android.app.Dialog;
import android.content.Context;
//import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import cn.memobird.gtx.common.GTXKey;

public class Common {
    static int filterType = GTXKey.FILTER.TYPE_DEFAULT;

    public static final int SIZE_384 = 384;                 // 纸条宽度
    public static final int SIZE_576 = 576;                 // 纸条宽度
    public static int DEFAULT_IMAGE_WIDTH = SIZE_384;//384;

    public static void showShortToast(Context context, String string) {
        Toast toast = Toast.makeText(context, string, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    public static void showLongToast(Context context, String string) {
        Toast toast = Toast.makeText(context, string, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static int getNextFilter() {
        filterType = filterType >= GTXKey.FILTER.TYPE_NEGATIVE ?
                GTXKey.FILTER.TYPE_DEFAULT : filterType + 1;
        return filterType;
    }

    public static String getFilterName(int filterType) {
        String strFilterName = "";
        switch (filterType) {
            case GTXKey.FILTER.TYPE_DEFAULT:
                strFilterName = "默认";
                break;
            case GTXKey.FILTER.TYPE_IMAGE_ENHANCE:
                strFilterName = "图片";
                break;
            case GTXKey.FILTER.TYPE_WORD_ENHANCE:
                strFilterName = "文字";
                break;
            case 2:
                strFilterName = "文字";
                break;
            case GTXKey.FILTER.TYPE_SKETCH:
                strFilterName = "素描";
                break;
            case GTXKey.FILTER.TYPE_STROKE:
                strFilterName = "描边";
                break;
            case GTXKey.FILTER.TYPE_INVERTED:
                strFilterName = "反色";
                break;
            case GTXKey.FILTER.TYPE_INK:
                strFilterName = "喷墨";
                break;
            case GTXKey.FILTER.TYPE_EMBOSS:
                strFilterName = "浮雕";
                break;
            case GTXKey.FILTER.TYPE_NEGATIVE:
                strFilterName = "底片";
                break;
        }
        return strFilterName;
    }

    /**
     * 圆形等待加载进度条
     */
    public static Dialog showLoadingDialog(Context mContext) {
        if (mContext != null) {
            LayoutInflater inflaters = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflaters.inflate(R.layout.dialog_circle_loading, null);
            ProgressBar progressBar = view.findViewById(R.id.progressbar_loading);
            if (android.os.Build.VERSION.SDK_INT > 22) {
                //android 6.0替换clip的加载动画
                progressBar.setIndeterminateDrawable(ContextCompat.getDrawable(mContext, R.drawable.dialog_circle_loading_animation));
            }
            Dialog loadingDialog = new Dialog(mContext, R.style.paringTheme);
            loadingDialog.setCancelable(true);
            loadingDialog.setContentView(view);
            loadingDialog.setCanceledOnTouchOutside(false);
            return loadingDialog;
        } else {
            return null;
        }

    }
}
