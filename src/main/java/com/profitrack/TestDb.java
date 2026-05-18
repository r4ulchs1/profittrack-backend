package com.profitrack;


import java.sql.Connection;
import java.sql.DriverManager;

public class TestDb {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres",
                "postgres.waxivkbmxwfhkwqdpaag",
                "Camarones0902200"
        );
        System.out.println("CONECTADO");
    }
}