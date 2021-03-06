package com.rarnu.devlib.base;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import com.rarnu.devlib.R;
import com.rarnu.devlib.common.UIInstance;
import com.rarnu.devlib.common.IFragments;
import com.rarnu.devlib.utils.UIUtils;

public abstract class BaseMainActivity extends Activity implements IFragments {

	protected static boolean oneTimeRun = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		UIUtils.initDisplayMetrics(this, getWindowManager());
		super.onCreate(savedInstanceState);
		registerReceiver(receiverHome, filterHome);

		loadFragments();

		if (!oneTimeRun) {
			oneTimeRun = true;
			initOneTime();
		}
		loadUI();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiverHome);
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			releaseFragments();
			oneTimeRun = false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initOneTime() {
		initOnce();
	}

	public abstract void initOnce();

	public abstract String getBarTitle();

	private void loadUI() {
		setContentView(R.layout.layout_main);
		replaceIndexFragment();
		View vDetail = findViewById(R.id.fragmentDetail);
		UIInstance.dualPane = vDetail != null
				&& vDetail.getVisibility() == View.VISIBLE;
		getActionBar().setTitle(getBarTitle());
		setDualPane();
	}

	public abstract Fragment getFragment(int currentFragment);

	private void setDualPane() {
		if (UIInstance.dualPane) {
			replaceDetailFragment(getFragment(UIInstance.currentFragment));
		}
	}

	public abstract Fragment getIndexFragment();

	private void replaceIndexFragment() {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(R.id.fragmentMain, getIndexFragment());
		fragmentTransaction.commit();
	}

	private void replaceDetailFragment(Fragment f) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(R.id.fragmentDetail, f);
		fragmentTransaction.show(f);
		fragmentTransaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		initMenu(menu);
		return true;
	}

	public abstract void initMenu(Menu menu);

	public abstract void onHomeClick();

	public abstract void onRecentAppClick();

	public class HomeReceiver extends BroadcastReceiver {

		static final String SYSTEM_REASON = "reason";
		static final String SYSTEM_HOME_KEY = "homekey";
		static final String SYSTEM_RECENT_APPS = "recentapps";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (reason != null) {
					if (reason.equals(SYSTEM_HOME_KEY)) {
						onHomeClick();
						oneTimeRun = false;
					} else if (reason.equals(SYSTEM_RECENT_APPS)) {
						onRecentAppClick();
					}
				}
			}

		}

	}

	public HomeReceiver receiverHome = new HomeReceiver();
	public IntentFilter filterHome = new IntentFilter(
			Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

}
