package gerber.uchicago.edu;

import android.app.Activity;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Jennifer Um's awesome code
 */
public class TabEdit extends Fragment {

    public static final String INT_KEY = "Int_Key";
    //THIS IS TAB3, EDIT oncreateview is not there, than there's just empty layout.

    private int mItemid = -1; //-1 denotes no item selected

    private OnTab1InteractionListener mListener;

    private ScrollView mRootViewGroup;
    private EditText mNameField, mCityField, mAddressField, mPhoneField, mYelpField;
    private TextView mPhoneText, mAddressText, mYelpText;
    private Button  mSaveButton, mCancelButton;
    //private CheckBox mCheckFavorite;
   // private View mViewFavorite;
    private ImageView mPhotoView;

    //the restaurant passed into this activity during edit operation
    private Restaurant mRestaurant;

    private String mStrImageUrl = "";

    //this is a proxy to our database
    private RestosDbAdapter mDbAdapter;
    private InputMethodManager mImm;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.frag_scroll_layout_edit,container,false);
        //this doesnt seem to do the work that Tab 1 is actually doing right now....


        mItemid = ((MainActivity)getActivity()).getRecentIdClicked();

        //open the db adapter for db operations
        mDbAdapter = new RestosDbAdapter(v.getContext());
        mDbAdapter.open();

        //fetch the restaurant that was passed into this activity upon edit.
        //if this activity was called from a new restaurant request, the result assigned to mRestaurant will be null
        if ((mItemid != -1) && (mItemid != 0)) {
            mRestaurant = mDbAdapter.fetchRestoById(mItemid);
        }
        else {
            mRestaurant = null;
        }

        //This is where things break
        //This is NOT ScrollView!!!!!!
        mRootViewGroup = (ScrollView) v.findViewById(R.id.data_root_view_group_edit);


        mNameField = (EditText) v.findViewById(R.id.restaurant_name);
        mCityField = (EditText) v.findViewById(R.id.restaurant_city);

        //each required field must monitor itself and other text field
        mNameField.addTextChangedListener(new RequiredEditWatcher(mCityField));
        mCityField.addTextChangedListener(new RequiredEditWatcher(mNameField));

        mAddressField = (EditText) v.findViewById(R.id.restaurant_address);
        mPhoneField = (EditText) v.findViewById(R.id.restaurant_phone);
        mYelpField = (EditText) v.findViewById(R.id.restaurant_yelp);
       // mExtractButton = (Button) v.findViewById(R.id.extract_yelp_button);


        mImm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        //listen for enter and save the record
        mNameField.setOnKeyListener(enterListener);
        mCityField.setOnKeyListener(enterListener);
        mAddressField.setOnKeyListener(enterListener);
        mPhoneField.setOnKeyListener(enterListener);
        mYelpField.setOnKeyListener(enterListener);


//        //button behaviors
//        mExtractButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new YelpSearchTask().execute(mNameField.getText().toString(), mCityField.getText().toString());
//                //hide soft keyboard
//                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(mCityField.getWindowToken(), 0);
//                imm.hideSoftInputFromWindow(mNameField.getWindowToken(), 0);
//            }
//        });
        mSaveButton = (Button) v.findViewById(R.id.save_data_button);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRecord();
                ((MainActivity) getActivity()).goToTab(0);
                Toast.makeText(getActivity(), "Update successfully", Toast.LENGTH_SHORT).show();


            }
        });
//        mCheckFavorite = (CheckBox) v.findViewById(R.id.check_favorite);
//        mCheckFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                toggleFavoriteView(isChecked);
//            }
//        });
        mCancelButton = (Button) v.findViewById(R.id.cancel_action_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).goToTab(0);
            }
        });
//        mViewFavorite = v.findViewById(R.id.view_favorite);
//        mViewFavorite.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mCheckFavorite.isChecked()) {
//                    mCheckFavorite.setChecked(false);
//                } else {
//                    mCheckFavorite.setChecked(true);
//                }
//            }
//        });
        mPhoneText = (TextView) v.findViewById(R.id.text_phone);
        mYelpText = (TextView) v.findViewById(R.id.text_yelp);
        mAddressText = (TextView) v.findViewById(R.id.text_address);


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
        //mRootViewGroup.setBackgroundColor(getResources().getColor(R.color.light_green));

        //the default on new restaurant is non-favorite
        //toggleFavoriteView(false);

        //if this is a new record then set the save button to disabled and extract button to gone
        mSaveButton.setEnabled(false);
       // mExtractButton.setVisibility(View.GONE);

        if (mRestaurant != null) {
            //populate the fields from the Restaurant we passed into the intent
            mNameField.setText(mRestaurant.getName());
            mCityField.setText(mRestaurant.getCity());
            mAddressField.setText(mRestaurant.getAddress());
            mPhoneField.setText(PhoneNumberUtils.formatNumber(mRestaurant.getPhone()));
            mYelpField.setText(mRestaurant.getYelp());
           // mCheckFavorite.setChecked(mRestaurant.getFavorite() == 1);
            //change the "save" button label to "update"
            mSaveButton.setText("Update");

            //set the root view group to light blue to indicate editing
            //mRootViewGroup.setBackgroundColor(getResources().getColor(R.color.light_blue));
            //toggle the color view green or orange
           // toggleFavoriteView(mCheckFavorite.isChecked());

            //if this is a edit record then set the save button to enabled and extract button to visible
            mSaveButton.setEnabled(true);
            //mExtractButton.setVisibility(View.VISIBLE);
            mStrImageUrl = mRestaurant.getImageUrl();

            fetchPhoto(mPhotoView);
        }



        return v;
    }

    private void fetchPhoto(ImageView imageView) {
        String strUrl = String.format("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s%s&imgsz=small&imgtype=photo",
                mNameField.getText().toString() + "%20restaurant%20", mCityField.getText().toString());
        strUrl = strUrl.replaceAll("\\s+", "%20");

        new DownloadImageTask(imageView).execute(strUrl);

    }

    private void saveRecord() {
        //check to see if required fields are populated - this is a constraint in the db
        if (mCityField.getText().toString().equals("") || mCityField.getText().toString().equals("")) {
            Toast toast = Toast.makeText(getActivity(),
                    "You must populate Search Name and Search City fields",
                    Toast.LENGTH_SHORT);
            toast.show();
        } else {

            //if no data was passed into this Activity, mRestaurant will be null. Create a new restaurant
            //no id is required because the sqlite database manages the ids for us
            if (mRestaurant == null) {
                Restaurant restoNew = new Restaurant(
                        0,
                       // mCheckFavorite.isChecked() ? 1 : 0,
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
                        0,
                       // mCheckFavorite.isChecked() ? 1 : 0,
                        mNameField.getText().toString(),
                        mCityField.getText().toString(),
                        mAddressField.getText().toString(),
                        mPhoneField.getText().toString(),
                        mYelpField.getText().toString(),
                        mStrImageUrl
                );
                mDbAdapter.updateResto(restoEdit);
            }


        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
          //  mListener = (OnTab1InteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTab1InteractionListener {
        // TODO: Update argument type and name
        public void onTab1Interaction(int id);
    }


/**
    private void toggleFavoriteView(boolean bFavorite) {
        if (bFavorite) {
            mViewFavorite.setBackgroundColor(getResources().getColor(R.color.orange));
        } else {
            mViewFavorite.setBackgroundColor(getResources().getColor(R.color.green));
        }

    }
*/

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

           // updateButtons(s, mOtherEdit.getText().toString());

        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    }
    //End RequiredEditWatcher



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
                //Not sure what this color is exactly doing
                //mTextTarget.setTextColor(getResources().getColorStateList(R.color.text_field_back_color));
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
//End Details Edit Watcher



    //open and close the db adapter when we don't need it


    @Override
    public void onPause() {
        super.onPause();
        mRestaurant = null;
        mDbAdapter.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDbAdapter.open();

        if ((mItemid != -1) && (mItemid != 0)) {
            mRestaurant = mDbAdapter.fetchRestoById(mItemid);
        }
        else {
            mRestaurant = null;
        }

        if (mRestaurant != null) {
            //populate the fields from the Restaurant we passed into the intent
            mNameField.setText(mRestaurant.getName());
            mCityField.setText(mRestaurant.getCity());
            mAddressField.setText(mRestaurant.getAddress());
            mPhoneField.setText(PhoneNumberUtils.formatNumber(mRestaurant.getPhone()));
            mYelpField.setText(mRestaurant.getYelp());
           // mCheckFavorite.setChecked(mRestaurant.getFavorite() == 1);
            //change the "save" button label to "update"
            mSaveButton.setText("Update");

            //set the root view group to light blue to indicate editing
            //mRootViewGroup.setBackgroundColor(getResources().getColor(R.color.light_blue));
            //toggle the color view green or orange
           // toggleFavoriteView(mCheckFavorite.isChecked());

            //if this is a edit record then set the save button to enabled and extract button to visible
            mSaveButton.setEnabled(true);
            //mExtractButton.setVisibility(View.VISIBLE);
            mStrImageUrl = mRestaurant.getImageUrl();


        }

    }



    EditText.OnKeyListener enterListener = new EditText.OnKeyListener(){

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press

                mImm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                saveRecord();


                ((MainActivity) getActivity()).goToTab(0);
                return true;
            }
            return false;
        }
    };


    //Download Image Task

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView mImageView;

        public DownloadImageTask(ImageView imageViewParam) {
            this.mImageView = imageViewParam;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mImageView.setImageResource(R.drawable.gear_dark);
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
                return null;
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (result == null){
                mImageView.setImageResource(R.drawable.gear_dark);
              //  Toast.makeText(getActivity(), "Associated image not found on google", Toast.LENGTH_SHORT).show();
            } else {
                mImageView.setImageBitmap(result);
            }


        }
    }





}
