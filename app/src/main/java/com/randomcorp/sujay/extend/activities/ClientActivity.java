package com.randomcorp.sujay.extend.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.randomcorp.sujay.extend.R;
import com.randomcorp.sujay.extend.networking.ClientExtendProtocol;

/**
 * Created by sujay on 21/9/15.
 */
public class ClientActivity extends AppCompatActivity implements ClientExtendProtocol.CommandFromServer {
    ClientExtendProtocol clientProtocol;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity);
        clientProtocol = new ClientExtendProtocol(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        clientProtocol.startClientProtocol();
    }

    @Override
    protected void onPause() {
        super.onPause();
        clientProtocol.stopClientProtocol();
    }

    @Override
    public void commandReceived(String type, String data)
    {
        ((TextView)findViewById(R.id.tv_received)).setText(data);
    }
}
