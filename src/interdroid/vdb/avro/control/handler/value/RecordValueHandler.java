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
package interdroid.vdb.avro.control.handler.value;

import java.io.IOException;

import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;

import org.apache.avro.Schema.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;

/**
 * Handles a field in a record.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class RecordValueHandler implements ValueHandler {
	/** Access to logger. */
	private static final Logger LOG =
			LoggerFactory.getLogger(RecordValueHandler.class);

	/** The record we are handling. */
	private final UriRecord mRecord;
	/** The name of the field we are handling. */
	private final String mFieldName;
	/** The data model for the record. */
	private final AvroRecordModel mDataModel;

	/**
	 * Construct a handler for record fields.
	 * @param model the model to work with
	 * @param record the record we are representing
	 * @param fieldName the name of the field being managed
	 */
	public RecordValueHandler(final AvroRecordModel model,
			final UriRecord record, final String fieldName) {
		mFieldName = fieldName;
		mRecord = record;
		mDataModel = model;
		LOG.debug("Constructed for: " + mRecord + "[" + fieldName + "]");
	}

	@Override
	public final Object getValue() {
		if (mRecord.get(mFieldName) == null) {
			Field field = mRecord.getSchema().getField(mFieldName);
			if (field.defaultValue() != null) {
				try {
					LOG.debug("Field {} has default.", mFieldName);
					Object defaultObject = AvroRecordModel.parseDefault(field);
					LOG.debug("Setting default value for: {} to: {}", mFieldName,
							defaultObject);
					setValue(defaultObject);
				} catch (IOException e) {
					LOG.warn("Error parsing default value. Ignored.", e);
				}
			}
		}
		return mRecord.get(mFieldName);
	}

	@Override
	public final void setValue(final Object value) {
		LOG.debug("Record Value Handler Setting {} to {}", mFieldName, value);
		mRecord.put(mFieldName, value);
		mDataModel.onChanged();
	}

	@Override
	public final String toString() {
		return "RecordValueHandler: " + mRecord + "[" + mFieldName + "]";
	}

	@Override
	public final Uri getValueUri() throws NotBoundException {
		return mRecord.getInstanceUri();
	}

	@Override
	public final String getFieldName() {
		return mFieldName;
	}

}
