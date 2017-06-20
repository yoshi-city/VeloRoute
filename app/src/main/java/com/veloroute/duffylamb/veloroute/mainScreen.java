package com.veloroute.duffylamb.veloroute;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.mapzen.android.graphics.MapFragment;
import com.mapzen.android.graphics.MapzenMap;
import com.mapzen.android.graphics.MapzenMapPeliasLocationProvider;
import com.mapzen.android.graphics.OnMapReadyCallback;
import com.mapzen.android.graphics.model.CinnabarStyle;
import com.mapzen.android.search.MapzenSearch;
import com.mapzen.pelias.gson.Feature;
import com.mapzen.pelias.gson.Result;
import com.mapzen.pelias.widget.AutoCompleteAdapter;
import com.mapzen.pelias.widget.AutoCompleteListView;
import com.mapzen.pelias.widget.PeliasSearchView;
import com.mapzen.tangram.LngLat;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class mainScreen extends AppCompatActivity {

	private static final int PERMISSIONS_REQUEST_CODE = 1;
	private MapzenMap map;
	private boolean enableLocationOnResume = false;

	private AutoCompleteListView listView;
	private MapzenMapPeliasLocationProvider peliasLocationProvider;
	private MapzenSearch mapzenSearch;
	private AutoCompleteAdapter autoCompleteAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);

		listView = (AutoCompleteListView) findViewById(R.id.list_view);
		autoCompleteAdapter = new AutoCompleteAdapter(this,android.R.layout.simple_list_item_1);
		listView.setAdapter(autoCompleteAdapter);
		peliasLocationProvider = new MapzenMapPeliasLocationProvider(this);

		final MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
		mapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(MapzenMap map) {
				mainScreen.this.map = map;
				map.setRotation(0f);
				map.setZoom(0);
				map.setTilt(0f);
				map.setPosition(new LngLat(0,0));
				map.setZoomButtonsEnabled(true);
				map.setCompassButtonEnabled(true);
				checkRuntimePermissions();
				map.setStyle(new CinnabarStyle());

				peliasLocationProvider.setMapzenMap(map);
			}
		});

		mapzenSearch = new MapzenSearch(this);
		mapzenSearch.setLocationProvider(peliasLocationProvider);
		PeliasSearchView searchView = (PeliasSearchView) findViewById(R.id.pelias_search_view);
		setupSearchView(searchView);

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (map.isMyLocationEnabled()) {
			map.setMyLocationEnabled(false);
			enableLocationOnResume = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (enableLocationOnResume) {
			map.setMyLocationEnabled(true);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


	private void setupSearchView(PeliasSearchView searchView){
		searchView.setAutoCompleteListView(listView);
		searchView.setPelias(mapzenSearch.getPelias());
		searchView.setCallback(new Callback<Result>() {
			@Override
			public void onResponse(Call<Result> call, Response<Result> response) {
				map.clearSearchResults();
				for (Feature feature : response.body().getFeatures()){
					List<Double> coords = feature.geometry.coordinates;
					LngLat point = new LngLat(coords.get(0), coords.get(1));
					map.setPosition(point);
					map.drawSearchResult(point);
					map.setZoom(16);
				}
			}

			@Override
			public void onFailure(Call<Result> call, Throwable t) {

			}
		});

		searchView.setIconifiedByDefault(false);
		searchView.setQueryHint(this.getString(R.string.search_hint));
		searchView.setOnBackPressListener(new PeliasSearchView.OnBackPressListener() {
			@Override public void onBackPressed() {
				map.clearSearchResults();
			}
		});
	}




	public void checkRuntimePermissions() {
		if (hasLocationPermission()) {
			map.setMyLocationEnabled(true);
		}
		else {
			requestPermission();
		}
	}

	private boolean hasLocationPermission() { // Returns true if permission is not granted #TheArtOftheBodge
		return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
	}

	private void requestPermission() {
		ActivityCompat.requestPermissions(this, new String[]{
				Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
		}, PERMISSIONS_REQUEST_CODE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {


		for(int i = 0; i<grantResults.length; i++){
			if(grantResults[i] == PackageManager.PERMISSION_DENIED)
				return;
		}
		map.setMyLocationEnabled(true);
	}


}






