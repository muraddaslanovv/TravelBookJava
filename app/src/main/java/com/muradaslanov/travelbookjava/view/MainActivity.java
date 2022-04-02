package com.muradaslanov.travelbookjava.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.muradaslanov.travelbookjava.R;
import com.muradaslanov.travelbookjava.adapter.PlaceAdaptor;
import com.muradaslanov.travelbookjava.databinding.ActivityMainBinding;
import com.muradaslanov.travelbookjava.model.Place;
import com.muradaslanov.travelbookjava.room.Database;
import com.muradaslanov.travelbookjava.room.PlaceDao;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    Database database;
    PlaceDao placeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        database = Room.databaseBuilder(getApplicationContext(),
                com.muradaslanov.travelbookjava.room.Database.class, "Places")
//                .allowMainThreadQueries()
                .build();

        placeDao = database.placeDao();

        compositeDisposable.add(placeDao.getEverything()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MainActivity.this::handleResponse)
        );


    }

    private void handleResponse(List<Place> placeList){
    binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PlaceAdaptor placeAdaptor = new PlaceAdaptor(placeList);
        binding.recyclerView.setAdapter(placeAdaptor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.travelmenu,menu);
            return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.addItem){
            Intent intent = new Intent(this,MapsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}