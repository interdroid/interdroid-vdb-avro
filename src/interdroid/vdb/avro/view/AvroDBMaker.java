package interdroid.vdb.avro.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interdroid.util.ToastOnUI;
import interdroid.util.view.AsyncTaskWithProgressDialog;
import interdroid.vdb.avro.AvroSchema;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;
import interdroid.vdb.avro.model.UriUnion;
import interdroid.vdb.content.EntityUriBuilder;
import interdroid.vdb.content.VdbMainContentProvider;
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
            return createDb(schema);
        }
    }

    protected void onStart() {
        super.onStart();
        AsyncTask<Void, Void, Uri> load = new LoadTask().execute();
        try {
            Uri dbUri = load.get();
            if (dbUri != null) {
                logger.debug("Launching new DB!");
                Intent i = new Intent(Intent.ACTION_VIEW, dbUri);
                startActivity(i);
                logger.debug("Finishing this activity.");
                this.setResult(RESULT_OK);
                finish();
            } else {
                logger.error("Schema was invalid.");
                throw new InvalidSchemaException();
            }
        } catch (Throwable e) {
            ToastOnUI.show(this, R.string.error_unknown_loading_and_creating, Toast.LENGTH_LONG);
            this.setResult(RESULT_CANCELED);
            finish();
        }
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
            } catch (InvalidSchemaException e) {
                // Already toasted about the problem.
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

        return schema;
    }

    private NamedType getNamedTypeInfo(UriRecord record, boolean namespaceRequired) throws InvalidSchemaException {
        NamedType typeInfo = new NamedType();
        typeInfo.name = (String)record.get("name");
        if (TextUtils.isEmpty(typeInfo.name)) {
            ToastOnUI.show(this, R.string.error_name_required, Toast.LENGTH_LONG);
            throw new InvalidSchemaException();
        }
        typeInfo.doc = (String)record.get("doc");
        typeInfo.namespace = (String)record.get("namespace");
        if (namespaceRequired && TextUtils.isEmpty(typeInfo.namespace)) {
            ToastOnUI.show(this, R.string.error_namespace_required, Toast.LENGTH_LONG);
            throw new InvalidSchemaException();
        }
        return typeInfo;
    }

    private void addAliases(NamedType typeInfo, Schema schema) {
        if (typeInfo.aliases != null) {
            for (String alias : typeInfo.aliases) {
                schema.addAlias(alias);
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
        NamedType typeInfo = getNamedTypeInfo(field, false);

        // TODO: Fix the way we load Enumerations...
        //        String order = (String)field.get("order");

        Schema fieldSchema = convertTypeToSchema((UriRecord)field.get("type"));
        Schema.Field f = new Schema.Field(typeInfo.name, fieldSchema, typeInfo.doc, null);// ,  Schema.Field.Order.valueOf(String.valueOf(order)));
        addAliases(typeInfo, f);
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
        } else {
            throw new RuntimeException("Unknown type record: " + typeName);
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
        Schema schema = Schema.createEnum(typeInfo.name, typeInfo.doc, typeInfo.namespace, values);
        addAliases(typeInfo, schema);

        return schema;
    }

    private Uri createDb(Schema schema) {
        if (schema != null) {
            // Register the schema with the provider registry.
            logger.debug("Initializing database: {}", schema);
            AvroProviderRegistry.registerSchema(this, schema);

            // Give back a URI for this database
            Uri uri = EntityUriBuilder.branchUri(VdbMainContentProvider.AUTHORITY, schema.getNamespace(), "master");
            logger.debug("Uri for {} is {}", schema.getNamespace(), uri);
            return uri;
        } else {
            logger.error("Unable to init. Schema is null.");
        }
        return null;
    }
}
