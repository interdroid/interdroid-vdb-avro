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
package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.R;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.view.factory.AvroViewFactory;

import java.util.List;

import org.apache.avro.Schema;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Handler for enumerations.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class EnumHandler
implements DialogInterface.OnClickListener, OnClickListener {
	/**
	 * The activity to work in.
	 */
	private final Activity mActivity;
	/**
	 * The data model we work for.
	 */
	private final AvroRecordModel mDataModel;
	/**
	 * The schema for the data.
	 */
	private final Schema mSchema;
	/**
	 * The text view to display the selection in.
	 */
	private final TextView mTextView;
	/**
	 * The value handler for the data.
	 */
	private final ValueHandler mValueHandler;

	/**
	 * Construct a new enumeration handler.
	 * @param activity the activity to work in
	 * @param dataModel the model for the data.
	 * @param schema the schema for the data
	 * @param view the view to display with
	 * @param valueHandler the value handler to get data from
	 */
	public EnumHandler(final Activity activity, final AvroRecordModel dataModel,
			final Schema schema, final TextView view,
			final ValueHandler valueHandler) {
		mActivity = activity;
		mDataModel = dataModel;
		mSchema = schema;
		mTextView = view;
		mValueHandler = valueHandler;

		mTextView.setClickable(true);
		mTextView.setFocusable(true);
		mTextView.setOnClickListener(this);
		setText();
	}

	/**
	 * Sets the text using the value in the handler.
	 */
	protected final void setText() {
		final Integer ordinal = (Integer) mValueHandler.getValue();
		mDataModel.runOnUI(new Runnable() {
			@Override
			public void run() {
				if (ordinal == null) {
					mTextView.setText(R.string.none);
				} else {
					mTextView.setText(mSchema.getEnumSymbols().get(ordinal));
				}
			}
		});
	}

	@Override
	public final void onClick(final View view) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(AvroViewFactory.toTitle(mActivity,
				R.string.label_pick, mSchema));
		final List<String> items = mSchema.getEnumSymbols();
		final CharSequence[] itemArray =
				items.toArray(new CharSequence[items.size()]);
		Integer selection = (Integer) mValueHandler.getValue();
		if (selection == null) {
			selection = -1;
		}
		builder.setSingleChoiceItems(itemArray, selection, this);
		final AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	public final void onClick(final DialogInterface dialog, final int which) {
		mValueHandler.setValue(which);
		mTextView.setText(mSchema.getEnumSymbols().get(which));
		mTextView.postInvalidate();
		mDataModel.onChanged();
		dialog.dismiss();
	}

}
