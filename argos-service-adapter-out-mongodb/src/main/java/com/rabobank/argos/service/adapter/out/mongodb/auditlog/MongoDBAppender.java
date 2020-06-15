package com.rabobank.argos.service.adapter.out.mongodb.auditlog;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.bson.Document;

import java.util.Date;
import java.util.stream.Collectors;

public class MongoDBAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private MongoDBConnectionSource connectionSource = null;

    @Override
    protected void append(ILoggingEvent eventObject) {
        Document logEntry = new Document();
        logEntry.append("thread", eventObject.getThreadName());
        logEntry.append("timestamp", new Date(eventObject.getTimeStamp()));
        eventObject.getMDCPropertyMap()
                .entrySet()
                .stream()
                .filter(stringStringEntry -> !stringStringEntry.getKey().equals("arguments"))
                .collect(Collectors.toMap(enntry -> enntry.getKey(), entry -> entry.getValue()))
                .forEach(logEntry::append);
        //arguments have nested json structure
        logEntry.append("arguments", Document.parse(eventObject
                .getMDCPropertyMap()
                .getOrDefault("arguments", "{}")));
        connectionSource.getDBCollection().insertOne(logEntry);
    }

    public void setConnectionSource(MongoDBConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }
}
