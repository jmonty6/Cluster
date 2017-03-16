package edu.niu.students.z1721198.cluster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ProfileDBAdapter {

    private static final String DB_NAME = "ProfileDB",
            DB_TABLE = "VehicleInfo";
    private static final int DB_VERSION = 1;

    /* Name of each column in the VehicleInfo table */
    private static final String TAG = "ProfileDBAdapter";
    private static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_YEAR = "year";
    public static final String KEY_MAKE = "make";
    public static final String KEY_MODEL = "model";
    public static final String KEY_WEIGHT = "weight";
    public static final String KEY_FUELCAP = "fuelcap";
    public static final String KEY_ENGDISP = "engdisp";
    public static final String KEY_MAXRPM = "maxrpm";
    public static final String KEY_IMG = "img";

    /* Array of each column name */
    private static final String[] ALL_KEYS = new String[] {
            KEY_ROWID, KEY_NAME, KEY_YEAR, KEY_MAKE, KEY_MODEL, KEY_WEIGHT,
            KEY_FUELCAP, KEY_ENGDISP, KEY_MAXRPM, KEY_IMG
    };

    /* Integer value for each column in order */
    public static final int COL_ROWID = 0,
            COL_NAME = 1,
            COL_YEAR = 2,
            COL_MAKE = 3,
            COL_MODEL = 4,
            COL_WEIGHT = 5,
            COL_FUELCAP = 6,
            COL_ENGDISP = 7,
            COL_MAXRPM = 8,
            COL_IMG = 9;

    /* String to create the VehicleInfo table */
    private static final String CREATE_SQL = "CREATE TABLE " + DB_TABLE + "("
            + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_NAME + " STRING, "
            + KEY_YEAR + " STRING, "
            + KEY_MAKE + " STRING, "
            + KEY_MODEL + " STRING, "
            + KEY_WEIGHT + " INTEGER, "
            + KEY_ENGDISP + " REAL, "
            + KEY_MAXRPM + " INTEGER, "
            + KEY_FUELCAP + " REAL, "
            + KEY_IMG + " STRING);";

    /* String to insert default row */
    private static final String INSERT_DEFAULT_PROFILE_SQL = "INSERT INTO " + DB_TABLE + "("
            + KEY_NAME + ", "
            + KEY_YEAR + ", "
            + KEY_MAKE + ", "
            + KEY_MODEL + ", "
            + KEY_WEIGHT + ", "
            + KEY_ENGDISP + ", "
            + KEY_MAXRPM + ", "
            + KEY_FUELCAP + ", "
            + KEY_IMG
            + ") VALUES ('Default Profile', '---', '---', '', 0, 0, 0, 0, "
            + "'android.resource://edu.niu.students.z1721198.cluster/" + R.drawable.defaultprofile + "');";

    /* Instance Variables */
    private ProfileDBHelper profileDBHelper;
    private SQLiteDatabase db;

    /* Constructor */
    public ProfileDBAdapter(Context context) {
        profileDBHelper = new ProfileDBHelper(context);
    }

    /* Methods */
    public ProfileDBAdapter open() {
        db = profileDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        profileDBHelper.close();
    }

    public long insertRow(String name, String year, String make, String model, int weight, float engdisp,
                          int maxrpm, float fuelcap, String img) {
        ContentValues rowValues = new ContentValues();
        rowValues.put(KEY_NAME, name);
        rowValues.put(KEY_YEAR, year);
        rowValues.put(KEY_MAKE, make);
        rowValues.put(KEY_MODEL, model);
        rowValues.put(KEY_WEIGHT, weight);
        rowValues.put(KEY_ENGDISP, engdisp);
        rowValues.put(KEY_MAXRPM, maxrpm);
        rowValues.put(KEY_FUELCAP, fuelcap);
        rowValues.put(KEY_IMG, img);

        return db.insert(DB_TABLE, null, rowValues);
    }

    public boolean updateRow(long rowId, String name, String year, String make, String model, int weight,
                             float engdisp, int maxrpm, float fuelcap, String img) {
        ContentValues rowValues = new ContentValues();
        rowValues.put(KEY_NAME, name);
        rowValues.put(KEY_YEAR, year);
        rowValues.put(KEY_MAKE, make);
        rowValues.put(KEY_MODEL, model);
        rowValues.put(KEY_WEIGHT, weight);
        rowValues.put(KEY_ENGDISP, engdisp);
        rowValues.put(KEY_MAXRPM, maxrpm);
        rowValues.put(KEY_FUELCAP, fuelcap);
        rowValues.put(KEY_IMG, img);

        String where = KEY_ROWID + " = " + rowId;
        return db.update(DB_TABLE, rowValues, where, null) != 0;
    }

    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + " = " + rowId;
        return db.delete(DB_TABLE, where, null) != 0;
    }

    public void deleteAllRows() {
        Cursor cursor = getAllRows();
        long rowId = cursor.getColumnIndexOrThrow(KEY_ROWID);

        boolean result = cursor.moveToFirst();
        while(result) {
            deleteRow(cursor.getInt((int) rowId));
            result = cursor.moveToNext();
        }
    }

    public Cursor getAllRows() {
        String where = null;
        Cursor cursor = db.query(true, DB_TABLE, ALL_KEYS, where, null, null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + " = " + rowId;
        Cursor cursor = db.query(true, DB_TABLE, ALL_KEYS, where, null, null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    /* Inner Class ProfileDBHelper */
    private static class ProfileDBHelper extends SQLiteOpenHelper {
        Context context;

        /* Constructor */
        public ProfileDBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this.context = context;
        }

        /* Methods */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // create the vehicleinfo table
            db.execSQL(CREATE_SQL);
            // insert the first row for a default profile
            db.execSQL(INSERT_DEFAULT_PROFILE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's Profile Database from Version " + oldVersion +
                " to " + newVersion + ". This will destroy all prior data.");
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(db);
        }
    }
}
