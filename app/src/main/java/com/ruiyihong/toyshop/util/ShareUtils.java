/*
 * 2017.
 * Huida.Burt
 * CopyRight
 *
 *
 *
 */

package com.ruiyihong.toyshop.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


/**
 * Created by Burt on 2017/8/9 0009.
 */

public class ShareUtils {


    private static Tencent mTencent;

    /*public ShareUtils(Activity activity) {
        this.activity = activity;
        mTencent = Tencent.createInstance(AppConstants.TENCENT_APP_ID, activity.getApplicationContext());
    }*/

    public static void shareToQq(Context context, String title, String summary, String targetUrl, String imageUrl, IUiListener sharedUiListener) {
        final Bundle params = new Bundle();
        if(mTencent==null){
            mTencent = Tencent.createInstance(AppConstants.TENCENT_APP_ID,context);
        }
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);//分享的类型
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);//分享标题
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);//要分享的内容摘要
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);//内容地址
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);//分享的图片URL
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME,getAppName(context) );//应用名称
        mTencent.shareToQQ((Activity) context, params, sharedUiListener);
    }

    public static void shareToQzone(Context context, String title, String summary, String targetUrl, String imageUrl, IUiListener sharedUiListener) {
        final Bundle params = new Bundle();
        if(mTencent==null){
            mTencent = Tencent.createInstance(AppConstants.TENCENT_APP_ID,context);
        }
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);//必填
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);//选填
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, targetUrl);//必填
        ArrayList<String> strings = new ArrayList<>();
        strings.add(imageUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, strings);
        mTencent.shareToQzone((Activity) context, params, sharedUiListener);
    }

    public static Tencent getTencent(Context context) {
        if(mTencent==null){
            mTencent = Tencent.createInstance(AppConstants.TENCENT_APP_ID,context);
        }
        return mTencent;
    }

    /*********************上面是QQ***********************下面是微信****************************/

    private static IWXAPI mIWXinApi;

    /**
     * 分享网页到朋友圈或者好友，视频和音乐的分享和网页大同小异，只是创建的对象不同。
     * 详情参考官方文档：
     * https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317340&token=&lang=zh_CN
     *
     * @param url         网页的url
     * @param title       显示分享网页的标题
     * @param description 对网页的描述
     * @param scene       分享方式：好友还是朋友圈 1朋友圈 0好友
     */
    public static boolean shareUrl(Context context, String url, String title, String imgUrl, String description, int scene) {
        Bitmap thumb = BitmapFactory.decodeFile(imgUrl);
        //初试话一个WXWebpageObject对象，填写url
        WXWebpageObject webPage = new WXWebpageObject();
        webPage.webpageUrl = url;
        return share(context, webPage, title, thumb, description, scene);
    }
    public static boolean shareUrl(Context context, String url, String title, int imgUrl, String description, int scene) {
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(),imgUrl);
        //初试话一个WXWebpageObject对象，填写url
        WXWebpageObject webPage = new WXWebpageObject();
        webPage.webpageUrl = url;
        return share(context, webPage, title, thumb, description, scene);
    }


    /**
     * 分享图片到朋友圈或者好友
     *
     * @param bmp   图片的Bitmap对象
     * @param scene 分享方式：好友还是朋友圈
     */
    public static boolean sharePic(Context context, Bitmap bmp, int scene) {
        //初始化一个WXImageObject对象
        WXImageObject imageObj = new WXImageObject(bmp);
        //设置缩略图
        Bitmap thumb = Bitmap.createScaledBitmap(bmp, 60, 60, true);
        bmp.recycle();
        return share(context, imageObj, bmp, scene);
    }

    /**
     * 分享文字到朋友圈或者好友
     *
     * @param text  文本内容
     * @param scene 分享方式：好友还是朋友圈
     */
    public static boolean shareText(Context context, String text, int scene) {
        //初始化一个WXTextObject对象，填写分享的文本对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        return share(context, textObj, text, scene);
    }


    private static boolean share(Context context, WXMediaMessage.IMediaObject mediaObject, Bitmap thumb, int scene) {
        return share(context, mediaObject, null, thumb, null, scene);
    }

    private static boolean share(Context context, WXMediaMessage.IMediaObject mediaObject, String description, int scene) {
        return share(context, mediaObject, null, null, description, scene);
    }


    private static boolean share(Context context, WXMediaMessage.IMediaObject mediaObject, String title, Bitmap thumb, String description, int scene) {
        //初始化一个WXMediaMessage对象，填写标题、描述
        WXMediaMessage msg = new WXMediaMessage(mediaObject);
        if (title != null) {
            msg.title = title;
        }
        if (description != null) {
            msg.description = description;
        }
        if (thumb != null) {
            msg.thumbData = bmpToByteArray(thumb, true);
        }
        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = scene;

        mIWXinApi = WXAPIFactory.createWXAPI(context, AppConstants.WEIXIN_APP_ID, false);
        if (mIWXinApi.isWXAppInstalled()) {
            mIWXinApi.registerApp(AppConstants.WEIXIN_APP_ID);
            return mIWXinApi.sendReq(req);
        } else {
            return false;
        }
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}