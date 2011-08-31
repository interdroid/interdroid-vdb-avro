package interdroid.vdb.avro.model;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

// TODO: Call verify to verify the data is of the right type?

/**
 * A class which knows how to load and store an arbitrary avro schema to both a
 * cursor and a Bundle and has a model for the original values and updated
 * values.
 */
public class AvroRecordModel extends DataSetObserver {
    private static final Logger logger = LoggerFactory.getLogger(AvroRecordModel.class);

    /* =-=-=-= Helper Constants For More Readable Code In This Class =-=-=-= */
//    static final String SEPARATOR = AvroContentProvider.SEPARATOR;
//    static final String _COUNT = SEPARATOR + "count";
//    static final String _KEY = AvroContentProvider.KEY_COLUMN_NAME;
//    static final String _VALUE = AvroContentProvider.VALUE_COLUMN_NAME;
//    static final String _TYPE = AvroContentProvider.TYPE_COLUMN_NAME;
//    static final String _TYPE_NAME = AvroContentProvider.TYPE_NAME_COLUMN_NAME;
//    static final String _URI_NAME = AvroContentProvider.TYPE_URI_COLUMN_NAME;

    /* =-=-=-= Model State =-=-=-= */
    private final Schema mSchema;
    private final Uri mUri;
    private final Activity mActivity;

    private ContentResolver mResolver;
    private UriRecord mCurrentStateModel;
    private UriRecord mOriginalModel;
    private boolean mDirty;

    // TODO: It would be really nice to have fine grained dirty flags at all levels.

    /**
     * Constructs a Model for the given Schema. The Schema must be of type
     * RECORD.
     *
     * @param schema
     *            The schema to model
     */
    public AvroRecordModel(Activity activity, Uri rootUri, Schema schema) {
        if (schema.getType() != Type.RECORD) {
            throw new RuntimeException("Not a record!");
        }
        logger.debug("Constructed model for: " + schema);
        mSchema = schema;
        mUri = rootUri;
        mResolver = activity.getContentResolver();
        mActivity = activity;
    }

    /**
     * Loads the original state of the model from the bundle
     *
     * @param savedInstanceState
     *            the bundle to store to
     * @throws NotBoundException
     */
    public void loadOriginals(Bundle savedInstanceState) throws NotBoundException {
        if (savedInstanceState != null) {
            logger.debug("Loading from bundle.");
            mCurrentStateModel = new UriRecord(mUri, mSchema).load(savedInstanceState);
            if (mOriginalModel == null) {
                mOriginalModel = new UriRecord(mUri, mSchema).load(savedInstanceState);
            }
        }
    }

    /**
     * Restores the original values stored by the model to the database.
     * @throws NotBoundException
     */
    public void storeOriginalValue() throws NotBoundException {
        logger.debug("Storing original values.");
        if (mDirty && mOriginalModel != null) {
            mOriginalModel.save(mResolver);
        }
    }

    /**
     * Stores the current values held by the model to the database.
     * @throws NotBoundException
     */
    public void storeCurrentValue() throws NotBoundException {
        logger.debug("Storing current state to uri: " + mUri);
        if (mDirty && mCurrentStateModel != null) {
           mCurrentStateModel.save(mResolver);
        }
    }

    /**
     * Loads the model from the database.
     * @throws NotBoundException
     */
    public void loadData() throws NotBoundException {
        logger.debug("Loading data from: " + mUri);
        mCurrentStateModel = new UriRecord(mUri, mSchema).load(mResolver);
        mDirty = false;
        // If there is no original model then load another copy
        // TODO: Can we do a clone here?
        if (mOriginalModel == null) {
            mOriginalModel = new UriRecord(mUri, mSchema).load(mResolver);
        }
    }

    /**
     * Saves the model to the given bundle.
     *
     * @param outState
     *            the bundle to save to
     * @throws NotBoundException
     */
    public void saveState(Bundle outState) throws NotBoundException {
        if (mDirty && mCurrentStateModel != null) {
            logger.debug("Saving current state to bundle.");
            mCurrentStateModel.save(outState);
        }
    }

    /**
     * Deletes the data for this model from the database.
     * @throws NotBoundException
     */
    public void delete() throws NotBoundException {
        mOriginalModel.delete(mResolver);
    }


    public Schema schema() {
        return mSchema;
    }

    public void put(String mFieldName, Object value) {
        if (mCurrentStateModel != null) {
            logger.debug("Updating field: " + mFieldName + " to: " + value);
            mCurrentStateModel.put(mFieldName, value);
        }
    }

    public void setResolver(ContentResolver contentResolver) {
        mResolver = contentResolver;
    }

    public UriRecord getCurrentModel() {
        return mCurrentStateModel;
    }

    public Object get(String nameField) {
        return mCurrentStateModel.get(nameField);
    }

    public void runOnUI(Runnable runnable) {
        mActivity.runOnUiThread(runnable);
    }

    public void onChanged() {
        mDirty = true;
    }

    public void onInvalidated() {
        mDirty = true;
    }
}
