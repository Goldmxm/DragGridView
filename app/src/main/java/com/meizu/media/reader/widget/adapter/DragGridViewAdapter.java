package com.meizu.media.reader.widget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meizu.media.reader.test.R;
import com.meizu.media.reader.widget.DragDataBean;
import com.meizu.media.reader.widget.DragGridView;

import java.util.List;

/**
 * 可拖拽View对应Adapter
 *
 * Created by maxueming on 17-8-30.
 */

public class DragGridViewAdapter extends BaseBeamAdapter<DragDataBean> implements DragGridView.DragActionListener{

    private int dragPos = -1;

    public DragGridViewAdapter(Context context) {
        super(context);
    }

    @Override
    protected View createItemRootView(int i, ViewGroup viewGroup) {
        return LayoutInflater.from(mContext).inflate(R.layout.drag_gridview_item, viewGroup, false);
    }

    @Override
    protected void dealItemView(int i, View view, List<DragDataBean> listData) {
        ViewHolder viewHolder;
        if(null == view.getTag()){
            viewHolder = new ViewHolder();
            viewHolder.textView = view.findViewById(R.id.num);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.textView.setText(listData.get(i).getNum());
        if(dragPos == i){
            view.setAlpha(0f);
        }else{
            view.setAlpha(1.0f);
        }
    }

    @Override
    public void switchItem(int srcPos, int destPos) {
        if(-1 == srcPos || -1 == destPos || srcPos == destPos){
            return;
        }
        if(srcPos != destPos){
            exchangeItem(srcPos, destPos);
        }
    }

    @Override
    public void endDrag(int pos) {
        dragPos = -1;
    }

    @Override
    public void startDrag() {

    }

    @Override
    public void setCurrentDragPos(int pos) {
        dragPos = pos;
    }

    private class ViewHolder{
        TextView textView;
    }
}
