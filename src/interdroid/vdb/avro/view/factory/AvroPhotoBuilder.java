package interdroid.vdb.avro.view.factory;

import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.util.view.ViewUtil;
import interdroid.vdb.R;
import interdroid.vdb.avro.control.handler.CameraHandler;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.view.DataFormatUtil;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * A builder for Type.BYTES && widget == "photo".
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroPhotoBuilder extends AvroTypedViewBuilder {

	/**
	 * Construct a builder for photos.
	 */
	protected AvroPhotoBuilder() {
		super(Type.BYTES, "photo");
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel, final ViewGroup viewGroup,
			final Schema schema, final Field field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {

		// Build the view
		LinearLayout layout = new LinearLayout(activity);
		LayoutParameters.setViewGroupLayoutParams(
				LayoutParameters.W_FILL_H_WRAP, layout);
		layout.setOrientation(LinearLayout.VERTICAL);

		ImageView image = new ImageView(activity);
		layout.addView(image);

		Button cameraButton = new Button(activity);
		cameraButton.setText(activity.getString(R.string.label_take_photo));
		layout.addView(cameraButton);

		// Add to the underlying view group
		ViewUtil.addView(activity, viewGroup, layout);

		// Build the handler
		new CameraHandler(dataModel, activity, valueHandler, cameraButton,
						image);

		return layout;
	}

	@Override
	final View buildListView(final Context context, final Field field) {
		ImageView image = new ImageView(context);
		image.setTag(field.name());
		return image;
	}

	@Override
	final void bindListView(final View view, final Cursor cursor,
			final Field field) {
		ImageView image = (ImageView) view.findViewWithTag(field.name());
		int index = cursor.getColumnIndex(field.name());
		Bitmap bitmap = DataFormatUtil.getBitmap(cursor.getBlob(index),
				AvroViewFactory.MAX_LIST_IMAGE_SIZE);
		image.setImageBitmap(bitmap);
	}

	@Override
	final List<String> getProjectionFields(final Field field) {
		return getFieldNameProjection(field);
	}

}
