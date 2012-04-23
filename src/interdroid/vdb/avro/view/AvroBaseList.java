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
/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package interdroid.vdb.avro.view;

import interdroid.util.ToastOnUI;
import interdroid.util.view.AsyncTaskWithProgressDialog;
import interdroid.vdb.Actions;
import interdroid.vdb.avro.R;
import interdroid.vdb.avro.view.factory.AvroViewFactory;
import interdroid.vdb.content.EntityUriMatcher;
import interdroid.vdb.content.EntityUriMatcher.UriMatch;
import interdroid.vdb.content.avro.AvroProviderRegistry;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Displays a list of avros. Will display avros from the {@link Uri}
 * provided in the intent if there is one, otherwise defaults to displaying the
 * contents of the {@link NotePadProvider}
 */
public class AvroBaseList extends ListActivity {
	/** Access to logger. */
	private static final Logger LOG =
			LoggerFactory.getLogger(AvroBaseList.class);

	/** Item delete menu. */
	public static final int MENU_ITEM_DELETE = Menu.FIRST;
	/** Item insert menu. */
	public static final int MENU_ITEM_INSERT = MENU_ITEM_DELETE + 1;
	/** Item commit menu. */
	public static final int MENU_ITEM_COMMIT = MENU_ITEM_INSERT + 1;
	/** Item edit menu. */
	public static final int MENU_ITEM_EDIT = MENU_ITEM_COMMIT + 1;

	/** The branch uri we are listing. */
	private Uri mBranchUri;
	/** Are we in read only mode? */
	private boolean mReadOnly = true;
	/** The schema for the records we are listing. */
	private Schema mSchema;

	/** Construct a list activity. */
	public AvroBaseList() {
		LOG.debug("Constructed AvroBaseList: " + this + ":");
	}

	/**
	 * Construct a list activity for the given schema and uri.
	 * @param schema the schema for the list type
	 * @param defaultUri the uri with the data for the list
	 */
	protected AvroBaseList(final Schema schema, final Uri defaultUri) {
		if (!setup(schema, defaultUri)) {
			ToastOnUI.show(this, "Invalid base type.", Toast.LENGTH_LONG);
			finish();
		}
	}

	/**
	 * Construct a list activity for the given schema.
	 * @param schema the schema for the list type
	 */
	protected AvroBaseList(final Schema schema) {
		this(schema, null);
	}

	/**
	 * Setup for listing the given type.
	 * @param schema the schema for the type
	 * @param defaultUri the uri with the data
	 */
	protected final boolean setup(final Schema schema, final Uri defaultUri) {
		if (schema.getType() != Schema.Type.RECORD) {
			return false;
		}
		mSchema = schema;

		if (defaultUri == null) {
			mBranchUri = Uri.parse("content://" + schema.getNamespace()
					+ "/branches/master/" + schema.getName());
			LOG.debug("Using default URI: {}", mBranchUri);
		} else {
			mBranchUri = defaultUri;
		}
		return true;
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Start the GIT service in the background.
		startService(new Intent("interdroid.vdb.GIT_SERVICE"));

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		// If no data was given in the intent (because we were started
		// as a MAIN activity), then use our default content provider.
		Intent intent = getIntent();
		if (mSchema != null || intent.getData() == null) {
			intent.setData(mBranchUri);
		} else {
			// We need a uri and a schema in the intent extras then.
			Uri defaultUri = intent.getData();
			if (defaultUri == null) {
				throw new IllegalArgumentException("A Uri is required.");
			}
			String schemaJson = intent.getStringExtra(AvroBaseEditor.SCHEMA);
			Schema schema = null;
			if (schemaJson == null) {
				LOG.debug("Checking for schema for: {}", defaultUri);
				schema = AvroProviderRegistry.getSchema(this, defaultUri);
				if (schema == null) {
					ToastOnUI.show(this,
							"Schema not found.",
							Toast.LENGTH_LONG);
					finish();
					return;
				}
			} else {
				schema = Schema.parse(schemaJson);
			}

			LOG.debug("Setting up: {} {}", defaultUri, schema);
			if (!setup(schema, defaultUri)) {
				ToastOnUI.show(this, "Invalid base type.", Toast.LENGTH_LONG);
				finish();
				return;
			}
		}

		UriMatch match = EntityUriMatcher.getMatch(intent.getData());
		if (!match.isCheckout()) {
			Toast.makeText(this, "Invalid URI.", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		// In case it's a branch/remote/repository, add the table
		if (match.entityName == null) {
			match.entityName = mSchema.getName();
			getIntent().setData(match.buildUri());
		}

		// For write checkouts we hold the branch Uri
		// for launching the commit activity
		mReadOnly = match.isReadOnlyCheckout();
		mBranchUri = match.getCheckoutUri();
		if (mReadOnly) {
			Toast.makeText(this, "Read only", Toast.LENGTH_LONG).show();
		}

		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);

		if (intent.getAction() == Intent.ACTION_PICK) {
			LOG.debug("In pick mode.");
			// We are canceled if they back out without picking.
			setResult(RESULT_CANCELED);
		}

		setTitle(AvroViewFactory.toTitle(mSchema));

		new InitTask().execute(getIntent());
	}

	/**
	 * Initialization task which loads the list.
	 * @author nick &lt;palmer@cs.vu.nl&gt;
	 *
	 */
	private class InitTask
		extends AsyncTaskWithProgressDialog<Object, Void, Void> {

		/**
		 * Construct the dialog.
		 */
		public InitTask() {
			super(AvroBaseList.this, getString(R.string.label_loading),
					getString(R.string.label_wait));
		}

		@Override
		protected Void doInBackground(final Object... params) {

			AvroBaseList.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final CursorAdapter adapter =
							new AvroListAdapter(AvroBaseList.this,
									mSchema, getIntent().getData());
					setListAdapter(adapter);
					TextView empty = new TextView(AvroBaseList.this);
					empty.setText("Press Menu -> Insert to add to the list.");
					empty.setGravity(Gravity.CENTER);
					empty.setId(android.R.id.empty);
					getListView().setEmptyView(empty);
					((ViewGroup) getListView().getParent()).addView(empty);

				}
			});
			return null;
		}

	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		if (!mReadOnly) {
			menu.add(0, MENU_ITEM_INSERT, 0, "Insert " + mSchema.getName())
			.setShortcut('3', 'a')
			.setIcon(android.R.drawable.ic_menu_add);

			menu.add(1, MENU_ITEM_COMMIT, 0, "Commit")
			.setShortcut('9', 'c')
			.setIcon(android.R.drawable.ic_menu_save);
		}
		// Generate any additional actions that can be performed on the
		// overall list.  In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, AvroBaseList.class),
				null, intent, 0, null);

		return true;
	}

	@Override
	public final boolean onPrepareOptionsMenu(final Menu menu) {
		super.onPrepareOptionsMenu(menu);
		final boolean haveItems = getListAdapter().getCount() > 0;

		// If there are any items in the list (which implies that one of
		// them is selected), then we need to generate the actions that
		// can be performed on the current selection. This will be a combination
		// of our own specific actions along with any extensions that can be
		// found.
		if (haveItems && !mReadOnly) {
			LOG.debug("Selected item is: {}", getSelectedItemId());
			// This is the selected item.
			Uri uri = ContentUris.withAppendedId(getIntent().getData(),
					getSelectedItemId());

			// Build menu...  always starts with the EDIT action...
			Intent[] specifics = new Intent[1];
			specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
			MenuItem[] items = new MenuItem[1];

			// ... is followed by whatever other actions are available...
			Intent intent = new Intent(null, uri);
			intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
			menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null,
					specifics, intent, 0, items);

			// Give a shortcut to the edit action.
			if (items[0] != null) {
				items[0].setShortcut('1', 'e');
			}
		} else {
			menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
		}

		return true;
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_INSERT:
			// Launch activity to insert a new item
			Intent i = new Intent(Intent.ACTION_INSERT, getIntent().getData());
			// We need a class name since we haven't registered
			// AvroBaseEdit with all URIs.
			i.setClassName(this, AvroBaseEditor.class.getName());
			startActivity(i);
			return true;
		case MENU_ITEM_COMMIT:
			startActivity(new Intent(Actions.ACTION_COMMIT, mBranchUri));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public final void onCreateContextMenu(final ContextMenu menu,
			final View view, final ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			LOG.error("bad menuInfo", e);
			return;
		}

		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			LOG.debug("Requested item not avaialble.");
			return;
		}

		// Setup the menu header
		menu.setHeaderTitle(
				((AvroListAdapter) getListAdapter()).getTitle(cursor));

		if (!mReadOnly) {
			// Add a menu item to delete the item
			menu.add(0, MENU_ITEM_DELETE, 0, "Delete " + mSchema.getName());
			menu.add(0, MENU_ITEM_EDIT, 1, "Edit " + mSchema.getName());
			// This is the selected item.
			Uri uri = ContentUris.withAppendedId(getIntent().getData(),
					info.id);
			Intent intent = new Intent(null, uri);
			intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
			Intent[] specifics = new Intent[1];
			specifics[0] = new Intent(Actions.ACTION_INIT_DB, uri);
			MenuItem[] items = new MenuItem[1];
			menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE,
					0, 0, null, specifics, intent, 0,
					items);
		}
	}

	@Override
	public final boolean onContextItemSelected(final MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			LOG.error("bad menuInfo", e);
			return false;
		}

		switch (item.getItemId()) {
		case MENU_ITEM_DELETE:
			deleteItem(info);
			return true;
		case MENU_ITEM_EDIT:
			startEditActivity(info);
			return true;
		default:
			return false;
		}
	}

	/**
	 * Delete an item from the list.
	 * @param info the item to delete
	 */
	private void deleteItem(
			final AdapterView.AdapterContextMenuInfo info) {
		// Delete the note that the context menu is for
		Uri noteUri = ContentUris.withAppendedId(
				getIntent().getData(), info.id);
		getContentResolver().delete(noteUri, null, null);
	}

	/**
	 * Starts an edit activity for the given item.
	 * @param info the tiem to edit
	 */
	private void startEditActivity(
			final AdapterView.AdapterContextMenuInfo info) {
		Uri noteUri =
				ContentUris.withAppendedId(getIntent().getData(), info.id);
		Intent i = new Intent(Intent.ACTION_EDIT, noteUri);
		i.setClassName(this, AvroBaseEditor.class.getName());
		startActivity(i);
	}

	@Override
	protected final void onListItemClick(final ListView l, final View v,
			final int position, final long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action)
				|| Intent.ACTION_GET_CONTENT.equals(action)) {
			// The caller is waiting for us to return a note selected by
			// the user.  The have clicked on one, so return it now.
			LOG.debug("Setting Result URI: {}",
					getContentResolver().getType(uri));
			setResult(RESULT_OK, new Intent().setData(uri));
		} else {
			// Launch activity to view/edit the currently selected item
			LOG.debug("Launching edit for: {}",
					getContentResolver().getType(uri));
			// TODO: We should try to find a custom one here as well.
			Intent editIntent = new Intent(Intent.ACTION_EDIT, uri);
			editIntent.setClassName(this, AvroBaseEditor.class.getName());
			startActivity(editIntent);
		}
	}
}
