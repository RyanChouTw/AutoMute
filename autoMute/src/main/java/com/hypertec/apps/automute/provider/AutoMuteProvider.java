package com.hypertec.apps.automute.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AutoMuteProvider extends ContentProvider {
    public final static String TAG = "AutoMute";

    private AutoMuteDatabaseHelper mOpenHelper;
    private static final int RULES = 1;
    private static final int RULES_ID = 2;
    
    private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURLMatcher.addURI(AutoMuteContract.AUTHORITY, "rules", RULES);
        sURLMatcher.addURI(AutoMuteContract.AUTHORITY, "rules/#", RULES_ID);
    }
    
    @Override
    public boolean onCreate() {
	mOpenHelper = new AutoMuteDatabaseHelper(getContext());
	return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
	    String[] selectionArgs, String sortOrder) {
	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
	
	// Generate the body of the query
	int match = sURLMatcher.match(uri);
	switch (match) {
    	case RULES:
    	    queryBuilder.setTables(AutoMuteDatabaseHelper.RULES_TABLE_NAME);
	    break;
        case RULES_ID:
            queryBuilder.setTables(AutoMuteDatabaseHelper.RULES_TABLE_NAME);
            queryBuilder.appendWhere(AutoMuteContract.RulesColumns._ID + "=");
            queryBuilder.appendWhere(uri.getLastPathSegment());
            break;
	default:
	    throw new IllegalArgumentException("Unknown URL " + uri);
	}
	
	SQLiteDatabase db = mOpenHelper.getReadableDatabase();
	Cursor ret = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	
	if (ret == null) {
	    Log.e(TAG, "Alarms.query: failed");
	}
	else {
	    ret.setNotificationUri(getContext().getContentResolver(), uri);
	}

	return ret;
    }

    @Override
    public String getType(Uri uri) {
        int match = sURLMatcher.match(uri);
        switch (match) {
            case RULES:
                return "vnd.android.cursor.dir/rules";
            case RULES_ID:
        	return "vnd.android.cursor.item/rules";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId;
        switch (sURLMatcher.match(uri)) {
            case RULES:
                rowId = mOpenHelper.fixRuleInsert(values);
                break;
            default:
                throw new IllegalArgumentException("Cannot insert from URL: " + uri);
        }

        Uri uriResult = ContentUris.withAppendedId(AutoMuteContract.RulesColumns.CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(uriResult, null);
        return uriResult;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        String primaryKey;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sURLMatcher.match(uri)) {
            case RULES:
                count = db.delete(AutoMuteDatabaseHelper.RULES_TABLE_NAME, selection, selectionArgs);
                break;
            case RULES_ID:
        	primaryKey = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = AutoMuteContract.RulesColumns._ID + "=" + primaryKey;
                } else {
                    selection = AutoMuteContract.RulesColumns._ID + "=" + primaryKey +
                            " AND (" + selection + ")";
                }
                count = db.delete(AutoMuteDatabaseHelper.RULES_TABLE_NAME, selection, selectionArgs);
        	break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
	    String[] selectionArgs) {
        int count;
        String ruleId;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sURLMatcher.match(uri)) {
            case RULES_ID:
            {
                ruleId = uri.getLastPathSegment();
                count = db.update(AutoMuteDatabaseHelper.RULES_TABLE_NAME, values,
            	    AutoMuteContract.RulesColumns._ID + "=" + ruleId,
            	    null);        	
            }
            break;
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + uri);
            }
        }
        
        Log.v(TAG, "*** notifyChange() id: " + ruleId + " url " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
