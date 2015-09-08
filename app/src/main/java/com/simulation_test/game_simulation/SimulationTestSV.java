package com.simulation_test.game_simulation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 15/01/05.
 */
public class SimulationTestSV extends SurfaceView implements SurfaceHolder.Callback {

	Paint paint;
	Move thread = null;
	float x, y, xxx, yyy;
	float width, height, height2,
			gridNumber = 10, gridEasyNumP, gridEasyNumE = gridNumber - 1,
			grid, blockLine1, blockLine2;
	float dX2 = 1f, dY2 = 1f, dX1 = 0f, dY1 = 0f;
	private float multiple1, multiple2;
	//パラメーター
	int speedP = 5, speedE = 4;
	//アニメーションの分割度合い
	float fps = 0.2f;
	//アニメーションの描画ループ回数
	int loopCount = 5;
	//プレイヤーのマス（スタート地点）
	private float pX1=0, pY1=0, pX2, pY2;
	private float pBlock[] = {0f,0f,0f,0f};//grid * 0, grid * 0 + blockLine1, grid * 1, grid * 1 + blockLine1};
	//プレイヤーのマス（仮）
	private float pBlock2[] = new float[4];//{grid * 0, grid * 0 + blockLine1, grid * 1, grid * 1 + blockLine1};
	// プレイヤーに対する何らかの判別にのみ使用する座標
	private float pBlock3[] = new float[2];
	//敵のマス（スタート地点）
	float eX1 = gridNumber - 1 * grid, eY1 = gridNumber - 1 * grid + blockLine1, eX2 = gridNumber - 1 * grid, eY2 = gridNumber - 1 * grid + blockLine1;
	private float eBlock[] = {eX1, eY1, eX2, eY2};            // 敵の簡易座標
	// 敵の視界（距離）
	private int eEye1=3;
	private float[][] eEyes2 = new float[30][2];
	// この座標にプレイヤーがいたら見つかる座標リストを作成
	ArrayList<ClosedArea> alarmArea = new ArrayList<ClosedArea>();
	// 進入禁止エリア
	int n = (int) gridNumber * (int) gridNumber;                // 前ブロック数
	private ArrayList<ClosedArea> closedArea = new ArrayList<ClosedArea>();	// 進入禁止エリアの簡易座標一覧
	private ArrayList<ClosedArea> closedAreaCopy = new ArrayList<ClosedArea>();	//
	private int cantMoveLeft=0,cantMoveTop=0,cantMoveRight=0,cantMoveUnder=0;	// 動かせないエリアの判定
	// ゴール地点
	private float[] goal = {9f, 1f};
	//
	private int movedPlayer;                                // プレイヤーが移動しなかった場合のフラグ
	private int moveAnim = 0;
	private float leftMove, topMove, rightMove, underMove;
	//描画のループ用変数
	private float dotW, dotH;
	//
	private int k = 0;
	// 移動可能範囲を光らせる際に使用する変数
	private float m = 1;
	private double pW, pH;
	private int pictW, pictH;
	private ClosedArea wLeft,wTop,wRight,wUnder;
	// 画像
	Resources res = this.getContext().getResources();
	private Bitmap gear1,gear1rs,snake_Urs,snake_Trs,snake_Lrs,snake_Rrs,
			sold1rs, sold2rs,enemy1,enemy2,
			flooroutrs,floorout2rs,wallcrossrs,wallwrs,floorwallrs,
			walltrs,walltrevrs,walltoprs,wallunderrs,
			exclamationrs;
	private boolean alertCheck = true;
	private int lineAnime;
	// 色（タイトル画面）
	private int grayRed,grayGrn,grayBlu,red,green,blue;
	private int toumei = 255;
	private boolean iconAnim;

	//ターン
	private enum Scene {Title, Pre, BattleStart, PlayerTurn, EnemyTurn, MoveCheck, Alarm, GameClear, GameOver}
	Scene scene = Scene.Title;
	//入力確認
	private enum YesNo {Yes, No, Flat}
	private YesNo yesno = YesNo.Flat;
	//移動用スイッチ
	private enum PlayerMove {Left, Right, Top, Under}
	private PlayerMove pMove = PlayerMove.Under;
	//敵行動パターン
	public enum EnemyMove {Left, Right, Top, Under}
	public EnemyMove eMove = EnemyMove.Left;

	// コンストラクタ
	public SimulationTestSV(Context context) {
		super(context);
		init();
		getHolder().addCallback(this);
		// コールバックインターフェースの実装
		// ---getHolderで画面(Surface)の描画に必要なSurfaceHolderを取得。
		// ---そして、SurfaceViewの描画はコールバックで行うので、これを登録
	}


	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		draw(surfaceHolder);
		startNow();
		enemyMovingCycle();
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
	}

	// 初期化メソッド
	private void init() {
		// アイコンアニメーションの切り替えに使用
		iconAnim = true;
		// タイトル画面の色を初期化
		// -> 色が徐々に変化するアニメーションで使用する
		toumei = 255;
		grayRed = 136;
		grayGrn = 136;
		grayBlu = 136;
		red = 115;
		green = 173;
		blue = 83;
		// リスト再構築のために前回のリストを削除
		for (int i=0; i<30; i++){
			eEyes2[i][0] = -1f;
			eEyes2[i][1] = -1f;
		}
		scene = Scene.Title;
		// 描画に必要なオブジェクトの生成
		paint = new Paint();
		// 画面サイズを取得
		windowSize();
		// 座標計算
		coordinateCulc();
		// プレイヤースタート位置
		pX1 = 0; pY1 = 0;
		// プレイヤーのスタート時の簡易座標を設定
		//prePlayerLocation();
		pBlock2[0]=0;
		pBlock2[1]=0;
		playerLocation();
		// 禁止エリア
		closedArea();
		// 敵の初期位置
		k = 0;
		// プレイヤーのスタート時の進入禁止エリアの設定
		pUDLR();
		// 画像の読み込み
		gear1 = BitmapFactory.decodeResource(res, R.drawable.metalgear_lex);
		gear1rs = Bitmap.createScaledBitmap(gear1, pictW, pictW, false);
		//画像の読み込み
		bitmapPreparation();
	}

	private void bitmapPreparation() {
		Bitmap snake_under = BitmapFactory.decodeResource(res, R.drawable.snake_under);
		Bitmap snake_top = BitmapFactory.decodeResource(res, R.drawable.snake_top);
		Bitmap snake_left = BitmapFactory.decodeResource(res, R.drawable.snake_left);
		Bitmap snake_right = BitmapFactory.decodeResource(res, R.drawable.snake_right);
		enemy1 = BitmapFactory.decodeResource(res, R.drawable.enemy1);
		enemy2 = BitmapFactory.decodeResource(res, R.drawable.enemy2);
		Bitmap floor = BitmapFactory.decodeResource(res, R.drawable.floor);
		Bitmap floor2 = BitmapFactory.decodeResource(res, R.drawable.floor2);
		Bitmap wall_cross = BitmapFactory.decodeResource(res, R.drawable.wallcross);
		Bitmap wall_h = BitmapFactory.decodeResource(res, R.drawable.wallw);
		Bitmap floorwall = BitmapFactory.decodeResource(res, R.drawable.floorwall);
		Bitmap wall_t_top = BitmapFactory.decodeResource(res, R.drawable.wallt);
		Bitmap wall_t_under = BitmapFactory.decodeResource(res, R.drawable.walltrev);
		Bitmap wall_v_top = BitmapFactory.decodeResource(res, R.drawable.walltop);
		Bitmap wall_v_under = BitmapFactory.decodeResource(res, R.drawable.wallunder);
		Bitmap exclamation = BitmapFactory.decodeResource(res, R.drawable.exclamation);
		snake_Urs = Bitmap.createScaledBitmap(snake_under, pictW, pictW, false);
		snake_Trs = Bitmap.createScaledBitmap(snake_top, pictW, pictW, false);
		snake_Lrs = Bitmap.createScaledBitmap(snake_left, pictW, pictW, false);
		snake_Rrs = Bitmap.createScaledBitmap(snake_right, pictW, pictW, false);
		sold1rs = Bitmap.createScaledBitmap(enemy1, pictW, pictW, false);
		sold2rs = Bitmap.createScaledBitmap(enemy2, pictW, pictW, false);
		flooroutrs = Bitmap.createScaledBitmap(floor, pictW, pictW, false);
		floorout2rs = Bitmap.createScaledBitmap(floor2, pictW, pictW, false);
		wallwrs = Bitmap.createScaledBitmap(wall_h, pictW, pictW, false);
		floorwallrs = Bitmap.createScaledBitmap(floorwall, pictW, pictW, false);
		walltrs = Bitmap.createScaledBitmap(wall_t_top, pictW, pictW, false);
		walltrevrs = Bitmap.createScaledBitmap(wall_t_under, pictW, pictW, false);
		walltoprs = Bitmap.createScaledBitmap(wall_v_top, pictW, pictW, false);
		wallunderrs = Bitmap.createScaledBitmap(wall_v_under, pictW, pictW, false);
		wallcrossrs = Bitmap.createScaledBitmap(wall_cross, pictW, pictW, false);
		exclamationrs = Bitmap.createScaledBitmap(exclamation, pictW, pictW, false);
	}

	// 初期化メソッド
	private void initRestart() {
		// アイコンアニメーションの切り替えに使用
		iconAnim = true;
		// タイトル画面の色を初期化
		toumei = 255;
		grayRed = 136;
		grayGrn = 136;
		grayBlu = 136;
		red = 115;
		green = 173;
		blue = 83;
		// リスト再構築のために前回のリストを削除
		for (int i=0; i<30; i++){
			eEyes2[i][0] = -1f;
			eEyes2[i][1] = -1f;
		}
		scene = Scene.Title;
		// 描画に必要なオブジェクトの生成
		paint = new Paint();
		// 画面サイズを取得
		windowSize();
		// 座標計算
		coordinateCulc();
		// プレイヤースタート位置
		pX1 = 0; pY1 = 0;
		// プレイヤーのスタート時の簡易座標を設定
		//prePlayerLocation();
		pBlock2[0]=0;
		pBlock2[1]=0;
		playerLocation();
		// 禁止エリア
		closedArea();
		// 敵の初期位置
		k = 0;
		// プレイヤーのスタート時の進入禁止エリアの設定
		pUDLR();
	}

	// 敵の行動スピード
	private void enemyMovingCycle() {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				switch (scene) {
					case PlayerTurn:
						if (scene == Scene.PlayerTurn || scene == Scene.Alarm) {
							// 敵を動かすための計算
							closedArea();
							enemyMove();
							EnemyEyes1 enemyEyes1 = new EnemyEyes1();
							enemyEyes1.enemyEyes1(eEyes2, eMove, eBlock, eEye1);
							pUDLR();
							draw(getHolder());
						}
						break;
					case Alarm:
						break;
				}
			}
		}, 2000, 500, TimeUnit.MILLISECONDS);
	}

	/** タイトル画面 */
	private void scene_Title(Canvas canvas) {
		canvas.drawColor(Color.rgb(red,green,blue));
		paint.setColor(Color.argb(toumei, 50, 60, 80));
		paint.setTextSize(width / 20);
		canvas.drawText("SNEAKING MISSION", width/8, height/10*4, paint);
	}

	/** ステージスタート */
	private void scene_Pre(Canvas canvas) {
		// グリッド線を描画
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(width / width * 3);
		//Log.v("test/SV", "scene_Pre = " + lineAnime);
		for (int i = 0; i<=gridNumber; i++) {
			//横線
			canvas.drawLine(0, blockLine1 + grid * i, lineAnime, blockLine1 + grid * i, paint);
			//縦線
			canvas.drawLine(grid * i, blockLine2, grid * i, blockLine2 - lineAnime, paint);
		}
	}

	// 敵アイコンのアニメーション。指定した時間間隔で呼ばれる
	private void startNow() {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (iconAnim == true) {
					sold1rs = Bitmap.createScaledBitmap(enemy1, pictW, pictW, false);
					iconAnim = false;
				} else {
					sold1rs = Bitmap.createScaledBitmap(enemy2, pictW, pictW, false);
					iconAnim = true;
				}
				draw(getHolder());
			}
		},1000,500,TimeUnit.MILLISECONDS);
		// 1,Runnable() 2,最初に？呼び出す間隔 3,次に呼び出すまでの間隔 4,時間の単位
	}

	// 描画準備
	private void layer0_PreDraw(Canvas canvas) {
		// リセットボタン
		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, grid * 3, grid * 1, paint);
		// 現在座標
		paint.setTextSize(dotH * 2);
		paint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText("X = ", width / 2, dotH * 2, paint);
		canvas.drawText(Float.toString(pBlock[0]), width / 4 * 3, dotH * 2, paint);
		canvas.drawText("Y = ", width / 2, dotH * 4, paint);
		canvas.drawText(Float.toString(pBlock[1]), width / 4 * 3, dotH * 4, paint);
		canvas.drawText("cantMoveLeft = ", width / 2, dotH * 6, paint);
		canvas.drawText(Integer.toString(cantMoveLeft), width / 4 * 3, dotH * 6, paint);
		canvas.drawText("cantMoveTop = ", width / 2, dotH * 8, paint);
		canvas.drawText(Integer.toString(cantMoveTop), width / 4 * 3, dotH * 8, paint);
		canvas.drawText("cantMoveRight = ", width / 2, dotH * 10, paint);
		canvas.drawText(Integer.toString(cantMoveRight), width / 4 * 3, dotH * 10, paint);
		canvas.drawText("cantMoveUnder = ", width / 2, dotH * 12, paint);
		canvas.drawText(Integer.toString(cantMoveUnder), width / 4 * 3, dotH * 12, paint);
		canvas.drawText("closedArea = ", width / 2, dotH * 14, paint);
		canvas.drawText(Integer.toString(closedArea.size()), width / 4 * 3, dotH * 14, paint);
		canvas.drawText("closedAreaCopie = ", width / 2, dotH * 16, paint);
		canvas.drawText(Integer.toString(closedAreaCopy.size()), width / 4 * 3, dotH * 16, paint);

		// グリッド線を描画
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(width / width * 3);
		for (int i = 0; i <= gridNumber; i++) {
			//横線
			canvas.drawLine(0, blockLine1 + grid * i, width, blockLine1 + grid * i, paint);
			//縦線
			canvas.drawLine(grid * i, blockLine1, grid * i, blockLine1 + width, paint);
		}
	}

	// 敵の視界（赤い四角）
	private void layer1_EnemyEye(Canvas canvas) {
		// 前回の視界の削除
		//enemyEyes1();
		alarmArea.clear();
		closedArea();
		switch (eMove) {
			// 左向き
			case Left:
				try {
					// 敵の視界チェック（左）
					checkLeft leftCheck = new checkLeft();
					leftCheck.leftCheck(canvas,paint,grid,blockLine1,eEyes2,eBlock, closedAreaCopy,alarmArea,gridNumber);
				} catch (ConcurrentModificationException e) {
					//非同期クラスのインスタンスが別スレッドから変更されると発生？
				}
				break;
			// 上向き
			case Top:
				try {
					checkTop checkTop = new checkTop();
					checkTop.topCheck(canvas, paint, grid, blockLine1, eEyes2, eBlock, closedAreaCopy, alarmArea, gridNumber);
				} catch (ConcurrentModificationException e) { }
				break;
			// 右向き
			case Right:
				try {
					checkRight rightCheck = new checkRight();
					rightCheck.rightCheck(canvas, paint, grid, blockLine1, eEyes2, eBlock, closedAreaCopy, alarmArea, gridNumber);
				} catch (ConcurrentModificationException e) { }
				break;
			// 下向き
			case Under:
				try {
					checkUnder underCheck = new checkUnder();
					underCheck.underCheck(canvas, paint, grid, blockLine1, eEyes2, eBlock, closedAreaCopy, alarmArea, gridNumber);
				} catch (ConcurrentModificationException e) { }
				break;
			default:
				break;
		}
	}

	// プレイヤー移動可能範囲（青い四角）
	private void layer2_PlayerMovableRange(Canvas canvas) {
		paint.setColor(Color.argb(100, 0, 100, 200));
		//左
		if (cantMoveLeft < 1 && pBlock[0] > 0) {
			canvas.drawRect(pBlock[0]*grid-grid, pBlock[1]*grid+blockLine1, pBlock[0]*grid, pBlock[1]*grid+grid+blockLine1, paint);
		}
		//上
		if (cantMoveTop < 1 && pBlock[1] > 0) {
			canvas.drawRect(pBlock[0]*grid, pBlock[1]*grid-grid+blockLine1, grid*pBlock[0]+grid, grid*pBlock[1]+blockLine1, paint);
		}
		//右
		if (cantMoveRight < 1 && pBlock[0] + 1f < gridNumber) {
			canvas.drawRect(pBlock[0]*grid+grid, grid*pBlock[1]+blockLine1, grid*pBlock[0]+grid+grid, grid*pBlock[1]+grid+blockLine1, paint);
		}
		//下
		if (cantMoveUnder < 1 && pBlock[1] + 1f < gridNumber) {
			canvas.drawRect(pBlock[0]*grid, pBlock[1]*grid+grid+blockLine1, pBlock[0]*grid+grid, pBlock[1]*grid+grid+grid+blockLine1, paint);
		}
	}

	// ステージ画面構成
	private void layer3_StageLayout(Canvas canvas) {
		paint.setColor(Color.GRAY);
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
		canvas.drawBitmap(walltoprs,   4 * grid, 5 * grid + blockLine1, paint);

		canvas.drawBitmap(walltoprs,   4 * grid, 7 * grid + blockLine1, paint);
		canvas.drawBitmap(wallunderrs, 4 * grid, 8 * grid + blockLine1, paint);

		canvas.drawBitmap(walltoprs,   4 * grid, 0 * grid + blockLine1, paint);
		canvas.drawBitmap(wallunderrs, 4 * grid, 1 * grid + blockLine1, paint);
		canvas.drawBitmap(walltoprs,   4 * grid, 2 * grid + blockLine1, paint);
		canvas.drawBitmap(wallunderrs, 4 * grid, 3 * grid + blockLine1, paint);

		canvas.drawBitmap(wallwrs, 5 * grid, 8 * grid + blockLine1, paint);
		canvas.drawBitmap(wallwrs, 6 * grid, 8 * grid + blockLine1, paint);
		canvas.drawBitmap(wallwrs, 7 * grid, 8 * grid + blockLine1, paint);
		canvas.drawBitmap(wallwrs, 9 * grid, 8 * grid + blockLine1, paint);

		canvas.drawBitmap(wallwrs, 8 * grid, 0 * grid + blockLine1, paint);
		canvas.drawBitmap(wallwrs, 8 * grid, 1 * grid + blockLine1, paint);
		canvas.drawBitmap(wallwrs, 9 * grid, 0 * grid + blockLine1, paint);
		canvas.drawBitmap(gear1rs, 9 * grid, 1 * grid + blockLine1, paint);
	}

	//プレイヤーのアイコン---進行方向に画像と向きを合わせる
	private void layer4_PlayerIcon(Canvas canvas) {
		paint.setColor(Color.rgb(0, 0, 0));
		float moving = 1f;
		float e = pBlock2[0] + moving;
		float ee= pBlock2[0] - moving;
		switch (pMove) {
			case Left:
				// 左へ向かう指示を受信したら、0.3秒（300）で動かす。コマ送りの分割はgrid/10づつ程度。
				// 指定の座標に到着するまでgrid/10（Thread.sleep(30)）づつ左に移動させる。
				// grid/10移動したらその位置を一時保存し
				canvas.drawBitmap(snake_Lrs, pBlock[0]*grid, pBlock[1]*grid+blockLine1, paint);
				pBlock2[0] -= moving;
				break;
			case Top:
				canvas.drawBitmap(snake_Trs, pBlock[0]*grid, pBlock[1]*grid+blockLine1, paint);
				break;
			case Right:
				// 左へ向かう指示を受信したら、0.3秒（300）で動かす。コマ送りの分割はgrid/10づつ程度。
				// 指定の座標に到着するまでgrid/10（Thread.sleep(30)）づつ左に移動させる。
				// grid/10移動したらその位置を一時保存し
				//canvas.drawBitmap(snake_Rrs, e * grid, pBlock[1] * grid + blockLine1, paint);
				canvas.drawBitmap(snake_Rrs, pBlock[0] * grid, pBlock[1] * grid + blockLine1, paint);
				// 移動した分を足して一時保存し、次のループに使う
				pBlock2[0] += moving;
				break;
			case Under:
				canvas.drawBitmap(snake_Urs, pBlock[0]*grid, pBlock[1]*grid+blockLine1, paint);
				break;
			default:
				break;
		}
	}

	// layer5_敵のアイコン
	private void layer5_EnemyIcon(Canvas canvas) {
		canvas.drawBitmap(sold1rs, eBlock[0] * grid, eBlock[1] * grid + blockLine1, paint);
	}

	// 敵の簡易座標
	private void layer6_EnemyAlertCheck(Canvas canvas) {
		//敵の現在地を進入禁止リストに追加 ＞ 削除方法が...(^_^;
		//closedArea.add(new WrongWay(eBlock[0], eBlock[1]));
		// 視界内（見つかる座標リスト）にプレイヤーがいるか確認
		if (alertCheck == true) {
			for (ClosedArea www : alarmArea) {
				float eee = www.getF1();
				float f = www.getF2();
				// もしいたら発見された流れになる
				if (pBlock[0] == eee && pBlock[1] == f) {
					// 警戒モードにする
					scene = Scene.Alarm;
					scene = Scene.GameOver;
					// 警戒マークを表示 -!-
					paint.setColor(Color.rgb(0, 0, 0));
					float tempY = eBlock[1] - 1;
					canvas.drawBitmap(exclamationrs, eBlock[0] * grid, tempY * grid + blockLine1, paint);
					break;
				}
			}
		}
	}


	//行動パターン決定確認のポップアップ
	private void layer9_PopUp(Canvas canvas) {
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
			case GameOver:
				// 外枠
				paint.setColor(Color.argb(230,255,108,91));
				canvas.drawRect(0, dotH * 43, width, dotH * 58, paint);
				paint.setColor(Color.argb(200,30,30,30));
				paint.setTextSize(dotH * 4);
				paint.setTextAlign(Paint.Align.CENTER);
				canvas.drawText("g  a  m  e    o  v  e  r", width / 2, dotH * 52, paint);
				// 文字
				break;
			default:
				break;
		}
	}

	// 描画メソッド
	private void draw(SurfaceHolder holder) {
		// Canvasのロック
		Canvas canvas = holder.lockCanvas();
		//アンチエイリアス有効
		paint.setAntiAlias(true);
		// canvasに残っている画像を塗りつぶして消す
		canvas.drawColor(Color.GRAY);
		switch (scene) {
			case Title:
				scene_Title(canvas);
				break;
			case Pre:
				scene_Pre(canvas);
				break;
			case BattleStart: case PlayerTurn: case GameClear: case GameOver: case Alarm:
				layer0_PreDraw(canvas);
				layer1_EnemyEye(canvas);
				layer2_PlayerMovableRange(canvas);
				layer3_StageLayout(canvas);
				layer4_PlayerIcon(canvas);
				layer5_EnemyIcon(canvas);
				if (alertCheck==true)
					layer6_EnemyAlertCheck(canvas);
				layer9_PopUp(canvas);
				break;
		}
		//Canvasの解放
		holder.unlockCanvasAndPost(canvas);
	}

	//タッチイベント
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 障害物エリアの初期化
				switch (scene) {
					case PlayerTurn: case Alarm:
						//タッチした座標を取得
						x = event.getX();
						y = event.getY();
						break;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				switch (scene) {
					//初期ターン
					case Title:
						// タイトルの透明度と色を徐々に変化させるループ
						int blueABS = (int)Math.abs(grayBlu - blue);
						int toumei10 = toumei/blueABS;
						do {
							toumei -= toumei10 * 2;
							if (red<grayRed-5) {
								red += 2;
							} else if (red!=grayRed) {
								red += 1;
							}
							if (green<grayGrn+5) {
								green -= 2;
							} else if (green!=grayGrn) {
								green -= 1;
							}
							blue += 2;
							draw(getHolder());
							Log.v("testSV", "blue = " + blue);
						} while (blue < grayBlu);
						// 描画完了次第、sceneを次に進める
						scene = Scene.Pre;
						// 自動的にscene_Preを実行するためbreakは削除。
						//break;
					case Pre:
						//タッチしたら縦横の線が画面端に達するまで描画を始める
						float wid100 = width/45;
						lineAnime = 0;
						for (lineAnime=0; lineAnime<width; lineAnime+=wid100) {
							draw(getHolder());
						}
						scene = Scene.BattleStart;
						draw(getHolder());
						break;
					case BattleStart:
						if (speedP >= speedE) {
							scene = Scene.PlayerTurn;
							//Log.v("test", "/// " + scene.toString());
						} else {
							scene = Scene.EnemyTurn;
							//Log.v("test", "/// " + scene.toString());
						}
						draw(getHolder());
						break;
					//プレイヤーターン
					case PlayerTurn: case Alarm:
						//Log.v("move", "現在のターン " + scene.toString());
						// 離した座標を取得
						xxx = event.getX();
						yyy = event.getY();
						//フリックの距離。DOWNとMOVEの差を調べる
						leftMove = x - xxx;
						topMove = y - yyy;
						rightMove = xxx - x;
						underMove = yyy - y;
						// 仮の移動先の計算
						playerMovingPoint();
						break;
					case GameClear: // クリアボタン
						x = event.getX();
						y = event.getY();
						init();
						//scene = Scene.Pre;
						break;
					case GameOver:
						init();
						scene = Scene.BattleStart;
						draw(getHolder());
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
					initRestart();
					draw(getHolder());
					x=0;
					y=0;
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

	// onTouchEventで使用。プレイヤー座標計算
	private void playerMovingPoint() {
		if (x != xxx || y != yyy) {
			// どのブロックを指しているか判断する
			easyFlickCulc();
			// 左に移動可能で、フリックしたのが縦より横が多い場合
			if (cantMoveLeft==0 && leftMove > topMove && leftMove > underMove && leftMove > rightMove) {
				// 左へ移動するフラグ
				pMove = PlayerMove.Left;
				playerMoveDrawAndCheck();
			}
			// 右
			else if (cantMoveRight==0 && rightMove > topMove && rightMove > underMove && rightMove > leftMove) {
				pMove = PlayerMove.Right;
				playerMoveDrawAndCheck();
			}
			// 上
			else if (cantMoveTop==0 && topMove > leftMove && topMove > rightMove && topMove > underMove) {
				pMove = PlayerMove.Top;
				playerMoveDrawAndCheck();
			}
			// 下
			else if (cantMoveUnder==0 && underMove > leftMove && underMove > topMove && underMove > rightMove) {
				pMove = PlayerMove.Under;
				playerMoveDrawAndCheck();
			}

			// プレイヤーがゴール地点に入ったか確認
			if (pBlock[0] == goal[0] && pBlock[1] == goal[1]) {
				// 一致したらゲームクリア
				scene = Scene.GameClear;
			}
		}
	}
	// playerMovingPoint()で使用
	private void playerMoveDrawAndCheck() {
		// 敵の視界サーチをOFF
		alertCheck = false;
		// 移動後の座標にする
		switch (pMove){
			case Left:
				// fpsによりループ回数可変
				for (int i=0; i<loopCount; i++) {
					// 移動後の座標にする
					pBlock[0] = pBlock[0] - fps; // プレイヤー位置から左に１マス移動
					if (i==loopCount-1) {
						pBlock[0] = Math.round(pBlock[0]);
						alertCheck = true;
					}
					draw(getHolder());
				}
				break;
			case Right:
				for (int i=0; i<loopCount; i++) {
					pBlock[0] = pBlock[0] + fps;
					if (i==loopCount-1) {
						pBlock[0] = Math.round(pBlock[0]);
						alertCheck = true;
					}
					draw(getHolder());
				}
				break;
			case Top:
				for (int i=0; i<loopCount; i++) {
					pBlock[1] = pBlock[1] - fps;
					if (i==loopCount-1) {
						pBlock[1] = Math.round(pBlock[1]);
						alertCheck = true;
					}
					draw(getHolder());
				}
				break;
			case Under:
				for (int i=0; i<loopCount; i++) {
					pBlock[1] = pBlock[1] + fps;
					if (i==loopCount-1) {
						pBlock[1] = Math.round(pBlock[1]);
						alertCheck = true;
					}
					draw(getHolder());
				}
				break;
		}
		// 移動後に進入禁止エリアの確認　＞　その後に描画される流れ
		pUDLR();
		draw(getHolder());
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



	// 敵の行動パターン
	void enemyMove() {
		int[] list = {4,0,0,0,0,7,3,3,6,2,5,1,1,6,2,2,2}; // 兵士１
		int[] list2 = {}; // 兵士２
		int[] list3 = {}; // 兵士３
		int[] list4 = {}; // 兵士４
		switch (list[k]) {
			case 0: // 左
				eMove = EnemyMove.Left;
				alertCheck = false;
				for (int i=0; i<4; i++) {
					eX1 -= 0.2f;
					eBlock[0] = eX1;
					draw(getHolder());
				}
				alertCheck = true;
				eX1 -= 0.2f;
				eBlock[0] = Math.round(eBlock[0]);
				draw(getHolder());
				k++;
				break;
			case 1: // 上
				eMove = EnemyMove.Top;
				alertCheck = false;
				for (int i=0; i<4; i++) {
					eY1 -= 0.2f;
					eBlock[1] = eY1;
					draw(getHolder());
				}
				alertCheck = true;
				eY1 -= 0.2f;
				eBlock[1] = Math.round(eBlock[1]);
				draw(getHolder());
				k++;
				break;
			case 2: // 右
				eMove = EnemyMove.Right;
				alertCheck = false;
				for (int i=0; i<4; i++) {
					eX1 += 0.2f;
					eBlock[0] = eX1;
					draw(getHolder());
				}
				alertCheck = true;
				eX1 += 0.2f;
				eBlock[0] = Math.round(eBlock[0]);
				draw(getHolder());
				k++;
				break;
			case 3: // 下
				eMove = EnemyMove.Under;
				alertCheck = false;
				for (int i=0; i<4; i++) {
					eY1 += 0.2f;
					eBlock[1] = eY1;
					draw(getHolder());
				}
				alertCheck = true;
				eY1 += 0.2f;
				eBlock[1] = Math.round(eBlock[1]);
				draw(getHolder());
				k++;
				break;
			case 4:
				eMove = EnemyMove.Left;
				k++;
				break;
			case 5:
				eMove = EnemyMove.Top;
				k++;
				break;
			case 6:
				eMove = EnemyMove.Right;
				k++;
				break;
			case 7:
				eMove = EnemyMove.Under;
				k++;
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
		Point size = new Point();
		disp.getSize(size);
		width = size.x;
		height = size.y;

	}

	// 進入禁止エリアを区別するのに用いるプレイヤー隣のマス（上下左右）
	void pUDLR() {
		// プレイヤーのいるマスの上下左右の簡易座標を設定
		wLeft  = new ClosedArea(pBlock[0]-1f, pBlock[1]   );
		wTop   = new ClosedArea(pBlock[0]   , pBlock[1]-1f);
		wRight = new ClosedArea(pBlock[0]+1f, pBlock[1]   );
		wUnder = new ClosedArea(pBlock[0]   , pBlock[1]+1f);
		// 進入禁止エリアの解除
		cantMoveLeft = cantMoveTop = cantMoveRight = cantMoveUnder = 0;
		// 進入禁止エリアと一つづつぶつけてチェック
		try {
			for (ClosedArea w : closedArea) {
				// 左
				if (cantMoveLeft == 0 && wLeft.getF1() == w.getF1() && wLeft.getF2() == w.getF2() || pBlock[0] == 0) {
					// プレイヤーの左１マスが進入禁止エリアの場合か、画面左端の場合にフラグON。
					cantMoveLeft = 1;
				}
				// 上
				if (cantMoveTop == 0 && wTop.getF1() == w.getF1() && wTop.getF2() == w.getF2() || pBlock[1] == 0) {
					cantMoveTop = 1;
				}
				// 右
				if (cantMoveRight == 0 && wRight.getF1() == w.getF1() && wRight.getF2() == w.getF2() || pBlock[0] == gridNumber - 1) {
					cantMoveRight = 1;
				}
				// 下
				if (cantMoveUnder == 0 && wUnder.getF1() == w.getF1() && wUnder.getF2() == w.getF2() || pBlock[1] == gridNumber - 1) {
					cantMoveUnder = 1;
				}
			}
		}catch (ConcurrentModificationException e){
			Log.v("test/ERROR", " = " + e);
		}
	}

	// 進入禁止エリア
	void closedArea() {
		// 初期化
		closedArea.clear();
		closedAreaCopy.clear();
		// 障害物
		closedArea.add(new ClosedArea(4f,0f));
		closedArea.add(new ClosedArea(4f,1f));
		closedArea.add(new ClosedArea(4f,2f));
		closedArea.add(new ClosedArea(4f,3f));

		closedArea.add(new ClosedArea(0f,2f));
		closedArea.add(new ClosedArea(1f,2f));
		closedArea.add(new ClosedArea(2f,2f));

		closedArea.add(new ClosedArea(1f,4f));
		closedArea.add(new ClosedArea(2f,4f));

		closedArea.add(new ClosedArea(0f,6f));
		closedArea.add(new ClosedArea(2f,6f));
		closedArea.add(new ClosedArea(3f,6f));
		closedArea.add(new ClosedArea(4f,6f));
		closedArea.add(new ClosedArea(4f,5f));
		closedArea.add(new ClosedArea(4f,7f));
		closedArea.add(new ClosedArea(4f,8f));

		closedArea.add(new ClosedArea(5f,8f));
		closedArea.add(new ClosedArea(6f,8f));
		closedArea.add(new ClosedArea(7f,8f));
		closedArea.add(new ClosedArea(9f,8f));

		closedArea.add(new ClosedArea(8f,0f));
		closedArea.add(new ClosedArea(9f,0f));
		closedArea.add(new ClosedArea(8f,1f));
		// 禁止エリアのコピー（敵の視界エリアの識別とプレイヤー確認で使用）
		try {
			for (ClosedArea w : closedArea) {
				float a = w.getF1(); // x
				float b = w.getF2(); // y
				closedAreaCopy.add(new ClosedArea(a, b));
			}
		}catch (ConcurrentModificationException e){
		}
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
