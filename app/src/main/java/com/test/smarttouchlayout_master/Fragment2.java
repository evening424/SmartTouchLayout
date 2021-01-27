package com.test.smarttouchlayout_master;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jagger.smartviewlibrary.SmartTouchLayout;

public class Fragment2 extends Fragment {

    public static final String ALBUM_ITEM = "ALBUM_ITEM";
    private DataBean dataBean;

    protected static Bundle getBundle(DataBean dataBean){
        Bundle bundle = new Bundle();
        bundle.putParcelable(ALBUM_ITEM, dataBean);
        return bundle;
    }

    public static Fragment2 newFragment(DataBean dataBean) {
        Fragment2 fragment2 = new Fragment2();
        fragment2.setArguments(getBundle(dataBean));
        return fragment2;
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
        View customView = inflater.inflate(R.layout.fragment_fragment2, container, false);
        customView.findViewById(R.id.img_photo).setBackgroundResource(dataBean.resId);
        customView.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "点击事件穿透到子控件" , Toast.LENGTH_LONG).show();
            }
        });


        SmartTouchLayout stl = customView.findViewById(R.id.stl);
        // 不赋值，向底部消失
//        stl.setEndViewLocalSize(dataBean.width, dataBean.height, dataBean.localX, dataBean.localY, SmartTouchLayout.EndViewScaleSide.Width);
        stl.setMoveExitEnable(true);
        stl.setZoomEnable(true);

        return customView;
    }

}

