package com.jagger.smartviewlibrary;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 手势触控布局, 布局中所有子View跟随布局改变;
 * 支持下滑退出，退回上一层位置(前提要把Activity背景设为透明)；双指缩放；双击缩放；支持 ViewPage;
 * 提供手势开关，事件回调接口
 *
 * 缺点：
 * ViewPage 会截取横移手势，所以在ViewPage中, 初始状态下横向双指缩放会触发ViewPage滑动
 *
 * @author Jagger 2021-1-12
 */
public class SmartTouchLayout extends FrameLayout implements GestureDetector.OnGestureListener {

    private String TAG = "Jagger--->";          //把"//Log."替换为"Log."开打全局日志
    private final float MAX_SCALE = 4f;
    private final float MIN_SCALE = 0.7f;
    private final int DOUBLE_CLICK_TIME_OFFSET = 300;   //ms
    private final int DRAG_DOWN_EXIT_Y_OFF = 300;       //下滑触发关闭Y距离

    //滑动
    private int originalLeft, originalRight, originalBottom;    // 原始边界
    //退出时需要缩放到的位置
    private boolean isSetEndViewLocal = false;        // 是否指定滑动退出位置
    private int endViewWidthSet, endViewHeightSet;    //结束动画飞回上一层Activity某个View的大小 设入
    private int endViewLeftSet, endViewTopSet;        //结束动画飞回上一层Activity某个View的 getLeft,getTop位置 设入
    private int endViewLeft, endViewTop;              //结束动画飞回上一层Activity某个View的 getLeft,getTop位置 计算后
    private float endViewScale;//, endViewScaleY;   // 求出上一层Activity某个View的缩放比例
    private EndViewScaleSide endViewScaleSide;
    public enum EndViewScaleSide {
        Width,
        Height
    }

    private int screenHeight , screenWidth;     //设备屏幕高度
    private float oldX, oldY;                   //手机放在屏幕的坐标
    private float movX, movY;                   //移动中在屏幕上的坐标
    private float alphaPercent = 1f;            //背景颜色透明度
    private boolean isFinish = false;           //是否执行关闭页面的操作
    private onEventListener eventListener = null;
    private GestureDetector detector;

    //缩放
    //移动前两指直接的距离
    private float mCurrentScale = 1f;
    private ScaleGestureDetector mScaleDetector;

    //状态
    private boolean isMoving = false;
    private boolean isZooming = false;
    private boolean isLockMoveInZooming = false;

    //开关
    private boolean isMoveExitEnable = true;
    private boolean isZoomEnable = true;

    //接口
    public interface onEventListener {
        /**
         * 当下滑触发 Activity finish()
         */
        void onActivityFinish();

        /**
         * 当 View 销毁时
         */
        void onViewDestroy();
    }

    public SmartTouchLayout(Context context) {
        super(context);
        initView(context, null, 0);
    }

    public SmartTouchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public SmartTouchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        screenHeight = getScreenHeight(context);
        screenWidth = getScreenWidth(context);
        detector = new GestureDetector(context, this);
        endViewScaleSide = EndViewScaleSide.Width;
        //
        setScaleDetector();
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    private int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ((Activity) context).getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        }else{
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     * 实际显示区域指定包含系统装饰的内容的显示部分 ： getRealSize（Point），getRealMetrics（DisplayMetrics）。
     * 应用程序显示区域指定可能包含应用程序窗口的显示部分，不包括系统装饰。 应用程序显示区域可以小于实际显示区域，因为系统减去诸如状态栏之类的装饰元素所需的空间。 使用以下方法查询应用程序显示区域：getSize（Point），getRectSize（Rect）和getMetrics（DisplayMetrics）。
     * @param context
     * @return
     */
    private int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ((Activity) context).getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        }else{
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        return dm.heightPixels;
    }

    //-------------------------------- 公开接口 start --------------------------------
    /**
     * 设置滑动完成后的回调方法
     */
    public void setEventListener(onEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * 设置结束时，动画回到什么位置和大小
     * @param w    view.getWidth()  结束时的宽
     * @param h    view.getHeight() 结束时的高
     * @param left view location[0] 结束时相对屏幕的X坐标
     * @param top  view location[1] 结束时相对屏幕的Y坐标
     * @param scaleSide             结束时以宽/高拉伸
     */
    public void setEndViewLocalSize(int w, int h, int left, int top, EndViewScaleSide scaleSide) {
        endViewWidthSet = w;
        endViewHeightSet = h;
        endViewLeftSet = left;
        endViewTopSet = top;
        isSetEndViewLocal = true;
        endViewScaleSide = scaleSide;
    }

    public boolean isMoveExitEnable() {
        return isMoveExitEnable;
    }

    /**
     * 能否滑动关闭
     * @param moveExitEnable
     */
    public void setMoveExitEnable(boolean moveExitEnable) {
        isMoveExitEnable = moveExitEnable;
    }

    public boolean isZoomEnable() {
        return isZoomEnable;
    }

    /**
     * 是否缩放
     * @param zoomEnable
     */
    public void setZoomEnable(boolean zoomEnable) {
        isZoomEnable = zoomEnable;
    }

    //-------------------------------- 公开接口 end --------------------------------

    //-------------------------------- 触摸事件分发 start --------------------------------

    private static final int TOUCH_TO_CHILDREN = 0;
    private static final int TOUCH_MYSELF = 1;
    private int mTouchState = TOUCH_TO_CHILDREN;
    private int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop(); // 触发移动的像素距?
    private long firstTouchTime = 0;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        if(action == MotionEvent.ACTION_MOVE && mTouchState == TOUCH_MYSELF){
            //Log.i(TAG, "拦截 为自己处理");
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //判断单双击
                checkClickDown(ev);

                //
                oldX = ev.getRawX();
                oldY = ev.getRawY();
                mTouchState = (isZooming || isMoving) ? TOUCH_MYSELF : TOUCH_TO_CHILDREN;
                //Log.e(TAG, "onInterceptTouchEvent ACTION_DOWN oldX:" + oldX + ",mTouchState:" + mTouchState);
                break;
            case MotionEvent.ACTION_MOVE:
                // 是否进行了滑动，设置滑动状态
                float tMoveX = ev.getRawX() - oldX;
                final float xDiff = Math.abs(tMoveX);

                float tMoveY = ev.getRawY() - oldY;
                final float yDiff = Math.abs(tMoveY);

                if (yDiff > mTouchSlop || xDiff > mTouchSlop) {
                    mTouchState = TOUCH_MYSELF;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_TO_CHILDREN;
                break;
        }

        // origin do
        return mTouchState != TOUCH_TO_CHILDREN;
    }

    //-------------------------------- 触摸事件分发 end --------------------------------


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 都不需要直接返回
        if(!isZoomEnable && !isMoveExitEnable){
            return true;
        }

        //可缩放
        if(isZoomEnable){
            mScaleDetector.onTouchEvent(event);
        }

        if (!mScaleDetector.isInProgress()) {
            detector.onTouchEvent(event);

            Log.e(TAG, "onTouchEvent: " + event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    oldX = event.getRawX();
                    oldY = event.getRawY();
                    //Log.e(TAG, "onTouchEvent ACTION_DOWN oldX:" + oldX);
                    break;
                case MotionEvent.ACTION_MOVE:
                    isFinish = false;
                    movX = event.getRawX() - oldX;
                    movY = event.getRawY() - oldY;
                    //Log.e(TAG, "onTouchEvent ACTION_MOVE movX:" + movX);
                    if(isZooming){
                        //缩放时， 自己处理MOVE事件
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }else {
                        //非缩放时， 由父控件处理MOVE事件
                        getParent().requestDisallowInterceptTouchEvent(false);

                        // 如果父不处理，且不是缩放过程中由双指变成单指手势，则由自己处理MOVE事件
                        if(!isLockMoveInZooming){
                            if(!isMoving && movY < 0) {
                                //不在滑动过程中的向上滑动手势 不接收
                                break;
                            }
                            animMoving(movX, movY);
                        }
                    }

                    if (Math.abs(movX) > Math.abs(movY)) {
                        if (movX < 0) {
//                            //Log.e(TAG, "左滑动");
                        } else {
//                            //Log.e(TAG, "右滑动");
                        }
                    } else {
                        if (movY < 0) {
//                            //Log.e(TAG, "上滑动");
                        } else {
//                            //Log.e(TAG, "下滑动");

                            //可下滑关闭
                            if(isMoveExitEnable && !isZooming && !isLockMoveInZooming) {
                                //下滑超出屏幕多少
//                                int[] location = new int[2];
//                                getLocationOnScreen(location);
//                                //Log.e(TAG, "下滑动:" + location[1] + ",退出界线" + (screenHeight * 6 / 10));
//                                if (location[1] > (screenHeight * 6 / 10)) {
//                                    isFinish = true;
//                                }

                                //下滑距离
                                if(movY > DRAG_DOWN_EXIT_Y_OFF){
                                    isFinish = true;
                                }
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_2_UP:
                case MotionEvent.ACTION_POINTER_2_DOWN:
                case MotionEvent.ACTION_POINTER_UP:
                    //双指状态下， 其中一只手指松开， 不处理滑动
                    //Log.e(TAG, "onTouchEvent  isZooming:" + isZooming);
                    isLockMoveInZooming = true;
                    break;
                case MotionEvent.ACTION_UP:
                    // 点击松开 不需要break
                    checkClickUp(event.getRawX(), event.getRawY());
                case MotionEvent.ACTION_CANCEL:
                    if (movX > mTouchSlop || movY > mTouchSlop) {
                        //滑动松开
                        if (isFinish) {
                            isFinish = false;
                            //Log.e(TAG, "页面返回");
                            animEnding();
                        } else {

                            if(isZooming){
                                checkBorder();
                            }else{
                                animRecovering();
                            }

                        }
                        isMoving = false;
                        isLockMoveInZooming = false;
                    }
//                    else{
//                        // 点击松开
//                        checkClickUp(event.getRawX(), event.getRawY());
//                    }
                    break;
            }
        }
        return true;
    }

    //-------------------------------- 动画 start --------------------------------

    /**
     * 非缩放状态下，还原位置
     */
    private void animRecovering() {
//        Log.i(TAG , "animRecovering");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            animate().setDuration(200)
                    .scaleX(1)
                    .scaleY(1)
                    .translationX(0)    //移到某个坐标 相对于自己原始坐标
                    .translationY(0)
                    .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (alphaPercent < animation.getAnimatedFraction()) {
                                ((ViewGroup) getParent()).setBackgroundColor(convertPercentToBlackAlphaColor(animation.getAnimatedFraction()));
                            }
                        }
                    })
                    .start();
        }else{
            animate().setDuration(200)
                    .scaleX(1)
                    .scaleY(1)
                    .translationX(0)    //移到某个坐标 相对于自己原始坐标
                    .translationY(0)
                    .start();
            ((ViewGroup) getParent()).setBackgroundColor(convertPercentToBlackAlphaColor(100));
        }
    }

    /**
     * 非缩放状态下，滑动
     * @param deltaX
     * @param deltaY
     */
    private void animMoving(float deltaX, float deltaY) {
        if (Math.abs(movY) < (screenHeight / 4)) {
            float scale = 1 - Math.abs(movY) / screenHeight;
            alphaPercent = 1 - Math.abs(deltaY) / (screenHeight / 2);

            setScaleX(scale);
            setScaleY(scale);
            ((ViewGroup) getParent()).setBackgroundColor(convertPercentToBlackAlphaColor(alphaPercent));
        }
        //Log.e(TAG, "setupMoving x:" + deltaX + ",y:" + deltaY);
        setTranslationX( deltaX);
        setTranslationY( deltaY);
        isMoving = true;
    }

    /**
     * 缩放状态下，回弹到边缘
     * @param deltaX
     * @param deltaY
     */
    private void animZoomingMoveToBorder(float deltaX, float deltaY) {
        animate().setDuration(200)
                .translationX(deltaX)    //移到某个坐标 相对于自己原始坐标
                .translationY(deltaY)
                .start();
    }

    /**
     * 缩放状态下，回弹到水平居中
     */
    private void animZoomingMoveToHorizontalCenter(){
        moveY4Zooming = 0;
        animate().setDuration(200)
                .translationY(0)
                .start();
    }

    /**
     * 下滑关闭， 回弹到指定位置
     */
    private void animEnding() {
        Log.i(TAG , "animEnding");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            animate().setDuration(250)
                    .scaleX(endViewScale)
                    .scaleY(endViewScale)
                    .translationX(endViewLeft)    //移到某个坐标
                    .translationY(endViewTop)
                    .alpha(30)
                    .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (0 < animation.getAnimatedFraction() || animation.getAnimatedFraction() < 1) {
                                float endAlphaPercent = (1 -animation.getAnimatedFraction());
                                if(endAlphaPercent < alphaPercent){
                                    ((ViewGroup) getParent()).setBackgroundColor(convertPercentToBlackAlphaColor(endAlphaPercent));
                                }
                            }
                        }
                    })
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            onDestroy();
                            finishActivity();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    })
                    .start();
        }else{
            animate().setDuration(1000)
                    .scaleX(endViewScale)
                    .scaleY(endViewScale)
                    .translationX(endViewLeft)    //移到某个坐标
                    .translationY(endViewTop)
                    .alpha(30)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            onDestroy();
                            finishActivity();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    })
                    .start();
        }
    }

    /**
     *  双击，放大动画
     */
    private void animZoomingDoubleClick() {
        animate().setDuration(200)
                .scaleX(mCurrentScale)
                .scaleY(mCurrentScale)
                .translationX(moveX4Zooming)    //移到某个坐标 相对于自己原始坐标
                .translationY(moveY4Zooming)
                .start();
    }

    //-------------------------------- 动画 end --------------------------------

    /**
     * 设置背景颜色透明度
     */
    protected int convertPercentToBlackAlphaColor(float percent) {
        percent = Math.min(1, Math.max(0, percent));
        int intAlpha = (int) (percent * 255);
        String stringAlpha = Integer.toHexString(intAlpha).toLowerCase();
        String color = "#" + (stringAlpha.length() < 2 ? "0" : "") + stringAlpha + "000000";
        return Color.parseColor(color);
    }

    //-------------------------------- 缩放处理 start --------------------------------

    /**
     * 设入缩放Detector
     */
    private void setScaleDetector(){
        ScaleGestureDetector.OnScaleGestureListener scaleListener = new ScaleGestureDetector
                .SimpleOnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();

                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                    return false;

                //双指缩放中
                isZooming = true;
                mCurrentScale *= scaleFactor;
                if(mCurrentScale < MIN_SCALE){
                    mCurrentScale = MIN_SCALE;
                }
                setScaleX(mCurrentScale);
                setScaleY(mCurrentScale);

                //返回 true 会一闪一闪的
                return false;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                super.onScaleEnd(detector);

                //Log.i(TAG, "onScaleEnd" );
                scaleEnd();
            }

        };
        mScaleDetector = new ScaleGestureDetector(getContext(), scaleListener);
    }

    /**
     *
     */
    private void scaleEnd(){
        if (mCurrentScale <= 1f) {
            reset();
            isZooming = false;
        }else if (mCurrentScale > MAX_SCALE) {
            setMaxSize();
        }
    }

    /**
     * 缩小过于小时，回复原来大小
     */
    private void reset() {
        mCurrentScale = 1f;
        setScaleX(mCurrentScale);
        setScaleY(mCurrentScale);
        isZooming = false;
    }

    /**
     * 放大过于大时，回复最大比例
     */
    private void setMaxSize() {
        mCurrentScale = MAX_SCALE;
        setScaleX(mCurrentScale);
        setScaleY(mCurrentScale);
    }

    /**
     * 检查边界是否移到边界以内
     */
    private void checkBorder() {
        int[]location = new int[2];
        getLocationOnScreen(location);
        //Log.i(TAG, "checkBorder 当前X:" + location[0] + ",移动过的X:" + moveX4Zooming);

        float overRightOffset = location[0] - (mCurrentScale*getWidth() - originalRight)*-1;
        float overBottomOffset = location[1] - (mCurrentScale*getHeight() - originalBottom)*-1;

        if(location[0] > 0){
            //是否越入左边界
            moveX4Zooming = ((1-mCurrentScale) * getWidth())/2 * -1;   //放大后相对于 原来大小的 X 坐标的偏移
            //是否越入上,下边界
            checkTopBottomBorder(location[1], overBottomOffset);
            animZoomingMoveToBorder(moveX4Zooming, moveY4Zooming);
        }else if(overRightOffset < 0){
            //是否越入右边界
            moveX4Zooming += Math.abs(overRightOffset);   //放大前的 X=0
            //是否越入上,下边界
            checkTopBottomBorder(location[1], overBottomOffset);
            animZoomingMoveToBorder(moveX4Zooming, moveY4Zooming);
        }else{
            //是否越入上,下边界
            checkTopBottomBorder(location[1], overBottomOffset);
            animZoomingMoveToBorder(moveX4Zooming, moveY4Zooming);
        }
    }

    /**
     * 是否越入上，下边界
     * @param top
     * @param bottomOffset
     */
    private void checkTopBottomBorder(float top, float bottomOffset) {
        //是否越入右、上边界
        if(top > 0){
            //是否越入上边界
            moveY4Zooming = ((1-mCurrentScale) * getHeight())/2 * -1;   //放大后相对于 原来大小的 Y 坐标的偏移
        }else if(bottomOffset < 0){
            //是否越入下边界
            moveY4Zooming += Math.abs(bottomOffset);   //放大前的 Y=0
        }
    }

    //缩放中时移动的X,Y坐标跟随缩放比例改变，跟非缩放状态的movX, movY分开处理
    private float moveX4Zooming = 0 , moveY4Zooming = 0;
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(isZooming && !isLockMoveInZooming){
            //缩放中的滑动处理
            if(e1 != null && e2 != null){
                //Log.i(TAG, "onScroll e1 X:" + e1.getX() + ",e2 X:" + e2.getX());
                //Log.i(TAG, "onScroll e1 Y:" + e1.getY() + ",e2 Y:" + e2.getY());
                this.moveX4Zooming = this.moveX4Zooming - (e1.getX()-e2.getX());    //e1.getX() 相对于VIEW自身的坐标系的X坐标
                this.moveY4Zooming =  this.moveY4Zooming - (e1.getY()-e2.getY());
                //Log.i(TAG, "onScroll dx x:" + moveX4Zooming + ",dy:" + moveY4Zooming);
                setTranslationX(moveX4Zooming);
                setTranslationY(moveY4Zooming);
            }
        }
        return true;
    }

    //-------------------------------- 缩放处理 end --------------------------------


    //-------------------------------- 单,双击处理 start --------------------------------

    private float clickXdown, clickYdown, firstClickX, firstClickY, secondClickX, secondClickY;
    // 统计?ms内的点击次数
    private TouchEventCountThread mInTouchEventCount = new TouchEventCountThread();
    // 根据TouchEventCountThread统计到的点击次数, perform单击还是双击事件
    private TouchEventHandler mTouchEventHandler = new TouchEventHandler();

    private void checkClickDown(MotionEvent ev){
        if (0 == mInTouchEventCount.touchCount) { // 第一次按下时,开始统计
            //Log.i(TAG , "checkClickDown 第一次按下时,开始统计" );
            postDelayed(mInTouchEventCount, DOUBLE_CLICK_TIME_OFFSET);
            clickXdown = ev.getRawX();
            clickYdown = ev.getRawY();
        }
    }

    private void checkClickUp(float clickX, float clickY){
        Log.i(TAG , "checkClickUp clickX:" + clickX + ",clickY:" + clickY);
        // 一次点击事件要有按下和抬起, 有抬起必有按下, 所以只需要在ACTION_UP中处理
        if (!mInTouchEventCount.isLongClick) {

            // 点下 和 松开 的距离判断，以免快速滑动导致误判为点击
            if(Math.abs(clickXdown - clickX) > 60 || Math.abs(clickYdown - clickY) > 60) {
                return;
            }

            // 累加点击数
            mInTouchEventCount.touchCount++;

            if(mInTouchEventCount.touchCount == 1){
                firstClickX = clickX;
                firstClickY = clickY;
                Log.i(TAG , "checkClickUp 点击第一下");
            }else if(mInTouchEventCount.touchCount == 2){
                secondClickX = clickX;
                secondClickY = clickY;

                float xOff = Math.abs(firstClickX - secondClickX);
                float yOff = Math.abs(firstClickY - secondClickY);
                //两次点击距离相近
                if(xOff < 60 && yOff < 60 ){
                    //Double click 成立
                    //Log.i(TAG , "checkClickUp Double click 成立");
                }else{
                    //Double click 不成立，当单击处理
                    mInTouchEventCount.touchCount = 1;
//                    Log.i(TAG , "checkClickUp Double click 不成立，当单击处理");
                }
            }else{
                mInTouchEventCount.touchCount = 0;
                //Log.i(TAG , "checkClickUp 复原");
            }
        }else {
            // 长按复原
            mInTouchEventCount.isLongClick = false;
            //Log.i(TAG , "checkClickUp 长按复原");
        }
    }

    private class TouchEventCountThread implements Runnable {
        public int touchCount = 0;
        public boolean isLongClick = false;

        @Override
        public void run() {
            Message msg = new Message();
            if(0 == touchCount){ // long click
                isLongClick = true;
            } else {
                msg.arg1 = touchCount;
                mTouchEventHandler.sendMessage(msg);
                touchCount = 0;
            }
//            Log.i(TAG , "TouchEventCountThread 结束:" + touchCount);
        }
    }

    private class TouchEventHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "touch " + msg.arg1 + " time.");
            if(msg.arg1 == 1){
                onSingleClicked(oldX, oldY);
            }else{
                onDoubleClicked(oldX, oldY);
            }
        }
    }

    /**
     * 单击事件
     * @param clickX
     * @param clickY
     */
    private void onSingleClicked(float clickX, float clickY){
        Log.i(TAG , "onSingleClicked isZooming:" + isZooming + ",isLockMoveInZooming:" + isLockMoveInZooming);
        if(!isZooming && !isLockMoveInZooming){
//            isFinish = true;
            animEnding();
        }
    }

    /**
     * 双击事件
     * @param clickX
     * @param clickY
     */
    private void onDoubleClicked(float clickX, float clickY){
        if(mCurrentScale < MAX_SCALE){
            mCurrentScale ++;
            if(mCurrentScale > MAX_SCALE){
                mCurrentScale = MAX_SCALE;
            }
        }else{
            mCurrentScale = 1.0f;
        }
        isZooming = true;

        if (mCurrentScale > 1) {
            this.moveX4Zooming = this.moveX4Zooming - (clickX - getWidth() / 2f); //双击VIEW中的X与VIEW中点的距离
            this.moveY4Zooming = this.moveY4Zooming - (clickY - getHeight() / 2f);
            //            this.moveY4Zooming = 0;
            //Logd(TAG, "onDoubleClicked--- clickX:" + clickX + ",getWidth()/2f:" + getWidth()/2f + ",moveX4Zooming:" + moveX4Zooming);
            animZoomingDoubleClick();
        } else {
            this.moveX4Zooming = 0;
            this.moveY4Zooming = 0;
            animZoomingDoubleClick();
            isZooming = false;
        }
    }

    //-------------------------------- 单,双击处理 end --------------------------------
    /**
     * 通知销毁VIEW
     */
    private void onDestroy(){
        if (eventListener != null) {
            eventListener.onViewDestroy();
        }
    }

    /**
     * 退出Activity
     */
    private void finishActivity(){
        if(eventListener != null){
            eventListener.onActivityFinish();
        }
        ((Activity)(getContext())).finish();
        ((Activity)(getContext())).overridePendingTransition(0,0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        ((ViewGroup) getParent()).setBackgroundColor(convertPercentToBlackAlphaColor(100));

        // 如果不指定滑动退出位置则缩小为原来的70%滑到屏幕底部退出
        if(!isSetEndViewLocal){
            endViewWidthSet = (int)(getMeasuredWidth() * 0.7);
            endViewHeightSet = (int)(getMeasuredHeight() * 0.7);
        }

        //计算结束动画缩放比例
//        endViewScale = endViewWidthSet*1.0f / getMeasuredWidth();
//        endViewScaleY = endViewHeightSet*1.0f / getMeasuredHeight();
        if(endViewScaleSide == EndViewScaleSide.Width){
            endViewScale = endViewWidthSet*1.0f / getMeasuredWidth();
        }else{
            endViewScale = endViewHeightSet*1.0f / getMeasuredHeight();
        }

        //Log.d(TAG, "getMeasuredWidth:" + getMeasuredWidth() + ",getMeasuredHeight:" + getMeasuredHeight());
        //Log.d(TAG, "endViewScaleX:" + endViewScaleX + ",endViewScaleY:" + endViewScaleY);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //当前VIEW处于屏幕坐标(如果在ViewPage中使用，X则处于屏幕外)
        int[]location = new int[2];
        getLocationOnScreen(location);
//        Log.d(TAG, "onLayout getX:" + location[0] + ",getY:" + location[1] + ",top:" + top);

        //自己处于屏幕的Y - 自己处于Activity的Y，计算出Activity左上角Y坐标，不会因为是否有TitleBar、ActionBar以致结束的位置不准
        int activityYOff = Math.abs(location[1] - top) ;

        //相对于Activity的坐标
        //Logd(TAG, "-------- left:" + left + ",top:" + top + ",right:" + right);
        originalLeft = left;
        originalRight = right;
        originalBottom = bottom;

        // 如果不指定滑动退出位置则缩小为原来的70%滑到屏幕底部居垂直中位置退出
        if(!isSetEndViewLocal){
            endViewLeftSet = screenWidth/2 - endViewWidthSet/2;
            endViewTopSet = screenHeight;
        }

        //计算结束的VIEW相对于当前VIEW的位置偏移
        //Logd(TAG, "onLayout endViewX1:" + endViewLeftSet + ",endViewY1:" + endViewTopSet);
        endViewLeft = endViewLeftSet - left;
        endViewTop =  endViewTopSet - top;
        //Logd(TAG, "onLayout endViewX2:" + endViewLeft + ",endViewY2:" + endViewTop);

        // 减去缩放后长宽 (左上角重合)
        endViewLeft -= ((1- endViewScale) * getWidth())/2;
        endViewTop -= ((1-endViewScale) * getHeight())/2;
        //Logd(TAG, "onLayout endViewX3:" + endViewLeft + ",endViewY3:" + endViewTop);

        // 再减去缩放后长宽差 (中心点重合)
        endViewLeft -= ((endViewScale * getWidth()) - endViewWidthSet) /2 ;
        endViewTop -= ((endViewScale * getHeight()) - endViewHeightSet) /2 ;

        // 再减去Activity的Y相对于屏幕的偏移
        endViewTop -= activityYOff;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
//        //Log.i(TAG, "onSingleTapUp isDoubleClickedZooming:" + isDoubleClickedZooming + ",isZooming:" + isZooming);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

}
