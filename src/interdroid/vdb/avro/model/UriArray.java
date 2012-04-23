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

import interdroid.vdb.avro.model.UriBoundAdapter.UriBoundAdapterImpl;
import interdroid.vdb.content.EntityUriMatcher;
import interdroid.vdb.content.EntityUriMatcher.UriMatch;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/**
 * A GenericData.Array which is UriBound.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 * @param <A> the inner type for the array
 */
public class UriArray<A> extends GenericData.Array<A>
implements UriBound<UriArray<A>> {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(UriArray.class);

	/** The adapter used to bind this to a Uri. */
	private final UriBoundAdapter<UriArray<A>> mUriBinder;

	/** The default size to construct arrays with. */
	private static final int DEFAULT_ARRAY_SIZE = 10;

	/** The implementation of our UriBoundAdapter. */
	private final UriBoundAdapterImpl<UriArray<A>> mBinderImpl =
			new UriBoundAdapterImpl<UriArray<A>>() {

		@Override
		public final void saveImpl(final ContentResolver resolver,
				final String fieldName) throws NotBoundException {
			LOG.debug("Saving array: {} : {}", getInstanceUri(), fieldName);

			deleteImpl(resolver, false);

			final ContentValues values = new ContentValues();
			for (Object value : UriArray.this) {
				values.clear();
				// First insert a null row
				final Uri idUri = UriDataManager.insertUri(resolver,
						getInstanceUri(), values);
				LOG.debug("Got id uri for array row: " + idUri);
				final Uri dataUri = UriDataManager.storeDataToUri(resolver,
						idUri, values, fieldName,
						getSchema().getElementType(), value);
				if (dataUri != null) {
					final UriMatch match = EntityUriMatcher.getMatch(dataUri);
					values.put(fieldName, match.entityIdentifier);
				}
				UriDataManager.updateUriOrThrow(resolver, idUri, values);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public final UriArray<A> loadImpl(final ContentResolver resolver,
				final String fieldName) throws NotBoundException {
			LOG.debug("Loading array from uri: {} : {}", getInstanceUri(),
					getSchema());
			final Cursor cursor = resolver.query(getInstanceUri(),
					null, null, null, null);
			try {
				if (cursor != null) {
					while (cursor.moveToNext()) {
						add((A) UriDataManager.loadDataFromUri(resolver,
								getInstanceUri(), cursor, fieldName,
								getSchema().getElementType()));
					}
				} else {
					throw new IllegalArgumentException("Unable to load: "
							+ getInstanceUri());
				}
			} finally {
				UriDataManager.safeClose(cursor);
			}
			return UriArray.this;
		}

		@Override
		public final void deleteImpl(final ContentResolver resolver)
				throws NotBoundException {
			deleteImpl(resolver, true);
		}

		@SuppressWarnings("rawtypes")
		public final void deleteImpl(final ContentResolver resolver,
				final boolean recursion)
						throws NotBoundException {
			LOG.debug("Deleting Array: {}", getInstanceUri());
			if (recursion) {
				LOG.debug("Handling recursive delete of array.");
				if (UriBoundAdapter.isBoundType(
						getSchema().getElementType().getType())) {
					for (Object element : UriArray.this) {
						if (element != null) {
							((UriBound) element).delete(resolver);
						}
					}
				} else if (getSchema().getElementType().getType()
						== Type.UNION) {
					for (Object element : UriArray.this) {
						((UriUnion) element).delete(resolver);
					}
				}
			}
			resolver.delete(getInstanceUri(), null, null);
		}

		@Override
		public void saveImpl(final Bundle outState, final String fieldFullName)
				throws NotBoundException {
			LOG.debug("Storing to bundle: {} field: ", outState, fieldFullName);
			switch (getSchema().getElementType().getType()) {
			case ARRAY:
				saveArray(outState, fieldFullName);
				break;
			case BOOLEAN:
				final boolean[] bools = saveBoolean();
				outState.putBooleanArray(fieldFullName, bools);
				break;
			case BYTES:
				saveBytes(outState, fieldFullName);
				break;
			case DOUBLE:
				saveDouble(outState, fieldFullName);
				break;
			case ENUM:
				saveString(outState, fieldFullName);
				break;
			case FIXED:
				saveBytes(outState, fieldFullName);
				break;
			case FLOAT:
				saveFloat(outState, fieldFullName);
				break;
			case INT:
				saveInt(outState, fieldFullName);
				break;
			case LONG:
				saveLong(outState, fieldFullName);
				break;
			case MAP:
				saveMap(outState, fieldFullName);
				break;
			case NULL:
				saveSize(outState, fieldFullName);
				break;
			case RECORD:
				saveRecord(outState, fieldFullName);
				break;
			case STRING:
				saveString(outState, fieldFullName);
				break;
			case UNION:
				saveSize(outState, fieldFullName);
				int i = 0;
				for (Object element : UriArray.this) {
					((UriUnion) element).save(outState,
							NameHelper.getIndexedFieldName(fieldFullName, i++));
				}
				break;
			default:
				throw new IllegalArgumentException("Unsupported array type: "
						+ getSchema());
			}
		}

		private void saveRecord(final Bundle outState,
				final String fieldFullName) throws NotBoundException {
			saveSize(outState, fieldFullName);
			for (Object element : UriArray.this) {
				((UriRecord) element).save(outState, fieldFullName);
			}
		}

		private void
				saveSize(final Bundle outState, final String fieldFullName) {
			outState.putInt(NameHelper.getCountName(fieldFullName),
					size());
		}

		private void saveMap(final Bundle outState, final String fieldFullName)
				throws NotBoundException {
			saveSize(outState, fieldFullName);
			int i = 0;
			for (Object element : UriArray.this) {
				((UriMap<?>) element).save(outState,
						NameHelper.getIndexedFieldName(fieldFullName, i++));
			}
		}

		private void
				saveLong(final Bundle outState, final String fieldFullName) {
			long[] longs = new long[size()];
			int i = 0;
			for (Object element : UriArray.this) {
				longs[i++] = (Long) element;
			}
			outState.putLongArray(fieldFullName, longs);
		}

		private void saveInt(final Bundle outState,
				final String fieldFullName) {
			int[] ints = new int[size()];
			int i = 0;
			for (Object element : UriArray.this) {
				ints[i++] = (Integer) element;
			}
			outState.putIntArray(fieldFullName, ints);
		}

		private void
				saveFloat(final Bundle outState, final String fieldFullName) {
			float[] floats = new float[size()];
			int i = 0;
			for (Object element : UriArray.this) {
				floats[i++] = (Float) element;
			}
			outState.putFloatArray(fieldFullName, floats);
		}

		private void
				saveString(final Bundle outState, final String fieldFullName) {
			String[] strings = new String[size()];
			int i = 0;
			for (Object element : UriArray.this) {
				strings[i++] = (String) element;
			}
			outState.putStringArray(fieldFullName, strings);
		}

		private void saveDouble(final Bundle outState,
				final String fieldFullName) {
			double[] doubles = new double[size()];
			int i = 0;
			for (Object element : UriArray.this) {
				doubles[i++] = (Double) element;
			}
			outState.putDoubleArray(fieldFullName, doubles);
		}

		private void
				saveBytes(final Bundle outState, final String fieldFullName) {
			saveSize(outState, fieldFullName);
			int i = 0;
			for (Object element : UriArray.this) {
				outState.putByteArray(NameHelper.getIndexedFieldName(
						fieldFullName, i++),
						(byte[]) element);
			}
		}

		private boolean[] saveBoolean() {
			boolean[] bools = new boolean[size()];
			int i = 0;
			for (Object element : UriArray.this) {
				bools[i++] = (Boolean) element;
			}
			return bools;
		}

		private void
				saveArray(final Bundle outState, final String fieldFullName)
						throws NotBoundException {
			saveSize(outState, fieldFullName);
			int i = 0;
			for (Object element : UriArray.this) {
				((UriArray<?>) element).save(outState,
						NameHelper.getIndexedFieldName(fieldFullName, i++));
			}
		}

		@Override
		public UriArray<A> loadImpl(final Bundle saved, final String fieldName)
				throws NotBoundException {
			switch (getSchema().getElementType().getType()) {
			case ARRAY:
				loadArray(saved, fieldName);
				break;
			case BOOLEAN:
				loadBoolean(saved, fieldName);
				break;
			case BYTES:
				loadBytes(saved, fieldName);
				break;
			case DOUBLE:
				loadDouble(saved, fieldName);
				break;
			case ENUM:
				loadString(saved, fieldName);
				break;
			case FIXED:
				loadBytes(saved, fieldName);
				break;
			case FLOAT:
				loadFloat(saved, fieldName);
				break;
			case INT:
				loadInt(saved, fieldName);
				break;
			case LONG:
				loadLong(saved, fieldName);
				break;
			case MAP:
				loadMap(saved, fieldName);
				break;
			case NULL:
				loadNull(saved, fieldName);
				break;
			case RECORD:
				loadRecord(saved, fieldName);
				break;
			case STRING:
				loadString(saved, fieldName);
				break;
			case UNION:
				loadUnion(saved, fieldName);
				break;
			default:
				throw new IllegalArgumentException("Unsupported array type: "
						+ getSchema());
			}

			return UriArray.this;
		}

		@SuppressWarnings("unchecked")
		private void loadUnion(final Bundle saved, final String fieldName)
				throws NotBoundException {
			int count = saved.getInt(NameHelper.getCountName(fieldName));
			for (int i = 0; i < count; i++) {
				add((A) new UriUnion(getSchema().getElementType()).load(
						saved, NameHelper.getIndexedFieldName(
								fieldName, i)));
			}
		}

		@SuppressWarnings("unchecked")
		private void loadRecord(final Bundle saved, final String fieldName)
				throws NotBoundException {
			int count = saved.getInt(NameHelper.getCountName(fieldName));
			for (int i = 0; i < count; i++) {
				add((A) new UriRecord(getInstanceUri(),
						getSchema().getElementType()).load(
								saved, NameHelper.getIndexedFieldName(
										fieldName, i)));
			}
		}

		private void loadNull(final Bundle saved, final String fieldName) {
			int count = saved.getInt(NameHelper.getCountName(fieldName));
			for (int i = 0; i < count; i++) {
				add(null);
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void loadMap(final Bundle saved, final String fieldName)
				throws NotBoundException {
			int count = saved.getInt(NameHelper.getCountName(fieldName));
			for (int i = 0; i < count; i++) {
				add((A) new UriMap(getInstanceUri(),
						getSchema().getElementType()).load(
								saved, NameHelper.getIndexedFieldName(
										fieldName, i)));
			}
		}

		@SuppressWarnings("unchecked")
		private void loadLong(final Bundle saved, final String fieldName) {
			long[] savedData = saved.getLongArray(fieldName);
			if (savedData != null) {
				for (long value : savedData) {
					add((A) Long.valueOf(value));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void loadInt(final Bundle saved, final String fieldName) {
			int[] savedData = saved.getIntArray(fieldName);
			if (savedData != null) {
				for (int value : savedData) {
					add((A) Integer.valueOf(value));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void loadFloat(final Bundle saved, final String fieldName) {
			float[] savedData = saved.getFloatArray(fieldName);
			if (savedData != null) {
				for (float value : savedData) {
					add((A) Float.valueOf(value));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void loadString(final Bundle saved, final String fieldName) {
			String[] savedData = saved.getStringArray(fieldName);
			if (savedData != null) {
				for (String value : savedData) {
					add((A) value);
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void loadDouble(final Bundle saved, final String fieldName) {
			double[] savedData = saved.getDoubleArray(fieldName);
			if (savedData != null) {
				for (double value : savedData) {
					add((A) Double.valueOf(value));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void loadBytes(final Bundle saved, final String fieldName) {
			int count = saved.getInt(NameHelper.getCountName(fieldName));
			for (int i = 0; i < count; i++) {
				byte[] data = saved.getByteArray(
						NameHelper.getIndexedFieldName(
								fieldName, i));
				add((A) data);
			}
		}

		@SuppressWarnings("unchecked")
		private void loadBoolean(final Bundle saved, final String fieldName) {
			boolean[] savedData = saved.getBooleanArray(fieldName);
			if (savedData != null) {
				for (boolean value : savedData) {
					add((A) Boolean.valueOf(value));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void loadArray(final Bundle saved, final String fieldName)
				throws NotBoundException {
			int count = saved.getInt(NameHelper.getCountName(fieldName));
			for (int i = 0; i < count; i++) {
				add((A) new UriArray<A>(getInstanceUri(),
						getSchema().getElementType()).load(
								saved,
								NameHelper.getIndexedFieldName(
										fieldName, i)));
			}
		}

	};

	/**
	 * Construct with the given schema from the bundle.
	 * @param schema the schema for the array
	 * @param saved the bundle with data
	 */
	public UriArray(final Schema schema, final Bundle saved) {
		super(DEFAULT_ARRAY_SIZE, schema);
		mUriBinder = new UriBoundAdapter<UriArray<A>>(saved, mBinderImpl);
	}

	/**
	 * Construct with the given schema from the given uri.
	 * @param uri the uri with the data
	 * @param schema the schema for the array
	 */
	public UriArray(final Uri uri, final Schema schema) {
		super(DEFAULT_ARRAY_SIZE, schema);
		LOG.debug("UriArray built and bound to: {}", uri);
		mUriBinder = new UriBoundAdapter<UriArray<A>>(uri, mBinderImpl);
	}

	@Override
	public final Uri getInstanceUri() throws NotBoundException {
		return mUriBinder.getInstanceUri();
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
	public final UriArray<A> load(final ContentResolver resolver,
			final String fieldName)
					throws NotBoundException {
		return mUriBinder.load(resolver, fieldName);
	}

	@Override
	public final void save(final Bundle outState, final String prefix)
			throws NotBoundException {
		mUriBinder.save(outState, prefix);
	}

	@Override
	public final UriArray<A> load(final Bundle saved,
			final String prefix) throws NotBoundException {
		return mUriBinder.load(saved, prefix);
	}

	@Override
	public final void delete(final ContentResolver resolver)
			throws NotBoundException {
		mUriBinder.delete(resolver);
	}

}
