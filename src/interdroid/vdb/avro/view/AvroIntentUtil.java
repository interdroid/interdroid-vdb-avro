package interdroid.vdb.avro.view;

import android.app.Activity;
import android.content.Intent;

/**
 * Some utilities for dealing with intents with the avro components.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public final class AvroIntentUtil {

	/**
	 * Prevent construction of this utility class.
	 */
	private AvroIntentUtil() {
		// No constuction
	}

	/**
	 * Launch an intent in the CATEGORY_DEFAULT.
	 * @param activity The activity to launch from
	 * @param intent The intent to launch
	 */
	public static void launchDefaultIntent(final Activity activity,
			final Intent intent) {
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		activity.startActivity(intent);
	}

	/**
	 * Launches the AvroBaseEditor activity with the given intent.
	 * @param activity The activity to launch from
	 * @param editIntent the intent to be launched
	 */
	public static void launchEditIntent(final Activity activity,
			final Intent editIntent) {
			editIntent.addCategory(Intent.CATEGORY_DEFAULT);
			editIntent.setClass(activity, AvroBaseEditor.class);
			activity.startActivity(editIntent);
	}

}
