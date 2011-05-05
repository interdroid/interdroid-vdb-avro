package interdroid.vdb.avro.provider;

import interdroid.vdb.content.ContentChangeHandler;
import interdroid.vdb.content.avro.AvroContentProvider;
import interdroid.vdb.avro.AvroSchema;

import android.content.ContentValues;
import android.content.res.Resources;


public class AvroSchemaProvider extends AvroContentProvider {

	public AvroSchemaProvider() {
		super(AvroSchema.SCHEMA);
	}

	// Register our change handler
	static {
		// TODO: Extract constants
		ContentChangeHandler.register(AvroSchema.NAMESPACE + ".Record", new ContentChangeHandler() {
			public void preInsertHook(ContentValues values) {
	            Resources r = Resources.getSystem();
				values.put("name", r.getString(android.R.string.untitled));
			}
		});
	}

}
