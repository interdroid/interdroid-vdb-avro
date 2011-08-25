package interdroid.vdb.avro.control;

import interdroid.vdb.avro.model.AvroRecordModel;
import interdroid.vdb.avro.model.NotBoundException;
import interdroid.vdb.avro.view.AvroBaseEditor;
import interdroid.vdb.avro.view.AvroViewFactory;
import interdroid.vdb.content.EntityUriMatcher;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

// TODO: The database should be wired to notify the controller and update the view should something else
// edit the database behind our back.

public class AvroController {
    private static final Logger logger = LoggerFactory.getLogger(AvroController.class);

    public static final int STATE_EDIT = 0;
    public static final int STATE_INSERT = 1;
    public static final int STATE_CANCELED = 2;

    private final Schema mSchema;
    private final String mTypeName;
    private final Uri mDefaultUri;
    private final AvroBaseEditor mActivity;
    private boolean mReadOnly;

    protected Uri mUri;
    protected int mState;

    private AvroRecordModel mDataModel;

    public AvroController(AvroBaseEditor activity, String typeName,
            Uri defaultUri, Schema schema) {
        mTypeName = typeName;
        mDefaultUri = defaultUri;
        mSchema = schema;
        mActivity = activity;
    }

    /**
     * Take care of saving the current state in the model.
     * @throws NotBoundException
     */
    public void handleSave() throws NotBoundException {
        if (mState != STATE_CANCELED && !mReadOnly) {
            mDataModel.storeCurrentValue();
        }
    }

    /**
     * Returns the name of the type the controller is currently handling
     *
     * @return the name of the type
     */
    public String getTypeName() {
        return mTypeName;
    }

    /**
     * Take care of loading the data from the database to the model.
     * @throws NotBoundException
     */
    public void loadData() throws NotBoundException {
        mDataModel.loadData();

        // Set the layout for this activity now that the model is ready.
        AvroViewFactory.buildRootView(mActivity, mDataModel);
    }

    /**
     * Take care of saving the current state of the model to the database.
     *
     * @param outState
     *            the bundle to save to
     */
    public void saveState(Bundle outState) {
        if (mState != STATE_CANCELED) {
            mDataModel.saveState(outState);
        }
    }

    /**
     * Take care of deleting the data for the model from the database.
     * @throws NotBoundException
     */
    public void handleDelete() throws NotBoundException {
        mState = STATE_CANCELED;
        mDataModel.delete();
    }

    /**
     * Take care of staring the data in the original verison of the model to the
     * database.
     * @throws NotBoundException
     */
    public void storeOriginalValue() throws NotBoundException {
        mDataModel.storeOriginalValue();
    }

    /**
     * Handle a cancel of the current operation
     * @throws NotBoundException
     */
    public void handleCancel() throws NotBoundException {
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
     */
    public Uri setup(Intent intent, Bundle savedState) {
        if (intent.getData() == null) {
            if (mDefaultUri == null) {
                logger.error("No URI and no default.");
                Toast.makeText(mActivity, "No URI specified and no default.",
                        Toast.LENGTH_SHORT).show();
                return null;
            }
            mUri = mDefaultUri;
        } else {
            mUri = intent.getData();
        }

        mReadOnly = EntityUriMatcher.getMatch(mUri).isReadOnlyCheckout();

        logger.debug("Setting up for uri: " + mUri);

        // Do some setup based on the action being performed.
        String action = intent.getAction();
        logger.debug("Performing action: " + action);
        if (action == null) {
            action = Intent.ACTION_INSERT;
        }
        if (Intent.ACTION_EDIT.equals(action)) {
            logger.debug("STATE_EDIT");
            mState = STATE_EDIT;
        } else if (Intent.ACTION_INSERT.equals(action)
                || Intent.ACTION_MAIN.equals(action)) {
            logger.debug("STATE_INSERT");
            mState = STATE_INSERT;
            logger.debug("Inserting new record into: " + mUri);
            Uri tempUri = null;
            try {
                tempUri = mActivity.getApplicationContext()
                    .getContentResolver().insert(mUri, new ContentValues());
            } catch (Exception e) {
                logger.error("Insert threw something: ", e);
            }
            logger.debug("Insert complete.");
            // If we were unable to create a new field, then just finish
            // this activity. A RESULT_CANCELED will be sent back to the
            // original activity if they requested a result.
            if (tempUri == null) {
                logger.error("Failed to insert into " + mUri);
                Toast.makeText(mActivity, "Unable to insert data.",
                        Toast.LENGTH_SHORT).show();
                mUri = null;
                return null;
            }
            mUri = tempUri;
        } else {
            // Whoops, unknown action! Bail.
            logger.error("Unknown action, exiting");
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
     * Return the state the controller is in
     *
     * @return either EDIT_STATE or INSERT_STATE
     */
    public int getState() {
        return mState;
    }

    public void setResolver(ContentResolver contentResolver) {
        mDataModel.setResolver(contentResolver);
    }

}
