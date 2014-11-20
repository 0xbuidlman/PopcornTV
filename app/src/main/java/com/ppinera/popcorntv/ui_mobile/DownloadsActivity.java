package com.ppinera.popcorntv.ui_mobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.controller.DownloadsAdapter;
import dp.ws.popcorntime.database.tables.Downloads;
import com.ppinera.popcorntv.model.DownloadInfo;
import com.ppinera.popcorntv.torrent.TorrentService;
import com.ppinera.popcorntv.ui_mobile.base.PopcornBaseActivity;

public class DownloadsActivity extends PopcornBaseActivity implements OnClickListener, LoaderCallbacks<Cursor> {

	public static final String VIDEO_URL = "video_url";

	private boolean init;
	private DownloadsAdapter mDownloadsAdapter;

	// view
	private TextView headerTitle;
	private ListView downloadsList;

	private RemoveAllDialog mRemoveAllDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);
		super.onCreate(savedInstanceState);
		init = getIntent().hasExtra(VIDEO_URL);

		// Header
		getPopcornLogoView().setVisibility(View.GONE);
		View header = setPopcornHeaderView(R.layout.header_downloads);
		header.findViewById(R.id.header_back).setOnClickListener(DownloadsActivity.this);
		header.findViewById(R.id.header_overflow).setOnClickListener(DownloadsActivity.this);
		headerTitle = (TextView) header.findViewById(R.id.header_title);

		// Content
		View content = setPopcornContentView(R.layout.activity_downloads);

		mDownloadsAdapter = new DownloadsAdapter(DownloadsActivity.this, null, false);
		downloadsList = (ListView) content.findViewById(R.id.downloads_list);
		downloadsList.setAdapter(mDownloadsAdapter);

		updateLocaleText();
		getLoaderManager().initLoader(0, null, DownloadsActivity.this);
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		headerTitle.setText(R.string.downloads);
		mDownloadsAdapter.updateLocaleText();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_back:
			onBackPressed();
			break;
		case R.id.header_overflow:
			onOverflowPressed(v);
			break;
		default:
			break;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(DownloadsActivity.this, Downloads.CONTENT_URI, null, null, null, Downloads._ID + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mDownloadsAdapter.swapCursor(cursor);
		if (init) {
			init = false;
			int position = 0;
			String url = getIntent().getStringExtra(VIDEO_URL);
			DownloadInfo downloadInfo = new DownloadInfo();
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				try {
					downloadInfo.populate(cursor);
					if (url.equals(downloadInfo.torrentUrl)) {
						position = i;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (0 != position) {
				setSelection(position);
			}
		}
	}

	private void setSelection(final int position) {
		downloadsList.post(new Runnable() {

			@Override
			public void run() {
				downloadsList.setSelection(position);
			}

		});
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mDownloadsAdapter.swapCursor(null);
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		super.onBackPressed();
	}

	private void onOverflowPressed(View v) {
		PopupMenu popup = new PopupMenu(DownloadsActivity.this, v);
		popup.setOnMenuItemClickListener(overflowMenuListener);
		popup.inflate(R.menu.popup_downloads);
		popup.show();
	}

	private void showRemoveAllDialog() {
		if (null == mRemoveAllDialog) {
			mRemoveAllDialog = new RemoveAllDialog();
		}
		if (!mRemoveAllDialog.isAdded()) {
			mRemoveAllDialog.show(getFragmentManager(), "downloads_remove_all_dialog");
		}
	}

	/*
	 * Listeners
	 */

	private OnMenuItemClickListener overflowMenuListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.downloads_resume_all:
				TorrentService.resumeAll(DownloadsActivity.this);
				return true;
			case R.id.downloads_pause_all:
				TorrentService.pauseAll(DownloadsActivity.this);
				return true;
			case R.id.downloads_remove_all:
				showRemoveAllDialog();
				return true;
			default:
				return false;
			}
		}
	};

	/*
	 * Dialogs
	 */

	public class RemoveAllDialog extends DialogFragment {

		public RemoveAllDialog() {

		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.remove_all);
			builder.setMessage(R.string.downloads_remove_msg);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					TorrentService.removeAll(getApplicationContext());
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});

			AlertDialog dialog = builder.create();
			dialog.show();

			return dialog;
		}
	}

}