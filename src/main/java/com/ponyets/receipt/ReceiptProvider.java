package com.ponyets.receipt;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created with IntelliJ IDEA.
 * User: panmingwei
 * Date: 13-6-20
 * Time: 下午2:40
 */
public class ReceiptProvider extends ContentProvider {
    public static final String AUTHORITY = "com.ponyets.receipt";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY);
    private static final UriMatcher MATCHER = new UriMatcher(0);
    private static final int RECEIPT = 1000;
    private static final int RECEIPT_ID = 1001;
    private static final int RECEIPT_REPAY = 1002;
    private static final int PEOPLE = 2000;
    private static final int PEOPLE_ID = 2001;
    private static final int CREDIT = 3000;
    private SQLiteDatabase db;
    private ContentResolver cr;

    static {
        MATCHER.addURI(AUTHORITY, "receipt", RECEIPT);
        MATCHER.addURI(AUTHORITY, "receipt/#", RECEIPT_ID);
        MATCHER.addURI(AUTHORITY, "receipt/repay", RECEIPT_REPAY);
        MATCHER.addURI(AUTHORITY, "people", PEOPLE);
        MATCHER.addURI(AUTHORITY, "people/#", PEOPLE_ID);
        MATCHER.addURI(AUTHORITY, "credit", CREDIT);
    }

    @Override
    public boolean onCreate() {
        cr = getContext().getContentResolver();
        db = new ReceiptSqliteOpenHelper(getContext()).getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (MATCHER.match(uri)) {
            case RECEIPT:
                cursor = db.rawQuery("select receipt._id as _id,description, amount, name,time from receipt inner join people on receipt.payer=people._id", null);
                break;
            case PEOPLE:
                qb.setTables("people");
                cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CREDIT:
                cursor = db.rawQuery("select people._id as _id, name, credit_sum from people inner join (select people_id, sum(credit) as credit_sum from relation group by people_id) on _id=people_id", null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        cursor.setNotificationUri(cr, uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long id = -1;
        switch (MATCHER.match(uri)) {
            case RECEIPT:
                db.beginTransaction();
                try {
                    String memberStr = contentValues.getAsString("members");
                    long payer = contentValues.getAsLong("payer");
                    double amount = contentValues.getAsDouble("amount");
                    contentValues.remove("members");
                    id = db.insert("receipt", null, contentValues);
                    String[] members = memberStr.split(",");
                    double burden = amount / members.length;
                    for (String member : members) {
                        ContentValues relationCv = new ContentValues();
                        long member_id = Long.valueOf(member.trim());
                        relationCv.put("people_id", member_id);
                        relationCv.put("receipt_id", id);
                        if (member_id == payer) {
                            relationCv.put("credit", amount - burden);
                        } else {
                            relationCv.put("credit", -burden);
                        }
                        db.insert("relation", null, relationCv);
                    }
                    db.setTransactionSuccessful();
                    cr.notifyChange(ContentUris.withAppendedId(uri, id), null);
                    cr.notifyChange(Uri.withAppendedPath(URI, "credit"), null);
                    return ContentUris.withAppendedId(uri, id);
                } finally {
                    db.endTransaction();
                }
            case RECEIPT_REPAY:
                db.beginTransaction();
                try {
                    long payer = contentValues.getAsLong("payer");
                    long receiver = contentValues.getAsLong("receiver");
                    double amount = contentValues.getAsDouble("amount");
                    contentValues.remove("receiver");
                    id = db.insert("receipt", null, contentValues);
                    ContentValues payerCv = new ContentValues();
                    payerCv.put("receipt_id", id);
                    payerCv.put("credit", amount);
                    payerCv.put("people_id", payer);
                    db.insert("relation", null, payerCv);
                    ContentValues receiverCv = new ContentValues();
                    receiverCv.put("receipt_id", id);
                    receiverCv.put("credit", -amount);
                    receiverCv.put("people_id", receiver);
                    db.insert("relation", null, receiverCv);
                    db.setTransactionSuccessful();
                    cr.notifyChange(Uri.withAppendedPath(URI, "receipt/" + id), null);
                    cr.notifyChange(Uri.withAppendedPath(URI, "credit"), null);
                    return Uri.withAppendedPath(URI, "receipt/" + id);
                } finally {
                    db.endTransaction();
                }
            case PEOPLE:
                id = db.insert("people", null, contentValues);
                cr.notifyChange(ContentUris.withAppendedId(uri, id), null);
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        switch (MATCHER.match(uri)) {
            case RECEIPT_ID:
                long receiptId = Long.valueOf(uri.getLastPathSegment());
                db.beginTransaction();
                try {
                    db.delete("relation", "receipt_id = " + receiptId, null);
                    int count = db.delete("receipt", "_id = " + receiptId, null);
                    db.setTransactionSuccessful();
                    cr.notifyChange(uri, null);
                    cr.notifyChange(Uri.withAppendedPath(URI, "credit"), null);
                    return count;
                } finally {
                    db.endTransaction();
                }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
