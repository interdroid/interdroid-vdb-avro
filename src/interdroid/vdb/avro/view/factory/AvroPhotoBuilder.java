/*
 * Copyright (c) 2008-2012 Vrije Universiteit, The Netherlands All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Vrije Universiteit nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package interdroid.vdb.avro.view.factory;

import interdroid.util.DbUtil;
import interdroid.util.view.LayoutUtil.LayoutParameters;
import interdroid.util.view.ViewUtil;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.control.handler.CameraHandler;
import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.view.DataFormatUtil;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * Access to Logger.
	 */
	public static final Logger LOG =
			LoggerFactory.getLogger(AvroPhotoBuilder.class);

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
		LOG.debug("Binding view: {}", field.name());
		ImageView image = (ImageView) view.findViewWithTag(field.name());
		LOG.debug("Columns are: {} {}", cursor.getColumnNames(), cursor.getColumnCount());
		int index = DbUtil.getFieldIndex(cursor, field.name());
		Bitmap bitmap = DataFormatUtil.getBitmap(cursor.getBlob(index),
				AvroViewFactory.MAX_LIST_IMAGE_SIZE);
		image.setImageBitmap(bitmap);
	}

	@Override
	final List<String> getProjectionFields(final Field field) {
		return getFieldNameProjection(field);
	}

}
