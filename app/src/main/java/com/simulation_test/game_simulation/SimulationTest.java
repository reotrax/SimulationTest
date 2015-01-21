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
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

/**
 * Created by user on 15/01/05.
 */
public class SimulationTest extends View {
	//フィールド変数
	Context context;
	Paint paint;
	float x,y,xx,yy,xxx,xxxx,yyy,yyyy,b1, b2, b3, b4;
	float width, height, height2, gridNumber=10, gridEasyNumP, gridEasyNumE=gridNumber-1, grid, blockLine1, blockLine2;
	float dX2=1f, dY2=1f, dX1=0f, dY1=0f;
	private float multiple1, multiple2;
	//int turn = 0;	//0=描画のみ,1~9=プレイヤー,10~19=敵,20~29=その他
	//パラメーター
	int speedP = 5, speedE = 4;
	//プレイヤーのマス（スタート地点）
	private float pX1, pY1, pX2, pY2;
	private float pBlock[] = {grid*0, grid*0+blockLine1, grid*1, grid*1+blockLine1};
	//プレイヤーのマス（仮）
	private  float pBlock2[] = {grid*0, grid*0+blockLine1, grid*1, grid*1+blockLine1};
	//敵のマス（スタート地点）
	float eX1=gridNumber-1*grid, eY1=gridNumber-1*grid+blockLine1, eX2=gridNumber-1*grid, eY2=gridNumber-1*grid+blockLine1;
	private float eBlock[] = {eX1, eY1, eX2, eY2};
	private float radius;
	private float b1x1, b1x2, b1y1,  b1y2;
	private Random rnd = new Random();
	private int walk;
	private int movedPlayer;		//動かなかった場合のフラグ
	private int moveAnim=0;
	private float leftMove, topMove, rightMove, underMove;
	private int kakunin;
	//描画のループ用変数
	private int i=1;
	private float dotW,dotH;
	//
	private float j = 0;
	private int k = 0;


	//ターン
	enum Scene {BattleStart,PlayerTurn,EnemyTurn,MoveCheck}
	Scene scene = Scene.BattleStart;
	//入力確認
	enum YesNo {Yes,No,Flat}
	YesNo yesno = YesNo.Flat;
	//移動用スイッチ
	enum PlayerMove {Left,Right,Top,Under,Flat}
	PlayerMove pMove = PlayerMove.Flat;
	//敵行動パターン
	enum EnemyMove {Left,Right,Top,Under,Flat}
	EnemyMove eMove = EnemyMove.Flat;

	//デフォルトコンストラクタ
	public SimulationTest(Context context) {
		super(context);
		init(context);
	}

	//XMLから利用するためのコンストラクタ
	public SimulationTest(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SimulationTest(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	//最初に実行される。初期化？
	private void init(Context context) { //-1-
		this.context = context;
		this.paint = new Paint();
		//Viewのサイズ
		onWindowFocusChanged(true);
		//エンカウント用の乱数
		walk = rnd.nextInt(3) + 3;

	}

	//1-1 Viewのサイズを取得
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);

		width = findViewById(R.id.SimulationTest).getWidth();
		height = findViewById(R.id.SimulationTest).getHeight();
	}

	//仮の簡易座標
	void prePlayerLocation() {
		pBlock2[0] = dX1;
		pBlock2[1] = dY1;
		pBlock2[2] = dX1 + 1f;
		pBlock2[3] = dY1 + 1f;
	}

	//仮の簡易座標を正式に座標にする
	void playerLocation() {
		pBlock[0] = pBlock2[0];
		pBlock[1] = pBlock2[1];
		pBlock[2] = pBlock2[0] + 1f;
		pBlock[3] = pBlock2[1] + 1f;
	}

	//敵の座標
	void enemyLocation() {
		eBlock[0] = eX1;
		eBlock[1] = eY1;
		eBlock[2] = eX2;
		eBlock[3] = eY2;
	}

	//敵の行動
	void enemyMoveAI() {
		float a = pBlock[0];
		float b = pBlock[1];

	}

	//初回の座標計算
	void coordinateCulc() {
		//サイズ用変数の定義
		height2 = height - width;
		blockLine1 = height2 / 2;
		blockLine2 = width + blockLine1;
		gridNumber = 10f;
		gridEasyNumE = gridNumber - 1f;
		gridEasyNumP = 0f;
		grid = width / gridNumber;
		radius = grid/2;
		dotW = width / 100;
		dotH = height / 100;
		//敵の位置
		eX1= gridEasyNumE;
		eY1= gridEasyNumE;
		eX2= gridNumber;
		eY2= gridNumber;
		eBlock[0] = eX1;
		eBlock[1] = eY1;

		//ログ
		Log.v("test", eX1+", "+eY1);
	}

	//画面描画
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		//初回だけ数値計算
		switch (scene){
			case BattleStart:
				prePlayerLocation();
				playerLocation();
				coordinateCulc();

			break;
		}
		//アンチエイリアス有効
		paint.setAntiAlias(true);
		//背景を塗りつぶす
		canvas.drawColor(Color.GRAY);
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(width/width*3);
		for(int i=0; i<=gridNumber; i++) {
			//横線
			canvas.drawLine(0, blockLine1 + grid * i, width, blockLine1 + grid * i, paint);
			//縦線
			canvas.drawLine(grid*i, blockLine1, grid*i, blockLine1 + width, paint);
		}
		//移動ターンになったら移動可能マスに色を付ける
			switch (scene) {
				case BattleStart: //初回
					break;
				case PlayerTurn: //プレイヤーターン
					//左
					if (pBlock[0] > 0) {
						b1 = pBlock[0] - 1f;
						paint.setColor(Color.BLUE);
						canvas.drawRect(b1 * grid, pBlock[1] * grid + blockLine1, pBlock[0] * grid, pBlock[3] * grid + blockLine1, paint);
						//スライドしたら仮の移動先を描画
						paint.setColor(Color.LTGRAY);
						canvas.drawCircle(grid * pBlock2[0] + radius, grid * pBlock2[1] + blockLine1 + radius, radius, paint);
					}
					//上
					if (pBlock[1] > 0) {
						b2 = pBlock[1] - 1f;
						paint.setColor(Color.BLUE);
						canvas.drawRect(pBlock[0] * grid, b2 * grid + blockLine1, grid * pBlock[2], grid * pBlock[1] + blockLine1, paint);
					}
					//右
					if (pBlock[2] < gridNumber) {
						paint.setColor(Color.BLUE);
						canvas.drawRect(pBlock[0] * grid + grid, grid * pBlock[1] + blockLine1, grid * pBlock[2] + grid, grid * pBlock[3] + blockLine1, paint);
					}
					//下
					if (pBlock[3] < gridNumber) {
						paint.setColor(Color.BLUE);
						canvas.drawRect(pBlock[0] * grid, pBlock[3] * grid + blockLine1, pBlock[2] * grid, pBlock[3] * grid + grid + blockLine1, paint);
					}
					break;
				case EnemyTurn: //敵ターン
					break;
				default:
					break;
			}
		//タッチしたブロックに色を付ける
		paint.setColor(Color.GREEN);
		canvas.drawRect(grid*dX1, grid*dY1+blockLine1,	//始点
						grid*dX2, grid*dY2+blockLine1,	//終点
						paint);
		//自分のアイコン
		paint.setColor(Color.WHITE);
		canvas.drawCircle(pBlock[0]*grid+radius, pBlock[1]*grid+radius+blockLine1, radius, paint);
		//画像の読み込み
		Resources res = this.getContext().getResources();
		Bitmap gear1 = BitmapFactory.decodeResource(res, R.drawable.gear1);
		Bitmap snake = BitmapFactory.decodeResource(res, R.drawable.snake);
		Bitmap sold1 = BitmapFactory.decodeResource(res, R.drawable.soldier1);
//		Bitmap floor1 = BitmapFactory.decodeResource(res, R.drawable.floor1);
//		Bitmap floor2 = BitmapFactory.decodeResource(res, R.drawable.floor2);
//		Bitmap wall1 = BitmapFactory.decodeResource(res, R.drawable.wall1);
//		Bitmap wall2 = BitmapFactory.decodeResource(res, R.drawable.wall2);
//		Bitmap wall3 = BitmapFactory.decodeResource(res, R.drawable.wall3);
//		Bitmap wall4 = BitmapFactory.decodeResource(res, R.drawable.wall4);
		//画像サイズ
		double pW = grid;
		double pH = grid;
		int pictW, pictH;
		pW = Math.floor(grid);
		pH = Math.floor(grid);
		pictW = (int)pW;
		pictH = (int)pH;
		Bitmap snakers = Bitmap.createScaledBitmap(snake, pictW, pictW, false);
		Bitmap sold1rs = Bitmap.createScaledBitmap(sold1, pictW, pictW, false);
//		Bitmap floor1rs = Bitmap.createScaledBitmap(floor1, pictW, pictW, false);
//		Bitmap floor2rs = Bitmap.createScaledBitmap(floor2, pictW, pictW, false);
//		Bitmap wall1rs = Bitmap.createScaledBitmap(wall1, pictW, pictW, false);
//		Bitmap wall2rs = Bitmap.createScaledBitmap(wall2, pictW, pictW, false);
//		Bitmap wall3rs = Bitmap.createScaledBitmap(wall3, pictW, pictW, false);
//		Bitmap wall4rs = Bitmap.createScaledBitmap(wall4, pictW, pictW, false);
		// 床アイコン
		for(int i=0; i<gridNumber; i++) {
//			for(int j=0; j<gridNumber; j++)
//				canvas.drawBitmap(floor1rs, i * grid, j * grid + blockLine1, paint);
		}
		// 壁アイコン
//		canvas.drawBitmap( wall1rs, 0*grid, 2*grid+blockLine1, paint );
//		canvas.drawBitmap( wall1rs, 1*grid, 2*grid+blockLine1, paint );
//		canvas.drawBitmap( wall1rs, 2*grid, 2*grid+blockLine1, paint );
//		canvas.drawBitmap( wall1rs, 4*grid, 0*grid+blockLine1, paint );
//		canvas.drawBitmap( wall1rs, 4*grid, 1*grid+blockLine1, paint );
//		canvas.drawBitmap( wall1rs, 4*grid, 2*grid+blockLine1, paint );
//		canvas.drawBitmap( wall1rs, 4*grid, 3*grid+blockLine1, paint );
//		canvas.drawBitmap( wall1rs, 4*grid, 4*grid+blockLine1, paint );

		//プレイヤーのアイコン
		canvas.drawBitmap( snakers, pBlock[0]*grid, pBlock[1]*grid+blockLine1, paint );
		//敵のアイコン
		canvas.drawBitmap( sold1rs, eBlock[0]*grid, eBlock[1]*grid+blockLine1, paint );
		//行動パターン決定確認のポップアップ
		switch (scene){
			case BattleStart:
				paint.setColor(Color.argb(200, 0, 0, 0));
				canvas.drawRect(width / 100 * 10, height / 100 * 40, width / 100 * 90, height / 100 * 60, paint);
				paint.setColor(Color.rgb(230, 230, 230));
				paint.setTextSize(width / 15);
				paint.setTextAlign(Paint.Align.CENTER);
				canvas.drawText("入力してください", width / 2, height / 100 * 50, paint);
				paint.setColor(Color.rgb(100, 180, 230));
				paint.setTextSize(width / 20);
				canvas.drawText("タッチして次にすすむ", width / 2, height / 100 * 55, paint);
				break;
			case MoveCheck:
				paint.setColor(Color.argb(200,0,0,0));
				canvas.drawRect(dotW * 10, dotH * 40, dotW * 90, dotH * 60 + dotW, paint);
				paint.setColor(Color.argb(200, 150, 150, 150));
				canvas.drawRect(dotW * 11, dotH * 50, dotW * 49, dotH * 60, paint);
				canvas.drawRect(dotW * 50, dotH * 50, dotW * 89, dotH * 60, paint);
				paint.setColor(Color.rgb(230, 230, 230));
				paint.setTextSize(width / 15);
				paint.setTextAlign(Paint.Align.CENTER);
				canvas.drawText("決定しますか？", width / 2, dotH * 45, paint);
				canvas.drawText("はい", dotW * 30, dotH * 57, paint);
				canvas.drawText("いいえ", dotW * 70, dotH * 57, paint);
				break;
			default:
				break;
		}
//		switch (scene) {
//			case BattleStart:
//				if(i==1) {
//					try {
//						Thread.sleep(500);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					i++;
//					invalidate();
//				}
//		}
	}




	//タッチイベント
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//タッチした座標を取得
				x = event.getX();
				y = event.getY();
				//描画ループ用変数を解除
				i = 0;
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				//動かしている座標を取得
				xx = event.getX();
				yy = event.getY();
				//ブロック内に入っている場合
				switch (scene){
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
				//離した座標を取得
				xxx = event.getX();
				yyy = event.getY();
				//どのブロックを指しているか判断する
				//Xの簡易座標
				multiple1 = xxx / grid;
				dX1 = (float) Math.floor(multiple1);    //小数点切り捨て0
				dX2 = (float) Math.ceil(multiple1);        //小数点切り上げ1
				//Yの簡易座標
				multiple2 = yyy - blockLine1;
				multiple2 = multiple2 / grid;
				dY1 = (float) Math.floor(multiple2);
				dY2 = (float) Math.ceil(multiple2);
				//フリックの距離。DOWNとMOVEの差を調べる
				leftMove  = x - xxx;
				topMove   = y - yyy;
				rightMove = xxx - x;
				underMove = yyy - y;

				//ターン初期状態の場合、素早さを比べてどちらのターンにするか決める
				switch ( scene ) {
					//初期ターン
					case BattleStart:
						if ( speedP >= speedE ) {
							scene = Scene.PlayerTurn;
						} else {
							scene = Scene.EnemyTurn;
						}
						break;
					//プレイヤーターン
					case PlayerTurn:
						if(x!=xxx || y!=yyy) {
							//フリックしたのが縦より横が多い場合
							if (leftMove > topMove && leftMove > underMove && leftMove > rightMove) {
								if ( pBlock[0] > 0) { //左端でもない場合
									//オリジナルの位置から左に１マス移動した数値を仮の位置にして仮を表示
									pBlock2[0] = pBlock[0] - 1f;
								}
							} else if (rightMove > topMove && rightMove > underMove && rightMove > leftMove) {
								if ( pBlock[0] < gridNumber - 1f) { //右端でもない場合
									pBlock2[0] = pBlock[0] + 1f;
								}
							} else if (topMove > leftMove && topMove > rightMove && topMove > underMove) {
								if ( pBlock[1] > 0) { //上端でもない場合
									pBlock2[1] = pBlock[1] - 1f;
								}
							} else if (underMove > leftMove && underMove > topMove && underMove > rightMove) {
								if ( pBlock[1] < gridNumber - 1f) { //下端でもない場合
									pBlock2[1] = pBlock[1] + 1f;
								}
							}
							//確認Yes/Noをするため
							scene = Scene.MoveCheck;
							//仮の移動先を描画
							invalidate();
						}
						break;
					//敵ターン
					case EnemyTurn:
						break;
					//確認ターン
					case MoveCheck:
						//「はい」
						if (x>dotW * 11 && y>dotH * 50 && x<dotW * 49 && y<dotH * 60) {
							playerLocation();
							scene = Scene.EnemyTurn;
							//敵の行動パターン
							int[] list = {0,1,1,1,0,0,3,3,2,2};
							switch (list[k]) {
								case 0:
									eX1 -= 1f;
									eBlock[0] = eX1;
									k++;
									break;
								case 1:
									eY1 -= 1f;
									eBlock[1] = eY1;
									k++;
									break;
								case 2:
									eX1 += 1f;
									eBlock[0] = eX1;
									k++;
									break;
								case 3:
									eY1 += 1f;
									eBlock[1] = eY1;
									k++;
									break;
								default:
									break;
							}
							if(k==10)
								k=2;
							scene = Scene.PlayerTurn;
							//結果を表示
							invalidate();
						//「いいえ」
						} else if (x>dotW * 50 && y>dotH * 50 && x<dotW * 89 && y<dotH * 60) {
							scene = Scene.PlayerTurn;
						}
						break;
					default:
						break;
				}

				Log.v("test", "dx1 " + dX1 + ", dy1 " + dY1);
				Log.v("test", "dx2 " + dX2 + ", dy2 " + dY2);
				Log.v("test", "pBlock = (" + pBlock[0] + ", " + pBlock[1] + ", " + pBlock[2] + ", " + pBlock[3]);

				invalidate();

				//もし移動していた場合
				if(movedPlayer !=0) {
					//相手のターンにする
					scene = Scene.EnemyTurn;
					moveAnim = 0;
				}

				//値をリセット
				dX1 = dX2 = dY1 = dY2 =  0;
				break;
			default:
				break;
		}
		return true;
	}

}

//				//指定のマスにタッチした場合のみ実行
//				if(pBlock[0]-1f==dX1 && pBlock[1]==dY1){		//左
//					playerLocation();
//					//エンカウント-1
//					rnd -= 1;
//					movedPlayer = 1;
//				}else if(pBlock[0]==dX1 && pBlock[1]-1f==dY1){	//上
//					playerLocation();
//					rnd -= 1;
//					movedPlayer = 1;
//				}else if(pBlock[0]+1f==dX1 && pBlock[1]==dY1){	//右
//					playerLocation();
//					rnd -= 1;
//					movedPlayer = 1;
//				}else if(pBlock[0]==dX1 && pBlock[1]+1f==dY1){	//下
//					playerLocation();
//					rnd -= 1;
//					movedPlayer = 1;
//				}else if(pBlock[0]==dX1 && pBlock[1]==dY1){		//移動してない
//					rnd -= 1;
//					movedPlayer = 0;
//				}
