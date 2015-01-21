package com.simulation_test.game_simulation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by user on 15/01/08.
 */
public class onDraw2 extends SimulationTest {
	//フィールド変数
	Canvas canvas;
	static void Draw(Canvas canvas) {
		canvas.drawColor(Color.YELLOW);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawColor(Color.YELLOW);
	}

	//コンストラクタ
	public onDraw2(Context context) {
		super(context);
	}
	public onDraw2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public onDraw2(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

}
