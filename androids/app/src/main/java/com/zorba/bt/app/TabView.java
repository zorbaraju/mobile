package com.zorba.bt.app;

import java.util.HashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * TODO: document your custom view class.
 */
public class TabView extends LinearLayout {
	private String mExampleString; // TODO: use a default from R.string...
	private int mExampleColor = Color.RED; // TODO: use a default from
											// R.color...
	private float mExampleDimension = 0; // TODO: use a default from R.dimen...
	private Drawable mExampleDrawable;

	private TextPaint mTextPaint;
	private float mTextWidth;
	private float mTextHeight;
	OnClickListener listener = null;

	private HashMap<String, View> contentMap = new HashMap<String, View>();
	
	public TabView(Context context) {
		super(context);
		init(context, null, 0);
	}

	public TabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public TabView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		// Load attributes
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabView, defStyle, 0);

		mExampleString = a.getString(R.styleable.TabView_exampleString);
		mExampleColor = a.getColor(R.styleable.TabView_exampleColor, mExampleColor);
		// Use getDimensionPixelSize or getDimensionPixelOffset when dealing
		// with
		// values that should fall on pixel boundaries.
		mExampleDimension = a.getDimension(R.styleable.TabView_exampleDimension, mExampleDimension);

		((LayoutInflater)context.getSystemService("layout_inflater")).inflate(R.layout.tabview, this);
	      
	}
	
	public void addTab(String tabName, int layoutid){
		LinearLayout buttonLayout = (LinearLayout)findViewById(R.id.tabButtonView);
		Button button = new Button(getContext());
		button.setText(tabName);
		button.setBackgroundColor(Color.TRANSPARENT);//+spb 060217 
		buttonLayout.addView(button);
		
		View view = (View) ((LayoutInflater)getContext().getSystemService("layout_inflater")).inflate(layoutid, null);
		LinearLayout contentLayout = (LinearLayout)findViewById(R.id.tabContentView);
		contentLayout.removeAllViews();
		contentLayout.addView(view, 0);
		
		contentMap.put(tabName, view);
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String tn = ((Button)v).getText().toString();
				selectTab(tn);
				listener.onClick(v);
			

			}
		});
		
	}
	
	public void selectTab(String tn){
		LinearLayout contentLayout = (LinearLayout)findViewById(R.id.tabContentView);
		
		contentLayout.removeAllViews();
		contentLayout.addView(contentMap.get(tn), 0);
	 
		
	}

	/**
	 * Gets the example string attribute value.
	 * 
	 * @return The example string attribute value.
	 */
	public String getExampleString() {
		return mExampleString;
	}

	/**
	 * Sets the view's example string attribute value. In the example view, this
	 * string is the text to draw.
	 * 
	 * @param exampleString
	 *            The example string attribute value to use.
	 */
	public void setExampleString(String exampleString) {
		mExampleString = exampleString;
	}

	/**
	 * Gets the example color attribute value.
	 * 
	 * @return The example color attribute value.
	 */
	public int getExampleColor() {
		return mExampleColor;
	}

	/**
	 * Sets the view's example color attribute value. In the example view, this
	 * color is the font color.
	 * 
	 * @param exampleColor
	 *            The example color attribute value to use.
	 */
	public void setExampleColor(int exampleColor) {
		mExampleColor = exampleColor;
	}

	/**
	 * Gets the example dimension attribute value.
	 * 
	 * @return The example dimension attribute value.
	 */
	public float getExampleDimension() {
		return mExampleDimension;
	}

	/**
	 * Sets the view's example dimension attribute value. In the example view,
	 * this dimension is the font size.
	 * 
	 * @param exampleDimension
	 *            The example dimension attribute value to use.
	 */
	public void setExampleDimension(float exampleDimension) {
		mExampleDimension = exampleDimension;
	}

	/**
	 * Gets the example drawable attribute value.
	 * 
	 * @return The example drawable attribute value.
	 */
	public Drawable getExampleDrawable() {
		return mExampleDrawable;
	}

	/**
	 * Sets the view's example drawable attribute value. In the example view,
	 * this drawable is drawn above the text.
	 * 
	 * @param exampleDrawable
	 *            The example drawable attribute value to use.
	 */
	public void setExampleDrawable(Drawable exampleDrawable) {
		mExampleDrawable = exampleDrawable;
	}

	public void setTabSelectionListener(OnClickListener listener) {
		this.listener = listener;
	}
}
