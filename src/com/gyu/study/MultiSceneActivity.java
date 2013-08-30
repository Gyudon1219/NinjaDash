package com.gyu.study;

import java.util.ArrayList;

import org.andengine.ui.activity.SimpleLayoutGameActivity;
import com.gyu.study.KeyListenScene;
import com.gyu.study.ResourceUtil;

import com.gyu.sexros5.R;


public abstract class MultiSceneActivity extends SimpleLayoutGameActivity {
	//ResourceUtilのインスタンス
	private ResourceUtil mResourceUtil;
	//起動済みのScene配列
	private ArrayList<KeyListenScene> mSceneArray;
	
	@Override
	protected void onCreateResources() {
		mResourceUtil = ResourceUtil.getInstance(this);
		mSceneArray = new ArrayList<KeyListenScene>();
	}
	
	public ResourceUtil getResourceUtil() {
		return mResourceUtil;
	}
	
	public ArrayList<KeyListenScene> getSceneArray() {
		return mSceneArray;
	}
	
	//起動済みのKeyListenSceneを格納する配列
	public abstract void appendScene(KeyListenScene scene);
	//最初のシーンに戻る為の関数
	public abstract void backToInitial();
	//シーンとシーン格納配列を更新する関数
	public abstract void refreshRunningScene(KeyListenScene scene);
}