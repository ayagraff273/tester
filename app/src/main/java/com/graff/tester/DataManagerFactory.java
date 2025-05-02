package com.graff.tester;

public class DataManagerFactory {

    public static DatabaseManager getDataManager() {
        switch (AppConfig.CURRENT_DATABASE) {
            case FIREBASE:
                return new FirebaseManager();
            case AWS:
                throw new IllegalArgumentException("The AWS data manager is not yet supported.");
            default:
                throw new IllegalArgumentException("Unsupported data source: "
                        + AppConfig.CURRENT_DATABASE);
        }
    }
}

