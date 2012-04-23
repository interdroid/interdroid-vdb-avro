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

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;

/**
 * The interface for data that is bound to a URI.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 * @param <A> the type which this data is bound to
 */
public interface UriBound<A> {

	/**
	 *
	 * @return the uri for this instance of the type
	 * @throws NotBoundException if it is not bound
	 */
	Uri getInstanceUri() throws NotBoundException;

	/**
	 * Sets the uri this data is bound to.
	 * @param uri the uri to which this data is bound
	 */
	void setInstanceUri(Uri uri);

	/**
	 * Saves the data in the given field to the given resolver.
	 * @param resolver the resolver to use
	 * @param fieldName the name of the field
	 * @throws NotBoundException if this data is not bound
	 */
	void save(ContentResolver resolver, String fieldName)
			throws NotBoundException;

	/**
	 * Loads the data from the resolver.
	 * @param resolver the resolver to use
	 * @param fieldName the field to be loaded
	 * @return the value for the field
	 * @throws NotBoundException if this is not bound
	 */
	A load(ContentResolver resolver, String fieldName) throws NotBoundException;

	/**
	 * Save the data to the bundle.
	 * @param outState the bundle to store to
	 * @param prefix the prefix to store with
	 * @throws NotBoundException if the data is not bound
	 */
	void save(Bundle outState, String prefix) throws NotBoundException;

	/**
	 * Load the data from the bundle.
	 * @param saved the bundle to load from
	 * @param prefix the prefix the data was stored with
	 * @return the value for the data
	 * @throws NotBoundException if the data is not properly bound
	 */
	A load(Bundle saved, String prefix) throws NotBoundException;

	/**
	 * Deletes the data from the content provider.
	 * @param resolver the resolver to delete with
	 * @throws NotBoundException if this is not bound properly
	 */
	void delete(ContentResolver  resolver) throws NotBoundException;

}
