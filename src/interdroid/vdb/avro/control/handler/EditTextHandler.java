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

import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Handler for an edit text.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class EditTextHandler implements TextWatcher {
	/** Access to logger. */
	private static final Logger LOG =
			LoggerFactory.getLogger(EditTextHandler.class);

	/** The model we work in. */
	private final AvroRecordModel mDataModel;
	/** The type we are showing. */
	private final Type mType;
	/** The value handler we use to get and set data. */
	private final ValueHandler mValueHandler;

	/**
	 * Construct this type of handler.
	 * @param dataModel the data model we work in.
	 * @param type the type we are handling
	 * @param valueHandler the value handler to set and get from
	 * @param text the edit text to handle
	 */
	public EditTextHandler(final AvroRecordModel dataModel,
			final Type type, final ValueHandler valueHandler,
			final EditText text) {
		mDataModel = dataModel;
		mType = type;
		mValueHandler = valueHandler;
		setWatched(text);
	}

	@Override
	public final void afterTextChanged(final Editable text) {
		if (text.length() == 0) {
			mValueHandler.setValue(null);
		} else {
			switch (mType) {
			case FLOAT:
				mValueHandler.setValue(Float.valueOf(text.toString()));
				break;
			case INT:
				mValueHandler.setValue(Integer.valueOf(text.toString()));
				break;
			case LONG:
				mValueHandler.setValue(Long.valueOf(text.toString()));
				break;
			case NULL:
				mValueHandler.setValue(null);
				break;
			case STRING:
				mValueHandler.setValue(text.toString());
				break;
			default:
				throw new IllegalArgumentException(
						"Unsupported type: " + mType);
			}
		}
		mDataModel.onChanged();
	}

	@Override
	public void beforeTextChanged(final CharSequence text,
			final int start, final int count, final int after) {
		// Do Nothing
	}

	@Override
	public void onTextChanged(final CharSequence text, final int start,
			final int before, final int count) {
		// Do Nothing
	}

	@Override
	public final String toString() {
		return "EditTextHandler: " + mType + " : " + mValueHandler;
	}

	/**
	 * Sets the edit text we watch.
	 * @param text the edit text to watch.
	 */
	private void setWatched(final EditText text) {

		if (mValueHandler.getValue() == null) {
			LOG.debug("Text watcher has null value: {}", this);
			mDataModel.runOnUI(new Runnable()
			{
				@Override
				public void run() {
					text.setText("");
				}
			});
		} else {
			LOG.debug("Setting value: {} for: {}",
					mValueHandler.getValue(), this);
			mDataModel.runOnUI(new Runnable()
			{
				@Override
				public void run() {
					text.setText(String.valueOf(mValueHandler.getValue()));
				}
			});
		}

		// Must come after the setText.
		text.addTextChangedListener(this);

	}
}
