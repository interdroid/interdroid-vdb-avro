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
import org.apache.avro.Schema.Field;
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
 * Represents a GenericData.Record that is bound to a uri.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class UriRecord extends GenericData.Record
implements UriBound<UriRecord> {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(UriRecord.class);

	/** The binder adapter we use to bind to a uri. */
	private final UriBoundAdapter<UriRecord> mUriBinder;

	/** The implementation for the binder adapter. */
	private final UriBoundAdapterImpl<UriRecord> mBinderImpl =
			new UriBoundAdapterImpl<UriRecord>() {

		@Override
		public void saveImpl(final ContentResolver resolver,
				final String fieldFullName) throws NotBoundException {
			ContentValues values = new ContentValues();
			LOG.debug("Storing record: {}", fieldFullName);
			for (Field field : getSchema().getFields()) {
				String fieldName = field.name();
				// Store the data to either the values or the right table
				Uri dataUri = UriDataManager.storeDataToUri(resolver,
						getInstanceUri(), values, field.name(), field.schema(),
						get(fieldName));
				// Update our reference if this is a record
				if (field.schema().getType() == Type.RECORD
						&& dataUri != null) {
					UriMatch match = EntityUriMatcher.getMatch(dataUri);
					values.put(fieldName, match.entityIdentifier);
				}
			}
			// Now we can update the data for this record.
			UriDataManager.updateUriOrThrow(resolver, getInstanceUri(), values);
		}

		@Override
		public UriRecord loadImpl(final ContentResolver resolver,
				final String fullFieldName) throws NotBoundException {
			LOG.debug("Loading record from uri: {} : {}",
					getInstanceUri(), getSchema());

			Cursor cursor = resolver.query(getInstanceUri(),
					null, null, null, null);

			try {
				LOG.debug("Cursor is: {}", cursor);
				if (cursor != null && cursor.getCount() == 1) {
					cursor.moveToFirst();

					for (Field field : getSchema().getFields()) {
						String fieldName = field.name();
						// Load the data for this field
						Object value = UriDataManager.loadDataFromUri(resolver,
								getInstanceUri(), cursor, fieldName,
								field.schema());
						LOG.debug("Loaded: {} : {}", fieldName, value);
						// And store it in the record
						put(fieldName, value);
					}
				}
			} finally {
				UriDataManager.safeClose(cursor);
			}
			return UriRecord.this;
		}

		@Override
		public void deleteImpl(final ContentResolver resolver)
				throws NotBoundException {
			LOG.debug("Deleting Record: {}", getInstanceUri());

			// TODO: The fields here may not reflect what we
			// really need to do to delete if this is not
			// loaded to match the DB. For now we
			// assume this is a clean record but we really
			// should have loaded and dirty flags all through
			// the model. This is dangerous but good enough for
			// now.

			for (Field field : getSchema().getFields()) {
				String fieldName = field.name();
				if (UriBoundAdapter.isBoundType(field.schema().getType())) {
					UriBound<?> data = (UriBound<?>) get(fieldName);
					if (data != null) {
						data.delete(resolver);
					}
				}
			}

			resolver.delete(getInstanceUri(), null, null);
		}

		@Override
		public void saveImpl(final Bundle outState,
				final String prefix) throws NotBoundException {
			LOG.debug("Saving record to bundle: {} : {}",
					prefix, getSchema().getFullName());
			String dataFullName = NameHelper.getPrefixName(prefix, getSchema()
					.getFullName());

			outState.putParcelable(NameHelper.getTypeNameUri(dataFullName),
					getInstanceUri());

			for (Field field : getSchema().getFields()) {
				String fieldName = field.name();
				String fieldFullName =
						NameHelper.getFieldFullName(dataFullName, fieldName);
				BundleDataManager.storeDataToBundle(outState,
						fieldFullName, field.schema(), get(fieldName));
			}
		}

		@Override
		public UriRecord loadImpl(final Bundle saved, final String prefix)
				throws NotBoundException {
			LOG.debug(
					"Loading data from bundle: {} : {}",
					prefix, getSchema().getFullName());
			String dataFullName =
					NameHelper.getPrefixName(prefix, getSchema().getFullName());

			for (Field field : getSchema().getFields()) {
				String fieldName = field.name();
				LOG.debug("Loading field: " + fieldName);
				String fieldFullName =
						NameHelper.getFieldFullName(dataFullName, fieldName);
				put(fieldName,
						BundleDataManager.loadDataFromBundle(
								saved, fieldFullName, field.schema()));
			}

			return UriRecord.this;
		}

	};

	/**
	 * Construct a UriBound Record from a bundle.
	 * @param schema the schema for the record
	 * @param saved the bundle with the data
	 */
	public UriRecord(final Schema schema, final Bundle saved) {
		super(schema);
		mUriBinder = new UriBoundAdapter<UriRecord>(saved, mBinderImpl);
	}

	/**
	 * Construct a uri bound record from a uri.
	 * @param uri the uri to load from
	 * @param schema the schema for the record
	 */
	public UriRecord(final Uri uri, final Schema schema) {
		super(schema);
		LOG.debug("UriRecord built and bound to: {}", uri);
		mUriBinder = new UriBoundAdapter<UriRecord>(uri, mBinderImpl);
	}

	@Override
	public final Uri getInstanceUri() throws NotBoundException {
		return mUriBinder.getInstanceUri();
	}

	@Override
	public final void setInstanceUri(final Uri uri) {
		LOG.debug("UriRecord now bound to: {}", uri);
		mUriBinder.setInstanceUri(uri);
	}

	@Override
	public final void save(final ContentResolver resolver,
			final String fieldName)
			throws NotBoundException {
		mUriBinder.save(resolver, fieldName);
	}

	@Override
	public final UriRecord load(final ContentResolver resolver,
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
	public final UriRecord load(final Bundle b, final String prefix)
			throws NotBoundException {
		return mUriBinder.load(b, prefix);
	}

	@Override
	public final void delete(final ContentResolver resolver)
			throws NotBoundException {
		mUriBinder.delete(resolver);
	}

	/**
	 * Load from the given bundle.
	 * @param savedInstanceState the bundle to load from
	 * @return the record value
	 * @throws NotBoundException if the data is not bound properly
	 */
	public final UriRecord load(final Bundle savedInstanceState)
			throws NotBoundException {
		return mUriBinder.load(savedInstanceState, null);
	}

	/**
	 * Save to the content provider.
	 * @param resolver the resolver to save with
	 * @throws NotBoundException if this is not bound properly
	 */
	public final void save(final ContentResolver resolver) throws
	NotBoundException {
		mUriBinder.save(resolver, null);
	}

	/**
	 * Load from the content provider.
	 * @param resolver the resolver to load with
	 * @return the record
	 * @throws NotBoundException if this is not bound properly
	 */
	public final UriRecord load(final ContentResolver resolver) throws
	NotBoundException {
		return mUriBinder.load(resolver, null);
	}

	/**
	 * Save to the given bundle.
	 * @param outState the bundle to save to
	 * @throws NotBoundException if this is not bound properly
	 */
	public final void save(final Bundle outState) throws NotBoundException {
		mUriBinder.save(outState, null);
	}


}
