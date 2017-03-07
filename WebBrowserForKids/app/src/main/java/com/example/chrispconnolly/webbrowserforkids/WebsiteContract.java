package com.example.chrispconnolly.webbrowserforkids;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class WebsiteContract {
    public static final String CONTENT_AUTHORITY = "com.example.chrispconnolly.webbrowserforkids";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEBSITE = "websites";

    public static final class WebsiteEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEBSITE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEBSITE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEBSITE;
        public static final String TABLE_NAME = "websites";
    }
}
