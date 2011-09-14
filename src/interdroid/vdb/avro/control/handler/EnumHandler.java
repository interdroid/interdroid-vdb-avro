package interdroid.vdb.avro.control.handler;

import interdroid.vdb.R;
import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.view.AvroViewFactory;

import java.util.List;

import org.apache.avro.Schema;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class EnumHandler implements DialogInterface.OnClickListener, OnClickListener {

    protected final Activity mActivity;
    protected final AvroRecordModel mDataModel;
    protected final Schema mSchema;
    protected final TextView mTextView;

    private final ValueHandler mValueHandler;

    public EnumHandler(Activity activity, AvroRecordModel dataModel,
            Schema schema, TextView view, ValueHandler valueHandler) {
        mActivity = activity;
        mDataModel = dataModel;
        mSchema = schema;
        mTextView = view;
        mValueHandler = valueHandler;

        mTextView.setClickable(true);
        mTextView.setFocusable(true);
        mTextView.setOnClickListener(this);
        setText();
    }

    protected void setText() {
        final Integer ordinal = (Integer)mValueHandler.getValue();
        mDataModel.runOnUI(new Runnable() {
            public void run() {
                if (ordinal == null) {
                    mTextView.setText(R.string.none);
                } else {
                    mTextView.setText(mSchema.getEnumSymbols().get(ordinal));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getString(R.string.label_pick) + " " + AvroViewFactory.toTitle(mSchema));
        List<String> items = mSchema.getEnumSymbols();
        CharSequence[] itemArray = items.toArray(new CharSequence[items.size()]);
        Integer selection = (Integer)mValueHandler.getValue();
        if (selection == null) {
            selection = -1;
        }
        builder.setSingleChoiceItems(itemArray, selection, this);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mValueHandler.setValue(which);
        mTextView.setText(mSchema.getEnumSymbols().get(which));
        mTextView.postInvalidate();
        mDataModel.onChanged();
        dialog.dismiss();
    }

}
