package com.ppinera.popcorntv.ui_mobile;

import org.videolan.libvlc.LibVLC;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.ppinera.popcorntv.PopcornApplication;
import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.subtitles.Subtitles;
import com.ppinera.popcorntv.torrent.TorrentService;
import com.ppinera.popcorntv.ui_mobile.base.PlayerBaseActivity;
import com.ppinera.popcorntv.ui_mobile.base.PopcornBaseActivity;
import com.ppinera.popcorntv.utils.LanguageUtil;
import com.ppinera.popcorntv.utils.StorageHelper;

public class SettingsActivity extends PopcornBaseActivity {

	private final int REQUEST_DIRECTORY = 3457;

	private PopcornApplication mApplication;
	private SharedPreferences preferences;

	// language
	private LanguageDialog languageDialog;

	// theme
	private String[] themes;
	private ThemeDialog themeDialog;

	// hardware acceleration
	private String[] accelerations;
	private final int[] accelerationCode = new int[] { LibVLC.HW_ACCELERATION_AUTOMATIC, LibVLC.HW_ACCELERATION_DISABLED, LibVLC.HW_ACCELERATION_DECODING,
			LibVLC.HW_ACCELERATION_FULL };
	private HwAccelerationDialog accelerationDialog;

	// subtitles
	private String[] fontSizeNames;
	private String[] fontColorNames;
	private SubtitleLanguageDialog subtitleLanguageDialog;
	private SubtitleFontSizeDialog subtitleFontSizeDialog;
	private SubtitleFontColorDialog subtitleFontColorDialog;

	// view
	private TextView headerTitle;
	private TextView interfaceTitle;
	private TextView languageTitle;
	private TextView languageSummary;
	private TextView themeTitle;
	private TextView themeSummary;
	private TextView playerTitle;
	private TextView hwAccelerationTitle;
	private TextView hwAccelerationSummary;
	private TextView subtitlesTitle;
	private TextView subtitlesLanguageTitle;
	private TextView subtitlesLanguageSummary;
	private TextView subtitlesFontSizeTitle;
	private TextView subtitlesFontSizeSummary;
	private TextView subtitlesFontColorTitle;
	private TextView subtitlesFontColorSummary;
	private TextView downloadsTitle;
	private TextView vpnTitle;
	private TextView vpnSummary;
	private ImageView vpnSecureIcon;
	private TextView chacheFolderTitle;
	private TextView chacheFolderSummary;
	private TextView clearChacheFolderOnExitTitle;
	private CheckBox clearChacheFolderOnExitCheckbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);
		super.onCreate(savedInstanceState);

		mApplication = (PopcornApplication) getApplication();
		preferences = getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);

		// Header
		getPopcornLogoView().setVisibility(View.GONE);
		View header = setPopcornHeaderView(R.layout.header_settings);
		header.findViewById(R.id.header_back).setOnClickListener(backListener);
		headerTitle = (TextView) header.findViewById(R.id.header_title);

		// Content
		View content = setPopcornContentView(R.layout.activity_settings);

		/*
		 * INTERFACE
		 */
		interfaceTitle = (TextView) content.findViewById(R.id.settings_interface_title);

		View language = content.findViewById(R.id.settings_language);
		addSettingsItem(language, languageListener);
		languageTitle = (TextView) language.findViewById(R.id.settings_language_title);
		languageSummary = (TextView) language.findViewById(R.id.settings_language_summary);

		View theme = content.findViewById(R.id.settings_theme);
		addSettingsItem(theme, themeListener);
		themeTitle = (TextView) theme.findViewById(R.id.settings_theme_title);
		themeSummary = (TextView) theme.findViewById(R.id.settings_theme_summary);

		/*
		 * PLAYER
		 */
		playerTitle = (TextView) content.findViewById(R.id.settings_player_title);

		View hwAcceleration = content.findViewById(R.id.settings_hw_acceleration);
		addSettingsItem(hwAcceleration, hwAccelerationListener);
		hwAccelerationTitle = (TextView) hwAcceleration.findViewById(R.id.settings_hw_acceleration_title);
		hwAccelerationSummary = (TextView) hwAcceleration.findViewById(R.id.settings_hw_acceleration_summary);

		/*
		 * Subtitles
		 */
		subtitlesTitle = (TextView) content.findViewById(R.id.settings_subtitles_title);

		View subtatlesLanguage = content.findViewById(R.id.settings_subtitles_language);
		addSettingsItem(subtatlesLanguage, subtitleLanguageListener);
		subtitlesLanguageTitle = (TextView) subtatlesLanguage.findViewById(R.id.settings_subtitles_language_title);
		subtitlesLanguageSummary = (TextView) subtatlesLanguage.findViewById(R.id.settings_subtitles_language_summary);

		View subtitlesFontSize = content.findViewById(R.id.settings_subtitles_font_size);
		addSettingsItem(subtitlesFontSize, subtitleFontSizeListener);
		subtitlesFontSizeTitle = (TextView) subtitlesFontSize.findViewById(R.id.settings_subtitles_font_size_title);
		subtitlesFontSizeSummary = (TextView) subtitlesFontSize.findViewById(R.id.settings_subtitles_font_size_summary);

		View subtitlesFontColor = content.findViewById(R.id.settings_subtitles_font_color);
		addSettingsItem(subtitlesFontColor, subtitleFontColorListener);
		subtitlesFontColorTitle = (TextView) subtitlesFontColor.findViewById(R.id.settings_subtitles_font_color_title);
		subtitlesFontColorSummary = (TextView) subtitlesFontColor.findViewById(R.id.settings_subtitles_font_color_summary);

		/*
		 * DOWNLOADS
		 */
		downloadsTitle = (TextView) content.findViewById(R.id.settings_downloads_title);

		View vpn = content.findViewById(R.id.settings_vpn);
		addSettingsItem(vpn, vpnListener);
		vpnTitle = (TextView) vpn.findViewById(R.id.settings_vpn_title);
		vpnSummary = (TextView) vpn.findViewById(R.id.settings_vpn_summary);
		View vpnSponsor = vpn.findViewById(R.id.settings_vpn_sponsor);
		vpnSponsor.setOnClickListener(vpnSponsorListener);
		vpnSecureIcon = (ImageView) vpn.findViewById(R.id.settings_vpn_secure_icon);
		boolean vpn_enable = preferences.getBoolean(TorrentService.IS_PROXY_ENABLE_KEY, TorrentService.PROXY_DEFAULT);
		vpnSecureIcon.setImageResource(vpn_enable ? R.drawable.ic_action_secure : R.drawable.ic_action_not_secure);

		View chacheFolder = content.findViewById(R.id.settings_cache_folder);
		addSettingsItem(chacheFolder, chacheListener);
		chacheFolderTitle = (TextView) chacheFolder.findViewById(R.id.settings_cache_folder_title);
		chacheFolderSummary = (TextView) chacheFolder.findViewById(R.id.settings_cache_folder_summary);

		View clearChacheFolderOnExit = content.findViewById(R.id.settings_clear_cache_folder_on_exit);
		addSettingsItem(clearChacheFolderOnExit, clearChacheOnExitListener);
		clearChacheFolderOnExitTitle = (TextView) clearChacheFolderOnExit.findViewById(R.id.settings_clear_cache_folder_on_exit_title);
		clearChacheFolderOnExitCheckbox = (CheckBox) clearChacheFolderOnExit.findViewById(R.id.settings_clear_cache_folder_on_exit_checkbox);
		clearChacheFolderOnExitCheckbox.setChecked(preferences.getBoolean(PopcornApplication.CLEAR_ON_EXIT, true));
		clearChacheFolderOnExitCheckbox.setOnCheckedChangeListener(clearChacheOnExitCheckedListener);

		updateLocaleText();
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		themes = getResources().getStringArray(R.array.themes);
		accelerations = getResources().getStringArray(R.array.accelerations);
		fontSizeNames = getResources().getStringArray(R.array.font_size_names);
		fontColorNames = getResources().getStringArray(R.array.font_color_names);
		LanguageUtil.SUBTITLE_NATIVE_LANGUAGES[0] = getString(R.string.without_subtitle);
		headerTitle.setText(R.string.settings);
		interfaceTitle.setText(R.string.interface_);
		languageTitle.setText(R.string.language);
		languageSummary.setText(LanguageUtil.isoToNativeLanguage(mApplication.getAppLocale().getLanguage()));
		themeTitle.setText(R.string.theme);
		themeSummary.setText(getCurrentTheme());
		playerTitle.setText(R.string.player);
		hwAccelerationTitle.setText(R.string.hardware_acceleration);
		hwAccelerationSummary.setText(getCurrentAccelerationDesc());
		subtitlesTitle.setText(R.string.subtitles);
		subtitlesLanguageTitle.setText(R.string.default_subtitle);
		String subLang = mApplication.getSubtitleLanguage();
		if ("".equals(subLang)) {
			subtitlesLanguageSummary.setText(R.string.without_subtitle);
		} else {
			subtitlesLanguageSummary.setText(LanguageUtil.languageToNativeLanguage(subLang));
		}
		subtitlesFontSizeTitle.setText(R.string.font_size);
		subtitlesFontSizeSummary.setText(getCurrentFontSizeName());
		subtitlesFontColorTitle.setText(R.string.font_color);
		subtitlesFontColorSummary.setText(getCurrentFontColorName());
		downloadsTitle.setText(R.string.downloads);
		boolean vpn_enable = preferences.getBoolean(TorrentService.IS_PROXY_ENABLE_KEY, TorrentService.PROXY_DEFAULT);
		vpnTitle.setText(vpn_enable ? R.string.vpn_enabled : R.string.vpn_disabled);
		vpnSummary.setText(R.string.sponsored_by);
		chacheFolderTitle.setText(R.string.cache_folder);
		chacheFolderSummary.setText(getCacheFolderPath());
		clearChacheFolderOnExitTitle.setText(R.string.clear_cache_folder_on_exit);
	}

	private String getCurrentAccelerationDesc() {
		int hw_acc = preferences.getInt(PlayerBaseActivity.SETTINGS_HW_ACCELERATION, LibVLC.HW_ACCELERATION_AUTOMATIC);
		return getAccelerationDesc(hw_acc);
	}

	private String getAccelerationDesc(int code) {
		switch (code) {
		case LibVLC.HW_ACCELERATION_AUTOMATIC:
			return getString(R.string.automatic);
		case LibVLC.HW_ACCELERATION_DECODING:
			return getString(R.string.hardware_acceleration_decoding);
		case LibVLC.HW_ACCELERATION_DISABLED:
			return getString(R.string.hardware_acceleration_disabled);
		case LibVLC.HW_ACCELERATION_FULL:
			return getString(R.string.hardware_acceleration_full);
		default:
			return "none";
		}
	}

	private int getCurrentAccelerationPosition() {
		int hw_acc = preferences.getInt(PlayerBaseActivity.SETTINGS_HW_ACCELERATION, LibVLC.HW_ACCELERATION_AUTOMATIC);
		for (int i = 0; i < accelerationCode.length; i++) {
			if (accelerationCode[i] == hw_acc) {
				return i;
			}
		}

		preferences.edit().putInt(PlayerBaseActivity.SETTINGS_HW_ACCELERATION, LibVLC.HW_ACCELERATION_AUTOMATIC).commit();
		return 0;
	}

	private String getCurrentTheme() {
		return getString(R.string.theme_classic);
	}

	private int getCurrentSubtitleLanguagePosition() {
		String subLang = mApplication.getSubtitleLanguage();
		for (int i = 0; i < LanguageUtil.SUBTITLE_LANGUAGES.length; i++) {
			if (subLang.equals(LanguageUtil.SUBTITLE_LANGUAGES[i])) {
				return i;
			}
		}
		return 0;
	}

	private String getCurrentFontSizeName() {
		int pos = preferences.getInt(Subtitles.FONT_SIZE_PREF, Subtitles.FontSize.DEFAULT_POSITION);
		if (pos < fontSizeNames.length) {
			return fontSizeNames[pos];
		} else {
			preferences.edit().putInt(Subtitles.FONT_SIZE_PREF, Subtitles.FontSize.DEFAULT_POSITION).commit();
			return fontSizeNames[Subtitles.FontSize.DEFAULT_POSITION];
		}
	}

	private String getCurrentFontColorName() {
		int pos = preferences.getInt(Subtitles.FONT_COLOR_PREF, Subtitles.FontColor.DEFAULT_POSITION);
		if (pos < fontColorNames.length) {
			return fontColorNames[pos];
		} else {
			preferences.edit().putInt(Subtitles.FONT_COLOR_PREF, Subtitles.FontColor.DEFAULT_POSITION).commit();
			return fontColorNames[Subtitles.FontColor.DEFAULT_POSITION];
		}
	}

	private String getCacheFolderPath() {
		String folderSummary = StorageHelper.getInstance().getRootDirectoryPath();
		if (TextUtils.isEmpty(folderSummary)) {
			return getResources().getString(R.string.cache_folder_not_selected);
		}
		return folderSummary;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (REQUEST_DIRECTORY == requestCode) {
				String path = data.getStringExtra(FolderChooserActivity.SELECTED_DIR);
				StorageHelper.getInstance().setRootDirectory(SettingsActivity.this, path);
				chacheFolderSummary.setText(getCacheFolderPath());
			}
		}
	}

	private void addSettingsItem(View view, OnClickListener clickListener) {
		view.setOnClickListener(clickListener);
		view.setOnTouchListener(itemTouchListener);
	}

	/*
	 * TODO: Listeners
	 */

	private OnTouchListener itemTouchListener = new OnTouchListener() {

		private View mView = null;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (mView == null) {
					mView = v;
					v.setPressed(true);
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mView != null && mView.equals(v)) {
					mView.performClick();
					mView.setPressed(false);
					mView = null;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				if (mView != null) {
					mView.setPressed(false);
					mView = null;
				}
				break;
			default:
				break;
			}

			return true;
		}

	};

	private OnClickListener backListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	private OnClickListener languageListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (languageDialog == null) {
				languageDialog = new LanguageDialog();
			}
			if (!languageDialog.isAdded()) {
				languageDialog.show(getFragmentManager(), "language_dialog");
			}
		}
	};

	private OnClickListener themeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (themeDialog == null) {
				themeDialog = new ThemeDialog();
			}
			if (!themeDialog.isAdded()) {
				themeDialog.show(getFragmentManager(), "theme_dialog");
			}
		}
	};

	private OnClickListener hwAccelerationListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (accelerationDialog == null) {
				accelerationDialog = new HwAccelerationDialog();
			}
			if (!accelerationDialog.isAdded()) {
				accelerationDialog.show(getFragmentManager(), "hw_acceleration_dialog");
			}
		}
	};

	private OnClickListener subtitleLanguageListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (subtitleLanguageDialog == null) {
				subtitleLanguageDialog = new SubtitleLanguageDialog();
			}
			if (!subtitleLanguageDialog.isAdded()) {
				subtitleLanguageDialog.show(getFragmentManager(), "subtitles_lang_dialog");
			}
		}
	};

	private OnClickListener subtitleFontSizeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (subtitleFontSizeDialog == null) {
				subtitleFontSizeDialog = new SubtitleFontSizeDialog();
			}
			if (!subtitleFontSizeDialog.isAdded()) {
				subtitleFontSizeDialog.show(getFragmentManager(), "subtitles_font_size_dialog");
			}
		}
	};

	private OnClickListener subtitleFontColorListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (subtitleFontColorDialog == null) {
				subtitleFontColorDialog = new SubtitleFontColorDialog();
			}
			if (!subtitleFontColorDialog.isAdded()) {
				subtitleFontColorDialog.show(getFragmentManager(), "subtitles_font_color_dialog");
			}
		}
	};

	private OnClickListener vpnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			boolean enabled = !preferences.getBoolean(TorrentService.IS_PROXY_ENABLE_KEY, TorrentService.PROXY_DEFAULT);
			preferences.edit().putBoolean(TorrentService.IS_PROXY_ENABLE_KEY, enabled).commit();
			vpnTitle.setText(enabled ? R.string.vpn_enabled : R.string.vpn_disabled);
			vpnSecureIcon.setImageResource(enabled ? R.drawable.ic_action_secure : R.drawable.ic_action_not_secure);
			TorrentService.setProxy(enabled);
		}
	};

	private OnClickListener vpnSponsorListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://kebrum.com/popcorntime")));
		}
	};

	private OnClickListener chacheListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent chooserIntent = new Intent(SettingsActivity.this, FolderChooserActivity.class);
			startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
		}
	};

	private OnClickListener clearChacheOnExitListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			clearChacheFolderOnExitCheckbox.setChecked(!clearChacheFolderOnExitCheckbox.isChecked());
		}
	};

	private OnCheckedChangeListener clearChacheOnExitCheckedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			preferences.edit().putBoolean(PopcornApplication.CLEAR_ON_EXIT, isChecked).commit();
		}
	};

	/*
	 * TODO: Dialogs
	 */

	private class LanguageDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.language));
			builder.setItems(LanguageUtil.INTERFACE_NATIVE_LANGUAGES, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mApplication.changeLanguage(LanguageUtil.INTERFACE_ISO_LANGUAGES[which]);
					SettingsActivity.this.mLocaleHelper.checkLanguage();
				}
			});
			return builder.create();
		}
	}

	private class ThemeDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.theme));
			builder.setItems(themes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			return builder.create();
		}
	}

	private class HwAccelerationDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.hardware_acceleration));
			builder.setSingleChoiceItems(accelerations, getCurrentAccelerationPosition(), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					preferences.edit().putInt(PlayerBaseActivity.SETTINGS_HW_ACCELERATION, accelerationCode[which]).commit();
					hwAccelerationSummary.setText(getAccelerationDesc(accelerationCode[which]));
					dialog.dismiss();
				}
			});
			return builder.create();
		}
	}

	private class SubtitleLanguageDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.subtitles));
			builder.setSingleChoiceItems(LanguageUtil.SUBTITLE_NATIVE_LANGUAGES, getCurrentSubtitleLanguagePosition(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					subtitlesLanguageSummary.setText(LanguageUtil.SUBTITLE_NATIVE_LANGUAGES[which]);
					mApplication.setSubtitleLanguage(LanguageUtil.SUBTITLE_LANGUAGES[which]);
					dialog.dismiss();
				}
			});
			return builder.create();
		}
	}

	private class SubtitleFontSizeDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.font_size));
			int pos = preferences.getInt(Subtitles.FONT_SIZE_PREF, Subtitles.FontSize.DEFAULT_POSITION);
			builder.setSingleChoiceItems(fontSizeNames, pos, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					preferences.edit().putInt(Subtitles.FONT_SIZE_PREF, which).commit();
					subtitlesFontSizeSummary.setText(fontSizeNames[which]);
					dialog.dismiss();
				}
			});
			return builder.create();
		}
	}

	private class SubtitleFontColorDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.font_color));
			int pos = preferences.getInt(Subtitles.FONT_COLOR_PREF, Subtitles.FontColor.DEFAULT_POSITION);
			builder.setSingleChoiceItems(fontColorNames, pos, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					preferences.edit().putInt(Subtitles.FONT_COLOR_PREF, which).commit();
					subtitlesFontColorSummary.setText(fontColorNames[which]);
					dialog.dismiss();
				}
			});
			return builder.create();
		}
	}

}
