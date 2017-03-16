package edu.niu.students.z1721198.cluster;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    /* Constants */

    private static final int NEW_PROFILE_REQUEST = 1;
    private static final int EDIT_PROFILE_REQUEST = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /* Instance Variables */

    private Toolbar appBar;
    private DrawerLayout drawerLayout;
    private NavigationView navDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private FrameLayout fragmentLayout;
    private Fragment content;

    private boolean profileMenuIsOpen = false;

    private ProfileDBAdapter profileDB;
    private long profileId;
    private String profileName;
    private String make;
    private int weight, maxRpm;
    private float engDisp, fuelCap;
    private Uri profileImage;

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;

    private View decorView;

    /* Methods */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialize the user's preferences
        initPreferences();

        // set the content view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // link the objects to their views
        fragmentLayout = (FrameLayout) findViewById(R.id.fragment_layout);

        // initialize the profiles database
        initDB();

        // initialize the app bar and nav drawer
        initAppBar();
        initNavDrawer();

        // set the content fragment to the default MonitorFragment
        content = new MonitorFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_layout, content);
        transaction.commit();

        // initialize bluetooth communications
        initBluetooth();

        decorView = getWindow().getDecorView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        profileDB.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            // user pressed the add button
            case R.id.action_add:
                return true;
            // user pressed the fullscreen button
            case R.id.action_fullscreen:
                // hide the title and set the app to fullscreen
                hideSystemUI();
                return true;
            // the user's action was not recognized
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case EDIT_PROFILE_REQUEST:
                if(resultCode == RESULT_OK) {
                    // update the active profile to this one
                    updateActiveProfile(data.getLongExtra("profileId", -1));

                    // remove the old menu item for this profile and add an updated one
                    Menu navDrawerMenu = navDrawer.getMenu();
                    navDrawerMenu.removeItem((int) profileId);
                    final MenuItem newItem = navDrawerMenu.add(R.id.menu_profile, (int) profileId, Menu.NONE, profileName);

                    // set the new item to update the active profile when clicked
                    newItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            updateActiveProfile(newItem.getItemId());
                            return true;
                        }
                    });
                }

            case NEW_PROFILE_REQUEST:
                if(resultCode == RESULT_OK) {
                    // update the active profile to this one
                    updateActiveProfile(data.getLongExtra("profileId", -1));

                    // add the new profile to the navigation drawer
                    Menu navDrawerMenu = navDrawer.getMenu();
                    final MenuItem newItem = navDrawerMenu.add(R.id.menu_profile, (int) profileId, Menu.NONE, profileName);
                    // set the new item to update the active profile when clicked
                    newItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            updateActiveProfile(newItem.getItemId());
                            return true;
                        }
                    });
                }
        }
    }

    private void initPreferences() {
        // set the default values for the app's preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.units_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.obd2_preferences, false);

        // set the app's theme based on the stored value in SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = preferences.getString(SettingsActivity.PREF_DISPLAY_THEME_KEY, null);
        if(theme.equals("0")) {
            setTheme(R.style.AppTheme_Light);
        }
        else if(theme.equals("1")) {
            setTheme(R.style.AppTheme_Dark);
        }

        // set the active profileId based on the profileId stored in shared preferences
        profileId = preferences.getLong("profileId", 1);
    }

    private void initAppBar() {
        appBar = (Toolbar) findViewById(R.id.app_bar);
        appBar.setTitle(R.string.title_monitor);
        setSupportActionBar(appBar);
    }

    private void initNavDrawer() {
        // set up the navigation drawer menu
        navDrawer = (NavigationView) findViewById(R.id.navigation_drawer);
        navDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item)
                    {
                        // set up the fragment transaction
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();

                        // switch to the proper fragment or new activity depending on the item pressed
                        Intent intent;
                        switch(item.getItemId()) {
                            case R.id.drawer_item_edit_profile:
                                // if not the default profile, send the user to the edit profile screen
                                if(profileId != 1) {
                                    intent = new Intent(MainActivity.this, ProfileActivity.class);
                                    intent.putExtra("profileExists", true);
                                    intent.putExtra("profileId", profileId);
                                    startActivityForResult(intent, EDIT_PROFILE_REQUEST);
                                    drawerLayout.closeDrawers();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "The default profile cannot be edited", Toast.LENGTH_SHORT).show();
                                }
                                return true;

                            case R.id.drawer_item_new_profile:
                                intent = new Intent(MainActivity.this, ProfileActivity.class);
                                intent.putExtra("profileExists", false);
                                startActivityForResult(intent, NEW_PROFILE_REQUEST);
                                //drawerLayout.closeDrawers();
                                return true;

                            case R.id.drawer_item_monitor:
                                content = new MonitorFragment();
                                transaction.replace(R.id.fragment_layout, content);
                                transaction.commit();
                                appBar.setTitle(R.string.title_monitor);
                                drawerLayout.closeDrawers();
                                return true;

                            case R.id.drawer_item_logger:
                                content = new LoggerFragment();
                                transaction.replace(R.id.fragment_layout, content);
                                transaction.commit();
                                appBar.setTitle(R.string.title_logger);
                                drawerLayout.closeDrawers();
                                return true;

                            case R.id.drawer_item_faults:
                                content = new FaultFragment();
                                transaction.replace(R.id.fragment_layout, content);
                                transaction.commit();
                                appBar.setTitle(R.string.title_faults);
                                drawerLayout.closeDrawers();
                                return true;

                            case R.id.drawer_item_settings:
                                intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                                return true;

                            case R.id.drawer_item_help:
                                return true;

                            default:
                                return true;
                        }
                    }
                }
        );

        // add the user's profiles to the navigation drawer
        final Menu navDrawerMenu = navDrawer.getMenu();
        Cursor cursor = profileDB.getAllRows();
        if(cursor.moveToFirst()) {
            do {
                int profileId = cursor.getInt(ProfileDBAdapter.COL_ROWID);
                String profileName = cursor.getString(ProfileDBAdapter.COL_NAME);
                if (profileName.equals("")) {
                    String year = cursor.getString(ProfileDBAdapter.COL_YEAR);
                    String make = cursor.getString(ProfileDBAdapter.COL_MAKE);
                    String model = cursor.getString(ProfileDBAdapter.COL_MODEL);

                    profileName = year + " " + make + " " + model;
                }

                final MenuItem newItem = navDrawerMenu.add(R.id.menu_profile, profileId, Menu.NONE, profileName);
                newItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        updateActiveProfile(newItem.getItemId());
                        return true;
                    }
                });
            } while(cursor.moveToNext());
        }
        // hide the newly added profiles
        navDrawerMenu.setGroupVisible(R.id.menu_profile, false);

        // change the current profile to the active one
        updateActiveProfile(profileId);

        // set up the profile select button
        View headerView = navDrawer.getHeaderView(0);
        final Button profileButton = (Button) headerView.findViewById(R.id.header_change_profile_button);
        profileButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // if the profile menu is closed and the button is pressed, hide the default menu and show the profile menu
                        // else, hide the profile menu and show the default menu
                        if (!profileMenuIsOpen) {
                            profileMenuIsOpen = true;
                            profileButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
                            navDrawerMenu.setGroupVisible(R.id.menu_profile, true);
                            navDrawerMenu.setGroupVisible(R.id.menu_top, false);
                        } else {
                            profileMenuIsOpen = false;
                            profileButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
                            navDrawerMenu.setGroupVisible(R.id.menu_profile, false);
                            navDrawerMenu.setGroupVisible(R.id.menu_top, true);
                        }
                    }
                }
        );

        // set up the navigation drawer toggle button
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                appBar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view)
            {
                // if the profile menu is open, hide it and show the default menu for next time the drawer is opened
                if(profileMenuIsOpen) {
                    profileMenuIsOpen = false;
                    profileButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
                    navDrawerMenu.setGroupVisible(R.id.menu_profile, false);
                    navDrawerMenu.setGroupVisible(R.id.menu_top, true);
                }
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        // change the drawer's icon from the default black icon to a white one
        drawerToggle.setDrawerIndicatorEnabled(false);
        appBar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        appBar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawerLayout.openDrawer(Gravity.LEFT);
                    }
                }
        );
        drawerToggle.syncState();
    }

    private void initDB() {
        profileDB = new ProfileDBAdapter(this);
        profileDB.open();
    }

    private void initBluetooth() {
        /*
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter != null) {
            if(!btAdapter.isEnabled()) {
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            }
        }
        boolean connected = checkForBluetoothConnection();
        */
    }

    private boolean checkForBluetoothConnection() {
        // get shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // get the value for key pref_bt_select
        String btSelectPref = sharedPreferences.getString(getString(R.string.pref_bt_select_key), "");

        // if the key pref_bt_select was found and has its default value, prompt the user with a
        // snackbar to select a bluetooth adapter in settings
        if(btSelectPref.equals("0")) {
            Snackbar btSelectSnackbar = Snackbar.make(fragmentLayout, "No OBD2 Adapter Selected", Snackbar.LENGTH_INDEFINITE);
            btSelectSnackbar.setAction("Settings",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // TODO: add extra to show user where the adapter selection preference is
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }
                    }
            );
            btSelectSnackbar.show();

            return false;
        }
        // if the key pref_bt_select wasn't found, prompt the user with a snackbar that preferences
        // couldn't be read
        else if(btSelectPref.equals("")) {
            Snackbar btSelectSnackbar = Snackbar.make(fragmentLayout, "Failed To Read Preferences", Snackbar.LENGTH_INDEFINITE);
            btSelectSnackbar.show();

            return false;
        }

        return true;
    }

    private void updateActiveProfile(long profId) {
        // get a cursor to the row that was edited/added to the profiles database
        Cursor cursor = profileDB.getRow(profId);
        if(cursor.moveToFirst()) {
            // store each column in the corresponding instance variable to make the new profile active
            profileId = cursor.getInt(ProfileDBAdapter.COL_ROWID);
            profileName = cursor.getString(ProfileDBAdapter.COL_NAME);
            // if there is no profile name, construct one from the year, make, and model
            if (profileName.equals("")) {
                String year = cursor.getString(ProfileDBAdapter.COL_YEAR);
                make = cursor.getString(ProfileDBAdapter.COL_MAKE);
                String model = cursor.getString(ProfileDBAdapter.COL_MODEL);

                profileName = year + " " + make + " " + model;
            }
            weight = cursor.getInt(ProfileDBAdapter.COL_WEIGHT);
            engDisp = cursor.getFloat(ProfileDBAdapter.COL_ENGDISP);
            maxRpm = cursor.getInt(ProfileDBAdapter.COL_MAXRPM);
            fuelCap = cursor.getInt(ProfileDBAdapter.COL_FUELCAP);
            profileImage = Uri.parse(cursor.getString(ProfileDBAdapter.COL_IMG));

            // update the header for the navigation drawer
            View headerView = navDrawer.getHeaderView(0);
            ImageView headerImage = (ImageView) headerView.findViewById(R.id.header_profile_image_view);
            headerImage.setImageURI(profileImage);

            Button changeProfileButton = (Button) headerView.findViewById(R.id.header_change_profile_button);
            changeProfileButton.setText(profileName);

            // save active profile to shared preferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("profileId", profileId);
        }
    }

    private void hideSystemUI() {
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    private void showSystemUI() {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }
}
