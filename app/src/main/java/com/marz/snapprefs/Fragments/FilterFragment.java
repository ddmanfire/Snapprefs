package com.marz.snapprefs.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TabHost;

import com.marz.snapprefs.FilterStoreUtils.Tab1Fragment;
import com.marz.snapprefs.FilterStoreUtils.Tab1FragmentNew;
import com.marz.snapprefs.FilterStoreUtils.Tab2Fragment;
import com.marz.snapprefs.FilterStoreUtils.Tab3Fragment;
import com.marz.snapprefs.FilterStoreUtils.TabsFragmentActivity;
import com.marz.snapprefs.R;

import java.util.ArrayList;
import java.util.List;

public class FilterFragment extends Fragment{

    private FragmentTabHost mTabHost;
    View rootView;

       @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
           /*
           super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.filter_layout, null);
           Button filters = (Button) view.findViewById(R.id.filterstore);
           filters.setOnClickListener(new Button.OnClickListener() {
               public void onClick(View v) {
                   Intent intent = new Intent(getActivity().getApplicationContext(), TabsFragmentActivity.class);
                   startActivity(intent);
               }
           });
        return view;
        */

           mTabHost = new FragmentTabHost(getActivity());
           mTabHost.setup(getActivity(), getChildFragmentManager(), R.layout.filterstablayout);

           //Bundle arg1 = new Bundle();
           //arg1.putInt("Arg for Frag1", 1);
           mTabHost.addTab(mTabHost.newTabSpec("My Filters").setIndicator("My Filters"),
                   Tab1Fragment.class, null);

           //Bundle arg2 = new Bundle();
           //arg2.putInt("Arg for Frag2", 2);
           mTabHost.addTab(mTabHost.newTabSpec("Google+ (Request Filters)").setIndicator("Google+ (Request Filters)"),
                   Tab3Fragment.class, null);

           mTabHost.addTab(mTabHost.newTabSpec("Reddit (Get Filters)").setIndicator("Reddit (Get Filters)"),
                   Tab2Fragment.class, null);

           return mTabHost;
       }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this.getFragmentManager());
        adapter.addFragment(new Tab1Fragment(), "My Filters");
        adapter.addFragment(new Tab2Fragment(), "G+ (Request Filters)");
        adapter.addFragment(new Tab3Fragment(), "Reddit (Download Filters)");
        viewPager.setAdapter(adapter);
    }


}

class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}