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
package interdroid.vdb.avro.view;

import interdroid.util.ToastOnUI;
import interdroid.util.view.AsyncTaskWithProgressDialog;
import interdroid.vdb.Authority;
import interdroid.vdb.avro.AvroSchema;
import interdroid.vdb.avro.AvroSchemaProperties;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;
import interdroid.vdb.avro.model.UriUnion;
import interdroid.vdb.content.EntityUriBuilder;
import interdroid.vdb.content.avro.AvroSchemaRegistrationHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * An activity which knows how to take a schema record and build a db with it.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class AvroDBMaker extends Activity {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(AvroDBMaker.class);

	/** Exception thrown when the schema is invalid. */
	private static class InvalidSchemaException extends Exception {

		/**
		 * Make the compiler happy.
		 */
		private static final long serialVersionUID = 1L;

	}

	/** A class which holds data about a named type. */
	private static class NamedType {
		// Checkstyle shouldn't require getters and setters for private
		// inner classes.
		// CHECKSTYLE:OFF
		/** The name for the type. */
		String name;
		/** The namespace for the type. */
		String namespace;
		/** The doc for the type. */
		String doc;
		/** Any aliases for the type. */
		List<String> aliases;
		/** The UI Label for the type. */
		public String label;
		/** Should this be shown in the list view. */
		public boolean inList;
		// CHECKSTYLE:ON
	}

	/** Construct a db maker. */
	public AvroDBMaker() {
		LOG.debug("Constructed AvroDBMaker: " + this);
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/** Dialog to display while we make a database. */
	private final class LoadTask
		extends AsyncTaskWithProgressDialog<Void, Void, Uri> {

		/** Build the dialog. */
		public LoadTask() {
			super(AvroDBMaker.this, getString(R.string.label_loading),
			getString(R.string.label_wait));
		}

		@Override
		protected Uri doInBackground(final Void... params) {
			Schema schema = getSchema();
			LOG.debug("Got schema: {}", schema);
			Uri uri = null;

			if (schema != null) {
				// Now we need to create the database
				try {
					uri = createDb(schema);
				} catch (IOException e) {
					LOG.error("Error registering schema!", e);
				}
			} else {
				LOG.warn("Schema is null.");
			}
			return uri;
		}
	}

	@Override
	protected final void onStart() {
		super.onStart();
		try {
			processSchema();
		} catch (Exception e) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	}

	/**
	 * Processes the schema record into a schema.
	 */
	private void processSchema() {
		AsyncTask<Void, Void, Uri> load = new LoadTask().execute();
		try {
			Uri dbUri = load.get();
			if (dbUri != null) {
				LOG.debug("DB Created!");
				Intent i = new Intent(Intent.ACTION_VIEW, dbUri);
				LOG.debug("Finishing this activity.");
				this.setResult(RESULT_OK, i);
				finish();
			} else {
				throw new InvalidSchemaException();
			}
		} catch (Throwable e) {
			LOG.error("Schema was invalid. Launching Edit.");
			startActivityForResult(getEditIntent(), 0);
		}
	}

	@Override
	protected final void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		LOG.error("Got activity result: {} {} " + data,
				requestCode, resultCode);
		if (resultCode == RESULT_OK) {
			LOG.error("Edit activity said okay, attempting to process again.");
			processSchema();
		} else {
			LOG.error("Edit activity failed to return okay result. Finishing.");
			this.setResult(resultCode);
			finish();
		}
	}

	/**
	 * @return an intent for editing the schema record being processed.
	 */
	private Intent getEditIntent() {
		Intent request = getIntent();
		Intent editIntent;
		if (request.getData() == null) {
			editIntent = new Intent(Intent.ACTION_EDIT);
			editIntent.putExtra(AvroBaseEditor.SCHEMA,
					request.getStringExtra(AvroBaseEditor.SCHEMA));
		} else {
			editIntent = new Intent(Intent.ACTION_EDIT, request.getData());
		}
		editIntent.setClassName(this, AvroBaseEditor.class.getName());
		LOG.debug("Editing with: {}", editIntent);
		return editIntent;
	}

	/**
	 * @return the schema given to construct a db from.
	 */
	private Schema getSchema() {
		Intent intent = getIntent();
		Schema schema = null;
		if (intent.getData() == null) {

			String schemaJson = intent.getStringExtra(AvroBaseEditor.SCHEMA);
			if (schemaJson == null) {
				ToastOnUI.show(AvroDBMaker.this, R.string.error_no_schema,
						Toast.LENGTH_LONG);
				return null;
			} else {
				try {
					schema = Schema.parse(schemaJson);
				} catch (Exception e) {
					ToastOnUI.show(AvroDBMaker.this,
							R.string.error_parsing_schema, Toast.LENGTH_LONG);
				}
			}
		} else {
			try {
				schema = loadSchema(intent.getData());
			} catch (NotBoundException e) {
				ToastOnUI.show(AvroDBMaker.this,
						R.string.error_parsing_schema, Toast.LENGTH_LONG);
			} catch (Exception e) {
				LOG.error("Schema Invalid!", e);
			}
		}

		return schema;
	}

	/**
	 * Load the schema from the given uri.
	 * @param data the uri to load from
	 * @return the schema
	 * @throws NotBoundException if the uri is not bound
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private Schema loadSchema(final Uri data)
			throws NotBoundException, InvalidSchemaException {
		LOG.debug("Loading schema from record: {}", data);
		if (isValidSchemaUri(data)) {
			UriRecord record =
					new UriRecord(data, AvroSchema.RECORD)
					.load(getContentResolver());
			LOG.debug("Record loaded. Converting to schema.");
			return convertRecordToSchema(record);
		}
		return null;
	}

	/**
	 * @param data the uri to check
	 * @return true if the uri is a valid schema uri
	 * @throws InvalidSchemaException if the uri is not valid
	 */
	private boolean isValidSchemaUri(final Uri data) throws InvalidSchemaException {
		List<String>segments = data.getPathSegments();
		try {
			Integer.parseInt(segments.get(segments.size()-1));
			String type = segments.get(segments.size()-2);
			if (type.equals(AvroSchema.RECORD_DEFINITION)) {
				return true;
			}
		} catch (Exception e) {
			throw new InvalidSchemaException();
		}

		return false;
	}

	/**
	 * converts the given record to an avro schema object.
	 * @param record the record to convert
	 * @return the converted schema
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	@SuppressWarnings("unchecked")
	private Schema convertRecordToSchema(final UriRecord record)
			throws InvalidSchemaException {

		boolean nsRequired = ! record.getSchema().getName().equals(
				AvroSchema.SIMPLE_RECORD_DEFINITION);
		LOG.debug("Converting record: {} {}", record.getSchema().getName(),
				nsRequired);

		NamedType typeInfo = getNamedTypeInfo(record, nsRequired);

		if (!nsRequired) {
			typeInfo.namespace = typeInfo.name;
		}

		Schema schema = Schema.createRecord(typeInfo.name,
				typeInfo.doc, typeInfo.namespace, false);
		addAliases(typeInfo, schema);

		LOG.debug("Processing schema fields.");
		List<UriRecord> fields = (List<UriRecord>) record.get("fields");
		List<Schema.Field> schemaFields = new ArrayList<Schema.Field>();
		if (fields != null) {
			for (UriRecord field : fields) {
				List<Field> f = convertToSchemaField(field);
				schemaFields.addAll(f);
			}
			schema.setFields(schemaFields);
		} else {
			ToastOnUI.show(this, getText(R.string.error_no_fields_in_record)
					+ " " + typeInfo.name, Toast.LENGTH_LONG);
			throw new InvalidSchemaException();
		}

		LOG.debug("Built schema: {}", schema);

		// Now we need to make sure that what we built is really valid.
		// I.E. No Namespace problems...
		// This will throw an exception if there is a problem with the schema.
		schema = Schema.parse(schema.toString());

		return schema;
	}

	/**
	 * @param record the record to getinfo on
	 * @return a NamedType with data on the record
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private NamedType getFieldTypeInfo(final UriRecord record)
			throws InvalidSchemaException {
		NamedType typeInfo = getNamedTypeInfo(record, false);
		typeInfo.inList = (Boolean) record.get("list");
		return typeInfo;
	}

	/**
	 *
	 * @param record the record to get data from
	 * @param namespaceRequired true if a namespace is required
	 * @return the NamedType info
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private NamedType getNamedTypeInfo(final UriRecord record,
			final boolean namespaceRequired) throws InvalidSchemaException {
		NamedType typeInfo = new NamedType();
		typeInfo.name = sanitizeName((String) record.get("name"));
		typeInfo.doc = (String) record.get("doc");
		typeInfo.namespace = sanitizeNamespace(
				(String) record.get("namespace"), namespaceRequired);
		if (record.get("label") != null) {
			typeInfo.label = ((String) record.get("label")).replace("\"", "\"\\\"");
		} else {
			typeInfo.label = ((String) record.get("name"))
					.replace("\"", "\"\\\"");
		}
		return typeInfo;
	}

	// TODO: This should be enforced in the UI.
	/**
	 * @param string the namespace
	 * @param required if a namespace is required
	 * @return a sanitized namespace
	 * @throws InvalidSchemaException if the name is required
	 */
	private String sanitizeNamespace(final String string,
			final boolean required) throws InvalidSchemaException {
		String clean = string;
		if (TextUtils.isEmpty(string)) {
			if (required) {
				ToastOnUI.show(this, R.string.error_name_required,
						Toast.LENGTH_LONG);
				throw new InvalidSchemaException();
			}
		} else {
			clean = clean.replace(' ', '.');
			clean = clean.replaceAll("[^A-Za-z0-9_\\.]", "");
		}
		return clean;
	}

	/**
	 * @param string the name to clean
	 * @return a clean version of the name
	 * @throws InvalidSchemaException if the name is invalid
	 */
	private String sanitizeName(final String string)
			throws InvalidSchemaException {
		String clean = string;
		if (TextUtils.isEmpty(string)) {
			ToastOnUI.show(this, R.string.error_name_required,
					Toast.LENGTH_LONG);
			throw new InvalidSchemaException();
		}
		clean = clean.replaceAll("[^A-Za-z0-9_]", "_");
		return clean;
	}

	/**
	 * Sanitizes a collection of names.
	 * @param values the names to clean
	 * @return the cleaned names
	 * @throws InvalidSchemaException if the names are invalid
	 */
	private List<String> sanitizeNames(final List<String> values)
			throws InvalidSchemaException {
		List<String> clean = new ArrayList<String>();
		for (String value : values) {
			clean.add(sanitizeName(value));
		}
		return clean;
	}

	/**
	 * Adds the aliases to given schema.
	 * @param typeInfo the info from which to get aliases
	 * @param schema the schema to which to add the aliases
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private void addAliases(final NamedType typeInfo, final Schema schema)
			throws InvalidSchemaException {
		if (typeInfo.aliases != null) {
			for (String alias : typeInfo.aliases) {
				schema.addAlias(sanitizeName(alias));
			}
		}
	}

	/**
	 * Adds aliases to the given field.
	 * @param typeInfo the info from which to get aliases
	 * @param field the field to add them to
	 */
	private void addAliases(final NamedType typeInfo, final Field field) {
		if (typeInfo.aliases != null) {
			for (String alias : typeInfo.aliases) {
				field.addAlias(alias);
			}
		}
	}

	/**
	 * Converts a field record into a field.
	 * @param field the field to convert
	 * @return the schema field
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private List<Field> convertToSchemaField(final UriRecord field)
			throws InvalidSchemaException {
		LOG.debug("Converting field: {}", field);
		NamedType typeInfo = getFieldTypeInfo(field);

		// TODO: Fix the way we load Enumerations...
		//        String order = (String)field.get("order");
		Object type = field.get("type");
		List<Field> fields;
		if (type instanceof Integer) {
			fields = convertTypeToSchema(typeInfo, (Integer) type);
		} else {
			fields = convertTypeToSchema(typeInfo, (UriRecord) type);
		}

		return fields;
	}

	/**
	 * Converts a Type record to a schema.
	 * @param typeInfo
	 * @param type the type field to convert
	 * @return the converted schema
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private List<Field> convertTypeToSchema(NamedType typeInfo, final Integer type)
			throws InvalidSchemaException {
		LOG.debug("Converting SimpleRecord Field: {}", type);

		ArrayList<Schema.Field> fields = new ArrayList<Schema.Field>();

		Schema schema = null;
		switch (type) {
		// String
		case 0:
			schema = Schema.create(Type.STRING);
			fields.add(buildFieldFromSchema(typeInfo, schema));
			break;
		// Number
		case 1:
			schema = Schema.create(Type.DOUBLE);
			fields.add(buildFieldFromSchema(typeInfo, schema));
			break;
		// Checkbox
		case 2:
			schema = Schema.create(Type.BOOLEAN);
			fields.add(buildFieldFromSchema(typeInfo, schema));
			break;
		// Date
		case 3:
			schema = getDateSchema();
			fields.add(buildFieldFromSchema(typeInfo, schema));
			break;
		// Time
		case 4:
			schema = getTimeSchema();
			fields.add(buildFieldFromSchema(typeInfo, schema));
			break;
		// Photo
		case 5:
			schema = getPhotoSchema();
			fields.add(buildFieldFromSchema(typeInfo, schema));
			break;
		// Location
		case 6:
			fields.addAll(getLocationSchema(typeInfo));
			break;
		default:
			throw new InvalidSchemaException();
		}

		return fields;
	}

	/**
	 * Builds a field from a schema
	 * @param typeInfo the type info for this name
	 * @param schema the schema for the field
	 * @return a Schema.Field representing the schema
	 */
	private Schema.Field buildFieldFromSchema(final NamedType typeInfo,
			final Schema schema) {
		Schema.Field f = new Schema.Field(typeInfo.name, schema,
				typeInfo.doc, null);
		// ,  Schema.Field.Order.valueOf(String.valueOf(order)));
		addAliases(typeInfo, f);
		if (!TextUtils.isEmpty(typeInfo.label)) {
			f.addProp("ui.label", typeInfo.label);
		}
		if (typeInfo.inList) {
			f.addProp("ui.list", "true");
		}
		return f;
	}

	/**
	 * Converts a Record Type field to a schema
	 * @param fieldType the union for the field to convert
	 * @return the schema for the field
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private List<Field> convertTypeToSchema(final NamedType typeInfo,
			final UriRecord fieldType)
			throws InvalidSchemaException {
		LOG.debug("Converting schema field: {}", fieldType);

		UriUnion type = (UriUnion) fieldType.get("type");
		UriRecord typeRecord = (UriRecord) type.getValue();
		String typeName = typeRecord.getSchema().getName();

		Schema schema = null;
		List<Field> fields = new ArrayList<Field>();

		if ("Record".equals(typeName)) {
			schema = convertRecordToSchema(typeRecord);
			fields.add(buildFieldFromSchema(typeInfo, schema));
		} else if ("Enumeration".equals(typeName)) {
			schema = convertEnumerationToSchema(typeRecord);
			fields.add(buildFieldFromSchema(typeInfo, schema));
		} else if ("Fixed".equals(typeName)) {
			schema = convertFixedToSchema(typeRecord);
			fields.add(buildFieldFromSchema(typeInfo, schema));
		} else if ("Array".equals(typeName)) {
			schema = convertArrayToSchema(typeRecord);
			fields.add(buildFieldFromSchema(typeInfo, schema));
		} else if ("Map".equals(typeName)) {
			schema = convertMapToSchema(typeRecord);
			fields.add(buildFieldFromSchema(typeInfo, schema));
		} else if ("Union".equals(typeName)) {
			schema = convertUnionToSchema(typeRecord);
			fields.add(buildFieldFromSchema(typeInfo, schema));
		} else if ("Primitive".equals(typeName)) {
			schema = convertPrimitiveToSchema(typeRecord);
			fields.add(buildFieldFromSchema(typeInfo, schema));
		} else if ("Complex".equals(typeName)) {
			fields.addAll(convertComplexToSchema(typeInfo, typeRecord));
		} else {
			throw new IllegalArgumentException(
					"Unknown type record: " + typeName);
		}

		return fields;
	}

	/**
	 * Converts a complex type to a schema.
	 * @param info the type info for this field
	 * @param typeRecord the field to convert
	 * @return the resulting schema
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private List<Field> convertComplexToSchema(final NamedType typeInfo,
			final UriRecord typeRecord)
			throws InvalidSchemaException {
		LOG.debug("Converting complex type: {}", typeRecord);
		Integer offset = (Integer) typeRecord.get("ComplexType");
		// TODO: Fix the way we load enumerations
		ArrayList<Field> fields = new ArrayList<Field>();
		Schema schema = null;
		switch(offset) {
		// I know the constants here suck but what to do?
		// CHECKSTYLE:OFF
		case 0: // Date
			schema = getDateSchema();
			fields.add(buildFieldFromSchema(typeInfo, schema));
			break;
		case 1: // Time
			schema = getTimeSchema();
			fields.add(buildFieldFromSchema(typeInfo, schema));
			break;
		case 2: // Photo
			schema = getPhotoSchema();
			fields.add(buildFieldFromSchema(typeInfo, schema));
			break;
		case 3: // Location
			fields.addAll(getLocationSchema(typeInfo));
			break;
		default:
			ToastOnUI.show(this, R.string.error_unknown_complex_type,
					Toast.LENGTH_LONG);
			throw new InvalidSchemaException();
		}
		// CHECKSTYLE:ON
		return fields;
	}

	private List<Field> getLocationSchema(NamedType typeInfo) {
		List<Field> fields = new ArrayList<Field>();

		Field f = new Field(typeInfo.name + AvroSchemaProperties.LATITUDE,
				Schema.create(Type.INT), "latitude", null);
		f.addProp("ui.label", "Latitude");
		f.addProp("ui.visible", "false");
		fields.add(f);

		f = new Field(typeInfo.name + AvroSchemaProperties.LONGITUDE,
				Schema.create(Type.INT), "longitude", null);
		f.addProp("ui.label", "Longitude");
		f.addProp("ui.visible", "false");
		fields.add(f);

		f = new Field(typeInfo.name + AvroSchemaProperties.RADIUS_LATITUDE,
				Schema.create(Type.INT), "radius latitude", null);
		f.addProp("ui.label", "Radius Latitude");
		f.addProp("ui.visible", "false");
		fields.add(f);

		f = new Field(typeInfo.name + AvroSchemaProperties.RADIUS_LONGITUDE,
				Schema.create(Type.INT), "radius longitude", null);
		f.addProp("ui.label", "Radius Longitude");
		f.addProp("ui.visible", "false");
		fields.add(f);

		f = new Field(typeInfo.name + AvroSchemaProperties.RADIUS_IN_METERS,
				Schema.create(Type.LONG), "radius in meters", null);
		f.addProp("ui.label", "Radius In Meters");
		f.addProp("ui.visible", "false");
		fields.add(f);

		Schema schema = Schema.create(Type.BYTES);
		schema.addProp("ui.widget", "location");
		f = new Field(typeInfo.name, schema, "map image", null);
		f.addProp("ui.label", typeInfo.label);
		f.addProp("ui.widget", "location");
		if (typeInfo.inList) {
			f.addProp("ui.list", "true");
		}
		fields.add(f);

		f = new Field(typeInfo.name + AvroSchemaProperties.ALTITUDE,
				Schema.create(Type.LONG), "altitude", null);
		f.addProp("ui.label", "Altitude");
		f.addProp("ui.visible", "false");
		fields.add(f);

		f = new Field(typeInfo.name + AvroSchemaProperties.ACCURACY,
				Schema.create(Type.LONG), "accuracy", null);
		f.addProp("ui.label", "Accuracy");
		f.addProp("ui.visible", "false");
		fields.add(f);

		return fields;
	}

	private Schema getPhotoSchema() {
		Schema schema;
		schema = Schema.create(Type.BYTES);
		schema.addProp("ui.widget", "photo");
		return schema;
	}

	private Schema getTimeSchema() {
		Schema schema;
		schema = Schema.create(Type.LONG);
		schema.addProp("ui.widget", "time");
		return schema;
	}

	private Schema getDateSchema() {
		Schema schema;
		schema = Schema.create(Type.LONG);
		schema.addProp("ui.widget", "date");
		return schema;
	}

	/**
	 * Converts a primitive type to a schema.
	 * @param typeRecord the primitive to convert
	 * @return the schema for the field
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private Schema convertPrimitiveToSchema(final UriRecord typeRecord)
			throws InvalidSchemaException {
		LOG.debug("Converting primitive type: {}", typeRecord);
		Integer offset = (Integer) typeRecord.get("PrimitiveType");
		// TODO: Fix the way we load enumerations
		Schema schema = null;
		// I know the constants here suck, but what to do?
		// CHECKSTYLE:OFF
		switch(offset) {
		case 0: // \"String\"
			schema = Schema.create(Type.STRING);
			break;
		case 1: // \"Bytes\",
			schema = Schema.create(Type.BYTES);
			break;
		case 2: // \"Int\",
			schema = Schema.create(Type.INT);
			break;
		case 3: // \"Long\",
			schema = Schema.create(Type.LONG);
			break;
		case 4: // \"Float\",
			schema = Schema.create(Type.FLOAT);
			break;
		case 5: // \"Double\",
			schema = Schema.create(Type.DOUBLE);
			break;
		case 6: // \"Boolean\",
			schema = Schema.create(Type.BOOLEAN);
			break;
		case 7: // \"Null\"
			schema = Schema.create(Type.NULL);
			break;
		default:
			ToastOnUI.show(this, R.string.error_unknown_primitive_type,
					Toast.LENGTH_LONG);
			throw new InvalidSchemaException();
		}
		// CHECKSTYLE:ON
		return schema;
	}

	/**
	 * Converts a union field to a schema.
	 * @param typeRecord the record
	 * @return the union schema
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	@SuppressWarnings("unchecked")
	private Schema convertUnionToSchema(final UriRecord typeRecord)
			throws InvalidSchemaException {
		LOG.debug("Converting union: {}", typeRecord);
		List<UriRecord> branches = (List<UriRecord>) typeRecord.get("branches");
		List<Schema> branchSchemas = new ArrayList<Schema>();
		for (UriRecord type : branches) {
			List<Field> fields = convertTypeToSchema(null, type);
			if (fields.size() > 1) {

			}
			branchSchemas.add(fields.get(0).schema());
		}
		return Schema.createUnion(branchSchemas);
	}

	/**
	 * Converts a fixed field record to a schema.
	 * @param typeRecord the record to convert
	 * @return the fixed schema
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private Schema convertFixedToSchema(final UriRecord typeRecord)
			throws InvalidSchemaException {
		LOG.debug("Converting fixed: {}", typeRecord);
		NamedType typeInfo = getNamedTypeInfo(typeRecord, false);
		Integer size = (Integer) typeRecord.get("size");
		Schema schema = Schema.createFixed(typeInfo.name, typeInfo.doc,
				typeInfo.namespace, size);
		addAliases(typeInfo, schema);

		return schema;
	}

	/**
	 * Converts a record for a map to a schema.
	 * @param typeRecord the record to convert
	 * @return the schema for the map
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private Schema convertMapToSchema(final UriRecord typeRecord)
			throws InvalidSchemaException {
		LOG.debug("Converting map: {}", typeRecord);
		List<Field> fields = convertTypeToSchema(null,
				(UriRecord) typeRecord.get("values"));
		return getSchemaForFields(fields);
	}

	private Schema getSchemaForFields(List<Field> fields) {
		if (fields.size() == 1) {
			return Schema.createMap(fields.get(0).schema());
		}
		return Schema.createRecord(fields);
	}

	/**
	 * Converts an array record to a schema.
	 * @param typeRecord the record to convert
	 * @return the array schema
	 * @throws InvalidSchemaException if the schema is invalid
	 */
	private Schema convertArrayToSchema(final UriRecord typeRecord)
			throws InvalidSchemaException {
		LOG.debug("Converting array: {}", typeRecord);
		List<Field> fields = convertTypeToSchema(null,
				(UriRecord) typeRecord.get("elements"));
		return getSchemaForFields(fields);
	}

	/**
	 * Converts an enumeration to a schema.
	 * @param typeRecord the record to convert
	 * @return the enumeration schema
	 * @throws InvalidSchemaException if the record is invalid
	 */
	@SuppressWarnings("unchecked")
	private Schema convertEnumerationToSchema(final UriRecord typeRecord)
			throws InvalidSchemaException {
		LOG.debug("Converting enum: {}", typeRecord);
		NamedType typeInfo = getNamedTypeInfo(typeRecord, false);
		List<String> values = (List<String>) typeRecord.get("symbols");
		values = sanitizeNames(values);
		Schema schema = Schema.createEnum(typeInfo.name, typeInfo.doc,
				typeInfo.namespace, values);
		addAliases(typeInfo, schema);

		return schema;
	}

	/**
	 * Creates a database from the given schema.
	 * @param schema the schema to create a db from
	 * @return the uri for the new database
	 * @throws IOException if the db could not be created
	 */
	private Uri createDb(final Schema schema) throws IOException {
		if (schema != null) {
			// Register the schema with the provider registry.
			LOG.debug("Initializing database: {}", schema);
			AvroSchemaRegistrationHandler.registerSchema(this, schema);

			// Give back a URI for this database
			Uri uri = EntityUriBuilder.branchUri(Authority.VDB,
					schema.getNamespace(), "master");
			LOG.debug("Uri for {} is {}", schema.getNamespace(), uri);
			return uri;
		} else {
			LOG.error("Unable to init. Schema is null.");
			ToastOnUI.show(this, "Schema is null.", Toast.LENGTH_LONG);
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		return null;
	}
}
