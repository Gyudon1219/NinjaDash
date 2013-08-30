package com.gyu.study;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
//import org.andengine.ui.activity.BaseGameActivity;
import android.view.KeyEvent;

public abstract class KeyListenScene extends Scene{
	
	private MultiSceneActivity baseActivity;
	
	public KeyListenScene(MultiSceneActivity baseActivity) {
		setTouchAreaBindingOnActionDownEnabled(true);
		this.baseActivity = baseActivity;
		prepareSoundAndMusic();
	}
	
	public MultiSceneActivity getBaseActivity(){
		return baseActivity;
	}
	
	//�C�j�V�����C�U
	public abstract void init();
	
	//�T�E���h�̏���
	public abstract void prepareSoundAndMusic();
	
	//KeyEvent�̃��X�i�[
	public abstract boolean dispatchKeyEvent(KeyEvent e);
	
	//Sprite�̍��W����ʒ����ɐݒ肷��(Sprite�̒�������ʒ�����)
	public Sprite placeToCenter(Sprite sp) {
		sp.setPosition(baseActivity.getEngine().getCamera().getWidth() / 2.0f - sp.getWidth() /2.0f , 
				baseActivity.getEngine().getCamera().getHeight() / 2.0f - sp.getHeight() / 2.0f);
		return sp;
	}

	//Sprite��x���W����ʒ����ɐݒ肷��(Sprite�̒�������ʒ�����)
	//(Sprite��x���W�̒��S����ʂ�x���W�̒��S��)y���W�͔C�ӂ̒l
	public Sprite placeToCenterX(Sprite sp, float y) {
		sp.setPosition(baseActivity.getEngine().getCamera().getWidth() / 2.0f - sp.getWidth() /2.0f , y);
		return sp;
	}
	
	//Sprite��y���W����ʒ����ɐݒ肷��
	//(Sprite��y���W�̒��S����ʂ�y���W�̒��S��)x���W�͔C�ӂ̒l
	public Sprite placeToCenterY(Sprite sp, float x) {
		sp.setPosition(x, baseActivity.getEngine().getCamera().getHeight() / 2.0f - sp.getHeight() / 2.0f);
		return sp;
	}
}