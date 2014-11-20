/*****************************************************************************
 * VideoPlayerActivity.java
 *****************************************************************************
 * Copyright © 2011-2014 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package com.ppinera.popcorntv.ui_mobile;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.libvlc.Media;
import org.videolan.vlc.MediaDatabase;
import org.videolan.vlc.VLCApplication;
import org.videolan.vlc.audio.AudioServiceController;
import org.videolan.vlc.gui.CommonDialogs;
import org.videolan.vlc.gui.CommonDialogs.MenuType;
import org.videolan.vlc.gui.PreferencesActivity;
import org.videolan.vlc.util.AndroidDevices;
import org.videolan.vlc.util.Strings;
import org.videolan.vlc.util.VLCInstance;
import org.videolan.vlc.util.WeakHandler;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.softwarrior.libtorrent.TorrentState;

import dp.ws.popcorntime.R;
import com.ppinera.popcorntv.ui_mobile.base.PlayerBaseActivity;

public class VLCPlayerActivity extends PlayerBaseActivity implements IVideoPlayer {
	public final static String TAG = "VLC/VideoPlayerActivity";

	private SurfaceView mSurface;
	private SurfaceView mSubtitlesSurface;
	private SurfaceHolder mSurfaceHolder;
	private SurfaceHolder mSubtitlesSurfaceHolder;
	private FrameLayout mSurfaceFrame;
	// private LibVLC mLibVLC;
	// private String mLocation;

	private static final int SURFACE_BEST_FIT = 0;
	private static final int SURFACE_FIT_HORIZONTAL = 1;
	private static final int SURFACE_FIT_VERTICAL = 2;
	private static final int SURFACE_FILL = 3;
	private static final int SURFACE_16_9 = 4;
	private static final int SURFACE_4_3 = 5;
	private static final int SURFACE_ORIGINAL = 6;
	private int mCurrentSize = SURFACE_BEST_FIT;

	private SharedPreferences mSettings;

	/** Overlay */
	private View mOverlayHeader;
	private View mOverlayOption;
	private View mOverlayProgress;
	// private View mOverlayBackground;
	private static final int OVERLAY_TIMEOUT = 4000;
	private static final int OVERLAY_INFINITE = 3600000;
	private static final int FADE_OUT = 1;
	private static final int SHOW_PROGRESS = 2;
	private static final int SURFACE_SIZE = 3;
	private static final int AUDIO_SERVICE_CONNECTION_SUCCESS = 5;
	private static final int AUDIO_SERVICE_CONNECTION_FAILED = 6;
	private static final int FADE_OUT_INFO = 4;
	private boolean mDragging;
	private boolean mShowing;
	private int mUiVisibility = -1;
	private SeekBar mSeekbar;
	// private TextView mTitle;
	private TextView mSysTime;
	private TextView mBattery;
	private TextView mTime;
	private TextView mLength;
	private TextView mInfo;
	// private ImageView mLoading;
	// private TextView mLoadingText;
	private ImageButton mPlayPause;
	private ImageButton mBackward;
	private ImageButton mForward;
	// private boolean mEnableJumpButtons;
	private boolean mEnableBrightnessGesture;
	// private boolean mEnableCloneMode;
	private boolean mDisplayRemainingTime = false;
	// private int mScreenOrientation;
	private ImageButton mAudioTrack;
	// private ImageButton mSubtitle;
	// private ImageButton mLock;
	private ImageButton mSize;
	// private ImageButton mMenu;
	private boolean mIsLocked = false;
	private int mLastAudioTrack = -1;
	private int mLastSpuTrack = -2;

	/**
	 * For uninterrupted switching between audio and video mode
	 */
	private boolean mSwitchingView;
	private boolean mEndReached;
	private boolean mCanSeek;

	// Playlist
	// private int savedIndexPosition = -1;

	// size of the video
	private int mVideoHeight;
	private int mVideoWidth;
	private int mVideoVisibleHeight;
	private int mVideoVisibleWidth;
	private int mSarNum;
	private int mSarDen;

	// Volume
	private OnAudioFocusChangeListener mAudioFocusListener;

	// Touch Events
	private static final int TOUCH_NONE = 0;
	private static final int TOUCH_VOLUME = 1;
	private static final int TOUCH_BRIGHTNESS = 2;
	private static final int TOUCH_SEEK = 3;
	private int mTouchAction;
	private int mSurfaceYDisplayRange;
	private float mTouchY, mTouchX, mVol;

	// Brightness
	private boolean mIsFirstBrightnessGesture = true;

	// Tracks & Subtitles
	private Map<Integer, String> mAudioTracksList;

	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);

		setContentView(R.layout.activity_video_player);

		if (LibVlcUtil.isICSOrLater())
			getWindow().getDecorView().findViewById(android.R.id.content).setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
				@Override
				public void onSystemUiVisibilityChange(int visibility) {
					if (visibility == mUiVisibility)
						return;
					setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
					if (visibility == View.SYSTEM_UI_FLAG_VISIBLE && !mShowing && !isFinishing()) {
						showOverlay();
					}
					mUiVisibility = visibility;
				}
			});

		/** initialize Views an their Events */
		mOverlayHeader = findViewById(R.id.player_overlay_header);
		mOverlayOption = findViewById(R.id.option_overlay);
		mOverlayProgress = findViewById(R.id.progress_overlay);
		// mOverlayBackground = findViewById(R.id.player_overlay_background);

		/* header */
		mTitle = (TextView) findViewById(R.id.player_overlay_title);
		mSysTime = (TextView) findViewById(R.id.player_overlay_systime);
		mBattery = (TextView) findViewById(R.id.player_overlay_battery);

		// Position and remaining time
		mTime = (TextView) findViewById(R.id.player_overlay_time);
		mTime.setOnClickListener(mRemainingTimeListener);
		mLength = (TextView) findViewById(R.id.player_overlay_length);
		mLength.setOnClickListener(mRemainingTimeListener);

		// the info textView is not on the overlay
		mInfo = (TextView) findViewById(R.id.player_overlay_info);

		mEnableBrightnessGesture = mSettings.getBoolean("enable_brightness_gesture", true);
		// mScreenOrientation =
		// Integer.valueOf(mSettings.getString("screen_orientation_value", "4"
		// /* SCREEN_ORIENTATION_SENSOR */));

		// mEnableJumpButtons = mSettings.getBoolean("enable_jump_buttons",
		// false);
		mPlayPause = (ImageButton) findViewById(R.id.player_overlay_play);
		mPlayPause.setOnClickListener(mPlayPauseListener);
		mBackward = (ImageButton) findViewById(R.id.player_overlay_backward);
		mBackward.setOnClickListener(mBackwardListener);
		mForward = (ImageButton) findViewById(R.id.player_overlay_forward);
		mForward.setOnClickListener(mForwardListener);

		mAudioTrack = (ImageButton) findViewById(R.id.player_overlay_audio);
		mAudioTrack.setVisibility(View.GONE);
		// mSubtitle = (ImageButton) findViewById(R.id.player_overlay_subtitle);
		// mSubtitle.setVisibility(View.GONE);
		// mNavMenu = (ImageButton) findViewById(R.id.player_overlay_navmenu);
		// mNavMenu.setVisibility(View.GONE);

		// mLock = (ImageButton) findViewById(R.id.lock_overlay_button);
		// mLock.setOnClickListener(mLockListener);

		mSize = (ImageButton) findViewById(R.id.player_overlay_size);
		mSize.setOnClickListener(mSizeListener);

		// mMenu = (ImageButton) findViewById(R.id.player_overlay_adv_function);

		try {
            LibVLC.getInstance().
			mLibVLC = VLCInstance.getLibVlcInstance();
			mLibVLC.setHardwareAcceleration(mPreferences.getInt(SETTINGS_HW_ACCELERATION, LibVLC.HW_ACCELERATION_AUTOMATIC));
			mLibVLC.setOnNativeCrashListener(new LibVLC.OnNativeCrashListener() {

				@Override
				public void onNativeCrash() {
					Log.w("tag", "onNativeCrash");
					Intent intent = getIntent();
					finish();
					startActivity(intent);
				}
			});
		} catch (LibVlcException e) {
			Log.d(TAG, "LibVLC initialisation failed");
			return;
		}

		mSurface = (SurfaceView) findViewById(R.id.player_surface);
		mSurfaceHolder = mSurface.getHolder();
		mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);
		String chroma = mSettings.getString("chroma_format", "");
		if (LibVlcUtil.isGingerbreadOrLater() && chroma.equals("YV12")) {
			mSurfaceHolder.setFormat(ImageFormat.YV12);
		} else if (chroma.equals("RV16")) {
			mSurfaceHolder.setFormat(PixelFormat.RGB_565);
		} else {
			mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		}
		mSurfaceHolder.addCallback(mSurfaceCallback);

		mSubtitlesSurface = (SurfaceView) findViewById(R.id.subtitles_surface);
		mSubtitlesSurfaceHolder = mSubtitlesSurface.getHolder();
		mSubtitlesSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
		mSubtitlesSurface.setZOrderMediaOverlay(true);
		mSubtitlesSurfaceHolder.addCallback(mSubtitlesSurfaceCallback);

		mSeekbar = (SeekBar) findViewById(R.id.player_overlay_seekbar);
		mSeekbar.setOnSeekBarChangeListener(mSeekListener);

		initPopcorn();

		/* Loading view */
		// mLoading = (ImageView) findViewById(R.id.player_overlay_loading);
		// mLoadingText = (TextView)
		// findViewById(R.id.player_overlay_loading_text);
		// startLoadingAnimation();

		mSwitchingView = false;
		mEndReached = false;

		// Clear the resume time, since it is only used for resumes in external
		// videos.
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putLong(PreferencesActivity.VIDEO_RESUME_TIME, -1);
		// Also clear the subs list, because it is supposed to be per session
		// only (like desktop VLC). We don't want the customs subtitle file
		// to persist forever with this video.
		editor.putString(PreferencesActivity.VIDEO_SUBTITLE_FILES, null);
		editor.commit();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(VLCApplication.SLEEP_INTENT);
		registerReceiver(mReceiver, filter);

		/* Only show the subtitles surface when using "Full Acceleration" mode */
		if (mLibVLC.getHardwareAcceleration() == LibVLC.HW_ACCELERATION_FULL)
			mSubtitlesSurface.setVisibility(View.VISIBLE);
		// Signal to LibVLC that the videoPlayerActivity was created, thus the
		// SurfaceView is now available for MediaCodec direct rendering.
		mLibVLC.eventVideoPlayerActivityCreated(true);

		EventHandler em = EventHandler.getInstance();
		em.addHandler(eventHandler);

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mSwitchingView) {
			AudioServiceController.getInstance().showWithoutParse(savedIndexPosition);
			AudioServiceController.getInstance().unbindAudioService(this);
			return;
		}

		long time = mLibVLC.getTime();
		long length = mLibVLC.getLength();
		// remove saved position if in the last 5 seconds
		if (length - time < 5000)
			time = 0;
		else
			time -= 5000; // go back 5 seconds, to compensate loading time

		/*
		 * Pausing here generates errors because the vout is constantly trying
		 * to refresh itself every 80ms while the surface is not accessible
		 * anymore. To workaround that, we keep the last known position in the
		 * playlist in savedIndexPosition to be able to restore it during
		 * onResume().
		 */
		mLibVLC.stop();

		mSurface.setKeepScreenOn(false);

		// Save position
		if (time >= 0) {
			mSettings.edit().putLong(PreferencesActivity.VIDEO_RESUME_TIME, time).commit();
		}

		AudioServiceController.getInstance().unbindAudioService(this);
	}

	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(mReceiver);

			EventHandler em = EventHandler.getInstance();
			em.removeHandler(eventHandler);

			// MediaCodec opaque direct rendering should not be used anymore
			// since
			// there is no surface to attach.
			mLibVLC.eventVideoPlayerActivityCreated(false);
			// HW acceleration was temporarily disabled because of an error,
			// restore
			// the previous value.
			// if (mDisabledHardwareAcceleration)
			// mLibVLC.setHardwareAcceleration(mPreviousHardwareAccelerationMode);

			mAudioManager = null;
		} catch (Exception ex) {
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (isResume()) {
			mSwitchingView = false;
			AudioServiceController.getInstance().bindAudioService(this, new AudioServiceController.AudioServiceConnectionListener() {
				@Override
				public void onConnectionSuccess() {
					mHandler.sendEmptyMessage(AUDIO_SERVICE_CONNECTION_SUCCESS);
				}

				@Override
				public void onConnectionFailed() {
					mHandler.sendEmptyMessage(AUDIO_SERVICE_CONNECTION_FAILED);
				}
			});
		}
	}

	/*
	 * TODO: Torrent
	 */

	private void startPlayback() {
		mSurface.setKeepScreenOn(true);
		if (isTorrentVideoReady) {
			mLibVLC.playIndex(savedIndexPosition);
			// resume video play
			long rTime = mSettings.getLong(PreferencesActivity.VIDEO_RESUME_TIME, -1);
			mSettings.edit().putLong(PreferencesActivity.VIDEO_RESUME_TIME, -1).commit();
			if (rTime > 0) {
				mLibVLC.setTime(rTime);
			}
		} else {
			loadVideo();
		}
	}

	private void errorReplay() {
		Log.w("tag", "errorPlay");
		long time = mLibVLC.getTime() - 15000;
		if (time < 0) {
			time = 0;
		}
		mLibVLC.stop();
		popcornPlay(time);
	}

	/*
	 * TODO: Chromecast
	 */

	@Override
	public void onCastStatePlaying() {
		super.onCastStatePlaying();
		updateOverlayPausePlay();
	}

	@Override
	public void onCastStatePaused() {
		super.onCastStatePaused();
		updateOverlayPausePlay();
	}

	// ================= VLC ======================

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
				int batteryLevel = intent.getIntExtra("level", 0);
				if (batteryLevel >= 50)
					mBattery.setTextColor(Color.GREEN);
				else if (batteryLevel >= 30)
					mBattery.setTextColor(Color.YELLOW);
				else
					mBattery.setTextColor(Color.RED);
				mBattery.setText(String.format("%d%%", batteryLevel));
			} else if (action.equalsIgnoreCase(VLCApplication.SLEEP_INTENT)) {
				finish();
			}
		}
	};

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		showOverlay();
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
		if (width * height == 0)
			return;

		// store video size
		mVideoHeight = height;
		mVideoWidth = width;
		mVideoVisibleHeight = visible_height;
		mVideoVisibleWidth = visible_width;
		mSarNum = sar_num;
		mSarDen = sar_den;
		Message msg = mHandler.obtainMessage(SURFACE_SIZE);
		mHandler.sendMessage(msg);
	}

	// /**
	// * Lock screen rotation
	// */
	// private void lockScreen() {
	// if (mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
	// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
	// setRequestedOrientation(14 /* SCREEN_ORIENTATION_LOCKED */);
	// else
	// setRequestedOrientation(getScreenOrientation());
	// }
	// showInfo(R.string.locked, 1000);
	// mLock.setBackgroundResource(R.drawable.ic_locked);
	// mTime.setEnabled(false);
	// mSeekbar.setEnabled(false);
	// mLength.setEnabled(false);
	// hideOverlay(true);
	// }
	//
	// /**
	// * Remove screen lock
	// */
	// private void unlockScreen() {
	// if (mScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
	// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	// showInfo(R.string.unlocked, 1000);
	// mLock.setBackgroundResource(R.drawable.ic_lock);
	// mTime.setEnabled(true);
	// mSeekbar.setEnabled(true);
	// mLength.setEnabled(true);
	// mShowing = false;
	// showOverlay();
	// }

	/**
	 * Show text in the info view for "duration" milliseconds
	 * 
	 * @param text
	 * @param duration
	 */
	private void showInfo(String text, int duration) {
		mInfo.setVisibility(View.VISIBLE);
		mInfo.setText(text);
		mHandler.removeMessages(FADE_OUT_INFO);
		mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
	}

	private void showInfo(int textid, int duration) {
		mInfo.setVisibility(View.VISIBLE);
		mInfo.setText(textid);
		mHandler.removeMessages(FADE_OUT_INFO);
		mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
	}

	/**
	 * Show text in the info view
	 * 
	 * @param text
	 */
	private void showInfo(String text) {
		mInfo.setVisibility(View.VISIBLE);
		mInfo.setText(text);
		mHandler.removeMessages(FADE_OUT_INFO);
	}

	/**
	 * hide the info view with "delay" milliseconds delay
	 * 
	 * @param delay
	 */
	private void hideInfo(int delay) {
		mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, delay);
	}

	/**
	 * hide the info view
	 */
	private void hideInfo() {
		hideInfo(0);
	}

	private void fadeOutInfo() {
		if (mInfo.getVisibility() == View.VISIBLE)
			mInfo.startAnimation(AnimationUtils.loadAnimation(VLCPlayerActivity.this, android.R.anim.fade_out));
		mInfo.setVisibility(View.INVISIBLE);
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private int changeAudioFocus(boolean acquire) {
		if (!LibVlcUtil.isFroyoOrLater()) // NOP if not supported
			return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

		if (mAudioFocusListener == null) {
			mAudioFocusListener = new OnAudioFocusChangeListener() {
				@Override
				public void onAudioFocusChange(int focusChange) {
					/*
					 * Pause playback during alerts and notifications
					 */
					switch (focusChange) {
					case AudioManager.AUDIOFOCUS_LOSS:
					case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
						if (mLibVLC.isPlaying())
							mLibVLC.pause();
						break;
					case AudioManager.AUDIOFOCUS_GAIN:
					case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
					case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
						if (!mLibVLC.isPlaying())
							mLibVLC.play();
						break;
					}
				}
			};
		}

		int result;
		if (acquire) {
			result = mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			mAudioManager.setParameters("bgm_state=true");
		} else {
			if (mAudioManager != null) {
				result = mAudioManager.abandonAudioFocus(mAudioFocusListener);
				mAudioManager.setParameters("bgm_state=false");
			} else
				result = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
		}

		return result;
	}

	/**
	 * Handle libvlc asynchronous events
	 */
	private final Handler eventHandler = new VideoPlayerEventHandler(this);

	private static class VideoPlayerEventHandler extends WeakHandler<VLCPlayerActivity> {
		public VideoPlayerEventHandler(VLCPlayerActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			VLCPlayerActivity activity = getOwner();
			if (activity == null)
				return;
			// Do not handle events if we are leaving the VideoPlayerActivity
			if (activity.mSwitchingView)
				return;

			switch (msg.getData().getInt("event")) {
			case EventHandler.MediaParsedChanged:
				Log.i(TAG, "MediaParsedChanged");
				// activity.updateNavStatus();
				// if (!activity.mHasMenu &&
				// activity.mLibVLC.getVideoTracksCount() < 1) {
				// Log.i(TAG, "No video track, open in audio mode");
				// activity.switchToAudioMode();
				// }
				if (activity.mLibVLC.getVideoTracksCount() < 1) {
					Log.w("tag", "MediaParsedChanged: no video track");
					activity.reloadVideo();
				}
				break;
			case EventHandler.MediaPlayerPlaying:
				Log.i(TAG, "MediaPlayerPlaying");
				// activity.stopLoadingAnimation();
				// activity.showOverlay();
				activity.setESTrackLists(true);
				activity.setESTracks();
				activity.changeAudioFocus(true);
				if (activity.isCastEnabled) {
					activity.mLibVLC.pause();
				}
				// activity.updateNavStatus();
				break;
			case EventHandler.MediaPlayerPaused:
				Log.i(TAG, "MediaPlayerPaused");
				break;
			case EventHandler.MediaPlayerStopped:
				Log.i(TAG, "MediaPlayerStopped");
				activity.changeAudioFocus(false);
				break;
			case EventHandler.MediaPlayerEndReached:
				Log.i(TAG, "MediaPlayerEndReached");
				if (TorrentState.FINISHED == activity.getTorrentState()) {
					activity.changeAudioFocus(false);
					activity.endReached();
				} else {
					Log.w("tag", "MediaPlayerEndReached: torrent not finished");
				}
				break;
			case EventHandler.MediaPlayerVout:
				// activity.updateNavStatus();
				// if (!activity.mHasMenu)
				activity.handleVout(msg);
				break;
			case EventHandler.MediaPlayerPositionChanged:
				if (!activity.mCanSeek)
					activity.mCanSeek = true;
				// don't spam the logs
				break;
			case EventHandler.MediaPlayerEncounteredError:
				Log.i(TAG, "MediaPlayerEncounteredError");
				activity.encounteredError();
				break;
			case EventHandler.HardwareAccelerationError:
				Log.i(TAG, "HardwareAccelerationError");
				activity.handleHardwareAccelerationError();
				break;
			default:
				// Log.e(TAG, String.format("Event not handled (0x%x)",
				// msg.getData().getInt("event")));
				break;
			}
			activity.updateOverlayPausePlay();
		}
	};

	/**
	 * Handle resize of the surface and the overlay
	 */
	private final Handler mHandler = new VideoPlayerHandler(this);

	private static class VideoPlayerHandler extends WeakHandler<VLCPlayerActivity> {
		public VideoPlayerHandler(VLCPlayerActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			VLCPlayerActivity activity = getOwner();
			if (activity == null) // WeakReference could be GC'ed early
				return;

			switch (msg.what) {
			case FADE_OUT:
				activity.hideOverlay(false);
				break;
			case SHOW_PROGRESS:
				int pos = activity.setOverlayProgress();
				if (activity.canShowProgress()) {
					msg = obtainMessage(SHOW_PROGRESS);
					sendMessageDelayed(msg, 1000 - (pos % 1000));
				}
				break;
			case SURFACE_SIZE:
				activity.changeSurfaceSize();
				break;
			case FADE_OUT_INFO:
				activity.fadeOutInfo();
				break;
			case AUDIO_SERVICE_CONNECTION_SUCCESS:
				activity.startPlayback();
				break;
			case AUDIO_SERVICE_CONNECTION_FAILED:
				activity.finish();
				break;
			}
		}
	};

	private boolean canShowProgress() {
		return !mDragging && mShowing && mLibVLC.isPlaying();
	}

	private void endReached() {
		mEndReached = true;
		finish();
	}

	private void encounteredError() {
		/* Encountered Error, exit player with a message */
		AlertDialog dialog = new AlertDialog.Builder(VLCPlayerActivity.this).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		}).setTitle(R.string.encountered_error_title).setMessage(R.string.encountered_error_message).create();
		dialog.show();
	}

	public void eventHardwareAccelerationError() {
		EventHandler em = EventHandler.getInstance();
		em.callback(EventHandler.HardwareAccelerationError, new Bundle());
	}

	private void handleHardwareAccelerationError() {
		Log.w("tag", "handleHardwareAccelerationError");
		mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
		errorReplay();
	}

	private void handleVout(Message msg) {
		if (msg.getData().getInt("data") == 0 && !mEndReached) {
			/* Video track lost, open in audio mode */
			Log.i(TAG, "Video track lost, switching to audio");
			mSwitchingView = true;
			finish();
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void changeSurfaceSize() {
		int sw;
		int sh;

		// get screen size
		// if (mPresentation == null) {
		sw = getWindow().getDecorView().getWidth();
		sh = getWindow().getDecorView().getHeight();
		// } else {
		// sw = mPresentation.getWindow().getDecorView().getWidth();
		// sh = mPresentation.getWindow().getDecorView().getHeight();
		// }

		double dw = sw, dh = sh;
		boolean isPortrait;

		// if (mPresentation == null) {
		// getWindow().getDecorView() doesn't always take orientation into
		// account, we have to correct the values
		isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		// } else {
		// isPortrait = false;
		// }

		if (sw > sh && isPortrait || sw < sh && !isPortrait) {
			dw = sh;
			dh = sw;
		}

		// sanity check
		if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
			Log.e(TAG, "Invalid surface size");
			return;
		}

		// compute the aspect ratio
		double ar, vw;
		if (mSarDen == mSarNum) {
			/* No indication about the density, assuming 1:1 */
			vw = mVideoVisibleWidth;
			ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
		} else {
			/* Use the specified aspect ratio */
			vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
			ar = vw / mVideoVisibleHeight;
		}

		// compute the display aspect ratio
		double dar = dw / dh;

		switch (mCurrentSize) {
		case SURFACE_BEST_FIT:
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_FIT_HORIZONTAL:
			dh = dw / ar;
			break;
		case SURFACE_FIT_VERTICAL:
			dw = dh * ar;
			break;
		case SURFACE_FILL:
			break;
		case SURFACE_16_9:
			ar = 16.0 / 9.0;
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_4_3:
			ar = 4.0 / 3.0;
			if (dar < ar)
				dh = dw / ar;
			else
				dw = dh * ar;
			break;
		case SURFACE_ORIGINAL:
			dh = mVideoVisibleHeight;
			dw = vw;
			break;
		}

		SurfaceView surface;
		SurfaceView subtitlesSurface;
		SurfaceHolder surfaceHolder;
		SurfaceHolder subtitlesSurfaceHolder;
		FrameLayout surfaceFrame;

		// if (mPresentation == null) {
		surface = mSurface;
		subtitlesSurface = mSubtitlesSurface;
		surfaceHolder = mSurfaceHolder;
		subtitlesSurfaceHolder = mSubtitlesSurfaceHolder;
		surfaceFrame = mSurfaceFrame;

		// force surface buffer size
		surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
		subtitlesSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

		// set display size
		LayoutParams lp = surface.getLayoutParams();
		lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
		lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
		surface.setLayoutParams(lp);
		subtitlesSurface.setLayoutParams(lp);

		// set frame size (crop if necessary)
		lp = surfaceFrame.getLayoutParams();
		lp.width = (int) Math.floor(dw);
		lp.height = (int) Math.floor(dh);
		surfaceFrame.setLayoutParams(lp);

		surface.invalidate();
		subtitlesSurface.invalidate();
	}

	/**
	 * show/hide the overlay
	 */

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isTorrentVideoReady) {
			return false;
		}

		if (mIsLocked) {
			// locked, only handle show/hide & ignore all actions
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (!mShowing) {
					showOverlay();
				} else {
					hideOverlay(true);
				}
			}
			return false;
		}

		DisplayMetrics screen = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(screen);

		if (mSurfaceYDisplayRange == 0)
			mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);

		float y_changed = event.getRawY() - mTouchY;
		float x_changed = event.getRawX() - mTouchX;

		// coef is the gradient's move to determine a neutral zone
		float coef = Math.abs(y_changed / x_changed);
		float xgesturesize = ((x_changed / screen.xdpi) * 2.54f);

		/* Offset for Mouse Events */
		int[] offset = new int[2];
		mSurface.getLocationOnScreen(offset);
		int xTouch = Math.round((event.getRawX() - offset[0]) * mVideoWidth / mSurface.getWidth());
		int yTouch = Math.round((event.getRawY() - offset[1]) * mVideoHeight / mSurface.getHeight());

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			// Audio
			mTouchY = event.getRawY();
			mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			mTouchAction = TOUCH_NONE;
			// Seek
			mTouchX = event.getRawX();
			// Mouse events for the core
			LibVLC.sendMouseEvent(MotionEvent.ACTION_DOWN, 0, xTouch, yTouch);
			break;

		case MotionEvent.ACTION_MOVE:
			// Mouse events for the core
			LibVLC.sendMouseEvent(MotionEvent.ACTION_MOVE, 0, xTouch, yTouch);

			// No volume/brightness action if coef < 2 or a secondary display is
			// connected
			if (coef > 2/* && mPresentation == null */) {
				// Volume (Up or Down - Right side)
				if (!mEnableBrightnessGesture || (int) mTouchX > (screen.widthPixels / 2)) {
					doVolumeTouch(y_changed);
				}
				// Brightness (Up or Down - Left side)
				if (mEnableBrightnessGesture && (int) mTouchX < (screen.widthPixels / 2)) {
					doBrightnessTouch(y_changed);
				}
				// Extend the overlay for a little while, so that it doesn't
				// disappear on the user if more adjustment is needed. This
				// is because on devices with soft navigation (e.g. Galaxy
				// Nexus), gestures can't be made without activating the UI.
				if (AndroidDevices.hasNavBar())
					showOverlay();
			}
			// Seek (Right or Left move)
			doSeekTouch(coef, xgesturesize, false);
			break;

		case MotionEvent.ACTION_UP:
			// Mouse events for the core
			LibVLC.sendMouseEvent(MotionEvent.ACTION_UP, 0, xTouch, yTouch);

			// Audio or Brightness
			if (mTouchAction == TOUCH_NONE) {
				if (!mShowing) {
					showOverlay();
				} else {
					hideOverlay(true);
				}
			}
			// Seek
			doSeekTouch(coef, xgesturesize, true);
			break;
		}
		return mTouchAction != TOUCH_NONE;
	}

	private void doSeekTouch(float coef, float gesturesize, boolean seek) {
		// No seek action if coef > 0.5 and gesturesize < 1cm
		if (coef > 0.5 || Math.abs(gesturesize) < 1 || !mCanSeek)
			return;

		if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK)
			return;
		mTouchAction = TOUCH_SEEK;

		// Always show seekbar when searching
		if (!mShowing)
			showOverlay();

		long length = mLibVLC.getLength();
		long time = mLibVLC.getTime();

		// Size of the jump, 10 minutes max (600000), with a bi-cubic
		// progression, for a 8cm gesture
		int jump = (int) (Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000));

		// Adjust the jump
		if ((jump > 0) && ((time + jump) > length))
			jump = (int) (length - time);
		if ((jump < 0) && ((time + jump) < 0))
			jump = (int) -time;

		// Jump !
		if (seek && length > 0) {
			popcornSetTime(time + jump);
		}

		if (length > 0)
			// Show the jump's size
			showInfo(String.format("%s%s (%s)", jump >= 0 ? "+" : "", Strings.millisToString(jump), Strings.millisToString(time + jump)), 1000);
		else
			showInfo(R.string.unseekable_stream, 1000);
	}

	private void doVolumeTouch(float y_changed) {
		if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
			return;
		int delta = -(int) ((y_changed / mSurfaceYDisplayRange) * mAudioMax);
		int vol = (int) Math.min(Math.max(mVol + delta, 0), mAudioMax);
		if (delta != 0) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
			mTouchAction = TOUCH_VOLUME;
			showInfo(getString(R.string.volume) + '\u00A0' + Integer.toString(vol), 1000);
		}
	}

	private void initBrightnessTouch() {
		float brightnesstemp = 0.01f;
		// Initialize the layoutParams screen brightness
		try {
			brightnesstemp = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = brightnesstemp;
		getWindow().setAttributes(lp);
		mIsFirstBrightnessGesture = false;
	}

	private void doBrightnessTouch(float y_changed) {
		if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
			return;
		if (mIsFirstBrightnessGesture)
			initBrightnessTouch();
		mTouchAction = TOUCH_BRIGHTNESS;

		// Set delta : 0.07f is arbitrary for now, it possibly will change in
		// the future
		float delta = -y_changed / mSurfaceYDisplayRange * 0.07f;

		// Estimate and adjust Brightness
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1);

		// Set Brightness
		getWindow().setAttributes(lp);
		showInfo(getString(R.string.brightness) + '\u00A0' + Math.round(lp.screenBrightness * 15), 1000);
	}

	/**
	 * handle changes of the seekbar (slicer)
	 */
	private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mDragging = true;
			showOverlay(OVERLAY_INFINITE);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mDragging = false;
			showOverlay();
			hideInfo();
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser && mCanSeek) {
				popcornSetTime(progress);
				setOverlayProgress();
				mTime.setText(Strings.millisToString(progress));
				showInfo(Strings.millisToString(progress));
			}

		}
	};

	/**
    *
    */
	private final OnClickListener mAudioTrackListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final String[] arrList = new String[mAudioTracksList.size()];
			int i = 0;
			int listPosition = 0;
			for (Map.Entry<Integer, String> entry : mAudioTracksList.entrySet()) {
				arrList[i] = entry.getValue();
				// map the track position to the list position
				if (entry.getKey() == mLibVLC.getAudioTrack())
					listPosition = i;
				i++;
			}
			AlertDialog dialog = new AlertDialog.Builder(VLCPlayerActivity.this).setTitle(R.string.track_audio)
					.setSingleChoiceItems(arrList, listPosition, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int listPosition) {
							int trackID = -1;
							// Reverse map search...
							for (Map.Entry<Integer, String> entry : mAudioTracksList.entrySet()) {
								if (arrList[listPosition].equals(entry.getValue())) {
									trackID = entry.getKey();
									break;
								}
							}
							if (trackID < 0)
								return;

							MediaDatabase.getInstance().updateMedia("file://" + mLocation, MediaDatabase.mediaColumn.MEDIA_AUDIOTRACK, trackID);
							mLibVLC.setAudioTrack(trackID);
							dialog.dismiss();
						}
					}).create();
			dialog.setCanceledOnTouchOutside(true);
			dialog.setOwnerActivity(VLCPlayerActivity.this);
			dialog.show();
		}
	};

	/**
    *
    */
	private final OnClickListener mPlayPauseListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (popcornIsPlaying()) {
				pause();
			} else {
				play();
			}
			showOverlay();
		}
	};

	/**
    *
    */
	private final OnClickListener mBackwardListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			seek(-10000);
		}
	};

	/**
    *
    */
	private final OnClickListener mForwardListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			seek(10000);
		}
	};

	public void seek(int delta) {
		// unseekable stream
		if (mLibVLC.getLength() <= 0 || !mCanSeek)
			return;

		long position = mLibVLC.getTime() + delta;
		if (position < 0)
			position = 0;
		popcornSetTime(position);
		showOverlay();
	}

	// /**
	// *
	// */
	// private final OnClickListener mLockListener = new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// if (mIsLocked) {
	// mIsLocked = false;
	// unlockScreen();
	// } else {
	// mIsLocked = true;
	// lockScreen();
	// }
	// }
	// };

	/**
     *
     */
	private final OnClickListener mSizeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (mCurrentSize < SURFACE_ORIGINAL) {
				mCurrentSize++;
			} else {
				mCurrentSize = 0;
			}
			changeSurfaceSize();
			switch (mCurrentSize) {
			case SURFACE_BEST_FIT:
				showInfo(R.string.surface_best_fit, 1000);
				break;
			case SURFACE_FIT_HORIZONTAL:
				showInfo(R.string.surface_fit_horizontal, 1000);
				break;
			case SURFACE_FIT_VERTICAL:
				showInfo(R.string.surface_fit_vertical, 1000);
				break;
			case SURFACE_FILL:
				showInfo(R.string.surface_fill, 1000);
				break;
			case SURFACE_16_9:
				showInfo("16:9", 1000);
				break;
			case SURFACE_4_3:
				showInfo("4:3", 1000);
				break;
			case SURFACE_ORIGINAL:
				showInfo(R.string.surface_original, 1000);
				break;
			}
			showOverlay();
		}
	};

	private final OnClickListener mRemainingTimeListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mDisplayRemainingTime = !mDisplayRemainingTime;
			showOverlay();
		}
	};

	/**
	 * attach and disattach surface to the lib
	 */
	private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (format == PixelFormat.RGBX_8888)
				Log.d(TAG, "Pixel format is RGBX_8888");
			else if (format == PixelFormat.RGB_565)
				Log.d(TAG, "Pixel format is RGB_565");
			else if (format == ImageFormat.YV12)
				Log.d(TAG, "Pixel format is YV12");
			else
				Log.d(TAG, "Pixel format is other/unknown");
			if (mLibVLC != null)
				mLibVLC.attachSurface(holder.getSurface(), VLCPlayerActivity.this);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mLibVLC != null)
				mLibVLC.detachSurface();
		}
	};

	private final SurfaceHolder.Callback mSubtitlesSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (mLibVLC != null)
				mLibVLC.attachSubtitlesSurface(holder.getSurface());
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mLibVLC != null)
				mLibVLC.detachSubtitlesSurface();
		}
	};

	/**
	 * show overlay the the default timeout
	 */
	private void showOverlay() {
		showOverlay(OVERLAY_TIMEOUT);
	}

	/**
	 * show overlay
	 */
	private void showOverlay(int timeout) {
		// if (mIsNavMenu)
		// return;
		mHandler.sendEmptyMessage(SHOW_PROGRESS);
		if (!mShowing) {
			mShowing = true;
			if (!mIsLocked) {
				mCloseButton.setVisibility(View.VISIBLE);
				mOverlayHeader.setVisibility(View.VISIBLE);
				mOverlayOption.setVisibility(View.VISIBLE);
				mPlayPause.setVisibility(View.VISIBLE);
				// mMenu.setVisibility(View.VISIBLE);
				dimStatusBar(false);
			}
			mOverlayProgress.setVisibility(View.VISIBLE);
			// if (mPresentation != null)
			// mOverlayBackground.setVisibility(View.VISIBLE);
		}
		Message msg = mHandler.obtainMessage(FADE_OUT);
		if (timeout != 0) {
			mHandler.removeMessages(FADE_OUT);
			mHandler.sendMessageDelayed(msg, timeout);
		}
		updateOverlayPausePlay();
	}

	/**
	 * hider overlay
	 */
	private void hideOverlay(boolean fromUser) {
		if (mShowing) {
			mHandler.removeMessages(SHOW_PROGRESS);
			Log.i(TAG, "remove View!");
			// if (mOverlayTips != null)
			// mOverlayTips.setVisibility(View.INVISIBLE);
			if (!fromUser && !mIsLocked) {
				mCloseButton.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
				mOverlayHeader.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
				mOverlayOption.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
				mOverlayProgress.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
				mPlayPause.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
				// mMenu.startAnimation(AnimationUtils.loadAnimation(this,
				// android.R.anim.fade_out));
			}
			// if (mPresentation != null) {
			// mOverlayBackground.startAnimation(AnimationUtils.loadAnimation(this,
			// android.R.anim.fade_out));
			// mOverlayBackground.setVisibility(View.INVISIBLE);
			// }
			mCloseButton.setVisibility(View.INVISIBLE);
			mOverlayHeader.setVisibility(View.INVISIBLE);
			mOverlayOption.setVisibility(View.INVISIBLE);
			mOverlayProgress.setVisibility(View.INVISIBLE);
			mPlayPause.setVisibility(View.INVISIBLE);
			// mMenu.setVisibility(View.INVISIBLE);
			mShowing = false;
			dimStatusBar(true);
		}
	}

	/**
	 * Dim the status bar and/or navigation icons when needed on Android 3.x.
	 * Hide it on Android 4.0 and later
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void dimStatusBar(boolean dim) {
		if (!LibVlcUtil.isHoneycombOrLater() || !AndroidDevices.hasNavBar())
			return;
		int layout = 0;
		if (!AndroidDevices.hasCombBar() && LibVlcUtil.isJellyBeanOrLater())
			layout = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		int visibility = (dim ? (AndroidDevices.hasCombBar() ? View.SYSTEM_UI_FLAG_LOW_PROFILE : View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
				: View.SYSTEM_UI_FLAG_VISIBLE) | layout;
		mSurface.setSystemUiVisibility(visibility);
		mSubtitlesSurface.setSystemUiVisibility(visibility);
	}

	private void updateOverlayPausePlay() {
		if (mLibVLC == null)
			return;

		mPlayPause.setBackgroundResource(popcornIsPlaying() ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
	}

	/**
	 * update the overlay
	 */
	private int setOverlayProgress() {
		if (mLibVLC == null) {
			return 0;
		}
		int time = (int) mLibVLC.getTime();
		int length = (int) mLibVLC.getLength();
		if (length == 0) {
			Media media = MediaDatabase.getInstance().getMedia("file://" + mLocation);
			if (media != null)
				length = (int) media.getLength();
		}

		mSeekbar.setMax(length);
		mSeekbar.setProgress(time);
		mSysTime.setText(DateFormat.getTimeFormat(this).format(new Date(System.currentTimeMillis())));
		if (time >= 0)
			mTime.setText(Strings.millisToString(time));
		if (length >= 0)
			mLength.setText(mDisplayRemainingTime && length > 0 ? "- " + Strings.millisToString(length - time) : Strings.millisToString(length));

		// torrent
		mSeekbar.setSecondaryProgress(prioritizer.getSeekProgress(length));

		return time;
	}

	private void setESTracks() {
		if (mLastAudioTrack >= 0) {
			mLibVLC.setAudioTrack(mLastAudioTrack);
			mLastAudioTrack = -1;
		}
		if (mLastSpuTrack >= -1) {
			mLibVLC.setSpuTrack(mLastSpuTrack);
			mLastSpuTrack = -2;
		}
	}

	private void setESTrackLists(boolean force) {
		if (mAudioTracksList == null || force) {
			if (mLibVLC.getAudioTracksCount() > 2) {
				mAudioTracksList = mLibVLC.getAudioTrackDescription();
				mAudioTrack.setOnClickListener(mAudioTrackListener);
				mAudioTrack.setVisibility(View.VISIBLE);
			} else {
				mAudioTrack.setVisibility(View.GONE);
				mAudioTrack.setOnClickListener(null);
			}
		}
		// if (mSubtitleTracksList == null || force) {
		// if (mLibVLC.getSpuTracksCount() > 0) {
		// mSubtitleTracksList = mLibVLC.getSpuTrackDescription();
		// mSubtitle.setOnClickListener(mSubtitlesListener);
		// mSubtitle.setVisibility(View.VISIBLE);
		// } else {
		// mSubtitle.setVisibility(View.GONE);
		// mSubtitle.setOnClickListener(null);
		// }
		// }
	}

	/**
     *
     */
	private void play() {
		popcornPlay();
		mSurface.setKeepScreenOn(true);
	}

	/**
     *
     */
	private void pause() {
		popcornPause();
		mSurface.setKeepScreenOn(false);
	}

	@SuppressWarnings("deprecation")
	private int getScreenRotation() {
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			try {
				Method m = display.getClass().getDeclaredMethod("getRotation");
				return (Integer) m.invoke(display);
			} catch (Exception e) {
				return Surface.ROTATION_0;
			}
		} else {
			return display.getOrientation();
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private int getScreenOrientation() {
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int rot = getScreenRotation();
		/*
		 * Since getRotation() returns the screen's "natural" orientation, which
		 * is not guaranteed to be SCREEN_ORIENTATION_PORTRAIT, we have to
		 * invert the SCREEN_ORIENTATION value if it is "naturally" landscape.
		 */
		@SuppressWarnings("deprecation")
		boolean defaultWide = display.getWidth() > display.getHeight();
		if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270)
			defaultWide = !defaultWide;
		if (defaultWide) {
			switch (rot) {
			case Surface.ROTATION_0:
				return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			case Surface.ROTATION_90:
				return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			case Surface.ROTATION_180:
				// SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
				// Level 9+
				return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
						: ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			case Surface.ROTATION_270:
				// SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
				// Level 9+
				return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
						: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			default:
				return 0;
			}
		} else {
			switch (rot) {
			case Surface.ROTATION_0:
				return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			case Surface.ROTATION_90:
				return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			case Surface.ROTATION_180:
				// SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
				// Level 9+
				return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
						: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			case Surface.ROTATION_270:
				// SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
				// Level 9+
				return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
						: ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			default:
				return 0;
			}
		}
	}

	public void showAdvancedOptions(View v) {
		CommonDialogs.advancedOptions(this, v, MenuType.Video);
	}
}
