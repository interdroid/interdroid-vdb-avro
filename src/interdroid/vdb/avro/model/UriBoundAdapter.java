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

import org.apache.avro.Schema.Type;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;

/**
 * An adapter for implementing the UriBound interface.
 * This adapter holds the bound uri and takes care of various
 * bound checks and provides implementations with some handy
 * utilities for easing the implementation.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 * @param <A> the type being bound
 */
public class UriBoundAdapter<A> implements UriBound<A> {

	/**
	 * The interface for the implementation of a UriBoundAdapter.
	 * @author nick &lt;palmer@cs.vu.nl&gt;
	 *
	 * @param <A> the type being bound.
	 */
	static interface UriBoundAdapterImpl<A> {
		/**
		 * The implementation of UriBound.loadImpl.
		 * @param saved the bundle to load from
		 * @param prefix the prefix to load with
		 * @return the data
		 * @throws NotBoundException if the data isn't bound.
		 */
		A loadImpl(Bundle saved, String prefix)
				throws NotBoundException;

		/**
		 * The implementation of UriBound.saveImpl.
		 * @param saved the bundle to save to
		 * @param prefix the prefix to load with
		 * @throws NotBoundException if the data isn't bound
		 */
		void saveImpl(Bundle saved, String prefix)
				throws NotBoundException;

		/**
		 * The implementation of UriBound.delete.
		 * @param resolver the resolver to use
		 * @throws NotBoundException if the data isn't bound
		 */
		void deleteImpl(ContentResolver resolver)
				throws NotBoundException;

		/**
		 * The implementation of UriBound.load.
		 * @param resolver the resolver to load from
		 * @param fieldName the field to load
		 * @return the value for the field
		 * @throws NotBoundException if the data isn't bound
		 */
		A loadImpl(ContentResolver resolver, String fieldName)
				throws NotBoundException;

		/**
		 * The implementation of UriBound.save.
		 * @param resolver the resolver to save with
		 * @param fieldName the field to be saved
		 * @throws NotBoundException if the data isn't bound
		 */
		void saveImpl(ContentResolver resolver, String fieldName)
				throws NotBoundException;
	}

	/**
	 * The instance uri this data is bound to.
	 */
	private Uri mInstanceUri;
	/**
	 * The adapter implementation we use to handle binding.
	 */
	private UriBoundAdapterImpl<A> mAdapter;

	/**
	 * Constructs an adapter for the given Uri using the given implementation.
	 * @param uri the uri to use
	 * @param adapter the adapter to use for binding
	 */
	public UriBoundAdapter(final Uri uri,
			final UriBoundAdapterImpl<A> adapter) {
		mInstanceUri = uri;
		mAdapter = adapter;
	}

	/**
	 * Constructs an adapter using the given prefix, bundle and implementation.
	 * @param prefix the prefix to load with
	 * @param saved the bundle to load from
	 * @param adapter the adapter with the implementation
	 */
	public UriBoundAdapter(final String prefix, final Bundle saved,
			final UriBoundAdapterImpl<A> adapter) {
		mInstanceUri = saved.getParcelable(NameHelper.getTypeNameUri(prefix));
		mAdapter = adapter;
	}

	/**
	 * Constructs an adapter with the given bundle and implementation.
	 * @param saved the bundle to load from
	 * @param adapter the adapter with the implementation
	 */
	public UriBoundAdapter(final Bundle saved,
			final UriBoundAdapterImpl<A> adapter) {
		this(null, saved, adapter);
	}

	@Override
	public final Uri getInstanceUri() throws NotBoundException {
		if (mInstanceUri == null) {
			throw new NotBoundException();
		}
		return mInstanceUri;
	}

	@Override
	public final void setInstanceUri(final Uri uri) {
		mInstanceUri = uri;
	}

	@Override
	public final void save(final ContentResolver resolver,
			final String fieldName) throws NotBoundException {
		verifyBound();
		mAdapter.saveImpl(resolver, fieldName);
	}

	/**
	 * @throws NotBoundException if this is not bound.
	 */
	private void verifyBound() throws NotBoundException {
		if (mInstanceUri == null) {
			throw new NotBoundException();
		}
	}

	@Override
	public final A load(final ContentResolver resolver,
			final String fieldName) throws NotBoundException {
		verifyBound();
		return mAdapter.loadImpl(resolver, fieldName);
	}

	@Override
	public final void save(final Bundle saved,
			final String prefix) throws NotBoundException {
		mAdapter.saveImpl(saved, null);
	}

	@Override
	public final A load(final Bundle saved, final String prefix)
			throws NotBoundException {
		return mAdapter.loadImpl(saved, prefix);
	}


	@Override
	public final void delete(final ContentResolver resolver)
			throws NotBoundException {
		verifyBound();
		mAdapter.deleteImpl(resolver);
	}

	/**
	 * @param type the type to check
	 * @return true if the type is uri bound
	 */
	protected static boolean isBoundType(final Type type) {
		boolean ret;
		switch (type) {
		case ARRAY:
		case MAP:
		case RECORD:
			ret = true;
			break;
		default:
			ret = false;
		}
		return ret;
	}

	/**
	 * @param type the type to check
	 * @return true if the type is a named type
	 */
	public static boolean isNamedType(final Type type) {
		boolean ret;
		switch (type) {
		case RECORD:
		case ENUM:
		case FIXED:
			ret = true;
			break;
		default:
			ret = false;
		}
		return ret;
	}
}
