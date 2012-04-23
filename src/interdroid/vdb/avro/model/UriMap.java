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

import interdroid.util.DbUtil;
import interdroid.vdb.avro.model.UriBoundAdapter.UriBoundAdapterImpl;
import interdroid.vdb.content.EntityUriMatcher;
import interdroid.vdb.content.EntityUriMatcher.UriMatch;

import java.util.HashMap;

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
 * Represents a UriBound Map implementation.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 * @param <V> the value type
 */
public class UriMap<V> extends HashMap<String, V>
implements UriBound<UriMap<V>> {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory.getLogger(UriMap.class);

	/** The serial version id for this class. */
	private static final long serialVersionUID = 1L;
	/** The schema for the map. */
	private final Schema mSchema;

	/**
	 * @return the schema for this map.
	 */
	public final Schema getSchema() {
		return mSchema;
	}

	/** The binder adapterused to bind this to a uri. */
	private final UriBoundAdapter<UriMap<V>> mUriBinder;

	/** The implementation we use for the uri binder. */
	private final UriBoundAdapterImpl<UriMap<V>> mBinderImpl =
			new UriBoundAdapterImpl<UriMap<V>>() {

		@SuppressWarnings("unchecked")
		@Override
		public UriMap<V> loadImpl(final Bundle saved,
				final String fieldFullName) throws NotBoundException {
			String keyName = NameHelper.getMapKeyName(fieldFullName);
			String valueName = NameHelper.getMapValueName(fieldFullName);

			int count = saved.getInt(NameHelper.getCountName(fieldFullName));
			for (int i = 0; i < count; i++) {
				String key = saved.getString(
						NameHelper.getIndexedFieldName(keyName, i));
				put(key, (V) BundleDataManager.loadDataFromBundle(
						saved, valueName, getSchema().getValueType()));
			}

			return UriMap.this;
		}

		@Override
		public void saveImpl(final Bundle outState, final String fieldFullName)
				throws NotBoundException {
			final String keyName = NameHelper.getMapKeyName(fieldFullName);
			final String valueName = NameHelper.getMapValueName(fieldFullName);
			outState.putParcelable(
					NameHelper.getTypeNameUri(fieldFullName), getInstanceUri());
			outState.putInt(NameHelper.getCountName(fieldFullName), size());
			int index = 0;
			for (String key : keySet()) {
				final String keyId =
						NameHelper.getIndexedFieldName(keyName, index);
				final String valueId =
						NameHelper.getIndexedFieldName(valueName, index++);

				outState.putString(keyId, key);
				BundleDataManager.storeDataToBundle(
						outState, valueId, getSchema().getValueType(),
						get(key));
			}
		}

		@Override
		public void deleteImpl(final ContentResolver resolver)
				throws NotBoundException {
			deleteImpl(resolver, true);
		}

		private void deleteImpl(final ContentResolver resolver,
				final boolean recursion)
				throws NotBoundException {
			LOG.debug("Deleting Map: " + getInstanceUri());
			if (recursion) {
				if (UriBoundAdapter.isBoundType(
						getSchema().getValueType().getType())) {
					for (Object element : UriMap.this.values()) {
						((UriBound<?>) element).delete(resolver);
					}
				} else if (getSchema().getValueType().getType() == Type.UNION) {
					for (Object element : UriMap.this.values()) {
						((UriUnion) element).delete(resolver);
					}
				}
			}
			resolver.delete(getInstanceUri(), null, null);
		}

		@Override
		public void saveImpl(final ContentResolver resolver,
				final String fieldName)
				throws NotBoundException {

			deleteImpl(resolver, false);

			final ContentValues values = new ContentValues();

			for (String key : UriMap.this.keySet()) {
				values.clear();
				values.put(NameHelper.getMapKeyName(fieldName), key);
				// First insert a row with just the key so we can get
				// the ID of the row in
				// case the row is really an array or some other table based row
				final Uri idUri = UriDataManager.insertUri(resolver,
						getInstanceUri(), values);
				LOG.debug("Got id uri for map row: " + idUri);

				final Uri dataUri = UriDataManager.storeDataToUri(resolver,
						idUri, values, fieldName,
						getSchema().getValueType(), get(key));
				if (dataUri != null) {
					final UriMatch match = EntityUriMatcher.getMatch(dataUri);
					values.put(fieldName, match.entityIdentifier);
				}
				UriDataManager.updateUriOrThrow(resolver, idUri, values);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public UriMap<V> loadImpl(final ContentResolver resolver,
				final String fieldName) throws NotBoundException {

			LOG.debug("Loading map from: " + getInstanceUri() + " : {} : {}",
					fieldName, getSchema());
			final Cursor cursor = resolver.query(getInstanceUri(),
					null, null, null, null);
			try {
				if (cursor != null) {
					final int keyIndex = DbUtil.getFieldIndex(cursor,
							NameHelper.getMapKeyName(fieldName));
					final int valueIndex = DbUtil.getFieldIndex(cursor,
							fieldName);
					while (cursor.moveToNext()) {
						Uri dataUri = Uri.withAppendedPath(getInstanceUri(),
								cursor.getString(keyIndex));
						Cursor dataCursor = cursor;
						try {
							if (UriBoundAdapter.isBoundType(
									getSchema().getValueType().getType())) {
								final int recordId = cursor.getInt(valueIndex);
								if (recordId > 0) {
									dataUri = Uri.withAppendedPath(
											UriDataManager.getRecordUri(
													getInstanceUri(),
													getSchema().getValueType()),
											String.valueOf(recordId));
									dataCursor = resolver.query(dataUri,
											null, null, null, null);
									if (dataCursor != null) {
										dataCursor.moveToFirst();
									}
								}
							}
							put(cursor.getString(keyIndex),
									(V) UriDataManager.loadDataFromUri(resolver,
											dataUri, dataCursor, fieldName,
											getSchema().getValueType()));
						} finally {
							if (UriBoundAdapter.isBoundType(
									getSchema().getValueType().getType())) {
								UriDataManager.safeClose(dataCursor);
							}
						}
					}
				} else {
					throw new IllegalArgumentException("Unable to load: "
							+ getInstanceUri());
				}
			} finally {
				UriDataManager.safeClose(cursor);
			}

			return UriMap.this;
		}

	};

	/**
	 * Construct from the given bundle and schema.
	 * @param schema the schema for the map
	 * @param saved the bundle with saved data
	 */
	public UriMap(final Schema schema, final Bundle saved) {
		super();
		mSchema = schema;
		mUriBinder = new UriBoundAdapter<UriMap<V>>(saved, mBinderImpl);
	}

	/**
	 * Construct bound to the given uri with the given schema.
	 * @param uri the uri to bind to
	 * @param schema the schema for the map
	 */
	public UriMap(final Uri uri, final Schema schema) {
		super();
		mSchema = schema;
		mUriBinder = new UriBoundAdapter<UriMap<V>>(uri, mBinderImpl);
	}

	@Override
	public final Uri getInstanceUri() throws NotBoundException {
		return mUriBinder.getInstanceUri();
	}

	@Override
	public final void save(final Bundle outState,
			final String fieldName) throws NotBoundException {
		mUriBinder.save(outState, fieldName);
	}

	@Override
	public final void delete(final ContentResolver resolver)
			throws NotBoundException {
		mUriBinder.delete(resolver);
	}

	@Override
	public final UriMap<V> load(final Bundle saved, final String prefix)
			throws NotBoundException {
		return mUriBinder.load(saved, prefix);
	}

	@Override
	public final void setInstanceUri(final Uri uri) {
		mUriBinder.setInstanceUri(uri);
	}

	@Override
	public final void save(final ContentResolver resolver,
			final String fieldName)
			throws NotBoundException {
		mUriBinder.save(resolver, fieldName);
	}

	@Override
	public final UriMap<V> load(final ContentResolver resolver,
			final String fieldName)
			throws NotBoundException {
		return mUriBinder.load(resolver, fieldName);
	}
}
