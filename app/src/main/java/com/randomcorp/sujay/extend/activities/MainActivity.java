package com.randomcorp.sujay.extend.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.randomcorp.sujay.extend.R;
import com.randomcorp.sujay.extend.Utils.SettingsDialogue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PREFERENCES = "EXTENDUSERPREFS";
    private static final String FIRSTUSE = "FIRST";
    private final String DEVICENAME = "DEVNAME";
    private final String USERNAME = "USERNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFERENCES,0);
        findViewById(R.id.b_server).setOnClickListener(this);
        findViewById(R.id.b_client).setOnClickListener(this);
        if(!pref.contains(FIRSTUSE))
        {
            SettingsDialogue dialogue = new SettingsDialogue();
            dialogue.show(getSupportFragmentManager(),"settings");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            SettingsDialogue dialogue = new SettingsDialogue();
            dialogue.show(getSupportFragmentManager(),"settings");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId())
        {
            case R.id.b_client:
                SharedPreferences pref = getApplicationContext().getSharedPreferences(PREFERENCES,0);
                if(!pref.contains(FIRSTUSE))
                    Toast.makeText(this,"Please Set Username and Devicename",Toast.LENGTH_SHORT).show();
                else
                {
                    String username = pref.getString(USERNAME,"USERNAME");
                    String devicename = pref.getString(DEVICENAME,"DEVICENAME");
                    i = new Intent(this,ClientActivity.class);
                    i.putExtra(USERNAME,username);
                    i.putExtra(DEVICENAME,devicename);
                    startActivity(i);
                }

                break;
            case R.id.b_server:
                i = new Intent(this,ClientsSelectionActivity.class);
                startActivity(i);
                break;
        }
    }
}
