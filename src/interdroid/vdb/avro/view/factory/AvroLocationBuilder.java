package interdroid.vdb.avro.view.factory;

import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.util.view.ViewUtil;
import interdroid.vdb.R;
import interdroid.vdb.avro.control.handler.LocationHandler;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * A builder for Type.RECORD && widget == "location".
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroLocationBuilder extends AvroTypedViewBuilder {

	/**
	 * Construct a location builder.
	 */
	protected AvroLocationBuilder() {
		super(Type.RECORD, "location");
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel,
			final ViewGroup viewGroup, final Schema schema,
			final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {

		// Build the view
		LinearLayout layout = new LinearLayout(activity);
		LayoutParameters.setLinearLayoutParams(
				LayoutParameters.W_FILL_H_WRAP, layout);
		layout.setOrientation(LinearLayout.VERTICAL);

		ImageView image = new ImageView(activity);
		layout.addView(image);

		Button cameraButton = new Button(activity);

		cameraButton.setText(activity.getString(R.string.label_pick_location));
		layout.addView(cameraButton);

		// Add it to the viewGroup
		ViewUtil.addView(activity, viewGroup, layout);

		// Construct a handler
		new LocationHandler(activity, schema, valueHandler, cameraButton,
				image);

		return layout;
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
