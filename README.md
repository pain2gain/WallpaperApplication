# WallpaperApplication
这是一个用于练习的安卓壁纸应用。功能非常杂乱，用于熟悉安卓开发的基础.

### 基本要求

第一阶段

1.实现一个普通壁纸浏览app，该app使用androidstudio编写；

2.要求app中预置50张高清手机壁纸，这些壁纸作为预置resource资源放到app中；

3.第一个界面为壁纸预览界面，能够同时浏览6张壁纸，能够滑动，能够点击，使用recycleview实现；

4.点击进入之后，能左右滑动上一张/下一张图片，能双指缩放图片，长按弹出按钮，设置为手机锁屏壁纸；
***
第二阶段

1.把第一阶段的50张图片copy到本地并且创建数据库，记录图片的路径、修改时间，修改时间后续会作为排序使用；

2.找一个壁纸API，能够每天从网上下载10张图片，图片存储到本地，这10张图片按照下载到本地的时间排序，按序加到原50张图片之前
***
第三阶段 

1.应用作为系统APK预置，目的是为了后续需求； 

2.要求能开机自启动，后台常驻一个服务，该服务能每10min更换锁屏壁纸； 

3.keyguard界面添加一个图标，点击该图标可以打开壁纸预览界面。
