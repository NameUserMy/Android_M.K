package com.example.animation;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Animation alphaAnimation;
    Animation rotateAnimation;
    Animation rotate2Animation;
    Animation scaleAnimation;

    Animation bellAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        alphaAnimation= AnimationUtils.loadAnimation(this,R.anim.anim_alpha);
        rotateAnimation= AnimationUtils.loadAnimation(this,R.anim.anim_rotate);
        rotate2Animation= AnimationUtils.loadAnimation(this,R.anim.anim_rotate2);
        scaleAnimation= AnimationUtils.loadAnimation(this,R.anim.anim_scale);
        findViewById(R.id.anim_v_scale).setOnClickListener(
                v->v.startAnimation(scaleAnimation) );
        findViewById(R.id.anim_v_alpha).setOnClickListener(
                v->v.startAnimation(alphaAnimation) );
        findViewById(R.id.anim_v_rotate).setOnClickListener(
                v->v.startAnimation(rotateAnimation) );
        findViewById(R.id.anim_v_rotate2).setOnClickListener(
                v->v.startAnimation(rotate2Animation) );


        bellAnimation= AnimationUtils.loadAnimation(this,R.anim.bell_anim);
        findViewById(R.id.anim_v_dz).setOnClickListener(
                v->v.startAnimation(bellAnimation) );
    }
}