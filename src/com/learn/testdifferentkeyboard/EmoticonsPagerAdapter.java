package com.learn.testdifferentkeyboard;

import java.util.ArrayList;

import com.learn.testdifferentkeyboard.EmoticonsGridAdapter.KeyClickListener;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.GridView;

public class EmoticonsPagerAdapter extends PagerAdapter implements IconPagerAdapter {

	FragmentActivity mActivity;
	KeyClickListener mListener;
	private int mService, initialPosition, emosPerPage;
	private String[] emosArray;
	protected static final int[] ICONS_WA = new int[] { R.drawable.tab_1, R.drawable.tab_2, R.drawable.tab_3, R.drawable.tab_4, R.drawable.tab_5, R.drawable.tab_6 };
	protected static final int[] ICONS_SMS = new int[] { R.drawable.tab_2 };

	public EmoticonsPagerAdapter(FragmentActivity activity, KeyClickListener listener, int service) {
		super();
		this.mActivity = activity;
		this.mListener = listener;
		this.mService = service;
	}

	@Override
	public int getCount() {
		switch (mService) {
		case 1:
			return 1;
		case 2:
			return 6;
		}
		return 0;
	}

	@Override
	public Object instantiateItem(View collection, int position) {

		View layout = mActivity.getLayoutInflater().inflate(R.layout.emoticons_grid, null);

		/*
		 * Switching number of emoji pages and emojis per page according to current service
		 */
		switch (mService) {
		case 1: // SMS/MMS
			initialPosition = 0;
			emosPerPage = 21;
			emosArray = mActivity.getResources().getStringArray(R.array.emoticons_sms);
			break;
		case 2: // Whatsapp
			switch (position) {
			case 0: // recent
				initialPosition = 0;
				emosPerPage = 32;
				break;
			case 1:
				initialPosition = 0;
				emosPerPage = 189;
				break;
			case 2:
				initialPosition = 189;
				emosPerPage = 116;
				break;
			case 3:
				initialPosition = 305;
				emosPerPage = 230;
				break;
			case 4:
				initialPosition = 535;
				emosPerPage = 101;
				break;
			case 5:
				initialPosition = 636;
				emosPerPage = 207;
			}
			emosArray = mActivity.getResources().getStringArray(R.array.emoticons_wa);
			break;
		}

		ArrayList<String> emoticonsInAPage = new ArrayList<String>();

		for (int i = initialPosition; i < initialPosition + emosPerPage && i < emosArray.length; i++) {
			emoticonsInAPage.add(emosArray[i]);
		}
		
		/*
		 * Special condition for Whatsapp recent emoji screen
		 */
		if (mService == 2 && position == 0) {
			emoticonsInAPage = MainActivity.getRecentEmoticons();
		}

		GridView grid = (GridView) layout.findViewById(R.id.emoticons_grid);
		EmoticonsGridAdapter adapter = new EmoticonsGridAdapter(mActivity.getApplicationContext(), emoticonsInAPage, position, mListener);
		grid.setAdapter(adapter);

		((ViewPager) collection).addView(layout);

		return layout;
	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager) collection).removeView((View) view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public int getIconResId(int position) {
		switch (mService) {
		case 1:
			return 0; //ICONS_SMS[position % ICONS_SMS.length];
		case 2:
			return ICONS_WA[position % ICONS_WA.length];
		}
		return 0;
	}
}