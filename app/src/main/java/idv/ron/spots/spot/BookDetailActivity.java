package idv.ron.spots.spot;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import idv.ron.spots.R;
import idv.ron.spots.main.Common;

public class BookDetailActivity extends AppCompatActivity {
    private final static String TAG = "SpotDetailActivity";
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_detail_activity);
//        initMap();
        Spot spot = (Spot) this.getIntent().getExtras().getSerializable("spot");
        if (spot == null) {
            Common.showToast(BookDetailActivity.this, R.string.msg_NoSpotsFound);
        } else {
            showMap(spot);
        }
    }

    private void initMap() {
        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fmMap)).getMap();
            if (map == null) {
                Common.showToast(this, R.string.msg_MapNotDisplayed);
                finish();
                return;
            }
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    private void showMap(Spot spot) {
        LatLng position = new LatLng(spot.getLatitude(), spot.getLongitude());
        String snippet = getString(R.string.col_Name) + ": " + spot.getName() + "\n" +
                getString(R.string.col_PhoneNo) + ": " + spot.getPhoneNo() + "\n" +
                getString(R.string.col_Address) + ": " + spot.getAddress();

        // focus on the spot
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(9)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);

        // add spot on the map
        map.addMarker(new MarkerOptions()
                .position(position)
                .title(spot.getName())
                .snippet(snippet));

        map.setInfoWindowAdapter(new MyInfoWindowAdapter(this, spot));
    }

    private class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View infoWindow;
        private Spot spot;


        MyInfoWindowAdapter(Context context, Spot spot) {
            infoWindow = LayoutInflater.from(context).inflate(
                    R.layout.spot_detail_infowindow, null);
            this.spot = spot;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            ImageView imageView = (ImageView) infoWindow.findViewById(R.id.imageView);
            String url = Common.URL + "SpotServlet";
            int id = spot.getId();
            int imageSize = 400;
            Bitmap bitmap = null;
            try {
                // passing null and calling get() means not to run FindImageByIdTask.onPostExecute()
                bitmap = new SpotGetImageTask(null).execute(url, id, imageSize).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.default_image);
            }
            TextView tvTitle = (TextView) infoWindow.findViewById(R.id.tvTitle);
            tvTitle.setText(marker.getTitle());

            TextView tvSnippet = (TextView) infoWindow.findViewById(R.id.tvSnippet);
            tvSnippet.setText(marker.getSnippet());
            return infoWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}
