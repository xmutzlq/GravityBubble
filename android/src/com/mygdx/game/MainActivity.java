package com.mygdx.game;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.mygdx.game.entity.BallData;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {
    private static final float MAX_BATCH_SIZE = 90;
    private static final float MIN_BATCH_SIZE = 43;
    private static final int BALL_SIZE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<BallData> ballData = buildBallsData();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("ballSprites", ballData);
        AndroidLauncher.GameFragment fragment = new AndroidLauncher.GameFragment();
        fragment.setArguments(bundle);
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.fragment, fragment);
        trans.commit();

    }

    private ArrayList<BallData> buildBallsData() {
        ArrayList<BallData> ballSprites = new ArrayList<>();
        for (int i = 0; i < BALL_SIZE; i ++) {
            BallData ballData = new BallData();
            float ranSize = (float) (Math.random() * (MAX_BATCH_SIZE - MIN_BATCH_SIZE) + MIN_BATCH_SIZE);
            ballData.size = ranSize; //大小
            int ranColor = 0xff000000 | new Random().nextInt(0x00ffffff);
            ballData.color = ranColor; //颜色
            ballSprites.add(ballData);
        }
        return ballSprites;
    }

    @Override
    public void exit() {

    }
}
