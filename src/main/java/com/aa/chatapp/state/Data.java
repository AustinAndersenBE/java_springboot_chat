package com.aa.chatapp.state;

public class Data {
    public record JwtConfig(String secretKey, long validityInMilliseconds) {}

}
