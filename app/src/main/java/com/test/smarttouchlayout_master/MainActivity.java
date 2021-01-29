package com.test.smarttouchlayout_master;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rcv_album;
    private AlbumAdapter mAdapter;
    private ArrayList<DataBean> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        GridLayoutManager manager = new GridLayoutManager(MainActivity.this,3, GridLayoutManager.VERTICAL, false);
        rcv_album = findViewById(R.id.rcv_album);
        rcv_album.setLayoutManager(manager);

        mAdapter = new AlbumAdapter(MainActivity.this, mList);
        mAdapter.setOnItemEventListener(new AlbumAdapter.onItemEventListener() {
            @Override
            public void onItemClicked(DataBean dataBean, int position) {
                //记录小图坐标
                for(int i = 0 ; i < mList.size(); i++){
                    int[]location = new int[2];
                    rcv_album.findViewHolderForAdapterPosition(i).itemView.getLocationOnScreen(location);
                    mList.get(i).localX = location[0];
                    mList.get(i).localY = location[1];
                    mList.get(i).width = rcv_album.findViewHolderForAdapterPosition(i).itemView.getWidth();
                    mList.get(i).height = rcv_album.findViewHolderForAdapterPosition(i).itemView.getHeight();
                }

                ViewPageActivity.launchActivity(MainActivity.this, mList, position);
            }
        });
        rcv_album.setAdapter(mAdapter);

    }

    private void initData(){
        DataBean bean1 = new DataBean();
        bean1.resId = R.drawable.img1;

        DataBean bean2 = new DataBean();
        bean2.resId = R.drawable.img2;

        DataBean bean3 = new DataBean();
        bean3.resId = R.drawable.img3;

        DataBean bean4 = new DataBean();
        bean4.resId = R.drawable.img4;

        DataBean bean5 = new DataBean();
        bean5.resId = R.drawable.img5;

        DataBean bean6 = new DataBean();
        bean6.resId = R.drawable.img6;

        DataBean bean7 = new DataBean();
        bean7.resId = R.drawable.img7;

        mList.add(bean1);
        mList.add(bean2);
        mList.add(bean3);
        mList.add(bean4);
        mList.add(bean5);
        mList.add(bean6);
        mList.add(bean7);

        mAdapter.notifyDataSetChanged();
    }
}
