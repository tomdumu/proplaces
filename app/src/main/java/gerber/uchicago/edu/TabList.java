package gerber.uchicago.edu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import gerber.uchicago.edu.db.RestosDbAdapter;
import gerber.uchicago.edu.db.RestosSimpleCursorAdapter;
import gerber.uchicago.edu.sound.SoundVibeUtils;


public class TabList extends Fragment  {

    //private ListView mListView;
    private RestosDbAdapter mDbAdapter;
    private RestosSimpleCursorAdapter mCursorAdapter;

    private SharedPreferences mPreferences;
    private static final String SORT_ORDER = "sort_order";
   // private static final String VERY_FIRST_LOAD = "our_very_first_load_3";
    private String mSortOrder;


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";


    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;

    private String mParam2;
    private OnTab2InteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static TabList newInstance(String param1, String param2) {
        TabList fragment = new TabList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TabList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.activity_reminders_layout, container, false);

        // Set the adapter
        mListView = (ListView) view.findViewById(R.id.reminders_list_view);
      //  mListView = (AbsListView) view.findViewById(R.id.reminders_list_view);
       mListView.setAdapter(mAdapter);


        super.onCreate(savedInstanceState);


        mListView = (ListView) view.findViewById(R.id.reminders_list_view);
        mListView.setDivider(null);

        //get the shared preferences
        mPreferences = getActivity().getSharedPreferences(
                "gerber.uchicago.edu", getActivity().MODE_PRIVATE);


        mDbAdapter = new RestosDbAdapter(getActivity());
        mDbAdapter.open();

        //get the value associated with "very_first_load";  return true if there is no value in SharedPreferences (will happen on the very first time only)
        boolean bFirstLoad = mPreferences.getBoolean(MainActivity.VERY_FIRST_LOAD_MAIN, true);
        if (bFirstLoad) {

            mDbAdapter.insertSomeRestos();
            //set the flag in preferences so that this block will never be called again.
            mPreferences.edit().putBoolean(MainActivity.VERY_FIRST_LOAD_MAIN, false).commit();
        }

        mSortOrder = mPreferences.getString(SORT_ORDER, null);
        Cursor cursor = mDbAdapter.fetchAllRestos(getSortOrder());


        //from columns defined in the db
        String[] from = new String[]{
                RestosDbAdapter.COL_NAME,
                RestosDbAdapter.COL_CITY,
                RestosDbAdapter.COL_ADDRESS
        };

        //to the ids of views in the layout
        int[] to = new int[]{
                R.id.list_text,
                R.id.list_city,
                R.id.list_address
        };

        mCursorAdapter = new RestosSimpleCursorAdapter(
                //context
                getActivity(),
                //the layout of the row - now pulling from our new layout
                R.layout.restos_row,
                //cursor
                cursor,
                //from columns defined in the db
                from,
                //to the ids of views in the layout
                to,
                //flag - not used
                0);


        //the cursorAdapter (controller) is now updating the listView (view) with data from the db (model)
        mListView.setAdapter(mCursorAdapter);

        //when we click an individual item in the listview
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            private Restaurant mRestoClicked;
            private int mIdClicked;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {

                //this is just an example, I would put this elsewhere
                SoundVibeUtils.playSound(getActivity(), R.raw.power_up);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                ListView modeList = new ListView(getActivity());

                String[] stringArray = new String[]{"Edit ",  //0
                        "Share ", //1
                        "Map of ", //2
                        "Dial ",  //3
                        "Yelp site ",  //4
                        "Navigate to ",  //5
                        "Delete ", //6
                        "Cancel " //7
                };

                ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
                modeList.setAdapter(modeAdapter);
                builder.setView(modeList);
                final Dialog dialog = builder.create();


                Window window = dialog.getWindow();
                window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);
                mIdClicked = getIdFromPosition(masterListPosition);
                mRestoClicked = mDbAdapter.fetchRestoById(mIdClicked);
                dialog.setTitle(mRestoClicked.getName());
                dialog.show();
                modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                        FavActionUtility favActionUtility = new FavActionUtility(getActivity());


                        try {
                            switch (position) {
                                case 0:
                                    //edit
                                   // Intent intent = new Intent(getActivity(), EditRestoActivity.class);
                                  //  intent.putExtra("resto_bundle_key", mRestoClicked);
                                   // startActivity(intent);
                                    ((MainActivity)getActivity()).goToTab( mIdClicked, 2);


                                    break;
                                case 1:
                                    //share
                                    String strSubject = "Check out: " + mRestoClicked.getName();
                                    String strMessage = "\n\n"; //give the user some room to type a message
                                    strMessage += "Restaurant: " + mRestoClicked.getName();
                                    strMessage += "\nAddress: " + mRestoClicked.getAddress() + ", " + mRestoClicked.getCity();
                                    strMessage += " \n\nPhone: " + PhoneNumberUtils.formatNumber(mRestoClicked.getPhone());
                                    strMessage += " \nYelp page: " + mRestoClicked.getYelp();
                                    strMessage += " \nCategory: " + mRestoClicked.getCategory();
                                    strMessage += "\n\nPowered by proPlaces on Android by Adam Gerber";
                                    favActionUtility.share(strSubject, strMessage );
                                    break;

                                case 2:
                                    //map of
                                    favActionUtility.mapOf(mRestoClicked.getAddress(), mRestoClicked.getCity());
                                    break;
                                case 3:
                                    //dial
                                    favActionUtility.dial(mRestoClicked.getPhone());
                                    break;
                                case 4:
                                    //yelp site
                                    favActionUtility.yelpSite(mRestoClicked.getYelp());
                                    break;
                                case 5:
                                    //navigate to
                                    favActionUtility.navigateTo(mRestoClicked.getAddress(), mRestoClicked.getCity());
                                    break;
                                case 6:
                                    //delete single resto (we need to keep this for devices running 11 or less)
                                    mDbAdapter.deleteRestoById(mIdClicked);
                                    mCursorAdapter.changeCursor(mDbAdapter.fetchAllRestos(getSortOrder()));
                                    break;
                                case 7:
                                    //cancel
                                    //do nothing and then dismiss
                                    break;
                            }
                        } catch (Exception e) {
                            //gracefully handle exceptions
                            favActionUtility.showErrorMessageInDialog(e.getMessage());
                            e.printStackTrace();
                        }

                        dialog.dismiss();
                    }
                });


            }
        });

        //contextual action mode set-up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.cam_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.menu_item_delete_reminder:
                            for (int nC = mCursorAdapter.getCount() - 1; nC >= 0; nC--) {
                                if (mListView.isItemChecked(nC)) {

                                    mDbAdapter.deleteRestoById(getIdFromPosition(nC));

                                }
                            }
                            mode.finish();
                            mCursorAdapter.changeCursor(mDbAdapter.fetchAllRestos(getSortOrder()));
                            return true;

                    }

                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });

        }





        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTab2InteractionListener) activity;
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
    public interface OnTab2InteractionListener {


        // TODO: Update argument type and name
        public void onTab2Interaction(String id);
    }


    public String getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(String strSortOrder) {
        mSortOrder = strSortOrder;
        mPreferences.edit().putString(SORT_ORDER, strSortOrder).commit();

    }



    private int getIdFromPosition(int nPosition) {
        Cursor cursor = mDbAdapter.fetchAllRestos(getSortOrder());
        cursor.move(nPosition);
        return cursor.getInt(RestosDbAdapter.INDEX_ID);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mDbAdapter.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDbAdapter.open();
        // mDbAdapter.insertSomeRestos();
        mCursorAdapter.changeCursor(mDbAdapter.fetchAllRestos(getSortOrder()));

    }

}
