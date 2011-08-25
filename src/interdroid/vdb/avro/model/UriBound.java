package interdroid.vdb.avro.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;

public interface UriBound<A> {

    public Uri getInstanceUri();

    public void setInstanceUri(Uri uri);

    public void save(ContentResolver resolver, String fieldName) throws NotBoundException;

    public A load(ContentResolver resolver, String fieldName) throws NotBoundException;

    public void save(Bundle outState, String prefix);

    public A load(Bundle b, String prefix);

    public void delete(ContentResolver  resolver) throws NotBoundException;

}
