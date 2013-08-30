package com.gyu.study;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;

public class CustomTimerHandler extends TimerHandler {
	
	private boolean isPaused;
	
	public CustomTimerHandler(float pTimerSeconds, ITimerCallback pTimerCallback) {
		super(pTimerSeconds, pTimerCallback);
	}
	
	//�|�[�Y����update�֐����Ăяo���Ȃ�
	@Override
	public void onUpdate(final float pSecondsElapsed) {
		if(!isPaused) {
			super.onUpdate(pSecondsElapsed);
		}
	}
	
	//�|�[�Y
	public void pause() {
		this.isPaused = true;
	}
	
	//�ĊJ
	public void resume() {
		if(this.isPaused) {
			this.isPaused = false;
		}
	}
}
