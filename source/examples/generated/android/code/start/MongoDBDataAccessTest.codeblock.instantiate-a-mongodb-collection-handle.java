User user = app.currentUser();
MongoClient mongoClient =
        user.getMongoClient("mongodb-atlas"); // service for MongoDB Atlas cluster containing custom user data
MongoDatabase mongoDatabase =
        mongoClient.getDatabase("custom-user-data-database");
MongoCollection<Document> mongoCollection =
        mongoDatabase.getCollection("custom-user-data-collection");
Log.v("EXAMPLE", "Successfully instantiated the MongoDB collection handle");