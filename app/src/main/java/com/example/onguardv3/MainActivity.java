package com.example.onguardv3;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import static com.wireguard.android.backend.Tunnel.State.DOWN;
import static com.wireguard.android.backend.Tunnel.State.UP;

import com.wireguard.android.backend.Backend;
import com.wireguard.android.backend.GoBackend;
import com.wireguard.config.Config;
import com.wireguard.config.InetEndpoint;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.Interface;
import com.wireguard.config.Peer;
import com.wireguard.android.backend.Tunnel;

public class MainActivity extends AppCompatActivity {

    private Backend backend;

    private ActivityResultLauncher<Intent> vpnPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize backend
        backend = PersistentConnectionProperties.getInstance().getBackend();
        if (backend == null) {
            PersistentConnectionProperties.getInstance().setBackend(new GoBackend(this));
            backend = PersistentConnectionProperties.getInstance().getBackend();
        }


        // Initialize vpnPermissionLauncher
        vpnPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Call connect again to proceed with VPN setup after permission is granted
                        connect(null);
                    }
                });

        // Attempt to get running tunnel names to check backend status
        try {
            backend.getRunningTunnelNames();
        } catch (Exception e) {
            Log.e("MainActivity", "Backend not initialized properly", e);
        }
    }


    public void connect(View v) {
        TunnelModel tunnelModel = DataSource.getTunnelModel();
        Tunnel tunnel =  PersistentConnectionProperties.getInstance().getTunnel();

        Intent intentPrepare = GoBackend.VpnService.prepare(this);
        if (intentPrepare != null) {
            vpnPermissionLauncher.launch(intentPrepare);
            return; // Exit to wait for permission result
        }

        Interface.Builder interfaceBuilder = new Interface.Builder();
        Peer.Builder peerBuilder = new Peer.Builder();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (backend.getState(tunnel) == UP) {
                        backend.setState(tunnel, DOWN, null);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show());
                    } else {
                        backend.setState(tunnel, UP, new Config.Builder()
                                .setInterface(interfaceBuilder.addAddress(InetNetwork.parse(tunnelModel.IP)).parsePrivateKey(tunnelModel.privateKey).build())
                                .addPeer(peerBuilder.addAllowedIps(tunnelModel.allowedIPs).setEndpoint(InetEndpoint.parse(tunnelModel.endpoint)).parsePublicKey(tunnelModel.publicKey).build())
                                .build());
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Failed to setup VPN", e);
                }
            }
        });
    }
    public void disconnect(View v) {
        Tunnel tunnel = PersistentConnectionProperties.getInstance().getTunnel();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (backend.getState(tunnel) == UP) {
                        backend.setState(tunnel, DOWN, null);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Already Disconnected", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Failed to disconnect VPN", e);
                }
            }
        });
    }
}
