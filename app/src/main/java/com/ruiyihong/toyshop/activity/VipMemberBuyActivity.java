package com.ruiyihong.toyshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.ruiyihong.toyshop.R;
import com.ruiyihong.toyshop.adapter.VipBuyAdapter;
import com.ruiyihong.toyshop.bean.VipMemberBuyBean;
import com.ruiyihong.toyshop.util.AppConstants;
import com.ruiyihong.toyshop.util.LogUtil;
import com.ruiyihong.toyshop.util.OkHttpUtil;
import com.ruiyihong.toyshop.util.SPUtil;
import com.ruiyihong.toyshop.util.ToastHelper;
import com.ruiyihong.toyshop.view.FullyLinearLayoutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by hegeyang on 2017/8/6 0006 .
 */

public class VipMemberBuyActivity extends BaseActivity {
	@InjectView(R.id.ry_vip_buy_content)
	RecyclerView ryVipBuyContent;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 0:
					paseData((String)msg.obj);
					break;
				case 1:
					ToastHelper.getInstance().displayToastShort("网络加载失败，正在重试");
					getDataFromNet();
					break;
			}
		}
	};
	private VipBuyAdapter vipBuyAdapter;
	private List<VipMemberBuyBean.DataBean> list;

	private void paseData(String data) {
		Gson gson = new Gson();
		VipMemberBuyBean vipMemberBuyBean = gson.fromJson(data, VipMemberBuyBean.class);
		list = vipMemberBuyBean.data;
		vipBuyAdapter = new VipBuyAdapter(this,list);
		ryVipBuyContent.setAdapter(vipBuyAdapter);
		vipBuyAdapter.setOnItemClickListener(new VipBuyAdapter.onRecyclerViewItemClickListener() {
			@Override
			public void onItemClick(View v, int position) {
				String user = SPUtil.getString(VipMemberBuyActivity.this, AppConstants.SP_LOGIN, "");
				String uid="";
				if (!TextUtils.isEmpty(user)){
					try {
						JSONObject object = new JSONObject(user);
						uid = object.getString("uid");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				if (TextUtils.isEmpty(uid)){
					Intent intent = new Intent(VipMemberBuyActivity.this, LoginActivity.class);
					intent.putExtra("type",position);
					startActivity(intent);
					return;
				}
				Intent intent = new Intent(VipMemberBuyActivity.this, VipMemberDetial.class);
				intent.putExtra("type",list.get(position).id);
				startActivity(intent);
			}
		});
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_vipmemberbuy;
	}

	@Override
	protected void initView() {

	}

	@Override
	protected void initData() {
		ryVipBuyContent.setLayoutManager(new FullyLinearLayoutManager(this));
		getDataFromNet();

	}

	private void getDataFromNet() {
		String url = AppConstants.SERVE_URL+"index/vipclass/viplist";
		try {
			OkHttpUtil.postString(url, new HashMap<String, String>(), new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					handler.sendEmptyMessage(1);
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					String result = OkHttpUtil.getResult(response);
					if (result!=null){
						Message obtain = Message.obtain();
						obtain.what = 0;
						obtain.obj = result;
						handler.sendMessage(obtain);
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initEvent() {

	}

	@Override
	protected void processClick(View v) throws IOException {

	}

}
