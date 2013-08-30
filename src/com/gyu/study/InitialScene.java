package com.gyu.study;

import java.io.IOException;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.util.modifier.ease.EaseBackInOut;

import android.content.Intent;
import android.view.KeyEvent;
public class InitialScene extends KeyListenScene implements ButtonSprite.OnClickListener {

	private static final int INITIAL_START = 1;
	private static final int INITIAL_RANKING = 2;
	private static final int INITIAL_FEEDBACK = 3;
	
	//ボタンが押された時の効果音
	private Sound btnPressedSound;
	
	public InitialScene(MultiSceneActivity context) {
		super(context);
		init();
	}
	
	@Override
	public void init() {
		//背景
		Sprite bg = getBaseActivity().getResourceUtil().getSprite("initial_bg.png");
		bg.setPosition(0,0);
		attachChild(bg);
		
		//タイトル。上から滑り落ちてくる
		Sprite titleSprite = getBaseActivity().getResourceUtil().getSprite("initial_title.png");
		placeToCenterX(titleSprite, 40);
		titleSprite.setY(titleSprite.getY() - 200);
		attachChild(titleSprite);
		
		titleSprite.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(0.5f), new MoveModifier(1.0f, titleSprite.getX(), titleSprite.getX(), 
						titleSprite.getY(), titleSprite.getY()+200, EaseBackInOut.getInstance())));
		
		//竹Sprite。左から滑り込んでくる。
		Sprite bambooLeft = getBaseActivity().getResourceUtil().getSprite("initial_bamboo_01.png");
		placeToCenterY(bambooLeft, -bambooLeft.getWidth());
		attachChild(bambooLeft);
		
		bambooLeft.registerEntityModifier(new SequenceEntityModifier(
				new MoveModifier(1.0f, bambooLeft.getX(), bambooLeft.getX()
						+ bambooLeft.getWidth() - 50, bambooLeft.getY(),
						bambooLeft.getY(), EaseBackInOut.getInstance())));
		
		//竹Sprite。右から滑り込んでくる。
		Sprite bambooRight = getBaseActivity().getResourceUtil().getSprite("initial_bamboo_02.png");
		placeToCenterY(bambooRight, getBaseActivity().getEngine().getCamera().getWidth());
		attachChild(bambooRight);
		
		bambooRight.registerEntityModifier(new SequenceEntityModifier(
				new MoveModifier(1.0f, bambooRight.getX(), bambooRight.getX()
						- bambooRight.getWidth() + 50, bambooRight.getY(),
						bambooRight.getY(), EaseBackInOut.getInstance())));
		//ボタン追加
		//スタートボタン
		ButtonSprite btnStart = getBaseActivity().getResourceUtil()
				.getButtonSprite("initial_btn_01.png", "initial_btn_01_p.png");
		placeToCenterX(btnStart, 240);
		btnStart.setY(btnStart.getY() + 400);
		btnStart.setTag(INITIAL_START);
		btnStart.setOnClickListener(this);
		attachChild(btnStart);
		//ボタンタップ可能に
		registerTouchArea(btnStart);
		
		btnStart.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(1.0f), new MoveModifier(1.0f, btnStart.getX(), btnStart.getX(), 
						btnStart.getY(), btnStart.getY() - 400, EaseBackInOut.getInstance())));
		
		//ランキングボタン
		ButtonSprite btnRanking = getBaseActivity().getResourceUtil()
				.getButtonSprite("initial_btn_02.png", "initial_btn_02_p.png");
		placeToCenterX(btnRanking, 310);
		btnRanking.setY(btnRanking.getY() + 400);
		btnRanking.setTag(INITIAL_RANKING);
		btnRanking.setOnClickListener(this);
		attachChild(btnRanking);
		//ボタンタップ可能に
		registerTouchArea(btnRanking);
		
		btnRanking.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(1.2f), new MoveModifier(1.0f, btnRanking.getX(), btnRanking.getX(), 
						btnRanking.getY(), btnRanking.getY() - 400, EaseBackInOut.getInstance())));
				
		//フィードバックボタン
		ButtonSprite btnRecommend = getBaseActivity().getResourceUtil()
				.getButtonSprite("initial_btn_03.png", "initial_btn_03_p.png");
		placeToCenterX(btnRecommend, 380);
		btnRecommend.setY(btnRecommend.getY() + 400);
		btnRecommend.setTag(INITIAL_FEEDBACK);
		btnRecommend.setOnClickListener(this);
		attachChild(btnRecommend);
		//ボタンタップ可能に
		registerTouchArea(btnRecommend);
		
		btnRecommend.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(1.4f), new MoveModifier(1.0f, btnRecommend.getX(), btnRecommend.getX(), 
						btnRecommend.getY(), btnRecommend.getY() - 400, EaseBackInOut.getInstance())));		
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}
	
	@Override
	public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
			float pTouchAreaLocalY) {
		//効果音を再生
		btnPressedSound.play();
		switch (pButtonSprite.getTag()) {
		case INITIAL_START:
			//リソースの解放
			ResourceUtil.getInstance(getBaseActivity()).resetAllTexture();
			KeyListenScene scene = new MainScene(getBaseActivity());
			//MainSceneへ移動
			getBaseActivity().getEngine().setScene(scene);
			//遷移管理用配列に追加
			getBaseActivity().appendScene(scene);
			break;
		case INITIAL_RANKING:
			
			break;
		case INITIAL_FEEDBACK:
			Intent it = new Intent(Intent.ACTION_SEND);
			it.putExtra(Intent.EXTRA_EMAIL, new String[]{"2chandorid@gmail.com"});
			it.putExtra(Intent.EXTRA_SUBJECT, "[Feedback]About Ninja Run");
			it.setType("message/rfc822");
			getBaseActivity().startActivity(Intent.createChooser(it, "Choose Email Client"));
			break;
		default:
			break;
		}
	}

	@Override
	public void prepareSoundAndMusic() {
		try {
			btnPressedSound = SoundFactory.createSoundFromAsset(getBaseActivity().getSoundManager(), 
					getBaseActivity(), "clock00.wav");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
}
