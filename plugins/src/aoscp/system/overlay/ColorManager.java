/*
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package aoscp.system.overlay;

import android.app.ActivityManager;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import aoscp.system.UiOffloadThread;

import com.android.internal.colorextraction.ColorExtractor;

/**
 * Helper class for Color Manager that works as a bridge
 * to get/set aoscp overlays.
 */
public class ColorManager implements ColorExtractor.OnColorsChangedListener {

    public static final String TAG = "ColorManager";

    // Theme Packages
    private static final String THEME_BLACK = "co.aoscp.theme.black";
    private static final String THEME_DARK = "co.aoscp.theme.dark";

    public static final String[] BLACK_THEME = {
            "co.aoscp.theme.black",
            "co.aoscp.theme.settings.black",
    };
    public static final String[] DARK_THEME = {
            "co.aoscp.theme.dark",
            "co.aoscp.theme.settings.dark",
    };

    // Accent Packages
    private static final String ACCENT_DEFAULT = "default";
    // Accent Packages: Blue
    private static final String ACCENT_BLUE = "co.aoscp.accent.blue";
    private static final String ACCENT_INDIGO = "co.aoscp.accent.indigo";
    private static final String ACCENT_OCEANIC = "co.aoscp.accent.oceanic";
    private static final String ACCENT_BRIGHTSKY = "co.aoscp.accent.brightsky";
    // Accent Packages: Green
    private static final String ACCENT_GREEN = "co.aoscp.accent.green";
    private static final String ACCENT_LIMA_BEAN = "co.aoscp.accent.limabean";
    private static final String ACCENT_LIME = "co.aoscp.accent.lime";
    private static final String ACCENT_TEAL = "co.aoscp.accent.teal";
    // Accent Packages: Pink
    private static final String ACCENT_PINK = "co.aoscp.accent.pink";
    private static final String ACCENT_PLAY_BOY = "co.aoscp.accent.playboy";
    // Accent Packages: Purple
    private static final String ACCENT_PURPLE = "co.aoscp.accent.purple";
    private static final String ACCENT_DEEP_VALLEY = "co.aoscp.accent.deepvalley";
    // Accent Packages: Red
    private static final String ACCENT_RED = "co.aoscp.accent.red";
    private static final String ACCENT_BLOODY_MARY = "co.aoscp.accent.bloodymary";
    // Accent Packages: Yellow
    private static final String ACCENT_YELLOW = "co.aoscp.accent.yellow";
    private static final String ACCENT_SUN_FLOWER = "co.aoscp.accent.sunflower";
    // Accent Packages: Other
    private static final String ACCENT_BLACK = "co.aoscp.accent.black";
    private static final String ACCENT_GREY = "co.aoscp.accent.grey";
    private static final String ACCENT_WHITE = "co.aoscp.accent.white";

    private static final String[] ACCENTS = {
            ACCENT_DEFAULT,
            ACCENT_BLUE,
            ACCENT_INDIGO,
            ACCENT_OCEANIC,
            ACCENT_BRIGHTSKY,
            ACCENT_GREEN,
            ACCENT_LIMA_BEAN,
            ACCENT_LIME,
            ACCENT_TEAL,
            ACCENT_PINK,
            ACCENT_PLAY_BOY,
            ACCENT_PURPLE,
            ACCENT_DEEP_VALLEY,
            ACCENT_RED,
            ACCENT_BLOODY_MARY,
            ACCENT_YELLOW,
            ACCENT_SUN_FLOWER,
            ACCENT_BLACK,
            ACCENT_GREY,
            ACCENT_WHITE,
    };
	
	/** Whether to force dark theme if Configuration.UI_MODE_NIGHT_YES. */
    private static final boolean DARK_THEME_IN_NIGHT_MODE = true;
	
	protected int mCurrentUserId = 0;
	private ColorExtractor mColorExtractor;
	private Context mContext;
	protected final Handler mHandler = Handler.getMain();
	private IOverlayManager mOverlayManager;
	private UiOffloadThread mUiOffloadThread;

	protected final ContentObserver mColorManagerObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            updateTheme();
            updateAccent();
        }
    };

	public ColorManager(Context context) {
		mColorExtractor = new ColorExtractor(context);
		mColorExtractor.addOnColorsChangedListener(this);
		mContext = context;
		mCurrentUserId = ActivityManager.getCurrentUser();
		mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
		mUiOffloadThread = new UiOffloadThread(context);
		
		mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.SYSTEM_THEME),
                true, mColorManagerObserver, UserHandle.USER_ALL);
        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.SYSTEM_ACCENT),
                true, mColorManagerObserver, UserHandle.USER_ALL);
	}

    public boolean isUsingDarkTheme() {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = mOverlayManager.getOverlayInfo(THEME_DARK, mCurrentUserId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }

    public boolean isUsingBlackTheme() {
        OverlayInfo themeInfo = null;
        try {
            themeInfo = mOverlayManager.getOverlayInfo(THEME_BLACK, mCurrentUserId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return themeInfo != null && themeInfo.isEnabled();
    }

    public void restoreDefaultTheme() {
        for (int i = 1; i < BLACK_THEME.length; i++) {
            String blackTheme = BLACK_THEME[i];
            try {
                mOverlayManager.setEnabled(blackTheme, false, mCurrentUserId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        for (int i = 1; i < DARK_THEME.length; i++) {
            String darkTheme = DARK_THEME[i];
            try {
                mOverlayManager.setEnabled(darkTheme, false, mCurrentUserId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void restoreDefaultAccent() {
        for (int i = 1; i < ACCENTS.length; i++) {
            String accents = ACCENTS[i];
            try {
                mOverlayManager.setEnabled(accents, false, mCurrentUserId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected void updateTheme() {
        // 0 = Auto, 1 = Light, 2 = Dark, 3 = Black
        final int systemTheme = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.SYSTEM_THEME, 0);

        final Configuration config = mContext.getResources().getConfiguration();
        // The system wallpaper defines if QS should be light or dark.
        WallpaperColors systemColors = mColorExtractor
                .getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
        final boolean useDarkTheme;
        final boolean useBlackTheme;
        final boolean wallpaperWantsDarkTheme = systemColors != null && (
                systemColors.getColorHints() & WallpaperColors.HINT_SUPPORTS_DARK_THEME) != 0;
        final boolean nightModeWantsDarkTheme = DARK_THEME_IN_NIGHT_MODE
                && (config.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        switch (systemTheme) {
            case 1:
                useDarkTheme = false;
                useBlackTheme = false;
                break;
            case 2:
                useDarkTheme = true;
                useBlackTheme = false;
                break;
            case 3:
                useDarkTheme = false;
                useBlackTheme = true;
                break;
            default:
                useDarkTheme = wallpaperWantsDarkTheme || nightModeWantsDarkTheme;
                useBlackTheme = false;
                break;
        }
		
		if (isUsingDarkTheme() != useDarkTheme) {
			for (String darkTheme: DARK_THEME) {
				mUiOffloadThread.submit(() -> {
					try {
						mOverlayManager.setEnabled(darkTheme, useDarkTheme, mCurrentUserId);
					} catch (RemoteException e) {
                        Log.d(TAG, "Can't change theme");
                    }
				});
			}
		}
		
		if (isUsingBlackTheme() != useBlackTheme) {
            for (String blackTheme: BLACK_THEME) {
                mUiOffloadThread.submit(() -> {
                    try {
                        mOverlayManager.setEnabled(blackTheme, useBlackTheme, mCurrentUserId);
                    } catch (RemoteException e) {
                        Log.d(TAG, "Can't change theme");
                    }
                });
            }
        }
	}

    public void updateAccent() throws RemoteException {
		final int systemAccent = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.SYSTEM_ACCENT, 0);
        switch (systemAccent) {
            case 0:
                restoreDefaultAccent();
                break;
            case 1:
                mOverlayManager.setEnabledExclusive(ACCENT_BLUE, true, mCurrentUserId);
                break;
            case 2:
                mOverlayManager.setEnabledExclusive(ACCENT_INDIGO, true, mCurrentUserId);
                break;
            case 3:
                mOverlayManager.setEnabledExclusive(ACCENT_OCEANIC, true, mCurrentUserId);
                break;
            case 4:
                mOverlayManager.setEnabledExclusive(ACCENT_BRIGHTSKY, true, mCurrentUserId);
                break;
            case 5:
                mOverlayManager.setEnabledExclusive(ACCENT_GREEN, true, mCurrentUserId);
                break;
            case 6:
                mOverlayManager.setEnabledExclusive(ACCENT_LIMA_BEAN, true, mCurrentUserId);
                break;
            case 7:
                mOverlayManager.setEnabledExclusive(ACCENT_LIME, true, mCurrentUserId);
                break;
            case 8:
                mOverlayManager.setEnabledExclusive(ACCENT_TEAL, true, mCurrentUserId);
                break;
            case 9:
                mOverlayManager.setEnabledExclusive(ACCENT_PINK, true, mCurrentUserId);
                break;
            case 10:
                mOverlayManager.setEnabledExclusive(ACCENT_PLAY_BOY, true, mCurrentUserId);
                break;
            case 11:
                mOverlayManager.setEnabledExclusive(ACCENT_PURPLE, true, mCurrentUserId);
                break;
            case 12:
                mOverlayManager.setEnabledExclusive(ACCENT_DEEP_VALLEY, true, mCurrentUserId);
                break;
            case 13:
                mOverlayManager.setEnabledExclusive(ACCENT_RED, true, mCurrentUserId);
                break;
            case 14:
                mOverlayManager.setEnabledExclusive(ACCENT_BLOODY_MARY, true, mCurrentUserId);
                break;
            case 15:
                mOverlayManager.setEnabledExclusive(ACCENT_YELLOW, true, mCurrentUserId);
                break;
            case 16:
                mOverlayManager.setEnabledExclusive(ACCENT_SUN_FLOWER, true, mCurrentUserId);
                break;
            case 17:
                mOverlayManager.setEnabledExclusive(ACCENT_BLACK, true, mCurrentUserId);
                break;
            case 18:
                mOverlayManager.setEnabledExclusive(ACCENT_GREY, true, mCurrentUserId);
                break;
            case 19:
                mOverlayManager.setEnabledExclusive(ACCENT_WHITE, true, mCurrentUserId);
                break;
        }
    }
}