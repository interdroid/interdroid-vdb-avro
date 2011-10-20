package interdroid.vdb.avro.control;

import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.view.AvroBaseEditor;
import interdroid.vdb.avro.view.factory.AvroViewFactory;
import interdroid.vdb.content.EntityUriMatcher;
import interdroid.vdb.content.EntityUriMatcher.UriMatch;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

// TODO: The database should be wired to notify the controller
// and update the view should something else
// edit the database behind our back.

/**
 * The AvroController manages the model writing it as required by the activity.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public class AvroController {
	/**
	 * Access to logger.
	 */
	private static final Logger LOG =
			LoggerFactory.getLogger(AvroController.class);

	/** The edit state. */
    public static final int STATE_EDIT = 0;
    /** The insert state. */
    public static final int STATE_INSERT = 1;
    /** The canceled state. */
    public static final int STATE_CANCELED = 2;

    /** The schema we are managing. */
    private final Schema mSchema;
    /** The name of the type. */
    private final String mTypeName;
    /** The default uri for this type. */
    private final Uri mDefaultUri;
    /** The editor we are working for. */
    private final AvroBaseEditor mActivity;
    /** Are we in read only mode? */
    private boolean mReadOnly;

    /** The uri for the data. */
    private Uri mUri;
    /** The state we are in. */
    private int mState;
    /** The model of the record. */
    private AvroRecordModel mDataModel;

    /**
     * Construct a controller.
     * @param activity the activity we are working for
     * @param typeName the name of the type
     * @param defaultUri the uri for the data
     * @param schema the schema for the data
     */
    public AvroController(final AvroBaseEditor activity, final String typeName,
            final Uri defaultUri, final Schema schema) {
        mTypeName = typeName;
        mDefaultUri = defaultUri;
        mSchema = schema;
        mActivity = activity;
    }

    /**
     * Take care of saving the current state in the model.
     * @throws NotBoundException if the model is not bound
     */
    public final void handleSave() throws NotBoundException {
        if (mState != STATE_CANCELED && !mReadOnly) {
            mDataModel.storeCurrentValue();
        }
    }

    /**
     * Returns the name of the type the controller is currently handling.
     *
     * @return the name of the type
     */
    public final String getTypeName() {
        return mTypeName;
    }

    /**
     * Take care of loading the data from the database to the model.
     * @throws NotBoundException if the record is not bound
     */
    public final void loadData() throws NotBoundException {
        mDataModel.loadData();

        // Set the layout for this activity now that the model is ready.
        AvroViewFactory.buildRootView(mActivity, mDataModel);
    }

    /**
     * Take care of saving the current state of the model to the database.
     *
     * @param outState
     *            the bundle to save to
     * @throws NotBoundException if the record is not bound
     */
    public final void saveState(final Bundle outState)
    		throws NotBoundException {
        if (mState != STATE_CANCELED) {
            mDataModel.saveState(outState);
        }
    }

    /**
     * Take care of deleting the data for the model from the database.
     * @throws NotBoundException if the record is not bound
     */
    public final void handleDelete() throws NotBoundException {
        mState = STATE_CANCELED;
        mDataModel.delete();
    }

    /**
     * Take care of staring the data in the original version of the model to the
     * database.
     * @throws NotBoundException if the record is not bound
     */
    public final void storeOriginalValue() throws NotBoundException {
        mDataModel.storeOriginalValue();
    }

    /**
     * Handle a cancel of the current operation.
     * @throws NotBoundException if the record is not bound
     */
    public final void handleCancel() throws NotBoundException {
        if (mState == STATE_EDIT) {
            storeOriginalValue();
        } else if (mState == STATE_INSERT) {
            // We inserted an empty field, make sure to delete it
            handleDelete();
        }
        mState = STATE_CANCELED;
    }

    /**
     * Setup the controller to handle the given intent and bundle Builds the
     * view and sets the activity to show it.
     *
     * @param intent
     *            the intent which is setting up this controller
     * @param savedState
     *            the saved state for the controller to load from
     * @return the uri of the data item.
     * @throws NotBoundException if the record is not bound
     */
    public final Uri setup(final Intent intent, final Bundle savedState)
    		throws NotBoundException {
        if (intent.getData() == null) {
            if (mDefaultUri == null) {
                LOG.error("No URI and no default.");
                Toast.makeText(mActivity, "No URI specified and no default.",
                        Toast.LENGTH_SHORT).show();
                return null;
            }
            mUri = mDefaultUri;
        } else {
            mUri = intent.getData();
        }

        UriMatch match = EntityUriMatcher.getMatch(mUri);
        mReadOnly = match.isReadOnlyCheckout();

        // Append the default entity name if we didn't get one already.
        if (match.entityName == null) {
            if (intent.hasExtra(AvroBaseEditor.ENTITY)) {
                LOG.debug("Adding intent entity: {}",
                		intent.getStringExtra(AvroBaseEditor.ENTITY));
                mUri = Uri.parse(mUri.toString() + "/"
                		+ intent.getStringExtra(AvroBaseEditor.ENTITY));
            } else {
                LOG.debug("Adding schema root entity: {}", mSchema.getName());
                mUri = mUri.buildUpon().appendPath(mSchema.getName()).build();
            }
        }

        LOG.debug("Setting up for uri: " + mUri);

        // Do some setup based on the action being performed.
        String action = intent.getAction();
        LOG.debug("Performing action: " + action);
        if (action == null) {
            action = Intent.ACTION_INSERT;
        }
        if (Intent.ACTION_EDIT.equals(action)) {
            LOG.debug("STATE_EDIT");
            mState = STATE_EDIT;
        } else if (Intent.ACTION_INSERT.equals(action)
                || Intent.ACTION_MAIN.equals(action)) {
            LOG.debug("STATE_INSERT");
            mState = STATE_INSERT;
            LOG.debug("Inserting new record into: " + mUri);
            Uri tempUri = null;
            try {
                tempUri = mActivity.getApplicationContext()
                    .getContentResolver().insert(mUri, new ContentValues());
            } catch (Exception e) {
                LOG.error("Insert threw something: ", e);
            }
            LOG.debug("Insert complete.");
            // If we were unable to create a new field, then just finish
            // this activity. A RESULT_CANCELED will be sent back to the
            // original activity if they requested a result.
            if (tempUri == null) {
                LOG.error("Failed to insert into " + mUri);
                Toast.makeText(mActivity, "Unable to insert data.",
                        Toast.LENGTH_SHORT).show();
                mUri = null;
                return null;
            }
            mUri = tempUri;
        } else {
            // Whoops, unknown action! Bail.
            LOG.error("Unknown action, exiting");
            Toast.makeText(mActivity, "Unknown action.", Toast.LENGTH_SHORT)
            .show();
            mUri = null;
            return null;
        }

        // Build the model
        mDataModel = new AvroRecordModel(mActivity, mUri, mSchema);

        // Load any savedInstanceState into the model
        mDataModel.loadOriginals(savedState);

        return mUri;
    }

    /**
     * Return the state the controller is in.
     *
     * @return either EDIT_STATE or INSERT_STATE
     */
    public final int getState() {
        return mState;
    }

    /**
     * Sets the content resolver on the underlying data model.
     * @param contentResolver the resolver for the model to use
     */
    public final void setResolver(final ContentResolver contentResolver) {
        mDataModel.setResolver(contentResolver);
    }

    /**
     * @return the schema for the type being controlled
     */
	public final Schema getSchema() {
		return mSchema;
	}

}
