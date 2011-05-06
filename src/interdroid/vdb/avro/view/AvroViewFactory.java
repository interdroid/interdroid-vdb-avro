package interdroid.vdb.avro.view;

import interdroid.vdb.R;
import interdroid.vdb.avro.control.handler.ArrayHandler;
import interdroid.vdb.avro.control.handler.ArrayValueHandler;
import interdroid.vdb.avro.control.handler.CheckboxHandler;
import interdroid.vdb.avro.control.handler.DateHandler;
import interdroid.vdb.avro.control.handler.EditTextHandler;
import interdroid.vdb.avro.control.handler.EnumHandler;
import interdroid.vdb.avro.control.handler.RecordTypeSelectHandler;
import interdroid.vdb.avro.control.handler.RecordValueHandler;
import interdroid.vdb.avro.control.handler.UnionHandler;
import interdroid.vdb.avro.control.handler.ValueHandler;
import interdroid.vdb.avro.model.AvroRecordModel;

import java.text.BreakIterator;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Array;
import org.apache.avro.generic.GenericData.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.AbsListView.LayoutParams;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AvroViewFactory {
	private static final Logger logger = LoggerFactory.getLogger(AvroViewFactory.class);

	private static final int LEFT_INDENT = 3;
	private static final int DEFAULT_CAPACITY = 10;

	private static LayoutInflater getLayoutInflater(Activity activity) {
		return (LayoutInflater) activity.getApplicationContext()
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public static void buildRootView(AvroBaseEditor activity, AvroRecordModel dataModel) {
		logger.debug("Constructing root view: " + dataModel.schema());
		ViewGroup viewGroup = (ViewGroup) getLayoutInflater(activity).inflate(
				R.layout.avro_base_editor, null);
		ScrollView scroll = new ScrollView(activity);
		scroll.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		scroll.addView(viewGroup);
		activity.setContentView(scroll);
		buildRecordView(true, activity, dataModel, dataModel.getCurrentModel(), viewGroup);
	}

	public static View buildRecordView(boolean isRoot, AvroBaseEditor activity, AvroRecordModel dataModel, Record record, ViewGroup viewGroup) {
		logger.debug("Building record view.");
		//		LinearLayout layout = new LinearLayout(activity);
		//		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		//		layout.setGravity(Gravity.FILL_HORIZONTAL);
		//		layout.setOrientation(LinearLayout.VERTICAL);
		//		layout.setPadding(LEFT_INDENT, 0, 0, 0);

		//		TextView label = new TextView(activity);
		//		label.setText(toTitle(record.getSchema().getName()));
		//		label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		//		label.setGravity(Gravity.LEFT);
		//		layout.addView(label);

		// Construct a view for each field
		for (Field field : record.getSchema().getFields()) {
			logger.debug("Building view for: " + field.name() + " in: " + record.getSchema() + " schema:" + field.schema());
			buildFieldView(isRoot, activity, dataModel, record, viewGroup, field);
		}

		//		if (viewGroup != null)
		//			viewGroup.addView(layout);

		return viewGroup;
	}

	private static View buildView(boolean isRoot, AvroBaseEditor activity, AvroRecordModel dataModel, ViewGroup viewGroup, Schema schema, ValueHandler valueHandler) {
		View view;
		switch (schema.getType()) {
		case ARRAY:
			view = buildArrayList(activity, dataModel, viewGroup, schema.getElementType(), getArray(valueHandler, schema));
			break;
		case BOOLEAN:
			view = buildCheckbox(activity, viewGroup, new CheckboxHandler(dataModel, valueHandler));
			break;
		case BYTES:
			// TODO:
			view = null;
			break;
		case ENUM:
			view = buildEnum(activity, dataModel, viewGroup, schema, valueHandler);
			break;
		case FIXED:
			// TODO:
			view = null;
			break;
		case DOUBLE:
		case FLOAT:
		{
			view = buildEditText(activity, viewGroup, schema,
					InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED,
					new EditTextHandler(dataModel, schema.getType(), valueHandler)
			);
			break;
		}
		case INT:
		case LONG:
		{
			if (schema.getProp("ui.widget") != null) {
				logger.debug("Building custom ui widget for long/int");
				if (schema.getProp("ui.widget").equals("date")) {
					logger.debug("Building date view.");
					view = buildDateView(activity, viewGroup, valueHandler);
				} else {
					throw new RuntimeException("Unknown widget type: " + schema.getProp("ui.widget"));
				}
			} else {
				view = buildEditText(activity, viewGroup, schema,
						InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED,
						new EditTextHandler(dataModel, schema.getType(), valueHandler)
				);
			}
			break;
		}
		case MAP:
			// TODO:
			view = buildTextView(activity, viewGroup, R.string.not_implemented);
			break;
		case NULL:
		{
			view = buildTextView(activity, viewGroup, R.string.null_text);
			break;
		}
		case RECORD:
			if (isRoot) {
				view = buildRecordView(false, activity, dataModel, getRecord(valueHandler, schema), viewGroup);
			} else {
				Button button = new Button(activity);
				Record record = (Record) valueHandler.getValue();
				if (record == null) {
					button.setText("Create: " + schema.getName());
				} else {
					button.setText("Edit: " + schema.getName());
				}
				button.setOnClickListener(getRecordTypeSelectorHandler(activity, dataModel, schema, valueHandler, viewGroup, button));

				view = button;
				viewGroup.addView(button);
			}
			break;
		case STRING:
		{
			view = buildEditText(activity, viewGroup, schema, InputType.TYPE_CLASS_TEXT,
					new EditTextHandler(dataModel, schema.getType(), valueHandler));
			break;
		}
		case UNION:
		{
			view = buildUnion(activity, dataModel, viewGroup, schema, new UnionHandler(dataModel, valueHandler));
			break;
		}
		default:
			throw new RuntimeException("Unsupported type: " + schema);
		}

		if (schema.getProp("ui.visible") != null) {
			logger.debug("Hiding view.");
			view.setVisibility(View.GONE);
		}
		if (schema.getProp("ui.enabled") != null) {
			logger.debug("Disabling view.");
			view.setEnabled(false);
		}

		return view;
	}

	private static View buildDateView(AvroBaseEditor activity,
			ViewGroup viewGroup, ValueHandler valueHandler) {
		DatePicker view;
		view = new DatePicker(activity);
		DateHandler handler = new DateHandler(view, valueHandler);
		view.setOnClickListener(handler);
		viewGroup.addView(view);
		return view;
	}

	private static OnClickListener getRecordTypeSelectorHandler(AvroBaseEditor activity, AvroRecordModel dataModel,
			Schema schema, ValueHandler valueHandler, ViewGroup container, Button button) {
		return new RecordTypeSelectHandler(activity, dataModel, schema, valueHandler, button);
	}

	private static Record getRecord(ValueHandler valueHandler, Schema schema) {
		Record subRecord = (Record) valueHandler.getValue();
		if (subRecord == null) {
			subRecord = new GenericData.Record(schema);
			valueHandler.setValue(subRecord);
		}
		return subRecord;
	}

	private static Array<Object> getArray(ValueHandler valueHandler, Schema schema) {
		@SuppressWarnings("unchecked")
		Array<Object> array = (Array<Object>)valueHandler.getValue();
		if (array == null) {
			array = new Array<Object>(DEFAULT_CAPACITY, schema);
			valueHandler.setValue(array);
		}
		return array;
	}

	private static View buildEnum(AvroBaseEditor activity, AvroRecordModel dataModel,
			ViewGroup viewGroup, Schema schema, ValueHandler valueHandler) {
		Button selectedText = new Button(activity);
		viewGroup.addView(selectedText);
		new EnumHandler(activity, dataModel, schema, selectedText, valueHandler);
		return selectedText;
	}

	private static View buildFieldView(boolean isRoot, AvroBaseEditor activity, AvroRecordModel dataModel, Record record, ViewGroup viewGroup, Field field) {
		//		LinearLayout layout = new LinearLayout(activity);
		//		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		//		layout.setOrientation(LinearLayout.VERTICAL);

		// TODO: Add field comment as pressed text on field
		if (field.getProp("ui.visible") == null) {
			TextView label = new TextView(activity);
			label.setText(toTitle(field.name()));
			label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			label.setGravity(Gravity.LEFT);

			viewGroup.addView(label);
		}

		buildView(isRoot, activity, dataModel, viewGroup, field.schema(), new RecordValueHandler(record, field.name()));

		//		if (viewGroup != null)
		//			viewGroup.addView(layout);

		return viewGroup;
	}

	public static View buildArrayView(boolean isRoot, AvroBaseEditor activity, AvroRecordModel dataModel, Array<Object> array, Schema elementSchema, int offset, ViewGroup viewGroup) {
		return buildView(isRoot, activity, dataModel, viewGroup, elementSchema, new ArrayValueHandler(array, offset));
	}

	private static View buildUnion(AvroBaseEditor activity, AvroRecordModel dataModel, ViewGroup viewGroup, Schema schema,
			UnionHandler handler) {
		TableLayout table = new TableLayout(activity);
		for (Schema innerType : schema.getTypes()) {
			TableRow row = new TableRow(activity);

			RadioButton radioButton = new RadioButton(activity);
			radioButton.setFocusableInTouchMode(false);

			row.addView(radioButton);

			handler.addType(radioButton, innerType);
			buildView(false, activity, dataModel, row, innerType, handler.getHandler(radioButton));

			table.addView(row);
		}
		viewGroup.addView(table);
		return table;
	}

	private static View buildArrayList(AvroBaseEditor activity, AvroRecordModel dataModel, ViewGroup viewGroup, Schema schema, Array<Object> array) {
		LinearLayout layout = new LinearLayout(activity);
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(LEFT_INDENT, 0, 0, 0);

		ArrayHandler adapter = new ArrayHandler(activity, dataModel, layout, array, schema);

		ImageButton addButton = new ImageButton(activity);
		addButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addButton.setOnClickListener(adapter);
		addButton.setImageResource(android.R.drawable.ic_menu_add);
		addButton.setTag("add");
		layout.addView(addButton);

		viewGroup.addView(layout);


		return layout;
	}

	private static View buildCheckbox(AvroBaseEditor activity, ViewGroup viewGroup, CheckboxHandler changeListener) {
		CheckBox text = new CheckBox(activity);
		text.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		text.setGravity(Gravity.FILL_HORIZONTAL);
		changeListener.setWatched(text);
		viewGroup.addView(text);
		return text;
	}

	private static View buildEditText(AvroBaseEditor activity, ViewGroup viewGroup, Schema schema, int inputType, EditTextHandler textWatcher) {
		logger.debug("Building edit text for: " + schema);
		EditText text = null;
		if (schema.getProp("ui.resource") != null) {
			logger.debug("Inflating custom resource: " + schema.getProp("ui.resource"));
			try {
				LayoutInflater inflater = (LayoutInflater)activity.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				text = (EditText) inflater.inflate(Integer.valueOf(schema.getProp("ui.resource")), null);
			} catch (Exception e) {
				logger.error("Unable to inflate resource: " + schema.getProp("ui.resource"));
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException("Unable to inflate UI resource: " + schema.getProp("ui.resource"), e);
			}
		} else {
			text = new EditText(activity);
			text.setLayoutParams(new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			text.setGravity(Gravity.FILL_HORIZONTAL);
			text.setInputType(inputType);
		}
		viewGroup.addView(text);
		textWatcher.setWatched(text);
		return text;
	}

	private static View buildTextView(AvroBaseEditor activity, ViewGroup viewGroup, int textId) {
		TextView text = new TextView(activity);
		text.setText(textId);
		viewGroup.addView(text);
		return text;
	}

	private static String toTitle(String name) {
		StringBuffer sb = new StringBuffer();
		name = name.toLowerCase();
		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(name);
		boolean first = true;
		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary
		.next()) {
			if (first) {
				first = false;
			} else {
				sb.append(" ");
			}
			sb.append(name.substring(start, start + 1).toUpperCase());
			sb.append(name.substring(start + 1, end));
		}
		sb.append(":");
		return sb.toString();
	}

}