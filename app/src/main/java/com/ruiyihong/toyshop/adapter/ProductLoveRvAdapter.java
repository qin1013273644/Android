package com.ruiyihong.toyshop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruiyihong.toyshop.R;
import com.ruiyihong.toyshop.activity.DetailActivity;
import com.ruiyihong.toyshop.bean.ToyBean;
import com.ruiyihong.toyshop.util.AppConstants;
import com.ruiyihong.toyshop.view.MyImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by 81521 on 2017/7/5.
 */

public class ProductLoveRvAdapter extends RecyclerView.Adapter<ProductLoveRvAdapter.MyViewHolder> {
    private final Context context;
    private final List<ToyBean> list;
    private onRecyclerViewItemClickListener itemClickListener;

    public ProductLoveRvAdapter(Context context, List<ToyBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_love_rv_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        ToyBean toyBean = list.get(position);
        Picasso.with(context).load(AppConstants.IMG_BASE_URL+toyBean.getShopimg()).fit().placeholder(R.mipmap.good_default).error(R.mipmap.good_default).into(holder.iv_item);
        holder.tv_name.setText(toyBean.getName());
        holder.tv_price.setText("吊牌价："+toyBean.getDpj()+"元");
        holder.tv_rent_price.setText(toyBean.getShopprice()+"元/天");


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener!=null){
                    itemClickListener.onItemClick(view,position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        private final ImageView iv_item;
      //  private final MyImageView iv_item;
        private final TextView tv_name;
        private final TextView tv_rent_price;
        private final TextView tv_price;

        public MyViewHolder(View itemView) {
            super(itemView);

            iv_item = itemView.findViewById(R.id.iv_product_love_rv_item);
            tv_name = itemView.findViewById(R.id.tv_product_love_rv_item_name);
            tv_rent_price = itemView.findViewById(R.id.tv_product_love_rv_item_rent_price);
            tv_price = itemView.findViewById(R.id.tv_product_love_rv_item_price);

        }
    }

    public void setOnItemClickListener(onRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;

    }


    /**条目点击事件的监听器*/
    public  interface onRecyclerViewItemClickListener {

        void onItemClick(View v,int position);
    }
}
