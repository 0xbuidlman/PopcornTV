package com.ppinera.popcorntv.ui_mobile;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.controller.FavoritesAdapter;
import dp.ws.popcorntime.database.tables.Favorites;
import com.ppinera.popcorntv.ui_mobile.locale.LocaleFragment;

public class GridFavoritesFragment extends LocaleFragment implements LoaderCallbacks<Cursor> {

	private GridView videoGrid;
	private FavoritesAdapter mFavoritesAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFavoritesAdapter = new FavoritesAdapter(getActivity(), null, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_grid_video, container, false);
		populateView(view);
		videoGrid.post(new Runnable() {

			@Override
			public void run() {
				videoGrid.setSelection(0);
			}
		});
		getLoaderManager().initLoader(0, null, GridFavoritesFragment.this);
		return view;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// change orientation
		ViewGroup container = (ViewGroup) getView();
		container.removeAllViewsInLayout();
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_grid_video, container);
		populateView(view);
	}

	private void populateView(View view) {
		videoGrid = (GridView) view.findViewById(R.id.videos_content_view);
		videoGrid.setAdapter(mFavoritesAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Favorites.CONTENT_URI, null, null, null, Favorites._ID + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mFavoritesAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mFavoritesAdapter.swapCursor(null);
	}
}
