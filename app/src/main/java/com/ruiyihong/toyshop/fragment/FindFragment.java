/*
 * 2017.
 * Huida.Burt
 * CopyRight
 *
 *
 *
 */

package com.ruiyihong.toyshop.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruiyihong.toyshop.R;
import com.ruiyihong.toyshop.activity.HotTalkingDetailActivity;
import com.ruiyihong.toyshop.activity.LoginActivity;
import com.ruiyihong.toyshop.activity.LunboDetailActivity;
import com.ruiyihong.toyshop.activity.PublishActivity;
import com.ruiyihong.toyshop.adapter.FindHotTalkRvAdapter;
import com.ruiyihong.toyshop.adapter.FindHotTuijianRvAdapter;
import com.ruiyihong.toyshop.adapter.HomeVpAdapter;
import com.ruiyihong.toyshop.bean.find.FindHotTalkingBean;
import com.ruiyihong.toyshop.bean.find.FindHotTuijianBean;
import com.ruiyihong.toyshop.bean.find.FindLunboBean;
import com.ruiyihong.toyshop.bean.find.FindWantuquanBean;
import com.ruiyihong.toyshop.util.AppConstants;
import com.ruiyihong.toyshop.util.GsonUtil;
import com.ruiyihong.toyshop.util.LogUtil;
import com.ruiyihong.toyshop.util.NetWorkUtil;
import com.ruiyihong.toyshop.util.OkHttpUtil;
import com.ruiyihong.toyshop.util.Refresh_Listener;
import com.ruiyihong.toyshop.util.SPUtil;
import com.ruiyihong.toyshop.util.ToastHelper;
import com.ruiyihong.toyshop.view.CommonLoadingView;
import com.ruiyihong.toyshop.view.FullyGridLayoutManager;
import com.ruiyihong.toyshop.view.FullyLinearLayoutManager;
import com.ruiyihong.toyshop.view.find.ImageDialog;
import com.ruiyihong.toyshop.view.MyScrollView;
import com.ruiyihong.toyshop.view.find.VideoDialog;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.util.DensityUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Burt on 2017/7/29 0029.
 */

public class FindFragment extends BaseFragment {


    private static final String FIND_DATA_BUFFER_TOPIC = "find_data_buffer_topic";
    private static final String FIND_DATA_BUFFER_LUNBO = "find_data_buffer_lunbo";
    private static final String FIND_DATA_BUFFER_TUIJIAN = "find_data_buffer_tuijian";
    private static final String FIND_DATA_BUFFER_WANTUQUAN = "find_data_buffer_wantuquan";

    @InjectView(R.id.tv_add_title)
    TextView mTvAddTitle;
    @InjectView(R.id.find_float_button)
    FloatingActionButton mIbAddSave;
    @InjectView(R.id.vp_fine)
    ViewPager mVpFine;
    @InjectView(R.id.ll_find_vp_point)
    LinearLayout llFindVpPoint;
    @InjectView(R.id.ry_find_hottalk)
    RecyclerView mRyFindHottalk;
    @InjectView(R.id.ll_playicon_text)
    LinearLayout llPlayiconText;
    @InjectView(R.id.ll_find_playicon_textback)
    LinearLayout llFindPlayiconTextback;
    @InjectView(R.id.ry_find_hottuijian)
    RecyclerView mRyFindHottuijian;
    @InjectView(R.id.find_ScrollView)
    MyScrollView findScrollView;
    @InjectView(R.id.common_LoadingView)
    CommonLoadingView loadingView;
    @InjectView(R.id.smartRefreshLayout)
    SmartRefreshLayout mSmartRefreshLayout;

    private FindHotTuijianRvAdapter mHotAdapter;
    private List<FindLunboBean.DataBean> mLunboList;
    private int loadNum = 0;//加载更多的次数
    private boolean isRefresh = false;//是否是刷新数据

    private static final int MSG_LUNBO = 11;//轮播图自动播放msg
    private static final int MSG_PARSEDATA = 12;//轮播图自动播放msg

    private int preSelectedPoint;//上一个选中的点
    private final int TYPE_LUNBO_DATA = 0;//轮播数据
    private final int TYPE_TOPIC_DATA = 1;//热门话题数据
    private final int TYPE_TUJIAN_DATA = 2;//热门推荐题数据
    private final int TYPE_WANTUQUAN_DATA = 3;//热门推荐题数据
    private static final int TYPE_MORE_DATA = 4; //加载更多数据

    private static final int NetWorkError = 21;
    private static final int PageLoading = 22;
    private static final int CloseLoadingView = 23;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LUNBO:
                    //自动播放
                    mVpFine.setCurrentItem(mVpFine.getCurrentItem() + 1);
                    handler.sendEmptyMessageDelayed(MSG_LUNBO, 3000);
                    break;
                case MSG_PARSEDATA:
                    int type = msg.arg1;
                    String result = (String) msg.obj;
                    switch (type) {
                        case TYPE_LUNBO_DATA:
                            //缓存数据
                            try {
                                parseLunboData(result);
                                SPUtil.setString(mActivity, FIND_DATA_BUFFER_LUNBO, result);
                            } catch (Exception e) {
                                handler.sendEmptyMessage(NetWorkError);
                            }
                            break;
                        case TYPE_TOPIC_DATA:
                            try {
                                parseHotTalkintData(result);
                                SPUtil.setString(mActivity, FIND_DATA_BUFFER_TOPIC, result);
                            } catch (Exception e) {
                                handler.sendEmptyMessage(NetWorkError);
                            }
                            break;
                        case TYPE_TUJIAN_DATA:
                            try {
                                parseHotTuijianData(result);
                                SPUtil.setString(mActivity, FIND_DATA_BUFFER_TUIJIAN, result);
                            }catch (Exception e){
                                handler.sendEmptyMessage(NetWorkError);
                            }
                            break;
                        case TYPE_WANTUQUAN_DATA:
                            try {
                                parseWantuquan(result);
                                SPUtil.setString(mActivity, FIND_DATA_BUFFER_WANTUQUAN, result);
                            }catch (Exception e){
                                handler.sendEmptyMessage(NetWorkError);
                            }
                            break;
                        case TYPE_MORE_DATA:
                            mSmartRefreshLayout.finishLoadmore(0);
                            parseMoreData(result);
                            break;
                    }
                    break;
                case NetWorkError:    //显示网络错误页面
                    if (loadingView != null)
                        loadingView.loadError();
                    break;
                case PageLoading:       //页面加载中动画
                    if (loadingView != null)
                        loadingView.load();
                    break;
                case CloseLoadingView:     //关闭Loading动画
                    if (loadingView != null)
                        loadingView.loadSuccess(false);
                    break;
            }
        }
    };
    private List<FindHotTuijianBean.ListBean> tuijianList;
    private HomeVpAdapter mLunboAdapter;


    @Override
    protected View initView() {
        View view = View.inflate(mActivity, R.layout.fragment_find, null);
        return view;
    }

    @Override
    protected void initData() {
        isRefresh = false;
        boolean networkAvailable = NetWorkUtil.isNetWorkAvailable(mActivity);
        if (!networkAvailable) {
            handler.sendEmptyMessage(NetWorkError);
            return;
        } else {
            handler.sendEmptyMessage(PageLoading);
        }
        //初始化下拉刷新
        initHeaderAndFooter();
        //轮播图
        initViewPager();
        //玩图圈
        initWantuQuan();
        //热门话题
        initHotTalking();
        //热门推荐
        initHotTuijian();
    }

    private void initHeaderAndFooter() {
        mSmartRefreshLayout.setOnMultiPurposeListener(new Refresh_Listener());
        mSmartRefreshLayout.setRefreshFooter(new BallPulseFooter(mActivity));

        //下拉刷新监听
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
                                                     @Override
                                                     public void onRefresh(RefreshLayout refreshlayout) {
                                                         boolean networkAvailable = NetWorkUtil.isNetWorkAvailable(mActivity);
                                                         if (!networkAvailable) {
                                                             handler.sendEmptyMessage(NetWorkError);
                                                             return;
                                                         } else {
                                                             handler.sendEmptyMessage(PageLoading);
                                                         }
                                                         isRefresh = true;
                                                         //轮播图
                                                         initViewPager();
                                                         //玩图圈
                                                         initWantuQuan();
                                                         //热门话题
                                                         initHotTalking();
                                                         //热门推荐
                                                         initHotTuijian();
                                                     }

                                                 }
        );
        //上拉加载监听
        mSmartRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {

                getMoreData();
            }
        });
    }

    //todo 加载更多
    private void getMoreData() {
        loadNum++;
        String[] uids = SPUtil.getUid(mActivity);
        String uid = "";
        if (uids != null) {
            uid = uids[0];
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("page", loadNum + "");
        map.put("uid", uid);
        try {
            getHotTuijianData(AppConstants.FIND_HOTTUIJIAN_LOADMORE, map, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void parseMoreData(String json) {
        //LogUtil.e("更多数据===="+json);
        FindHotTuijianBean hotTuijianBean = GsonUtil.parseJsonWithGson(json, FindHotTuijianBean.class);
        if (hotTuijianBean == null)
            return;
        List<FindHotTuijianBean.ListBean> list = hotTuijianBean.getList();
        if (list.size() == 0) {
            ToastHelper.getInstance().displayToastShort("没有更多数据了");
            return;
        }
        if (tuijianList != null) {
            tuijianList.addAll(list);
            mHotAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 玩图圈
     */
    private void initWantuQuan() {
        //获取缓存数据
        String result = SPUtil.getString(mActivity, FIND_DATA_BUFFER_WANTUQUAN, "");
        if (!TextUtils.isEmpty(result)) {
            CloseLoadingView();
            //有缓存数据
            parseWantuquan(result);
        }
        try {
            getDataFromNet(AppConstants.FIND_HOT_WANTUQUAN, TYPE_WANTUQUAN_DATA);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 轮播图
     */
    private void initViewPager() {
        //获取缓存数据
        String result = SPUtil.getString(mActivity, FIND_DATA_BUFFER_LUNBO, "");
        if (!TextUtils.isEmpty(result)) {
            CloseLoadingView();
            //有缓存数据
            parseLunboData(result);
        }
        try {
            getDataFromNet(AppConstants.FIND_LUNBO, TYPE_LUNBO_DATA);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 热门推荐
     */
    public void initHotTuijian() {
        LogUtil.e("加载热门推荐数据了======");
        //获取缓存数据
        String result = SPUtil.getString(mActivity, FIND_DATA_BUFFER_TUIJIAN, "");
        if (!TextUtils.isEmpty(result)) {
            CloseLoadingView();
            //有缓存数据
            parseHotTuijianData(result);
        }
        try {
            String[] uids = SPUtil.getUid(mActivity);
            String uid = "";
            if (uids != null) {
                uid = uids[0];
            }
            HashMap<String, String> map = new HashMap<>();
            map.put("uid", uid);
            getHotTuijianData(AppConstants.FIND_HOT_TUIJIAN, map, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getHotTuijianData(String path, Map<String, String> map, final boolean isLoadMore) throws IOException {
        OkHttpUtil.postString(path, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(NetWorkError);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = OkHttpUtil.getResult(response);
                if (!TextUtils.isEmpty(result)) {
                    CloseLoadingView();
                    Message obtain = Message.obtain();
                    obtain.obj = result;
                    obtain.what = MSG_PARSEDATA;
                    if (!isLoadMore) {
                        obtain.arg1 = TYPE_TUJIAN_DATA;
                        handler.sendMessage(obtain);
                    } else {
                        //加载更多
                        obtain.arg1 = TYPE_MORE_DATA;
                        handler.sendMessage(obtain);
                    }
                }
            }
        });
    }

    private void initHotTalking() {
        //缓存数据
        String result = SPUtil.getString(mActivity, FIND_DATA_BUFFER_TOPIC, "");
        if (!TextUtils.isEmpty(result)) {
            LogUtil.e("话题有缓存，关闭loadview");
            CloseLoadingView();
            //有缓存数据
            parseHotTalkintData(result);
        }
        try {

            getDataFromNet(AppConstants.FIND_HOT_TALKING, TYPE_TOPIC_DATA);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void initEvent() {
        //发布按钮
        mIbAddSave.setOnClickListener(this);
        //网络错误后刷新页面的监听
        loadingView.setOnRefreshPagerListener(new CommonLoadingView.OnRefreshPageListener() {
            @Override
            public void Refresh() {
                initData();
            }
        });
    }

    /**
     * 放大显示图片
     */
    private void showImage(int position, FindHotTuijianBean.ListBean listBean) {
        final ImageDialog imageDialog = ImageDialog.getDialog(mActivity);
        String pic = listBean.getPic();
        if (pic.endsWith(";")) {
            pic = pic.substring(0, pic.length() - 1);
        }
        String[] split = pic.split(";");

        imageDialog.setImageData(split, position);

        imageDialog.setOnDialogClickListener(new ImageDialog.DialogItemClickListener() {
            @Override
            public void itemClick(int position) {
                imageDialog.dismiss();
            }
        });

//        imageDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        imageDialog.show();


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.find_float_button:
                String[] uid = SPUtil.getUid(mActivity);
                if (uid == null) {
                    //未登录
                    Intent intent = new Intent(mActivity, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(mActivity, PublishActivity.class);
                    startActivityForResult(intent, 20);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initHotTalking();
        if (requestCode == 20) {
            LogUtil.e("发布页面返回======");
            //发布成功，刷新页面
            initHotTuijian();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * 获取服务器数据
     *
     * @param url
     * @param type 数据类型（轮播，热门话题，热门推荐）
     * @throws IOException
     */
    private void getDataFromNet(String url, final int type) throws IOException {

        OkHttpUtil.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(NetWorkError);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String result = OkHttpUtil.getResult(response);


                Message msg = Message.obtain();
                msg.what = MSG_PARSEDATA;
                msg.obj = result;
                msg.arg1 = type;
                handler.sendMessage(msg);
                if (isRefresh) {
                    isRefresh = false;
                    mSmartRefreshLayout.finishRefresh(0);
                }
            }
        });
    }


    private void parseWantuquan(String json) {
        FindWantuquanBean wantuBean = GsonUtil.parseJsonWithGson(json, FindWantuquanBean.class);
        TextView tv = null;
        if (wantuBean != null) {
            List<FindWantuquanBean.DataBean> wantuList = wantuBean.getData();
            if (wantuList != null) {
                for (int i = 0; i < wantuList.size(); i++) {
                    String pname = wantuList.get(i).getPname();
                    //LogUtil.e("玩图圈name====="+pname);
                    tv = new TextView(mActivity);
                    tv.setText(pname);
                    tv.setTextSize(16);
                    tv.setPadding(3, 3, 3, 3);
                    tv.setGravity(Gravity.CENTER_VERTICAL);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    if (i != 0) {
                        params.leftMargin = DensityUtil.dp2px(5);
                    }
                    tv.setLayoutParams(params);
                    llPlayiconText.addView(tv);
                }
            }

        }
    }

    private void parseHotTuijianData(String json) {
        LogUtil.e("热门推荐==" + json);
        FindHotTuijianBean hotTuijianBean = GsonUtil.parseJsonWithGson(json, FindHotTuijianBean.class);
        if (hotTuijianBean == null)
            return;
        loadNum = 1;
        tuijianList = hotTuijianBean.getList();

        List<FindHotTuijianBean.List1Bean> zanList = hotTuijianBean.getList1();
        List<FindHotTuijianBean.List2Bean> collectList = hotTuijianBean.getList2();

        if (tuijianList != null && mRyFindHottuijian != null) {
            mRyFindHottuijian.setLayoutManager(new FullyLinearLayoutManager(mActivity));
            mHotAdapter = new FindHotTuijianRvAdapter(mActivity, tuijianList, zanList, collectList);
            mRyFindHottuijian.setAdapter(mHotAdapter);

            mHotAdapter.setOnGvItemClickListener(new FindHotTuijianRvAdapter.OnGvItemClickListener() {

                @Override
                public void onClickListener(View v, int position, int listPostion) {
                    //点击图片，放大显示图片
                    showImage(position, tuijianList.get(listPostion));
                }
            });

            mHotAdapter.setOnVideoClickListener(new FindHotTuijianRvAdapter.OnVideoClickListener() {
                @Override
                public void onClickListener(View v, int position, String picPath, String thumbImage_path) {

                    //跳转到视频播放
                    playVideo(picPath, thumbImage_path);
                }
            });

        }
    }

    /**
     * 播放视频
     *
     * @param picPath
     * @param thumbImage_path
     */
    private void playVideo(String picPath, String thumbImage_path) {
        if (picPath.endsWith(";")) {
            picPath = picPath.substring(0, picPath.length() - 1);
        }
        Intent intent = new Intent(mActivity, VideoDialog.class);
        intent.putExtra("video_path", AppConstants.FIND_IMAGE_BASE_URL + picPath);
        intent.putExtra("thumbImage_path", AppConstants.FIND_IMAGE_BASE_URL + thumbImage_path);//缩略图路径
        intent.putExtra("useCache", true);
        startActivity(intent);
    }

    /**
     * 热门话题数据解析
     *
     * @param json
     */
    private void parseHotTalkintData(String json) {
        // LogUtil.e("发现，热门话题====" + json);
        final FindHotTalkingBean hotTalkingBean = GsonUtil.parseJsonWithGson(json, FindHotTalkingBean.class);
        if (mRyFindHottalk != null && hotTalkingBean != null) {
            mRyFindHottalk.setLayoutManager(new FullyGridLayoutManager(mActivity, 2));
            final FindHotTalkRvAdapter hotTalkAdapter = new FindHotTalkRvAdapter(mActivity, hotTalkingBean.getData());
            mRyFindHottalk.setAdapter(hotTalkAdapter);

            //条目点击
            hotTalkAdapter.setOnItemClickListener(new FindHotTalkRvAdapter.OnItemClickListener() {
                @Override
                public void onClickListener(View v, int position) {
                    //跳转详情页
                    Intent intent = new Intent(mActivity, HotTalkingDetailActivity.class);
                    intent.putExtra("id", hotTalkingBean.getData().get(position).getId());
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * 轮播图数据解析
     *
     * @param json
     */
    private void parseLunboData(String json) {
        FindLunboBean mLunboBean = GsonUtil.parseJsonWithGson(json, FindLunboBean.class);
        if (mLunboBean == null)
            return;
        mLunboList = mLunboBean.getData();
        if (mLunboList == null)
            return;
        String[] images = new String[mLunboList.size()];
        llFindVpPoint.removeAllViews();
        for (int i = 0; i < mLunboList.size(); i++) {
            images[i] = AppConstants.IMG_BASE_URL + mLunboList.get(i).getImg();
            //LogUtil.e("轮播图url======" + images[i]);
            //添加点
            ImageView iv_point = new ImageView(mActivity);
            iv_point.setBackgroundResource(R.drawable.nomarl_point);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DensityUtil.dp2px(5), DensityUtil.dp2px(5));
            if (i != 0) {
                params.leftMargin = DensityUtil.dp2px(10);
            }
            iv_point.setLayoutParams(params);
            llFindVpPoint.addView(iv_point);
        }

        if (mVpFine != null) {
            mLunboAdapter = new HomeVpAdapter(images, mActivity);
            mVpFine.setAdapter(mLunboAdapter);
            //ViewPager条目的触摸事件
            mLunboAdapter.setOnItemClickListener(new View.OnTouchListener() {
                long start = 0;

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            start = System.currentTimeMillis();
                            handler.removeMessages(MSG_LUNBO);
                            break;
                        case MotionEvent.ACTION_UP:
                            handler.sendEmptyMessageDelayed(MSG_LUNBO, 3000);
                            long end = System.currentTimeMillis();
                            long offset = end - start;
                            LogUtil.e("offset===" + offset);
                            if (offset < 500) {
                                // 跳转详情页
                                Intent intent = new Intent(mActivity, LunboDetailActivity.class);
                                intent.putExtra("url", mLunboList.get(mVpFine.getCurrentItem() % mLunboList.size()).getUrl());
                                startActivity(intent);
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            handler.sendEmptyMessageDelayed(MSG_LUNBO, 3000);
                            break;
                    }
                    return true;
                }
            });
            //点的初始化
            preSelectedPoint = mVpFine.getCurrentItem();
            ImageView point = (ImageView) llFindVpPoint.getChildAt(preSelectedPoint);
            if (point != null)
                point.setImageResource(R.drawable.blue_point);
            //页面变化监听
            mVpFine.setOnPageChangeListener(new OnLunboPageChangeListener());

            //轮播图自动切换
            handler.removeMessages(MSG_LUNBO);
            handler.sendEmptyMessageDelayed(MSG_LUNBO, 3000);


        }
    }


    private class OnLunboPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            //点的选中切换
            position = position % mLunboList.size();
            ImageView point = (ImageView) llFindVpPoint.getChildAt(position);
            if (point != null) {
                point.setImageResource(R.drawable.blue_point);
            }
            ImageView prePoint = (ImageView) llFindVpPoint.getChildAt(preSelectedPoint);
            if (prePoint != null) {
                prePoint.setImageResource(R.drawable.nomarl_point);
            }
            preSelectedPoint = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    //关闭LoadingView
    private void CloseLoadingView() {
        handler.sendEmptyMessage(CloseLoadingView);
    }


}
