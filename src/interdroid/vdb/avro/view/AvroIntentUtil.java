package interdroid.vdb.avro.view;

import android.app.Activity;
import android.content.Intent;

public class AvroIntentUtil {

	/**
	 * Launch an intent in the CATEGORY_DEFAULT.
	 * @param activity The activity to launch from
	 * @param intent The intent to launch
	 */
	public static void launchDefaultIntent(Activity activity,
			Intent intent) {
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		activity.startActivity(intent);
	}

	/**
	 * Launches the AvroBaseEditor activity with the given intent.
	 * @param activity The activity to launch from
	 * @param editIntent
	 */
	public static void launchEditIntent(Activity activity, Intent editIntent) {
			editIntent.addCategory(Intent.CATEGORY_DEFAULT);
			editIntent.setClass(activity, AvroBaseEditor.class);
			activity.startActivity(editIntent);
	}

}
