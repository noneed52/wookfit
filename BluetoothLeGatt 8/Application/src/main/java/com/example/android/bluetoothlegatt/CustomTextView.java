package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by kane on 15. 11. 29..
 */
public class CustomTextView extends TextView {
    public CustomTextView(Context context) {
        this(context, null);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(!isInEditMode()) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CustomTextView,
                    0, 0);

            try {
                String fontName = a.getString(R.styleable.CustomTextView_textFontName);
                if (fontName != null && fontName.substring(0, 1).equals("N"))
                    setTypeface(Typeface.createFromAsset(getResources().getAssets(), fontName + ".otf"));
                else
                    setTypeface(Typeface.createFromAsset(getResources().getAssets(), fontName + ".ttf"));
            } finally {
                a.recycle();
            }
        }
    }

}
