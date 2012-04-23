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

import interdroid.vdb.content.EntityUriMatcher;
import interdroid.vdb.content.EntityUriMatcher.UriMatch;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/**
 * Represents a union. This class is required because we need to be
 * able to track the type and the value of that type which in Avro
 * are implicit but for us must be explicitly managed.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class UriUnion {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(UriUnion.class);

	/** The current value for the union. */
	private Object mValue;
	/** The schema for the union. */
	private final Schema mSchema;
	/** The current type being held. */
	private Type mType;
	/** The name of the type if the type is a named type. */
	private String mName;

	/**
	 * Construct a new union.
	 * @param fieldSchema the schema for the union
	 */
	public UriUnion(final Schema fieldSchema) {
		LOG.debug("UriUnion constructed with schema: {} {}",
				fieldSchema.getType(), fieldSchema);
		if (!fieldSchema.getType().equals(Type.UNION)) {
			LOG.error("Wrong type for union.");
			throw new IllegalArgumentException("Not a union.");
		}
		mSchema = fieldSchema;
	}

	/**
	 * @return the current value for the union
	 */
	public final Object getValue() {
		return mValue;
	}

	/**
	 * Sets the value for the union to the given value which must be.
	 * of the given schema
	 * @param value the value to set
	 * @param schema the schema for the value
	 */
	public final void setValue(final Object value, final Schema schema) {
		LOG.debug("Union Value set to: {} {}", value, schema);
		mValue = value;
		mType = schema.getType();
		mName = schema.getFullName();
	}

	/**
	 * @return the type the union currently holds
	 */
	public final Type getType() {
		return mType;
	}

	/**
	 * @return the name for the type the union curently holds or null
	 */
	public final String getTypeName() {
		return mName;
	}

	/**
	 * @return the schema for the value the union currently holds
	 */
	public final Schema getValueSchema() {
		Schema ret = null; // NOPMD by nick
		for (Schema type : mSchema.getTypes()) {
			if (type.getType() == mType) {
				if (UriBoundAdapter.isNamedType(mType)) {
					if (type.getName().equals(mName)) {
						ret = type;
						break;
					}
				} else {
					ret = type;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * Saves this union.
	 * @param resolver the resolver to use to save
	 * @param rootUri the root uri for the data being saved
	 * @param values the values being saved
	 * @param fieldName the name of the field being saved
	 * @throws NotBoundException if the data is not properly bound
	 */
	public final void save(final ContentResolver resolver, final Uri rootUri,
			final ContentValues values, final String fieldName)
					throws NotBoundException {
		if (mType == null) {
			values.putNull(NameHelper.getTypeName(fieldName));
		} else {
			values.put(NameHelper.getTypeName(fieldName), mType.toString());
		}
		values.put(NameHelper.getTypeNameName(fieldName), mName);
		if (mValue == null) {
			values.put(fieldName, -1);
		} else {
			UriDataManager.storeDataToUri(resolver, rootUri, values,
					fieldName, getTypeSchema(), mValue);
			if (UriBoundAdapter.isBoundType(mType)) {
				values.put(fieldName, getInstanceId((UriBound<?>) mValue));
			}
		}
		LOG.debug("Values now has: {}", values);
	}

	/**
	 * Returns the instance id for the given value.
	 * @param value the value to check
	 * @return the instance id or -1 if it has no id
	 * @throws NotBoundException if the data is not properly bound
	 */
	private int getInstanceId(final UriBound<?> value)
			throws NotBoundException {
		int ret = -1; // NOPMD by nick
		final Uri uri = value.getInstanceUri();
		final UriMatch match = EntityUriMatcher.getMatch(uri);
		LOG.debug("Instance id for: {} : {}", uri, match.entityIdentifier);
		if (match.entityIdentifier != null) {
			ret = Integer.parseInt(match.entityIdentifier);
		}
		return ret;
	}

	/**
	 * Loads the data from the union from the content provider.
	 * @param resolver the resolver to use
	 * @param rootUri the root uri for the data being loaded
	 * @param cursor the cursor to load from
	 * @param fieldName the name of the field being loaded
	 * @return the loaded union
	 * @throws NotBoundException if the data is not properly bound
	 */
	public final UriUnion load(final ContentResolver resolver,
			final Uri rootUri, final Cursor cursor, final String fieldName)
					throws NotBoundException {
		final String name = NameHelper.getTypeName(fieldName);
		LOG.debug("Looking for column: {}", name);
		final int index = cursor.getColumnIndex(name);
		LOG.debug("Got column: {}", index);
		if (index >= 0) {
			final String typeName = cursor.getString(index);

			if (typeName != null) {
				mType = Type.valueOf(typeName);
				mName = cursor.getString(cursor.getColumnIndex(
						NameHelper.getTypeNameName(fieldName)));
				mValue = UriDataManager.loadDataFromUri(resolver, rootUri,
						cursor, fieldName, getTypeSchema());
			}
		} else {
			LOG.debug("Cursor doesn't have field: {} {}",
					fieldName, cursor.getColumnNames());
			throw new IllegalStateException("Column not in cursor:"
					+ fieldName + " " + rootUri);
		}
		return this;
	}

	/**
	 * Save this union to the bundle.
	 * @param outState the bundle to save to
	 * @param fieldFullName the name of the field for this union
	 * @throws NotBoundException if the data is not properly bound
	 */
	public final void save(final Bundle outState, final String fieldFullName)
			throws NotBoundException {
		if (mType == null) {
			outState.putString(NameHelper.getTypeName(fieldFullName), null);
		} else {
			outState.putString(NameHelper.getTypeName(fieldFullName),
					mType.toString());
		}
		outState.putString(NameHelper.getTypeNameName(fieldFullName), mName);
		BundleDataManager.storeDataToBundle(outState, fieldFullName,
				getTypeSchema(), mValue);
		if (mValue != null && UriBoundAdapter.isBoundType(mType)) {
			outState.putParcelable(fieldFullName,
					((UriBound<?>) mValue).getInstanceUri());
		}
	}

	/**
	 * Deletes this union.
	 * @param values the values to store to
	 * @param fieldName the name of this field.
	 * @return null
	 */
	public final UriUnion delete(final ContentValues values,
			final String fieldName) {
		values.putNull(NameHelper.getTypeName(fieldName));
		values.putNull(NameHelper.getTypeNameName(fieldName));
		return null;
	}

	/**
	 * Loads a union from the bundle.
	 * @param saved the bundle to load from
	 * @param fieldName the name of the field to load
	 * @return the loaded union
	 * @throws NotBoundException if the data isn't bound properly
	 */
	public final UriUnion load(final Bundle saved, final String fieldName)
			throws NotBoundException {
		mType = Type.valueOf(saved.getString(
				NameHelper.getTypeName(fieldName)));
		mName = saved.getString(NameHelper.getTypeNameName(fieldName));

		final Schema fieldType = getTypeSchema();
		setValue(BundleDataManager.loadDataFromBundle(saved,
				fieldName, fieldType), fieldType);
		return this;
	}

	/**
	 * @return the schema for the held type
	 */
	private Schema getTypeSchema() {
		Schema fieldType = null; // NOPMD by nick
		if (mType != null) {
			for (Schema unionType : mSchema.getTypes()) {
				if (unionType.getType().equals(mType)
						&& (!UriBoundAdapter.isNamedType(mType)
								|| (mName.equals(unionType.getFullName())
										|| mName.equals(unionType.getName()))
								)) {
					fieldType = unionType;
					break;
				}
			}
			if (fieldType == null) {
				throw new
				IllegalStateException("Unable to find union inner type: "
						+ mType + " : " + mName);
			}
		}
		return fieldType;
	}

	/**
	 * Deletes using the given content resolver.
	 * @param resolver the resolver to use
	 * @throws NotBoundException if this is not bound properly.
	 */
	public final void delete(final ContentResolver resolver)
			throws NotBoundException {
		if (mValue != null && UriBoundAdapter.isBoundType(getType())) {
			((UriBound<?>) mValue).delete(resolver);
		}
	}

	@Override
	public final String toString() {
		String ret = "[]"; // NOPMD by nick
		if (mValue != null) {
			ret = "[" + mValue.toString() + "]";
		}
		return ret;
	}

}
