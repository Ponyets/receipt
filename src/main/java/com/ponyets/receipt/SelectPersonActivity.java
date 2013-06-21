package com.ponyets.receipt;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: panmingwei
 * Date: 13-6-20
 * Time: 下午5:41
 */
public class SelectPersonActivity extends BaseActivity {
    @Override
    public void finish() {
        ListFragment listFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        SparseBooleanArray sa = listFragment.getListView().getCheckedItemPositions();
        ListAdapter adapter = listFragment.getListAdapter();
        ArrayList<Person> persons = new ArrayList<Person>();
        for (int i = 0, n = adapter.getCount(); i < n; i++) {
            if (sa.get(i)) {
                persons.add((Person) adapter.getItem(i));
            }
        }
        Intent intent = new Intent();
        intent.putExtra("persons", persons);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_person);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_person, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_person) {
            DialogFragment fragment = new AddPersonDialogFragment();
            fragment.show(getSupportFragmentManager(), "add_person");
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public static class PersonListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(getActivity(), Uri.withAppendedPath(ReceiptProvider.URI, "people"), null, null, null, null);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            List<Person> persons = new ArrayList<Person>();
            for (; cursor.moveToNext(); ) {
                Person person = new Person();
                person.id = cursor.getLong(cursor.getColumnIndex("_id"));
                person.name = cursor.getString(cursor.getColumnIndex("name"));
                persons.add(person);
            }
            if (getListAdapter() instanceof PersonAdapter) {
                ((PersonAdapter) getListAdapter()).setPersons(persons);
            } else {
                PersonAdapter adapter = new PersonAdapter(android.R.layout.simple_list_item_multiple_choice);
                adapter.setPersons(persons);
                setListAdapter(adapter);
                long[] selectIds = getActivity().getIntent().getLongArrayExtra("personIds");
                for (int i = 0, n = adapter.getCount(); i < n; i++) {
                    for (long id : selectIds) {
                        if (adapter.getItemId(i) == id) {
                            getListView().setItemChecked(i, true);
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
        }

    }

    public static class AddPersonDialogFragment extends SherlockDialogFragment implements View.OnClickListener, EditText.OnEditorActionListener {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_add_person, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((EditText) view.findViewById(R.id.name)).setOnEditorActionListener(this);
            view.findViewById(R.id.ok).setOnClickListener(this);
        }

        private void addPerson() {
            EditText nameEditText = (EditText) getView().findViewById(R.id.name);
            String name = nameEditText.getText().toString();
            if (TextUtils.isEmpty(name)) {
                nameEditText.setError(getString(R.string.cannot_be_empty));
                return;
            }
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            getActivity().getContentResolver().insert(Uri.withAppendedPath(ReceiptProvider.URI, "people"), cv);
            dismiss();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.setTitle(R.string.add_person);
            return dialog;
        }

        @Override
        public void onClick(View view) {
            addPerson();
        }

        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            addPerson();
            return true;
        }
    }

}
