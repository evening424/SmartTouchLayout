package com.test.smarttouchlayout_master;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jagger.smartviewlibrary.SmartTouchLayout;

public class Fragment1 extends Fragment {
    public static final String ALBUM_ITEM = "ALBUM_ITEM";
    private DataBean dataBean;

    protected static Bundle getBundle(DataBean dataBean){
        Bundle bundle = new Bundle();
        bundle.putParcelable(ALBUM_ITEM, dataBean);
        return bundle;
    }

    public static Fragment1 newFragment(DataBean dataBean) {
        Fragment1 fragment1 = new Fragment1();
        fragment1.setArguments(getBundle(dataBean));
        return fragment1;
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
        View customView = inflater.inflate(R.layout.fragment_fragment1, container, false);
        customView.findViewById(R.id.img_photo).setBackgroundResource(dataBean.resId);

        SmartTouchLayout stl = customView.findViewById(R.id.stl);

        stl.setEndViewLocalSize(dataBean.width, dataBean.height, dataBean.localX, dataBean.localY, SmartTouchLayout.EndViewScaleSide.Width);
        stl.setMoveExitEnable(true);
        stl.setZoomEnable(true);

        return customView;
    }

}

