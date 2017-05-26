package com.aj.sendall.greendao;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.ToOne;

public class GreenDaoGenerator {

    public static void main(String[] args){
        Schema schema = new Schema(4, "com.aj.sendall.dal.model");
        schema.setDefaultJavaPackageDao("com.aj.sendall.dal.dao");
        schema.setDefaultJavaPackageTest("com.aj.sendall.dal.daotest");

        //Connections
        Entity connection = schema.addEntity("Connections");
        connection.addLongProperty("connectionId").primaryKey().autoincrement().getProperty();
        connection.addStringProperty("connectionName").notNull();
        connection.addStringProperty("SSID").notNull();
        connection.addStringProperty("password").notNull();

        //PersonalInteractions
        Entity personalInteraction = schema.addEntity("PersonalInteraction");
        personalInteraction.addLongProperty("personalInteractionId").primaryKey().autoincrement();
        Property persIntConnIdProp = personalInteraction.addLongProperty("connectionId").notNull().getProperty();
        personalInteraction.addStringProperty("fileUri").notNull();
        personalInteraction.addIntProperty("mediaType").notNull();
        personalInteraction.addIntProperty("fileStatus").customType(
                "com.aj.sendall.dal.enums.FileStatus", "com.aj.sendall.dal.converters.FileStatusToIntConverter");
        personalInteraction.addDateProperty("modifiedTime").notNull();
        personalInteraction.addStringProperty("fileName").notNull();
        personalInteraction.addStringProperty("fileSize");

        connection.addToMany(personalInteraction, persIntConnIdProp, "personalInters");

        try {
            new DaoGenerator().generateAll(schema, "/home/ajilal/AndroidStudioProjects/SendAll/app/src/main/java");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
