package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.UriUnion;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;

import android.net.Uri;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

public class UnionHandler implements OnCheckedChangeListener {

    private final AvroRecordModel mDataModel;
    private final Map<RadioButton, Schema> mSchema = new HashMap<RadioButton, Schema>();
    private final ValueHandler mValueHandler;
    private final Map<RadioButton, View> mViews = new HashMap<RadioButton, View>();
    private final Map<RadioButton, ValueHandler>mHandlers = new HashMap<RadioButton, ValueHandler>();
    private final UriUnion mUnion;

    public UnionHandler(AvroRecordModel dataModel, ValueHandler valueHandler, UriUnion union) {
        mDataModel = dataModel;
        mValueHandler = valueHandler;
        mUnion = union;
        mValueHandler.setValue(mUnion);
    }

    public void addType(RadioButton radioButton, Schema innerType, View view) {
        mSchema.put(radioButton, innerType);
        mViews.put(radioButton, view);
        view.setEnabled(false);
        radioButton.setOnCheckedChangeListener(this);
        if (mUnion == null) {
            if (innerType.getType() == Schema.Type.NULL) {
                radioButton.setChecked(true);
            } else {
                radioButton.setChecked(false);
            }
        } else if (innerType.getType() == mUnion.getType()) {
            switch (mUnion.getType()) {
            case RECORD:
            case ENUM:
                // TODO: Fixed is a named type as well
                if ((innerType.getName() == null && mUnion.getTypeName() == null) ||
                        (innerType.getName() != null && innerType.getName().equals(mUnion.getTypeName()))) {
                    radioButton.setChecked(true);
                }
                break;
            default:
                radioButton.setChecked(true);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Uncheck the other radio buttons in the group.
        if (isChecked) {
            for (RadioButton button : mSchema.keySet()) {
                if (button != buttonView) {
                    button.setChecked(false);
                }
            }
        }
        // Set the value based on this button
        ValueHandler innerHandler = mHandlers.get(buttonView);
        mUnion.setValue(innerHandler.getValue(), mSchema.get(buttonView));
        mDataModel.onChanged();
        mViews.get(buttonView).setEnabled(isChecked);
    }

    public ValueHandler getHandler(final RadioButton radioButton) {
        ValueHandler handler = new ValueHandler() {

            Object mValue;

            @Override
            public Object getValue() {
                return mValue;
            }

            @Override
            public void setValue(Object value) {
                mValue = value;
                onCheckedChanged(radioButton, true);
            }

            @Override
            public Uri getValueUri(Uri uri) {
                return uri;
            }
        };
        mHandlers.put(radioButton, handler);

        return handler;
    }

}
