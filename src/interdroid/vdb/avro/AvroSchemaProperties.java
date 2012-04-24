/*
 * Copyright (c) 2008-2012 Vrije Universiteit, The Netherlands All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Vrije Universiteit nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package interdroid.vdb.avro;

/**
 * This class holds various custom properties that our system supports.
 *
 * @author nick &lt;palmer@cs.vu.nl&gt;
 *
 */
public final class AvroSchemaProperties {

	/**
	 * Prevent construction.
	 */
	private AvroSchemaProperties() {
		// No constuction
	}

	/**
	 * A custom widget type for the schema or field.
	 */
	public static final String	UI_WIDGET	= "ui.widget";

	/**
	 * A field which is the title field.
	 */
	public static final String UI_TITLE = "ui.title";

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

	/**
	 * A custom resource to use in a list view.
	 */
	public static final String	UI_LIST_RESOURCE	= "ui.resource.list";

	/** The radius given in meters. */
	public static final String RADIUS_IN_METERS = "_RadiusInMeters";
	/** The longitude key. */
	public static final String RADIUS_LONGITUDE = "_Longitude";
	/** The latitude key. */
	public static final String RADIUS_LATITUDE = "_Latitude";
	/** The radius longitude key. */
	public static final String LONGITUDE = "_RadiusLongitude";
	/** The radius latitude key. */
	public static final String LATITUDE = "_RadiusLatitude";
	/** The altitude in meters. */
	public static final String ALTITUDE = "_Altitude";
	/** The accuracy of the measurement. */
	public static final String ACCURACY = "_Accuracy";

}
