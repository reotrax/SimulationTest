package com.simulation_test.game_simulation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by user on 15/04/16.
 */
public class checkUnder {
	// 敵の視界チェック（左）
	void underCheck(Canvas canvas, Paint paint,
				  float grid, float blockLine1,
				  float[][] eEyes2, float[] eBlock,
				  ArrayList<ClosedArea> wrongWaysCopie,
				  ArrayList<ClosedArea> alarmArea,
				  float gridNumber){
		// １列目、敵の正面にカベがない場合か、左端でない場合なら実行
		//if (cantMoveLeftEnemy<1 || eBlock[0]>0){
		int l;
		// 敵の座標が左端でなければ
		if (eBlock[0]>0) {	//////////
			// 敵の視界リスト左
			for (int i = 0; i <= 7; i++) {
				// lの初期化
				l = 0;
				// 敵の視界リスト
				float a = eEyes2[i][0]; // 視界エリアiのx
				float b = eEyes2[i][1]; // 視界エリアiのy
				// 視界の座標が0,0以上（グリッド枠内）の場合
				if (a >= 0 && b >= 0) {
					// 障害物リストと視界リストn個目a,bが同じでない場合に描画する
					for (ClosedArea ww : wrongWaysCopie) { // 障害物の座標リスト
						float c = ww.getF1();        	 // x
						float d = ww.getF2();        	 // y
						//Log.v("move", i + "/ " + a + "=" + c + " " + b + "=" + d);
						// もし障害物と視界の座標が被ったらl＝１にしてフラグを立てる
						if (a == c && b == d) {
							l += 1;
						}
					}
					if (l > 0) {
						// もしある0番が障害物エリアなら関係する場所も一時的に障害物エリアにする
						wrongWaysCopie.add(new ClosedArea(a, b+1));
						wrongWaysCopie.add(new ClosedArea(a-1, b+1));
					} else if (l == 0) {
						// 視界を描画
						paint.setColor(Color.argb(100, 200, 0, 0));
						canvas.drawRect(a*grid, b*grid+blockLine1, a*grid+grid, b*grid+grid+blockLine1, paint);
						// この座標にプレイヤーがいたら見つかる座標リストに追加
						alarmArea.add(new ClosedArea(a, b));
					}
				}
			}
		}
		// 敵の座標が下端でなければ
		if (eBlock[1]<gridNumber-1) {
			// 敵の視界リスト中
			for (int i = 8; i <= 11; i++) {
				l = 0;
				float a = eEyes2[i][0]; // 視界エリアiのx
				float b = eEyes2[i][1]; // 視界エリアiのy
				if (a >= 0 && b >= 0) {
					try {
						for (ClosedArea ww : wrongWaysCopie) { // 障害物の座標リスト
							float c = ww.getF1();        // x
							float d = ww.getF2();        // y
							if (a == c && b == d) {
								l += 1;
							}
						}
					}catch (ArrayIndexOutOfBoundsException e){
					}
					if (l > 0) {
						wrongWaysCopie.add(new ClosedArea(a, b + 1));
						wrongWaysCopie.add(new ClosedArea(a, b + 2));
						wrongWaysCopie.add(new ClosedArea(a, b + 3));
					} else if (l == 0) {
						paint.setColor(Color.argb(100, 200, 0, 0));
						canvas.drawRect(a*grid, b*grid+blockLine1, a*grid+grid, b*grid+grid+blockLine1, paint);
						alarmArea.add(new ClosedArea(a, b));
					}
				}
			}
		}
		// 敵の座標が右端でなければ
		if (eBlock[0]<gridNumber-1) {
			// 敵の視界リスト右
			for (int i = 12; i <= 19; i++) {
				l = 0;
				float a = eEyes2[i][0]; // 視界エリアiのx
				float b = eEyes2[i][1]; // 視界エリアiのy
				if (a >= 0 && b >= 0) {
					for (ClosedArea ww : wrongWaysCopie) { // 障害物の座標リスト
						float c = 0; float d = 0;
						try {
							c = ww.getF1();        // x
							d = ww.getF2();        // y
						} catch (NullPointerException e) {
						}
						if (a == c && b == d) {
							l += 1;
						}
					}
					if (l > 0) {
						wrongWaysCopie.add(new ClosedArea(a, b + 1));
						wrongWaysCopie.add(new ClosedArea(a + 1, b + 1));
					} else if (l == 0) {
						paint.setColor(Color.argb(100, 200, 0, 0));
						canvas.drawRect(a*grid, b*grid+blockLine1, a*grid+grid, b*grid+grid+blockLine1, paint);
						alarmArea.add(new ClosedArea(a, b));
					}
				}
			}
		}
	}
}
