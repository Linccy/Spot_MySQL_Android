package idv.ron.spots.main;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import idv.ron.spots.R;
import idv.ron.spots.news.NewsActivity;
import idv.ron.spots.spot.SpotListActivity;

public class Common {
//    public static String URL = "http://192.168.196.189:8080/Spot_MySQL_Web/";
    public static String URL = "http://192.168.0.106:8080/Book_MySQL_Web/";

    public final static Category[] CATEGORIES = {
            new Category(0, "News", R.drawable.news, NewsActivity.class),
            new Category(1, "Book List", R.drawable.spot, SpotListActivity.class),
    };

    // check if the device connect to the network
    public static boolean networkConnected(Activity activity) {
        ConnectivityManager conManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void showToast(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
