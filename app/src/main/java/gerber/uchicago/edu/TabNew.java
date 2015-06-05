package gerber.uchicago.edu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import gerber.uchicago.edu.db.RestosDbAdapter;
import gerber.uchicago.edu.yelp.Yelp;
import gerber.uchicago.edu.CategoryManager;

/**
 * Created by Edwin on 15/02/2015.
 */
public class TabNew extends Fragment {
    private ScrollView mRootViewGroup;
    private EditText mNameField, mCityField, mAddressField, mPhoneField, mYelpField;
    private TextView mPhoneText, mAddressText, mYelpText;
    private Spinner mCategory;
    private Button mExtractButton, mSaveButton, mCancelButton;
    private ImageView mPhotoView;
    private MainActivity main;
    private CategoryManager categoryManger;
    private String[] mCategories;
    private String mCategoryString;

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

    private int findPositionGivenCode(String code, String[] categories){
        for(int i = 0; i < categories.length; i++){
            if(categories[i].equalsIgnoreCase(code)){
                return i;
            }
        }
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.frag_scroll_layout_new,container,false);

        main = (MainActivity) getActivity();
        categoryManger = new CategoryManager(getActivity());

        mDbAdapter = new RestosDbAdapter(getActivity());
        mDbAdapter.open();

        //fetch the restaurant that was passed into this fragment upon edit.
        //if this was called from a new restaurant request, the result assigned to mRestaurant will be null

        mRootViewGroup = (ScrollView) v.findViewById(R.id.data_root_view_group);
        mNameField = (EditText) v.findViewById(R.id.restaurant_name);
        mCityField = (EditText) v.findViewById(R.id.restaurant_city);

        //each required field must monitor itself and other text field
        mNameField.addTextChangedListener(new RequiredEditWatcher(mCityField));
        mCityField.addTextChangedListener(new RequiredEditWatcher(mNameField));

        mAddressField = (EditText) v.findViewById(R.id.restaurant_address);
        mPhoneField = (EditText) v.findViewById(R.id.restaurant_phone);
        mYelpField = (EditText) v.findViewById(R.id.restaurant_yelp);
        mExtractButton = (Button) v.findViewById(R.id.extract_yelp_button);
        mExtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new YelpSearchTask().execute(mNameField.getText().toString(), mCityField.getText().toString());
                //hide soft keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mCityField.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(mNameField.getWindowToken(), 0);
            }
        });
        mSaveButton = (Button) v.findViewById(R.id.save_data_button);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check to see if required fields are populated - this is a constraint in the db
                if (mCityField.getText().toString().equals("") || mCityField.getText().toString().equals("")) {
                    Toast toast = Toast.makeText(getActivity(),
                            "You must populate Search Name and Search City fields",
                            Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Restaurant restoNew = new Restaurant(
                            mCategories[mCategory.getSelectedItemPosition()],
                            mNameField.getText().toString(),
                            mCityField.getText().toString(),
                            mAddressField.getText().toString(),
                            mPhoneField.getText().toString(),
                            mYelpField.getText().toString(),
                            mStrImageUrl
                    );
                    mDbAdapter.createResto(restoNew);
                    mNameField.setText("");
                    mCityField.setText("");
                    mAddressField.setText("");
                    mPhoneField.setText("");
                    mYelpField.setText("");
                    mStrImageUrl = "";

                    main.goToTab(0);
                    //if we had passed in a restaurant, then we're in edit-mode. Edit the restaurant
                    //notice that we are calling the 7-arg constructor with the id


                    Toast.makeText(main, "Save Successfully!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mCancelButton = (Button) v.findViewById(R.id.cancel_action_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                main.goToTab(0);
            }
        });

        mPhoneText = (TextView) v.findViewById(R.id.text_phone);
        mYelpText = (TextView) v.findViewById(R.id.text_yelp);
        mAddressText = (TextView) v.findViewById(R.id.text_address);

        mCategory = (Spinner) v.findViewById(R.id.text_category);
        mCategories = new String[10];
        mCategories[0] = "Food";
        mCategories[1] = "Sports";
        mCategories[2] = "Nature";
        mCategories[3] = "Culture";
        mCategories[4] = "Arts";
        mCategories[5] = "Nightlife";
        mCategories[6] = "Shopping";
        mCategories[7] = "Services";
        mCategories[8] = "Travel";
        mCategories[9] = "Other";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(

                //context
                getActivity(),
                //view: layout you see when the spinner is closed
                R.layout.spinner_closed,
                //model: the array of Strings
                mCategories
        );
        arrayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        mCategory.setAdapter(arrayAdapter);
        mCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getId()) {

                    case R.id.text_category:
                        //define behavior here
                        //PrefsMgr.setString(this, FOR, extractCodeFromCurrency((String)mForSpinner.getSelectedItem()));
                        mCategoryString = mCategories[mCategory.getSelectedItemPosition()];
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mPhoneText.setEnabled(false);
        mPhoneText.setOnClickListener(new DetailsEditWatcher());
        mPhoneField.addTextChangedListener(new DetailsEditWatcher(mPhoneText));

        mAddressText.setEnabled(false);
        mAddressText.setOnClickListener(new DetailsEditWatcher());
        mAddressField.addTextChangedListener(new DetailsEditWatcher(mAddressText));

        mYelpText.setEnabled(false);
        mYelpText.setOnClickListener(new DetailsEditWatcher());
        mYelpField.addTextChangedListener(new DetailsEditWatcher(mYelpText));

        mPhotoView = (ImageView) v.findViewById(R.id.restaurant_image_view);

        //default behavior is to create a new restaurant which is indicated by green
        mRootViewGroup.setBackgroundColor(getResources().getColor(R.color.light_green));

        //the default on new restaurant is non-favorite
       // toggleFavoriteView(false);

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
            mCategory.setSelection(findPositionGivenCode(mRestaurant.getCategory(), mCategories));
            //change the "save" button label to "update"
            mSaveButton.setText("Update");

            //set the root view group to light blue to indicate editing
            mRootViewGroup.setBackgroundColor(getResources().getColor(R.color.light_blue));
            //toggle the color view green or orange
          //  toggleFavoriteView(mCheckFavorite.isChecked());

            //if this is a edit record then set the save button to enabled and extract button to visible
            mSaveButton.setEnabled(true);
            mExtractButton.setVisibility(View.VISIBLE);

            mStrImageUrl = mRestaurant.getImageUrl();

            fetchPhoto(mPhotoView);


        }


        return v;
    }


    /*private void toggleFavoriteView(boolean bFavorite) {
        if (bFavorite) {
            mViewFavorite.setBackgroundColor(getResources().getColor(R.color.orange));
        } else {
            mViewFavorite.setBackgroundColor(getResources().getColor(R.color.green));
        }

    }
*/
    public YelpResultsData getYelpResultsData() {
        return mYelpResultsData;
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

            FavActionUtility favActionUtility = new FavActionUtility(getActivity());

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
                Toast.makeText(getActivity(), "Associated image not found on google", Toast.LENGTH_SHORT).show();
            } else {
                mImageView.setImageBitmap(result);
            }


        }
    }

    private class YelpSearchTask extends AsyncTask<String, Void, YelpResultsData> {


        private ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(getActivity());
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
                Toast.makeText(getActivity(), "No data for that search term", Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<String> stringArrayList = mYelpResultsData.getSimpleValues();
            if (stringArrayList.size() == 0) {
                Toast.makeText(getActivity(), "No data for that search term", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable("simple_data_bundle_key", stringArrayList);
            Intent intent = new Intent(getActivity(), ResultsDialogActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, 1001);

            if (mYelpTaskCallback != null) {
                mYelpTaskCallback.executionDone();
            }

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1001) {
            if (resultCode == getActivity().RESULT_OK) {
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
                        mCategory.setSelection(findPositionGivenCode(categoryManger.getCategoryFromYelpCat(biz.categories.get(0).get(1)), mCategories));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    fetchPhoto(mPhotoView);
                }
            }
            if (resultCode == getActivity().RESULT_CANCELED) {
                //do nothing
            }
        }
    }






}
