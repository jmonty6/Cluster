package edu.niu.students.z1721198.cluster;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    /* Constants */
    private static final int PROFILE_IMAGE_REQUEST = 1;

    /* Instance Variables */

    private Intent intent;
    private boolean profileExists;
    private long profileId;
    private String profileName, year, make, model;
    private int weight, maxRpm;
    private float engDisp, fuelCap;

    private ProfileDBAdapter profileDB;

    private Toolbar appBar;
    private ScrollView scrollView;
    private ImageView profileImageView;
    private Button profileImageButton;
    private EditText profileNameET, modelET, weightET, engDispET, maxRpmET, fuelCapET;
    private Spinner yearSpinner, makeSpinner;

    /* Methods */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialize the user's preferences
        initPreferences();

        // set the content view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // initialize the database connection
        initDB();

        // initialize the app bar
        initAppBar();

        // instantiate views
        initViews();

        // find out if the user is adding or editing a profile
        intent = getIntent();
        profileExists = intent.getBooleanExtra("profileExists", false);
        // if they are editing, populate each view with the current profile information
        if(profileExists) {
            profileId = intent.getLongExtra("profileId", -1);
            populateFields(profileId);
        }
    }

    @Override
    protected void onDestroy() {
        profileDB.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // user pressed the save profile button
            case R.id.action_save:
                boolean success;
                // if the user is editing a profile, edit the current row
                if(profileExists) { success = editProfile(); }
                // if the user is adding a new profile, add a new row
                else { success = addProfile(); }

                // set the proper result code
                if(success) { setResult(RESULT_OK, intent); }
                else { setResult(RESULT_CANCELED, intent); }

                // pass the profileId of the inserted row back to MainActivity and finish
                intent.putExtra("profileId", profileId);
                intent.putExtra("profileSaved", true);
                finish();
                return true;

            // user pressed the delete profile button
            case R.id.action_delete:
                // if the profile exists, delete its row from the database
                if(profileExists) {
                    success = profileDB.deleteRow(profileId);

                    // set the proper result code
                    if(success) { setResult(RESULT_OK, intent); }
                    else { setResult(RESULT_CANCELED, intent); }

                    // pass the deleted profileId back to MainActivity and let it know the profile was deleted
                    intent.putExtra("profileId", profileId);
                    intent.putExtra("profileSaved", false);
                    finish();
                }
                // if the profile hasn't been added yet, prompt the user
                else {
                    Toast.makeText(this, "This profile hasn't been created yet!", Toast.LENGTH_SHORT).show();
                }
                return true;

            // the user's action was not recognized
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // if returning from selecting a profile image, put the image in profileImageView
            case PROFILE_IMAGE_REQUEST:
                if(resultCode == RESULT_OK) {
                    // get the URI of the selected image
                    Uri imageUri = data.getData();
                    try {
                        // get a bitmap from the URI and put it in profileImageView
                        Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        profileImageView.setImageBitmap(selectedImage);
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private void initPreferences() {
        // set the app's theme based on the stored value in SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = preferences.getString(SettingsActivity.PREF_DISPLAY_THEME_KEY, null);
        if(theme.equals("0")) {
            setTheme(R.style.AppTheme_Light);
        }
        else if(theme.equals("1")) {
            setTheme(R.style.AppTheme_Dark);
        }
    }

    private void initDB() {
        // open the database
        profileDB = new ProfileDBAdapter(this);
        profileDB.open();
    }

    private void initAppBar() {
        appBar = (Toolbar) findViewById(R.id.app_bar_profiles);
        setSupportActionBar(appBar);

        appBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        appBar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // go back to MainActivity
                        finish();
                    }
                }
        );
    }

    private void initViews() {
        // create the ScrollView objects
        scrollView = (ScrollView) findViewById(R.id.scroll_view);

        // create the ImageButton and ImageView objects
        profileImageView = (ImageView) findViewById(R.id.prof_image_view);
        profileImageView.setImageResource(R.drawable.defaultprofile);
        profileImageButton = (Button) findViewById(R.id.prof_image_button);
        profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send the user to their image library to select a photo
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PROFILE_IMAGE_REQUEST);
            }
        });

        // create the EditText objects
        profileNameET = (EditText) findViewById(R.id.prof_name_et);
        modelET = (EditText) findViewById(R.id.prof_model_et);
        weightET = (EditText) findViewById(R.id.prof_weight_et);
        engDispET = (EditText) findViewById(R.id.prof_engdisp_et);
        maxRpmET = (EditText) findViewById(R.id.prof_maxrpm_et);
        fuelCapET = (EditText) findViewById(R.id.prof_fuelcap_et);

        // create and populate the year spinner
        yearSpinner = (Spinner) findViewById(R.id.prof_year_spinner);
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this, R.array.prof_year_array,
                android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // create and populate the make spinner
        makeSpinner = (Spinner) findViewById(R.id.prof_make_spinner);
        ArrayAdapter<CharSequence> makeAdapter = ArrayAdapter.createFromResource(this, R.array.prof_make_array,
                android.R.layout.simple_spinner_item);
        makeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        makeSpinner.setAdapter(makeAdapter);
    }

    private void populateFields(long rowId) {
        // get a cursor to the row with the given profileId
        Cursor cursor = profileDB.getRow(profileId);
        if(cursor.moveToFirst()) {
            // populate each field with the values in each column
            profileNameET.setText(cursor.getString(ProfileDBAdapter.COL_NAME));
            modelET.setText(cursor.getString(ProfileDBAdapter.COL_MODEL));
            weightET.setText(cursor.getString(ProfileDBAdapter.COL_WEIGHT));
            engDispET.setText(cursor.getString(ProfileDBAdapter.COL_ENGDISP));
            maxRpmET.setText(cursor.getString(ProfileDBAdapter.COL_MAXRPM));
            fuelCapET.setText(cursor.getString(ProfileDBAdapter.COL_FUELCAP));

            // each spinner sets its selection index to the index of the matching string in an array in strings.xml
            List<String> yearArray = Arrays.asList(getResources().getStringArray(R.array.prof_year_array));
            yearSpinner.setSelection(yearArray.indexOf(cursor.getString(ProfileDBAdapter.COL_YEAR)));
            List<String> makeArray = Arrays.asList(getResources().getStringArray(R.array.prof_make_array));
            makeSpinner.setSelection(makeArray.indexOf(cursor.getString(ProfileDBAdapter.COL_MAKE)));

            // get the profile's image from internal storage
            try {
                FileInputStream fiStream = openFileInput(profileId + "");
                Bitmap bitmap = BitmapFactory.decodeStream(fiStream);
                profileImageView.setImageBitmap(bitmap);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseFields() {
        // parse the fields the user populated
        profileName = profileNameET.getText().toString();
        year = yearSpinner.getSelectedItem().toString();
        make = makeSpinner.getSelectedItem().toString();
        model = modelET.getText().toString();
        weight = 0;
        if(!weightET.getText().toString().equals("")) {
            weight = Integer.parseInt(weightET.getText().toString());
        }
        engDisp = 0;
        if(!engDispET.getText().toString().equals("")) {
            engDisp = Float.parseFloat(engDispET.getText().toString());
        }
        maxRpm = 0;
        if(!maxRpmET.getText().toString().equals("")) {
            maxRpm = Integer.parseInt(maxRpmET.getText().toString());
        }
        fuelCap = 0;
        if(!fuelCapET.getText().toString().equals("")) {
            fuelCap = Float.parseFloat(fuelCapET.getText().toString());
        }
    }

    private boolean addProfile() {
        // pass the value of each field the user populated into instance variables
        parseFields();
        // insert the row with the data collected from the each field and store the id of the inserted row
        profileId = profileDB.insertRow(profileName, year, make, model, weight, engDisp, maxRpm, fuelCap);

        // if the row insertion was successful, save the profile image to internal storage
        if(profileId != -1) {
            saveProfileImage();
            return true;
        }
        return false;
    }

    private boolean editProfile() {
        // pass the value of each field the user populated into instance variables
        parseFields();
        // update the row with the new values collected from each field
        boolean success = profileDB.updateRow(profileId, profileName, year, make, model, weight, engDisp, maxRpm, fuelCap);

        // if the update was successful, save the profile image to internal storage
        if(success) {
            saveProfileImage();
            return true;
        }
        return false;
    }

    private boolean deleteProfile() {
        return true;
    }

    private void saveProfileImage() {
        // get a byte[] containing the image data in profileImageView
        Bitmap bitmap = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baoStream);
        byte[] bitmapBytes = baoStream.toByteArray();

        // save the profile image data to internal storage with the same name as the profileId
        String filename = profileId + "";
        try {
            FileOutputStream foStream = openFileOutput(filename, Context.MODE_PRIVATE);
            foStream.write(bitmapBytes);
            foStream.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}


