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
package interdroid.vdb.avro.view.factory;

import interdroid.vdb.avro.AvroSchemaProperties;

import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the type of a given view.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroViewType {
	/**
	 * Access to LOG.
	 */
	private static final Logger LOG =
			LoggerFactory.getLogger(AvroViewType.class);

	/**
	 * A prime we use in hashing.
	 */
	private static final int	HASH_PRIME	= 71;

	/**
	 * The Avro Type this builder knows how to build.
	 */
	private final Type mType;

	/**
	 * The ui.widget this view builder knows how to build.
	 */
	private final String mWidget;

	/**
	 * Constructs a new view type represented by the given field.
	 * @param field the field to represent
	 */
	public AvroViewType(final Field field) {
		// Widget may be either on schema or on field.
		LOG.debug("View Type: {} {}", field.schema().getType(),
				field.schema().getProp(AvroSchemaProperties.UI_WIDGET));
		LOG.debug("View Type: {} {}", field.schema().getType(),
				field.getProp(AvroSchemaProperties.UI_WIDGET));
		if (field.schema().getProp(AvroSchemaProperties.UI_WIDGET) != null) {
			mWidget = field.schema().getProp(AvroSchemaProperties.UI_WIDGET);
		} else {
			mWidget = field.getProp(AvroSchemaProperties.UI_WIDGET);
		}
		mType = field.schema().getType();
	}

	/**
	 * @return the type for this ViewType
	 */
	public final Type getType() {
		return mType;
	}

	/**
	 * @return the widget for this ViewType. May be null.
	 */
	public final String getWidget() {
		return mWidget;
	}

	/**
	 * Constructs a new view type represented by the given parameters.
	 * @param type the type
	 * @param widget the widget type or null
	 */
	public AvroViewType(final Type type, final String widget) {
		mType = type;
		mWidget = widget;
	}

	/**
	 * Constructs a new view type represented by the given type
	 * with a null widget.
	 * @param type the type to support
	 */
	public AvroViewType(final Type type) {
		mType = type;
		mWidget = null;
	}

	/**
	 * @see Object.equals()
	 * @param o the object to compare to
	 * @return true if they are equal
	 */
	@Override
	public final boolean equals(final Object o) {
		try {
			AvroViewType other = (AvroViewType) o;

			if (mType.equals(other.mType)
				&& ((mWidget == null && other.mWidget == null)
					|| (mWidget != null
						&& mWidget.equals(other.mWidget)))) {
					return true;
				}
		} catch (Exception e) {
			LOG.warn("Exception while checking equality", e);
			return false;
		}
		return false;
	}

	/**
	 * @see Object.hashCode()
	 * @return the hash code
	 */
	@Override
	public final int hashCode() {
		if (mWidget == null) {
			return mType.hashCode();
		} else {
			return mType.hashCode() + (HASH_PRIME * mWidget.hashCode());
		}
	}

	@Override
	public final String toString() {
		return "Type: " + mType + " Widget: " + mWidget;
	}
}
