package com.example.pawmart;

public class User {
    public String uid;
    public String name;
    public String email;
    public String address;

    public User() {}

    public User(String uid, String name, String email, String address) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.address = address;
    }
}