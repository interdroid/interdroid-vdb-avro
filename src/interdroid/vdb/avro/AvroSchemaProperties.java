package interdroid.vdb.avro;

/**
 * This class holds various custom properties that our system supports.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public interface AvroSchemaProperties {

	/**
	 * A custom widget type for the schema or field.
	 */
	public static final String	UI_WIDGET	= "ui.widget";

	/**
	 * A custom resource to inflate as the widget.
	 */
	public static final String	UI_RESOURCE	= "ui.resource";

	/**
	 * The label for a field or schema.
	 */
	public static final String	UI_LABEL	= "ui.label";

	/**
	 * Set to false to hide a UI element.
	 */
	public static final String	UI_VISIBLE	= "ui.visible";

	/**
	 * Set to false to disable editing of a given element.
	 */
	public static final String	UI_ENABLED	= "ui.enabled";

}
