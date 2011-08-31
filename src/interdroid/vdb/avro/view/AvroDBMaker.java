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
import interdroid.vdb.content.avro.AvroContentProviderProxy;
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
                this.setResult(RESULT_OK);
                finish();
            } else {
                this.setResult(RESULT_CANCELED);
                finish();
            }
        } catch (Exception e) {
            ToastOnUI.show(this, R.string.error_unknown_loading_and_creating, Toast.LENGTH_LONG);
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
        if (isValidSchemaUri(data)) {
            UriRecord record = new UriRecord(data, AvroSchema.RECORD).load(getContentResolver());
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

        NamedType typeInfo = getNamedTypeInfo(record);
        Schema schema = Schema.createRecord(typeInfo.name, typeInfo.doc, typeInfo.namespace, false);
        addAliases(typeInfo, schema);

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

    private NamedType getNamedTypeInfo(UriRecord record) {
        NamedType typeInfo = new NamedType();
        typeInfo.name = (String)record.get("name");
        if (TextUtils.isEmpty(typeInfo.name)) {
            ToastOnUI.show(this, R.string.error_name_required, Toast.LENGTH_LONG);
        }
        typeInfo.doc = (String)record.get("doc");
        typeInfo.namespace = (String)record.get("namespace");
        if (TextUtils.isEmpty(typeInfo.namespace)) {
            ToastOnUI.show(this, R.string.error_namespace_required, Toast.LENGTH_LONG);
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

    @SuppressWarnings("unchecked")
    private Schema.Field convertToSchemaField(UriRecord field) throws InvalidSchemaException {
        NamedType typeInfo = getNamedTypeInfo(field);

        // TODO: Fix the way we load Enumerations...
        //        String order = (String)field.get("order");

        Schema fieldSchema = convertTypeToSchema((UriRecord)field.get("type"));
        Schema.Field f = new Schema.Field(typeInfo.name, fieldSchema, typeInfo.doc, null);// ,  Schema.Field.Order.valueOf(String.valueOf(order)));
        addAliases(typeInfo, f);
        return f;
    }


    private Schema convertTypeToSchema(UriRecord fieldType) throws InvalidSchemaException {
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
        Integer offset = (Integer) typeRecord.get("PrimitiveType");
        // TODO: Fix the way we load enumerations
        Schema schema = null;
        switch(offset) {
        case 0: // \"String\"
            schema = Schema.create(Type.STRING);
            break;
        case 1: // \"Bytes\",
            schema = Schema.create(Type.BYTES);
        case 2: // \"Int\",
            schema = Schema.create(Type.INT);
        case 3: // \"Long\",
            schema = Schema.create(Type.LONG);
        case 4: // \"Float\",
            schema = Schema.create(Type.FLOAT);
        case 5: // \"Double\",
            schema = Schema.create(Type.DOUBLE);
        case 6: // \"Boolean\",
            schema = Schema.create(Type.BOOLEAN);
        case 7: // \"Null\"
            schema = Schema.create(Type.NULL);
        default:
            ToastOnUI.show(this, R.string.error_unknown_primitive_type, Toast.LENGTH_LONG);
            throw new InvalidSchemaException();
        }
        return schema;
    }

    @SuppressWarnings("unchecked")
    private Schema convertUnionToSchema(UriRecord typeRecord) throws InvalidSchemaException {
        List<UriRecord> branches = (List<UriRecord>) typeRecord.get("branches");
        List<Schema> branchSchemas = new ArrayList<Schema>();
        for (UriRecord type: branches) {
            branchSchemas.add(convertTypeToSchema(type));
        }
        return Schema.createUnion(branchSchemas);
    }

    @SuppressWarnings("unchecked")
    private Schema convertFixedToSchema(UriRecord typeRecord) {
        NamedType typeInfo = getNamedTypeInfo(typeRecord);
        Integer size = (Integer)typeRecord.get("size");
        Schema schema = Schema.createFixed(typeInfo.name, typeInfo.doc, typeInfo.namespace, size);
        addAliases(typeInfo, schema);

        return schema;
    }

    private Schema convertMapToSchema(UriRecord typeRecord) throws InvalidSchemaException {
        Schema valueType = convertTypeToSchema((UriRecord)typeRecord.get("values"));
        return Schema.createMap(valueType);
    }

    private Schema convertArrayToSchema(UriRecord typeRecord) throws InvalidSchemaException {
        Schema elementType = convertTypeToSchema((UriRecord)typeRecord.get("elements"));
        return Schema.createArray(elementType);
    }

    private Schema convertEnumerationToSchema(UriRecord typeRecord) {
        NamedType typeInfo = getNamedTypeInfo(typeRecord);
        Integer size = (Integer)typeRecord.get("size");
        List<String> values = (List<String>) typeRecord.get("symbols");
        Schema schema = Schema.createEnum(typeInfo.name, typeInfo.doc, typeInfo.namespace, values);
        addAliases(typeInfo, schema);

        return schema;
    }

    private Uri createDb(Schema schema) {
        if (schema != null) {
            // Build a43774 provider proxy for the first time which will register this schema
            logger.debug("Initializing database: {}", schema);
            AvroProviderRegistry.registerSchema(this, schema);

            // Give back a URI for this database
            logger.debug("Getting uri for: {}", schema.getNamespace());
            return new Uri.Builder().scheme("content").authority(schema.getNamespace()).appendPath("/branches/master").build();
        }
        return null;
    }
}
