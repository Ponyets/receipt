package com.ponyets.receipt;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;

/**
 * Created with IntelliJ IDEA.
 * User: panmingwei
 * Date: 13-6-20
 * Time: 下午3:52
 */
public class CreditListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), Uri.withAppendedPath(ReceiptProvider.URI, "credit"), null, null, null, "time DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (getListAdapter() instanceof CursorAdapter) {
            ((CursorAdapter) getListAdapter()).swapCursor(cursor);
        } else {
            setListAdapter(new CreditAdapter(getActivity(), cursor));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (getListAdapter() instanceof CursorAdapter) {
            ((CursorAdapter) getListAdapter()).swapCursor(null);
        }
    }

    private static class CreditAdapter extends CursorAdapter {
        private CreditAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(R.layout.listitem_credit, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view.findViewById(R.id.name)).setText(cursor.getString(cursor.getColumnIndex("name")));
            ((TextView) view.findViewById(R.id.credit)).setText(cursor.getString(cursor.getColumnIndex("credit_sum")));
        }
    }
}
