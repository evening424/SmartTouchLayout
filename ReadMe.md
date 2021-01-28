智能识别手势布局-SmartTouchLayout
============================

需求来源：
-------
产品需要实现跟微信朋友圈看大图可下滑退出的效果，<br>
但项目中不仅有大图，图中还有按钮，有文本，有视频等等，总之布局很复杂<br>
反正这些他不管，就是要下滑退出，再缩回上一层界面小图片位置。<br>
<br>
找了不少DEMO，基本都是只满足图片，视频实现这个功能， 那就自己来
<br>
<br>
<br>

SmartTouchLayout
-----------------
多手势识别的布局, 只要在布局里的VIEW，就支持：<br>
双指、双击缩放；单指滑动；下滑退出；单击退出；不影响子控件事件；与ViewPage不冲突；<br>

无脑调用：<br>
FrameLayout怎么用，它就怎么用。<br>
<br>
<br>
<br>

疑车不能无据，直接上图
-----------------

![](https://github.com/evening424/SmartTouchLayout/blob/master/ImageCache/Video_20210125_053733_974.gif)
<br>
双指、双击缩放；下滑退出到指定位置;
<br>
<br>

![](https://github.com/evening424/SmartTouchLayout/blob/master/ImageCache/Video_20210125_053832_721.gif)
<br>
不影响子控件事件；不指定位置时，下滑到底部消失;
<br>
<br>

![](https://github.com/evening424/SmartTouchLayout/blob/master/ImageCache/Video_20210125_053812_258.gif)
<br>
与ViewPage不冲突；
<br>
<br>
<br>

如何使用
-----------------
1.引用<br>

```
implementation 'com.jagger:SmartTouchLayout:1.0.2'
``` 


2.直接在layout.xml文件中使用<br>
  使用方式跟FrameLayout一样
  
```
<com.jagger.smartviewlibrary.SmartTouchLayout
        android:id="@+id/stl"
        android:layout_width="match_parent"
        android:layout_height="match_parent" >

        ...

</com.jagger.smartviewlibrary.SmartTouchLayout>
```

3.属性设置
<br>设置结束时动画飞到哪去，可指定位置和大小，效果如图1；不设置，则飞到底部如图2；
```
/**
 * 设置结束时，动画回到什么位置和大小
 * @param w    view.getWidth()  结束时的宽
 * @param h    view.getHeight() 结束时的高
 * @param left view location[0] 结束时相对屏幕的X坐标
 * @param top  view location[1] 结束时相对屏幕的Y坐标
 * @param scaleSide             结束时以宽/高拉伸
 */
public void setEndViewLocalSize(int w, int h, int left, int top, EndViewScaleSide scaleSide)
```
设置是否需要支持下滑关闭
```
smartTouchLayout.setMoveExitEnable(true);
```
设置是否需要支持缩放
```
smartTouchLayout.setZoomEnable(true);
```

4.最后要把归属的Activity设置为透明
```
<!-- AppCompatActivity设置透明主题 -->
<style name="MyTranslucentTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowBackground">@android:color/transparent</item>
        <item name="android:windowIsTranslucent">true</item>
</style>
```
<br>
<br>

代码解读
-----------------
通过计算点击的时间差，判断是单击还是双击

```java

private void checkClickDown(MotionEvent ev){
    if (0 == mInTouchEventCount.touchCount) { // 第一次按下时,开始统计
        //Log.i(TAG , "checkClickDown 第一次按下时,开始统计" );
        postDelayed(mInTouchEventCount, DOUBLE_CLICK_TIME_OFFSET);
    }
}

private void checkClickUp(float clickX, float clickY){
    //Log.i(TAG , "checkClickUp clickX:" + clickX + ",clickY:" + clickY);
    // 一次点击事件要有按下和抬起, 有抬起必有按下, 所以只需要在ACTION_UP中处理
    if (!mInTouchEventCount.isLongClick) {
        mInTouchEventCount.touchCount++;

        if(mInTouchEventCount.touchCount == 1){
            firstClickX = clickX;
            firstClickY = clickY;
            //Log.i(TAG , "checkClickUp 点击第一下");
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
                //Log.i(TAG , "checkClickUp Double click 不成立，当单击处理");
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
        //Log.i(TAG , "TouchEventCountThread 结束:" + touchCount);
    }
}

private class TouchEventHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        //Log.i(TAG, "touch " + msg.arg1 + " time.");
        if(msg.arg1 == 1){
            onSingleClicked(oldX, oldY);
        }else{
            onDoubleClicked(oldX, oldY);
        }
    }
}
```

<br>
如果单击，判断把事件向子VIEW传递还是自已处理

```java
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
```
<br>
处理缩放

```java
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

```
<br>
在 onTouchEvent() 中处理滑动事件, 
缩放时自己处理滑动事件，
非缩放时把滑动事件向父传递，所以 ViewPage 会处理左右滑动事件

```
if(isZooming){
   //缩放时， 自己处理MOVE事件
   getParent().requestDisallowInterceptTouchEvent(true);
}else {
   //非缩放时， 由父控件处理MOVE事件
   getParent().requestDisallowInterceptTouchEvent(false);
   ...
}
```

<br>
滑动和缩放过程中，处理边界回弹 checkBorder()

```java
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
```

<br>


下载体验
-----------------
<br>

![](https://github.com/evening424/resource/blob/master/images/SmartTouchLayout_Demo_v1.0.2_download.png?raw=true)

<br>

如果为哥你节省了几天宝贵的时间，为何不打赏一杯柠檬茶呢？
-----------------
<br>

![](https://github.com/evening424/resource/blob/master/images/WechatIMG1.jpeg)

