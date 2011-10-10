package interdroid.vdb.avro.view.factory;

import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.control.handler.CheckboxHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;

import android.app.Activity;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.AbsListView.LayoutParams;

/**
 * A builder for Type.BOOLEAN.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
class AvroBooleanBuilder extends AvroViewBuilder {

	/**
	 * A builder for Type.BOOLEAN && widget == null.
	 */
	AvroBooleanBuilder() {
		super(Type.BOOLEAN);
	}

	@Override
	public final View buildEditView(final Activity activity,
			final AvroRecordModel dataModel,
			final ViewGroup viewGroup, final Schema schema,
			final String field, final Uri uri,
			final ValueHandler valueHandler) throws NotBoundException {

		// Build the view
		CheckBox text = new CheckBox(activity);
		text.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		text.setGravity(Gravity.FILL_HORIZONTAL);

		// Build the handler
		new CheckboxHandler(dataModel, valueHandler, text);

		ViewUtil.addView(activity, viewGroup, text);

		return text;
	}

}
