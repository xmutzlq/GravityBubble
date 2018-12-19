package com.mygdx.game;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.mygdx.game.entity.BallData;

import java.util.ArrayList;

public class AndroidLauncher extends FragmentActivity implements AndroidFragmentApplication.Callbacks {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    }

	@Override
	public void exit() {

	}

	public static class GameFragment extends AndroidFragmentApplication {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ArrayList<BallData> ballSprites = this.getArguments().getParcelableArrayList("ballSprites");
            return initializeForView(new MyGdxGame(getActivity(), ballSprites));
        }
	}
}

