package com.gfirem.elrosacruz.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import com.gfirem.elrosacruz.SplashActivity;
import com.gfirem.elrosacruz.entity.Size;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class BaseUtils {

	private static Random r;

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static String inputStreamToString(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder total = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			total.append(line);
		}

		is.close();
		return total.toString();
	}

	public static int getResourceString(Context ctx, String name) {
		return ctx.getResources().getIdentifier(name, "string", ctx.getPackageName());
	}

	public static int getResourceId(Context ctx, String name) {
		return ctx.getResources().getIdentifier(name, "id", ctx.getPackageName());
	}

	public static String md5(String s) {
		if (s == null) {
			return null;
		}
		try {
			byte messageDigest[] = MessageDigest.getInstance("MD5").digest(s.getBytes());
			// Create Hex String
			BigInteger bi = new BigInteger(1, messageDigest);
			String result = bi.toString(16);
			if (result.length() % 2 != 0)
				result = (new StringBuilder("0")).append(result).toString();

			return result;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";

		// try {
		// MessageDigest md = MessageDigest.getInstance("MD5");
		// byte[] messageDigest = md.digest(input.getBytes());
		// BigInteger number = new BigInteger(1, messageDigest);
		// String md5 = number.toString(16);
		//
		// while (md5.length() < 32)
		// md5 = "0" + md5;
		//
		// return md5;
		// } catch (NoSuchAlgorithmException e) {
		// return null;
		// }
	}

	/**
	 *
	 * @param parameters
	 * @return
	 */
	public static String generateGetParameters(Map<String, Object> parameters, boolean isFirstElement) {
		String getParameters = "";
		Iterator it = parameters.entrySet().iterator();

		while (it.hasNext()) {
			if (isFirstElement) {
				isFirstElement = false;
				getParameters += "?";
			} else {
				getParameters += "&";
			}

			Map.Entry pairs = (Map.Entry) it.next();
			try {
				getParameters += (pairs.getKey() + "=" + URLEncoder.encode(pairs.getValue().toString(), "UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return getParameters;
	}

	public static void openUrlLink(Context ctx, String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		ctx.startActivity(intent);
	}

	public static int parseInt(String s) {
		return isInteger(s) ? Integer.parseInt(s) : 0;
	}

	public static boolean isInteger(String s) {
		return isInteger(s, 10);
	}

	private static boolean isInteger(String s, int radix) {
		if (isNullOrEmpty(s))
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0)
				return false;
		}
		return true;
	}

	public static int getRandomInt() {
		if (r == null) {
			r = new Random();
			r.setSeed((new Date()).getTime());
		}
		return r.nextInt();
	}

	/**
	 * Returns a pseudo-random number between min and max, inclusive. The
	 * difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min
	 *            Minimum value
	 * @param max
	 *            Maximum value. Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see Random#nextInt(int)
	 */
	public static int randInt(int min, int max) {

		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	public static int convertDpToPixels(float dp, Context context) {
		Resources resources = context.getResources();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
	}

	public static float convertPixelsToDp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return dp;
	}

	public static SpannableString setUnderlineText(CharSequence charSequence) {
		SpannableString content = new SpannableString(charSequence);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		return content;
	}

	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	public static void expand(final View v) {
		// set Visible
		v.setVisibility(View.VISIBLE);
		final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY);
		v.measure(ViewGroup.LayoutParams.MATCH_PARENT, heightSpec);

		v.getLayoutParams().height = 0;

		ValueAnimator mAnimator = slideAnimator(0, v.getMeasuredHeight(), v);
		mAnimator.start();
	}

	public static void collapse(final View view) {
		int finalHeight = view.getHeight();

		ValueAnimator mAnimator = slideAnimator(finalHeight, 0, view);

		mAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationEnd(Animator animator) {
				// Height=0, but it set visibility to GONE
				view.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationStart(Animator animator) {
			}

			@Override
			public void onAnimationCancel(Animator animator) {
			}

			@Override
			public void onAnimationRepeat(Animator animator) {
			}
		});
		mAnimator.start();
	}

	public static ValueAnimator slideAnimator(int start, int end, final View summary) {

		ValueAnimator animator = ValueAnimator.ofInt(start, end);

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				// Update Height
				int value = (Integer) valueAnimator.getAnimatedValue();

				ViewGroup.LayoutParams layoutParams = summary.getLayoutParams();
				layoutParams.height = value;
				summary.setLayoutParams(layoutParams);
			}
		});
		return animator;
	}

	public static void toggleState(final View v) {
		if (v.getVisibility() == View.GONE) {
			expand(v);
		} else {
			collapse(v);
		}
	}

	// Check if Google Playservices is installed in Device or not
	public static boolean checkPlayServices(SplashActivity context) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		// When Play services not found in device
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				// Show Error dialog to install Play services
				// GooglePlayServicesUtil.getErrorDialog(resultCode,
				// ApplicationController.getMainActivity(), 9000).show();
				Toast.makeText(context.getApplicationContext(),
						"Error with connect to Play Services. Error #" + resultCode, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context.getApplicationContext(),
						"This device doesn't support Play services, App will not work normally", Toast.LENGTH_LONG)
						.show();
				context.finish();
			}
			return false;
		}
		return true;
	}

	public static int GetCacheSize(Context context) {
		int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		int cacheSize = 1024 * 1024 * memClass / 8;
		return cacheSize;
	}

	public static String getBaseName(String Url) {
		return Url.substring(Url.lastIndexOf('/') + 1);
	}

	public static Size getTextSize(String text){
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);

		paint.setStyle(Paint.Style.FILL);
		paint.setTextAlign(Paint.Align.LEFT);

		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);

		return new Size(bounds.width(), bounds.height());
	}
}
