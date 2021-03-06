package com.ruiyihong.toyshop.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruiyihong.toyshop.R;
import com.ruiyihong.toyshop.bean.ToyShopToyListBean;
import com.ruiyihong.toyshop.util.AppConstants;
import com.ruiyihong.toyshop.util.LogUtil;
import com.ruiyihong.toyshop.util.OkHttpUtil;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 81521 on 2017/7/10.
 * 玩具城商品列表 adapter
 */

public class ToyShopAdapter extends RecyclerView.Adapter<ToyShopAdapter.MyViewHolder> {

    private final Activity context;
    private List<ToyShopToyListBean.DataBeanX.DataBean> toyList;
    private onRecyclerViewItemClickListener itemClickListener;
    private onShoppingcartClickListener shoppingcartClickListener;


    public ToyShopAdapter(Activity context, List<ToyShopToyListBean.DataBeanX.DataBean> mToyList) {
        this.context = context;
        this.toyList = mToyList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.toyshop_rv_item, null);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        ToyShopToyListBean.DataBeanX.DataBean dataBean = toyList.get(position);
        // LogUtil.e(position+"====="+dataBean.getShopprice());
        //名称
        String name = dataBean.getName();
        if (TextUtils.isEmpty(name)){
            name = dataBean.getName();
        }
        holder.tv_name.setText(name);
        //每天单价
        holder.tv_daypay.setText(dataBean.getShopprice()+"元/天");
        //吊牌价
         holder.tv_price.setText(dataBean.getDpj()+"元");
        //图片
        String imgUrl = AppConstants.IMG_BASE_URL+dataBean.getShopimg();

        Picasso.with(context).load(imgUrl).fit().placeholder(R.mipmap.good_default).error(R.mipmap.good_default).into(holder.iv_image);

        //条目点击事件的监听
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener!=null) {
                    itemClickListener.onItemClick(view, position);
                }
            }
        });
        //购物车按钮的点击事件
        holder.ib_shopping_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shoppingcartClickListener!=null){
                    shoppingcartClickListener.onShoppingcartClick(view,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (toyList!=null) {
            return toyList.size();
        }else {
            return 0;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iv_image;
        private final TextView tv_name;
        private final TextView tv_daypay;
        private final TextView tv_price;
        private final TextView tv_hot;
        private final TextView tv_canpost;
        private final ImageButton ib_shopping_cart;

        public MyViewHolder(View itemView) {
            super(itemView);
            iv_image = itemView.findViewById(R.id.iv_toyshop_image);
            tv_name = itemView.findViewById(R.id.tv_toyshop_name);
            tv_daypay = itemView.findViewById(R.id.tv_toyshop_daypay);
            tv_price = itemView.findViewById(R.id.tv_toyshop_price);
            tv_hot = itemView.findViewById(R.id.tv_toyshop_hot);
            tv_canpost = itemView.findViewById(R.id.tv_toyshop_canpost);
            ib_shopping_cart = itemView.findViewById(R.id.ib_shopping_cart);

        }
    }
    public void setOnItemClickListener(onRecyclerViewItemClickListener listener) {
        this.itemClickListener =  listener;

    }
    /**条目点击事件的监听器*/
    public  interface onRecyclerViewItemClickListener {

        void onItemClick(View v,int position);
    }

    /**
     * 设置购物车按钮点击 监听器
     * @param listener
     */
    public void setOnShoppingcartClickListener(onShoppingcartClickListener listener) {
        this.shoppingcartClickListener =  listener;

    }
    /**购物车按钮点击的监听器*/
    public  interface onShoppingcartClickListener {

        void onShoppingcartClick(View v,int position);
    }

    public void updataList( List<ToyShopToyListBean.DataBeanX.DataBean> toyList){
        this.toyList = toyList;

    }

}
