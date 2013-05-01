/*
 * Copyright (c) 2008-2012 Vrije Universiteit, The Netherlands All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Vrije Universiteit nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package interdroid.util.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;

/**
 * Defines an asynchronous task which runs with a dialog showing.
 * Handles dialog management while the task is running.
 * @author nick <palmer@cs.vu.nl>
 *
 * @param <A>
 * @param <B>
 * @param <C>
 */
public abstract class AsyncTaskWithProgressDialog<A, B, C> extends AsyncTask<A, B, C> {
	/** Logger Interface */
	private static final Logger logger = LoggerFactory
			.getLogger(AsyncTaskWithProgressDialog.class);

	/** The contet we run in */
	protected Activity mContext;

	/** The dialog we are showing */
	private ProgressDialog mDialog;
	/** The title for the dialog */
	private String mTitle;
	/** The message for the dialog */
	private String mMessage;

	/**
	 * Constructs a dialog
	 * @param context The context to run in
	 * @param title The title for a dialog
	 * @param message The message for the dialog
	 */
	public AsyncTaskWithProgressDialog(Activity context, String title, String message) {
		mContext = context;
		mTitle = title;
		mMessage = message;
	}

	/**
	 * Shows the dialog
	 */
	@Override
	protected void onPreExecute() {
		mDialog = ProgressDialog.show(mContext, mTitle, mMessage, true, false);
		mDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				logger.debug("Dialog dismissed.");
				mDialog = null;
			}

		});
	}

	/**
	 * Hides the dialog.
	 */
	@Override
	protected void onPostExecute(C v) {
		try {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
				mDialog = null;
			}
		} catch (Throwable e) {
			logger.error("Exception while closing dialog.", e);
		}
	}
}
