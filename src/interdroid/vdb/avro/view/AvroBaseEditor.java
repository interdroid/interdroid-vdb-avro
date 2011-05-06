package interdroid.vdb.avro.view;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interdroid.vdb.R;
import interdroid.vdb.avro.control.AvroController;
import interdroid.vdb.avro.control.handler.RecordTypeSelectHandler;
import interdroid.vdb.content.EntityUriBuilder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AvroBaseEditor extends Activity {
	private static final Logger logger = LoggerFactory.getLogger(AvroBaseEditor.class);

	public static final String ACTION_EDIT_SCHEMA = "interdroid.vdb.sm.action.EDIT_SCHEMA";

	// Enums we may need dialogs for
	Map<Integer, Schema> enums = new HashMap<Integer, Schema>();

	// Menu options.
	private static final int REVERT_ID = Menu.FIRST;
	private static final int DISCARD_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;

	public static final String SCHEMA = "schema";

	public static final int REQUEST_RECORD_SELECTION = 1;

	private AvroController mController;

	private RecordTypeSelectHandler mRecordTypeSelectHandler;

	public AvroBaseEditor() {
		logger.debug("Constructed AvroBaseEditor: " + this + ":" + mController);
	}

	protected AvroBaseEditor(Schema schema) {
		this(schema, null);
	}

	public AvroBaseEditor(Schema schema, Uri defaultUri) {
		this();
		if (defaultUri == null) {
			logger.debug("Using default URI.");
			defaultUri = Uri.withAppendedPath(EntityUriBuilder.branchUri(schema.getNamespace(), "master"), schema.getName());
		}
		mController = new AvroController(this, schema.getName(), defaultUri, schema);
		logger.debug("Set controller for schema: " + schema.getName() + " : " + defaultUri + " : " + schema);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		logger.debug("onCreate: " + this);

		final Intent intent = getIntent();

		// If we don't have a controller, then create one...
		if (mController == null) {
			Uri defaultUri = intent.getData();
			if (defaultUri == null) {
				throw new IllegalArgumentException("A Uri is required.");
			}
			String schemaJson = intent.getStringExtra(SCHEMA);
			if (schemaJson == null) {
				throw new IllegalArgumentException("A Schema is required.");
			}
			Schema schema = Schema.parse(schemaJson);
			logger.debug("Building controller for: " + schema.getName() + " : " + defaultUri);

			mController = new AvroController(this, schema.getName(), defaultUri, schema);
		}

		final Uri editUri = mController.setup(intent, savedInstanceState);

		if (editUri == null) {
			logger.debug("No edit URI built.");
			finish();
			return;
		}

		// Everything was setup properly so assume the result will work.
		setResult(RESULT_OK, (new Intent()).setAction(editUri.toString()));
	}

	protected void onPause() {
		super.onPause();

		logger.debug("onPause");

		mController.handleSave();
	}


	@Override
	protected void onResume() {
		super.onResume();

		logger.debug("onResume");

		// Modify our overall title depending on the mode we are running in.
		if (mController.getState() == AvroController.STATE_EDIT) {
			setTitle(getText(R.string.title_edit) + " " + mController.getTypeName());
		} else if (mController.getState() == AvroController.STATE_INSERT) {
			setTitle(getText(R.string.title_create) + " " + mController.getTypeName());
		}

		mController.loadData();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		logger.debug("onSaveInstanceState");
		mController.saveState(outState);
	}

	public void onStop() {
		super.onStop();
		logger.debug("onStop");
	}

	public void onDestroy() {
		super.onDestroy();
		logger.debug("onDestroy");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Build the menus that are shown when editing.
		if (mController.getState() == AvroController.STATE_EDIT) {
			menu.add(0, REVERT_ID, 0, R.string.menu_revert)
			.setShortcut('0', 'r')
			.setIcon(android.R.drawable.ic_menu_revert);
			menu.add(0, DELETE_ID, 0, R.string.menu_delete)
			.setShortcut('1', 'd')
			.setIcon(android.R.drawable.ic_menu_delete);
			// Build the menus that are shown when inserting.
		} else {
			menu.add(0, DISCARD_ID, 0, R.string.menu_discard)
			.setShortcut('0', 'd')
			.setIcon(android.R.drawable.ic_menu_delete);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case DELETE_ID:
			mController.handleDelete();
			finish();
			break;
		case DISCARD_ID:
		case REVERT_ID:
			mController.handleCancel();
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void registerEnum(int hashCode, Schema schema) {
		enums.put(hashCode, schema);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		logger.debug("Result: " + requestCode + " : " + resultCode);
		if (requestCode == REQUEST_RECORD_SELECTION) {
			if (resultCode == RESULT_OK) {
				mController.setResolver(getContentResolver());
				mRecordTypeSelectHandler.setResult(data);
			}
		}
	}

	public void launchResultIntent(RecordTypeSelectHandler recordTypeSelectHandler, Intent editIntent, int action) {
		mRecordTypeSelectHandler = recordTypeSelectHandler;
		editIntent.addCategory(Intent.CATEGORY_DEFAULT);
		editIntent.setComponent(new ComponentName(this, AvroBaseEditor.class));
		logger.debug("TYPE: " + getContentResolver().getType(editIntent.getData()));
		startActivityForResult(editIntent, action);
	}
}
