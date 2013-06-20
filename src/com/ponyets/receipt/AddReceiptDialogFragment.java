package com.ponyets.receipt;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockDialogFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: panmingwei
 * Date: 13-6-20
 * Time: 下午4:13
 */
public class AddReceiptDialogFragment extends SherlockDialogFragment implements View.OnClickListener {
    private List<Person> persons;

    private static String getMemberIds(List<Person> persons) {
        ArrayList<Long> ids = new ArrayList<Long>(persons.size());
        for (Person person : persons) {
            ids.add(person.id);
        }
        String s = ids.toString();
        return s.substring(1, s.length() - 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_receipt, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.ok).setOnClickListener(this);
        view.findViewById(R.id.select_member).setOnClickListener(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.add_receipt);
        return dialog;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ok) {
            EditText descriptionEditText = (EditText) getView().findViewById(R.id.description);
            String description = descriptionEditText.getText().toString();
            if (TextUtils.isEmpty(description)) {
                descriptionEditText.setError(getString(R.string.cannot_be_empty));
                return;
            }
            EditText amountEditText = (EditText) getView().findViewById(R.id.amount);
            String amount = amountEditText.getText().toString();
            if (TextUtils.isEmpty(amount)) {
                amountEditText.setError(getString(R.string.cannot_be_empty));
                return;
            }
            if (persons == null || persons.size() <= 1) {
                Toast.makeText(getActivity(), R.string.need_to_select_some_person, Toast.LENGTH_SHORT).show();
                return;
            }
            long payer = ((Spinner) getView().findViewById(R.id.payer)).getSelectedItemId();
            if (payer < 0) {
                Toast.makeText(getActivity(), R.string.need_to_assign_payer, Toast.LENGTH_SHORT).show();
                return;
            }
            ContentValues cv = new ContentValues();
            cv.put("description", description);
            cv.put("amount", amount);
            cv.put("members", getMemberIds(persons));
            cv.put("payer", payer);
            cv.put("time", System.currentTimeMillis());
            getActivity().getContentResolver().insert(Uri.withAppendedPath(ReceiptProvider.URI, "receipt"), cv);
            dismiss();
        } else if (view.getId() == R.id.select_member) {
            Intent intent = new Intent(getActivity(), SelectPersonActivity.class);
            long[] personsIds = new long[persons == null ? 0 : persons.size()];
            if (persons != null) {
                for (int i = 0, n = persons.size(); i < n; i++) {
                    personsIds[i] = persons.get(i).id;
                }
            }
            intent.putExtra("personIds", personsIds);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        persons = (ArrayList<Person>) data.getSerializableExtra("persons");
        StringBuilder builder = new StringBuilder();
        for (Person person : persons) {
            builder.append(person.name).append(" ");
        }
        ((TextView) getView().findViewById(R.id.members)).setText(builder.toString());
        PersonAdapter adapter = new PersonAdapter(android.R.layout.simple_dropdown_item_1line);
        adapter.setPersons(persons);
        ((Spinner) getView().findViewById(R.id.payer)).setAdapter(adapter);
    }
}
