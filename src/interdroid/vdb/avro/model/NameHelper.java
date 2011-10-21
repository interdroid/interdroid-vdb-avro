package interdroid.vdb.avro.model;

import interdroid.vdb.content.avro.AvroContentProvider;

/**
 * A class for assisting with managing field names.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
final class NameHelper {

	/**
	 * No construction.
	 */
	private NameHelper() {
		// No construction
	}

    /* =-=-=-= Helper Constants For More Readable Code =-=-=-= */
	/** The separator for field names. */
    private static final String SEPARATOR =
    		AvroContentProvider.SEPARATOR;
    /** The count field suffix. */
    private static final String SUFFIX_COUNT =
    		SEPARATOR + "count";
    /** The key field suffix. */
    private static final String SUFFIX_KEY =
    		AvroContentProvider.KEY_COLUMN_NAME;
    /** the value field suffix. */
    private static final String SUFFIX_VALUE =
    		AvroContentProvider.VALUE_COLUMN_NAME;
    /** The type field suffix. */
    private static final String SUFFIX_TYPE =
    		AvroContentProvider.TYPE_COLUMN_NAME;
    /** The type name suffix. */
    private static final String SUFFIX_TYPE_NAME =
    		AvroContentProvider.TYPE_NAME_COLUMN_NAME;
    /** The uri name suffix. */
    private static final String SUFFIX_URI_NAME =
    		AvroContentProvider.TYPE_URI_COLUMN_NAME;

    /**
     * @param dataFullName the full name of the data type
     * @param fieldName the field name
     * @return the full field name for a field
     */
    static String getFieldFullName(final String dataFullName, // NOPMD by nick
    		final String fieldName) {
        return dataFullName + SEPARATOR + fieldName;
    }

   	/**
   	 *
   	 * @param fieldFullName the full field name
   	 * @return the name of a count field
   	 */
    static String getCountName(final String fieldFullName) { // NOPMD by nick
        return fieldFullName + SUFFIX_COUNT;
    }

    /**
     * @param fieldFullName the full name for the field
     * @param index the index into the array
     * @return a field name with the index appended
     */
    static String getIndexedFieldName(final String fieldFullName, // NOPMD nick
    		final int index) {
        return fieldFullName + SEPARATOR + index;
    }

    /**
     * @param prefix the prefix for the field
     * @param fullName the full name for the field
     * @return a name with the given prefix added if non-null
     */
    static String getPrefixName(final String prefix, // NOPMD by nick
    		final String fullName) {
        String dataFullName = fullName; // NOPMD by nick
        if (prefix != null) {
            dataFullName = prefix + SEPARATOR + fullName;
        }
        return dataFullName;
    }

    /**
     * @param fieldFullName the full name of the field
     * @return the map key column name
     */
    static String getMapValueName(final String fieldFullName) { // NOPMD by nick
        return fieldFullName + SUFFIX_VALUE;
    }

    /**
     * @param fieldFullName the full name of the field
     * @return the map value column name
     */
    static String getMapKeyName(final String fieldFullName) { // NOPMD by nick
        return fieldFullName + SUFFIX_KEY;
    }

    /**
     * @param fieldName the full field name
     * @return the type field for the field
     */
    static String getTypeName(final String fieldName) { // NOPMD by nick
        return fieldName + SUFFIX_TYPE;
    }

    /**
     * @param fieldName the full field name
     * @return the type name for the field
     */
    static String getTypeNameName(final String fieldName) { // NOPMD by nick
        return fieldName + SUFFIX_TYPE_NAME;
    }

    /**
     * @param fieldName the full field name
     * @return the uri name for the field
     */
    static String getTypeNameUri(final String fieldName) { // NOPMD by nick
        return fieldName + SUFFIX_URI_NAME;
    }

}
