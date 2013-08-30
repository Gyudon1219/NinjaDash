package com.gyu.study;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil {
	
	//���g�̃C���X�^���X
	private static SPUtil instance;
	
	//�V���O���g��
	public static synchronized SPUtil getInstance(Context context){
		if(instance == null) {
			instance = new SPUtil(context);
		}
		return instance;
	}
	
	private static SharedPreferences settings;
	private static SharedPreferences.Editor editor;
	
	private SPUtil(Context context) {
		settings = context.getSharedPreferences("shared_preference_1.0", 0);
		editor = settings.edit();
	}
	
	public int getHighScore() {
		return settings.getInt("highScore", 0);
	}
	
	public void setHighSocre(int value) {
		editor.putInt("highScore", value);
		editor.commit();
	}
	

}
