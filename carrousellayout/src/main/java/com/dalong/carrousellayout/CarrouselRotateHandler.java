package com.dalong.carrousellayout;


import android.os.Handler;
import android.os.Message;

/**
 * 旋转木马自动旋转控制handler
 * Created by dalong on 2016/11/12.
 */

public abstract class CarrouselRotateHandler extends Handler {
    //消息what
    public static final int mMsgWhat = 1000;
    //是否旋转
    private boolean isAutoRotation;
    //旋转事件间隔
    private  long mRotationTime;
    //消息对象
    private Message message;
    //旋转方向
    private CarrouselRotateDirection  mRotateDirection;

    public CarrouselRotateHandler(boolean isAutoRotation, int mRotationTime , int mRotateDirection) {
        this.isAutoRotation = isAutoRotation;
        this.mRotationTime = mRotationTime;
        this.mRotateDirection=mRotateDirection==0?CarrouselRotateDirection.clockwise:CarrouselRotateDirection.anticlockwise;
        message=createMessage();
        setAutoRotation(isAutoRotation);
    }

    /**
     * 消息处理
     * @param msg
     */
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case mMsgWhat:
                //如果自动旋转
                if(isAutoRotation){
                    //旋转通知
                    onRotating(mRotateDirection);
                    //再次发送消息  循环
                    sendMessage();
                }
                break;
        }
    }

    /**
     * 需要旋转通知方法
     */
    public abstract void onRotating(CarrouselRotateDirection mRotateDirection);

    /**
     * 创建消息对象
     * @return
     */
    private Message createMessage(){
        Message message=new Message();
        message.what=mMsgWhat;
        return  message;
    }

    /**
     * 发送消息
     */
    public void sendMessage(){
        //清除所有mMsgWhat的消息
        try {
            removeMessages(mMsgWhat);
        } catch (Exception e) {
        }
        message=createMessage();
        this.sendMessageDelayed(message,mRotationTime);

    }

    /**
     * 获取是否自动旋转
     * @return
     */
    public boolean isAutoRotation() {
        return isAutoRotation;
    }

    /**
     * 设置是否自动旋转
     * @param autoRotation
     */
    public void setAutoRotation(boolean autoRotation) {
        isAutoRotation = autoRotation;
        if(autoRotation){//如果需要旋转
            sendMessage();
        }else{//不需要旋转  需要清除所有消息队列中的消息
            removeMessages(mMsgWhat);
        }
    }

    /**
     * 获取旋转事件间隔
     * @return
     */
    public long getmRotationTime() {
        return mRotationTime;
    }

    /**
     * 设置旋转事件间隔
     * @param mRotationTime
     */
    public void setmRotationTime(long mRotationTime) {
        this.mRotationTime = mRotationTime;
    }

    /**
     * 获取旋转方向
     * @return
     */
    public CarrouselRotateDirection getmRotateDirection() {
        return mRotateDirection;
    }

    /**
     * 设置旋转方向
     * @param mRotateDirection
     */
    public void setmRotateDirection(CarrouselRotateDirection mRotateDirection) {
        this.mRotateDirection = mRotateDirection;
    }

}
