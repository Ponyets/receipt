package com.ponyets.receipt;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: panmingwei
 * Date: 13-6-20
 * Time: 下午9:34
 */
public class RepayDialogFragment extends SherlockDialogFragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_repay, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Person payer = (Person) ((Spinner) getView().findViewById(R.id.payer)).getSelectedItem();
        Person receiver = (Person) ((Spinner) getView().findViewById(R.id.receiver)).getSelectedItem();
        if (payer == null || receiver == null) {
            return;
        }
        if (payer.id == receiver.id) {
            Toast.makeText(getActivity(), R.string.payer_receiver_cannot_be_same, Toast.LENGTH_SHORT).show();
            return;
        }
        EditText amountEditText = (EditText) getView().findViewById(R.id.amount);
        String amount = amountEditText.getText().toString();
        if (TextUtils.isEmpty(amount)) {
            amountEditText.setError(getString(R.string.cannot_be_empty));
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put("description", getString(R.string.repay_description_format, payer.name, receiver.name, Double.valueOf(amount)));
        cv.put("amount", Double.valueOf(amount));
        cv.put("payer", payer.id);
        cv.put("receiver", receiver.id);
        getActivity().getContentResolver().insert(Uri.withAppendedPath(ReceiptProvider.URI, "receipt/repay"), cv);
        dismiss();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), Uri.withAppendedPath(ReceiptProvider.URI, "people"), null, null, null, null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.repay);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        PersonAdapter payerAdapter = new PersonAdapter(android.R.layout.simple_dropdown_item_1line);
        payerAdapter.setPersons(persons);
        PersonAdapter receiverAdapter = new PersonAdapter(android.R.layout.simple_dropdown_item_1line);
        receiverAdapter.setPersons(persons);
        ((Spinner) getView().findViewById(R.id.payer)).setAdapter(payerAdapter);
        ((Spinner) getView().findViewById(R.id.receiver)).setAdapter(receiverAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}
