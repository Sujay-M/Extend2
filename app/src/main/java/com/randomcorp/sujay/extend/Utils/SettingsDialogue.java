package com.randomcorp.sujay.extend.Utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.randomcorp.sujay.extend.R;

/**
 * Created by sujay on 3/10/15.
 */
public class SettingsDialogue extends DialogFragment
{
    final String PREFERENCES = "EXTENDUSERPREFS";
    final String FIRSTUSE = "FIRST";
    final String DEVICENAME = "DEVNAME";
    final String USERNAME = "USERNAME";
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(PREFERENCES, 0);
        final SharedPreferences.Editor editor = pref.edit();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.settings_dialogue, null);
        builder.setTitle(R.string.setting_dialogue)
        .setView(v)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String username = ((EditText) ((Dialog) dialog).findViewById(R.id.et_username)).getText().toString();
                        String devicename = ((EditText) ((Dialog) dialog).findViewById(R.id.et_devicename)).getText().toString();
                        editor.putString(USERNAME, username);
                        editor.putString(DEVICENAME, devicename);
                        editor.putBoolean(FIRSTUSE, false);
                        editor.apply();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SettingsDialogue.this.getDialog().cancel();
                    }
                });
        if(pref.contains(FIRSTUSE))
        {

            ((EditText)(v.findViewById(R.id.et_username))).setText(pref.getString(USERNAME, "USER NAME"));
            ((EditText)(v.findViewById(R.id.et_devicename))).setText(pref.getString(DEVICENAME,"DEVICE NAME"));
        }
        return builder.create();
    }
}
