package com.simulation_test.game_simulation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by user on 15/01/05.
 */
public class SimulationTestSV extends SurfaceView implements Runnable {
	//フィールド変数
	Context context;
	Paint paint;
	Move thread = null;
	float x, y, xx, yy, xxx, xxxx, yyy, yyyy, b1, b2, b3, b4, b5, b6;
	float width, height, height2, gridNumber = 10, gridEasyNumP, gridEasyNumE = gridNumber - 1, grid, blockLine1, blockLine2;
	float dX2 = 1f, dY2 = 1f, dX1 = 0f, dY1 = 0f;
	private float multiple1, multiple2;
	//int turn = 0;	//0=描画のみ,1~9=プレイヤー,10~19=敵,20~29=その他
	//パラメーター
	int speedP = 5, speedE = 4;
	//プレイヤーのマス（スタート地点）
	private float pX1, pY1, pX2, pY2;
	private float pBlock[] = {grid * 0, grid * 0 + blockLine1, grid * 1, grid * 1 + blockLine1};
	//プレイヤーのマス（仮）
	private float pBlock2[] = {grid * 0, grid * 0 + blockLine1, grid * 1, grid * 1 + blockLine1};
	//敵のマス（スタート地点）
	float eX1 = gridNumber - 1 * grid, eY1 = gridNumber - 1 * grid + blockLine1, eX2 = gridNumber - 1 * grid, eY2 = gridNumber - 1 * grid + blockLine1;
	private float eBlock[] = {eX1, eY1, eX2, eY2};            // 敵の簡易座標
	// 敵の視界（距離）
	private int eEye1=3;
	private float[][] eEyes2 = new float[30][2];
	private ArrayList<WrongWay> eEyes1 = new ArrayList<WrongWay>();		// 敵の視界リスト１
	// この座標にプレイヤーがいたら見つかる座標リストを作成
	ArrayList<WrongWay> alarmArea = new ArrayList<WrongWay>();
	// 進入禁止エリア
	int n = (int) gridNumber * (int) gridNumber;                // 前ブロック数
	//private float[] wrongWay = new float[n];						//
	private ArrayList<WrongWay> wrongWays = new ArrayList<WrongWay>();	// 進入禁止エリアの簡易座標一覧
	private ArrayList<WrongWay> wrongWaysCopie = new ArrayList<WrongWay>();
	private ArrayList<WrongWay> pUDLR = new ArrayList<WrongWay>();		// 移動できるか調べるの上下左右ブロック簡易座標
	private int cantMoveLeft=0,cantMoveTop=0,cantMoveRight=0,cantMoveUnder=0;	// 動かせないエリアの判定
	// ゴール地点
	private float[] goal = {9f, 1f};
	//
	private float radius;                                    // 半径
	private float b1x1, b1x2, b1y1, b1y2;
	private Random rnd = new Random();                        // 乱数
	private int walk;
	private int movedPlayer;                                // プレイヤーが移動しなかった場合のフラグ
	private int moveAnim = 0;
	private float leftMove, topMove, rightMove, underMove;
	private int kakunin;
	//描画のループ用変数
	private int i = 1;
	private float dotW, dotH;
	//
	private float j = 0;
	private int k = 0;
	// 移動可能範囲を光らせる際に使用する変数
	private float m = 1;
	private double pW, pH;
	private int pictW, pictH;
	private WrongWay wLeft,wTop,wRight,wUnder;
	// 画像
	Resources res = this.getContext().getResources();
	private Bitmap gear1,gear1rs,snake_under,snake_Urs,snake_top,snake_Trs,snake_left,snake_Lrs,snake_right,snake_Rrs,
			sold1,sold1rs,
			floorout,flooroutrs,floorout2,floorout2rs,wallcross,wallcrossrs,wallw,wallwrs,floorwall,floorwallrs,
			wallt,walltrs,walltrev,walltrevrs,walltop,walltoprs,wallunder,wallunderrs,
			exclamation,exclamationrs;

	//ターン
	enum Scene {
		BattleStart, PlayerTurn, EnemyTurn, MoveCheck, Alarm, GameClear
	}

	Scene scene = Scene.BattleStart;

	//入力確認
	enum YesNo {
		Yes, No, Flat
	}

	YesNo yesno = YesNo.Flat;

	//移動用スイッチ
	enum PlayerMove {
		Left, Right, Top, Under
	}

	PlayerMove pMove = PlayerMove.Under;

	//敵行動パターン
	enum EnemyMove {
		Left, Right, Top, Under
	}

	EnemyMove eMove = EnemyMove.Left;

	// コンストラクタ
	public SimulationTestSV(Context context) {
		super(context);
		init();
	}

	public SimulationTestSV(Context context, AttributeSet attrs, Context context1) {
		super(context, attrs);
		init();
	}

	public SimulationTestSV(Context context, AttributeSet attrs, int defStyle, Context context1) {
		super(context, attrs, defStyle);
		init();
	}

	// 初期化？メソッド
	private void init() {
		// リスト再構築のために前回のリストを削除
		for (int i=0; i<30; i++){
			eEyes2[i][0] = -1f;
			eEyes2[i][1] = -1f;
		}
		scene = Scene.BattleStart;
		// 描画に必要なオブジェクトの生成
		paint = new Paint();
		paint.setColor(Color.WHITE);
		// 画面サイズを取得
		windowSize();
		// 座標計算
		coordinateCulc();
		// プレイヤーのスタート時の簡易座標を設定
		//prePlayerLocation();
		pBlock2[0]=0;
		pBlock2[1]=0;
		playerLocation();
		// 禁止エリア
		wrongWays();
		// 敵の初期位置
		k = 0;
		// プレイヤーのスタート時の進入禁止エリアの設定
		pUDLR();
		// 画像の読み込み
		gear1 = BitmapFactory.decodeResource(res, R.drawable.metalgear_lex);
		gear1rs = Bitmap.createScaledBitmap(gear1, pictW, pictW, false);
		//画像の読み込み
		snake_under = BitmapFactory.decodeResource(res, R.drawable.snake_under);
		snake_top = BitmapFactory.decodeResource(res, R.drawable.snake_top);
		snake_left = BitmapFactory.decodeResource(res, R.drawable.snake_left);
		snake_right = BitmapFactory.decodeResource(res, R.drawable.snake_right);
		sold1 = BitmapFactory.decodeResource(res, R.drawable.soldier1);
		floorout = BitmapFactory.decodeResource(res, R.drawable.floorout);
		floorout2 = BitmapFactory.decodeResource(res, R.drawable.floorout2);
		wallcross = BitmapFactory.decodeResource(res, R.drawable.wallcross);
		wallw = BitmapFactory.decodeResource(res, R.drawable.wallw);
		floorwall = BitmapFactory.decodeResource(res, R.drawable.floorwall);
		wallt = BitmapFactory.decodeResource(res, R.drawable.wallt);
		walltrev = BitmapFactory.decodeResource(res, R.drawable.walltrev);
		walltop = BitmapFactory.decodeResource(res, R.drawable.walltop);
		wallunder = BitmapFactory.decodeResource(res, R.drawable.wallunder);
		exclamation = BitmapFactory.decodeResource(res, R.drawable.exclamation);
		snake_Urs = Bitmap.createScaledBitmap(snake_under, pictW, pictW, false);
		snake_Trs = Bitmap.createScaledBitmap(snake_top, pictW, pictW, false);
		snake_Lrs = Bitmap.createScaledBitmap(snake_left, pictW, pictW, false);
		snake_Rrs = Bitmap.createScaledBitmap(snake_right, pictW, pictW, false);
		sold1rs = Bitmap.createScaledBitmap(sold1, pictW, pictW, false);
		flooroutrs = Bitmap.createScaledBitmap(floorout, pictW, pictW, false);
		floorout2rs = Bitmap.createScaledBitmap(floorout2, pictW, pictW, false);
		wallwrs = Bitmap.createScaledBitmap(wallw, pictW, pictW, false);
		floorwallrs = Bitmap.createScaledBitmap(floorwall, pictW, pictW, false);
		walltrs = Bitmap.createScaledBitmap(wallt, pictW, pictW, false);
		walltrevrs = Bitmap.createScaledBitmap(walltrev, pictW, pictW, false);
		walltoprs = Bitmap.createScaledBitmap(walltop, pictW, pictW, false);
		wallunderrs = Bitmap.createScaledBitmap(wallunder, pictW, pictW, false);
		wallcrossrs = Bitmap.createScaledBitmap(wallcross, pictW, pictW, false);
		exclamationrs = Bitmap.createScaledBitmap(exclamation, pictW, pictW, false);

		// スレッド（別？）でmoveクラスを動かす準備
		//thread = new Move();
		// コールバックインターフェースの実装
		// ---getHolderで画面(Surface)の描画に必要なSurfaceHolderを取得。
		// ---そして、SurfaceViewの描画はコールバックで行うので、これを登録
		getHolder().addCallback(
				new SurfaceHolder.Callback() {
					//SurfaceHolderのコールバックメソッド１---Surfaceが作られたとき
					public void surfaceCreated(SurfaceHolder holder) {
						draw(holder);
					}

					//SurfaceHolderのコールバックメソッド２---Surfaceに変化があったとき
					public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
					}

					//SurfaceHolderのコールバックメソッド３---Surfaceが破棄されたとき
					public void surfaceDestroyed(SurfaceHolder holder) {
					}
				});
		// 描画の準備
		// スレッド開始---run()メソッドが実行される
		//thread.start();
	}

	// 描画メソッド
	private void draw(SurfaceHolder holder) {
		// Canvasのロック
		Canvas canvas = holder.lockCanvas();
		//アンチエイリアス有効
		paint.setAntiAlias(true);

			// canvasに残っている画像を塗りつぶして消す
			canvas.drawColor(Color.GRAY);
			// リセットボタン
			paint.setColor(Color.WHITE);
			canvas.drawRect(0, 0, grid*3, grid*1, paint);
		// グリッド線を描画
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(width / width * 3);
			for (int i = 0; i <= gridNumber; i++) {
				//横線
				canvas.drawLine(0, blockLine1 + grid * i, width, blockLine1 + grid * i, paint);
				//縦線
				canvas.drawLine(grid * i, blockLine1, grid * i, blockLine1 + width, paint);
			}
			// 画像の張り付け --- 床
			//for (int i = 0; i < gridNumber; i++) {
			//	for (int j = 0; j < gridNumber; j++)
			//		canvas.drawBitmap(flooroutrs, i * grid, j * grid + blockLine1, paint);
			//}
			// 画像の張り付け --- 壁
			canvas.drawBitmap(wallwrs, 0 * grid, 2 * grid + blockLine1, paint);
			canvas.drawBitmap(wallwrs, 1 * grid, 2 * grid + blockLine1, paint);
			canvas.drawBitmap(wallwrs, 2 * grid, 2 * grid + blockLine1, paint);
			canvas.drawBitmap(floorwallrs, 3 * grid, 2 * grid + blockLine1, paint);

			canvas.drawBitmap(wallwrs, 1 * grid, 4 * grid + blockLine1, paint);
			canvas.drawBitmap(wallwrs, 2 * grid, 4 * grid + blockLine1, paint);

			canvas.drawBitmap(wallwrs, 0 * grid, 6 * grid + blockLine1, paint);
			canvas.drawBitmap(wallwrs, 2 * grid, 6 * grid + blockLine1, paint);
			canvas.drawBitmap(wallwrs, 3 * grid, 6 * grid + blockLine1, paint);
			canvas.drawBitmap(wallcrossrs, 4 * grid, 6 * grid + blockLine1, paint);
			canvas.drawBitmap(walltoprs, 4 * grid, 5 * grid + blockLine1, paint);

			canvas.drawBitmap(walltoprs, 4 * grid, 7 * grid + blockLine1, paint);
			canvas.drawBitmap(wallunderrs, 4 * grid, 8 * grid + blockLine1, paint);

			canvas.drawBitmap(walltoprs, 4 * grid, 0 * grid + blockLine1, paint);
			canvas.drawBitmap(wallunderrs, 4 * grid, 1 * grid + blockLine1, paint);
			canvas.drawBitmap(walltoprs, 4 * grid, 2 * grid + blockLine1, paint);
			canvas.drawBitmap(wallunderrs, 4 * grid, 3 * grid + blockLine1, paint);

			canvas.drawBitmap(wallwrs, 5 * grid, 8 * grid + blockLine1, paint);
			canvas.drawBitmap(wallwrs, 6 * grid, 8 * grid + blockLine1, paint);
			canvas.drawBitmap(wallwrs, 7 * grid, 8 * grid + blockLine1, paint);
			canvas.drawBitmap(wallwrs, 9 * grid, 8 * grid + blockLine1, paint);

			canvas.drawBitmap(wallwrs, 8 * grid, 0 * grid + blockLine1, paint);
			canvas.drawBitmap(wallwrs, 8 * grid, 1 * grid + blockLine1, paint);
			canvas.drawBitmap(wallwrs, 9 * grid, 0 * grid + blockLine1, paint);
			canvas.drawBitmap(gear1rs, 9 * grid, 1 * grid + blockLine1, paint);
			// 敵の視界を表示
			switch (eMove) {
				// 左向き
				case Left:
					leftCheck(canvas);
					break;
				// 上向き
				case Top:
					paint.setColor(Color.argb(100, 200, 0, 0));
					canvas.drawRect(eBlock[0] * grid, eBlock[1] * grid - grid + blockLine1, eBlock[0] * grid + grid, eBlock[1] * grid + blockLine1, paint);
					break;
				// 右向き
				case Right:
					paint.setColor(Color.argb(100, 200, 0, 0));
					canvas.drawRect(eBlock[0] * grid + grid, eBlock[1] * grid + blockLine1, eBlock[0] * grid + grid + grid, eBlock[1] * grid + grid + blockLine1, paint);
					break;
				// 下向き
				case Under:
					paint.setColor(Color.argb(100, 200, 0, 0));
					canvas.drawRect(eBlock[0] * grid, eBlock[1] * grid + grid + blockLine1, eBlock[0] * grid + grid, eBlock[1] * grid + grid + grid + blockLine1, paint);
					break;
				default:
					break;
			}
			// プレイヤーターンになったら移動可能マスに色を付ける
			paint.setColor(Color.argb(100, 0, 100, 200));
			//左
			if (cantMoveLeft < 1 && pBlock[0] > 0) {
				canvas.drawRect(pBlock[0] * grid - grid, pBlock[1] * grid + blockLine1, pBlock[0] * grid, pBlock[1] * grid + grid + blockLine1, paint);
			}
			//上
			if (cantMoveTop < 1 && pBlock[1] > 0) {
				canvas.drawRect(pBlock[0] * grid, pBlock[1] * grid - grid + blockLine1, grid * pBlock[0] + grid, grid * pBlock[1] + blockLine1, paint);
			}
			//右
			if (cantMoveRight < 1 && pBlock[0] + 1f < gridNumber) {
				canvas.drawRect(pBlock[0] * grid + grid, grid * pBlock[1] + blockLine1, grid * pBlock[0] + grid + grid, grid * pBlock[1] + grid + blockLine1, paint);
			}
			//下
			if (cantMoveUnder < 1 && pBlock[1] + 1f < gridNumber) {
				canvas.drawRect(pBlock[0] * grid, pBlock[1] * grid + grid + blockLine1, pBlock[0] * grid + grid, pBlock[1] * grid + grid + grid + blockLine1, paint);
			}
			// タッチしたブロックに色を付ける
			paint.setColor(Color.GREEN);
			//canvas.drawRect(grid * dX1, grid * dY1 + blockLine1,    //始点
			//		grid * dX2, grid * dY2 + blockLine1,    //終点
			//		paint);

			//プレイヤーのアイコン---進行方向に画像と向きを合わせる
			float moving = 0.1f;
			float e = pBlock2[0] + moving;
			float ee= pBlock2[0] - moving;
			switch (pMove) {
				case Left:
					// 左へ向かう指示を受信したら、0.3秒（300）で動かす。コマ送りの分割はgrid/10づつ程度。
					// 指定の座標に到着するまでgrid/10（Thread.sleep(30)）づつ左に移動させる。
					// grid/10移動したらその位置を一時保存し
					canvas.drawBitmap(snake_Lrs, ee*grid, pBlock[1]*grid+blockLine1, paint);
					pBlock2[0] -= moving;
					break;
				case Top:
					canvas.drawBitmap(snake_Trs, pBlock[0] * grid, pBlock[1] * grid + blockLine1, paint);
					break;
				case Right:
					// 左へ向かう指示を受信したら、0.3秒（300）で動かす。コマ送りの分割はgrid/10づつ程度。
					// 指定の座標に到着するまでgrid/10（Thread.sleep(30)）づつ左に移動させる。
					// grid/10移動したらその位置を一時保存し
						canvas.drawBitmap(snake_Rrs, e*grid, pBlock[1] * grid + blockLine1, paint);
						// 移動した分を足して一時保存し、次のループに使う
						pBlock2[0] += moving;
					break;
				case Under:
					canvas.drawBitmap(snake_Urs, pBlock[0] * grid, pBlock[1] * grid + blockLine1, paint);
					break;
				default:
					break;
			}
			//敵のアイコン
			canvas.drawBitmap(sold1rs, eBlock[0] * grid, eBlock[1] * grid + blockLine1, paint);
			// 敵の簡易座標
			wrongWays.add(new WrongWay(eBlock[0], eBlock[1]));

		// 視界内（見つかる座標リスト）にプレイヤーがいるか確認
		for (WrongWay www : alarmArea) {
			float eee = www.getF1();
			float f = www.getF2();
			// もしいたら発見された流れになる
			if (pBlock[0] == eee && pBlock[1] == f) {
				// 警戒モードにする
				scene = Scene.Alarm;
				// 警戒マークを表示
				paint.setColor(Color.rgb(0,0,0));
				float tempY = eBlock[1] - 1;
				canvas.drawBitmap(exclamationrs, eBlock[0] * grid, tempY * grid + blockLine1, paint);
			}
		}

		//行動パターン決定確認のポップアップ
			switch (scene) {
				case BattleStart:
					// 外枠
					paint.setColor(Color.argb(230, 0, 150, 150));
					canvas.drawRect(0, dotH * 43, width, dotH * 58, paint);
					paint.setColor(Color.argb(200, 0, 0, 0));
					paint.setTextSize(dotH * 4);
					paint.setTextAlign(Paint.Align.CENTER);
					canvas.drawText("g  a  m  e    s  t  a  r  t", width / 2, dotH * 52, paint);
					break;
				case MoveCheck:
					//外枠
					paint.setColor(Color.argb(200, 0, 0, 0));
					canvas.drawRect(dotW * 10, dotH * 40, dotW * 90, dotH * 60 + dotW, paint);
					paint.setColor(Color.argb(200, 150, 150, 150));
					canvas.drawRect(dotW * 11, dotH * 50, dotW * 49, dotH * 60, paint);
					canvas.drawRect(dotW * 50, dotH * 50, dotW * 89, dotH * 60, paint);
					//文字
					paint.setColor(Color.rgb(230, 230, 230));
					paint.setTextSize(width / 15);
					paint.setTextAlign(Paint.Align.CENTER);
					canvas.drawText("決定しますか？", width / 2, dotH * 47, paint);
					canvas.drawText("はい", dotW * 30, dotH * 57, paint);
					canvas.drawText("いいえ", dotW * 70, dotH * 57, paint);
					break;
				case GameClear:
					// 外枠
					paint.setColor(Color.argb(230, 0, 150, 150));
					canvas.drawRect(0, dotH * 43, width, dotH * 58, paint);
					paint.setColor(Color.argb(200, 0, 0, 0));
					paint.setTextSize(dotH * 4);
					paint.setTextAlign(Paint.Align.CENTER);
					canvas.drawText("g  a  m  e    c  l  e  a  r", width / 2, dotH * 52, paint);
					// 文字
					break;
				default:
					break;
			}

			//Canvasの解放
			holder.unlockCanvasAndPost(canvas);

	}

	void leftCheck(Canvas canvas){
		// １列目、敵の正面にカベがない場合か、左端でない場合なら実行
		//if (cantMoveLeftEnemy<1 || eBlock[0]>0){
		int l;
		// 敵の座標が上端でなければ
		if (eBlock[1]>0) {	//////////
			// 敵の視界リスト上
			for (int i = 0; i <= 7; i++) {
				// lの初期化
				l = 0;
				// 敵の視界リスト
				float a = eEyes2[i][0]; // 視界エリアiのx
				float b = eEyes2[i][1]; // 視界エリアiのy
				// 視界の座標が0,0以上（グリッド枠内）の場合
				if (a >= 0 && b >= 0) {
					// 障害物リストと視界リストn個目a,bが同じでない場合に描画する
					for (WrongWay ww : wrongWaysCopie) { // 障害物の座標リスト
						float c = ww.getF1();        // x
						float d = ww.getF2();        // y
						//Log.v("move", i + "/ " + a + "=" + c + " " + b + "=" + d);
						// もし障害物と視界の座標が被ったらl＝１にしてフラグを立てる
						if (a == c && b == d) {
							l += 1;
						}
					}
					if (l > 0) {
						// もしある0番が障害物エリアなら関係する場所も一時的に障害物エリアにする
						wrongWaysCopie.add(new WrongWay(a - 1, b - 1)); //////////
						wrongWaysCopie.add(new WrongWay(a - 1, b));
					} else if (l == 0) {
						// 視界を描画
						paint.setColor(Color.argb(100, 200, 0, 0));
						canvas.drawRect(a*grid, b*grid+blockLine1, a*grid+grid, b*grid+grid+blockLine1, paint);
						// この座標にプレイヤーがいたら見つかる座標リストに追加
						alarmArea.add(new WrongWay(a, b));
					}
				}
			}
		}
		// 敵の座標が左端でなければ
		if (eBlock[0]>0) {
			// 敵の視界リスト中
			for (int i = 8; i <= 11; i++) {
				l = 0;
				float a = eEyes2[i][0]; // 視界エリアiのx
				float b = eEyes2[i][1]; // 視界エリアiのy
				if (a >= 0 && b >= 0) {
					for (WrongWay ww : wrongWaysCopie) { // 障害物の座標リスト
						float c = ww.getF1();        // x
						float d = ww.getF2();        // y
						if (a == c && b == d) {
							l += 1;
						}
					}
					if (l > 0) {
						wrongWaysCopie.add(new WrongWay(a - 1, b));
						wrongWaysCopie.add(new WrongWay(a - 2, b));
						wrongWaysCopie.add(new WrongWay(a - 3, b));
					} else if (l == 0) {
						paint.setColor(Color.argb(100, 200, 0, 0));
						canvas.drawRect(a*grid, b*grid+blockLine1, a*grid+grid, b*grid+grid+blockLine1, paint);
						alarmArea.add(new WrongWay(a, b));
					}
				}
			}
		}
		// 敵の座標が下端でなければ
		if (eBlock[1]<gridNumber-1) {
			// 敵の視界リスト下
			for (int i = 12; i <= 19; i++) {
				l = 0;
				float a = eEyes2[i][0]; // 視界エリアiのx
				float b = eEyes2[i][1]; // 視界エリアiのy
				if (a >= 0 && b >= 0) {
					for (WrongWay ww : wrongWaysCopie) { // 障害物の座標リスト
						float c = ww.getF1();        // x
						float d = ww.getF2();        // y
						if (a == c && b == d) {
							l += 1;
						}
					}
					if (l > 0) {
						wrongWaysCopie.add(new WrongWay(a - 1, b));
						wrongWaysCopie.add(new WrongWay(a - 1, b + 1));
					} else if (l == 0) {
						paint.setColor(Color.argb(100, 200, 0, 0));
						canvas.drawRect(a*grid, b*grid+blockLine1, a*grid+grid, b*grid+grid+blockLine1, paint);
						alarmArea.add(new WrongWay(a, b));
					}
				}
			}
		}
	}

	//タッチイベント
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 障害物エリアの初期化
//				wrongWaysCopie.clear();
				switch (scene) {
					case PlayerTurn:
						//タッチした座標を取得
						x = event.getX();
						y = event.getY();
						draw(getHolder());
						break;
					default:
						break;
				}
				//Log.v("move", "DOWN/ "+"L="+cantMoveLeft+", T="+cantMoveTop+", R="+cantMoveRight+", U="+cantMoveUnder);
				break;
			case MotionEvent.ACTION_MOVE:
				//動かしている座標を取得
				xx = event.getX();
				yy = event.getY();
				//ブロック内に入っている場合
				switch (scene) {
					case BattleStart:
						break;
					case PlayerTurn:
						break;
					case EnemyTurn:
						break;
					case MoveCheck:
						break;
					default:
						break;
				}
				break;
			case MotionEvent.ACTION_UP:
				//ターン初期状態の場合、素早さを比べてどちらのターンにするか決める
				switch (scene) {
					//初期ターン
					case BattleStart:
						if (speedP >= speedE) {
							scene = Scene.PlayerTurn;
							Log.v("test", "/// " + scene.toString());
						} else {
							scene = Scene.EnemyTurn;
							Log.v("test", "/// " + scene.toString());
						}
						draw(getHolder());
						break;
					//プレイヤーターン
					case PlayerTurn: case Alarm:
						Log.v("move", "現在のターン " + scene.toString());
						// 離した座標を取得
						xxx = event.getX();
						yyy = event.getY();
						//フリックの距離。DOWNとMOVEの差を調べる
						leftMove = x - xxx;
						topMove = y - yyy;
						rightMove = xxx - x;
						underMove = yyy - y;
						// 仮の移動先の計算
						if (x != xxx || y != yyy) {
							// どのブロックを指しているか判断する
							easyFlickCulc();
							//Log.v("move", "UP/ "+"L="+cantMoveLeft+", T="+cantMoveTop+", R="+cantMoveRight+", U="+cantMoveUnder);
							// 左に移動可能で、フリックしたのが縦より横が多い場合
							//if (cantMoveLeft==0)
							if (cantMoveLeft==0 && leftMove > topMove && leftMove > underMove && leftMove > rightMove) {
								// 左端でもない場合
								if (pBlock[0] > 0) {
									// 移動前の座標を一時保存
									pBlock2[0] = pBlock[0];
									// 移動後の座標にする
									pBlock[0] = pBlock[0] - 1f; // プレイヤー位置から左に１マス移動
									// 左へ移動するフラグを立てる
									pMove = PlayerMove.Left;
								}
							}
							// 右
							else if (cantMoveRight==0 && rightMove > topMove && rightMove > underMove && rightMove > leftMove) {
								if (pBlock[0] < gridNumber - 1f) { //右端でもない場合
									// 移動前の座標を一時保存
									pBlock2[0] = pBlock[0];
									// 移動後の座標にする
									pBlock[0] = pBlock[0] + 1f;
									// 右へ移動するフラグを立てる
									pMove = PlayerMove.Right;
								}
							}
							// 上
							else if (cantMoveTop==0 && topMove > leftMove && topMove > rightMove && topMove > underMove) {
								if (pBlock[1] > 0) { //上端でもない場合
									pBlock[1] = pBlock[1] - 1f;
									pMove = PlayerMove.Top;
								}
							}
							// 下
							else if (cantMoveUnder==0 && underMove > leftMove && underMove > topMove && underMove > rightMove) {
								if (pBlock[1] < gridNumber - 1f) { //下端でもない場合
									pBlock[1] = pBlock[1] + 1f;
									pMove = PlayerMove.Under;
								}
							}
							//確認Yes/Noをするため
							//scene = Scene.MoveCheck;
							// プレイヤーと敵を動かす
							//playerLocation();
							enemyMove();
							enemyEyes1();
							pUDLR();	//Log.v("move", "L=" + cantMoveLeft + ", T=" + cantMoveTop + ", R=" + cantMoveRight + ", U=" + cantMoveUnder);
							// プレイヤーがゴール地点に入ったか確認
							if (pBlock[0] == goal[0] && pBlock[1] == goal[1]) {
								// 一致したらゲームクリア
								scene = Scene.GameClear;
							}
							draw(getHolder());

							//if ( pBlock2[0]!=pBlock[0] ) {
								draw(getHolder());
							draw(getHolder());
							draw(getHolder());
							draw(getHolder());
							draw(getHolder());
							draw(getHolder());
							draw(getHolder());
							draw(getHolder());
							draw(getHolder());
							//}

							// 一時座標を正式に座標にする
							pBlock[0] = pBlock2[0];
							//pBlock[1] = pBlock2[1];
						}
						break;
					case GameClear:
						x = event.getX();
						y = event.getY();
						init();
					default:
						break;
				}

				//もし移動していた場合
				if (movedPlayer != 0) {
					//相手のターンにする
					scene = Scene.EnemyTurn;
					Log.v("test", "/// " + scene.toString());
					moveAnim = 0;
				}

				if (x>0 && x<grid*3 && y>0 && y<grid*1) {
					init();
				}
				// 値をリセット
				dX1 = dX2 = dY1 = dY2 = 0;
				// ログ
				Log.v("test", "x = " + x + ", " + "y = " + y);
				Log.v("test", "[はい] " + dotW * 11 + ", " + dotH * 50 + ", " + dotW * 49 + ", " + dotH * 60);
				Log.v("test", "/// " + scene.toString());
				break;
			default:
				break;
		}
		return true;
	}

	// 簡易座標とフリック距離の計算
	void easyFlickCulc() {
		//Xの簡易座標
		multiple1 = xxx / grid;
		dX1 = (float) Math.floor(multiple1);    //小数点切り捨て0
		dX2 = (float) Math.ceil(multiple1);        //小数点切り上げ1
		//Yの簡易座標
		multiple2 = yyy - blockLine1;
		multiple2 = multiple2 / grid;
		dY1 = (float) Math.floor(multiple2);
		dY2 = (float) Math.ceil(multiple2);

	}

	//初回の座標計算
	void coordinateCulc() {
		//サイズ用変数の定義
		height2 = height - width;
		blockLine1 = height2 / 2;
		blockLine2 = width + blockLine1;
		gridNumber = 10f;
		gridEasyNumP = 0f;
		grid = width / gridNumber;
		radius = grid / 2;
		dotW = width / 100;
		dotH = height / 100;
		// 敵の初期位置
		gridEasyNumE = gridNumber - 6f;
		eX1 = gridEasyNumE + 5;
		eY1 = gridEasyNumE;
		eX2 = gridNumber;
		eY2 = gridNumber;
		eBlock[0] = eX1;
		eBlock[1] = eY1;
		//画像サイズ
		pW = grid;
		pH = grid;
		pW = Math.floor(grid);
		pH = Math.floor(grid);
		pictW = (int) pW;
		pictH = (int) pH;
	}

	//プレイヤーの位置を簡易座標にする
	void playerLocation() {
		pBlock[0] = dX1;
		pBlock[1] = dY1;
		pBlock[2] = dX2;
		pBlock[3] = dY2;
	}

	//敵の座標
	void enemyLocation() {
		eBlock[0] = eX1;
		eBlock[1] = eY1;
		eBlock[3] = eY2;
	}

	// 敵１の視界リスト
	void enemyEyes1() {
		// リスト再構築のために前回のリストを削除
		for (int i=0; i<30; i++){
			eEyes2[i][0] = -1f;
			eEyes2[i][1] = -1f;
		}
		// 敵１の視界座標リスト作成
		switch (eMove) {
			case Left:
				for (int ip=0; ip<=eEye1; ip++) { // ip = 列
					switch (ip) {
						case 0:
							eEyes2[8][0] = eBlock[0] - 1;
							eEyes2[8][1] = eBlock[1];
							break;
						case 1:
							eEyes2[0][0] = eBlock[0] - ip;
							eEyes2[0][1] = eBlock[1] - ip;
							eEyes2[12][0] = eBlock[0] - ip;
							eEyes2[12][1] = eBlock[1] + ip;
							eEyes2[9][0] = eBlock[0] - 2;
							eEyes2[9][1] = eBlock[1];
							break;
						case 2:
							eEyes2[1][0] = eBlock[0] - ip;
							eEyes2[1][1] = eBlock[1] - 2;
							eEyes2[2][0] = eBlock[0] - ip;
							eEyes2[2][1] = eBlock[1] - 1;
							eEyes2[13][0] = eBlock[0] - ip;
							eEyes2[13][1] = eBlock[1] + 1;
							eEyes2[14][0] = eBlock[0] - ip;
							eEyes2[14][1] = eBlock[1] + 2;

							eEyes2[5][0] = eBlock[0] - 3;
							eEyes2[5][1] = eBlock[1] - 1;
							eEyes2[10][0] = eBlock[0] - 3;
							eEyes2[10][1] = eBlock[1];
							eEyes2[15][0] = eBlock[0] - 3;
							eEyes2[15][1] = eBlock[1] + 1;
							break;
						case 3:
							eEyes2[3][0] = eBlock[0] - ip;
							eEyes2[3][1] = eBlock[1] - 3;
							eEyes2[4][0] = eBlock[0] - ip;
							eEyes2[4][1] = eBlock[1] - 2;
							eEyes2[16][0] = eBlock[0] - ip;
							eEyes2[16][1] = eBlock[1] + 2;
							eEyes2[17][0] = eBlock[0] - ip;
							eEyes2[17][1] = eBlock[1] + 3;

							eEyes2[6][0] = eBlock[0] - 4;
							eEyes2[6][1] = eBlock[1] - 2;
							eEyes2[7][0] = eBlock[0] - 4;
							eEyes2[7][1] = eBlock[1] - 1;
							eEyes2[11][0] = eBlock[0] - 4;
							eEyes2[11][1] = eBlock[1];
							eEyes2[18][0] = eBlock[0] - 4;
							eEyes2[18][1] = eBlock[1] + 1;
							eEyes2[19][0] = eBlock[0] - 4;
							eEyes2[19][1] = eBlock[1] + 2;
							break;
						default:
							break;
					}
				}
				break;
			case Top:
				break;
			case Right:
				break;
			case Under:
				break;
			default:
				break;
		}
		// 視界の距離だけループ？

		//

	}

	// 敵の行動パターン
	void enemyMove() {
		int[] list = {4,0,0,0,0,7,3,3,6,2,2,2,2,5,1,1}; // 兵士１
		int[] list2 = {}; // 兵士２
		int[] list3 = {}; // 兵士３
		int[] list4 = {}; // 兵士４
		switch (list[k]) {
			case 0: // 左
				eX1 -= 1f;
				eBlock[0] = eX1;
				k++;
				eMove = EnemyMove.Left;
				break;
			case 1: // 上
				eY1 -= 1f;
				eBlock[1] = eY1;
				k++;
				eMove = EnemyMove.Top;
				break;
			case 2: // 右
				eX1 += 1f;
				eBlock[0] = eX1;
				k++;
				eMove = EnemyMove.Right;
				break;
			case 3: // 下
				eY1 += 1f;
				eBlock[1] = eY1;
				k++;
				eMove = EnemyMove.Under;
				break;
			case 4:
				k++;
				eMove = EnemyMove.Left;
				break;
			case 5:
				k++;
				eMove = EnemyMove.Top;
				break;
			case 6:
				k++;
				eMove = EnemyMove.Right;
				break;
			case 7:
				k++;
				eMove = EnemyMove.Under;
				break;
			default:
				break;
		}
		// 行動をループするために指定の配列に戻す
		if (k == list.length)
			k = 0;

	}

	// プレイヤーターンになったら移動可能先を光らせるクラス
	private void windowSize() {
		// 機種の画面サイズを取得
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		width = disp.getWidth();
		height = disp.getHeight();

	}

	//敵の行動
	void enemyMoveAI() {
		float a = pBlock[0];
		float b = pBlock[1];

	}

	// 進入禁止エリアを区別するのに用いるプレイヤー隣のマス（上下左右）
	void pUDLR() {

		// プレイヤーのいるマスの上下左右の簡易座標を設定
		wLeft  = new WrongWay(pBlock[0]-1f, pBlock[1]   );
		wTop   = new WrongWay(pBlock[0]   , pBlock[1]-1f);
		wRight = new WrongWay(pBlock[0]+1f, pBlock[1]   );
		wUnder = new WrongWay(pBlock[0]   , pBlock[1]+1f);
		// 進入禁止エリアの解除
		cantMoveLeft = cantMoveTop = cantMoveRight = cantMoveUnder = 0;
		// 進入禁止エリアと一つづつぶつけてチェック
		for (WrongWay w : wrongWays){
			// 左
			if(cantMoveLeft==0 && wLeft.getF1()==w.getF1() && wLeft.getF2()==w.getF2()){
				cantMoveLeft = 1;
			}
			// 上
			if(cantMoveTop==0 && wTop.getF1()==w.getF1() && wTop.getF2()==w.getF2()){
				cantMoveTop = 1;
			}
			// 右
			if(cantMoveRight==0 && wRight.getF1()==w.getF1() && wRight.getF2()==w.getF2()){
				cantMoveRight = 1;
			}
			// 下
			if(cantMoveUnder==0 && wUnder.getF1()==w.getF1() && wUnder.getF2()==w.getF2()){
				cantMoveUnder = 1;
			}
		}
	}

	// 進入禁止エリア
	void wrongWays() {
		// 初期化
		wrongWays.clear();
		wrongWaysCopie.clear();
		// 障害物
		wrongWays.add(new WrongWay(4f,0f));
		wrongWays.add(new WrongWay(4f,1f));
		wrongWays.add(new WrongWay(4f,2f));
		wrongWays.add(new WrongWay(4f,3f));

		wrongWays.add(new WrongWay(0f,2f));
		wrongWays.add(new WrongWay(1f,2f));
		wrongWays.add(new WrongWay(2f,2f));

		wrongWays.add(new WrongWay(1f,4f));
		wrongWays.add(new WrongWay(2f,4f));

		wrongWays.add(new WrongWay(0f,6f));
		wrongWays.add(new WrongWay(2f,6f));
		wrongWays.add(new WrongWay(3f,6f));
		wrongWays.add(new WrongWay(4f,6f));
		wrongWays.add(new WrongWay(4f,5f));
		wrongWays.add(new WrongWay(4f,7f));
		wrongWays.add(new WrongWay(4f,8f));

		wrongWays.add(new WrongWay(5f,8f));
		wrongWays.add(new WrongWay(6f,8f));
		wrongWays.add(new WrongWay(7f,8f));
		wrongWays.add(new WrongWay(9f,8f));

		wrongWays.add(new WrongWay(8f,0f));
		wrongWays.add(new WrongWay(9f,0f));
		wrongWays.add(new WrongWay(8f,1f));
		// 禁止エリアのコピー（敵の視界エリアの識別で使用）
		for (WrongWay w : wrongWays) {
			float a = w.getF1(); // x
			float b = w.getF2(); // y
			wrongWaysCopie.add(new WrongWay(a,b));
		}

	}

	//Runnableで必要---thread.start()で呼ばれる
	@Override
	public void run() {
		//Threadクラスのコンストラクタに渡すために用いる
		//while(true) {
		//}
	}

	// プレイヤーの移動に使う
	class Move extends Thread {
		int stage = 0;

		public void run() {
			try {
				while (thread != null) {
					Thread.sleep(500);
					switch (stage) {
						case 0:
							m = 1;
							stage = 1;
							break;
						case 1:
							m = 0;
							stage = 0;
							break;
						default:
							break;
					}
					draw(getHolder());
				}
			} catch (InterruptedException e) {
			}
		}
	}
}

//// 墓場 ////

//	// 敵１の視界リスト
//	void enemyEyes1() {
//		// リスト再構築のために前回のリストを削除
//		eEyes1.clear();
//		// 敵１の視界座標リスト作成
//		switch (eMove) {
//			case Left:
//				for (int ip=1; ip<=eEye1; ip++) { // ip = 列
//					switch (ip) {
//						case 1:
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] - ip));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1]));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] + ip));
//							break;
//						case 2:
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] - 1));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1]));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] + 1));
//
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] - ip));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] + ip));
//							break;
//						case 3:
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] - 1));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1]));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] + 1));
//							eEyes1.add(new WrongWay(eBlock[0] - ip - 1, eBlock[1]));
//							break;
//						case 4:
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] - 1));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1]));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] + 1));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] - 2));
//							eEyes1.add(new WrongWay(eBlock[0] - ip, eBlock[1] + 2));
//							eEyes1.add(new WrongWay(eBlock[0] - ip - 1, eBlock[1]));
//							break;
//						default:
//							break;
//					}
//				}
//				break;
//			default:
//				break;
//		}
//	}