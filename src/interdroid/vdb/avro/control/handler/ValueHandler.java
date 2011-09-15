package interdroid.vdb.avro.control.handler;

import interdroid.vdb.avro.model.NotBoundException;
import android.net.Uri;

public interface ValueHandler {
	public Object getValue();
	public void setValue(Object value);
	public Uri getValueUri() throws NotBoundException;
	public String getFieldName();
}
