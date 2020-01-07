package course.labs.contentproviderlab;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import course.labs.contentproviderlab.provider.PlaceBadgesContract;
//import course.labs.locationlab.PlaceDownloaderTask;
//import course.labs.locationlab.PlaceViewActivity;
import course.labs.contentproviderlab.PlaceDownloaderTask;
import course.labs.contentproviderlab.PlaceViewActivity;
//import course.labs.locationlab.R;

public class PlaceViewActivity extends ListActivity implements
		LocationListener, LoaderCallbacks<Cursor> {
	private static final long FIVE_MINS = 5 * 60 * 1000;

	private static String TAG = "Lab-ContentProvider";

	// False if you don't have network access
	public static boolean sHasNetwork = false;

	private boolean mMockLocationOn = false;

	// The last valid location reading
	private Location mLastLocationReading;

	// The ListView's adapter
	// private PlaceViewAdapter mAdapter;
	private PlaceViewAdapter mCursorAdapter;

	// default minimum time between new location readings
	private long mMinTime = 5000;

	// default minimum distance between old and new readings.
	private float mMinDistance = 1000.0f;

	// Reference to the LocationManager
	private LocationManager mLocationManager;

	// A fake location provider used for testing
	private MockLocationProvider mMockLocationProvider;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i("Andre", "Criou o options menu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(getApplicationContext(),
					"External Storage is not available.", Toast.LENGTH_LONG)
					.show();
			finish();
		}

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// TODO - add a footerView to the ListView
		// You can use footer_view.xml to define the footer
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		TextView footerView = (TextView) inflater.inflate(R.layout.footer_view, null, false);
		getListView().addFooterView(footerView);
		
		// Can be removed after implementing the TODO above
		//if (null == footerView ) {
		//	return;
		//}

		// TODO - footerView must respond to user clicks, handling 3 cases:

		// There is no current location - response is up to you. The best
		// solution is to always disable the footerView until you have a
		// location.

		// There is a current location, but the user has already acquired a
		// PlaceBadge for this location - issue a Toast message with the text -
		// "You already have this location badge." 
		// Use the PlaceRecord class' intersects() method to determine whether 
		// a PlaceBadge already exists for a given location

		// There is a current location for which the user does not already have
		// a PlaceBadge. In this case download the information needed to make a new
		// PlaceBadge.

		footerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (PlaceViewActivity.this.getActionBar() == null) {
					Log.i("Andre", "Action bar onClick: null");			
				} else {
					Log.i("Andre", "Action bar onClick: " +  PlaceViewActivity.this.getActionBar().toString());
				}
				
				/*
				Toast.makeText(
						PlaceViewActivity.this ,
						String.valueOf(PlaceViewActivity.this.getListView().getChildCount()),
						Toast.LENGTH_LONG
				).show();
				*/
				
				Log.i("Andre - itens na Litview:", String.valueOf(PlaceViewActivity.this.getListView().getChildCount()));
				
				Log.i(TAG, "Entered footerView.OnClickListener.onClick()");
				//Log.i("Andre", "mLastLocationReading: " + String.valueOf(mLastLocationReading.getLatitude()));				
				if (mLastLocationReading instanceof Location) {
					Log.i("Andre", "mLastLocationReading: " + String.valueOf(mLastLocationReading.getLatitude()));
					//Log.i("Andre", "mCursorAdapter.toString: " + mCursorAdapter.getCursor().toString()); 					
					//Log.i("Andre", "mCursorAdapter.toString: " + mCursorAdapter.g); 					
					//mCursorAdapter.getCursor().moveToFirst();
					
					/*
					while (mCursorAdapter.getCursor().moveToNext()) {
						Log.i(
							"Andre", 
							"mCursorAdapter: " + mCursorAdapter.getCursor().getString(
									mCursorAdapter.getCursor().getColumnIndex(
										PlaceBadgesContract.COUNTRY_NAME
									)
								)
						);
					}
					*/
					if (mCursorAdapter.intersects(mLastLocationReading)) {
						Log.i("Andre", "mLastLocationReading footerView.onClick: " + String.valueOf(mLastLocationReading.getLatitude()));
						Log.i("Andre", "footerView.onClick - mCursorAdapter.getCount(): " + String.valueOf(mCursorAdapter.getCount()));
						Toast.makeText(
							PlaceViewActivity.this,
							"(footerView.onClick) You already have this location badge.",
							Toast.LENGTH_LONG
						).show();
						

						
					} else {
						//Log.i("Andre", "downloading...");						
						//Toast.makeText(PlaceViewActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
						new PlaceDownloaderTask(
								PlaceViewActivity.this, 
								PlaceViewActivity.sHasNetwork
						).execute(mLastLocationReading);
					}
				} else {
					Toast.makeText(
						PlaceViewActivity.this,
						"Location system is not ready",
						Toast.LENGTH_LONG
					).show();
				}
			}
		});

		// TODO - Create and set empty PlaceViewAdapter
		mCursorAdapter = new PlaceViewAdapter(this, null, 0);
		this.setListAdapter(mCursorAdapter);

		// TODO - Initialize the loader
		this.getLoaderManager().initLoader(0, null, this);
		
		if (this.getActionBar() == null) {
			Log.i("Andre", "Action bar onCreate: null");			
		} else {
			Log.i("Andre", "Action bar onCreate: " + this.getActionBar().toString());
		}
		
		//requestWindowFeature(Window.FEATURE_ACTION_BAR);
	}

	@Override
	protected void onResume() {
		Log.i("Andre", "Executado o onResume");			
		
		super.onResume();

		startMockLocationManager();

		// TODO - Check NETWORK_PROVIDER for an existing location reading.
		// Only keep this last reading if it is fresh - less than 5 minutes old
		mLastLocationReading = null;
		Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			if (this.ageInMilliseconds(location) < PlaceViewActivity.FIVE_MINS) {
				mLastLocationReading = location;
			}
		}
		
		// TODO - register to receive location updates from NETWORK_PROVIDER
		mLocationManager.requestLocationUpdates(
			LocationManager.NETWORK_PROVIDER, 
			this.mMinTime, 
			this.mMinDistance, 
			this
		);		
	}

	@Override
	protected void onPause() {

		// TODO - unregister for location updates
		mLocationManager.removeUpdates(this);
		
		shutdownMockLocationManager();
		super.onPause();

	}

	public void addNewPlace(PlaceRecord place) {

		// TODO - Attempt to add place to the adapter, considering the following cases

		// The place is null - issue a Toast message with the text
		// "PlaceBadge could not be acquired"
		// Do not add the PlaceBadge to the adapter
		if (null == place) {
			Toast.makeText(
				PlaceViewActivity.this, 
				"PlaceBadge could not be acquired", 
				Toast.LENGTH_LONG
			).show();
		} 

		// A PlaceBadge for this location already exists - issue a Toast message
		// with the text - "You already have this location badge." Use the PlaceRecord 
		// class' intersects() method to determine whether a PlaceBadge already exists
		// for a given location. Do not add the PlaceBadge to the adapter
		//else if (place.intersects(mLastLocationReading)) {
		else if (mCursorAdapter.intersects(place.getLocation())) {
			Log.i("Andre", "mLastLocationReading addNewPlace: " + String.valueOf(mLastLocationReading.getLatitude()));
			Log.i("Andre", "place: " + String.valueOf(place.getLocation().getLatitude()));	
			
			Toast.makeText(
				PlaceViewActivity.this, 
				"(addNewPlace) You already have this location badge.", 
				Toast.LENGTH_LONG
			).show();
		}
		
		// The place has no country name - issue a Toast message
		// with the text - "There is no country at this location". 
		// Do not add the PlaceBadge to the adapter
		else if ("" == place.getCountryName()) {
			Toast.makeText(
				PlaceViewActivity.this, 
				R.string.no_country_string,
				Toast.LENGTH_LONG
			).show();			
		}
		
		// Otherwise - add the PlaceBadge to the adapter
		else {
			Log.i("Andre", "Place added");			
			mCursorAdapter.add(place);
		}
	}

	// LocationListener methods
	@Override
	public void onLocationChanged(Location currentLocation) {
		// TODO - Update location considering the following cases.
		// 1) If there is no last location, set the last location to the current
		// location.
		// 2) If the current location is older than the last location, ignore
		// the current location
		// 3) If the current location is newer than the last locations, keep the
		// current location.
		Log.i("Andre", "Executado o onLocationChanged");		
		
		if (mLastLocationReading == null || (currentLocation.getTime() > mLastLocationReading.getTime())) {
			mLastLocationReading = currentLocation; 
		}		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// not implemented
	}

	@Override
	public void onProviderEnabled(String provider) {
		// not implemented
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// not implemented
	}

	// LoaderCallback methods
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO - Create a new CursorLoader and return it
		String fields[] = null; //{PlaceBadgesContract.PLACE_NAME, PlaceBadgesContract.FLAG_BITMAP_PATH};
		Log.i("Andre", "Executado o onCreateLoader");
		
		
		return new CursorLoader(this, PlaceBadgesContract.BASE_URI, fields, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> newLoader, Cursor newCursor) {
		// TODO - Swap in the newCursor
		Log.i("Andre", "Executado o onLoadFinished");		
		mCursorAdapter.swapCursor(newCursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> newLoader) {
		// TODO - swap in a null Cursor
		mCursorAdapter.swapCursor(null);
	}

	// Returns age of location in milliseconds
	private long ageInMilliseconds(Location location) {
		return System.currentTimeMillis() - location.getTime();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete_badges:
			mCursorAdapter.removeAllViews();
			return true;
		case R.id.place_one:
			mMockLocationProvider.pushLocation(37.422, -122.084);
			return true;
		case R.id.place_no_country:
			mMockLocationProvider.pushLocation(0, 0);
			return true;
		case R.id.place_two:
			mMockLocationProvider.pushLocation(38.996667, -76.9275);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void shutdownMockLocationManager() {
		if (mMockLocationOn) {
			mMockLocationProvider.shutdown();
		}
	}

	private void startMockLocationManager() {
		if (!mMockLocationOn) {
			mMockLocationProvider = new MockLocationProvider(
					LocationManager.NETWORK_PROVIDER, this);
		}
	}
}
