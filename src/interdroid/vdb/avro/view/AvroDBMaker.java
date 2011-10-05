package interdroid.vdb.avro.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interdroid.util.ToastOnUI;
import interdroid.util.view.AsyncTaskWithProgressDialog;
import interdroid.vdb.Authority;
import interdroid.vdb.avro.AvroSchema;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;
import interdroid.vdb.avro.model.UriUnion;
import interdroid.vdb.content.EntityUriBuilder;
import interdroid.vdb.content.avro.AvroProviderRegistry;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

public class AvroDBMaker extends Activity {
	private static final Logger logger = LoggerFactory
			.getLogger(AvroDBMaker.class);

	private static class InvalidSchemaException extends Exception {

		/**
		 * Make the compiler happy.
		 */
		private static final long serialVersionUID = 1L;

	}

	private static class NamedType {
		String name;
		String namespace;
		String doc;
		List<String> aliases;
		public String label;
		public boolean in_list;
	}

	public AvroDBMaker() {
		logger.debug("Constructed AvroDBMaker: " + this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private class LoadTask extends AsyncTaskWithProgressDialog<Void, Void, Uri> {

		public LoadTask() {
			super(AvroDBMaker.this, getString(R.string.label_loading), getString(R.string.label_wait));
		}

		@Override
		protected Uri doInBackground(Void... params) {
			Schema schema = getSchema();
			// Now we need to create the database
			Uri uri = null;
			try {
				uri =createDb(schema);
			} catch (IOException e) {
				logger.error("Error registering schema!", e);
			}
			return uri;
		}
	}

	protected void onStart() {
		super.onStart();
		processSchema();
	}

	private void processSchema() {
		AsyncTask<Void, Void, Uri> load = new LoadTask().execute();
		try {
			Uri dbUri = load.get();
			if (dbUri != null) {
				logger.debug("DB Created!");
				Intent i = new Intent(Intent.ACTION_VIEW, dbUri);
				logger.debug("Finishing this activity.");
				this.setResult(RESULT_OK, i);
				finish();
			} else {
				throw new InvalidSchemaException();
			}
		} catch (Throwable e) {
			logger.error("Unknown error.", e);
			ToastOnUI.show(this, R.string.error_unknown_loading_and_creating, Toast.LENGTH_LONG);
			logger.error("Schema was invalid. Launching Edit.");
			ToastOnUI.show(this, e.getMessage(), Toast.LENGTH_LONG);
			startActivityForResult(getEditIntent(), 0);
		}
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		logger.error("Got activity result: {} {} " + data, requestCode, resultCode);
		if (resultCode == RESULT_OK) {
			logger.error("Edit activity said okay, attempting to process again.");
			processSchema();
		} else {
			logger.error("Edit activity failed to return okay result. Finishing.");
			this.setResult(resultCode);
			finish();
		}
	}

	private Intent getEditIntent() {
		Intent request = getIntent();
		Intent editIntent;
		if (request.getData() == null) {
			editIntent = new Intent(Intent.ACTION_EDIT);
			editIntent.putExtra(AvroBaseEditor.SCHEMA, request.getStringExtra(AvroBaseEditor.SCHEMA));
		} else {
			editIntent = new Intent(Intent.ACTION_EDIT, request.getData());
		}
		editIntent.setClassName(this, AvroBaseEditor.class.getName());
		logger.debug("Editing with: {}", editIntent);
		return editIntent;
	}

	private Schema getSchema() {
		Intent intent = getIntent();
		Schema schema = null;
		if (intent.getData() == null) {

			String schemaJson = intent.getStringExtra(AvroBaseEditor.SCHEMA);
			if (schemaJson == null) {
				ToastOnUI.show(AvroDBMaker.this, R.string.error_no_schema, Toast.LENGTH_LONG);
				return null;
			} else {
				try {
					schema = Schema.parse(schemaJson);
				} catch (Exception e) {
					ToastOnUI.show(AvroDBMaker.this, R.string.error_parsing_schema, Toast.LENGTH_LONG);
				}
			}
		} else {
			try {
				schema = loadSchema(intent.getData());
			} catch (NotBoundException e) {
				ToastOnUI.show(AvroDBMaker.this, R.string.error_parsing_schema, Toast.LENGTH_LONG);
			} catch (Exception e) {
				logger.error("Schema Invalid!", e);
			}
		}

		return schema;
	}

	private Schema loadSchema(Uri data) throws NotBoundException, InvalidSchemaException {
		logger.debug("Loading schema from record: {}", data);
		if (isValidSchemaUri(data)) {
			UriRecord record = new UriRecord(data, AvroSchema.RECORD).load(getContentResolver());
			logger.debug("Record loaded. Converting to schema.");
			return convertRecordToSchema(record);
		}
		return null;
	}

	private boolean isValidSchemaUri(Uri data) {
		// TODO: What does a valid URI look like?
		return true;
	}

	@SuppressWarnings("unchecked")
	private Schema convertRecordToSchema(UriRecord record) throws InvalidSchemaException {

		NamedType typeInfo = getNamedTypeInfo(record, true);
		Schema schema = Schema.createRecord(typeInfo.name, typeInfo.doc, typeInfo.namespace, false);
		addAliases(typeInfo, schema);
		logger.debug("Processing schema fields.");
		List<UriRecord> fields = (List<UriRecord>)record.get("fields");
		List<Schema.Field> schemaFields = new ArrayList<Schema.Field>();
		if (fields != null) {
			for(UriRecord field: fields) {
				Schema.Field f = convertToSchemaField(field);
				schemaFields.add(f);
			}
			schema.setFields(schemaFields);
		} else {
			ToastOnUI.show(this, getText(R.string.error_no_fields_in_record) + " " + typeInfo.name, Toast.LENGTH_LONG);
			throw new InvalidSchemaException();
		}

		// Now we need to make sure that what we built is really valid. I.E. No Namespace problems...
		// This will throw an exception if there is a problem reparsing the schema.
		schema = Schema.parse(schema.toString());

		return schema;
	}

	private NamedType getFieldTypeInfo(UriRecord record) throws InvalidSchemaException {
		NamedType typeInfo = getNamedTypeInfo(record, false);
		typeInfo.in_list = (Boolean)record.get("list");
		return typeInfo;
	}
	private NamedType getNamedTypeInfo(UriRecord record, boolean namespaceRequired) throws InvalidSchemaException {
		NamedType typeInfo = new NamedType();
		typeInfo.name = sanitizeName((String)record.get("name"));
		typeInfo.doc = (String)record.get("doc");
		typeInfo.namespace = sanitizeNamespace((String)record.get("namespace"), namespaceRequired);
		typeInfo.label = (String)record.get("label");
		return typeInfo;
	}

	// TODO: This should be enforced in the UI.
	private String sanitizeNamespace(String string, boolean required) throws InvalidSchemaException {
		if (TextUtils.isEmpty(string)) {
			if (required) {
				ToastOnUI.show(this, R.string.error_name_required, Toast.LENGTH_LONG);
				throw new InvalidSchemaException();
			}
		} else {
			string = string.replace(' ', '.');
			string = string.replaceAll("/[^A-Za-z0-9_\\.]/", "");
		}
		return string;
	}

	private String sanitizeName(String string) throws InvalidSchemaException {
		if (TextUtils.isEmpty(string)) {
			ToastOnUI.show(this, R.string.error_name_required, Toast.LENGTH_LONG);
			throw new InvalidSchemaException();
		}
		string = string.replace(' ', '_');
		string = string.replaceAll("/[^A-Za-z0-9_]/", "");
		return string;
	}

	private List<String> sanitizeNames(List<String> values) throws InvalidSchemaException {
		List<String> clean = new ArrayList<String>();
		for (String value : values) {
			clean.add(sanitizeName(value));
		}
		return clean;
	}

	private void addAliases(NamedType typeInfo, Schema schema) throws InvalidSchemaException {
		if (typeInfo.aliases != null) {
			for (String alias : typeInfo.aliases) {
				schema.addAlias(sanitizeName(alias));
			}
		}
	}

	private void addAliases(NamedType typeInfo, Field field) {
		if (typeInfo.aliases != null) {
			for (String alias : typeInfo.aliases) {
				field.addAlias(alias);
			}
		}
	}

	private Schema.Field convertToSchemaField(UriRecord field) throws InvalidSchemaException {
		logger.debug("Converting field: {}", field);
		NamedType typeInfo = getFieldTypeInfo(field);

		// TODO: Fix the way we load Enumerations...
		//        String order = (String)field.get("order");

		Schema fieldSchema = convertTypeToSchema((UriRecord)field.get("type"));
		Schema.Field f = new Schema.Field(typeInfo.name, fieldSchema, typeInfo.doc, null);// ,  Schema.Field.Order.valueOf(String.valueOf(order)));
		addAliases(typeInfo, f);
		if (!TextUtils.isEmpty(typeInfo.label)) {
			f.addProp("ui.label", typeInfo.label);
		}
		if (typeInfo.in_list) {
			f.addProp("ui.list", "true");
		}
		return f;
	}


	private Schema convertTypeToSchema(UriRecord fieldType) throws InvalidSchemaException {
		logger.debug("Converting field type: {}", fieldType);
		UriUnion type = (UriUnion)fieldType.get("type");
		UriRecord typeRecord = (UriRecord) type.getValue();
		String typeName = typeRecord.getSchema().getName();

		Schema schema = null;
		if ("Record".equals(typeName)) {
			schema = convertRecordToSchema(typeRecord);
		} else if ("Enumeration".equals(typeName)) {
			schema = convertEnumerationToSchema(typeRecord);
		} else if ("Fixed".equals(typeName)) {
			schema = convertFixedToSchema(typeRecord);
		} else if ("Array".equals(typeName)) {
			schema = convertArrayToSchema(typeRecord);
		} else if ("Map".equals(typeName)) {
			schema = convertMapToSchema(typeRecord);
		} else if ("Union".equals(typeName)) {
			schema = convertUnionToSchema(typeRecord);
		} else if ("Primitive".equals(typeName)) {
			schema = convertPrimitiveToSchema(typeRecord);
		} else if ("Complex".equals(typeName)) {
			schema = convertComplexToSchema(typeRecord);
		} else {
			throw new RuntimeException("Unknown type record: " + typeName);
		}

		return schema;
	}

	private Schema convertComplexToSchema(UriRecord typeRecord) throws InvalidSchemaException {
		logger.debug("Converting complex type: {}", typeRecord);
		Integer offset = (Integer) typeRecord.get("ComplexType");
		// TODO: Fix the way we load enumerations
		Schema schema = null;
		switch(offset) {
		case 0: // Date
			schema = Schema.create(Type.LONG);
			schema.addProp("ui.widget", "date");
			break;
		case 1: // Time
			schema = Schema.create(Type.LONG);
			schema.addProp("ui.widget", "time");
			break;
		case 2: // Photo
			schema = Schema.create(Type.BYTES);
			schema.addProp("ui.widget", "photo");
			break;
		case 3: // Location
			List<Field> fields = new ArrayList<Field>();

			Field f = new Field("Latitude", Schema.create(Type.INT), "latitude", null);
			f.addProp("ui.label", "Latitude");
			fields.add(f);

			f = new Field("Longitude", Schema.create(Type.INT), "longitude", null);
			f.addProp("ui.label", "Longitude");
			fields.add(f);

			f = new Field("RadiusLatitude", Schema.create(Type.INT), "radius latitude", null);
			f.addProp("ui.label", "Radius Latitude");
			fields.add(f);

			f = new Field("RadiusLongitude", Schema.create(Type.INT), "radius longitude", null);
			f.addProp("ui.label", "Radius Longitude");
			fields.add(f);

			f = new Field("RadiusInMeters", Schema.create(Type.LONG), "radius in meters", null);
			f.addProp("ui.label", "Radius In Meters");
			fields.add(f);

			f = new Field("MapImage", Schema.create(Type.BYTES), "map image", null);
			f.addProp("ui.label", "Map Image");
			fields.add(f);

			f = new Field("Altitude", Schema.create(Type.LONG), "altitude", null);
			f.addProp("ui.label", "Altitude");
			fields.add(f);

			f = new Field("Accuracy", Schema.create(Type.LONG), "accuracy", null);
			f.addProp("ui.label", "Accuracy");
			fields.add(f);

			schema = Schema.createRecord("Location", "A Location", null, false);
			schema.setFields(fields);
			schema.addProp("ui.widget", "location");
			break;
		default:
			ToastOnUI.show(this, R.string.error_unknown_complex_type, Toast.LENGTH_LONG);
			throw new InvalidSchemaException();
		}
		return schema;
	}

	private Schema convertPrimitiveToSchema(UriRecord typeRecord) throws InvalidSchemaException {
		logger.debug("Converting primitive type: {}", typeRecord);
		Integer offset = (Integer) typeRecord.get("PrimitiveType");
		// TODO: Fix the way we load enumerations
		Schema schema = null;
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
			ToastOnUI.show(this, R.string.error_unknown_primitive_type, Toast.LENGTH_LONG);
			throw new InvalidSchemaException();
		}
		return schema;
	}

	@SuppressWarnings("unchecked")
	private Schema convertUnionToSchema(UriRecord typeRecord) throws InvalidSchemaException {
		logger.debug("Converting union: {}", typeRecord);
		List<UriRecord> branches = (List<UriRecord>) typeRecord.get("branches");
		List<Schema> branchSchemas = new ArrayList<Schema>();
		for (UriRecord type: branches) {
			branchSchemas.add(convertTypeToSchema(type));
		}
		return Schema.createUnion(branchSchemas);
	}

	private Schema convertFixedToSchema(UriRecord typeRecord) throws InvalidSchemaException {
		logger.debug("Converting fixed: {}", typeRecord);
		NamedType typeInfo = getNamedTypeInfo(typeRecord, false);
		Integer size = (Integer)typeRecord.get("size");
		Schema schema = Schema.createFixed(typeInfo.name, typeInfo.doc, typeInfo.namespace, size);
		addAliases(typeInfo, schema);

		return schema;
	}

	private Schema convertMapToSchema(UriRecord typeRecord) throws InvalidSchemaException {
		logger.debug("Converting map: {}", typeRecord);
		Schema valueType = convertTypeToSchema((UriRecord)typeRecord.get("values"));
		return Schema.createMap(valueType);
	}

	private Schema convertArrayToSchema(UriRecord typeRecord) throws InvalidSchemaException {
		logger.debug("Converting array: {}", typeRecord);
		Schema elementType = convertTypeToSchema((UriRecord)typeRecord.get("elements"));
		return Schema.createArray(elementType);
	}

	@SuppressWarnings("unchecked")
	private Schema convertEnumerationToSchema(UriRecord typeRecord) throws InvalidSchemaException {
		logger.debug("Converting enum: {}", typeRecord);
		NamedType typeInfo = getNamedTypeInfo(typeRecord, false);
		List<String> values = (List<String>) typeRecord.get("symbols");
		values = sanitizeNames(values);
		Schema schema = Schema.createEnum(typeInfo.name, typeInfo.doc, typeInfo.namespace, values);
		addAliases(typeInfo, schema);

		return schema;
	}

	private Uri createDb(Schema schema) throws IOException {
		if (schema != null) {
			// Register the schema with the provider registry.
			logger.debug("Initializing database: {}", schema);
			AvroProviderRegistry.registerSchema(this, schema);

			// Give back a URI for this database
			Uri uri = EntityUriBuilder.branchUri(Authority.VDB, schema.getNamespace(), "master");
			logger.debug("Uri for {} is {}", schema.getNamespace(), uri);
			return uri;
		} else {
			logger.error("Unable to init. Schema is null.");
		}
		return null;
	}
}
