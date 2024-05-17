package com.example.onguardv3;

import androidx.annotation.NonNull;

import com.wireguard.android.backend.Tunnel;

public class WgTunnel implements Tunnel {

    @NonNull
    @Override
    public String getName() {
        return "wgpreconf1";
    }

    @Override
    public void onStateChange(@NonNull State newState) {
    }


}
