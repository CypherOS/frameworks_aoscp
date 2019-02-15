package aoscp.support.chips;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import aoscp.support.chips.R

public class Chip extends RelativeLayout {

    private Drawable mChipIcon;
    private TextView mChipTextView;
	private String mChipText;

    public Chip(Context context) {
        this(context, null, 0);
    }

    public Chip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Chip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initViews();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup.LayoutParams baseParams = getLayoutParams();

        baseParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        baseParams.height = (int) getResources().getDimension(R.dimen.chip_height);

        this.setLayoutParams(baseParams);
    }

    private void initViews() {
		initImageView();
        initTextView();
		initBackgroundColor();
    }

    private void initImageView() {
        if (!hasChipIcon()) {
            return;
        }

        ImageView icon = new ImageView(getContext());
        LayoutParams iconParams = new LayoutParams((int) getResources().getDimension(R.dimen.chip_height), (int) getResources().getDimension(R.dimen.chip_height));
        iconParams.addRule(ALIGN_PARENT_START);
        icon.setLayoutParams(iconParams);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setId(Bits.ICON_RESID);

        if (mChipIcon != null && ((BitmapDrawable) mChipIcon).getBitmap() != null) {
            Bitmap bitmap = ((BitmapDrawable) mChipIcon).getBitmap();
            bitmap = Bits.getSquareBitmap(bitmap);
            bitmap = Bits.getScaledBitmap(getContext(), bitmap);
            icon.setImageBitmap(Bits.getCircleBitmap(getContext(), bitmap));
        }

        addView(icon);
    }

    private void initTextView() {
        if (!isAttachedToWindow()) {
            return;
        }

        if (mChipTextView == null) {
            mChipTextView = new TextView(getContext());
        }

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (hasChipIcon()) {
            params.addRule(END_OF, Bits.ICON_RESID);
            params.addRule(CENTER_VERTICAL);
        } else {
            params.addRule(CENTER_IN_PARENT);
        }

        int startMargin = hasChipIcon() ? (int) getResources().getDimension(R.dimen.chip_icon_horizontal_margin) : (int) getResources().getDimension(R.dimen.chip_horizontal_padding);
        int endMargin = (int) getResources().getDimension(R.dimen.chip_horizontal_padding);
        params.setMargins(
                startMargin,
                0,
                endMargin,
                0
        );

        mChipTextView.setLayoutParams(params);
        mChipTextView.setTextColor(R.color.chip_text_color);
        mChipTextView.setText(mChipText);
        mChipTextView.setId(TEXT_RESID);

        removeView(mChipTextView);
        addView(mChipTextView);
    }

    private void initBackgroundColor() {
		int cornerRadius = getResources().getDimension(R.dimen.chip_height) / 2;

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                cornerRadius, cornerRadius, cornerRadius, cornerRadius});
        bg.setColor(R.color.chip_background);

        setBackground(bg);
    }

    public String getChipText() {
        return mChipText;
    }

    public void setChipText(String text) {
        mChipText = text;
        requestLayout();
    }

    public boolean hasChipIcon() {
        return mChipIcon != null;
    }

    public Drawable getChipIcon() {
        return mChipIcon;
    }

    public void setChipIcon(Drawable icon) {
        mChipIcon = icon;
        requestLayout();
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        throw new RuntimeException("Background color cannot be changed");
    }
}