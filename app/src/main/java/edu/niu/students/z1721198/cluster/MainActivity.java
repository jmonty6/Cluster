package edu.niu.students.z1721198.cluster;

import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity
{
    /* Constants */

    /* Instance Variables */

    private Toolbar appBar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navDrawer;

    /* Methods */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAppBar();
        initNavDrawer();
    }

    private void initAppBar()
    {
        appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
    }

    private void initNavDrawer()
    {
        // set up the navigation drawer menu
        navDrawer = (NavigationView) findViewById(R.id.navigation_drawer);
        navDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item)
                    {
                        if(item.isChecked())
                        {
                            item.setChecked(false);
                        }
                        else
                        {
                            item.setChecked(true);
                        }
                        drawerLayout.closeDrawers();

                        switch(item.getItemId())
                        {
                            case R.id.drawer_item_monitor:
                                return true;

                            case R.id.drawer_item_logger:
                                return true;

                            case R.id.drawer_item_faults:
                                return true;

                            case R.id.drawer_item_settings:
                                return true;

                            case R.id.drawer_item_help:
                                return true;

                            default:
                                return true;
                        }
                    }
                }
        );

        // set up the navigation drawer toggle button
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                appBar, R.string.drawer_open, R.string.drawer_close)
        {
            public void onDrawerClosed(View view)
            {
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

        // set up the profile change button
        ImageButton profileButton = (ImageButton) findViewById(R.id.change_profile_button);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.appbarmenu, menu);
        return super.onCreateOptionsMenu(menu);
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
                hideSystemUI();
                return true;
            // the user's action was not recognized
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hideSystemUI()
    {

    }
}
