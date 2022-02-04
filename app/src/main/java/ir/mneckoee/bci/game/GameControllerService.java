package ir.mneckoee.bci.game;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameControllerService extends Service {
    private static String LOG_TAG = "GameControllerService";
    public static String CONTROLLER_VALUE="controller_value";
    public static final int MSG_START_GAME=1;
    public static final int MSG_RUNNING=0x11;
    public static final int MSG_SEND_DATA=0x12;
    public static final int MSG_CLIENT_SCORE=0x02;
    public static final int MSG_REST= 0x13;
    public static final int MSG_STOPPED=0x14;
    public static final int MSG_FAILED_SERVICE=0x15;
    public static final int MSG_FAILED_USER=0x03;



//    private IBinder mBinder = new MyBinder();
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Timer timer,timerGame;
    private TimerTask task,taskGame;
    private int gameCounter;
    public GameControllerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gameCounter=0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        return mMessenger.getBinder();
       // return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
    //    return super.onUnbind(intent);
        if (timerGame!=null) timerGame.cancel();
        if (timer!=null) timer.cancel();
        gameCounter=0;
        return true;
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "in onDestroy");
        super.onDestroy();
        if (timerGame!=null) timerGame.cancel();
        if (timer!=null) timer.cancel();
        gameCounter=0;
    }
    //-----------------------------------------------------handle failed
    private void failed(int errorCode,String reason,Messenger messenger){
        Bundle bundle=new Bundle();
        bundle.putInt("error_code", errorCode);
        bundle.putString("reason", reason);
        sendMessage(messenger,MSG_FAILED_SERVICE,bundle);
    }
    //-----------------------------------------------------send message
    private void sendMessage(Messenger messenger,int what,Object obj){
        Message msg=new Message();
        msg.obj = obj;
        msg.what = what;
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    //-----------------------------------------------------create neuro data
    private Float neuroData(int min,int max){
        Random r = new Random();
        return (min + r.nextFloat() * (max - min));
    }
    //-----------------------------------------------------send data to activity
    private void sendData(Messenger messenger) {

        task = new TimerTask()  {
            @Override
            public void run() {
                Bundle bundle=new Bundle();
                bundle.putFloat(CONTROLLER_VALUE, neuroData(0,2));
                sendMessage(messenger,MSG_SEND_DATA,bundle);
            }
        };

        if (timer==null) timer = new Timer();
        //start after 3 sec
       timer.schedule(task, 0, 1000);


    }
    private void handleGame(Messenger messenger){
        if (timerGame==null) timerGame=new Timer();
        if (taskGame==null) {
            taskGame = new TimerTask() {
                @Override
                public void run() {
                    gameCounter++;
                    switch (gameCounter) {
                        case 2:
                        case 5:
                            //rest
                            rest(messenger);
                            break;
                        case 8:
                            //finish game
                            finishGame(messenger);
                            break;
                        default:
                            //play
                            play(messenger);
                            break;

                    }

                }
            };
        }

        timerGame.schedule(taskGame,0,30000);
    }
    //-----------------------------------------------------handle rest
    private void rest(Messenger messenger){
        sendMessage(messenger,MSG_REST,null);
        if (timer!=null)
            timer.cancel();
    }
    //-----------------------------------------------------handle finish game
    //stopped
    private void finishGame(Messenger messenger){
        sendMessage(messenger,MSG_STOPPED,null);
        if (timerGame!=null) timerGame.cancel();
        if (timer!=null) timer.cancel();
        gameCounter=0;
    }
    //-----------------------------------------------------handle play
    //running
    private void play(Messenger messenger)  {
        sendMessage(messenger,MSG_RUNNING,null);
        //start sending neuro data
        sendData(messenger);
    }
    //-----------------------------------------------------MessageHandler
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_START_GAME:
                    try {
                        msg.replyTo.send(Message.obtain(null,
                                MSG_RUNNING, msg.arg1, 0));
                        handleGame(msg.replyTo);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_CLIENT_SCORE:
                    //save score
                    break;
                case MSG_FAILED_USER:
                    failed(0,"",msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
//    //-----------------------------------------------------Class MyBinder
//    public class MyBinder extends Binder {
//        public GameControllerService getService() {
//            return GameControllerService.this;
//        }
//    }
}

