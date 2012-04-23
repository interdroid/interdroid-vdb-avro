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

import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * A handler for a boolean represented by a checkbox.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class CheckboxHandler implements OnCheckedChangeListener {

	/** The model we work with. */
	private final AvroRecordModel mDataModel;
	/** The value handler for the data. */
	private final ValueHandler mValueHandler;

	/**
	 * Construct a checkbox handler.
	 * @param dataModel the model to work in
	 * @param valueHandler the value handler with the data
	 * @param text the checkbox
	 */
	public CheckboxHandler(final AvroRecordModel dataModel,
			final ValueHandler valueHandler, final CheckBox text) {
		mDataModel = dataModel;
		mValueHandler = valueHandler;
		setWatched(text);
	}

	@Override
	public final void onCheckedChanged(final CompoundButton buttonView,
			final boolean isChecked) {
		mValueHandler.setValue(isChecked);
		mDataModel.onChanged();
	}

	/**
	 * Sets the onCheckedChangeListener for the checkbox.
	 * @param text the checkbox to listen to
	 */
	private void setWatched(final CheckBox text) {
		final Object value = mValueHandler.getValue();
		if (Boolean.TRUE.equals(value)) {
			text.setChecked(true);
		} else {
			text.setChecked(false);
		}
		text.setOnCheckedChangeListener(this);
	}

}
