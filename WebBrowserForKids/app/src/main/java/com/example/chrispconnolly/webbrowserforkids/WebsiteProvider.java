package com.example.chrispconnolly.webbrowserforkids;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class WebsiteProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WebsiteSpHelper mWebsiteSpHelper;
    static final int WEBSITE = 100;

    public WebsiteProvider(Context context){
        mWebsiteSpHelper = new WebsiteSpHelper(context);
    }

    public WebsiteProvider(){
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEBSITE:
                return WebsiteContract.WebsiteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEBSITE: {
                mWebsiteSpHelper.insertWebsite(values.getAsString("url"));
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
            return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case WEBSITE:
                rowsDeleted = mWebsiteSpHelper.deleteWebsite(selection);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return rowsDeleted;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if(mWebsiteSpHelper == null)
            mWebsiteSpHelper = new WebsiteSpHelper(getContext());
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"urls"});
        matrixCursor.addRow(new String[]{mWebsiteSpHelper.getWebsites()});
        return matrixCursor;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WebsiteContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, WebsiteContract.PATH_WEBSITE, WEBSITE);
        matcher.addURI("content://com.example.chrispconnolly.webbrowserforkids", "websites", 100);
        matcher.addURI("com.example.chrispconnolly.webbrowserforkids", "websites", 100);
        return matcher;
    }
}


