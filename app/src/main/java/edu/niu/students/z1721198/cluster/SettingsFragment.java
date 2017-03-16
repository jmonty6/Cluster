package edu.niu.students.z1721198.cluster;

import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment {

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter btAdapter;
    CharSequence[] btDeviceNames;
    CharSequence[] btDeviceAddresses;

    private int xmlId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // display the main preferences menu
        xmlId = R.xml.preferences;
        addPreferencesFromResource(R.xml.preferences);

        // initialize bluetooth for the bluetooth preferences
        initBluetooth();

        // change the theme immediately if it is changed in preferences
        final ListPreference themePreference = (ListPreference) findPreference(SettingsActivity.PREF_DISPLAY_THEME_KEY);
        themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                TaskStackBuilder.create(getActivity())
                        .addNextIntent(new Intent(getActivity(), MainActivity.class))
                        .addNextIntent(getActivity().getIntent())
                        .startActivities();
                return true;
            }
        });
    }

    private void initBluetooth() {
        final SwitchPreference btEnabledPreference = (SwitchPreference) findPreference(SettingsActivity.PREF_BT_ENABLED_KEY);
        final ListPreference btSelectPreference = (ListPreference) findPreference(SettingsActivity.PREF_BT_SELECT_KEY);
        final Preference btPairPreference = findPreference(SettingsActivity.PREF_BT_PAIR_KEY);

        // get the device's bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        // if the device does not have a bluetooth adapter, disable all bluetooth preferences
        if(btAdapter == null) {
            btEnabledPreference.setEnabled(false);
            btEnabledPreference.setSelectable(false);
            btPairPreference.setEnabled(false);
            btPairPreference.setSelectable(false);
            btSelectPreference.setEnabled(false);
            btSelectPreference.setSelectable(false);
        }
        // if the adapter exists, set up the bluetooth preferences
        else {
            // set the Bluetooth Enabled preference to enable/disable bluetooth
            btEnabledPreference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            // if the preference was turned on, enable bluetooth and its preferences
                            if(btEnabledPreference.isChecked()) {
                                btAdapter.enable();

                                btPairPreference.setEnabled(true);
                                btPairPreference.setSelectable(true);
                                btSelectPreference.setEnabled(true);
                                btSelectPreference.setSelectable(true);

                            }
                            // if the preference was just turned off, disable bluetooth and its preferences
                            else {
                                btAdapter.disable();

                                btPairPreference.setEnabled(false);
                                btPairPreference.setSelectable(false);
                                btSelectPreference.setEnabled(false);
                                btSelectPreference.setSelectable(false);
                            }

                            return false;
                        }
                    }
            );

            // if bluetooth is currently disabled, disable all bluetooth preferences except the enable switch
            if(!btAdapter.isEnabled()) {
                btEnabledPreference.setChecked(false);

                btPairPreference.setEnabled(false);
                btPairPreference.setSelectable(false);
                btSelectPreference.setEnabled(false);
                btSelectPreference.setSelectable(false);
            }
            // if bluetooth is enabled, set up the rest of the preferences
            else {
                btEnabledPreference.setChecked(true);

                // get a Set of the currently paired devices
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                // if the set has at least one item in it, extract information from the devices
                if (pairedDevices.size() > 0) {
                    // store the devices' names and addresses
                    btDeviceNames = new CharSequence[pairedDevices.size()];
                    btDeviceAddresses = new CharSequence[pairedDevices.size()];

                    int i = 0;
                    for (BluetoothDevice device : pairedDevices) {
                        btDeviceNames[i] = device.getName();
                        btDeviceAddresses[i] = device.getAddress();
                        i++;
                    }

                    btSelectPreference.setEntries(btDeviceNames);
                    btSelectPreference.setEntryValues(btDeviceAddresses);
                }

                // set the behavior for bluetooth pairing
                btPairPreference.setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                return false;
                            }
                        }
                );
            }
        }
    }

    public void changedPrefScreen(int xmlId, int titleId) {
        this.xmlId = xmlId;

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getActivity().getResources().getString(titleId));

        getPreferenceScreen().removeAll();
        addPreferencesFromResource(xmlId);
    }

    public void onUpButton() {
        if(xmlId == R.xml.preferences) {
            getActivity().finish();
        }
        else {
            changedPrefScreen(R.xml.preferences, R.string.title_settings);
            initBluetooth();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();

        if(key.equals(getString(R.string.pref_general_obd2_key))) {
            changedPrefScreen(R.xml.obd2_preferences, R.string.title_obd2_settings);
        }
        else if(key.equals(getString(R.string.pref_general_units_key))) {
            changedPrefScreen(R.xml.units_preferences, R.string.title_units_settings);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
