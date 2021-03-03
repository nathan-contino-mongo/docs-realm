RealmConfiguration config = new RealmConfiguration.Builder()
        .assetFile("bundled.realm")
        .readOnly() 
        .modules(new BundledRealmModule())
        .build();
Realm realm = Realm.getInstance(config);
