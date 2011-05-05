package interdroid.vdb.avro.control.handler;

import java.util.Calendar;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;

public class DateHandler implements OnClickListener {

	private DatePicker mView;
	private ValueHandler mValueHandler;

	public DateHandler(DatePicker view, ValueHandler valueHandler) {
		mView = view;
		mValueHandler = valueHandler;
		// Set the initial value
		Calendar value = Calendar.getInstance();
		value.setTimeInMillis((Long)mValueHandler.getValue() / 1000L);
		mView.updateDate(value.get(Calendar.YEAR), value.get(Calendar.MONTH), value.get(Calendar.DATE));
	}

	@Override
	public void onClick(View v) {
		// Update the model
		Calendar value = Calendar.getInstance();
		value.set(mView.getYear(), mView.getMonth(), mView.getDayOfMonth());
		mValueHandler.setValue(value.getTimeInMillis()/1000);
	}

}
