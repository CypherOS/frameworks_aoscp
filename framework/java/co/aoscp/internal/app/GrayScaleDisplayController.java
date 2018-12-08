/*
 * Copyright (C) 2018 CypherOS
 * Copyright (C) 2018 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
 
package co.aoscp.internal.app;

import android.content.Context;
import android.opengl.Matrix;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import android.util.SparseArray;

import aoscp.hardware.DeviceHardwareManager;

import com.android.internal.app.ColorDisplayController;

import java.util.List;
import java.util.Arrays;

public class GrayScaleDisplayController {

	private static final String TAG = "GrayScaleDisplayController";

	private static final int LEVEL_COLOR_MATRIX_READING = 201;
	private static final SparseArray<float[]> mColorMatrix = new SparseArray<>(3);
    private static final int SURFACE_FLINGER_TRANSACTION_COLOR_MATRIX = 1015;
    private static final float[][] mTempColorMatrix = new float[2][16];

	/**
     * Matrix and offset used for converting color to grayscale.
     * Copied from com.android.server.accessibility.DisplayAdjustmentUtils.MATRIX_GRAYSCALE
     */
    private static final float[] MATRIX_GRAYSCALE = new float[] {
            .2126f, .2126f, .2126f, 0,
            .7152f, .7152f, .7152f, 0,
            .0722f, .0722f, .0722f, 0,
            0,      0,      0, 1
    };

    /** Full color matrix and offset */
    private static final float[] MATRIX_NORMAL = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

	private Context mContext;
	private ColorDisplayController mColorDisplayController;
	private DeviceHardwareManager mHwManager;
	private boolean mHasNative;

	public GrayScaleDisplayController(Context context) {
		mContext = context;
		mColorDisplayController = new ColorDisplayController(context);
		mHwManager = DeviceHardwareManager.getInstance(context);
		mHasNative = mHwManager.isSupported(DeviceHardwareManager.FEATURE_READING_ENHANCEMENT);
	}

	public void setGrayScale(boolean state) {
		if (mHasNative) {
			mHwManager.setGrayScale(state);
			return;
		}
        if (state) {
            setColorMatrix(LEVEL_COLOR_MATRIX_READING, MATRIX_GRAYSCALE);
        } else {
            setColorMatrix(LEVEL_COLOR_MATRIX_READING, MATRIX_NORMAL);
            if (mColorDisplayController.isActivated()) {
                // hack a shak : restore night mode if it was on
                final int origValue = mColorDisplayController.getColorTemperature();
                mColorDisplayController.setColorTemperature(origValue + 1);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mColorDisplayController.setColorTemperature(origValue);
                    }
                }, 1500);
            }
        }
    }

	public void setColorMatrix(int level, float[] value) {
        if (value != null && value.length != 16) {
            throw new IllegalArgumentException("Expected length: 16 (4x4 matrix)"
                    + ", actual length: " + value.length);
        }

        synchronized (mColorMatrix) {
            final float[] oldValue = mColorMatrix.get(level);
            if (!Arrays.equals(oldValue, value)) {
                if (value == null) {
                    mColorMatrix.remove(level);
                } else if (oldValue == null) {
                    mColorMatrix.put(level, Arrays.copyOf(value, value.length));
                } else {
                    System.arraycopy(value, 0, oldValue, 0, value.length);
                }

                // Update the current color transform.
                applyColorMatrix(computeColorMatrixLocked());
            }
        }
    }

	private void applyColorMatrix(float[] matrix) {
        final IBinder flinger = ServiceManager.getService("SurfaceFlinger");
        if (flinger != null) {
            final Parcel data = Parcel.obtain();
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            if (matrix != null) {
                data.writeInt(1);
                for (int i = 0; i < 16; i++) {
                    data.writeFloat(matrix[i]);
                }
            } else {
                data.writeInt(0);
            }
            try {
                flinger.transact(SURFACE_FLINGER_TRANSACTION_COLOR_MATRIX, data, null, 0);
            } catch (RemoteException ex) {
                Slog.e(TAG, "Failed to set color transform", ex);
            } finally {
                data.recycle();
            }
        }
    }

    private float[] computeColorMatrixLocked() {
        final int count = mColorMatrix.size();
        if (count == 0) {
            return null;
        }

        final float[][] result = mTempColorMatrix;
        Matrix.setIdentityM(result[0], 0);
        for (int i = 0; i < count; i++) {
            float[] rhs = mColorMatrix.valueAt(i);
            Matrix.multiplyMM(result[(i + 1) % 2], 0, result[i % 2], 0, rhs, 0);
        }
        return result[count % 2];
    }
}