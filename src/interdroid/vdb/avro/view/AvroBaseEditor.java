package interdroid.vdb.avro.view;

import interdroid.util.view.AsyncTaskWithProgressDialog;
import interdroid.vdb.R;
import interdroid.vdb.avro.control.AvroController;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.view.factory.AvroViewFactory;
import interdroid.vdb.content.avro.AvroProviderRegistry;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * This class is the activity for editing an Avro Record.
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class AvroBaseEditor extends Activity {
	/** Access to logger. */
	private static final Logger LOG =
			LoggerFactory.getLogger(AvroBaseEditor.class);

	/** The action which triggers this activity. */
	public static final String ACTION_EDIT_SCHEMA =
			"interdroid.vdb.sm.action.EDIT_SCHEMA";

	/** Revert menu id. */
	private static final int REVERT_ID = Menu.FIRST;
	/** Discard menu id. */
	private static final int DISCARD_ID = Menu.FIRST + 1;
	/** Delete menu id. */
	private static final int DELETE_ID = Menu.FIRST + 2;

	/** The schema bundle parameter. */
	public static final String SCHEMA = "schema";
	/** The entity bundle parameter. */
	public static final String ENTITY = "entity";

	/** The code for requesting a Type selection. */
	public static final int REQUEST_RECORD_SELECTION = 1;

	/** The controller we use to manage the model. */
	private AvroController mController;

	/** Empty constructor for the editor. */
	public AvroBaseEditor() {
		LOG.debug("Constructed AvroBaseEditor: " + this);
	}

	/**
	 * Construct with the given schema.
	 * @param schema the schema
	 */
	protected AvroBaseEditor(final Schema schema) {
		this(schema, null);
	}

	/**
	 * Construct with the given schema pointing at the given Uri.
	 * @param schema the schema
	 * @param defaultUri the uri
	 */
	public AvroBaseEditor(final Schema schema, final Uri defaultUri) {
		this();
		setup(schema, defaultUri);
	}

	/**
	 * Sets up this activity using the given schema and uri.
	 * @param schema the schema to edit
	 * @param defaultUri the uri for the data
	 */
	protected final void setup(final Schema schema, final Uri defaultUri) {
		Uri theUri = defaultUri;
		if (theUri == null) {
			LOG.debug("Using default URI.");
			theUri = Uri.parse("content://"
			+ schema.getNamespace() + "/branches/master/" + schema.getName());
		}
		mController = new AvroController(this, schema.getName(),
				theUri, schema);
		LOG.debug("Set controller for schema: {} : {}", schema, theUri);
	}

	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LOG.debug("onCreate: " + this);

		setupController(savedInstanceState);
	}

	/**
	 * Sets up the controller this activity will use to manage the model.
	 * @param savedState the bundle to load from
	 */
	private void setupController(final Bundle savedState) {
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
					throw new IllegalArgumentException(
							"Schema not found and not provided in the intent.");
				}
			} else {
				schema = Schema.parse(schemaJson);
			}
			LOG.debug("Building controller for: {} : {}", schema.getName(),
					defaultUri);

			mController = new AvroController(this, schema.getName(),
					defaultUri, schema);
			LOG.debug("Controller built: {}", mController);
		}

		Uri editUri = null;
		try {
			editUri = mController.setup(intent, savedState);
		} catch (NotBoundException e) {
			LOG.error("Unable to build controller due to bind problem.", e);
		}

		if (editUri == null) {
			LOG.debug("No edit URI built.");
			finish();
			return;
		} else {
			// Everything was setup properly so assume the result will work.
			LOG.debug("Setting result ok: {}", editUri);
			Intent resultIntent = new Intent();
			resultIntent.setData(editUri);
			setResult(RESULT_OK, resultIntent);
		}
	}

	/**
	 * Loads the record in the background and builds the edit ui.
	 * @author nick &lt;palmer@cs.vu.nl&gt;
	 *
	 */
	private class LoadTask
		extends AsyncTaskWithProgressDialog<Object, Void, Void> {

		/**
		 * Build the dialog.
		 */
		public LoadTask() {
			super(AvroBaseEditor.this,
					getString(R.string.label_loading),
					getString(R.string.label_wait));
		}

		@Override
		protected void onPostExecute(final Void v) {

			// Modify our overall title depending on the mode we are running in.
			if (mController.getState() == AvroController.STATE_EDIT) {
				AvroBaseEditor.this.setTitle(getText(R.string.title_edit)
						+ " "
						+ AvroViewFactory.toTitle(mController.getSchema()));
			} else if (mController.getState() == AvroController.STATE_INSERT) {
				AvroBaseEditor.this.setTitle(getText(R.string.title_create)
						+ " "
						+ AvroViewFactory.toTitle(mController.getSchema()));
			}

			super.onPostExecute(v);
		}

		@Override
		protected Void doInBackground(final Object... params) {
			try {
				mController.loadData();
			} catch (NotBoundException e) {
				LOG.error("Error loading the data.", e);
			}
			return null;
		}

	}

	@Override
	protected final void onPause() {
		super.onPause();

		LOG.debug("onPause");

		try {
			mController.handleSave();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	protected final void onResume() {
		super.onResume();

		LOG.debug("onResume");

		// Are we come backing from a for result task?
		LOG.debug("Loading Data");

		new LoadTask().execute(mController);

		LOG.debug("Ready");
	}

	@Override
	protected final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		LOG.debug("onSaveInstanceState");
		try {
			mController.saveState(outState);
		} catch (NotBoundException e) {
			LOG.error("Error saving state due to record binding problem.", e);
		}
	}

	@Override
	public final void onStop() {
		super.onStop();
		LOG.debug("onStop");
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();
		LOG.debug("onDestroy");
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
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
	public final boolean onOptionsItemSelected(final MenuItem item) {
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
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
