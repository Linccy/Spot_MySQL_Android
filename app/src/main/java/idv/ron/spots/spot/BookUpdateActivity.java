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
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import idv.ron.spots.R;
import idv.ron.spots.main.Common;

public class BookUpdateActivity extends AppCompatActivity {
    private final static String TAG = "BookUpdateActivity";
    private ImageView ivBook;
    private TextView tvISBN;
    private EditText etName;
    private EditText etPrice;
    private EditText etAuthor;
    private byte[] image;
    private File file;
    private static final int REQUEST_TAKE_PICTURE = 0;
    private static final int REQUEST_PICK_IMAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_update_activity);
        findViews();
    }

    private void findViews() {
        ivBook = (ImageView) findViewById(R.id.ivBook);
        tvISBN = (TextView) findViewById(R.id.tvISBN);
        etName = (EditText) findViewById(R.id.etName);
        etPrice = (EditText) findViewById(R.id.etPrice);
        etAuthor = (EditText) findViewById(R.id.etAuthor);

        Book book = (Book) getIntent().getExtras().getSerializable("book");
        if (book == null) {
            Common.showToast(this, "no fount");
            finish();
            return;
        }
        String url = Common.URL + "BookServlet";
        String ISBN = book.getISBN();
        int imageSize = 400;
        Bitmap bitmap = null;
        try {
            // passing null and calling get() means not to run FindImageByIdTask.onPostExecute()
            bitmap = new BookGetImageTask(null).execute(url, ISBN, imageSize).get();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (bitmap != null) {
            ivBook.setImageBitmap(bitmap);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            image = out.toByteArray();
        } else {
            ivBook.setImageResource(R.drawable.default_image);
        }
        ivBook.setImageBitmap(bitmap);
        tvISBN.setText(String.valueOf(ISBN));
        etName.setText(book.getName());
        etPrice.setText(String.valueOf(book.getPrice()));
        etAuthor.setText(book.getAuthor());
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

    public boolean isIntentAvailable(Context context, Intent intent) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                    Uri uri = data.getData();
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

    public void onFinishUpdateClick(View view) {
        String ISBN = tvISBN.getText().toString();
        String name = etName.getText().toString();
        if (name.length() <= 0) {
            Toast.makeText(this, "Book name not fount",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        double price = Double.valueOf(etPrice.getText().toString());
        String author = etAuthor.getText().toString();
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
            Toast.makeText(this, R.string.msg_NoImage,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (Common.networkConnected(this)) {
            String url = Common.URL + "BookServlet";
            Book book = new Book(ISBN, name, price, author);
            String imageBase64 = Base64.encodeToString(image, Base64.DEFAULT);
            String action = "bookUpdate";
            int count = 0;
            try {
                count = new BookUpdateTask().execute(url, action, book, imageBase64).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (count == 0) {
                Common.showToast(BookUpdateActivity.this, R.string.msg_UpdateFail);
            } else {
                Common.showToast(BookUpdateActivity.this, R.string.msg_UpdateSuccess);
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
