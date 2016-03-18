package edu.niu.students.z1721198.cluster;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    /* Constants */

    static final int REQUEST_ENABLE_BT = 1;

    /* Instance Variables */

    private Toolbar appBar;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView menuList;
    private String[] menuItems;

    private BluetoothAdapter adap;

    /* Methods */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAppBar();
        initNavDrawer();
        if(savedInstanceState == null)
        {
            selectItem(0);
        }

        initBluetooth();
    }

    private void initAppBar()
    {
        appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
    }

    private void initNavDrawer()
    {
        // set up the navigation menu and toggle button
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                appBar, R.string.drawer_open, R.string.drawer_close)
        {
            public void onDrawerClosed(View view)
            {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(getTitle());
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getTitle());
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        // set the app bar's up button to behave as the navigation drawer button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // set up the menu inside the nav drawer
        menuItems = getResources().getStringArray(R.array.menu_array);
        menuList = (ListView) findViewById(R.id.drawer_list);
        menuList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuItems));
        menuList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private void initBluetooth()
    {/*
        adap = BluetoothAdapter.getDefaultAdapter();
        if(adap == null)
        {
            Toast.makeText(getApplicationContext(), "No Bluetooth Radio found. This application requires " +
                    "a Bluetooth Radio to function", Toast.LENGTH_LONG).show();
        }
        if(!adap.isEnabled())
        {
            Intent btOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btOn, REQUEST_ENABLE_BT);
        }
     */
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbarmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    public boolean onPrepareOptionsMenu(Menu menu)
    {
        boolean drawerOpen = drawerLayout.isDrawerOpen(menuList);
        menu.findItem(R.id.action_add).setVisible(!drawerOpen);
        menu.findItem(R.id.action_fullscreen).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        switch(item.getItemId())
        {
            // user pressed the add button
            case R.id.action_add:
                return true;
            // user pressed the fullscreen button
            case R.id.action_fullscreen:
                // hide the title and set the app to fullscreen
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                return true;
            // the user's action was not recognized
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            selectItem(position);
        }
    }

    private void selectItem(int position)
    { /*
                        Fragment fragment = new Fragment();
                        Bundle args = new Bundle();
                        args.putInt(Fragment.ARG_PLANET_NUMBER, position);
                        fragment.setArguments(args);

                        // Insert the fragment by replacing any existing fragment
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

                        // Highlight the selected item, update the title, and close the drawer
                        drawerList.setItemChecked(position, true);
                        setTitle(mPlanetTitles[position]);
                        drawerLayout.closeDrawer(drawerList); */
    }
}
