package gerber.uchicago.edu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.telephony.PhoneNumberUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gerber.uchicago.edu.sound.SoundVibeUtils;

/**
 * Created by Edwin on 15/02/2015.
 */
public class MainActivity extends ActionBarActivity implements TabList.OnTab2InteractionListener, ViewPager.OnPageChangeListener, android.support.v7.view.ActionMode.Callback {

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    ExpandableListAdapter mDrawerList_Right;
    ExpandableListView expandListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    String filterType = "";

    // Declaring Your View and Variables
    public static final String VERY_FIRST_LOAD_MAIN = "our_very_first_load_999";
    public static final String BOOLEAN_ARRAY_KEY = "boolean_array_key";
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence mCharSequences[] = {"list", "grid", "edit", "new"};
    int mNumboftabs = 4;
    ActionBar actionBar;

    private   int mRecentIdClicked;


    public enum Tab {
        LIST(0), GRID(1), EDIT(2), NEW(3);

        private int numVal;

       private  Tab(int numVal) {
            this.numVal = numVal;
        }

        public int getNumVal() {
            return numVal;
        }
    }

    ActionMode mActionMode;
    boolean bButtonArray[] = new boolean[3];
    SharedPreferences mPreferences;




    //private Menu mMenu;

    // private Drawable oldBackground = null;
    private int currentColor;
    private SystemBarTintManager mTintManager;
    // private LayoutInflater mInflator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);


        //Jennifer Um's code
        // Creating The ViewPagerAdapter and Passing Fragment Manager, mCharSequences fot the Tabs and Number Of Tabs.
        adapter = new ViewPagerAdapter(this, getSupportFragmentManager(),
                mCharSequences, mNumboftabs);
        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);


        // create our manager instance after the content view is set
        mTintManager = new SystemBarTintManager(this);
        // enable status bar tint
        mTintManager.setStatusBarTintEnabled(true);




        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available wid

        tabs.setOnPageChangeListener(this);

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {


                return getResources().getColor(R.color.tabsScrollColor);

            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
        changeColor(getResources().getColor(R.color.purple_dark), getResources().getColor(R.color.purple));

        //get the shared preferences
        mPreferences = this.getSharedPreferences(
                "gerber.uchicago.edu", this.MODE_PRIVATE);

        boolean bFirstLoad = mPreferences.getBoolean(VERY_FIRST_LOAD_MAIN, true);
        if (bFirstLoad) {
            for (int nC = 0; nC < 3; nC++) {
                if (nC == 1) {
                    bButtonArray[nC] = true;
                } else {
                    bButtonArray[nC] = false;
                }
            }
            Intent itn = new Intent(this,TutorialActivity.class);
            startActivity(itn);

            //set the flag in preferences so that this block will never be called again.
          //  mPreferences.edit().putBoolean(VERY_FIRST_LOAD_MAIN, false).commit();
        } else {

            //get it from the prefs
            bButtonArray = PrefsMgr.getBooleanArray(this, BOOLEAN_ARRAY_KEY, bButtonArray.length );
        }
        inflateActionBar(actionBar, 0);


       /* mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.drawer_icon_left_gray, R.string.abc_action_bar_home_description, R.string.abc_action_bar_home_description){
            public void onDrawerClosed(View view) {
                getActionBar().setTitle("111");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle("222");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };*/
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        expandListView = (ExpandableListView) findViewById(R.id.drawer_list_right);

        prepareListData();
        mDrawerList_Right = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        expandListView.setAdapter(mDrawerList_Right);
        expandListView.setOnItemClickListener(new DrawerItemClickListener());


    }

    private void selectItem(int position) {
        expandListView.setItemChecked(position,true);
//        if(position==0) {
//            Intent mIntent = new Intent();
//            mIntent.setClass(this, SettingActivity.class);
//            startActivity(mIntent);
//        }
        switch(position){
            case 0:
                startActivity(new Intent(this,SettingActivity.class));
                break;
            default:
                break;
        }

    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Setting");
        listDataHeader.add("All Saved Places");
        listDataHeader.add("All Saved People");
        listDataHeader.add("Favorites");
        listDataHeader.add("Places Visited");
        listDataHeader.add("More Features");

        List<String> setting = new ArrayList<String>();
        List<String> savedPlace = new ArrayList<String>();
        List<String> savedPeople = new ArrayList<String>();
        List<String> favorites = new ArrayList<String>();
        List<String> visitedPlace = new ArrayList<String>();
        List<String> more = new ArrayList<String>();


        // Adding child data
        // Header, Child data
        listDataChild.put(listDataHeader.get(0), setting);
        listDataChild.put(listDataHeader.get(1), savedPlace);
        listDataChild.put(listDataHeader.get(2), savedPeople);
        listDataChild.put(listDataHeader.get(3), favorites);
        listDataChild.put(listDataHeader.get(4), visitedPlace);
        listDataChild.put(listDataHeader.get(5), more);

        //add some fake features in child list.
        setting.add("General Setting");
        setting.add("My Account");
        setting.add("Information");

        savedPlace.add("University of Chicago");
        savedPlace.add("Art Museum");
        savedPlace.add("Navy Pier");

        savedPeople.add("Matt Holliday");
        savedPeople.add("Taylor Swift");
        savedPeople.add("Adam Gerber");

        favorites.add("Alinea");
        favorites.add("Bavette's Bar and Boeuf");
        favorites.add("Momotaro Restaurant");

        visitedPlace.add("UChicago");
        visitedPlace.add("Navy Pier");
        visitedPlace.add("Shedd Aquarium");

        more.add("Want More? Add Here...");


    }

    private class DrawerItemClickListener implements ExpandableListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }



    private void inflateActionBar(ActionBar bar, int pos) {

        LayoutInflater inflator = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.ab_custom, null);

        bar.setDisplayHomeAsUpEnabled(false);
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowCustomEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setCustomView(v);


        ImageButton v0 = (ImageButton) v.findViewById(R.id.action_0);
        v0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("view0", "GGG");
                bButtonArray[0] = !bButtonArray[0];
                bButtonArray[1] = !bButtonArray[1];
                toggleActionBarButton(0, bButtonArray[0]);
                toggleActionBarButton(1, bButtonArray[1]);
                PrefsMgr.setBooleanArray(MainActivity.this, BOOLEAN_ARRAY_KEY, bButtonArray);

            }
        });
        ImageButton v1 = (ImageButton) v.findViewById(R.id.action_1);
        v1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("view1", "GGG");
                bButtonArray[1] = !bButtonArray[1];
                bButtonArray[0] = !bButtonArray[0];
                toggleActionBarButton(0, bButtonArray[0]);
                toggleActionBarButton(1, bButtonArray[1]);
                PrefsMgr.setBooleanArray(MainActivity.this, BOOLEAN_ARRAY_KEY, bButtonArray);

            }
        });
        ImageButton v2 = (ImageButton) v.findViewById(R.id.action_2);
        v2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("view2", "GGG");
                bButtonArray[2] = !bButtonArray[2];
                toggleActionBarButton(2, bButtonArray[2]);
                PrefsMgr.setBooleanArray(MainActivity.this, BOOLEAN_ARRAY_KEY, bButtonArray);


                if (bButtonArray[2]) {
                    //this is just an example sound
                    SoundVibeUtils.playSound(MainActivity.this, R.raw.power_up);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    ListView modeList = new ListView(MainActivity.this);

                    final String[] stringArray = new String[]{"Food ",  //0
                            "Sports", //1
                            "Nature", //2
                            "Culture",  //3
                            "Arts",  //4
                            "Nightlife",  //5
                            "Shopping", //6
                            "Services", //7
                            "Travel", //8
                            "Other", //9
                    };

                    ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
                    modeList.setAdapter(modeAdapter);
                    builder.setView(modeList);
                    final Dialog dialog = builder.create();


                    Window window = dialog.getWindow();
                    window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    window.setGravity(Gravity.CENTER);
                    dialog.setTitle("Set filter");
                    dialog.show();
                    modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            filterType = stringArray[position];
                            System.out.println(filterType);
                            dialog.dismiss();
                        }
                    });
                }
/*
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout ll = new LinearLayout(MainActivity.this);
                    View vx = inflater.inflate(R.layout.filters_row, ll);

                    builder.setView(vx);

                    final Dialog dialog = builder.create();


                    Window window = dialog.getWindow();
                    window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    window.setGravity(Gravity.CENTER);


                    // mIdClicked = getIdFromPosition(masterListPosition);
                    //  mRestoClicked = mDbAdapter.fetchRestoById(mIdClicked);
                    dialog.setTitle("Set Filter");
                    dialog.show();


                    vx.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                        }
                    });


                    Fragment newF = getFragmentManager().findFragmentById(0);


*/


                else{
                    filterType = null;
                    System.out.println("null");

                }


            }
        });
        ImageButton v3 = (ImageButton) v.findViewById(R.id.action_3);
        v3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("view3", "GGG");


                mActionMode = MainActivity.this.startSupportActionMode(MainActivity.this);


            }
        });

        //use position to switch the gone/view


        // MenuInflater inflater = getMenuInflater();
        for (int nC = 0; nC < bButtonArray.length; nC++) {
            toggleActionBarButton(nC, bButtonArray[nC]);
        }
        toggleActionBarButton(3, false);


    }




    private void toggleActionBarButton(int pos, final boolean checked) {

        int nId = getResourceId("fram_button_" + pos, "id", getPackageName());
        final LinearLayout ll = (LinearLayout) findViewById(nId);
        int nIdTopBar = getResourceId("topbar_button_" + pos, "id", getPackageName());
        final View vTopBar = ll.findViewById(nIdTopBar);
        int nButton = getResourceId("action_" + pos, "id", getPackageName());
        final ImageButton imageButton = (ImageButton) ll.findViewById(nButton);


        if (checked) {
            vTopBar.setVisibility(View.VISIBLE);
            imageButton.setBackground(getResources().getDrawable(R.drawable.pressed_mask));
        } else {
            vTopBar.setVisibility(View.INVISIBLE);
            imageButton.setBackground(getResources().getDrawable(R.drawable.unpressed_mask));
        }

        int nDp = dpToPx(4);
        imageButton.setPadding(nDp, nDp, nDp, nDp);

    }

    //http://stackoverflow.com/questions/8309354/formula-px-to-dp-dp-to-px-android
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    //http://stackoverflow.com/questions/4427608/android-getting-resource-id-from-string
    public int getResourceId(String pVariableName, String pResourcename, String pPackageName) {
        try {
            return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void changeColor(int newColor, int tabColor) {
        tabs.setBackgroundColor(tabColor);


        mTintManager.setTintColor(newColor);
        // change ActionBar color just if an ActionBar is available
        Drawable colorDrawable = new ColorDrawable(newColor);
        // Drawable bottomDrawable = new ColorDrawable(getResources().getColor(android.R.color.transparent));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        currentColor = newColor;

    }


    @Override
    public void onTab2Interaction(String id) {

    }


    @Override
    public void onPageSelected(int position) {

      //  SoundVibeUtils.playSound(this, R.raw.swish);
        switch (position) {
            case 0:
            case 1:
                changeColor(getResources().getColor(R.color.purple_dark), getResources().getColor(R.color.purple));
                break;
            case 2:
                changeColor(getResources().getColor(R.color.orange_dark), getResources().getColor(R.color.orange));

                break;
            case 3:
                changeColor(getResources().getColor(R.color.green_dark), getResources().getColor(R.color.green));

                break;
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //callbacks for contextual action mode
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.cam_menu_search, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_done:


                mode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    public int getRecentIdClicked() {
        return mRecentIdClicked;
    }

    //overloaded
    public void goToTab( int tabNum) {
        adapter.notifyDataSetChanged();
        pager.setCurrentItem(tabNum);

    }

    //overloaded
    public void goToTab( int itemID, int tabNum) {

        mRecentIdClicked = itemID;
        adapter.notifyDataSetChanged();
        pager.setCurrentItem(tabNum);

    }


}