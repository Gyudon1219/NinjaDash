package com.gyu.study;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;

public class CustomTimerHandler extends TimerHandler {
	
	private boolean isPaused;
	
	public CustomTimerHandler(float pTimerSeconds, ITimerCallback pTimerCallback) {
		super(pTimerSeconds, pTimerCallback);
	}
	
	//ポーズ中はupdate関数を呼び出さない
	@Override
	public void onUpdate(final float pSecondsElapsed) {
		if(!isPaused) {
			super.onUpdate(pSecondsElapsed);
		}
	}
	
	//ポーズ
	public void pause() {
		this.isPaused = true;
	}
	
	//再開
	public void resume() {
		if(this.isPaused) {
			this.isPaused = false;
		}
	}
}
