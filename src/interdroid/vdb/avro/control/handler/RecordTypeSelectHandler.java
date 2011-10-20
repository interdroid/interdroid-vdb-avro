package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;
import interdroid.vdb.avro.view.AvroBaseEditor;
import interdroid.vdb.avro.view.AvroIntentUtil;
import interdroid.vdb.content.EntityUriBuilder;
import interdroid.vdb.avro.AvroSchema;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Handles selection of a Type.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class RecordTypeSelectHandler implements OnClickListener {
	/** Access to logger. */
    private static final Logger LOG =
    		LoggerFactory.getLogger(RecordTypeSelectHandler.class);

    /** The activity we work for. */
    private final Activity mActivity;
    /** The schema for the type. */
    private final Schema mSchema;
    /** The value handler to get and set data with. */
    private final ValueHandler mValueHandler;

    /**
     * Construct a selection handler.
     * @param activity the activity to work in
     * @param schema the schema for the data
     * @param handler the handler to get and set data with
     */
    public RecordTypeSelectHandler(final Activity activity,
            final Schema schema, final ValueHandler handler) {
        mActivity = activity;
        mSchema = schema;
        mValueHandler = handler;
    }

	@Override
    public final void onClick(final View v) {
        Uri uri;
        if (mValueHandler.getValue() == null) {
            // Do an insert so we can stash away the URI for the record
            uri = Uri.withAppendedPath(
            		EntityUriBuilder.branchUri(mSchema.getNamespace(),
                        AvroSchema.NAMESPACE, "master"), mSchema.getName());
            uri = mActivity.getContentResolver().insert(uri,
            		new ContentValues());
            mValueHandler.setValue(new UriRecord(uri, mSchema));
        } else {
            try {
                uri = ((UriRecord) mValueHandler.getValue()).getInstanceUri();
            } catch (NotBoundException e) {
                LOG.error(
                	"Unable to get record URI because record is not bound.");
                uri = null;
            }
        }
        LOG.debug("Launching edit on URI: {} type: {}",
        		uri, mActivity.getContentResolver().getType(uri));
        Intent editIntent = new Intent(Intent.ACTION_EDIT, uri);
        editIntent.putExtra(AvroBaseEditor.SCHEMA, mSchema.toString());
        AvroIntentUtil.launchEditIntent(mActivity, editIntent);
    }

}
