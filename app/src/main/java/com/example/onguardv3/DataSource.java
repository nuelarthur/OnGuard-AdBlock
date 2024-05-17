package com.example.onguardv3;

public class DataSource {

    private static final String JSON_STRING = "{\"user_info\":{\"username\":\"redacted\",\"password\":\"redacted\",\"vpn-name\":\"redacted\",\"PrivateKey\":\"IMKLl1e02pUYYe/OZo0oF7TsqTcCixgSOqFzKpNcVVU=\",\"Address\":\"10.149.29.2/24\",\"DNS\":\"10.149.29.1\",\"PublicKey\":\"0B2e8IdGdp67EjbefX5l0FcFpc8Krje/mW4CSXD6Wxc=\",\"AllowedIPs\":\"0.0.0.0/0\",\"Endpoint\":\"ec2-52-91-218-73.compute-1.amazonaws.com:51820\",\"status\":\"Active\",\"vpnConnection\":true},\"server_info\":{\"url\":\"10.149.29.1\"}}";

    public static TunnelModel getTunnelModel() {
        return TunnelDecoder.decode(JSON_STRING);
    }
}
