package com.zorba.bt.app;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SvgButton extends LinearLayout {
	int svgimageid = -1;
	ImageButton iv = null;

	public SvgButton(Context context) {
		this(context, (AttributeSet) null);
	}

	public SvgButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.svgbutton, this);
		TypedArray typearr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SvgButton, 0, 0);
		iv = (ImageButton) this.findViewById(R.id.svgimagebutton);

		try {
			this.svgimageid = typearr.getResourceId(R.styleable.SvgButton_svgbuttonsrc, R.raw.g1);
		} finally {
			typearr.recycle();
		}
		setImage();
	}

	private void setImage(){
		try {
			SVG svg = SVGParser.getSVGFromResource(getResources(), svgimageid);
			iv.setImageDrawable(svg.createPictureDrawable());
		} finally {
			
		}
	}
	@Override 
	public void setOnClickListener(OnClickListener l){
		iv.setOnClickListener(l);
	}

	public void setImageResource(int resid) {
		if( resid == 0 ) {
			iv.setImageDrawable(null);
		} else {
			this.svgimageid = resid;
			setImage();
		}
	}
}
