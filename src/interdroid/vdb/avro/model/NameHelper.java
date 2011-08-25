package interdroid.vdb.avro.model;

import interdroid.vdb.content.avro.AvroContentProvider;

class NameHelper {

    /* =-=-=-= Helper Constants For More Readable Code In This Class =-=-=-= */
    static final String SEPARATOR = AvroContentProvider.SEPARATOR;
    static final String _COUNT = SEPARATOR + "count";
    static final String _KEY = AvroContentProvider.KEY_COLUMN_NAME;
    static final String _VALUE = AvroContentProvider.VALUE_COLUMN_NAME;
    static final String _TYPE = AvroContentProvider.TYPE_COLUMN_NAME;
    static final String _TYPE_NAME = AvroContentProvider.TYPE_NAME_COLUMN_NAME;
    static final String _URI_NAME = AvroContentProvider.TYPE_URI_COLUMN_NAME;

    static String getFieldFullName(String dataFullName, String fieldName) {
        return dataFullName + SEPARATOR + fieldName;
    }

    static String getCountName(String fieldFullName) {
        return fieldFullName + _COUNT;
    }

    static String getIndexedFieldName(String fieldFullName, int i) {
        return fieldFullName + SEPARATOR + i;
    }

    static String getPrefixName(String prefix, String fullName) {
        String dataFullName = fullName;
        if (prefix != null) {
            dataFullName = prefix + SEPARATOR + fullName;
        }
        return dataFullName;
    }

    static String getMapValueName(String fieldFullName) {
        return fieldFullName + _KEY;
    }

    static String getMapKeyName(String fieldFullName) {
        return fieldFullName + _VALUE;
    }

    static String getTypeName(String fieldName) {
        return fieldName + _TYPE;
    }

    static String getTypeNameName(String fieldName) {
        return fieldName + _TYPE_NAME;
    }

    static String getTypeNameUri(String fieldName) {
        return fieldName + _URI_NAME;
    }

}
