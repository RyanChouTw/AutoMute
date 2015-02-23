package com.hypertec.apps.automute.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AutoMuteDatabaseHelper extends SQLiteOpenHelper {

    public final static String TAG = "AutoMute";

    private static final int VERSION_1 = 1;
    
    static final String DATABASE_NAME = "rules.db";
    static final String RULES_TABLE_NAME = "rule_templates";

    private static void createRulesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + RULES_TABLE_NAME + " (" +
                AutoMuteContract.RulesColumns._ID + " INTEGER PRIMARY KEY," +
                AutoMuteContract.RulesColumns.TITLE + " TEXT NOT NULL, " +
                AutoMuteContract.RulesColumns.START_YEAR + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.START_MONTH + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.START_DAY + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.START_HOUR + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.START_MINUTE + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.END_YEAR + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.END_MONTH + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.END_DAY + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.END_HOUR + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.END_MINUTE + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.DAYS_OF_WEEK + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.ENABLED + " INTEGER NOT NULL, " +
                AutoMuteContract.RulesColumns.VIBRATE + " INTEGER NOT NULL DEFAULT 0);");
        Log.i(TAG, "Rules Table created");
    }
    

    public AutoMuteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	createRulesTable(db);	
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	Log.v(TAG, "Upgrading automute database from version " + oldVersion + " to " + newVersion);
    }

    long fixRuleInsert(ContentValues values) {
        // Why are we doing this? Is this not a programming bug if we try to
        // insert an already used id?
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long rowId = -1;
        try {
            // Check if we are trying to re-use an existing id.
            Object value = values.get(AutoMuteContract.RulesColumns._ID);
            if (value != null) {
                long id = (Long) value;
                if (id > -1) {
                    final Cursor cursor = db.query(RULES_TABLE_NAME,
                            new String[]{AutoMuteContract.RulesColumns._ID},
                            AutoMuteContract.RulesColumns._ID + " = ?",
                            new String[]{id + ""}, null, null, null);
                    if (cursor.moveToFirst()) {
                        // Record exists. Remove the id so sqlite can generate a new one.
                        values.putNull(AutoMuteContract.RulesColumns._ID);
                    }
                }
            }

            rowId = db.insert(RULES_TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (rowId < 0) {
            throw new SQLException("Failed to insert row");
        }

        Log.v(TAG, "Added rule rowId = " + rowId);

        return rowId;
    }

}
