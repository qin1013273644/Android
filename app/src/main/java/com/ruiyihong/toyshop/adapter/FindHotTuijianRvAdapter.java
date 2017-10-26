package com.ruiyihong.toyshop.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ruiyihong.toyshop.R;
import com.ruiyihong.toyshop.bean.find.FindHotTuijianBean;
import com.ruiyihong.toyshop.util.AppConstants;
import com.ruiyihong.toyshop.util.LogUtil;
import com.ruiyihong.toyshop.util.MyThumbnailUtils;
import com.ruiyihong.toyshop.util.OkHttpUtil;
import com.ruiyihong.toyshop.util.SPUtil;
import com.ruiyihong.toyshop.util.ScreenUtil;
import com.ruiyihong.toyshop.util.ToastHelper;
import com.ruiyihong.toyshop.view.find.FindPlView;
import com.scwang.smartrefresh.layout.util.DensityUtil;
import com.squareup.picasso.Picasso;
import com.w4lle.library.NineGridlayout;
import com.yixia.camera.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import io.vov.vitamio.ThumbnailUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by 李晓曼 on 2017/8/5.
 */

public class FindHotTuijianRvAdapter extends RecyclerView.Adapter<FindHotTuijianRvAdapter.MyViewHolder> {

    private final Activity context;
    private final List<FindHotTuijianBean.ListBean> list;
    private final List<FindHotTuijianBean.List1Bean> zanList;
    private final List<FindHotTuijianBean.List2Bean> collectList;
    //private String videoPath = "";

    private OnVideoClickListener itemListener;

    private OnGvItemClickListener gvItemListener;
    private HashMap<Integer, Boolean> is_comment = new HashMap<>();//每个条目点赞展开的情况
    private HashMap<Integer, Integer> zanCountMap = new HashMap<>();//每个条目点赞展开的情况

    public FindHotTuijianRvAdapter(Activity context, List<FindHotTuijianBean.ListBean> list, List<FindHotTuijianBean.List1Bean> zanList, List<FindHotTuijianBean.List2Bean> collectList) {
        this.context = context;
        this.list = list;
        this.zanList = zanList;
        this.collectList = collectList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.find_hottuijian_item, null));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final int listPostion = position;
        final FindHotTuijianBean.ListBean listBean = list.get(position);
        int tid = listBean.getId();//热门评论的id
        //是否是自己的说说
        int uid = listBean.getUid();
        String s = "";
        if (SPUtil.getUid(context) != null) {
            s = SPUtil.getUid(context)[0];
        }
        if (!TextUtils.isEmpty(s)) {
            int my_uid = Integer.parseInt(s);
            if (my_uid == uid) {
                holder.tv_delete.setVisibility(View.VISIBLE);
            } else {
                holder.tv_delete.setVisibility(View.INVISIBLE);
            }
        }
        //初始化评论
        for (int i = 0; i < list.size(); i++) {
            is_comment.put(i, false);
        }
        //回显是否点赞
        for (int i = 0; i < zanList.size(); i++) {
            if (zanList.get(i).getTid() == tid) {
                //点赞的条目
                holder.iv_dianzan.setSelected(true);
                break;
            }
        }
        // 回显是否收藏
        for (int i = 0; i < collectList.size(); i++) {
            if (collectList.get(i).getSign() == 0) {
                if (collectList.get(i).getWid() == tid) {
                    holder.iv_shoucang.setSelected(true);
                    break;
                }
            }
        }
        //是否官方
        if (listBean.getIsadmin() == 1) {
            holder.tv_item_isguanfang.setVisibility(View.VISIBLE);//官方
        } else {
            holder.tv_item_isguanfang.setVisibility(View.INVISIBLE);  //非官方
        }
        holder.tv_content_item.setText(listBean.getContent());//发布内容
        holder.tv_name_item.setText(listBean.getYhniche());//用户昵称
        holder.tv_time_item.setText(listBean.getTime());//发布时间
        holder.tv_pinglun.setText("评论 (" + listBean.getPlnum() + ")");//评论
        holder.tv_dianzan.setText("点赞 (" + listBean.getDzl() + ")");//点赞

        //用户头像
        String head_url = listBean.getYhimg();
        //LogUtil.e("发现页，用户头像====="+head_url);
        if (!TextUtils.isEmpty(head_url)) {
            if (head_url.startsWith("http")) {
                head_url = head_url.replace("\\", "");
            }else {
                head_url = AppConstants.FIND_HOT_TALKING_IAMGE_BASE+head_url;
            }
            Picasso.with(context).load(head_url).placeholder(R.mipmap.personinfo_head_icon).error(R.mipmap.personinfo_head_icon).into(holder.iv_head_item);
        }else{
            Picasso.with(context).load(R.mipmap.personinfo_head_icon).error(R.mipmap.personinfo_head_icon).into(holder.iv_head_item);
        }
        if (TextUtils.isEmpty(listBean.getPic())) {
            //没有图片或视频
            holder.gv_item.setVisibility(View.GONE);
            holder.rl_video.setVisibility(View.GONE);
        } else {
            //有图片或视频
            int ispic = listBean.getIspic();
            if (ispic == 0) {
                holder.gv_item.setVisibility(View.VISIBLE);
                holder.rl_video.setVisibility(View.GONE);
                //是图片
                String pic = listBean.getPic();
                if (pic.endsWith(";")) {
                    pic = pic.substring(0, pic.length() - 1);
                }
                String[] split = pic.split(";");
                List<String> imageList = Arrays.asList(split);
                FindPicGvAdapter adapter = new FindPicGvAdapter(context, imageList);
                holder.gv_item.setAdapter(adapter);
                holder.gv_item.setDefaultWidth(ScreenUtil.dp2px(context, 300));
                holder.gv_item.setDefaultHeight(ScreenUtil.dp2px(context, 200));
                holder.gv_item.setOnItemClickListerner(new NineGridlayout.OnItemClickListerner() {
                    @Override
                    public void onItemClick(View view, int position) {

                        if (gvItemListener != null) {
                            gvItemListener.onClickListener(view, position, listPostion);
                        }
                    }
                });

            } else if (ispic == 1) {
                //是视频
                holder.gv_item.setVisibility(View.GONE);
                holder.rl_video.setVisibility(View.VISIBLE);
                //获取视频缩略图
                String picPath = listBean.getPic();
                if (picPath.endsWith(";")) {
                    picPath = picPath.substring(0, picPath.length() - 1);
                }
                String[] split = picPath.split(";");
                final String s1 = split[0];
                String s2 = "";
                if (split.length>1){
                    s2 = split[1];
                }
                if (s1.endsWith(".PNG") ||s1.endsWith(".png") ||s1.endsWith(".JPG")||s1.endsWith(".jpg")){
                    //s1是图片，s2是视频
                    Picasso.with(context).load(AppConstants.FIND_IMAGE_BASE_URL+s1).fit().into(holder.vv_item);
                   // videoPath = s2;
                    final String finalS = s2;
                    holder.rl_video.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (itemListener != null) {

                                itemListener.onClickListener(view, position, finalS,s1);
                            }
                        }
                    });

                }else{
                    //s2是图片，s1是视频
                    Picasso.with(context).load(AppConstants.FIND_IMAGE_BASE_URL+s2).fit().into(holder.vv_item);
                   // videoPath = s1;
                    final String finalS1 = s2;
                    holder.rl_video.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (itemListener != null) {
                                itemListener.onClickListener(view, position, s1, finalS1);
                            }
                        }
                    });
                }

            }
        }

        /**点赞*/
        zanCountMap.put(listBean.getId(), listBean.getDzl());
        holder.ll_dianzan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    postZan(listBean, holder.iv_dianzan, holder.tv_dianzan);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        /**收藏*/
        holder.ll_shoucang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    postCollect(listBean, holder.iv_shoucang, holder.tv_shoucang);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        /**评论*/
        holder.ll_pinglun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] is_login = SPUtil.getUid(context);
                if (is_login == null) {
                    ToastHelper.getInstance().displayToastShort("请登录后操作");
                    return;
                }
                //展示评论列表
                if (is_comment.get(position)) {
                    //展开----收回
                    holder.fl_pinglun.removeAllViews();
                    is_comment.put(position, false);
                } else {
                    is_comment.put(position, true);
                    String[] uids = SPUtil.getUid(context);
                    String uid = "";
                    if (uids != null) {
                        uid = uids[0];
                    }
                    FindPlView findPlView = new FindPlView(context, list.get(position).getId(), uid, holder.tv_pinglun, listBean);
                    holder.fl_pinglun.addView(findPlView.mRootView);
                }
            }
        });
        /**
         * 删除说说
         */
        holder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("是否删除");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            deleteShuoshuo(position, list.get(position).getId(), list.get(position).getUid());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
            }
        });
    }

    /**
     * 删除说说
     *
     * @param position
     * @param tid
     * @param uid
     */
    private void deleteShuoshuo(final int position, int tid, int uid) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("tid", tid + "");
        map.put("uid", uid + "");
        OkHttpUtil.postString(AppConstants.FIND_HOTTUIJIAN_DELETE, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = OkHttpUtil.getResult(response);
                if (result != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String msg = jsonObject.getString("msg");
                        if (msg.contains("成功")) {
                            //刷新页面
                            list.remove(position);
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyDataSetChanged();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iv_head_item; //头像
        private final TextView tv_name_item; //用户名
        private final TextView tv_content_item;//内容
        private final NineGridlayout gv_item;//图片
        private final ImageView vv_item; //视频
        private final TextView tv_time_item; // 发布时间
        private final TextView tv_item_isguanfang; //是否是官方
        private final LinearLayout ll_pinglun;
        private final LinearLayout ll_dianzan;
        private final LinearLayout ll_shoucang;
        private final TextView tv_pinglun;
        private final TextView tv_dianzan;
        private final TextView tv_shoucang;
        private final ImageView tv_delete;
        private final ImageView iv_pinglun;
        private final ImageView iv_shoucang;
        private final ImageView iv_dianzan;
        private final FrameLayout fl_pinglun;
        private final RelativeLayout rl_video;


        public MyViewHolder(View itemView) {
            super(itemView);
            iv_head_item = itemView.findViewById(R.id.tv_find_hottuijian_item_icon);
            tv_name_item = itemView.findViewById(R.id.tv_find_hottuijian_item_name);
            tv_time_item = itemView.findViewById(R.id.tv_find_hottuijian_item_time);
            tv_item_isguanfang = itemView.findViewById(R.id.find_item_isguanfang);
            tv_content_item = itemView.findViewById(R.id.tv_item_content);
            gv_item = itemView.findViewById(R.id.gv_find_hottuijian_item);
            vv_item = itemView.findViewById(R.id.video_find_hottuijian_item);
            ll_pinglun = itemView.findViewById(R.id.ll_pinglun);
            ll_dianzan = itemView.findViewById(R.id.ll_dianzan);
            ll_shoucang = itemView.findViewById(R.id.ll_shoucang);
            tv_pinglun = itemView.findViewById(R.id.tv_pinglun);
            tv_shoucang = itemView.findViewById(R.id.tv_shoucang);
            tv_dianzan = itemView.findViewById(R.id.tv_dianzan);
            iv_pinglun = itemView.findViewById(R.id.iv_pinglun);
            iv_shoucang = itemView.findViewById(R.id.iv_shoucang);
            iv_dianzan = itemView.findViewById(R.id.iv_dianzan);
            fl_pinglun = itemView.findViewById(R.id.fl_find_pinglun);
            rl_video = itemView.findViewById(R.id.rl_video);
            tv_delete = itemView.findViewById(R.id.find_item_delete);
        }
    }

    public void setOnVideoClickListener(OnVideoClickListener itemListener) {
        this.itemListener = itemListener;
    }


    public interface OnVideoClickListener {
        void onClickListener(View v, int position, String picPath, String finalS1);
    }

    /**
     * 九宫格图片条目点击事件
     */
    public void setOnGvItemClickListener(OnGvItemClickListener itemListener) {
        this.gvItemListener = itemListener;
    }


    public interface OnGvItemClickListener {
        void onClickListener(View v, int position, int listPostion);
    }


    /**
     * 点赞
     */
    private void postZan(final FindHotTuijianBean.ListBean bean, final ImageView iv_dianzan, final TextView tv_dianzan) throws IOException {
        String[] uid = SPUtil.getUid(context);
        if (uid == null) {
            ToastHelper.getInstance().displayToastShort("请登录后操作");
            return;
        }
        final HashMap<String, String> map = new HashMap<>();
        map.put("tid", bean.getId() + "");
        map.put("uid", uid[0]);
        OkHttpUtil.postString(AppConstants.FIND_HOTTUIJIAN_DIANZAN, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = OkHttpUtil.getResult(response);
                LogUtil.e("点赞=================" + result);
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject object = new JSONObject(result);
                        final String msg = object.getString("msg");
                        if (!TextUtils.isEmpty(msg) && msg.contains("成功")) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int id = bean.getId();
                                    Integer count = zanCountMap.get(id);
                                    if (msg.contains("取消")) {
                                        //取消点赞
                                        iv_dianzan.setSelected(false);
                                        count = count - 1;
                                        tv_dianzan.setText("点赞 (" + count + ")");
                                        zanCountMap.put(id, count);
                                    } else {
                                        //点赞
                                        iv_dianzan.setSelected(true);
                                        count = count + 1;
                                        tv_dianzan.setText("点赞 (" + count + ")");
                                        zanCountMap.put(id, count);
                                    }
                                }
                            });
                        }
                        if (!TextUtils.isEmpty(msg) && msg.contains("失败")) {
                            ToastHelper.getInstance().displayToastShort("操作失败，请稍后再试");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    /**
     * 收藏
     *
     * @param bean
     * @param iv_shoucang
     * @param tv_shoucang
     */
    private void postCollect(final FindHotTuijianBean.ListBean bean, final ImageView iv_shoucang, final TextView tv_shoucang) throws IOException {
        String[] uid = SPUtil.getUid(context);
        if (uid == null) {
            ToastHelper.getInstance().displayToastShort("请登录后操作");
            return;
        }
        final HashMap<String, String> map = new HashMap<>();
        map.put("wid", bean.getId() + "");//说说id
        map.put("uid", uid[0]);//用户id
        map.put("sign", "0");//标记 0 是热门推荐收藏

        OkHttpUtil.postString(AppConstants.COLLECT_URL, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = OkHttpUtil.getResult(response);
                LogUtil.e(response + "==热门推荐收藏===" + result);
                if (result != null) {
                    try {
                        JSONObject object = new JSONObject(result);
                        final String msg = object.getString("msg");
                        if (!TextUtils.isEmpty(msg) && msg.contains("成功")) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    int id = bean.getId();
//                                    Integer count = zanCountMap.get(id);
                                    if (msg.contains("取消")) {
                                        //取消收藏
                                        iv_shoucang.setSelected(false);

                                    } else {
                                        //收藏
                                        iv_shoucang.setSelected(true);
                                    }
                                }
                            });
                        }
                        if (!TextUtils.isEmpty(msg) && msg.contains("失败")) {
                            ToastHelper.getInstance().displayToastShort("收藏失败，请稍后再试");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastHelper.getInstance().displayToastShort("收藏失败，请稍后再试");
                        }
                    });
                }
            }
        });
    }
}