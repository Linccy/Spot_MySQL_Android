package idv.ron.spots.spot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.List;

import idv.ron.spots.R;
import idv.ron.spots.main.Common;

public class BookInsertActivity extends AppCompatActivity {
    private final static String TAG = "BookInsertActivity";
    private EditText etISBN;
    private EditText etName;
    private EditText etPirce;
    private EditText etAuthor;
    private ImageView ivBook;
    private byte[] image;
    private File file;
    private static final int REQUEST_TAKE_PICTURE = 0;
    private static final int REQUEST_PICK_IMAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_insert_activity);
        findViews();
    }

    private void findViews() {
        etISBN = (EditText) findViewById(R.id.etISBN);
        ivBook = (ImageView) findViewById(R.id.ivBook);
        etName = (EditText) findViewById(R.id.etName);
        etPirce = (EditText) findViewById(R.id.etPrice);
        etAuthor = (EditText) findViewById(R.id.etAuthor);
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
                    ivBook.setImageBitmap(picture);
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
                        ivBook.setImageBitmap(bitmap);
                        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out2);
                        image = out2.toByteArray();
                    }
                    break;
            }
        }
    }

    public void onFinishInsertClick(View view) {
        String ISBN = etISBN.getText().toString().trim();//trim()去除空格
        String name = etName.getText().toString().trim();
        if (name.length() <= 0) {
            Toast.makeText(this, R.string.msg_NameIsInvalid,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        double price = Double.valueOf(etPirce.getText().toString().trim());
        String author = etAuthor.getText().toString().trim();
//        List<Address> addressList;
//        double latitude = 0.0;
//        double longitude = 0.0;
//        try {
//            addressList = new Geocoder(this).getFromLocationName(author, 1);
//            latitude = addressList.get(0).getLatitude();
//            longitude = addressList.get(0).getLongitude();
//        } catch (IOException e) {
//            Log.e(TAG, e.toString());
//        }
        if (image == null) {
            Common.showToast(this, R.string.msg_NoImage);
            return;
        }

        if (Common.networkConnected(this)) {
            String url = Common.URL + "BookServlet";
            Book book = new Book(ISBN, name, price, author);
            String imageBase64 = Base64.encodeToString(image, Base64.DEFAULT);
            String action = "bookInsert";
            int count = 0;
            try {
                count = new BookUpdateTask().execute(url, action, book, imageBase64).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (count == 0) {
                Common.showToast(BookInsertActivity.this, R.string.msg_InsertFail);
            } else {
                Common.showToast(BookInsertActivity.this, R.string.msg_InsertSuccess);
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