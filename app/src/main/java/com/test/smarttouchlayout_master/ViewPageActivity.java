package com.test.smarttouchlayout_master;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class ViewPageActivity extends AppCompatActivity {
    private static final String ALBUM_LIST = "ALBUM_LIST";
    private static final String ALBUM_CURR_INDEX = "ALBUM_CURR_INDEX";

    private List<Fragment> fragmentList;
    private ViewPager viewPager;
    private List<DataBean> mAttachList = null;
    private int mCurrIndex = 0;

    public static void launchActivity(Context context , ArrayList<DataBean> anchorAlbumItems, int selectIndex) {
        Intent intent = null;
        intent = new Intent(context, ViewPageActivity.class);

        intent.putParcelableArrayListExtra(ALBUM_LIST, anchorAlbumItems);
        intent.putExtra(ALBUM_CURR_INDEX, selectIndex);

        if (intent != null) {
            context.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_page);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(ALBUM_LIST)) {
                mAttachList = bundle.getParcelableArrayList(ALBUM_LIST);
            }

            if (bundle.containsKey(ALBUM_CURR_INDEX)) {
                mCurrIndex = bundle.getInt(ALBUM_CURR_INDEX);
            }
        }

        initViewPager();
    }

    private void initViewPager() {
        viewPager = findViewById(R.id.viewPager);
        //
        fragmentList = new ArrayList<>();
        fragmentList.add(Fragment1.newFragment(mAttachList.get(0)));
        fragmentList.add(Fragment2.newFragment(mAttachList.get(1)));
        fragmentList.add(Fragment3.newFragment(mAttachList.get(2)));
        fragmentList.add(Fragment1.newFragment(mAttachList.get(3)));
        fragmentList.add(Fragment1.newFragment(mAttachList.get(4)));
        fragmentList.add(Fragment1.newFragment(mAttachList.get(5)));
        fragmentList.add(Fragment1.newFragment(mAttachList.get(6)));
        //
        MyFragmentPagerAdapter myFragmentPager = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(myFragmentPager);

        viewPager.setCurrentItem(mCurrIndex, true);
    }
}
