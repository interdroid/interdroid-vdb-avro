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
package interdroid.vdb.avro.model;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;

/**
 * A handler for persisting models to bundles.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public final class BundleDataManager { // NOPMD by nick
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(BundleDataManager.class);

	/**
	 * No construction.
	 */
	private BundleDataManager() {
		// No construction please.
	}

	/**
	 * Loads data from a bundle.
	 * @param saved the bundle to load from
	 * @param fieldFullName the full name of the field
	 * @param fieldSchema the schema for the field
	 * @return the data
	 * @throws NotBoundException if the record model is not bound
	 */
	@SuppressWarnings("rawtypes") // NOPMD by nick
	static Object loadDataFromBundle(final Bundle saved, // NOPMD by nick
			final String fieldFullName, final Schema fieldSchema)
					throws NotBoundException {
		LOG.debug("Loading data from bundle: " + fieldFullName);
		Object value;
		switch (fieldSchema.getType()) {
		case ARRAY:
			value = new UriArray(fieldSchema, saved).load(saved, fieldFullName);
			break;
		case BOOLEAN:
			value = saved.getBoolean(fieldFullName);
			break;
		case BYTES:
			value = saved.getByteArray(fieldFullName);
			break;
		case DOUBLE:
			value = saved.getDouble(fieldFullName);
			break;
		case ENUM:
			value = saved.getString(fieldFullName);
			break;
		case FIXED:
			value = saved.getByteArray(fieldFullName);
			break;
		case FLOAT:
			value = saved.getFloat(fieldFullName);
			break;
		case INT:
			value = saved.getInt(fieldFullName);
			break;
		case LONG:
			value = saved.getLong(fieldFullName);
			break;
		case MAP:
			value = new UriMap(fieldSchema, saved).load(saved, fieldFullName);
			break;
		case NULL:
			value = null; // NOPMD by nick
			break;
		case RECORD:
			value = new UriRecord(fieldSchema, saved).load(
					saved, fieldFullName);
			break;
		case STRING:
			value = saved.getString(fieldFullName);
			break;
		case UNION:
			value = new UriUnion(fieldSchema).load(saved, fieldFullName);
			break;
		default:
			throw new IllegalArgumentException(
					"Unsupported type: " + fieldSchema);
		}
		return value;
	}

	/**
	 * Stores data to a bundle.
	 * @param outState the bundle to store to
	 * @param fieldFullName the full name of the field to store
	 * @param fieldSchema the schema for the field
	 * @param data the data to store
	 * @throws NotBoundException if the data is not bound properly
	 */
	@SuppressWarnings("rawtypes") // NOPMD by nick
	static void storeDataToBundle(final Bundle outState, // NOPMD by nick
			final String fieldFullName, final Schema fieldSchema,
			final Object data) throws NotBoundException {
		if (data != null) {
			switch (fieldSchema.getType()) {
			case ARRAY:
				final UriArray array = (UriArray) data;
				array.save(outState, fieldFullName);
				break;
			case BOOLEAN:
				outState.putBoolean(fieldFullName, (Boolean) data);
				break;
			case BYTES:
				outState.putByteArray(fieldFullName, (byte[]) data);
				break;
			case DOUBLE:
				outState.putDouble(fieldFullName, (Double) data);
				break;
			case ENUM:
				outState.putInt(fieldFullName, (Integer) data);
				break;
			case FIXED:
				outState.putByteArray(fieldFullName, (byte[]) data);
				break;
			case FLOAT:
				outState.putFloat(fieldFullName, (Float) data);
				break;
			case INT:
				outState.putInt(fieldFullName, (Integer) data);
				break;
			case LONG:
				outState.putLong(fieldFullName, (Long) data);
				break;
			case MAP:
				final UriMap<?> map = (UriMap<?>) data;
				map.save(outState, fieldFullName);
				break;
			case NULL:
				// No need to do anything.
				break;
			case RECORD:
				final UriRecord record = (UriRecord) data;
				record.save(outState, fieldFullName);
				break;
			case STRING:
				outState.putString(fieldFullName, (String) data);
				break;
			case UNION:
				final UriUnion union = (UriUnion) data;
				union.save(outState, fieldFullName);
				break;
			default:
				throw new IllegalArgumentException(
						"Unsupported type: " + fieldSchema);
			}
		}
	}

}
