package com.meizu.media.reader.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.meizu.media.reader.widget.DragDataBean;
import com.meizu.media.reader.widget.DragGridView;
import com.meizu.media.reader.widget.adapter.DragGridViewAdapter;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ReaderMainActivity extends AppCompatActivity{

    private DragGridView gridView;
    private DragGridViewAdapter mAdapter;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_main);
        initView();
    }

    private void initView(){
        gridView = (DragGridView) findViewById(R.id.grid_view);
        mAdapter = new DragGridViewAdapter(this);
        gridView.setAdapter(mAdapter);
        getData();
    }

    private void getData(){
        subscription = Observable.create(new Observable.OnSubscribe<List<DragDataBean>>() {
            @Override
            public void call(Subscriber<? super List<DragDataBean>> subscriber) {
                List<DragDataBean> list = new ArrayList<>();
                for(int i = 0; i < 50; i++){
                    DragDataBean bean = new DragDataBean();
                    bean.setNum(String.valueOf(i));
                    list.add(bean);
                }
                subscriber.onNext(list);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<List<DragDataBean>>() {
            @Override
            public void call(List<DragDataBean> dragDataBeen) {
                mAdapter.swapData(dragDataBeen);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消订阅，防止内存泄漏
        if(null != subscription && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
    }
}
