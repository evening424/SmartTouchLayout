package com.test.smarttouchlayout_master;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jagger.smartviewlibrary.SmartTouchLayout;

public class Fragment3 extends Fragment {

    public static final String ALBUM_ITEM = "ALBUM_ITEM";
    private DataBean dataBean;

    protected static Bundle getBundle(DataBean dataBean){
        Bundle bundle = new Bundle();
        bundle.putParcelable(ALBUM_ITEM, dataBean);
        return bundle;
    }

    public static Fragment3 newFragment(DataBean dataBean) {
        Fragment3 fragment3 = new Fragment3();
        fragment3.setArguments(getBundle(dataBean));
        return fragment3;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(ALBUM_ITEM)) {
                dataBean = bundle.getParcelable(ALBUM_ITEM);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View customView = inflater.inflate(R.layout.fragment_fragment3, container, false);
        ((ImageView)customView.findViewById(R.id.img_photo)).setImageResource(dataBean.resId);

        SmartTouchLayout stl = customView.findViewById(R.id.stl);

//        stl.setEndViewLocalSize(dataBean.width, dataBean.height, dataBean.localX, dataBean.localY, SmartTouchLayout.EndViewScaleSide.Width);
        // 不可滑动关闭
        stl.setMoveExitEnable(false);
        // 不可缩放
        stl.setZoomEnable(false);
        return customView;
    }

}

