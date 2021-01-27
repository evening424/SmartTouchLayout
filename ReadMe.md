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
多手势识别的布局：<br>
只要在布局里的VIEW，就支持：<br>
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
implementation 'com.jagger:SmartTouchLayout:1.0.1'
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

下载体验
-----------------
<br>

![](https://github.com/evening424/resource/blob/master/images/SmartTouchLayout_Demo_v1.0.2_download.png?raw=true)

<br>

如果为哥你节省了几天宝贵的时间，为何不打赏一杯柠檬茶呢？
-----------------
<br>

![](https://github.com/evening424/resource/blob/master/images/WechatIMG1.jpeg)

