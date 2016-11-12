package com.dalong.carrousellayout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * 旋转木马布局
 * Created by dalong on 2016/11/12.
 */

public class CarrouselLayout  extends RelativeLayout{

    private Context mContext;
    //自动旋转 默认不自动
    private boolean mAutoRotation;

    //旋转间隔时间  默认设置为2秒
    private int mRotationTime;

    //旋转木马旋转半径  圆的半径
    private float mCarrouselR;

    //camera和旋转木马距离
    private float mDistance=2f*mCarrouselR;

    //旋转方向 分0顺时针和 1逆时针 俯视旋转木马看
    private int mRotateDirection;

    //handler
    private CarrouselRotateHandler mHandler;

    //手势处理
    private GestureDetector mGestureDetector;

    //x旋转
    private int mRotationX;

    //Z旋转
    private int mRotationZ;

    //旋转的角度
    private float mAngle = 0;

    //旋转木马子view
    private List<View> mCarrouselViews=new ArrayList<>();

    //旋转木马子view的数量
    private int  viewCount;

    //半径扩散动画
    private ValueAnimator mAnimationR;

    //记录最后的角度 用来记录上一次取消touch之后的角度
    private float mLastAngle;

    //是否在触摸
    private boolean isTouching;

    //旋转动画
    private ValueAnimator restAnimator;

    //选中item
    private int selectItem;

    //item选中回调接口
    private OnCarrouselItemSelectedListener mOnCarrouselItemSelectedListener;

    //item点击回调接口
    private OnCarrouselItemClickListener mOnCarrouselItemClickListener;

    //x轴旋转动画
    private ValueAnimator xAnimation;

    //z轴旋转动画
    private ValueAnimator zAnimation;

    public CarrouselLayout(Context context) {
        this(context,null);
    }

    public CarrouselLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CarrouselLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext=context;
        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.CarrouselLayout);
        mAutoRotation=typedArray.getBoolean(R.styleable.CarrouselLayout_autoRotation,false);
        mRotationTime=typedArray.getInt(R.styleable.CarrouselLayout_rotationTime,2000);
        mCarrouselR=typedArray.getDimension(R.styleable.CarrouselLayout_r,200);
        mRotateDirection=typedArray.getInt(R.styleable.CarrouselLayout_rotateDirection,0);
        typedArray.recycle();
        mGestureDetector = new GestureDetector(context, getGestureDetectorController());
        initHandler();
    }

    /**
     * 初始化handler对象
     */
    private void initHandler() {
        mHandler=new CarrouselRotateHandler(mAutoRotation,mRotationTime,mRotateDirection) {
            @Override
            public void onRotating(CarrouselRotateDirection rotateDirection) {//接受到需要旋转指令
                try {
                    if (viewCount != 0) {//判断自动滑动从那边开始
                        int perAngle = 0;
                        switch (rotateDirection){
                            case clockwise:
                                perAngle = 360 /viewCount;
                                break;
                            case anticlockwise:
                                perAngle = -360/viewCount;
                                break;
                        }
                        if (mAngle == 360) {
                            mAngle = 0f;
                        }
                        startAnimRotation(mAngle + perAngle, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }



    private GestureDetector.SimpleOnGestureListener getGestureDetectorController() {
        return new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //转换成弧度
                double radians= Math.toRadians(mRotationZ);
                //Math.cos(radians) 返回对应的radians弧度的余弦值
                mAngle+=Math.cos(radians)*(distanceX/4) + Math.sin(radians)*(distanceY/4);
                //初始化
                refreshLayout();
                return true;
            }
        };
    }

    /**
     * 初始化 计算平均角度后各个子view的位置
     */
    public  void refreshLayout() {
        for (int i=0;i<mCarrouselViews.size();i++){
            double radians = mAngle + 180 - i * 360 / viewCount;
            float x0 = (float) Math.sin(Math.toRadians(radians)) * mCarrouselR;
            float y0 = (float) Math.cos(Math.toRadians(radians)) * mCarrouselR;
            float scale0 = (mDistance - y0) / (mDistance + mCarrouselR);
            mCarrouselViews.get(i).setScaleX(scale0);
            mCarrouselViews.get(i).setScaleY(scale0);
            float rotationX_y = (float) Math.sin(Math.toRadians(mRotationX * Math.cos(Math.toRadians(radians)))) * mCarrouselR;
            float rotationZ_y = -(float) Math.sin(Math.toRadians(-mRotationZ)) * x0;
            float rotationZ_x = (((float) Math.cos(Math.toRadians(-mRotationZ)) * x0) - x0);

            mCarrouselViews.get(i).setTranslationX(x0 + rotationZ_x);
            mCarrouselViews.get(i).setTranslationY(rotationX_y + rotationZ_y);
        }
        List<View> arrayViewList =new ArrayList<>();
        arrayViewList.clear();
        for (int i=0;i<mCarrouselViews.size();i++){
            arrayViewList.add(mCarrouselViews.get(i));
        }
        sortList(arrayViewList);
        postInvalidate();
    }

    /**
     * 排序
     * 對子View 排序，然后根据变化选中是否重绘,这样是为了实现view 在显示的时候来控制当前要显示的是哪三个view，可以改变排序看下效果
     * @param list
     */
    @SuppressWarnings("unchecked")
    private <T> void sortList(List<View> list) {
        @SuppressWarnings("rawtypes")
        Comparator comparator = new SortComparator();
        T[] array = list.toArray((T[]) new Object[list.size()]);
        Arrays.sort(array, comparator);
        int i = 0;
        ListIterator<T> it = (ListIterator<T>) list.listIterator();
        while (it.hasNext()) {
            it.next();
            it.set(array[i++]);
        }
        for (int j = 0; j < list.size(); j++) {
            list.get(j).bringToFront();
        }
    }

    /**
     * 筛选器
     */
    private class SortComparator implements Comparator<View> {
        @Override
        public int compare(View o1, View o2) {
            return (int) (1000 * o1.getScaleX() - 1000 * o2.getScaleX());
        }
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        refreshLayout();
        if (mAutoRotation) {
            mHandler.sendEmptyMessageDelayed(CarrouselRotateHandler.mMsgWhat, mHandler.getmRotationTime());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            checkChildView();
            startAnimationR();
        }
    }

    /**
     * 旋转木马半径打开动画
     */
    public void startAnimationR() {
        startAnimationR(1f, mCarrouselR);
    }

    /**
     * 旋转木马半径动画
     * @param isOpen 是否打开  否则关闭
     */
    public void startAnimationR(boolean isOpen) {
        if (isOpen) {
            startAnimationR(1f, mCarrouselR);
        } else {
            startAnimationR(mCarrouselR, 1f);
        }
    }

    /**
     * 半径扩散、收缩动画 根据设置半径来实现
     * @param from
     * @param to
     */
    public void startAnimationR(float from, float to) {
        mAnimationR = ValueAnimator.ofFloat(from, to);
        mAnimationR.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCarrouselR = (Float) valueAnimator.getAnimatedValue();
                refreshLayout();
            }
        });
        mAnimationR.setInterpolator(new DecelerateInterpolator());
        mAnimationR.setDuration(2000);
        mAnimationR.start();
    }

    public void checkChildView(){
        //先清空views里边可能存在的view防止重复
        for (int i = 0; i < mCarrouselViews.size(); i++) {
            mCarrouselViews.remove(i);
        }
        final int count = getChildCount(); //获取子View的个数
        viewCount = count;
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i); //获取指定的子view
            final int position = i;
            mCarrouselViews.add(view);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnCarrouselItemClickListener!=null){
                        mOnCarrouselItemClickListener.onItemClick(view,position);
                    }
                }
            });

        }

    }

    /**
     * 复位
     */
    private void restView() {
        if (viewCount == 0) {
            return;
        }
        float resultAngle = 0;
        //平均角度
        float averageAngle = 360 / viewCount;
        if (mAngle < 0) {
            averageAngle = -averageAngle;
        }
        float minvalue = (int) (mAngle / averageAngle) * averageAngle;//最小角度
        float maxvalue = (int) (mAngle / averageAngle) * averageAngle + averageAngle;//最大角度
        if (mAngle >= 0) {//分为是否小于0的情况
            if (mAngle - mLastAngle > 0) {
                resultAngle = maxvalue;
            } else {
                resultAngle = minvalue;
            }
        } else {
            if (mAngle - mLastAngle < 0) {
                resultAngle = maxvalue;
            } else {
                resultAngle = minvalue;
            }
        }
        startAnimRotation(resultAngle, null);
    }


    /**
     * 动画旋转
     * @param resultAngle
     * @param complete
     */
    private void startAnimRotation(float resultAngle, final Runnable complete) {
        if (mAngle == resultAngle) {
            return;
        }
        restAnimator = ValueAnimator.ofFloat(mAngle, resultAngle);
        //设置旋转匀速插值器
        restAnimator.setInterpolator(new LinearInterpolator());
        restAnimator.setDuration(300);
        restAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isTouching == false) {
                    mAngle = (Float) animation.getAnimatedValue();
                    refreshLayout();
                }
            }
        });
        restAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isTouching == false) {
                    selectItem = calculateItem();
                    if (selectItem < 0) {
                        selectItem = viewCount + selectItem;
                    }
                    if (mOnCarrouselItemSelectedListener != null) {
                        mOnCarrouselItemSelectedListener.selected(mCarrouselViews.get(selectItem),selectItem);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (complete != null) {
            restAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    complete.run();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        restAnimator.start();
    }

    /**
     * 通过角度计算是第几个item
     *
     * @return
     */
    private int calculateItem() {
        return (int) (mAngle / (360 / viewCount)) % viewCount;
    }

    /**
     * 触摸操作
     *
     * @param event
     * @return
     */
    private boolean onTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mLastAngle = mAngle;
            isTouching = true;
        }
        boolean result = mGestureDetector.onTouchEvent(event);
        if (result) {
            this.getParent().requestDisallowInterceptTouchEvent(true);//通知父控件勿拦截本控件
        }
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            isTouching = false;
            restView();
            return true;
        }
        return true;
    }


    /**
     * 触摸方法
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setCanAutoRotation(event);
        return true;
    }


    /**
     * 触摸停止计时器
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        onTouch(ev);
        setCanAutoRotation(ev);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 触摸时停止自动加载
     * @param event
     */
    public void  setCanAutoRotation(MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopAutoRotation();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resumeAutoRotation();
                break;
        }
    }

    /**
     * 停止自动加载
     */
    public  void stopAutoRotation(){
        if (mHandler!=null&&mAutoRotation) {
            mHandler.removeMessages(CarrouselRotateHandler.mMsgWhat);
        }
    }

    /**
     * 从新启动自动加载
     */
    public  void resumeAutoRotation(){
        if (mHandler!=null&&mAutoRotation) {
            mHandler.sendEmptyMessageDelayed(CarrouselRotateHandler.mMsgWhat,  mHandler.getmRotationTime());
        }
    }

    /**
     * 获取所有的view
     *
     * @return
     */
    public List<View> getViews() {
        return mCarrouselViews;
    }

    /**
     * 获取角度
     *
     * @return
     */
    public float getAngle() {
        return mAngle;
    }


    /**
     * 设置角度
     *
     * @param angle
     */
    public void setAngle(float angle) {
        this.mAngle = angle;
    }

    /**
     * 获取距离
     *
     * @return
     */
    public float getDistance() {
        return mDistance;
    }

    /**
     * 设置距离
     *
     * @param distance
     */
    public void setDistance(float distance) {
        this.mDistance = distance;
    }

    /**
     * 获取半径
     *
     * @return
     */
    public float getR() {
        return mCarrouselR;
    }

    /**
     * 获取选择是第几个item
     *
     * @return
     */
    public int getSelectItem() {
        return selectItem;
    }

    /**
     * 设置选中方法
     *
     * @param selectItem
     */
    public void setSelectItem(int selectItem) {
        if (selectItem >= 0) {
            float angle = 0;
            if (getSelectItem() == 0) {
                if (selectItem == mCarrouselViews.size() - 1) {
                    angle = mAngle - (360 / viewCount);
                } else {
                    angle = mAngle + (360 / viewCount);
                }
            } else if (getSelectItem() == mCarrouselViews.size() - 1) {
                if (selectItem == 0) {
                    angle = mAngle + (360 / viewCount);
                } else {
                    angle = mAngle - (360 / viewCount);
                }
            } else {
                if (selectItem > getSelectItem()) {
                    angle = mAngle + (360 / viewCount);
                } else {
                    angle = mAngle - (360 / viewCount);
                }
            }

            float resultAngle = 0;
            float part = 360 / viewCount;
            if (angle < 0) {
                part = -part;
            }
            //最小角度
            float minvalue = (int) (angle / part) * part;
            //最大角度
            float maxvalue = (int) (angle / part) * part;
            if (angle >= 0) {//分为是否小于0的情况
                if (angle - mLastAngle > 0) {
                    resultAngle = maxvalue;
                } else {
                    resultAngle = minvalue;
                }
            } else {
                if (angle - mLastAngle < 0) {
                    resultAngle = maxvalue;
                } else {
                    resultAngle = minvalue;
                }
            }

            if (viewCount > 0) startAnimRotation(resultAngle, null);
        }
    }

    /**
     * 设置半径
     *
     * @param r
     */
    public CarrouselLayout setR(float r) {
        this.mCarrouselR = r;
        mDistance = 2f * r;
        return  this;
    }

    /**
     * 选中回调接口实现
     *
     * @param mOnCarrouselItemSelectedListener
     */
    public void setOnCarrouselItemSelectedListener(OnCarrouselItemSelectedListener mOnCarrouselItemSelectedListener) {
        this.mOnCarrouselItemSelectedListener = mOnCarrouselItemSelectedListener;
    }

    /**
     * 点击事件回调
     *
     * @param mOnCarrouselItemClickListener
     */
    public void setOnCarrouselItemClickListener(OnCarrouselItemClickListener mOnCarrouselItemClickListener) {
        this.mOnCarrouselItemClickListener = mOnCarrouselItemClickListener;
    }


    /**
     * 设置是否自动切换
     *
     * @param autoRotation
     */
    public CarrouselLayout setAutoRotation(boolean autoRotation) {
        this.mAutoRotation = autoRotation;
        mHandler.setAutoRotation(autoRotation);
        return this;
    }

    /**
     * 获取自动切换时间
     *
     * @return
     */
    public long getAutoRotationTime() {
        return mHandler.getmRotationTime();
    }

    /**
     * 设置自动切换时间间隔
     *
     * @param autoRotationTime
     */
    public CarrouselLayout setAutoRotationTime(long autoRotationTime) {
        if(mHandler!=null)
            mHandler.setmRotationTime(autoRotationTime);
        return this;
    }

    /**
     * 是否自动切换
     *
     * @return
     */
    public boolean isAutoRotation() {
        return mAutoRotation;
    }

    /**
     * 设置自动选择方向
     * @param mCarrouselRotateDirection
     * @return
     */
    public CarrouselLayout setAutoScrollDirection(CarrouselRotateDirection mCarrouselRotateDirection) {
        if(mHandler!=null)
            mHandler.setmRotateDirection(mCarrouselRotateDirection);
        return this;
    }

    public void createXAnimation(int from, int to, boolean start){
        if(xAnimation!=null)if(xAnimation.isRunning()==true)xAnimation.cancel();
        xAnimation= ValueAnimator.ofInt(from,to);
        xAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRotationX= (Integer) animation.getAnimatedValue();
                refreshLayout();
            }
        });
        xAnimation.setInterpolator(new LinearInterpolator());
        xAnimation.setDuration(2000);
        if(start)xAnimation.start();
    }


    public ValueAnimator createZAnimation(int from, int to, boolean start){
        if(zAnimation!=null)if(zAnimation.isRunning()==true)zAnimation.cancel();
        zAnimation= ValueAnimator.ofInt(from,to);
        zAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRotationZ= (Integer) animation.getAnimatedValue();
                refreshLayout();
            }
        });
        zAnimation.setInterpolator(new LinearInterpolator());
        zAnimation.setDuration(2000);
        if(start)zAnimation.start();
        return zAnimation;
    }

    public CarrouselLayout setRotationX(int mRotationX) {
        this.mRotationX = mRotationX;
        return this;
    }

    public CarrouselLayout setRotationZ(int mRotationZ) {
        this.mRotationZ = mRotationZ;
        return this;
    }

    public float getRotationX() {
        return mRotationX;
    }

    public int getRotationZ() {
        return mRotationZ;
    }

    public ValueAnimator getRestAnimator() {
        return restAnimator;
    }

    public ValueAnimator getAnimationR() {
        return mAnimationR;
    }

    public void setAnimationZ(ValueAnimator zAnimation) {
        this.zAnimation = zAnimation;
    }

    public ValueAnimator getAnimationZ() {
        return zAnimation;
    }

    public void setAnimationX(ValueAnimator xAnimation) {
        this.xAnimation = xAnimation;
    }

    public ValueAnimator getAnimationX() {
        return xAnimation;
    }


}
