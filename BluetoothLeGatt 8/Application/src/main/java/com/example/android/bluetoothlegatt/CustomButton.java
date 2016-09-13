package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by kane on 15. 11. 29..
 */
public class CustomButton extends Button {
    public CustomButton(Context context) {
        this(context, null);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(!isInEditMode()) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CustomButton,
                    0, 0);

            try {
                String fontName = a.getString(R.styleable.CustomButton_buttonFontName);
                if (fontName.substring(0, 1).equals("N"))
                    setTypeface(Typeface.createFromAsset(getResources().getAssets(), fontName + ".otf"));
                else
                    setTypeface(Typeface.createFromAsset(getResources().getAssets(), fontName + ".ttf"));
            } finally {
                a.recycle();
            }
            setTextAlignment(TEXT_ALIGNMENT_CENTER);
        }
    }

}
