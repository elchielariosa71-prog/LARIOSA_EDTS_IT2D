package com.scc.model;

public record User(int user_id, String username, String password, String full_name, String roles, String status) {
    @Override
    public String toString(){
         return String.format("[%d] %s %s (%s) - Status: %s", user_id, username, full_name, roles, status);

    }
}
