package com.begers.seyahatdefterim.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Place implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "langitude")
    public Double langitude;

    public Place(String name, Double latitude, Double langitude) {
        this.name = name;
        this.latitude = latitude;
        this.langitude = langitude;
    }
}
