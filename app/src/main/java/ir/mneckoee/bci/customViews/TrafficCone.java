package ir.mneckoee.bci.customViews;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ir.mneckoee.bci.R;

public class TrafficCone extends androidx.appcompat.widget.AppCompatImageView {
    public boolean isRunning;
    ObjectAnimator animation;
    public TrafficCone(@NonNull Context context) {
        super(context);
        init();
    }

    public TrafficCone(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrafficCone(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        isRunning=false;
        setVisibility(INVISIBLE);
        setImageResource(R.drawable.cone);
//        animation = ObjectAnimator.ofFloat(this, "translationY",2000f);
//        animation.setDuration(4000);
//        animation.addListener(animatorListener);
        //getRootView().getHeight()

    }

    public void moveDown(){
        setVisibility(VISIBLE);
        animation = ObjectAnimator.ofFloat(this, "translationY",2000f);
        animation.setDuration(2000);
        animation.addListener(animatorListener);
        animation.start();
        isRunning= animation.isRunning();
    }
    public boolean getIsRunning(){
        return isRunning;
    }
     public void changeImage(){

       setImageResource(R.drawable.cone2);
     }

    Animator.AnimatorListener animatorListener=new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            isRunning=true;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            isRunning=false;
            setVisibility(INVISIBLE);

            setImageResource(R.drawable.cone);
           setY(0f);
        }

        @Override
        public void onAnimationCancel(Animator animator) {
          //
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
          //
        }
    };
}
