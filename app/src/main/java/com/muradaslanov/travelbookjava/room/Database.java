package com.muradaslanov.travelbookjava.room;

import androidx.room.RoomDatabase;

import com.muradaslanov.travelbookjava.model.Place;

@androidx.room.Database(entities = {Place.class},version = 1)
public abstract class Database extends RoomDatabase {
    public abstract PlaceDao placeDao();
}
