package com.zorba.bt.app;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.zorba.bt.app.dao.DeviceData;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SvgView extends ImageView {
	int svgimageid = -1;

	public SvgView(Context var1) {
		this(var1, (AttributeSet) null);
	}

	public SvgView(Context context, AttributeSet var2) {
		super(context, var2);

	}

	public void setSvgDrawable(int svgdrawable) {
		this.svgimageid = svgdrawable;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (svgimageid != -1) {
			SVG svg = SVGParser.getSVGFromResource(getResources(), svgimageid);
			// Needed because of image accelaration in some devices such as
			// samsung
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			setImageDrawable(svg.createPictureDrawable());
		}
	}

}
