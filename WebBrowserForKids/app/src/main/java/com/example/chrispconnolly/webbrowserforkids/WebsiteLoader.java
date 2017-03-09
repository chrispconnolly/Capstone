package com.example.chrispconnolly.webbrowserforkids;

import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;

public class WebsiteLoader extends CursorLoader {
    public WebsiteLoader(Context context) {
        super(context);
    }

    @Override
    public Cursor loadInBackground() {
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = WebsiteContract.WebsiteEntry.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,
                null, null, null, null);
        return cursor;
    }
}
