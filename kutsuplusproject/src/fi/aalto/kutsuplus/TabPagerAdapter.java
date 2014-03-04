package fi.aalto.kutsuplus;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

public class TabPagerAdapter extends FragmentPagerAdapter {
	
	List<Fragment> fragList;
	
	public TabPagerAdapter(android.support.v4.app.FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		fragList = fragments;
	}
	
	@Override
	public Fragment getItem(int position) {
		Fragment fragment = fragList.get(position);
		return fragment;
	}
	
	@Override
	public int getCount() {
		return fragList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		//TODO should this do something?
		return "null";
	}
	
}
