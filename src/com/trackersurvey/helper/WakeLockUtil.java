/**
 * @description: 
 * @author chenshiqiang E-mail:csqwyyx@163.com
 * @date 2014�?4�?27�? 下午5:47:20   
 * @version 1.0   
 */
package com.trackersurvey.helper;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class WakeLockUtil {

	// ------------------------ Constants ------------------------
	private static final String TAG = WakeLockUtil.class.getSimpleName();

	// ------------------------- Fields --------------------------
	
	private Context context;
	private WakeLock wakeLock;

	// ----------------------- Constructors ----------------------
	
	public WakeLockUtil(Context context) {
		super();
		this.context = context;
	}
	

	// -------- Methods for/from SuperClass/Interfaces -----------

	// --------------------- Methods public ----------------------

	/**
	 * Acquires the wake lock.
	 */
	public void acquireWakeLock() {
		try {
			PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			if (powerManager == null) {
				return;
			}
			if (wakeLock == null) {
				wakeLock = powerManager.newWakeLock(
						PowerManager.PARTIAL_WAKE_LOCK, TAG);
				if (wakeLock == null) {
					Log.e("AmapErr", "wakeLock is null.");
					return;
				}
			}
			if (!wakeLock.isHeld()) {
				wakeLock.acquire();
				Log.i("LogDemo", "wakelock");
				if (!wakeLock.isHeld()) {
					Log.e("AmapErr", "Unable to hold wakeLock.");
				}
			}
		} catch (RuntimeException e) {
			Log.e("AmapErr", "Caught unexpected exception", e);
		}
	}

	

	/**
	 * Releases the wake lock.
	 */
	public void releaseWakeLock() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	// --------------------- Methods private ---------------------

	// --------------------- Getter & Setter ---------------------

	// --------------- Inner and Anonymous Classes ---------------
}
