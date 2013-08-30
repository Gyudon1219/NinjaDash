package com.gyu.study;

import java.io.IOException;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.handler.IUpdateHandler;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.FadeInModifier;
import org.andengine.entity.modifier.FadeOutModifier;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.ease.EaseQuadOut;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Address;
import android.util.Log;
import android.view.KeyEvent;

public class MainScene extends KeyListenScene implements IOnSceneTouchListener, 
	ButtonSprite.OnClickListener {
	
	//ボタン用タグ
	private static final int MENU_MENU = 1;
	private static final int MENU_TWEET = 2;
	private static final int MENU_RANKING = 3;
	private static final int MENU_RETRY = 4;
	private static final int MENU_RESUME = 5;
	
	//障害物用タグ
	private static final int TAG_OBSTACLE_TRAP = 1;
	private static final int TAG_OBSTACLE_FIRE = 2;
	private static final int TAG_OBSTACLE_ENEMY = 3;
	private static final int TAG_OBSTACLE_EAGLE = 4;
	private static final int TAG_OBSTACLE_HEART = 5;
	
	//死体用タグ
	private static final int TAG_DEAD_BODY = 11;
	
	//草
	private Sprite grass01;
	private Sprite grass02;
	
	//スクロールのスピード
	private float scrollSpeed;
	
	//忍者
	private AnimatedSprite ninja;
	
	//タッチ可否
	private boolean isTouchEnabled;
	
	//ドラッグ開始座標
	private float[] touchStartPoint;
	
	//攻撃中か否か
	private boolean isAttacking;
	
	//ジャンプ中か否か
	private boolean isJumping;
	
	//スライディング中か否か
	private boolean isSliding;
	
	//死んだ後の回復中か否か
	private boolean isRecovering;
	
	//画面外へ移動した障害物を除去する為に利用する配列
	private ArrayList<Sprite> spriteOutOfBoundsArray;
	
	//遊び方画面
	private Sprite instructionSprite;
	
	//遊び方画面のボタン
	private ButtonSprite instructionBtn;
	
	//ポーズ中か否か
	private boolean isPaused;
	
	//ポーズ画面の背景
	private Rectangle pauseBg;
	
	//ゲームオーバー済みか否か
	private boolean isGameOver;
	
	//登録済みアップデートハンドラを格納する配列
	private ArrayList<CustomTimerHandler> updateHandlerArray;
	
	//サウンド
	private Sound ninjaAttackSound;
	private Sound enemyAttackSound;
	private Sound ninjaBurnSound;
	private Sound recoverySound;
	private Sound gameoverSound;
	private Sound btnPressedSound;
	
	//残りライフ
	private int life;
	
	//ライフ表示Spriteの配列
	private ArrayList<Sprite> lifeSpriteArray;
	
	//現在のスコアを表示するテキスト
	private Text currentScoreText;
	
	//過去最高のスコアを表示するテキスト
	private Text highSocreText;
	
	//現在のスコア
	private int currentScore;
	
	//結果画面用z-index
	private int zIndexResult = 1;
	
	public MainScene(MultiSceneActivity baseActivity) {
		super(baseActivity);
		init();
	}

	public void init(){
		attachChild(getBaseActivity().getResourceUtil().getSprite("main_bg.png"));
		
		//スクロールのスピードの初期値を6に
		scrollSpeed = 6;
		
		//1つめの草
		grass01 = getBaseActivity().getResourceUtil().getSprite("main_grass.png");
		
		grass01.setPosition(0,420);
		attachChild(grass01);
		
		//2つめの草
		grass02 = getBaseActivity().getResourceUtil().getSprite("main_grass.png");
		
		grass02.setPosition(getBaseActivity().getEngine().getCamera().getWidth(), 420);
		attachChild(grass02);
		
		//忍者を追加
		ninja = getBaseActivity().getResourceUtil().getAnimatedSprite("main_ninja.png", 1, 6);
		
		//0コマ目、1コマ目のみをアニメーション
		ninja.animate(new long[]{100,100},0,1,true);
		ninja.setPosition(80,325);
		attachChild(ninja);
		
		//スコア初期値を0に
		currentScore = 0;
		
		//端末に搭載されたフォントを利用して得点を表示
		//フォント用のTextureを用意
		Texture texture = new BitmapTextureAtlas(getBaseActivity().getTextureManager(), 512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		//フォントをイニシャライズ
		Font font = new Font(getBaseActivity().getFontManager(), texture, Typeface.DEFAULT_BOLD, 22, true, Color.YELLOW);
		
		//EngineのTextureManagerにフォントTextureを読み込み
		getBaseActivity().getTextureManager().loadTexture(texture);
		//FontManagerにフォントを読み込み
		getBaseActivity().getFontManager().loadFont(font);
		
		//読み込んだフォントをリ用して得点を表示
		currentScoreText = new Text(20,20,font,"得点：" + currentScore, 20, 
				new TextOptions(HorizontalAlign.LEFT), getBaseActivity().getVertexBufferObjectManager());
		
		attachChild(currentScoreText);
		highSocreText = new Text(20, 50, font, "ハイスコア:" + SPUtil.getInstance(getBaseActivity()).getHighScore(),20,
				new TextOptions(HorizontalAlign.LEFT), getBaseActivity().getVertexBufferObjectManager());
		
		attachChild(highSocreText);
		
		touchStartPoint = new float[2];
		isTouchEnabled = true;
		isJumping = false;
		isSliding = false;
		isAttacking = false;
		isRecovering = false;
		isPaused = false;
		isGameOver = false;
		
		//ライフの初期値を3とし、画面左上にSpriteを表示
		lifeSpriteArray = new ArrayList<Sprite>();
		life = 3;
		for(int i = 0; i < life; i++) {
			Sprite heart = getBaseActivity().getResourceUtil().getSprite("main_heart.png");
			//小さめにスケール
			heart.setScale(0.6f);
			heart.setPosition(10 + 45 * i, 90);
			attachChild(heart);
			lifeSpriteArray.add(heart);
		}
		
		spriteOutOfBoundsArray = new ArrayList<Sprite>();
		updateHandlerArray = new ArrayList<CustomTimerHandler>();
		
		//ハイスコアが500以下の時(初プレイ時)のみヘルプ画面を出す
		if(SPUtil.getInstance(getBaseActivity()).getHighScore() > 500) {
		
		//1秒間に60回、updateHandlerを呼び出し
		registerUpdateHandler(updateHandler);
		//1秒毎に障害物出現関数を呼び出し
		registerUpdateHandler(obstacleAppearHandler);
		//Sceneのタッチリスナーを登録
		setOnSceneTouchListener(this);

		} else {
			showHelp();
		}
	}
	
	//遊び方画面を出現させる
	public void showHelp() {
		instructionSprite = ResourceUtil.getInstance(getBaseActivity())
				.getSprite("instruction.png");
		placeToCenter(instructionSprite);
		attachChild(instructionSprite);
		
		//ボタン
		instructionBtn = ResourceUtil.getInstance(getBaseActivity())
				.getButtonSprite("instruction_btn.png", "instruction_btn_p.png");
		placeToCenterX(instructionBtn, 380);
		attachChild(instructionBtn);
		registerTouchArea(instructionBtn);
		instructionBtn.setOnClickListener(new ButtonSprite.OnClickListener() {
			public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
					float pTouchAreaLocalY) {
				instructionSprite.detachSelf();
				instructionBtn.detachSelf();
				unregisterTouchArea(instructionBtn);
				
				//1秒間に60回、updateHandlerを呼び出し
				registerUpdateHandler(updateHandler);
				//1秒間に障害物出現関数を呼び出し
				registerUpdateHandler(obstacleAppearHandler);
				//Sceneのタッチリスナーを登録
				setOnSceneTouchListener(MainScene.this);
			}
		});
	}

	@Override
	public void prepareSoundAndMusic(){
		try {
			ninjaAttackSound = SoundFactory.createSoundFromAsset(getBaseActivity().getSoundManager(), getBaseActivity(), "saku02.wav");
			enemyAttackSound = SoundFactory.createSoundFromAsset(getBaseActivity().getSoundManager(), getBaseActivity(), "fall00.wav");
			ninjaBurnSound = SoundFactory.createSoundFromAsset(getBaseActivity().getSoundManager(), getBaseActivity(), "fire01.wav");
			recoverySound = SoundFactory.createSoundFromAsset(getBaseActivity().getSoundManager(), getBaseActivity(), "pi30.wav");
			gameoverSound = SoundFactory.createSoundFromAsset(getBaseActivity().getSoundManager(), getBaseActivity(), "bell14.wav");
			btnPressedSound = SoundFactory.createSoundFromAsset(getBaseActivity().getSoundManager(), getBaseActivity(), "clock00.wav");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e){
		if(e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			//ゲームオーバー済みなら何もしない
			if(isGameOver) {
				return false;
			}
			//ポーズ中ならポーズ画面を消去
			if(isPaused) {
				//detach系のメソッドは別スレッドで
				getBaseActivity().runOnUpdateThread(new Runnable() {
					public void run() {
						for(int i = 0; i < pauseBg.getChildCount(); i++) {
							//忘れずにタッチの検知を無効に
							unregisterTouchArea((ButtonSprite) pauseBg.getChildByIndex(i));
						}
						pauseBg.detachChildren();
						pauseBg.detachSelf();
					}
				});
				resumeGame();
				isPaused = false;
				return true;
			} else {
				return false;
			}
		} else if(e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			//ポーズ中でなければポーズ画面を出す
			if(!isPaused) {
				showMenu();
			}
			return true;
		}
		return false;
	}
	
	//メニューを出す
	public void showMenu() {
		if(isGameOver) {
			return;
		}
		pauseGame();
		//四角形を描画
		pauseBg = new Rectangle(0, 0, getBaseActivity().getEngine().getCamera().getWidth(), 
				getBaseActivity().getEngine().getCamera().getHeight(),
				getBaseActivity().getVertexBufferObjectManager());
		pauseBg.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		pauseBg.setColor(0, 0, 0);
		pauseBg.setAlpha(0.7f);
		attachChild(pauseBg);
		
		try{
			ButtonSprite btnMenu = getBaseActivity().getResourceUtil().getButtonSprite("menu_btn_05.png", "menu_btn_05_p.png");
			placeToCenterX(btnMenu, 100);
			btnMenu.setTag(MENU_RESUME);
			btnMenu.setOnClickListener(this);
			pauseBg.attachChild(btnMenu);
			registerTouchArea(btnMenu);
			
			ButtonSprite btnTweet = getBaseActivity().getResourceUtil().getButtonSprite("menu_btn_02.png", "menu_btn_02_p.png");
			placeToCenterX(btnTweet, 220);
			btnTweet.setTag(MENU_RETRY);
			btnTweet.setOnClickListener(this);
			pauseBg.attachChild(btnTweet);
			registerTouchArea(btnTweet);
			
			ButtonSprite btnRanking = getBaseActivity().getResourceUtil().getButtonSprite("menu_btn_04.png", "menu_btn_04_p.png");
			placeToCenterX(btnRanking, 340);
			btnRanking.setTag(MENU_MENU);
			btnRanking.setOnClickListener(this);
			pauseBg.attachChild(btnRanking);
			registerTouchArea(btnRanking);
		} catch(Exception e) {
			e.printStackTrace();
		}
		isPaused = true;
	}

	//ゲームを一時停止
	public void pauseGame() {
		//全てのAnimatedSpriteのアニメーションをストップ
		for(int i = 0; i < getChildCount(); i++) {
			if(getChildByIndex(i) instanceof AnimatedSprite) {
				((AnimatedSprite)getChildByIndex(i)).stopAnimation();
			}
		}
//		//全てのUpdateHandlerをストップ
//		unregisterUpdateHandlers(new IUpdateHandlerMatcher() {
//			public boolean matches(IUpdateHandler pObject) {
//				return true;
//			}
//		});
		unregisterUpdateHandler(updateHandler);
		unregisterUpdateHandler(obstacleAppearHandler);
		
		for(CustomTimerHandler handler : updateHandlerArray) {
			handler.pause();
		}
	}

	//一時停止したゲームを再開
	public void resumeGame() {
		//ストップしUpdateHandlerを再度登録
		registerUpdateHandler(updateHandler);
		registerUpdateHandler(obstacleAppearHandler);

		//AnimatedSpriteのアニメを再開
		for(int i = 0; i < getChildCount(); i++) {
			if(getChildByIndex(i).getTag() == TAG_OBSTACLE_EAGLE) {
				((AnimatedSprite)getChildByIndex(i)).animate(200);
			} else if(getChildByIndex(i).getTag() == TAG_OBSTACLE_ENEMY) {
				((AnimatedSprite)getChildByIndex(i)).animate(100);
			}
		}
		if(!isJumping && !isSliding) {
			ninja.animate(new long[] {100, 100}, 0, 1, true);
		}
		
		for(CustomTimerHandler handler : updateHandlerArray) {
			handler.resume();
		}
	}

	//アップデートハンドラ。1秒間に60回呼び出される
	public TimerHandler updateHandler = new TimerHandler(1f/60f, true, new ITimerCallback() {
		public void onTimePassed(TimerHandler pTimerHandler) {
			//スコアの増加とセット
			if(!isRecovering) {
				//スコアをインクリメント
				currentScore++;
				//スコアをセット
				currentScoreText.setText("得点:" + currentScore);
			}
			
			//草をscrollSpeedの分移動させる
			grass01.setX(grass01.getX() - scrollSpeed);
			if(grass01.getX() <= -getBaseActivity().getEngine().getCamera().getWidth()) {
				//草の右端が画面左端より左に移動した場合は画面幅*2分右へ移動
				grass01.setX(grass01.getX() + getBaseActivity().getEngine().getCamera().getWidth()*2);
			}
			grass02.setX(grass02.getX() - scrollSpeed);
			if(grass02.getX() <= -getBaseActivity().getEngine().getCamera().getWidth()) {
				grass02.setX(grass02.getX() + getBaseActivity().getEngine().getCamera().getWidth()*2);
			}
			
			//障害物を移動
			for(int i= 0; i < getChildCount(); i++) {
				if(getChildByIndex(i).getTag() == TAG_OBSTACLE_TRAP
						|| getChildByIndex(i).getTag() == TAG_OBSTACLE_FIRE
						|| getChildByIndex(i).getTag() == TAG_OBSTACLE_ENEMY
						|| getChildByIndex(i).getTag() == TAG_OBSTACLE_EAGLE
						|| getChildByIndex(i).getTag() == TAG_DEAD_BODY
						|| getChildByIndex(i).getTag() == TAG_OBSTACLE_HEART) {
					//鷹のみ忍者の上空から滑空させる
					if(getChildByIndex(i).getTag() == TAG_OBSTACLE_EAGLE) {
						if(getChildByIndex(i).getX() < 300) {
							getChildByIndex(i).setY(getChildByIndex(i).getY() + 10);
						}
					}
					getChildByIndex(i).setPosition(
							getChildByIndex(i).getX() - scrollSpeed,
							getChildByIndex(i).getY());
					if(getChildByIndex(i).getX()
							+ ((Sprite)getChildByIndex(i)).getWidth() < 0){
						//すぐに削除するとインデックスがずれるため
						//一旦配列に追加
						spriteOutOfBoundsArray.add((Sprite)getChildByIndex(i));
					}
					
					//回復中は無視
					if(!isRecovering) {
						//Sprite同士が衝突している時のみ
						//高度な衝突判定へ
						if(((Sprite)getChildByIndex(i)).collidesWith(ninja)){
							//忍者のx座標中心と
							//障害物のx座標中心間の距離
							float distanceBetweenCenterXOfNinjaAndObstacle = Math.abs((getChildByIndex(i).getX()
									+((Sprite)getChildByIndex(i)).getWidth()/2) -(ninja.getX() + (ninja.getWidth()/2)));
							
							//衝突を許容する距離
							float allowableDistance = 0;
							if(getChildByIndex(i).getTag() == TAG_OBSTACLE_TRAP){
								allowableDistance = 50;
								if(distanceBetweenCenterXOfNinjaAndObstacle < ninja.getWidth()/2
										+ ((Sprite)getChildByIndex(i)).getWidth() /2 -allowableDistance) {
									//敵の攻撃
									enemyAttack(getChildByIndex(i).getTag());
								}
							} else if(getChildByIndex(i).getTag() == TAG_OBSTACLE_FIRE) {
								allowableDistance = 40;
								if(distanceBetweenCenterXOfNinjaAndObstacle < ninja.getWidth() /2
										+ ((Sprite)getChildByIndex(i)).getWidth() /2 - allowableDistance && !isSliding) {
									enemyAttack(getChildByIndex(i).getTag());
								}
							} else if(getChildByIndex(i).getTag() == TAG_OBSTACLE_ENEMY){
								allowableDistance = 80;
								if(distanceBetweenCenterXOfNinjaAndObstacle < ninja.getWidth() /2
										+ ((Sprite)getChildByIndex(i)).getWidth() /2 - allowableDistance && !isJumping) {
									enemyAttack(getChildByIndex(i).getTag());
								}
							} else if(getChildByIndex(i).getTag() == TAG_OBSTACLE_EAGLE) {
								allowableDistance = 70;
								if(distanceBetweenCenterXOfNinjaAndObstacle < ninja.getWidth() / 2
										+ ((Sprite)getChildByIndex(i)).getWidth() /2 - allowableDistance) {
									enemyAttack(getChildByIndex(i).getTag());
								}
							} else if(getChildByIndex(i).getTag() == TAG_OBSTACLE_HEART) {
								spriteOutOfBoundsArray.add((Sprite)getChildByIndex(i));
								addLife();
							}
						}
					}
				}
			}
			//配列の中身を削除
			for(Sprite sp : spriteOutOfBoundsArray) {
				sp.detachSelf();
			}
			
			//残りライフが3未満かつステージ上に回復アイテムが無い時
			if(life < 3 && getChildByTag(TAG_OBSTACLE_HEART) == null) {
				//500分の1の確立で回復アイテムを出現
				if((int)(Math.random()*500) == 1) {
					Sprite obstacle = getBaseActivity().getResourceUtil().getSprite("main_heart.png");
					//画面右側に追加。y座標はランダム
					obstacle.setPosition(getBaseActivity().getEngine().getCamera().getWidth()
							+ obstacle.getWidth(), 350 - (int)(Math.random() * 200));
					obstacle.setTag(TAG_OBSTACLE_HEART);
					attachChild(obstacle);
				}
			}
		}

	});
	
	//1秒毎に画面右外に障害物を出現させる関数
	private TimerHandler obstacleAppearHandler = new TimerHandler(1, true, new ITimerCallback(){
		public void onTimePassed(TimerHandler pTimerHandler) {
			Sprite obstacle = null;
			AnimatedSprite animatedObstacle = null;
			//敵の種類をランダムに選択
			int r = (int)(Math.random()*4);
			switch(r){
			case 0:
				//竹でできた罠
				obstacle = getBaseActivity().getResourceUtil().getSprite("main_trap.png");
				obstacle.setPosition(getBaseActivity().getEngine().getCamera().getWidth() + obstacle.getWidth(), 380);
				obstacle.setTag(TAG_OBSTACLE_TRAP);
				attachChild(obstacle);
				break;
			case 1:
				//火の玉
				obstacle = getBaseActivity().getResourceUtil().getSprite("main_fire.png");
				obstacle.setPosition(getBaseActivity().getEngine().getCamera().getWidth() + obstacle.getWidth(), 260);
				obstacle.setTag(TAG_OBSTACLE_FIRE);
				attachChild(obstacle);
				break;
			case 2:
				//敵忍者
				animatedObstacle = getBaseActivity().getResourceUtil().getAnimatedSprite("main_enemy.png",1,2);
				animatedObstacle.setPosition(getBaseActivity().getEngine().getCamera().getWidth() + animatedObstacle.getWidth(), 325);
				animatedObstacle.setTag(TAG_OBSTACLE_ENEMY);
				attachChild(animatedObstacle);
				animatedObstacle.animate(100);
				break;
			case 3:
				//鷹
				animatedObstacle = getBaseActivity().getResourceUtil().getAnimatedSprite("main_eagle.png",1,2);
				animatedObstacle.setPosition(getBaseActivity().getEngine().getCamera().getWidth() + animatedObstacle.getWidth(), 30);
				animatedObstacle.setTag(TAG_OBSTACLE_ENEMY);
				attachChild(animatedObstacle);
				animatedObstacle.animate(200);
				break;
			}
			sortChildren();
		}
	});

	@Override
	//タッチイベントが発生したら呼ばれる
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		
		//タッチの座標を取得
		float x = pSceneTouchEvent.getX();
		float y = pSceneTouchEvent.getY();
		
		//攻撃
		if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			//タップが画面中心より右側なら
			if(x > getBaseActivity().getEngine().getCamera().getWidth() /2 ){
				//スライディング中ではなく、攻撃中でもなければ攻撃
				if(!isSliding && !isAttacking) {
					//攻撃
					fireWeapon();
				}
				return true;
			}
		}		
		
		if(!isTouchEnabled) {
			return true;
		}
		
		if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			//開始点を登録
			touchStartPoint[0] = x;
			touchStartPoint[1] = y;
		} else if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP
				|| pSceneTouchEvent.getAction() == TouchEvent.ACTION_CANCEL) {
			float[] touchEndPoint = new float[2];
			touchEndPoint[0] = x;
			touchEndPoint[1] = y;
			
			//フリックの距離が短すぎる時にはフリックと判定しない
			if(Math.abs(touchEndPoint[0] - touchStartPoint[0]) < 50
					&& Math.abs(touchEndPoint[1] - touchStartPoint[1]) < 50 ) {
				return true;
			}
			
			//フリックの角度を求める
			double angle = getAngleByTwoPosition(touchStartPoint, touchEndPoint);
			//下から上のフリックを0°に調整(★ここのロジックが若干変。要デバッグ)
			angle -= 180;
			if(angle > -45 && angle < 45){
				jumpSprite();
			} else if( angle > 135 && angle < 225) {
				slideSprite();
			}
		}
		return true;
	}

	//忍者をジャンプさせる
	private void jumpSprite() {
		//ジャンプ中はタップは受け付けない
		isTouchEnabled = false;
		isJumping = true;
		ninja.stopAnimation();
		//忍者の画像を2に変更(0から始まるので上から3番目)
		ninja.setCurrentTileIndex(2);
		ninja.setPosition(ninja.getX(), ninja.getY() - 100);
		
//		registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback(){
//			public void onTimePassed(TimerHandler pTimeHandler) {
//				//元夫に戻す
//				setSpriteToDefaultPosition();
//			}
//		}));
		
		registerUpdateHandler(setSpriteToDefaultPositionHander);
		updateHandlerArray.add(setSpriteToDefaultPositionHander);
	}

	//忍者をスライディングさせる
	private void slideSprite() {
		//スライディング中はタップは受け付けない
		isTouchEnabled = false;
		isSliding = true;
		ninja.stopAnimation();
		//忍者の画像を3に変更(0から始まるので上から4番目)
		ninja.setCurrentTileIndex(3);
		
		registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback() {
			public void onTimePassed(TimerHandler pTimerHandler){
				//元に戻す
				setSpriteToDefaultPosition();
			}
		}));
	}

	//忍者の座標と動きを通常に戻す
	private void setSpriteToDefaultPosition() {
		isJumping = false;
		isSliding = false;
		isTouchEnabled = true;
		ninja.animate(new long[]{200,200},0,1,true);
		ninja.setPosition(80,325);
	}
	
	public CustomTimerHandler setSpriteToDefaultPositionHander = new CustomTimerHandler(0.5f, new ITimerCallback() {
		public void onTimePassed(TimerHandler pTimerHandler) {
			isJumping = false;
			isSliding = false;
			isTouchEnabled = true;
			ninja.animate(new long[]{200,200},0,1,true);
			ninja.setPosition(80,325);
			
			setSpriteToDefaultPositionHander.reset();
			unregisterUpdateHandler(setSpriteToDefaultPositionHander);
			updateHandlerArray.remove(setSpriteToDefaultPositionHander);
		}
	});
	
	public CustomTimerHandler startNinjaAnimationAfterDeathHandler = new CustomTimerHandler(1.0f, new ITimerCallback() {
		public void onTimePassed(TimerHandler pTimerHandler) {
				setSpriteToDefaultPosition();
				ninja.registerEntityModifier(new LoopEntityModifier(
						new SequenceEntityModifier(new FadeOutModifier(0.25f),new FadeInModifier(0.25f)), 4));
				
			registerUpdateHandler(finishRecoveringHandler);
			updateHandlerArray.add(finishRecoveringHandler);
			
			startNinjaAnimationAfterDeathHandler.reset();
			unregisterUpdateHandler(startNinjaAnimationAfterDeathHandler);
			updateHandlerArray.remove(startNinjaAnimationAfterDeathHandler);
			}
	});
	
	public CustomTimerHandler finishRecoveringHandler = new CustomTimerHandler(2.0f, new ITimerCallback() {
		public void onTimePassed(TimerHandler pTimerHandler) {
			isRecovering = false;
			finishRecoveringHandler.reset();
			unregisterUpdateHandler(finishRecoveringHandler);
			updateHandlerArray.remove(finishRecoveringHandler);
		}
	});

	//攻撃
	private void fireWeapon() {
		//攻撃の連射を防ぐ
		isAttacking = true;
		ninja.stopAnimation();
		final Sprite weapon = getBaseActivity().getResourceUtil().getSprite("main_weapon.png");
		if(!isJumping){
			weapon.setPosition(130,27);
			ninja.setCurrentTileIndex(5);
		} else {
			weapon.setPosition(130,10);
			ninja.setCurrentTileIndex(4);
		}
		ninja.attachChild(weapon);
		
		//削除するSpriteを一旦格納する配列
		ArrayList<AnimatedSprite> spToRemoveArray = new ArrayList<AnimatedSprite>();
		
		for(int i = 0; i < getChildCount(); i++) {
			//攻撃で倒せるのは敵忍者と鷹のみ
			if(getChildByIndex(i).getTag() == TAG_OBSTACLE_ENEMY || 
					getChildByIndex(i).getTag() == TAG_OBSTACLE_EAGLE ) {
				//衝突判定
				if(((AnimatedSprite)getChildByIndex(i)).collidesWith(weapon)) {
					//効果音を再生
					ninjaAttackSound.play();
					spToRemoveArray.add((AnimatedSprite)getChildByIndex(i));
				}
			}
		}
		
		//削除
		for(AnimatedSprite sp : spToRemoveArray) {
			sp.detachSelf();
		}
		
		weapon.registerEntityModifier(new FadeOutModifier(0.5f));
		
		registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback() {
			public void onTimePassed(TimerHandler pTimerHandler) {
				weapon.detachSelf();
				isAttacking = false;
				setSpriteToDefaultPosition();
			}
		}));
	}
	
	//敵の攻撃
	public void enemyAttack(int enemyTag){
		ninja.setAlpha(0);
		Sprite deadBody = null;
		//火の玉に当たった時は黒こげSpriteに、他はズッコケSpriteに変更
		if(enemyTag == TAG_OBSTACLE_FIRE){
			deadBody = getBaseActivity().getResourceUtil().getSprite("main_ninja_burn.png");
			//効果音を再生
			ninjaBurnSound.play();
		} else {
			deadBody = getBaseActivity().getResourceUtil().getSprite("main_ninja_dead.png");
			//効果音を再生
			enemyAttackSound.play();
		}
		deadBody.setTag(TAG_DEAD_BODY);
		deadBody.setPosition(ninja);
		attachChild(deadBody);
		
		//回復後は一定時間無敵状態に
		isRecovering = true;
		
		//ライフを減らす
		lifeSpriteArray.get(life-1).detachSelf();
		life--;
		
		//ライフが0ならゲームオーバー
		if(life == 0){
//			Log.d("ae", "gameover");
			showGameOver();
		}
		else {
		
//			TimerHandler timerHandler = new TimerHandler(1.0f,
//					new ITimerCallback() {
//						public void onTimePassed(TimerHandler pTimerHandler) {
//							setSpriteToDefaultPosition();
//							ninja.registerEntityModifier(new LoopEntityModifier(
//									new SequenceEntityModifier(new FadeOutModifier(0.25f),
//											new FadeInModifier(0.25f)),4));
//							
//							//無敵状態を解除
//							TimerHandler timerHandler = new TimerHandler(2.0f, 
//									new ITimerCallback() {
//										public void onTimePassed(TimerHandler pTimerHandler) {
//											isRecovering = false;
//										}
//									});
//							registerUpdateHandler(timerHandler);
//						}
//					});
//			registerUpdateHandler(timerHandler);
			registerUpdateHandler(startNinjaAnimationAfterDeathHandler);
			updateHandlerArray.add(startNinjaAnimationAfterDeathHandler);
		}
	}

	//ゲームオーバー
	private void showGameOver() {
		
		//効果音を再生
		gameoverSound.play();
		
		//Sceneのタッチリスナーを解除
		setOnSceneTouchListener(null);
		
		//ハイスコア更新時は保存
		if(currentScore > SPUtil.getInstance(getBaseActivity()).getHighScore()) {
			SPUtil.getInstance(getBaseActivity()).setHighSocre(currentScore);
		}
		
		//透明な背景を作成
		Rectangle resultBg = new Rectangle(getBaseActivity().getEngine()
				.getCamera().getWidth(), 0 , getBaseActivity().getEngine()
				.getCamera().getWidth(), getBaseActivity().getEngine()
				.getCamera().getHeight(), getBaseActivity()
				.getVertexBufferObjectManager());
		
		//透明に
		resultBg.setColor(Color.TRANSPARENT);
		
		//敵キャラクターより前面に表示
		resultBg.setZIndex(zIndexResult);
		attachChild(resultBg);
		sortChildren();
		
		//ビットマップフォントを作成
		BitmapFont bitmapFont = new BitmapFont(getBaseActivity()
				.getTextureManager(), getBaseActivity().getAssets(), 
				"font/result.fnt");
		bitmapFont.load();
		
		//ビットマップフォントを元にスコアを表示
		Text resultText = new Text(0, 0, bitmapFont, 
				""+currentScore+"pts", 20, new TextOptions(
						HorizontalAlign.CENTER), getBaseActivity()
						.getVertexBufferObjectManager());
		
		resultText.setPosition(getBaseActivity().getEngine().getCamera()
				.getWidth() / 2.0f - resultText.getWidth() / 2.0f, 60);
		resultBg.attachChild(resultText);
		
		//各ボタン
		ButtonSprite btnRanking = getBaseActivity().getResourceUtil()
				.getButtonSprite("menu_btn_01.png", "menu_btn_01_p.png");
		placeToCenterX(btnRanking, 145);
		btnRanking.setTag(MENU_RANKING);
		btnRanking.setOnClickListener(this);
		resultBg.attachChild(btnRanking);
		registerTouchArea(btnRanking);
		
		ButtonSprite btnRetry = getBaseActivity().getResourceUtil()
				.getButtonSprite("menu_btn_02.png", "menu_btn_02_p.png");
		placeToCenterX(btnRetry, 260);
		btnRetry.setTag(MENU_RETRY);
		btnRetry.setOnClickListener(this);
		resultBg.attachChild(btnRetry);
		registerTouchArea(btnRetry);
		
		ButtonSprite btnTweet = getBaseActivity().getResourceUtil()
				.getButtonSprite("menu_btn_03.png", "menu_btn_03_p.png");
		placeToCenterX(btnTweet, 330);
		btnTweet.setTag(MENU_TWEET);
		btnTweet.setOnClickListener(this);
		resultBg.attachChild(btnTweet);
		registerTouchArea(btnTweet);

		ButtonSprite btnMenu = getBaseActivity().getResourceUtil()
				.getButtonSprite("menu_btn_04.png", "menu_btn_04_p.png");
		placeToCenterX(btnMenu, 400);
		btnMenu.setTag(MENU_MENU);
		btnMenu.setOnClickListener(this);
		resultBg.attachChild(btnMenu);
		registerTouchArea(btnMenu);
		
		//ゲームオーバー画面を右側からスライドイン
		resultBg.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(1.0f), new MoveModifier(1.0f, resultBg.getX(), resultBg.getX()
						- getBaseActivity().getEngine().getCamera()
						.getWidth(), resultBg.getY(), resultBg.getY(), EaseQuadOut.getInstance())));
		
	}

	public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		//効果音を再生
		btnPressedSound.play();
		switch(pButtonSprite.getTag()) {
		case MENU_RESUME:
			//detachChildrenとdetachSelfを同じタイミングで呼ぶ時は別スレッドで
			getBaseActivity().runOnUpdateThread(new Runnable() {
				public void run() {
					for(int i = 0; i < pauseBg.getChildCount(); i++ ) {
						//忘れずにタッチの検知を無効に
						unregisterTouchArea((ButtonSprite) pauseBg.getChildByIndex(i));
					}
					pauseBg.detachChildren();
					pauseBg.detachSelf();
				}
			});
			resumeGame();
			isPaused = false;
			break;
		case MENU_RETRY:
			MainScene newScene = new MainScene(getBaseActivity());
			getBaseActivity().refreshRunningScene(newScene);
			break;
		case MENU_MENU:
			getBaseActivity().backToInitial();
			break;
		case MENU_TWEET:
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
			sendIntent.putExtra(Intent.EXTRA_TEXT, "Androidゲーム「にんじゃDASH！！」で"
					+ currentScore + "点獲得!! → "
					+ "http://bit.ly/SrpS46");
			getBaseActivity().startActivity(sendIntent);
			break;
		case MENU_RANKING:
			break;
		}
	}

	//回復
	private void addLife() {
		if(life < 3) {
			//効果音を再生
			recoverySound.play();
			life++;
			attachChild(lifeSpriteArray.get(life -1));
			lifeSpriteArray.get(life-1).registerEntityModifier(new LoopEntityModifier(
					new SequenceEntityModifier(
							new ScaleModifier(0.25f, 0.6f, 1.3f), new ScaleModifier(0.25f, 1.3f,0.6f)),4));
		}
		
	}
	
	//2点間の角度を求める公式
	private double getAngleByTwoPosition(float[] start, float[] end) {
		double result = 0;
		
		float xDistance = end[0] - start[0];
		float yDistance = end[1] - start[1];
		
		result = Math.atan2((double)yDistance, (double)xDistance) * 180 / Math.PI;
		result += 270;
		
		return result;
	}


}
