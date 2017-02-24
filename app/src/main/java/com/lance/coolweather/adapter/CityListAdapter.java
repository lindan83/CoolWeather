package com.lance.coolweather.adapter;

import android.view.View;

import com.lance.common.recyclerview.adapter.CommonRecyclerViewAdapter;
import com.lance.common.recyclerview.adapter.base.CommonRecyclerViewHolder;
import com.lance.common.util.SPUtil;
import com.lance.coolweather.R;
import com.lance.coolweather.activity.CityManagerActivity;
import com.lance.coolweather.db.County;

import java.util.List;

/**
 * Created by lindan on 17-2-23.
 * 城市管理列表适配器
 */

public class CityListAdapter extends CommonRecyclerViewAdapter<County> {
    private boolean showDelete;
    private CityManagerActivity cityManagerActivity;

    public CityListAdapter(CityManagerActivity context, List<County> data) {
        super(context, R.layout.city_item, data);
        this.cityManagerActivity = context;
    }

    @Override
    protected void convert(CommonRecyclerViewHolder holder, County item, final int position) {
        holder.setText(R.id.tv_city, item.countyName);
        holder.setVisible(R.id.iv_delete, showDelete);
        holder.getConvertView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDelete = true;
                notifyDataSetChanged();
                return false;
            }
        });
        holder.setOnClickListener(R.id.iv_delete, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showDelete) {
                    String weatherId = data.get(position).weatherId;
                    SPUtil.remove(context, "weather_" + weatherId);
                    data.remove(position);
                    showDelete = false;
                    notifyDataSetChanged();
                    cityManagerActivity.setUpdated(true);
                }
            }
        });
    }
}
