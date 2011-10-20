package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.control.handler.value.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.model.UriBoundAdapter;
import interdroid.vdb.avro.model.UriUnion;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

/**
 * A handler for union values.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class UnionHandler implements OnCheckedChangeListener {
	/** Access to logger. */
	private static final Logger LOG = LoggerFactory
			.getLogger(UnionHandler.class);

	/** The data model we work for. */
	private final AvroRecordModel mDataModel;
	/** The map from radio buttons to the schema for the union branches. */
	private final Map<RadioButton, Schema> mSchema =
			new HashMap<RadioButton, Schema>();
	/** The value handler to get and set data with. */
	private final ValueHandler mValueHandler;
	/** The map from the radio button to the views. */
	private final Map<RadioButton, View> mViews =
			new HashMap<RadioButton, View>();
	/** The map from the radio button to the inner value handlers. */
	private final Map<RadioButton, ValueHandler>mHandlers =
			new HashMap<RadioButton, ValueHandler>();
	/** The union we are managing. */
	private final UriUnion mUnion;

	/**
	 * Construct a UnionHandler.
	 * @param dataModel the data model to work in
	 * @param valueHandler the valueHandler to get and set data with
	 * @param union the union we are managing
	 */
	public UnionHandler(final AvroRecordModel dataModel,
			final ValueHandler valueHandler, final UriUnion union) {
		LOG.debug("UnionHanlder built for: {}", union);
		mDataModel = dataModel;
		mValueHandler = valueHandler;
		mUnion = union;
		mValueHandler.setValue(mUnion);
	}

	/**
	 * Adds a branch to be managed.
	 * @param radioButton the radio button for this branch
	 * @param innerType the type for this branch
	 * @param view the view for this branch
	 */
	public final void addType(final RadioButton radioButton,
			final Schema innerType, final View view) {
		mSchema.put(radioButton, innerType);
		mViews.put(radioButton, view);
		view.setEnabled(false);
		radioButton.setOnCheckedChangeListener(this);
		if (mUnion == null) {
			if (innerType.getType() == Schema.Type.NULL) {
				LOG.debug(
				"Checking radio since inner union is null and type is NULL");
				radioButton.setChecked(true);
			} else {
				LOG.debug("Unchecking radio since inner union is null.");
				radioButton.setChecked(false);
			}
		} else {
			LOG.debug("Checking if type matches: {} {}",
					innerType.getType(), mUnion.getType());
			if (isMatchingType(innerType)) {
				LOG.debug("Type match. Enabling.");
				radioButton.setChecked(true);
				view.setEnabled(true);
			}
		}
	}

	/**
	 * @param innerType the type to check
	 * @return true if the union type matches.
	 */
	private boolean isMatchingType(final Schema innerType) {
		LOG.debug("Checking for type match: {} {}",
				innerType.getType(), mUnion.getType());
		if (innerType.getType().equals(mUnion.getType())) {
			LOG.debug("Checking if type is named.");
			if (UriBoundAdapter.isNamedType(mUnion.getType())) {
				LOG.debug("Checking if short names match: {} {}",
						innerType.getName(), mUnion.getTypeName());
				LOG.debug("Checking if long names match: {} {}",
						innerType.getFullName(), mUnion.getTypeName());
				if (// Is named and Both names are null
					((innerType.getName() == null
					&& mUnion.getTypeName() == null)
					// or Is Named and The names match
					|| (innerType.getName() != null
						&& innerType.getName().equals(mUnion.getTypeName()))
					|| (innerType.getFullName() != null
						&& innerType.getFullName().equals(mUnion.getTypeName()))
					)
				) {
					LOG.debug("Types match.");
					return true;
				}
			} else {
				LOG.debug("Simple types match.");
				return true;
			}
		}
		LOG.debug("Types don't match");
		return false;
	}

	@Override
	public final void onCheckedChanged(final CompoundButton buttonView,
			final boolean isChecked) {
		// Uncheck the other radio buttons in the group.
		if (isChecked) {
			for (RadioButton button : mSchema.keySet()) {
				if (button != buttonView) {
					button.setChecked(false);
				}
			}
			// Set the value based on this button
			ValueHandler innerHandler = mHandlers.get(buttonView);
			LOG.debug("Union value set to: {} : {}",
					innerHandler.getValue(), mSchema.get(buttonView));
			mUnion.setValue(innerHandler.getValue(), mSchema.get(buttonView));
			mDataModel.onChanged();
			mViews.get(buttonView).setEnabled(isChecked);
		} else {
			mViews.get(buttonView).setEnabled(isChecked);
		}
	}

	/**
	 *
	 * @param radioButton the radio button to get a handler for
	 * @param innerSchema the schema for the type
	 * @return the value handler for the given radio button
	 */
	public final ValueHandler getHandler(final RadioButton radioButton,
			final Schema innerSchema) {
		final Object value;
		if (isMatchingType(innerSchema)) {
			value = mUnion.getValue();
		} else {
			value = null;
		}

		ValueHandler handler = new ValueHandler() {

			/** The value we are currently holding. */
			private Object mValue = value;

			@Override
			public Object getValue() {
				return mValue;
			}

			@Override
			public void setValue(final Object value) {
				mValue = value;
				onCheckedChanged(radioButton, true);
			}

			@Override
			public Uri getValueUri() throws NotBoundException {
				return mValueHandler.getValueUri();
			}

			@Override
			public String getFieldName() {
				return mValueHandler.getFieldName();
			}
		};
		mHandlers.put(radioButton, handler);

		return handler;
	}

}
