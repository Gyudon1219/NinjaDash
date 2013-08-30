package com.gyu.study;

import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.*;

import android.view.KeyEvent;
//import org.andengine.ui.activity.SimpleLayoutGameActivity;

import com.gyu.sexros5.R;


public class MainActivity extends MultiSceneActivity {
	
	
	//��ʂ̃T�C�Y
	private int CAMERA_WIDTH = 800;
	private int CAMERA_HEIGHT = 480;
	
	public EngineOptions onCreateEngineOptions() {
		//�T�C�Y���w�肵�`��͈͂��C���X�^���X��
		final Camera camera = new Camera(0,0,CAMERA_WIDTH,CAMERA_HEIGHT);
		
		//�Q�[���̃G���W�����������B
		//��1�����@�^�C�g���o�[��\�����Ȃ����[�h
		//��2�����@��ʂ͏c�����i��480, ����800�j
		//��3�����@�𑜓x�̏c�����ۂ����܂܍ő�܂Ŋg�傷��
		//��4�����@�`��͈�
//		EngineOptions eo = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, 
//				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		
		EngineOptions eo = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, 
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		//���ʉ��̎g�p��������
		eo.getAudioOptions().setNeedsSound(true);
		return eo;
	}
	
	@Override
	protected Scene onCreateScene() {
//		//MaineScene���C���X�^���X�����A�G���W���ɃZ�b�g
//		MainScene mainScene = new MainScene(this);
//		return mainScene;
		//�T�E���h�t�@�C���̊i�[�ꏊ���w��
		SoundFactory.setAssetBasePath("mfx/");
		//InitialScene���C���X�^���X�����A�G���W���ɃZ�b�g
		InitialScene initialScene = new InitialScene(this);
		//�J�ڊǗ��p�z��ɒǉ�
		getSceneArray().add(initialScene);
		return initialScene;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		//MainScene���s���Ȃ�ꎞ��~.(�Q�[���r���Ńz�[����ʂɖ߂����肷��ƈꎞ��~���j���[�\���ɂȂ�)
		if(getEngine().getScene() instanceof MainScene) {
			((MainScene)getEngine().getScene()).showMenu();
		}
	}
	
	@Override
	protected int getLayoutID() {
		//Activity�̃��C�A�E�g��ID��Ԃ�
		return R.layout.activity_main;
	}
	
	@Override
	protected int getRenderSurfaceViewID() {
		//Scene���Z�b�g�����View��ID��Ԃ�
		return R.id.renderview;
	}

	@Override
	public void appendScene(KeyListenScene scene) {
		getSceneArray().add(scene);
	}

	@Override
	public void backToInitial() {
		//�J�ڊǗ��p�z����N���A
		getSceneArray().clear();
		//�V����Initialscene����X�^�[�g
		KeyListenScene scene = new InitialScene(this);
		getSceneArray().add(scene);
		getEngine().setScene(scene);
	}

	@Override
	public void refreshRunningScene(KeyListenScene scene) {
		//�z��̍Ō�̗v�f���폜���A�V�������̂ɓ���ւ���
		getSceneArray().remove(getSceneArray().size()-1);
		getSceneArray().add(scene);
		getEngine().setScene(scene);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		//�o�b�N�{�^���������ꂽ��
		if(e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			//�N������Scene��dispatchKeyEvent�֐����Ăяo���B
			//�ǉ��̏������K�v�Ȏ���false���Ԃ��Ă���ׁA����
			if(!getSceneArray().get(getSceneArray().size()-1).dispatchKeyEvent(e)) {
				//Scene��1�����N�����Ă��Ȃ����̓Q�[�����I��
				if(getSceneArray().size() == 1) {
					ResourceUtil.getInstance(this).resetAllTexture();
					finish();
				}
				//������Scene���N�����Ă��鎞��1�O�̃V�[���֖߂�
				else {
					getEngine().setScene(getSceneArray().get(getSceneArray().size()-2));
					getSceneArray().remove(getSceneArray().size()-1);
				}
			}
			return true;
		} else if(e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			getSceneArray().get(getSceneArray().size() - 1).dispatchKeyEvent(e);
			return true;
		}
		return false;
	}
}