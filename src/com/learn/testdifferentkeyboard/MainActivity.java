package com.learn.testdifferentkeyboard;

import java.util.ArrayList;
import java.util.Collections;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import com.learn.testdifferentkeyboard.EmoticonsGridAdapter.KeyClickListener;

/**
 * Main activity for chat window
 * 
 * @author Chirag Jain
 * @author Tobias Moll
 */
public class MainActivity extends FragmentActivity implements KeyClickListener {

	private ListView chatList;
	private View popUpView;
	private ArrayList<Spanned> chats;
	private ChatListAdapter mAdapter;

	private LinearLayout emoticonsCover;
	private PopupWindow popupWindow;

	private int keyboardHeight, mService;
	private EditText content;

	private LinearLayout parentLayout;

	private boolean isKeyBoardVisible;
	
	protected static ArrayList<String> recentEmoticons = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		chatList = (ListView) findViewById(R.id.chat_list);

		parentLayout = (LinearLayout) findViewById(R.id.list_parent);

		emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);

		popUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);
		
		// Setting current service to show emojis for
		// 1 = SMS/MMS, 2 = WhatsApp
		mService = 2;
		
		// Setting adapter for chat list
		chats = new ArrayList<Spanned>();
		mAdapter = new ChatListAdapter(getApplicationContext(), chats);
		chatList.setAdapter(mAdapter);
		chatList.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (popupWindow.isShowing())
					popupWindow.dismiss();
				return false;
			}
		});

		// Defining default height of keyboard which is equal to 230 dip
		final float popUpheight = getResources().getDimension(R.dimen.keyboard_height);
		changeKeyboardHeight((int) popUpheight);

		// Showing and Dismissing pop up on clicking emoticons button
		// Changing drawable of emoticons button accordingly
		final ImageView emoticonsButton = (ImageView) findViewById(R.id.emoticons_button);
		emoticonsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!popupWindow.isShowing()) {

					popupWindow.setHeight((int) (keyboardHeight));

					if (isKeyBoardVisible) {
						emoticonsCover.setVisibility(LinearLayout.GONE);
						emoticonsButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard));
					} else {
						emoticonsCover.setVisibility(LinearLayout.VISIBLE);
						emoticonsButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_down));
					}
					popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);

				} else {
					popupWindow.dismiss();
					emoticonsButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_smiley));
				}

			}
		});

		enablePopUpView();
		checkKeyboardHeight(parentLayout);
		enableFooterView();

	}
	
	/**
	 * Save recent emojis
	 */
	@Override
	protected void onPause(){
		String joinedEmoticons = TextUtils.join("#", recentEmoticons);
		SharedPreferences.Editor editor = getSharedPreferences("EmojiPrefs",0).edit();
		editor.clear();
		editor.putString("recent_emoticons", joinedEmoticons);
		editor.commit();
		super.onPause();
	}
	
	/**
	 * Reload recent emojis (if any) from shared prefs
	 */
	@Override
	protected void onResume() {
		super.onResume();
		recentEmoticons.clear();
		String joinedEmoticons = getSharedPreferences("EmojiPrefs",0).getString("recent_emoticons", null);
		Collections.addAll(recentEmoticons, joinedEmoticons.split("#"));
	}

	/**
	 * Enabling all content in footer i.e. post window
	 */
	private void enableFooterView() {

		content = (EditText) findViewById(R.id.chat_content);
		content.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (popupWindow.isShowing()) {

					popupWindow.dismiss();

				}

			}
		});
		final Button postButton = (Button) findViewById(R.id.post_button);

		postButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (content.getText().toString().length() > 0) {

					Spanned sp = content.getText();
					chats.add(sp);
					content.setText("");
					mAdapter.notifyDataSetChanged();

				}

			}
		});
	}

	/**
	 * Overriding onKeyDown for dismissing keyboard on key down
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (popupWindow.isShowing()) {
			popupWindow.dismiss();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Checking keyboard height and keyboard visibility
	 */
	int previousHeightDiffrence = 0;

	private void checkKeyboardHeight(final View parentLayout) {

		parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {

				Rect r = new Rect();
				parentLayout.getWindowVisibleDisplayFrame(r);

				int screenHeight = parentLayout.getRootView().getHeight();
				int heightDifference = screenHeight - (r.bottom);

				if (previousHeightDiffrence - heightDifference > 50) {
					popupWindow.dismiss();
				}

				previousHeightDiffrence = heightDifference;
				if (heightDifference > 100) {

					isKeyBoardVisible = true;
					changeKeyboardHeight(heightDifference);

				} else {

					isKeyBoardVisible = false;

				}

			}
		});

	}

	/**
	 * change height of emoticons keyboard according to height of actual
	 * keyboard
	 * 
	 * @param height
	 *            minimum height by which we can make sure actual keyboard is
	 *            open or not
	 */
	private void changeKeyboardHeight(int height) {

		if (height > 100) {
			keyboardHeight = height;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, keyboardHeight);
			emoticonsCover.setLayoutParams(params);
		}

	}

	/**
	 * Defining all components of emoticons keyboard
	 */
	private void enablePopUpView() {

		ViewPager pager = (ViewPager) popUpView.findViewById(R.id.emoticons_pager);
		pager.setOffscreenPageLimit(5);

		EmoticonsPagerAdapter adapter = new EmoticonsPagerAdapter(MainActivity.this, this, mService);
		pager.setAdapter(adapter);
		
		TabPageIndicator indicator = (TabPageIndicator) popUpView.findViewById(R.id.emoticons_indicator);
		indicator.setViewPager(pager);
		indicator.setService(mService);

		// Creating a pop window for emoticons keyboard
		popupWindow = new PopupWindow(popUpView, LayoutParams.MATCH_PARENT,
				(int) keyboardHeight, false);
		
		ImageView backSpace = (ImageView) popUpView.findViewById(R.id.back);
		backSpace.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
				content.dispatchKeyEvent(event);	
			}
		});

		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				emoticonsCover.setVisibility(LinearLayout.GONE);
			}
		});
	}

	/**
	 * For placing smileys in chat window in desired width 
	 */
	private Bitmap getImage(String path) {
		int desiredWidth = 48;
		int resID = getResources().getIdentifier(path, "drawable", getPackageName());

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), resID, options);
		int srcWidth = options.outWidth;
		int inSampleSize = 1;
		while (srcWidth / 2 > desiredWidth) {
			srcWidth /= 2;
			inSampleSize *= 2;
		}

		float desiredScale = (float) desiredWidth / srcWidth;
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inSampleSize = inSampleSize;
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap sampledSrcBitmap = BitmapFactory.decodeResource(getResources(), resID, options);
		Matrix matrix = new Matrix();
		matrix.postScale(desiredScale, desiredScale);
		Bitmap scaledBitmap = Bitmap.createBitmap(sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);
		sampledSrcBitmap = null;

		return scaledBitmap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void keyClickedIndex(final String index) {

		ImageGetter imageGetter = new ImageGetter() {
			public Drawable getDrawable(String source) {
				Drawable d = new BitmapDrawable(getResources(),getImage(index));
				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
				return d;
			}
		};

		Spanned cs = Html.fromHtml("<img src ='" + index + ".png'/>", imageGetter, null);

		int cursorPosition = content.getSelectionStart();
		content.getText().insert(cursorPosition, cs);
		
		/*
		 * WhatsApp recent screen
		 * move last clicked emoji to first position of recent screen
		 * remove it first, if it already is in recent screen
		 * truncate number of emojis to 32, equals 4 rows
		 */
		
		if (mService == 2) {
			if (recentEmoticons.contains(index)){
				recentEmoticons.remove(recentEmoticons.indexOf(index));
			}
			recentEmoticons.add(0, index);
			if(recentEmoticons.size() > 32) {
				recentEmoticons.remove(recentEmoticons.size() - 1);
			}
		}

	}
	
	/**
	 * For getting recent emojis in EmoticonsPagerAdapter
	 */
	public static ArrayList<String> getRecentEmoticons() {
		return recentEmoticons;
	}

}
