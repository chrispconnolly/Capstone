package com.example.chrispconnolly.webbrowserforkids;

import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<Cursor> {
    ListView mWebsiteListView;
    String[] mWebsites;
    TextView mAddWebsitesView, mCurfewTextView, mTimeLimitTextView, mPasscodeTextView;
    CheckBox mParentModeCheckBox;
    WebsiteProvider mWebsiteProvider;
    WebsiteSpHelper mWebsiteSpHelper;
    WebsiteLoader mWebsiteLoader;
    AdView mAdView;
    JSONArray mWebsiteJsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getLoaderManager().initLoader(0, null, this);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mWebsiteProvider = new WebsiteProvider(this);
        mWebsiteSpHelper = new WebsiteSpHelper(this);
        mWebsiteLoader = new WebsiteLoader(this);
        mWebsiteListView = (ListView)findViewById(R.id.websiteListView);
        mWebsiteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = mWebsites[position];
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        mAddWebsitesView = (TextView)findViewById(R.id.add_websites_textview);
        mCurfewTextView = (TextView)findViewById(R.id.curfew_textview);
        mTimeLimitTextView = (TextView)findViewById(R.id.timelimit_textview);
        mPasscodeTextView = (TextView)findViewById(R.id.set_passcode_textview);
        mParentModeCheckBox = (CheckBox)findViewById(R.id.parentMode_checkbox);

        refreshViews();
    }

    private void refreshViews(){
        mCurfewTextView.setText(getString(R.string.curfew) + " " +  mWebsiteSpHelper.getCurfew());
        mTimeLimitTextView.setText(getString(R.string.time_limit) + " " + mWebsiteSpHelper.getTimeLimit());
        String passCodeMask = (mWebsiteSpHelper.getParentPasscode() == null) ? getString(R.string.not_set) : getString(R.string.set);
        mPasscodeTextView.setText(getString(R.string.set_passcode) + " " + passCodeMask);

        boolean parentMode = mWebsiteSpHelper.getParentMode();
        mAddWebsitesView.setEnabled(parentMode);
        mCurfewTextView.setEnabled(parentMode);
        mTimeLimitTextView.setEnabled(parentMode);
        mParentModeCheckBox.setChecked(parentMode);
        mPasscodeTextView.setEnabled(parentMode || mWebsiteSpHelper.getParentPasscode() == null);

        String websites = mWebsiteSpHelper.getWebsites();
        if(websites != null) {
            mWebsites = websites.isEmpty() ? new String[]{} : websites.split(",");
            mWebsiteListView.setAdapter(new WebSiteAdapter(mWebsites));
        }
    }

    public void addWebsite(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.add_website);
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        alertDialogBuilder.setView(editText);
        alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String website = editText.getText().toString();
                ContentValues contentValues = new ContentValues();
                contentValues.put("url", website);
                mWebsiteProvider.insert(WebsiteContract.WebsiteEntry.CONTENT_URI, new ContentValues(contentValues));
                refreshViews();
                SearchWebsitesTask searchWebsitesTask = new SearchWebsitesTask();
                searchWebsitesTask.execute(website);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();
    }

    public void goBack(MenuItem menuItem){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void deleteWebsite(View view)
    {
        int position = (int)view.getTag();
        String website = mWebsites[position];
        mWebsiteProvider.delete(WebsiteContract.WebsiteEntry.CONTENT_URI, website, null);
        refreshViews();
    }

    public void setCurfew(View view){
        TimePickerDialog curfewTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                mWebsiteSpHelper.setCurfewHour(hour);
                mWebsiteSpHelper.setCurfewMinute(minute);
                refreshViews();
            }
        }, mWebsiteSpHelper.getCurfewHour(), mWebsiteSpHelper.getCurfewMinute(), true);
        curfewTimePickerDialog.show();
    }

    public void setTimeLimit(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.set_time_limit));
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        alertDialogBuilder.setView(editText);
        alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    long timeLimit = Long.parseLong(editText.getText().toString());
                    if(timeLimit < 1 || timeLimit > 24)
                        throw new Exception();
                    mWebsiteSpHelper.setTimeLimit(timeLimit);
                    refreshViews();
                }
                catch(Exception exception) {
                    Toast.makeText(getApplicationContext(), getString(R.string.invalid_time_limit), Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mParentModeCheckBox.toggle();
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();
    }

    public void setPasscode(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.set_passcode));
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alertDialogBuilder.setView(editText);
        alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mWebsiteSpHelper.setParentPasscode(editText.getText().toString());
                refreshViews();
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.show();
    }

    public void toggleParentMode(View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.enter_passcode));
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        alertDialogBuilder.setView(editText);
        alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPasscode = editText.getText().toString();
                String storedPasscode = mWebsiteSpHelper.getParentPasscode();
                if(enteredPasscode.equals(storedPasscode)) {
                    mWebsiteSpHelper.toggleParentMode();
                    refreshViews();
                }
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.passcode_incorrect), Toast.LENGTH_LONG).show();
                    mParentModeCheckBox.toggle();
                }
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mParentModeCheckBox.toggle();
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new WebsiteLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        String websites = "";
        if(cursor.getCount() > 0){
            cursor.moveToPosition(0);
            websites = cursor.getString(0);
        }
        if(websites != null) {
            mWebsites = websites.isEmpty() ? new String[]{} : websites.split(",");
            mWebsiteListView.setAdapter(new WebSiteAdapter(mWebsites));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private class WebSiteAdapter extends BaseAdapter {
        String [] mWebsites;
        LayoutInflater inflater=null;

        public WebSiteAdapter(String[] websites) {
            mWebsites = websites;
            inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {return mWebsites.length;}
        @Override
        public Object getItem(int position) {return position;}
        @Override
        public long getItemId(int position) {return position;}

        public View getView(int position, View convertView, ViewGroup parent) {
            String website = mWebsites[position];
            convertView = inflater.inflate(R.layout.website_list_item, null);
            TextView websiteTextView = (TextView)convertView.findViewById(R.id.website_textview);
            websiteTextView.setContentDescription(website);
            ImageView deleteImageView = (ImageView)convertView.findViewById(R.id.delete_imageview);
            deleteImageView.setContentDescription(getString(R.string.delete_website) + " " + website);
            deleteImageView.setTag(position);
            deleteImageView.setEnabled(mWebsiteSpHelper.getParentMode());
            websiteTextView.setText(website);
            return convertView;
        }
    }

    private class SearchWebsitesTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if(jsonObject == null)
                return;
            if(jsonObject != null || jsonObject.length() != 0)
                Toast.makeText(getApplicationContext(), getString(R.string.website_unsafe), Toast.LENGTH_LONG).show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("safebrowsing.googleapis.com")
                        .appendPath("v4")
                        .appendEncodedPath("threatMatches:find")
                        .appendQueryParameter("key", BuildConfig.GOOGLE_SAFE_BROWSING_API_KEY);
                JSONObject jsonObject = getWebsiteJson(builder.build().toString(), params[0]);
                return jsonObject;
            }
            catch (Exception e){
                Log.e("getWebsiteJson", "Error fetching JSON.");
            }
            return null;
        }
    }

    public static JSONObject getWebsiteJson(String urlString, String urlToCheck)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
            outputStreamWriter.write(
                    "{\n" +
                    "    \"client\": {\n" +
                    "      \"clientId\":      \"Chris Connolly\",\n" +
                    "      \"clientVersion\": \"1.5.2\"\n" +
                    "    },\n" +
                    "    \"threatInfo\": {\n" +
                    "      \"threatTypes\":      [\"MALWARE\", \"SOCIAL_ENGINEERING\"],\n" +
                    "      \"platformTypes\":    [\"WINDOWS\"],\n" +
                    "      \"threatEntryTypes\": [\"URL\"],\n" +
                    "      \"threatEntries\": [\n" +
                    "        {\"url\": \"http://" + urlToCheck + "\"}\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  }");
            outputStreamWriter.close();
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null)
                return null;
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line + "\n");

            if (buffer.length() == 0)
                return null;
            return new JSONObject(buffer.toString());
        } catch (Exception e) {
            Log.e("getWebsiteJson", "Error ", e);
            return null;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("getWebsiteJson", "Error closing stream", e);
                }
            }
        }
    }

}
