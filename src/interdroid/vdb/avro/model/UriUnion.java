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
			throw new RuntimeException();
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
	 * @param v the value to set
	 * @param schema the schema for the value
	 */
	public final void setValue(final Object v, final Schema schema) {
		LOG.debug("Union Value set to: {} {}", v, schema);
		mValue = v;
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
		for (Schema type : mSchema.getTypes()) {
			if (type.getType() == mType) {
				if (UriBoundAdapter.isNamedType(mType)) {
					if (type.getName().equals(mName)) {
						return type;
					}
				} else {
					return type;
				}
			}
		}
		return null;
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
		if (mValue != null) {
			UriDataManager.storeDataToUri(resolver, rootUri, values,
					fieldName, getTypeSchema(), mValue);
			if (UriBoundAdapter.isBoundType(mType)) {
				values.put(fieldName, getInstanceId((UriBound<?>) mValue));
			}
		} else {
			values.put(fieldName, -1);
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
		Uri uri = value.getInstanceUri();
		UriMatch match = EntityUriMatcher.getMatch(uri);
		LOG.debug("Instance id for: {} : {}", uri, match.entityIdentifier);
		if (match.entityIdentifier != null) {
			return Integer.parseInt(match.entityIdentifier);
		} else {
			return -1;
		}
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
		String name = NameHelper.getTypeName(fieldName);
		LOG.debug("Looking for column: {}", name);
		int index = cursor.getColumnIndex(name);
		LOG.debug("Got column: {}", index);
		if (index >= 0) {
			String typeName = cursor.getString(index);

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
			throw new RuntimeException("Column not in cursor:"
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

		Schema fieldType = getTypeSchema();
		setValue(BundleDataManager.loadDataFromBundle(saved,
				fieldName, fieldType), fieldType);
		return this;
	}

	/**
	 * @return the schema for the held type
	 */
	private Schema getTypeSchema() {
		Schema fieldType = null;
		if (mType == null) {
			return null;
		}
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
			throw new RuntimeException("Unable to find union inner type: "
					+ mType + " : " + mName);
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
		if (mValue == null) {
			return "[]";
		}
		return "[" + mValue.toString() + "]";
	}

}
