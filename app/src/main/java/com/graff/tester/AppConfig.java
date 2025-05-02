package com.graff.tester;

public class AppConfig {
    public enum DataSourceType {
        FIREBASE,
        AWS //an example
    }

    // Change this value in one place only
    public static final DataSourceType CURRENT_DATABASE = DataSourceType.FIREBASE;
}

