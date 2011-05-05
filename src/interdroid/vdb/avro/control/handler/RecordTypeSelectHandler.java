package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.view.AvroBaseEditor;
import interdroid.vdb.content.EntityUriBuilder;
import interdroid.vdb.sm.SchemaMakerConstants;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RecordTypeSelectHandler implements OnClickListener {

	private static final String TAG = "RecordTypeSelectHandler";
	private final AvroBaseEditor mActivity;
	private final Schema mSchema;
	private final ValueHandler mValueHandler;
	private final AvroRecordModel mDataModel;
	private final Button mButton;

	public RecordTypeSelectHandler(AvroBaseEditor activity,
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
		Intent editIntent = new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(EntityUriBuilder.branchUri(SchemaMakerConstants.NAMESPACE, "master"), mSchema.getName()));
		editIntent.putExtra(AvroBaseEditor.SCHEMA, mSchema.toString());
		mActivity.launchResultIntent(this, editIntent, AvroBaseEditor.REQUEST_RECORD_SELECTION);
	}

	public void setResult(Intent data) {
		Log.d(TAG, "Got result: " + data.getAction());
		Record record = mDataModel.loadRecordFromUri(Uri.parse(data.getAction()), mSchema);
		mValueHandler.setValue(record);
		mDataModel.storeCurrentValue();
		mButton.setText("Edit: " + mSchema.getName());
	}

}
