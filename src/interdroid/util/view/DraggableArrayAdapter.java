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
package interdroid.util.view;

import interdroid.vdb.avro.R;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * An ArrayAdapter which implements DraggableAdapter for use in a
 * DraggableListView
 * @author nick <palmer@cs.vu.nl>
 *
 */
public class DraggableArrayAdapter<T> extends ArrayAdapter<T> implements DraggableAdapter {
	/** Logger interface **/
	private static final Logger logger = LoggerFactory
			.getLogger(DraggableArrayAdapter.class);

	/** True if this supports removing. */
	protected boolean mRemoveable = true;

	/**
	 * Build an adapter
	 * @param context the context the adapter runs in
	 * @param content the objects to represent in the list view
	 */
	public DraggableArrayAdapter(Context context, List<T> content) {
		super(context, R.layout.draggable_item, R.id.drag_label, content);
	}

	/**
	 * The layout specified must include a view with IDs interdroid.util.R.drag_handle and interdroid.util.R.remove_button.
	 */
	public DraggableArrayAdapter(Context context, int layout, int itemID, List<T> content) {
		super(context, layout, itemID, content);
	}

	/**
	 * Returns the view for the specific item.
	 * @param position the position of the item to be visualized
	 * @param convertView the view to be converted if possible
	 * @param parent the parent ViewGroup for the view
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = super.getView(position, convertView, parent);

		if (!mRemoveable) {
			convertView.findViewById(R.id.remove_button).setVisibility(View.GONE);
		}

		return convertView;
	}

	/**
	 * Toggles if items in this adapter may be removed
	 * @param b true if items are removable
	 */
	public void setRemoveable(boolean b) {
		mRemoveable = b;
	}

	/**
	 * Called when an item is removed.
	 * @param which the index of the removed item
	 */
	public void onRemove(int which) {
		logger.debug("onRemove: {}", which);
		if (mRemoveable && which >= 0 && which < getCount()) {
			remove(getItem(which));
			notifyDataSetChanged();
		}
	}

	/**
	 * Called when an item is dragged and dropped
	 * @param from the item being moved
	 * @param to where it should be moved to
	 */
	public void onDrop(int from, int to) {
		logger.debug("onDrop: {} {}", from, to);
		T item = getItem(from);
		remove(item);
		insert(item, from < to ? to - 1 : to);
		notifyDataSetChanged();
	}

}
