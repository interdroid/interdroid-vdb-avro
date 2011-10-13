package interdroid.vdb.avro.view.factory;

import java.util.List;

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
import android.content.Context;
import android.database.Cursor;
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
class AvroRecordBuilder extends AvroTypedViewBuilder {

	/**
	 * Constructs a builder for Type.RECORD && widget == null.
	 */
	protected AvroRecordBuilder() {
		super(Type.RECORD);
	}

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

	@Override
	final View buildListView(final Context context, final Field field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		// TODO Auto-generated method stub

	}

	@Override
	final List<String> getProjectionFields(final Field field) {
		// TODO Auto-generated method stub
		return null;
	}
}
