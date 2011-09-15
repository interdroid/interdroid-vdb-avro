package interdroid.vdb.avro.view;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interdroid.util.view.AsyncTaskWithProgressDialog;
import interdroid.vdb.R;
import interdroid.vdb.avro.control.AvroController;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.content.avro.AvroProviderRegistry;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
	public static final String ENTITY = "entity";

	public static final int REQUEST_RECORD_SELECTION = 1;

	private AvroController mController;

	public AvroBaseEditor() {
		logger.debug("Constructed AvroBaseEditor: " + this);
	}

	protected AvroBaseEditor(Schema schema) {
		this(schema, null);
	}

	public AvroBaseEditor(Schema schema, Uri defaultUri) {
		this();
		setup(schema, defaultUri);
	}

	protected void setup(Schema schema, Uri defaultUri) {
		if (defaultUri == null) {
			logger.debug("Using default URI.");
			defaultUri = Uri.parse("content://" + schema.getNamespace() + "/branches/master/"+ schema.getName());
		}
		mController = new AvroController(this, schema.getName(), defaultUri, schema);
		logger.debug("Set controller for schema: " + schema.getName() + " : " + defaultUri + " : " + schema);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		logger.debug("onCreate: " + this);

		setupController(savedInstanceState);
	}

	private void setupController(Bundle savedState) {
		final Intent intent = getIntent();

		// If we don't have a controller, then create one...
		if (mController == null) {
			Uri defaultUri = intent.getData();
			if (defaultUri == null) {
				throw new IllegalArgumentException("A Uri is required.");
			}
			String schemaJson = intent.getStringExtra(SCHEMA);
			Schema schema = null;
			if (schemaJson == null) {
				schema = AvroProviderRegistry.getSchema(this, defaultUri);
				if (schema == null) {
					throw new IllegalArgumentException("Schema not found and not provided in the intent.");
				}
			} else {
				schema = Schema.parse(schemaJson);
			}
			logger.debug("Building controller for: " + schema.getName() + " : " + defaultUri);

			mController = new AvroController(this, schema.getName(), defaultUri, schema);
			logger.debug("Controller built: {}", mController);
		}

		Uri editUri = null;
		try {
			editUri = mController.setup(intent, savedState);
		} catch (NotBoundException e) {
			logger.error("Unable to build controller due to bind problem.", e);
		}

		if (editUri == null) {
			logger.debug("No edit URI built.");
			finish();
			return;
		} else {
			// Everything was setup properly so assume the result will work.
			logger.debug("Setting result ok: {}", editUri);
			Intent resultIntent = new Intent();
			resultIntent.setData(editUri);
			setResult(RESULT_OK, resultIntent);
		}
	}

	private class LoadTask extends AsyncTaskWithProgressDialog<Object, Void, Void> {

		public LoadTask() {
			super(AvroBaseEditor.this, getString(R.string.label_loading), getString(R.string.label_wait));
		}

		protected void onPostExecute(Void v) {

			// Modify our overall title depending on the mode we are running in.
			if (mController.getState() == AvroController.STATE_EDIT) {
				AvroBaseEditor.this.setTitle(getText(R.string.title_edit) + " " + mController.getTypeName());
			} else if (mController.getState() == AvroController.STATE_INSERT) {
				AvroBaseEditor.this.setTitle(getText(R.string.title_create) + " " + mController.getTypeName());
			}

			super.onPostExecute(v);
		}

		@Override
		protected Void doInBackground(Object... params) {
			try {
				mController.loadData();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

	protected void onPause() {
		super.onPause();

		logger.debug("onPause");

		try {
			mController.handleSave();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	protected void onResume() {
		super.onResume();

		logger.debug("onResume");

		// Are we come backing from a for result task?
		logger.debug("Loading Data");

		new LoadTask().execute(mController);

		logger.debug("Ready");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		logger.debug("onSaveInstanceState");
		try {
			mController.saveState(outState);
		} catch (NotBoundException e) {
			logger.error("Error saving state due to record binding problem.", e);
		}
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
			try {
				mController.handleDelete();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finish();
			break;
		case DISCARD_ID:
		case REVERT_ID:
			try {
				mController.handleCancel();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void registerEnum(int hashCode, Schema schema) {
		enums.put(hashCode, schema);
	}

	public void launchIntent(Intent editIntent) {
		editIntent.addCategory(Intent.CATEGORY_DEFAULT);
		editIntent.setClassName(this.getPackageName(), AvroBaseEditor.class.getName());
		startActivity(editIntent);
	}

	public void launchCameraIntent(Intent cameraIntent) {
		cameraIntent.addCategory(Intent.CATEGORY_DEFAULT);
		startActivity(cameraIntent);
	}
}
