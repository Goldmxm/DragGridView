package com.meizu.media.reader.widget.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter基类，不可修改，谨慎添加公用方法
 *
 * Created by maxueming on 17-8-30.
 */

public abstract class BaseBeamAdapter<T> extends BaseAdapter{

    protected Context mContext;

    public BaseBeamAdapter(Context context){
        mContext = context;
    }

    private List<T> mListData = new ArrayList<>();

    // 更新数据
    public void swapData(List<T> listData){
        if(null != mListData){
            mListData.clear();
            mListData.addAll(listData);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int pos) {
        return mListData.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        if(null == view){
            view = createItemRootView(pos, viewGroup);
        }
        dealItemView(pos, view, mListData);
        return view;
    }

    // 交换列表item位置
    protected void exchangeItem(int srcPos, int destPos){
        if(null != mListData){
            mListData.add(destPos, mListData.remove(srcPos));
            notifyDataSetChanged();
        }
    }

    // 生成Item根视图
    protected abstract View createItemRootView(int i, ViewGroup viewGroup);

    // 处理每个Item的View
    protected abstract void dealItemView(int i, View view, List<T> listData);
}
