package com.gfirem.elrosacruz.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nineoldandroids.view.ViewHelper;

public class DisplayUtil {

	public static int getDPI(Context ctx) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 1, ctx.getResources().getDisplayMetrics());
	}

	public static float getRatio(Context ctx){
        float w = getDisplayWidth(ctx), h = getDisplayHeight(ctx);
        return Math.max(w,h) / Math.min(w,h);
	}

    public static float getDPI(Context ctx, float dipValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, ctx.getResources().getDisplayMetrics());
    }

    public static float PixelsToDip(Context ctx, float dipValue) {
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        //return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
        return (dipValue / metrics.scaledDensity);
    }

	public static float getDisplayDensity(Context ctx) {
		return ctx.getResources().getDisplayMetrics().scaledDensity;
	}

    public static float getDisplayWithInDpi(Context ctx, int aPercent) {
        return getWithPercent(ctx, aPercent) / ctx.getResources().getDisplayMetrics().scaledDensity;
    }

    public static float getDisplayHeightInDpi(Context ctx, int aPercent) {
        return getHeightPercent(ctx, aPercent) / ctx.getResources().getDisplayMetrics().scaledDensity;
    }

    public static int getDisplayWidth(Context ctx) {
		return ctx.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getDisplayHeight(Context ctx) {
		return ctx.getResources().getDisplayMetrics().heightPixels;
	}

	public static int getDisplayOrientation(Context ctx) {
		return ctx.getResources().getConfiguration().orientation;
	}

	public static ViewGroup.LayoutParams setLayoutSize(View view, int wPercent, int hPercent) {
		ViewGroup.LayoutParams _result = null;
		ViewGroup.LayoutParams aLayoutParams = view.getLayoutParams();
		Context ctx = view.getContext();
		if (aLayoutParams instanceof ViewGroup.LayoutParams) {
			if (hPercent > 0) {
				aLayoutParams.height = getHeightPercent(ctx, hPercent);
			} else {
				aLayoutParams.height = hPercent;
			}
			if (wPercent > 0) {
				aLayoutParams.width = getWithPercent(ctx, wPercent);
			} else {
				aLayoutParams.height = wPercent;
			}
			_result = aLayoutParams;
		} else if (aLayoutParams instanceof LinearLayout.LayoutParams) {
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) aLayoutParams;
		} else if (aLayoutParams instanceof RelativeLayout.LayoutParams) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) aLayoutParams;
		} else {
			ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(getWithPercent(ctx, wPercent), getHeightPercent(ctx, hPercent));
			_result = lp;
		}
		return _result;
	}

	public static void setSizeByPercent(View aView, int wPercent, int hPercent) {
		if (aView != null) {
			ViewGroup.LayoutParams lp = aView.getLayoutParams();
			if (wPercent > 0) {
				lp.width = getWithPercent(aView.getContext(), wPercent);
			}

			if (hPercent > 0) {
				lp.height = getHeightPercent(aView.getContext(), hPercent);
			}

			aView.setLayoutParams(lp);
		}
	}

	public static int getHeightPercent(Context ctx, int aPercent) {
		return getPercent(aPercent, getDisplayHeight(ctx));
	}

	public static int getWithPercent(Context ctx, int aPercent) {
		return getPercent(aPercent, getDisplayWidth(ctx));
	}

	public static int getPercent(int aPercent, int aTotal) {
		return (aTotal * aPercent) / 100;
	}

	public static void setAlpha(View view, float alpha) {
        ViewHelper.setAlpha(view, alpha);
	}
}
