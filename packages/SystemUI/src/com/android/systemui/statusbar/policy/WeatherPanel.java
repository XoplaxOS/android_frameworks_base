
package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.PorterDuff;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;

public class WeatherPanel extends FrameLayout {

    private static final String TAG = "WeatherPanel";

    private boolean mAttached;

    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_CONDITION = "condition";
    public static final String EXTRA_CONDITION_CODE = "condition_code";
    public static final String EXTRA_FORECAST_DATE = "forecast_date";
    public static final String EXTRA_TEMP = "temp";
    public static final String EXTRA_HUMIDITY = "humidity";
    public static final String EXTRA_WIND = "wind";
    public static final String EXTRA_LOW = "todays_low";
    public static final String EXTRA_HIGH = "todays_high";

    private TextView mHighTemp;
    private TextView mSlash;
    private TextView mLowTemp;
    private TextView mCurrentTemp;
    private TextView mCity;
    private TextView mHumidity;
    private TextView mWinds;
    private TextView mCondition;
    private ImageView mConditionImage;
    private Context mContext;
    private String mCondition_code = "";
    private ContentObserver mContentObserver;
    private ContentResolver mContentResolver;
    private boolean mShowLocation;
    private int mCurrentColor = -3;

    BroadcastReceiver weatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateWeather(intent);
        }
    };

    public void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();
        mShowLocation = Settings.XOPLAX.getBoolean(resolver, Settings.XOPLAX.WEATHER_SHOW_LOCATION, true);
        updateCityVisibility();
    }

    private void updateCityVisibility() {
        if (mCity == null)
            return;

        if (mShowLocation) {
            mCity.setVisibility(View.VISIBLE);
        } else {
            mCity.setVisibility(View.GONE);
        }
    }

    public void updateWeather(Intent intent) {
        mCondition_code = (String) intent.getCharSequenceExtra(EXTRA_CONDITION_CODE);
        if (mCurrentTemp != null)
            mCurrentTemp.setText(intent.getCharSequenceExtra(EXTRA_TEMP));
        if (mHighTemp != null)
            mHighTemp.setText(intent.getCharSequenceExtra(EXTRA_HIGH));
        if (mLowTemp != null)
            mLowTemp.setText(intent.getCharSequenceExtra(EXTRA_LOW));
        if (mCity != null)
            mCity.setText(intent.getCharSequenceExtra(EXTRA_CITY));
        if (mHumidity != null)
            mHumidity.setText(intent.getCharSequenceExtra(EXTRA_HUMIDITY));
        if (mWinds != null)
            mWinds.setText(intent.getCharSequenceExtra(EXTRA_WIND));
        if (mCondition != null)
            mCondition.setText(intent.getCharSequenceExtra(EXTRA_CONDITION));
        if (mConditionImage != null) {
            int level = 100;
            try {
                level = Integer.parseInt(mCondition_code);
            } catch (Exception e) {
            }
            mConditionImage.setImageLevel(level);
        }
        updateCityVisibility();
    }

    public void updateSettings(int defaultColor) {
        mCurrentColor = defaultColor;
        updateTextColor();
    }

    private void updateTextColor() {
        int clockColor = getResources().getColor(R.color.status_bar_clock_color);
        int nowColor = mCurrentColor != -3 ? mCurrentColor : clockColor;
        setTextColor(nowColor);
    }

    public void setTextColor(int color)
    {
        if (mCurrentTemp != null)
            mCurrentTemp.setTextColor(color);
        if (mHighTemp != null)
            mHighTemp.setTextColor(color);
        if (mLowTemp != null)
            mLowTemp.setTextColor(color);
        if (mCity != null)
            mCity.setTextColor(color);
        if (mHumidity != null)
            mHumidity.setTextColor(color);
        if (mWinds != null)
            mWinds.setTextColor(color);
        if (mCondition != null)
            mCondition.setTextColor(color);
        if (mSlash != null)
            mSlash.setTextColor(color);
        if (mConditionImage != null)
            mConditionImage.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public WeatherPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHighTemp = (TextView) this.findViewById(R.id.high_temp);
        mLowTemp = (TextView) this.findViewById(R.id.low_temp);
        mCurrentTemp = (TextView) this.findViewById(R.id.current_temp);
        mCity = (TextView) this.findViewById(R.id.city);
        mHumidity = (TextView) this.findViewById(R.id.humidity);
        mWinds = (TextView) this.findViewById(R.id.winds);
        mCondition = (TextView) this.findViewById(R.id.condition);
        mConditionImage = (ImageView) this.findViewById(R.id.condition_image);
        mSlash = (TextView) this.findViewById(R.id.weatherpanel_slash);

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter("com.xoplax.INTENT_WEATHER_UPDATE");
            getContext().registerReceiver(weatherReceiver, filter, null, getHandler());

            updateSettings();
            mContentResolver = getContext().getContentResolver();
            mContentObserver = new ContentObserver(getHandler()) {
                @Override
                public void onChange(boolean selfChange) {
                    updateSettings();
                }
            };
            mContentResolver.registerContentObserver(Settings.XOPLAX
                    .getUriFor(Settings.XOPLAX.WEATHER_SHOW_LOCATION), false, mContentObserver);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(weatherReceiver);
            mAttached = false;
            mContentResolver.unregisterContentObserver(mContentObserver);
        }
    }
}
