package interdroid.vdb.sm.provider;

import interdroid.vdb.content.ContentChangeHandler;
import interdroid.vdb.content.avro.AvroContentProviderProxy;
import interdroid.vdb.avro.AvroSchema;

import android.content.ContentValues;
import android.content.res.Resources;

public class AvroSchemaProvider extends AvroContentProviderProxy {

	public AvroSchemaProvider() {
		super(AvroSchema.SCHEMA);
	}

	// Register our change handler so that all schema's get a title
	static {
		// TODO: These should come from the defaults in the schema and be generated
		ContentChangeHandler.register(AvroSchema.NAMESPACE,
				AvroSchema.RECORD_DEFINITION, new ContentChangeHandler() {
			public void preInsertHook(ContentValues values) {
				Resources r = Resources.getSystem();
				values.put("name", r.getString(android.R.string.untitled));
			}
		});
	}

}
