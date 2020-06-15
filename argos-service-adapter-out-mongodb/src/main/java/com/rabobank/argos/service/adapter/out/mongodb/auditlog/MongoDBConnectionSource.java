package com.rabobank.argos.service.adapter.out.mongodb.auditlog;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoDBConnectionSource {
    private volatile MongoCollection dbCollection = null;

    private String uri = null;

    private String db = null;

    private String collection = null;

    protected MongoCollection getDBCollection() {
        MongoCollection dbCollectionHelper = dbCollection;
        if (dbCollectionHelper == null) {
            synchronized (this) {
                dbCollectionHelper = dbCollection;
                if (dbCollectionHelper == null) {
                    try {
                        final MongoClient client = MongoClients.create(uri);
                        dbCollection = client.getDatabase(db).getCollection(collection);
                        Runtime.getRuntime().addShutdownHook(
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        client.close();
                                    }
                                }, "mongo shutdown"));
                    } catch (MongoException mongoException) {
                        log.error("mongoException {}", mongoException.getMessage());
                    }
                }
            }
        }
        return dbCollection;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }
}
