package interdroid.vdb.avro.control.handler;

import android.net.Uri;

public interface ValueHandler {
    public Object getValue();
    public void setValue(Object value);
    public Uri getValueUri(Uri uri);
}
