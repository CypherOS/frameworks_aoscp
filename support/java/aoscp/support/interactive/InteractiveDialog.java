package aoscp.support.lottie;

import static android.app.WallpaperManager.FLAG_SYSTEM;
import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;

import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.internal.colorextraction.ColorExtractor.GradientColors;
import com.android.internal.colorextraction.drawable.GradientDrawable;

public class InteractiveDialog {

	private static final String TAG = "InteractiveDialog";

	private static final int INTERACTIVE_NO_COLOR = 0;
	private static final float INTERACTIVE_NO_ALPHA = 0.0f;

	private static final float INTERACTIVE_SCRIM_ALPHA = 0.95f;

	private Context mContext;
	private Dialog mDialog;
	private WallpaperManager mWallpaperManager;

	private boolean mBgColorAnimatable;
	private int mBackgroundColor;
	private float mBackgroundAlpha;
	private int mMessageColor;
	private String mMessage;
	private int mProgressColor;

	public InteractiveDialog(Context context) {
        mContext = context;
		mDialog = new Dialog(context, R.style.Theme_Interactive_Dialog);
		mWallpaperManager = context.getSystemService(WallpaperManager.class);
    }
	
	public void showInteractive() {
		GradientDrawable background = new GradientDrawable(mContext);
        background.setAlpha((int) (mBackgroundAlpha * 255));

		Window window = mDialog.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.getAttributes().systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
		window.getDecorView();
        window.getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.getAttributes().height = ViewGroup.LayoutParams.MATCH_PARENT;
        window.getAttributes().layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        window.setType(WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        window.setBackgroundDrawable(background);
        window.setWindowAnimations(com.android.internal.R.style.Animation_Toast);

		mDialog.setContentView(R.layout.interactive_dialog);
        mDialog.setCancelable(false);

		ProgressBar bar = mDialog.findViewById(R.id.progress);
        bar.getIndeterminateDrawable().setTint(mProgressColor);
		TextView message = mDialog.findViewById(R.id.message);
		message.setTextColor(mMessageColor);
		message.setText(mMessage);

		GradientColors gradientColors = mWallpaperManager.getWallpaperColors(FLAG_SYSTEM);
		Point displaySize = new Point();
        mContext.getDisplay().getRealSize(displaySize);
		if (mBackgroundColor == INTERACTIVE_NO_COLOR) {
			background.setColors(gradientColors, mBgColorAnimatable);
		} else {
			background.setColors(mBackgroundColor, mBackgroundColor, mBgColorAnimatable);
		}
        background.setScreenSize(displaySize.x, displaySize.y);
		if (mMessage == null) Log.d(TAG, "A message must be set to use InteractiveDialog") return;
		mDialog.show();
	}
	
	public void dismissInteractive() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	public void setMessage(String message) {
		mMessage = message;
	}

	public void setMessageColor(int messageColor) {
		mMessageColor = messageColor;
		if (mMessageColor == INTERACTIVE_NO_COLOR) {
			mMessageColor = mContext.getResources().getColor(com.android.internal.R.color.white);
		}
	}

	public void setBackgroundAlpha(float backgroundAlpha) {
		mBackgroundAlpha = backgroundAlpha;
		if (mBackgroundAlpha == INTERACTIVE_NO_ALPHA) {
			mBackgroundAlpha = INTERACTIVE_SCRIM_ALPHA;
		}
	}

	public void setBackgroundColor(int backgroundColor) {
		mBackgroundColor = backgroundColor;
	}

	public void setBackgroundColorAnimatable(boolean bgColorAnimatable) {
		mBgColorAnimatable = bgColorAnimatable;
	}
	
	public void setProgressColor(int progressColor) {
		mProgressColor = progressColor;
		if (mProgressColor == INTERACTIVE_NO_COLOR) {
			mProgressColor = mContext.getResources().getColor(com.android.internal.R.color.white);
		}
	}
}
