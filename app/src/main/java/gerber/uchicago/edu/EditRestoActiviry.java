package gerber.uchicago.edu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import gerber.uchicago.edu.db.RestosDbAdapter;
import gerber.uchicago.edu.yelp.Yelp;

public class EditRestoActiviry extends ActionBarActivity {
    private ScrollView mRootViewGroup;
    private EditText mNameField, mCityField, mAddressField, mPhoneField, mYelpField;
    private TextView mPhoneText, mAddressText, mYelpText;
    private Button mExtractButton, mSaveButton, mCancelButton;
    private CheckBox mCheckFavorite;
    private View mViewFavorite;
    private ImageView mPhotoView;

    //the restaurant passed into this activity during edit operation
    private Restaurant mRestaurant;

    private String mStrImageUrl = "";

    //this is a proxy to our database
    private RestosDbAdapter mDbAdapter;

    //gson model defined to store search results
    private YelpResultsData mYelpResultsData;

    //create this interface for instrumentation testing with threads
    private YelpTaskCallback mYelpTaskCallback;

    public static interface YelpTaskCallback {
        void executionDone();
    }

    public void setYelpTaskCallback(YelpTaskCallback yelpTaskCallback) {
        this.mYelpTaskCallback = yelpTaskCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_resto);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_resto, menu);
        //open the db adapter for db operations
        mDbAdapter = new RestosDbAdapter(this);
        mDbAdapter.open();

        //fetch the restaurant that was passed into this activity upon edit.
        //if this activity was called from a new restaurant request, the result assigned to mRestaurant will be null
        mRestaurant = (Restaurant) getIntent().getSerializableExtra("resto_bundle_key");


        mRootViewGroup = (ScrollView) findViewById(R.id.data_root_view_group);
        mNameField = (EditText) findViewById(R.id.restaurant_name);
        mCityField = (EditText) findViewById(R.id.restaurant_city);

        //each required field must monitor itself and other text field
        mNameField.addTextChangedListener(new RequiredEditWatcher(mCityField));
        mCityField.addTextChangedListener(new RequiredEditWatcher(mNameField));

        mAddressField = (EditText) findViewById(R.id.restaurant_address);
        mPhoneField = (EditText) findViewById(R.id.restaurant_phone);
        mYelpField = (EditText) findViewById(R.id.restaurant_yelp);
        mExtractButton = (Button) findViewById(R.id.extract_yelp_button);
        mExtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new YelpSearchTask().execute(mNameField.getText().toString(), mCityField.getText().toString());
                //hide soft keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mCityField.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(mNameField.getWindowToken(), 0);
            }
        });
        mSaveButton = (Button) findViewById(R.id.save_data_button);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check to see if required fields are populated - this is a constraint in the db
                if (mCityField.getText().toString().equals("") || mCityField.getText().toString().equals("")) {
                    Toast toast = Toast.makeText(EditRestoActiviry.this,
                            "You must populate Search Name and Search City fields",
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    //if no data was passed into this Activity, mRestaurant will be null. Create a new restaurant
                    //no id is required because the sqlite database manages the ids for us
                    if (mRestaurant == null) {
                        Restaurant restoNew = new Restaurant(
                                mCheckFavorite.isChecked() ? 1 : 0,
                                mNameField.getText().toString(),
                                mCityField.getText().toString(),
                                mAddressField.getText().toString(),
                                mPhoneField.getText().toString(),
                                mYelpField.getText().toString(),
                                mStrImageUrl
                        );
                        mDbAdapter.createResto(restoNew);
                        //if we had passed in a restaurant, then we're in edit-mode. Edit the restaurant
                        //notice that we are calling the 7-arg constructor with the id
                    } else {
                        Restaurant restoEdit = new Restaurant(
                                mRestaurant.getId(),
                                mCheckFavorite.isChecked() ? 1 : 0,
                                mNameField.getText().toString(),
                                mCityField.getText().toString(),
                                mAddressField.getText().toString(),
                                mPhoneField.getText().toString(),
                                mYelpField.getText().toString(),
                                mStrImageUrl
                        );
                        mDbAdapter.updateResto(restoEdit);

                    }

                    finish();

                }

            }
        });
        mCheckFavorite = (CheckBox) findViewById(R.id.check_favorite);
        mCheckFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleFavoriteView(isChecked);
            }
        });
        mCancelButton = (Button) findViewById(R.id.cancel_action_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mViewFavorite = findViewById(R.id.view_favorite);
        mViewFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckFavorite.isChecked()) {
                    mCheckFavorite.setChecked(false);
                } else {
                    mCheckFavorite.setChecked(true);
                }
            }
        });
        mPhoneText = (TextView) findViewById(R.id.text_phone);
        mYelpText = (TextView) findViewById(R.id.text_yelp);
        mAddressText = (TextView) findViewById(R.id.text_address);


        mPhoneText.setEnabled(false);
        mPhoneText.setOnClickListener(new DetailsEditWatcher());
        mPhoneField.addTextChangedListener(new DetailsEditWatcher(mPhoneText));

        mAddressText.setEnabled(false);
        mAddressText.setOnClickListener(new DetailsEditWatcher());
        mAddressField.addTextChangedListener(new DetailsEditWatcher(mAddressText));

        mYelpText.setEnabled(false);
        mYelpText.setOnClickListener(new DetailsEditWatcher());
        mYelpField.addTextChangedListener(new DetailsEditWatcher(mYelpText));

        mPhotoView = (ImageView) findViewById(R.id.restaurant_image_view);

        //default behavior is to create a new restaurant which is indicated by green
        mRootViewGroup.setBackgroundColor(getResources().getColor(R.color.light_green));

        //the default on new restaurant is non-favorite
        toggleFavoriteView(false);

        //if this is a new record then set the save button to disabled and extract button to gone
        mSaveButton.setEnabled(false);
        mExtractButton.setVisibility(View.GONE);


        if (mRestaurant != null) {
            //populate the fields from the Restaurant we passed into the intent
            mNameField.setText(mRestaurant.getName());
            mCityField.setText(mRestaurant.getCity());
            mAddressField.setText(mRestaurant.getAddress());
            mPhoneField.setText(PhoneNumberUtils.formatNumber(mRestaurant.getPhone()));
            mYelpField.setText(mRestaurant.getYelp());
            mCheckFavorite.setChecked(mRestaurant.getFavorite() == 1);
            //change the "save" button label to "update"
            mSaveButton.setText("Update");

            //set the root view group to light blue to indicate editing
            mRootViewGroup.setBackgroundColor(getResources().getColor(R.color.light_blue));
            //toggle the color view green or orange
            toggleFavoriteView(mCheckFavorite.isChecked());

            //if this is a edit record then set the save button to enabled and extract button to visible
            mSaveButton.setEnabled(true);
            mExtractButton.setVisibility(View.VISIBLE);

            mStrImageUrl = mRestaurant.getImageUrl();

            fetchPhoto(mPhotoView);


        }
        return true;
    }

    private void toggleFavoriteView(boolean bFavorite) {
        if (bFavorite) {
            mViewFavorite.setBackgroundColor(getResources().getColor(R.color.orange));
        } else {
            mViewFavorite.setBackgroundColor(getResources().getColor(R.color.green));
        }

    }

    //will need this later for testing
    public YelpResultsData getYelpResultsData() {
        return mYelpResultsData;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_close) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateButtons(CharSequence charSequence, String strOther) {
        if (!charSequence.toString().trim().equalsIgnoreCase("") && !strOther.trim().equalsIgnoreCase("")) {

            mExtractButton.setVisibility(View.VISIBLE);
            mSaveButton.setEnabled(true);


        } else {
            mExtractButton.setVisibility(View.GONE);
            mSaveButton.setEnabled(false);

        }

    }

    private class RequiredEditWatcher implements TextWatcher {

        private EditText mOtherEdit;

        private RequiredEditWatcher(EditText otherEdit) {
            mOtherEdit = otherEdit;
        }

        //the following three methods satisfy the TextWatcher interface
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            updateButtons(s, mOtherEdit.getText().toString());

        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    }


    private class DetailsEditWatcher implements TextWatcher, View.OnClickListener {

        private TextView mTextTarget;

        //consturctor used with EditText
        private DetailsEditWatcher(TextView textView) {
            mTextTarget = textView;

        }

        //constructor used with Textview
        private DetailsEditWatcher() {
        }

        //the following three methods satisfy the TextWatcher interface
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            //if the text inside the corresponding EditText is not empty, then change
            if (s.toString().equalsIgnoreCase("")) {
                mTextTarget.setTextColor(getResources().getColor(R.color.black));
                mTextTarget.setEnabled(false);
            } else {
                mTextTarget.setTextColor(getResources().getColorStateList(R.color.pop_back_color));
                mTextTarget.setEnabled(true);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        //this satisfies the OnClickListener interface
        @Override
        public void onClick(View v) {

            FavActionUtility favActionUtility = new FavActionUtility(EditRestoActiviry.this);

            try {
                switch (v.getId()) {
                    case R.id.text_phone:
                        favActionUtility.dial(mPhoneField.getText().toString());
                        break;
                    case R.id.text_address:
                        favActionUtility.mapOf(mAddressField.getText().toString(), mCityField.getText().toString());
                        break;
                    case R.id.text_yelp:
                        favActionUtility.yelpSite(mYelpField.getText().toString());
                        break;
                }
            } catch (Exception e) {
                favActionUtility.showErrorMessageInDialog(e.getMessage());
                e.printStackTrace();
            }

        }
    }


    //open and close the db adapter when we don't need it
 /*   @Override
    protected void onPause() {
        super.onPause();
        mDbAdapter.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDbAdapter.open();
    }
*/
    private void fetchPhoto(ImageView imageView) {

        String strUrl = String.format("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s%s&imgsz=small&imgtype=photo",
                mNameField.getText().toString() + "%20restaurant%20", mCityField.getText().toString());
        strUrl = strUrl.replaceAll("\\s+", "%20");
        new DownloadImageTask(imageView).execute(strUrl);

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView mImageView;

        public DownloadImageTask(ImageView imageViewParam) {
            this.mImageView = imageViewParam;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mImageView.setImageResource(R.drawable.gear);
        }

        protected Bitmap doInBackground(String... urls) {


            GoogleResultsData googleResultsData = null;
            Bitmap bitmap = null;

            try {

                if (mStrImageUrl != null && !mStrImageUrl.equals("")){
                    InputStream in = new java.net.URL(mStrImageUrl).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                } else {
                    JSONObject jsonRaw = new JSONParser().getSecureJSONFromUrl(urls[0]);
                    googleResultsData = new Gson().fromJson(jsonRaw.toString(), GoogleResultsData.class);
                    mStrImageUrl = googleResultsData.responseData.results.get(0).unescapedUrl;
                    InputStream in = new java.net.URL(mStrImageUrl).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (result == null){
                mImageView.setImageResource(R.drawable.gear);
                Toast.makeText(EditRestoActiviry.this, "Associated image not found on google", Toast.LENGTH_SHORT).show();
            } else {
                mImageView.setImageBitmap(result);
            }


        }
    }

    private class YelpSearchTask extends AsyncTask<String, Void, YelpResultsData> {


        private ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(EditRestoActiviry.this);
            progressDialog.setTitle("Fetching data");
            progressDialog.setMessage("One moment please...");
            progressDialog.setCancelable(true);

            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    YelpSearchTask.this.cancel(true);
                    progressDialog.dismiss();
                }
            });
            progressDialog.show();

        }


        @Override
        protected YelpResultsData doInBackground(String... params) {

            String name = params[0];
            String location = params[1];
            Yelp yelpApi = new Yelp();
            YelpResultsData yelpSearchResultLocal = null;
            try {
                yelpSearchResultLocal = yelpApi.searchMultiple(name, location);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return yelpSearchResultLocal;

        }

        @Override
        protected void onPostExecute(YelpResultsData yelpResultsData) {
            progressDialog.dismiss();
            mYelpResultsData = yelpResultsData;

            if (mYelpResultsData == null){
                Toast.makeText(EditRestoActiviry.this, "No data for that search term", Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<String> stringArrayList = mYelpResultsData.getSimpleValues();
            if (stringArrayList.size() == 0) {
                Toast.makeText(EditRestoActiviry.this, "No data for that search term", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable("simple_data_bundle_key", stringArrayList);
            Intent intent = new Intent(EditRestoActiviry.this, ResultsDialogActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, 1001);

            if (mYelpTaskCallback != null) {
                mYelpTaskCallback.executionDone();
            }

        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                //fetch the integer we passed into the dialog result which corresponds to the list position
                int nResult = data.getIntExtra(ResultsDialogActivity.POSITION, -99);
                if (nResult != -99) {
                    try {
                        mStrImageUrl = "";
                        YelpResultsData.Business biz = mYelpResultsData.businesses.get(nResult);
                        mNameField.setText(biz.name);
                        mAddressField.setText(biz.location.address.get(0));
                        mPhoneField.setText(PhoneNumberUtils.formatNumber(biz.phone));
                        mYelpField.setText(biz.url);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    fetchPhoto(mPhotoView);
                }
            }
            if (resultCode == RESULT_CANCELED) {
                //do nothing
            }
        }
    }
}
