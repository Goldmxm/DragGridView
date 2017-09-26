package com.meizu.media.reader.widget;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.meizu.media.reader.test.R;
import com.orhanobut.logger.Logger;

/**
 * 可拖动排序的GridView
 *
 * Created by maxueming on 17-8-30.
 */

public class DragGridView extends GridView implements AdapterView.OnItemLongClickListener{

    // 停留时间超过100毫秒，触发滑动
    private int triggerDragTime = 100;
    private float mStartedTouchX;
    private float mStartedTouchY;
    private int mTargetPos;
    private int mTempPos;
    // 判断是否正在滑动
    private boolean isDraging;
    private int mLastPos;
    private long mLastPosEqualTime;

    // 列表项位移动画时间
    private static final long TRANSLATE_DURATION = 160;

    public DragGridView(Context context) {
        super(context);
        init();
    }

    public DragGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setOnItemLongClickListener(this);
    }

    private enum SlideDirection{
        UP, DOWN, STOP
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if(adapter instanceof DragActionListener){
            setDragActionListener((DragActionListener) adapter);
        }
        super.setAdapter(adapter);
    }

    /**
     * 默认触发滑动的时间值为100ms，此处提供自定义
     *
     * @param time
     */
    private void setTriggerDragTime(int time){
        triggerDragTime = time;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Logger.e("i = " + i + ", l = " + l);
        startDrag(i, view);
        return true;
    }

    private void intoEditMode(){

    }

    /**
     * 开始拖动
     *
     * @param pos
     * @param view
     */
    private void startDrag(int pos, View view){
        // 设置当前View透明度，制造被拖出的假象，关键
        view.setAlpha(0);
        startDrag(null, new DragShadowBuilder(view){
            @Override
            public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint);
                View shadowView = getView();
                if (shadowView != null) {
                    final int width = shadowView.getWidth() + getResources().getDimensionPixelOffset(R.dimen.common_8dp);
                    final int height = shadowView.getHeight();
                    // 此处适当扩大shadow的宽度，使得负margin的View显示正确，不会对功能产生影响
                    outShadowSize.set(width, height);
                    int[] location = new int[2];
                    shadowView.getLocationOnScreen(location);
                    int offsetX = (int) mStartedTouchX - location[0];
                    int offsetY = (int) mStartedTouchY - location[1];
                    if (offsetX <= width && offsetX >= 0 && offsetY <= height && offsetY >= 0) {
                        // 保障shadow显示在item原位置
                        outShadowTouchPoint.set(offsetX, offsetY);
                    } else {
                        outShadowTouchPoint.set(width / 2, height / 2);
                    }
                }
            }
        }, pos, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN){
            mStartedTouchX = ev.getRawX();
            mStartedTouchY = ev.getRawY();
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onDragEvent(DragEvent dragEvent) {
        super.onDragEvent(dragEvent);
        int dragAction = dragEvent.getAction();
        int dragX = (int) dragEvent.getX();
        int dragY = (int) dragEvent.getY();
        int pos;
        int[] parentLocInWindow = new int[2];

        switch (dragAction){
            // 开始拖动
            case DragEvent.ACTION_DRAG_STARTED:{
                //Android 7.0 之前获得是窗口中的位置，因此需要减去 Parent 的坐标
                if (Build.VERSION.SDK_INT < 24) {
                    getLocationInWindow(parentLocInWindow);
                    dragX -= parentLocInWindow[0];
                    dragY -= parentLocInWindow[1];
                }
                pos = pointToPosition(dragX, dragY);
                mTargetPos = pos;
                mTempPos = -1;
                isDraging = true;
                mLastPos = -1;
                mLastPosEqualTime = 0;
                if(null != mDragActionListener){
                    mDragActionListener.startDrag();
                }
                break;
            }

            // 正在拖动
            case DragEvent.ACTION_DRAG_ENTERED:{

                break;
            }

            // 目标区域不断移动就会触发
            case DragEvent.ACTION_DRAG_LOCATION:{
                pos = pointToPosition(dragX, dragY);
                long stayTime = 0;
                if(mLastPos != pos){
                    mLastPos = pos;
                    mLastPosEqualTime = System.currentTimeMillis();
                }else{
                    stayTime = System.currentTimeMillis() - mLastPosEqualTime;
                }

                // 停留时间超过triggerDragTime，触发滑动
                if(stayTime >= triggerDragTime){
                    onDragTo(pos, mTargetPos);
                    mLastPosEqualTime = -1;
                }
                slideGridView(pos);
                break;
            }

            // 放下视图
            case DragEvent.ACTION_DROP:{
                pos = pointToPosition(dragX, dragY);
                if(null != mDragActionListener) {
                    mDragActionListener.endDrag(pos);
                }
                break;
            }

            // 结束拖放
            case DragEvent.ACTION_DRAG_EXITED:{
                break;
            }

            // 不管视图在哪里放下，都会触发
            case DragEvent.ACTION_DRAG_ENDED:{
                stopDrag();
                break;
            }
        }
        return true;
    }

    private void stopDrag(){
        isDraging = false;
        if(null != mDragActionListener){
            mDragActionListener.endDrag(-1);
        }
        int firstVisiblePosition = getFirstVisiblePosition();
        if(mTargetPos >= 0 && mTargetPos < getCount()){
            getChildAt(mTargetPos - firstVisiblePosition).setAlpha(1.0f);
        }
        if(mTempPos >= 0 && mTempPos < getCount()){
            getChildAt(mTempPos - firstVisiblePosition).setAlpha(1.0f);
        }
    }

    private void slideGridView(int pos){
        int firstVisiblePosition = getFirstVisiblePosition();
        View currentView = getChildAt(pos - firstVisiblePosition);
        View firstView = getChildAt(0);
        View lastView = getChildAt(getLastVisiblePosition() - firstVisiblePosition);
        if(currentView.getTop() < firstView.getBottom()){
            Logger.e("向上滚动");
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    smoothScrollBy(-100, 16);
                }
            }, 50);
        }

        if(currentView.getBottom() > lastView.getTop()){
            Logger.e("向下滚动");
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    smoothScrollBy(100, 16);
                }
            }, 50);
        }
    }

    private class SlideViewRunnable implements Runnable{
        @Override
        public void run() {

        }
    }

    /**
     * 拖动函数
     *
     * @param destPos
     * @param srcPos
     */
    private void onDragTo(int destPos, int srcPos){
        if(destPos == srcPos || mTempPos == destPos || destPos < 0 || destPos > getCount()) return;
        mTempPos = destPos;
        int firstVisiblePosition = getFirstVisiblePosition();
        if(srcPos > destPos){
            for(int i = srcPos - 1; i >= destPos; i--){
                View oldView = getChildAt(i - firstVisiblePosition);
                View destView = getChildAt(i - firstVisiblePosition + 1);
                oldView.startAnimation(getTranslateAnimation(oldView, destView));
            }
        }else{
            for(int i = srcPos + 1; i <= destPos; i++){
                View oldView = getChildAt(i - firstVisiblePosition);
                View destView = getChildAt(i - firstVisiblePosition - 1);
                oldView.startAnimation(getTranslateAnimation(oldView, destView));
            }
        }
    }

    /**
     * 返回位移动画函数
     *
     * @param srcView
     * @param destView
     * @return
     */
    private TranslateAnimation getTranslateAnimation(final View srcView, View destView){
        TranslateAnimation animation = new TranslateAnimation(0,
                destView.getLeft() - srcView.getLeft(), 0, destView.getTop() - srcView.getTop());
        animation.setDuration(TRANSLATE_DURATION);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(null != mDragActionListener){
                    mDragActionListener.switchItem(mTargetPos, mTempPos);
                    mTargetPos = mTempPos;
                    if(isDraging){
                        mDragActionListener.setCurrentDragPos(mTargetPos);
                    }
                }
                srcView.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        return animation;
    }

    private DragActionListener mDragActionListener;

    private void setDragActionListener(DragActionListener dragActionListener){
        mDragActionListener = dragActionListener;
    }

    /**
     * 拖动接口，与Adapter进行交互，用于拖动过程刷新item位置数据
     */
    public interface DragActionListener{
        void switchItem(int srcPos, int destPos);
        void endDrag(int pos);
        void startDrag();
        void setCurrentDragPos(int pos);
    }
}
