package com.simulation_test.game_simulation;

/**
 * Created by user on 15/04/17.
 */
public class EnemyEyes1 {

	// 敵１の視界リスト
	void enemyEyes1(float[][] eEyes2, SimulationTestSV.EnemyMove eMove, float[] eBlock, int eEye1) {
		// 一歩毎に視界リストを再構築するので、最初に前回のリストを削除する
		for (int i=0; i<30; i++){
			eEyes2[i][0] = -1f;
			eEyes2[i][1] = -1f;
		}
		// 敵１の視界座標リスト作成
		switch (eMove) {
			case Left:
				// eEye1 == 敵の視界の距離 == i
				for (int i=0; i<=eEye1; i++) { // ip = 列
					switch (i) {
						case 0: // 正面１マスのみ
							eEyes2[8][0] = eBlock[0] - 1;
							eEyes2[8][1] = eBlock[1];
							break;
						case 1:
							eEyes2[0][0] = eBlock[0] - i;
							eEyes2[0][1] = eBlock[1] - i;
							eEyes2[12][0] = eBlock[0] - i;
							eEyes2[12][1] = eBlock[1] + i;
							eEyes2[9][0] = eBlock[0] - 2;
							eEyes2[9][1] = eBlock[1];
							break;
						case 2:
							eEyes2[1][0] = eBlock[0] - i;
							eEyes2[1][1] = eBlock[1] - 2;
							eEyes2[2][0] = eBlock[0] - i;
							eEyes2[2][1] = eBlock[1] - 1;
							eEyes2[13][0] = eBlock[0] - i;
							eEyes2[13][1] = eBlock[1] + 1;
							eEyes2[14][0] = eBlock[0] - i;
							eEyes2[14][1] = eBlock[1] + 2;

							eEyes2[5][0] = eBlock[0] - 3;
							eEyes2[5][1] = eBlock[1] - 1;
							eEyes2[10][0] = eBlock[0] - 3;
							eEyes2[10][1] = eBlock[1];
							eEyes2[15][0] = eBlock[0] - 3;
							eEyes2[15][1] = eBlock[1] + 1;
							break;
						case 3:
							eEyes2[3][0] = eBlock[0] - i;
							eEyes2[3][1] = eBlock[1] - 3;
							eEyes2[4][0] = eBlock[0] - i;
							eEyes2[4][1] = eBlock[1] - 2;
							eEyes2[16][0] = eBlock[0] - i;
							eEyes2[16][1] = eBlock[1] + 2;
							eEyes2[17][0] = eBlock[0] - i;
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
					}
				}
				break;
			case Right:
				for (int i=0; i<=eEye1; i++) { // ip = 列
					switch (i) {
						case 0:
							eEyes2[8][0] = eBlock[0] + 1;
							eEyes2[8][1] = eBlock[1];
							break;
						case 1:
							eEyes2[0][0] = eBlock[0] + i;
							eEyes2[0][1] = eBlock[1] + i;
							eEyes2[12][0] = eBlock[0] + i;
							eEyes2[12][1] = eBlock[1] - i;
							eEyes2[9][0] = eBlock[0] + 2;
							eEyes2[9][1] = eBlock[1];
							break;
						case 2:
							eEyes2[1][0] = eBlock[0] + i;
							eEyes2[1][1] = eBlock[1] + 2;
							eEyes2[2][0] = eBlock[0] + i;
							eEyes2[2][1] = eBlock[1] + 1;
							eEyes2[13][0] = eBlock[0] + i;
							eEyes2[13][1] = eBlock[1] - 1;
							eEyes2[14][0] = eBlock[0] + i;
							eEyes2[14][1] = eBlock[1] - 2;

							eEyes2[5][0] = eBlock[0] + 3;
							eEyes2[5][1] = eBlock[1] + 1;
							eEyes2[10][0] = eBlock[0] + 3;
							eEyes2[10][1] = eBlock[1];
							eEyes2[15][0] = eBlock[0] + 3;
							eEyes2[15][1] = eBlock[1] - 1;
							break;
						case 3:
							eEyes2[3][0] = eBlock[0] + i;
							eEyes2[3][1] = eBlock[1] + 3;
							eEyes2[4][0] = eBlock[0] + i;
							eEyes2[4][1] = eBlock[1] + 2;
							eEyes2[16][0] = eBlock[0] + i;
							eEyes2[16][1] = eBlock[1] - 2;
							eEyes2[17][0] = eBlock[0] + i;
							eEyes2[17][1] = eBlock[1] - 3;

							eEyes2[6][0] = eBlock[0] + 4;
							eEyes2[6][1] = eBlock[1] + 2;
							eEyes2[7][0] = eBlock[0] + 4;
							eEyes2[7][1] = eBlock[1] + 1;
							eEyes2[11][0] = eBlock[0] + 4;
							eEyes2[11][1] = eBlock[1];
							eEyes2[18][0] = eBlock[0] + 4;
							eEyes2[18][1] = eBlock[1] - 1;
							eEyes2[19][0] = eBlock[0] + 4;
							eEyes2[19][1] = eBlock[1] - 2;
							break;
					}
				}
				break;
			case Top:
				// eEye1 == 敵の視界の距離 == i
				for (int i=0; i<=eEye1; i++) { // ip = 列
					switch (i) {
						case 0: // 正面１マスのみ
							eEyes2[8][0] = eBlock[0];
							eEyes2[8][1] = eBlock[1]-1;
							break;
						case 1:
							eEyes2[0][0] = eBlock[0]-1;
							eEyes2[0][1] = eBlock[1]-1;
							eEyes2[12][0] = eBlock[0]+1;
							eEyes2[12][1] = eBlock[1]-1;

							eEyes2[9][0] = eBlock[0];
							eEyes2[9][1] = eBlock[1]-2;
							break;
						case 2:
							eEyes2[1][0] = eBlock[0]-2;
							eEyes2[1][1] = eBlock[1]-2;
							eEyes2[2][0] = eBlock[0]-1;
							eEyes2[2][1] = eBlock[1]-2;
							eEyes2[13][0] = eBlock[0]+1;
							eEyes2[13][1] = eBlock[1]-2;
							eEyes2[14][0] = eBlock[0]+2;
							eEyes2[14][1] = eBlock[1]-2;

							eEyes2[5][0] = eBlock[0]-1;
							eEyes2[5][1] = eBlock[1]-3;
							eEyes2[10][0] = eBlock[0];
							eEyes2[10][1] = eBlock[1]-3;
							eEyes2[15][0] = eBlock[0]+1;
							eEyes2[15][1] = eBlock[1]-3;
							break;
						case 3:
							eEyes2[3][0] = eBlock[0]-3;
							eEyes2[3][1] = eBlock[1]-3;
							eEyes2[4][0] = eBlock[0]-2;
							eEyes2[4][1] = eBlock[1]-3;
							eEyes2[16][0] = eBlock[0]+2;
							eEyes2[16][1] = eBlock[1]-3;
							eEyes2[17][0] = eBlock[0]+3;
							eEyes2[17][1] = eBlock[1]-3;

							eEyes2[6][0] = eBlock[0]-2;
							eEyes2[6][1] = eBlock[1]-4;
							eEyes2[7][0] = eBlock[0]-1;
							eEyes2[7][1] = eBlock[1]-4;
							eEyes2[11][0] = eBlock[0];
							eEyes2[11][1] = eBlock[1]-4;
							eEyes2[18][0] = eBlock[0]+1;
							eEyes2[18][1] = eBlock[1]-4;
							eEyes2[19][0] = eBlock[0]+2;
							eEyes2[19][1] = eBlock[1]-4;
							break;
					}
				}
				break;
			case Under:
				// eEye1 == 敵の視界の距離 == i
				for (int i=0; i<=eEye1; i++) { // ip = 列
					switch (i) {
						case 0: // 正面１マスのみ
							eEyes2[8][0] = eBlock[0];
							eEyes2[8][1] = eBlock[1]+1;
							break;
						case 1:
							eEyes2[0][0] = eBlock[0]-1;
							eEyes2[0][1] = eBlock[1]+1;
							eEyes2[12][0] = eBlock[0]+1;
							eEyes2[12][1] = eBlock[1]+1;

							eEyes2[9][0] = eBlock[0];
							eEyes2[9][1] = eBlock[1]+2;
							break;
						case 2:
							eEyes2[1][0] = eBlock[0]-2;
							eEyes2[1][1] = eBlock[1]+2;
							eEyes2[2][0] = eBlock[0]-1;
							eEyes2[2][1] = eBlock[1]+2;
							eEyes2[13][0] = eBlock[0]+1;
							eEyes2[13][1] = eBlock[1]+2;
							eEyes2[14][0] = eBlock[0]+2;
							eEyes2[14][1] = eBlock[1]+2;

							eEyes2[5][0] = eBlock[0]-1;
							eEyes2[5][1] = eBlock[1]+3;
							eEyes2[10][0] = eBlock[0];
							eEyes2[10][1] = eBlock[1]+3;
							eEyes2[15][0] = eBlock[0]+1;
							eEyes2[15][1] = eBlock[1]+3;
							break;
						case 3:
							eEyes2[3][0] = eBlock[0]-3;
							eEyes2[3][1] = eBlock[1]+3;
							eEyes2[4][0] = eBlock[0]-2;
							eEyes2[4][1] = eBlock[1]+3;
							eEyes2[16][0] = eBlock[0]+2;
							eEyes2[16][1] = eBlock[1]+3;
							eEyes2[17][0] = eBlock[0]+3;
							eEyes2[17][1] = eBlock[1]+3;

							eEyes2[6][0] = eBlock[0]-2;
							eEyes2[6][1] = eBlock[1]+4;
							eEyes2[7][0] = eBlock[0]-1;
							eEyes2[7][1] = eBlock[1]+4;
							eEyes2[11][0] = eBlock[0];
							eEyes2[11][1] = eBlock[1]+4;
							eEyes2[18][0] = eBlock[0]+1;
							eEyes2[18][1] = eBlock[1]+4;
							eEyes2[19][0] = eBlock[0]+2;
							eEyes2[19][1] = eBlock[1]+4;
							break;
					}
				}				break;
		}
	}
}
