package com.ponyets.receipt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created with IntelliJ IDEA.
 * User: panmingwei
 * Date: 13-6-20
 * Time: 下午2:40
 */
public class ReceiptSqliteOpenHelper extends SQLiteOpenHelper {
    public ReceiptSqliteOpenHelper(Context context) {
        super(context, "receipt", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table people (_id integer primary key autoincrement, name text);");
        db.execSQL("create table receipt (_id integer primary key autoincrement, description text, payer integer, amount real, time long);");
        db.execSQL("create table relation (_id integer primary key autoincrement, people_id integer, receipt_id integer, credit real);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
