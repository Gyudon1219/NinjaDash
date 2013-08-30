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
	
	//�{�^���p�^�O
	private static final int MENU_MENU = 1;
	private static final int MENU_TWEET = 2;
	private static final int MENU_RANKING = 3;
	private static final int MENU_RETRY = 4;
	private static final int MENU_RESUME = 5;
	
	//��Q���p�^�O
	private static final int TAG_OBSTACLE_TRAP = 1;
	private static final int TAG_OBSTACLE_FIRE = 2;
	private static final int TAG_OBSTACLE_ENEMY = 3;
	private static final int TAG_OBSTACLE_EAGLE = 4;
	private static final int TAG_OBSTACLE_HEART = 5;
	
	//���̗p�^�O
	private static final int TAG_DEAD_BODY = 11;
	
	//��
	private Sprite grass01;
	private Sprite grass02;
	
	//�X�N���[���̃X�s�[�h
	private float scrollSpeed;
	
	//�E��
	private AnimatedSprite ninja;
	
	//�^�b�`��
	private boolean isTouchEnabled;
	
	//�h���b�O�J�n���W
	private float[] touchStartPoint;
	
	//�U�������ۂ�
	private boolean isAttacking;
	
	//�W�����v�����ۂ�
	private boolean isJumping;
	
	//�X���C�f�B���O�����ۂ�
	private boolean isSliding;
	
	//���񂾌�̉񕜒����ۂ�
	private boolean isRecovering;
	
	//��ʊO�ֈړ�������Q������������ׂɗ��p����z��
	private ArrayList<Sprite> spriteOutOfBoundsArray;
	
	//�V�ѕ����
	private Sprite instructionSprite;
	
	//�V�ѕ���ʂ̃{�^��
	private ButtonSprite instructionBtn;
	
	//�|�[�Y�����ۂ�
	private boolean isPaused;
	
	//�|�[�Y��ʂ̔w�i
	private Rectangle pauseBg;
	
	//�Q�[���I�[�o�[�ς݂��ۂ�
	private boolean isGameOver;
	
	//�o�^�ς݃A�b�v�f�[�g�n���h�����i�[����z��
	private ArrayList<CustomTimerHandler> updateHandlerArray;
	
	//�T�E���h
	private Sound ninjaAttackSound;
	private Sound enemyAttackSound;
	private Sound ninjaBurnSound;
	private Sound recoverySound;
	private Sound gameoverSound;
	private Sound btnPressedSound;
	
	//�c�胉�C�t
	private int life;
	
	//���C�t�\��Sprite�̔z��
	private ArrayList<Sprite> lifeSpriteArray;
	
	//���݂̃X�R�A��\������e�L�X�g
	private Text currentScoreText;
	
	//�ߋ��ō��̃X�R�A��\������e�L�X�g
	private Text highSocreText;
	
	//���݂̃X�R�A
	private int currentScore;
	
	//���ʉ�ʗpz-index
	private int zIndexResult = 1;
	
	public MainScene(MultiSceneActivity baseActivity) {
		super(baseActivity);
		init();
	}

	public void init(){
		attachChild(getBaseActivity().getResourceUtil().getSprite("main_bg.png"));
		
		//�X�N���[���̃X�s�[�h�̏����l��6��
		scrollSpeed = 6;
		
		//1�߂̑�
		grass01 = getBaseActivity().getResourceUtil().getSprite("main_grass.png");
		
		grass01.setPosition(0,420);
		attachChild(grass01);
		
		//2�߂̑�
		grass02 = getBaseActivity().getResourceUtil().getSprite("main_grass.png");
		
		grass02.setPosition(getBaseActivity().getEngine().getCamera().getWidth(), 420);
		attachChild(grass02);
		
		//�E�҂�ǉ�
		ninja = getBaseActivity().getResourceUtil().getAnimatedSprite("main_ninja.png", 1, 6);
		
		//0�R�}�ځA1�R�}�ڂ݂̂��A�j���[�V����
		ninja.animate(new long[]{100,100},0,1,true);
		ninja.setPosition(80,325);
		attachChild(ninja);
		
		//�X�R�A�����l��0��
		currentScore = 0;
		
		//�[���ɓ��ڂ��ꂽ�t�H���g�𗘗p���ē��_��\��
		//�t�H���g�p��Texture��p��
		Texture texture = new BitmapTextureAtlas(getBaseActivity().getTextureManager(), 512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		//�t�H���g���C�j�V�����C�Y
		Font font = new Font(getBaseActivity().getFontManager(), texture, Typeface.DEFAULT_BOLD, 22, true, Color.YELLOW);
		
		//Engine��TextureManager�Ƀt�H���gTexture��ǂݍ���
		getBaseActivity().getTextureManager().loadTexture(texture);
		//FontManager�Ƀt�H���g��ǂݍ���
		getBaseActivity().getFontManager().loadFont(font);
		
		//�ǂݍ��񂾃t�H���g�����p���ē��_��\��
		currentScoreText = new Text(20,20,font,"���_�F" + currentScore, 20, 
				new TextOptions(HorizontalAlign.LEFT), getBaseActivity().getVertexBufferObjectManager());
		
		attachChild(currentScoreText);
		highSocreText = new Text(20, 50, font, "�n�C�X�R�A:" + SPUtil.getInstance(getBaseActivity()).getHighScore(),20,
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
		
		//���C�t�̏����l��3�Ƃ��A��ʍ����Sprite��\��
		lifeSpriteArray = new ArrayList<Sprite>();
		life = 3;
		for(int i = 0; i < life; i++) {
			Sprite heart = getBaseActivity().getResourceUtil().getSprite("main_heart.png");
			//�����߂ɃX�P�[��
			heart.setScale(0.6f);
			heart.setPosition(10 + 45 * i, 90);
			attachChild(heart);
			lifeSpriteArray.add(heart);
		}
		
		spriteOutOfBoundsArray = new ArrayList<Sprite>();
		updateHandlerArray = new ArrayList<CustomTimerHandler>();
		
		//�n�C�X�R�A��500�ȉ��̎�(���v���C��)�̂݃w���v��ʂ��o��
		if(SPUtil.getInstance(getBaseActivity()).getHighScore() > 500) {
		
		//1�b�Ԃ�60��AupdateHandler���Ăяo��
		registerUpdateHandler(updateHandler);
		//1�b���ɏ�Q���o���֐����Ăяo��
		registerUpdateHandler(obstacleAppearHandler);
		//Scene�̃^�b�`���X�i�[��o�^
		setOnSceneTouchListener(this);

		} else {
			showHelp();
		}
	}
	
	//�V�ѕ���ʂ��o��������
	public void showHelp() {
		instructionSprite = ResourceUtil.getInstance(getBaseActivity())
				.getSprite("instruction.png");
		placeToCenter(instructionSprite);
		attachChild(instructionSprite);
		
		//�{�^��
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
				
				//1�b�Ԃ�60��AupdateHandler���Ăяo��
				registerUpdateHandler(updateHandler);
				//1�b�Ԃɏ�Q���o���֐����Ăяo��
				registerUpdateHandler(obstacleAppearHandler);
				//Scene�̃^�b�`���X�i�[��o�^
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
			//�Q�[���I�[�o�[�ς݂Ȃ牽�����Ȃ�
			if(isGameOver) {
				return false;
			}
			//�|�[�Y���Ȃ�|�[�Y��ʂ�����
			if(isPaused) {
				//detach�n�̃��\�b�h�͕ʃX���b�h��
				getBaseActivity().runOnUpdateThread(new Runnable() {
					public void run() {
						for(int i = 0; i < pauseBg.getChildCount(); i++) {
							//�Y�ꂸ�Ƀ^�b�`�̌��m�𖳌���
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
			//�|�[�Y���łȂ���΃|�[�Y��ʂ��o��
			if(!isPaused) {
				showMenu();
			}
			return true;
		}
		return false;
	}
	
	//���j���[���o��
	public void showMenu() {
		if(isGameOver) {
			return;
		}
		pauseGame();
		//�l�p�`��`��
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

	//�Q�[�����ꎞ��~
	public void pauseGame() {
		//�S�Ă�AnimatedSprite�̃A�j���[�V�������X�g�b�v
		for(int i = 0; i < getChildCount(); i++) {
			if(getChildByIndex(i) instanceof AnimatedSprite) {
				((AnimatedSprite)getChildByIndex(i)).stopAnimation();
			}
		}
//		//�S�Ă�UpdateHandler���X�g�b�v
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

	//�ꎞ��~�����Q�[�����ĊJ
	public void resumeGame() {
		//�X�g�b�v��UpdateHandler���ēx�o�^
		registerUpdateHandler(updateHandler);
		registerUpdateHandler(obstacleAppearHandler);

		//AnimatedSprite�̃A�j�����ĊJ
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

	//�A�b�v�f�[�g�n���h���B1�b�Ԃ�60��Ăяo�����
	public TimerHandler updateHandler = new TimerHandler(1f/60f, true, new ITimerCallback() {
		public void onTimePassed(TimerHandler pTimerHandler) {
			//�X�R�A�̑����ƃZ�b�g
			if(!isRecovering) {
				//�X�R�A���C���N�������g
				currentScore++;
				//�X�R�A���Z�b�g
				currentScoreText.setText("���_:" + currentScore);
			}
			
			//����scrollSpeed�̕��ړ�������
			grass01.setX(grass01.getX() - scrollSpeed);
			if(grass01.getX() <= -getBaseActivity().getEngine().getCamera().getWidth()) {
				//���̉E�[����ʍ��[��荶�Ɉړ������ꍇ�͉�ʕ�*2���E�ֈړ�
				grass01.setX(grass01.getX() + getBaseActivity().getEngine().getCamera().getWidth()*2);
			}
			grass02.setX(grass02.getX() - scrollSpeed);
			if(grass02.getX() <= -getBaseActivity().getEngine().getCamera().getWidth()) {
				grass02.setX(grass02.getX() + getBaseActivity().getEngine().getCamera().getWidth()*2);
			}
			
			//��Q�����ړ�
			for(int i= 0; i < getChildCount(); i++) {
				if(getChildByIndex(i).getTag() == TAG_OBSTACLE_TRAP
						|| getChildByIndex(i).getTag() == TAG_OBSTACLE_FIRE
						|| getChildByIndex(i).getTag() == TAG_OBSTACLE_ENEMY
						|| getChildByIndex(i).getTag() == TAG_OBSTACLE_EAGLE
						|| getChildByIndex(i).getTag() == TAG_DEAD_BODY
						|| getChildByIndex(i).getTag() == TAG_OBSTACLE_HEART) {
					//��̂ݔE�҂̏�󂩂犊�󂳂���
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
						//�����ɍ폜����ƃC���f�b�N�X������邽��
						//��U�z��ɒǉ�
						spriteOutOfBoundsArray.add((Sprite)getChildByIndex(i));
					}
					
					//�񕜒��͖���
					if(!isRecovering) {
						//Sprite���m���Փ˂��Ă��鎞�̂�
						//���x�ȏՓ˔����
						if(((Sprite)getChildByIndex(i)).collidesWith(ninja)){
							//�E�҂�x���W���S��
							//��Q����x���W���S�Ԃ̋���
							float distanceBetweenCenterXOfNinjaAndObstacle = Math.abs((getChildByIndex(i).getX()
									+((Sprite)getChildByIndex(i)).getWidth()/2) -(ninja.getX() + (ninja.getWidth()/2)));
							
							//�Փ˂����e���鋗��
							float allowableDistance = 0;
							if(getChildByIndex(i).getTag() == TAG_OBSTACLE_TRAP){
								allowableDistance = 50;
								if(distanceBetweenCenterXOfNinjaAndObstacle < ninja.getWidth()/2
										+ ((Sprite)getChildByIndex(i)).getWidth() /2 -allowableDistance) {
									//�G�̍U��
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
			//�z��̒��g���폜
			for(Sprite sp : spriteOutOfBoundsArray) {
				sp.detachSelf();
			}
			
			//�c�胉�C�t��3�������X�e�[�W��ɉ񕜃A�C�e����������
			if(life < 3 && getChildByTag(TAG_OBSTACLE_HEART) == null) {
				//500����1�̊m���ŉ񕜃A�C�e�����o��
				if((int)(Math.random()*500) == 1) {
					Sprite obstacle = getBaseActivity().getResourceUtil().getSprite("main_heart.png");
					//��ʉE���ɒǉ��By���W�̓����_��
					obstacle.setPosition(getBaseActivity().getEngine().getCamera().getWidth()
							+ obstacle.getWidth(), 350 - (int)(Math.random() * 200));
					obstacle.setTag(TAG_OBSTACLE_HEART);
					attachChild(obstacle);
				}
			}
		}

	});
	
	//1�b���ɉ�ʉE�O�ɏ�Q�����o��������֐�
	private TimerHandler obstacleAppearHandler = new TimerHandler(1, true, new ITimerCallback(){
		public void onTimePassed(TimerHandler pTimerHandler) {
			Sprite obstacle = null;
			AnimatedSprite animatedObstacle = null;
			//�G�̎�ނ������_���ɑI��
			int r = (int)(Math.random()*4);
			switch(r){
			case 0:
				//�|�łł����
				obstacle = getBaseActivity().getResourceUtil().getSprite("main_trap.png");
				obstacle.setPosition(getBaseActivity().getEngine().getCamera().getWidth() + obstacle.getWidth(), 380);
				obstacle.setTag(TAG_OBSTACLE_TRAP);
				attachChild(obstacle);
				break;
			case 1:
				//�΂̋�
				obstacle = getBaseActivity().getResourceUtil().getSprite("main_fire.png");
				obstacle.setPosition(getBaseActivity().getEngine().getCamera().getWidth() + obstacle.getWidth(), 260);
				obstacle.setTag(TAG_OBSTACLE_FIRE);
				attachChild(obstacle);
				break;
			case 2:
				//�G�E��
				animatedObstacle = getBaseActivity().getResourceUtil().getAnimatedSprite("main_enemy.png",1,2);
				animatedObstacle.setPosition(getBaseActivity().getEngine().getCamera().getWidth() + animatedObstacle.getWidth(), 325);
				animatedObstacle.setTag(TAG_OBSTACLE_ENEMY);
				attachChild(animatedObstacle);
				animatedObstacle.animate(100);
				break;
			case 3:
				//��
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
	//�^�b�`�C�x���g������������Ă΂��
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		
		//�^�b�`�̍��W���擾
		float x = pSceneTouchEvent.getX();
		float y = pSceneTouchEvent.getY();
		
		//�U��
		if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			//�^�b�v����ʒ��S���E���Ȃ�
			if(x > getBaseActivity().getEngine().getCamera().getWidth() /2 ){
				//�X���C�f�B���O���ł͂Ȃ��A�U�����ł��Ȃ���΍U��
				if(!isSliding && !isAttacking) {
					//�U��
					fireWeapon();
				}
				return true;
			}
		}		
		
		if(!isTouchEnabled) {
			return true;
		}
		
		if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			//�J�n�_��o�^
			touchStartPoint[0] = x;
			touchStartPoint[1] = y;
		} else if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP
				|| pSceneTouchEvent.getAction() == TouchEvent.ACTION_CANCEL) {
			float[] touchEndPoint = new float[2];
			touchEndPoint[0] = x;
			touchEndPoint[1] = y;
			
			//�t���b�N�̋������Z�����鎞�ɂ̓t���b�N�Ɣ��肵�Ȃ�
			if(Math.abs(touchEndPoint[0] - touchStartPoint[0]) < 50
					&& Math.abs(touchEndPoint[1] - touchStartPoint[1]) < 50 ) {
				return true;
			}
			
			//�t���b�N�̊p�x�����߂�
			double angle = getAngleByTwoPosition(touchStartPoint, touchEndPoint);
			//�������̃t���b�N��0���ɒ���(�������̃��W�b�N���኱�ρB�v�f�o�b�O)
			angle -= 180;
			if(angle > -45 && angle < 45){
				jumpSprite();
			} else if( angle > 135 && angle < 225) {
				slideSprite();
			}
		}
		return true;
	}

	//�E�҂��W�����v������
	private void jumpSprite() {
		//�W�����v���̓^�b�v�͎󂯕t���Ȃ�
		isTouchEnabled = false;
		isJumping = true;
		ninja.stopAnimation();
		//�E�҂̉摜��2�ɕύX(0����n�܂�̂ŏォ��3�Ԗ�)
		ninja.setCurrentTileIndex(2);
		ninja.setPosition(ninja.getX(), ninja.getY() - 100);
		
//		registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback(){
//			public void onTimePassed(TimerHandler pTimeHandler) {
//				//���v�ɖ߂�
//				setSpriteToDefaultPosition();
//			}
//		}));
		
		registerUpdateHandler(setSpriteToDefaultPositionHander);
		updateHandlerArray.add(setSpriteToDefaultPositionHander);
	}

	//�E�҂��X���C�f�B���O������
	private void slideSprite() {
		//�X���C�f�B���O���̓^�b�v�͎󂯕t���Ȃ�
		isTouchEnabled = false;
		isSliding = true;
		ninja.stopAnimation();
		//�E�҂̉摜��3�ɕύX(0����n�܂�̂ŏォ��4�Ԗ�)
		ninja.setCurrentTileIndex(3);
		
		registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback() {
			public void onTimePassed(TimerHandler pTimerHandler){
				//���ɖ߂�
				setSpriteToDefaultPosition();
			}
		}));
	}

	//�E�҂̍��W�Ɠ�����ʏ�ɖ߂�
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

	//�U��
	private void fireWeapon() {
		//�U���̘A�˂�h��
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
		
		//�폜����Sprite����U�i�[����z��
		ArrayList<AnimatedSprite> spToRemoveArray = new ArrayList<AnimatedSprite>();
		
		for(int i = 0; i < getChildCount(); i++) {
			//�U���œ|����͓̂G�E�҂Ƒ�̂�
			if(getChildByIndex(i).getTag() == TAG_OBSTACLE_ENEMY || 
					getChildByIndex(i).getTag() == TAG_OBSTACLE_EAGLE ) {
				//�Փ˔���
				if(((AnimatedSprite)getChildByIndex(i)).collidesWith(weapon)) {
					//���ʉ����Đ�
					ninjaAttackSound.play();
					spToRemoveArray.add((AnimatedSprite)getChildByIndex(i));
				}
			}
		}
		
		//�폜
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
	
	//�G�̍U��
	public void enemyAttack(int enemyTag){
		ninja.setAlpha(0);
		Sprite deadBody = null;
		//�΂̋ʂɓ����������͍�����Sprite�ɁA���̓Y�b�R�PSprite�ɕύX
		if(enemyTag == TAG_OBSTACLE_FIRE){
			deadBody = getBaseActivity().getResourceUtil().getSprite("main_ninja_burn.png");
			//���ʉ����Đ�
			ninjaBurnSound.play();
		} else {
			deadBody = getBaseActivity().getResourceUtil().getSprite("main_ninja_dead.png");
			//���ʉ����Đ�
			enemyAttackSound.play();
		}
		deadBody.setTag(TAG_DEAD_BODY);
		deadBody.setPosition(ninja);
		attachChild(deadBody);
		
		//�񕜌�͈�莞�Ԗ��G��Ԃ�
		isRecovering = true;
		
		//���C�t�����炷
		lifeSpriteArray.get(life-1).detachSelf();
		life--;
		
		//���C�t��0�Ȃ�Q�[���I�[�o�[
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
//							//���G��Ԃ�����
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

	//�Q�[���I�[�o�[
	private void showGameOver() {
		
		//���ʉ����Đ�
		gameoverSound.play();
		
		//Scene�̃^�b�`���X�i�[������
		setOnSceneTouchListener(null);
		
		//�n�C�X�R�A�X�V���͕ۑ�
		if(currentScore > SPUtil.getInstance(getBaseActivity()).getHighScore()) {
			SPUtil.getInstance(getBaseActivity()).setHighSocre(currentScore);
		}
		
		//�����Ȕw�i���쐬
		Rectangle resultBg = new Rectangle(getBaseActivity().getEngine()
				.getCamera().getWidth(), 0 , getBaseActivity().getEngine()
				.getCamera().getWidth(), getBaseActivity().getEngine()
				.getCamera().getHeight(), getBaseActivity()
				.getVertexBufferObjectManager());
		
		//������
		resultBg.setColor(Color.TRANSPARENT);
		
		//�G�L�����N�^�[���O�ʂɕ\��
		resultBg.setZIndex(zIndexResult);
		attachChild(resultBg);
		sortChildren();
		
		//�r�b�g�}�b�v�t�H���g���쐬
		BitmapFont bitmapFont = new BitmapFont(getBaseActivity()
				.getTextureManager(), getBaseActivity().getAssets(), 
				"font/result.fnt");
		bitmapFont.load();
		
		//�r�b�g�}�b�v�t�H���g�����ɃX�R�A��\��
		Text resultText = new Text(0, 0, bitmapFont, 
				""+currentScore+"pts", 20, new TextOptions(
						HorizontalAlign.CENTER), getBaseActivity()
						.getVertexBufferObjectManager());
		
		resultText.setPosition(getBaseActivity().getEngine().getCamera()
				.getWidth() / 2.0f - resultText.getWidth() / 2.0f, 60);
		resultBg.attachChild(resultText);
		
		//�e�{�^��
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
		
		//�Q�[���I�[�o�[��ʂ��E������X���C�h�C��
		resultBg.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(1.0f), new MoveModifier(1.0f, resultBg.getX(), resultBg.getX()
						- getBaseActivity().getEngine().getCamera()
						.getWidth(), resultBg.getY(), resultBg.getY(), EaseQuadOut.getInstance())));
		
	}

	public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		//���ʉ����Đ�
		btnPressedSound.play();
		switch(pButtonSprite.getTag()) {
		case MENU_RESUME:
			//detachChildren��detachSelf�𓯂��^�C�~���O�ŌĂԎ��͕ʃX���b�h��
			getBaseActivity().runOnUpdateThread(new Runnable() {
				public void run() {
					for(int i = 0; i < pauseBg.getChildCount(); i++ ) {
						//�Y�ꂸ�Ƀ^�b�`�̌��m�𖳌���
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
			sendIntent.putExtra(Intent.EXTRA_TEXT, "Android�Q�[���u�ɂ񂶂�DASH�I�I�v��"
					+ currentScore + "�_�l��!! �� "
					+ "http://bit.ly/SrpS46");
			getBaseActivity().startActivity(sendIntent);
			break;
		case MENU_RANKING:
			break;
		}
	}

	//��
	private void addLife() {
		if(life < 3) {
			//���ʉ����Đ�
			recoverySound.play();
			life++;
			attachChild(lifeSpriteArray.get(life -1));
			lifeSpriteArray.get(life-1).registerEntityModifier(new LoopEntityModifier(
					new SequenceEntityModifier(
							new ScaleModifier(0.25f, 0.6f, 1.3f), new ScaleModifier(0.25f, 1.3f,0.6f)),4));
		}
		
	}
	
	//2�_�Ԃ̊p�x�����߂����
	private double getAngleByTwoPosition(float[] start, float[] end) {
		double result = 0;
		
		float xDistance = end[0] - start[0];
		float yDistance = end[1] - start[1];
		
		result = Math.atan2((double)yDistance, (double)xDistance) * 180 / Math.PI;
		result += 270;
		
		return result;
	}


}
