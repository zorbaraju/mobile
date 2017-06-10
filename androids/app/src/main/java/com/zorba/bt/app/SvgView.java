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
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SvgView extends LinearLayout {
	int svgimageid = -1;
	ImageView iv = null;

	public SvgView(Context context) {
		this(context, (AttributeSet) null);
	}

	public SvgView(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.svgview, this);
		TypedArray typearr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SvgView, 0, 0);
		iv = (ImageView) this.findViewById(R.id.svgimageview);

		try {
			this.svgimageid = typearr.getResourceId(R.styleable.SvgView_svgviewsrc, R.raw.unknown);
			SVG svg = SVGParser.getSVGFromResource(getResources(), svgimageid);
			// Needed because of image accelaration in some devices such as
			// samsung
			iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			iv.setImageDrawable(svg.createPictureDrawable());
		} finally {
			typearr.recycle();
		}
	}

	public void setSvgImageDrawable(Drawable drawable) {
		iv.setImageDrawable(drawable);
	}
	
	public void setImageBitmap(Bitmap bm) {
		iv.setImageBitmap(bm);
	}
	
	public void setImageResource(int resid) {
		this.svgimageid = resid;
		if( resid != 0 ) {
			SVG svg = SVGParser.getSVGFromResource(getResources(), svgimageid);
			// Needed because of image accelaration in some devices such as
			// samsung
			iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			iv.setImageDrawable(svg.createPictureDrawable());
			//iv.setImageResource(resid);
		} else {
			iv.setImageDrawable(null);
		}
	}
}
