package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;
import interdroid.vdb.avro.view.AvroBaseEditor;
import interdroid.vdb.avro.view.AvroIntentUtil;
import interdroid.vdb.content.EntityUriBuilder;
import interdroid.vdb.avro.AvroSchema;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RecordTypeSelectHandler implements OnClickListener {
    private static final Logger logger = LoggerFactory.getLogger(RecordTypeSelectHandler.class);

    private final Activity mActivity;
    private final Schema mSchema;
    private final ValueHandler mValueHandler;
    private final AvroRecordModel mDataModel;
    private final Button mButton;

    public RecordTypeSelectHandler(Activity activity,
            AvroRecordModel dataModel, Schema schema, ValueHandler handler,
            Button button) {
        mActivity = activity;
        mSchema = schema;
        mValueHandler = handler;
        mDataModel = dataModel;
        mButton = button;
    }

	@Override
    public void onClick(View v) {
        Uri uri;
        if (mValueHandler.getValue() == null) {
            // Do an insert so we can stash away the URI for the record
            uri = Uri.withAppendedPath(EntityUriBuilder.branchUri(mSchema.getNamespace(),
                        AvroSchema.NAMESPACE, "master"), mSchema.getName());
            uri = mActivity.getContentResolver().insert(uri, new ContentValues());
            mValueHandler.setValue(new UriRecord(uri, mSchema));
        } else {
            try {
                uri = ((UriRecord) mValueHandler.getValue()).getInstanceUri();
            } catch (NotBoundException e) {
                logger.error("Unable to get record URI because record is not bound.");
                uri = null;
            }
        }
        logger.debug("Launching edit on URI: {} type: {}", uri, mActivity.getContentResolver().getType(uri));
        Intent editIntent = new Intent(Intent.ACTION_EDIT, uri);
        editIntent.putExtra(AvroBaseEditor.SCHEMA, mSchema.toString());
        AvroIntentUtil.launchEditIntent(mActivity, editIntent);
    }

}
