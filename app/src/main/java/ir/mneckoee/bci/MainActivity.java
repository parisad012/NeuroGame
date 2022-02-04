package ir.mneckoee.bci;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ir.mneckoee.bci.customViews.Car;
import ir.mneckoee.bci.customViews.TrafficCone;
import ir.mneckoee.bci.databinding.ActivityMainBinding;
import ir.mneckoee.bci.game.GameControllerService;

public class MainActivity extends AppCompatActivity {
    GameControllerService gameService;
    boolean mServiceBound = false;
    private ActivityMainBinding mBinding;
    Intent intent;
    Timer timerTrafficCone;
    TimerTask timerTask;
    TrafficCone coneLeft,coneRight;
    Car car;
    Button btnStart;
    TextView scoreTxt,statusTxt;
    boolean isStop;
    Messenger mService = null;
    MutableLiveData<Integer> score=new MutableLiveData<>();
    MutableLiveData<String> status=new MutableLiveData<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding=ActivityMainBinding.inflate(getLayoutInflater());
        View view=mBinding.getRoot();
        setContentView(view);
        getViewItems();
        handleBtnStart();
        isStop=true;
        score.postValue(1000);
        status.postValue("waiting");
        setObserver();
    }

    private void setObserver(){
        score.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                scoreTxt.setText(""+integer);
            }
        });
        status.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                statusTxt.setText(s);
            }
        });
    }

    private void getViewItems(){
        btnStart= mBinding.btnStart;
        car=mBinding.car;
        coneLeft= mBinding.coneLeft;
        coneRight= mBinding.coneRight;
        scoreTxt=mBinding.textView;
        statusTxt=mBinding.textView2;
    }
    private void handleBtnStart(){
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mServiceBound){
                startService(intent);
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                btnStart.setText(R.string.stop);
                score.setValue(1000);
                }else{
                    failed();
                    btnStart.setText(R.string.start);
                    stopGame();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        intent = new Intent(this, GameControllerService.class);

    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
           doUnbindService();
        }
    }
    //--------------------------------------unbind service
    private void doUnbindService(){
        mService=null;
        stopService(new Intent(this, GameControllerService.class));
        unbindService(mServiceConnection);
        mServiceBound = false;
        btnStart.setText(R.string.start);
    }
    //-------------------------------------send failed msg
    private void failed(){
        try {
            Message msg=new Message();
            msg.replyTo = mMessenger;
            msg.what=GameControllerService.MSG_FAILED_USER;
            mService.send(msg);

        } catch (RemoteException e) {

        }
    }
    //-------------------------------------send client score
    private void sendScore(Messenger messenger,int score){
        try {
            Message msg=new Message();//??
            msg.replyTo = mMessenger;
            msg.what=GameControllerService.MSG_CLIENT_SCORE;
            msg.arg1=score;
            mService.send(msg);

        } catch (RemoteException e) {

        }
    }
    //-------------------------------------create random int
    private int getRandom(){
        Random r = new Random();
        return r.nextInt(2);
    }
    //-------------------------------------show cone randomly
    private void showTrafficCones(){
        int r=getRandom();
        switch (r){
            case 0:
                if (!coneRight.getIsRunning()) {
                    coneLeft.moveDown();
                }
                break;
            case 1:
                if (!coneLeft.getIsRunning()) {
                    coneRight.moveDown();
                }
                break;
        }

    }
    //-------------------------------------stop game
    private void stopGame(){
        isStop=true;
//        if (timerTrafficCone!=null) {
//            timerTrafficCone.cancel();
//        }
    }
    //-------------------------------------start game
    private void startGame(){
       // Log.d("TAG", "startGame: ");
        if (timerTrafficCone==null) timerTrafficCone=new Timer();
    //    if (timerTask==null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isStop) {
                                showTrafficCones();
                                if (car.getX() - coneLeft.getX() <= coneLeft.getWidth() && car.getY() - coneLeft.getY() <= coneLeft.getHeight()) {
                                    coneLeft.changeImage();
                                    int s = score.getValue() - 1;
                                    score.postValue(score.getValue() - 1);
                                }
                                if (car.getX() - coneRight.getX() <= coneRight.getWidth() && car.getY() - coneRight.getY() <= coneRight.getHeight()) {
                                    coneRight.changeImage();
                                    score.postValue(score.getValue() - 1);
                                }
                            }
                        }
                    });
                }
            };
            try {
                timerTrafficCone.schedule(timerTask, 0, 1000);
            }catch (Exception ex){

            }
     //   }
    }
    //-------------------------------------handle service connection
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
            mService = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceBound = true;

            mService = new Messenger(service);
            try {
                Message msg=new Message();
                msg.replyTo = mMessenger;
                msg.what=GameControllerService.MSG_START_GAME;
                mService.send(msg);

            } catch (RemoteException e) {

            }


        }
    };
    //-----------------------------------handle message
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GameControllerService.MSG_RUNNING:
                    try {
                        isStop=false;
                        status.setValue("running");
                        startGame();
                    }catch (Exception ex){

                    }
                    break;
                case GameControllerService.MSG_SEND_DATA:
                    Bundle bundle= (Bundle) msg.obj;
                    Float value=bundle.getFloat(GameControllerService.CONTROLLER_VALUE);
                    if (value>=0 && value<=1){
                        car.move(Car.LEFT);
                    }else{
                        car.move(Car.RIGHT);
                    }
                    break;
                case GameControllerService.MSG_REST:
                    status.setValue("rest");
                    stopGame();
                    break;
                case GameControllerService.MSG_STOPPED:
                    status.setValue("stop");
                    doUnbindService();
                    stopGame();
                    break;
                case GameControllerService.MSG_FAILED_SERVICE:
                    status.setValue("failed");
                    doUnbindService();
                    stopGame();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}