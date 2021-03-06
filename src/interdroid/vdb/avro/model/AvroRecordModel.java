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
    /** Access to logger. */
    private static final Logger LOG =
            LoggerFactory.getLogger(AvroRecordModel.class);

    /* =-=-=-= Model State =-=-=-= */
    /** The schema we are modeling. */
    private final Schema mSchema;
    /** The uri for the data. */
    private final Uri mUri;
    /** The activity using the model. */
    private final Activity mActivity;

    /** The content resolver we are using. */
    private ContentResolver mResolver;
    /** The current state of the model. */
    private UriRecord mCurrentModel;
    /** The original state of the model. */
    private UriRecord mOriginalModel;
    /** Is the model dirty. */
    private boolean mDirty;

    // TODO: It would be really nice to have fine
    // grained dirty flags at all levels.


    public void onChanged() {
        super.onChanged();
        mDirty = true;
    }

    /**
     * Constructs a Model for the given Schema. The Schema must be of type
     * RECORD.
     *
     * @param activity the activity to work for
     * @param rootUri the uri to model
     * @param schema the schema for the given uri
     */
    public AvroRecordModel(final Activity activity, final Uri rootUri,
            final Schema schema) {
        super();
        if (schema.getType() != Type.RECORD) {
            throw new IllegalArgumentException("Not a record!");
        }
        LOG.debug("Constructed model for: " + schema);
        mSchema = schema;
        mUri = rootUri;
        mResolver = activity.getContentResolver();
        mActivity = activity;
    }

    /**
     * Loads the original state of the model from the bundle.
     *
     * @param saved
     *            the bundle to store to
     * @throws NotBoundException if the record model is not bound
     */
    public final void loadOriginals(final Bundle saved)
            throws NotBoundException {
        if (saved != null) {
            LOG.debug("Loading from bundle.");
            mCurrentModel =
                    new UriRecord(mUri, mSchema).load(saved);
            if (mOriginalModel == null) {
                mOriginalModel =
                        new UriRecord(mUri, mSchema).load(saved);
            }
        }
    }

    /**
     * Restores the original values stored by the model to the database.
     * @throws NotBoundException if the record model is not bound
     */
    public final void storeOriginalValue() throws NotBoundException {
        if (mDirty && mOriginalModel != null) {
            LOG.debug("Storing original values.");
            mOriginalModel.save(mResolver);
        }
        else {
            LOG.debug("Not storing original: {} {}", mDirty, mOriginalModel != null);
        }
    }

    /**
     * Stores the current values held by the model to the database.
     * @throws NotBoundException ifthe record model is not bound
     */
    public final void storeCurrentValue() throws NotBoundException {
        if (mDirty && mCurrentModel != null) {
            LOG.debug("Storing current state to uri: " + mUri);
            mCurrentModel.save(mResolver);
        } else {
            LOG.debug("Not storing: {} {}", mDirty, mCurrentModel != null);
        }
    }

    /**
     * Loads the model from the database.
     * @throws NotBoundException if the record model is not bound
     */
    public final void loadData() throws NotBoundException {
        LOG.debug("Loading data from: " + mUri);
        mCurrentModel = new UriRecord(mUri, mSchema).load(mResolver);
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
     * @throws NotBoundException if the record model is not bound
     */
    public final void saveState(final Bundle outState)
            throws NotBoundException {
        if (mDirty && mCurrentModel != null) {
            LOG.debug("Saving current state to bundle.");
            mCurrentModel.save(outState);
        } else {
            LOG.debug("Not saving to bundle: {} {}",
                    mDirty, mCurrentModel != null);
        }
    }

    /**
     * Deletes the data for this model from the database.
     * @throws NotBoundException if the record model is not bound
     */
    public final void delete() throws NotBoundException {
        mOriginalModel.delete(mResolver);
    }

    /**
     * @return the schema for the model.
     */
    public final Schema schema() {
        return mSchema;
    }

    /**
     * Sets the value for the given field.
     * @param mFieldName the field to set
     * @param value the value to set the field to.
     */
    public final void put(final String mFieldName, final Object value) {
        if (mCurrentModel != null) {
            LOG.debug("Updating field: " + mFieldName + " to: " + value);
            mCurrentModel.put(mFieldName, value);
        }
        mDirty = true;
    }

    /**
     * Sets the resolver to be used to get access to data.
     * @param contentResolver the resolver to be used
     */
    public final void setResolver(final ContentResolver contentResolver) {
        mResolver = contentResolver;
    }

    /**
     * @return the current data model.
     */
    public final UriRecord getCurrentModel() {
        return mCurrentModel;
    }

    /**
     * @param nameField the name of the field to get
     * @return the value currently in the record for this field
     */
    public final Object get(final String nameField) {
        return mCurrentModel.get(nameField);
    }

    /**
     * Utility to run on the models activity ui thread.
     * @param runnable the runnable to run on the ui thread.
     */
    public final void runOnUI(final Runnable runnable) {
        mActivity.runOnUiThread(runnable);
    }
}
