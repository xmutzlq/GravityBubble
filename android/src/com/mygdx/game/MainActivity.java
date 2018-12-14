package com.mygdx.game;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {
    private static final float MAX_BATCH_SIZE = 90;
    private static final float MIN_BATCH_SIZE = 43;
    ArrayList<BallSprite> ballSprites = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i = 0; i < 16; i ++) {
            BallSprite ballSprite = new BallSprite();
            float ranSize = (float) (Math.random() * (MAX_BATCH_SIZE - MIN_BATCH_SIZE) + MIN_BATCH_SIZE);
            ballSprite.size = ranSize;
            int ranColor = 0xff000000 | new Random().nextInt(0x00ffffff);
            ballSprite.color = ranColor;
            ballSprites.add(ballSprite);
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("ballSprites", ballSprites);
        AndroidLauncher.GameFragment fragment = new AndroidLauncher.GameFragment();
        fragment.setArguments(bundle);
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.fragment, fragment);
        trans.commit();

    }

    @Override
    public void exit() {

    }
}
