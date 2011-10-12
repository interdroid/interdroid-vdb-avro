package interdroid.vdb.avro.view.factory;

import interdroid.util.view.ViewUtil;
import interdroid.vdb.R;
import interdroid.vdb.avro.control.handler.RecordTypeSelectHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriRecord;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * A builder which knows how to build Type.RECORD fields.
 * @author nick
 *
 */
class AvroRecordBuilder extends AvroViewBuilder {

	/**
	 * Constructs a builder for Type.RECORD && widget == null.
	 */
	protected AvroRecordBuilder() {
		super(Type.RECORD);
	}

//	/**
//	 * Returns or constructs UriRecord and sets in valueHandler.
//	 * @param activity the activity we are working in
//	 * @param valueHandler the value handler with the data
//	 * @param uri the uri for the record
//	 * @param schema the schema for the record
//	 * @return a UriRecord.
//	 */
//	private static UriRecord getRecord(final Activity activity,
//			final ValueHandler valueHandler, final Uri uri,
//			final Schema schema) {
//
//		UriRecord subRecord = (UriRecord) valueHandler.getValue();
//		if (subRecord == null) {
//			UriMatch match = EntityUriMatcher.getMatch(uri);
//			Uri pathUri = Uri.withAppendedPath(
//					EntityUriBuilder.branchUri(match.authority,
//							match.repositoryName, match.reference),
//							schema.getName());
//			pathUri = activity.getContentResolver().insert(pathUri,
//					new ContentValues());
//			subRecord = new UriRecord(uri, schema);
//			valueHandler.setValue(subRecord);
//		}
//		return subRecord;
//	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {

		final Button button = new Button(activity);
		UriRecord record = (UriRecord) valueHandler.getValue();
		if (record == null) {
			button.setText(
					AvroViewFactory.toTitle(activity,
							R.string.label_create, schema));
		} else {
			button.setText(
					AvroViewFactory.toTitle(activity,
							R.string.label_edit, schema));
		}
		button.setOnClickListener(
				getRecordTypeSelectorHandler(activity, dataModel, schema,
						valueHandler, viewGroup, button));
		ViewUtil.addView(activity, viewGroup, button);
		return button;
	}

	/**
	 * Returns an on click listener for the given data.
	 * @param activity the activity to work in
	 * @param dataModel the data model to get data from
	 * @param schema the schema for the data
	 * @param valueHandler the value handler for the data
	 * @param container the view group
	 * @param button the button to press
	 * @return the on click listener
	 */
	private static OnClickListener getRecordTypeSelectorHandler(
			final Activity activity, final AvroRecordModel dataModel,
			final Schema schema, final ValueHandler valueHandler,
			final ViewGroup container, final Button button) {
		return new RecordTypeSelectHandler(activity, dataModel, schema,
				valueHandler, button);
	}
}
