package interdroid.vdb.avro.model;

import org.apache.avro.Schema.Type;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;

public class UriBoundAdapter<A> implements UriBound<A> {

    public static interface UriBoundAdapterImpl<A> {
        public A loadImpl(Bundle b, String prefix) throws NotBoundException;

        public void saveImpl(Bundle b, String prefix) throws NotBoundException;

        public void deleteImpl(ContentResolver resolver) throws NotBoundException;

        public A loadImpl(ContentResolver resolver, String fieldName) throws NotBoundException;

        public void saveImpl(ContentResolver resolver, String fieldName) throws NotBoundException;
    }

    private Uri mInstanceUri;
    private UriBoundAdapterImpl<A> mAdapter;

    public UriBoundAdapter(final Uri uri, final UriBoundAdapterImpl<A> adapter) {
        mInstanceUri = uri;
        mAdapter = adapter;
    }

    public UriBoundAdapter(String prefix, Bundle saved, UriBoundAdapterImpl<A> adapter) {
        mInstanceUri = saved.getParcelable(NameHelper.getTypeNameUri(prefix));
        mAdapter = adapter;
    }

    public UriBoundAdapter(Bundle saved, UriBoundAdapterImpl<A> adapter) {
        this(null, saved, adapter);
    }

    public final Uri getInstanceUri() throws NotBoundException {
        if (mInstanceUri == null) {
            throw new NotBoundException();
        }
        return mInstanceUri;
    }

    public final void setInstanceUri(final Uri uri) {
        mInstanceUri = uri;
    }

    @Override
    public final void save(final ContentResolver resolver, final String fieldName) throws NotBoundException {
        verifyBound();
        mAdapter.saveImpl(resolver, fieldName);
    }

    private void verifyBound() throws NotBoundException {
        if (mInstanceUri == null) {
            throw new NotBoundException();
        }
    }

    @Override
    public final A load(ContentResolver resolver, String fieldName) throws NotBoundException {
        verifyBound();
        return mAdapter.loadImpl(resolver, fieldName);
    }

    public final void save(Bundle b, String prefix) throws NotBoundException {
        mAdapter.saveImpl(b, null);
    }

    @Override
    public final A load(Bundle b, String prefix) throws NotBoundException {
        return mAdapter.loadImpl(b, prefix);
    }


    @Override
    public final void delete(ContentResolver resolver) throws NotBoundException {
        verifyBound();
        mAdapter.deleteImpl(resolver);
    }


    protected static boolean isBoundType(Type type) {
        switch (type) {
        case ARRAY:
        case MAP:
        case RECORD:
            return true;
        }
        return false;
    }

    public static boolean isNamedType(Type type) {
        switch (type) {
        case RECORD:
        case ENUM:
        case FIXED:
            return true;
        default:
            return false;
        }
    }
}
