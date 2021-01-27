智能识别手势布局-SmartTouchLayout
============================

需求来源：
-------
产品需要实现跟微信朋友圈看大图可下滑退出的效果，<br>
但项目中不仅有大图，图中还有按钮，有文本，有视频等等，总之布局很复杂<br>
反正这些他不管，就是要下滑退出,再缩回上一层界面小图片位置。<br>
<br>
找了不少DEMO，基本都是只满足图片，视频实现这个功能，只能自己动手了...
<br>
<br>
<br>

SmartTouchLayout
-----------------
支持多手势识别的布局：<br>
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
1.引用
` `` Java
implementation 'com.jagger:SmartTouchLayout:1.0.0'

