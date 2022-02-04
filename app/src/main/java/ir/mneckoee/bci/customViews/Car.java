package ir.mneckoee.bci.customViews;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ir.mneckoee.bci.R;

public class Car extends androidx.appcompat.widget.AppCompatImageView {
    public boolean isRunning;
    public static final int LEFT=1;
    public static final int RIGHT=2;
    ObjectAnimator animation;
    public Car(@NonNull Context context) {
        super(context);
        init();
    }

    public Car(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Car(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        isRunning=false;
        setImageResource(R.drawable.car1);
        animation = ObjectAnimator.ofFloat(this, "translationX",230f);
        animation.setDuration(1000);
        animation.addListener(animatorListener);

    }

    public void move(int direction){
     //   animation.cancel();
        switch (direction){
            case LEFT:
                animation.setFloatValues(230f);
                break;
            case RIGHT:
                animation.setFloatValues(760f);
                break;
            default:


        }

        animation.start();
        isRunning= animation.isRunning();
    }
    public boolean getIsRunning(){
        return isRunning;
    }

    Animator.AnimatorListener animatorListener=new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            isRunning=true;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            isRunning=false;
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };


}
