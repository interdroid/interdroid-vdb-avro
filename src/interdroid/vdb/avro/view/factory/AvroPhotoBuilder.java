package interdroid.vdb.avro.view.factory;

import interdroid.util.view.ViewUtil;
import interdroid.vdb.R;
import interdroid.vdb.avro.control.handler.CameraHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AbsListView.LayoutParams;

/**
 * A builder for Type.BYTES && widget == "photo".
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroPhotoBuilder extends AvroViewBuilder {

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
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);

		ImageView image = new ImageView(activity);
		layout.addView(image);

		Button cameraButton = new Button(activity);
		cameraButton.setText(activity.getString(R.string.label_take_photo));
		layout.addView(cameraButton);

		// Add to the underlying view group
		ViewUtil.addView(activity, viewGroup, layout);

		// Build the handler
		new CameraHandler(dataModel, activity, schema, valueHandler,
						cameraButton, image);

		return layout;
	}

}
