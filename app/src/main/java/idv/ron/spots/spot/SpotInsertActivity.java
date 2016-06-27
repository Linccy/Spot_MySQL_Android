package idv.ron.spots.spot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import idv.ron.spots.R;
import idv.ron.spots.main.Common;

public class SpotInsertActivity extends AppCompatActivity {
    private final static String TAG = "SpotInsertActivity";
    private EditText etName;
    private EditText etPhone;
    private EditText etAddress;
    private ImageView ivSpot;
    private byte[] image;
    private File file;
    private static final int REQUEST_TAKE_PICTURE = 0;
    private static final int REQUEST_PICK_IMAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_insert_activity);
        findViews();
    }

    private void findViews() {
        ivSpot = (ImageView) findViewById(R.id.ivSpot);
        etName = (EditText) findViewById(R.id.etName);
        etPhone = (EditText) findViewById(R.id.etPhoneNo);
        etAddress = (EditText) findViewById(R.id.etAddress);
    }

    public void onTakePictureClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        if (isIntentAvailable(this, intent)) {
            startActivityForResult(intent, REQUEST_TAKE_PICTURE);
        } else {
            Toast.makeText(this, R.string.msg_NoCameraAppsFound,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public void onPickPictureClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PICTURE:
                    Bitmap picture = BitmapFactory.decodeFile(file.getPath());
                    ivSpot.setImageBitmap(picture);
                    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
                    picture.compress(Bitmap.CompressFormat.JPEG, 100, out1);
                    image = out1.toByteArray();
                    break;
                case REQUEST_PICK_IMAGE:
                    Uri uri = intent.getData();
                    String[] columns = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(uri, columns,
                            null, null, null);
                    if (cursor.moveToFirst()) {
                        String imagePath = cursor.getString(0);
                        cursor.close();
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        ivSpot.setImageBitmap(bitmap);
                        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out2);
                        image = out2.toByteArray();
                    }
                    break;
            }
        }
    }

    public void onFinishInsertClick(View view) {
        String name = etName.getText().toString().trim();
        if (name.length() <= 0) {
            Toast.makeText(this, R.string.msg_NameIsInvalid,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String phoneNo = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        List<Address> addressList;
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            addressList = new Geocoder(this).getFromLocationName(address, 1);
            latitude = addressList.get(0).getLatitude();
            longitude = addressList.get(0).getLongitude();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        if (image == null) {
            Common.showToast(this, R.string.msg_NoImage);
            return;
        }

        if (Common.networkConnected(this)) {
            String url = Common.URL + "SpotServlet";
            Spot spot = new Spot(0, name, phoneNo, address, latitude, longitude);
            String imageBase64 = Base64.encodeToString(image, Base64.DEFAULT);
            String action = "spotInsert";
            int count = 0;
            try {
                count = new SpotUpdateTask().execute(url, action, spot, imageBase64).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (count == 0) {
                Common.showToast(SpotInsertActivity.this, R.string.msg_InsertFail);
            } else {
                Common.showToast(SpotInsertActivity.this, R.string.msg_InsertSuccess);
            }
        } else {
            Common.showToast(this, R.string.msg_NoNetwork);
        }
        finish();
    }

    public void onCancelClick(View view) {
        finish();
    }
}