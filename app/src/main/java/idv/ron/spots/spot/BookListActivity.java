package idv.ron.spots.spot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import idv.ron.spots.R;
import idv.ron.spots.main.Category;
import idv.ron.spots.main.Common;

// Project Structure > add RecyclerView and CardView API
public class BookListActivity extends AppCompatActivity {
    private static final String TAG = "BookListActivity";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvSpots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_list_activity);
        askPermissions();

        swipeRefreshLayout =
                (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                showAllBooks();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        rvSpots = (RecyclerView) findViewById(R.id.rvNews);
        rvSpots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void showAllBooks() {
        if (Common.networkConnected(this)) {
            String url = Common.URL + "BookServlet";
            List<Book> books = null;
            try {
                books = new BookGetAllTask().execute(url).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (books == null || books.isEmpty()) {
                Common.showToast(BookListActivity.this, R.string.msg_NoSpotsFound);
            } else {
                rvSpots.setAdapter(new BooksRecyclerViewAdapter(BookListActivity.this, books));
            }
        } else {
            Common.showToast(this, R.string.msg_NoNetwork);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showAllBooks();
    }

    private class BooksRecyclerViewAdapter extends RecyclerView.Adapter<BooksRecyclerViewAdapter.ViewHolder> {
        private Context context;
        private LayoutInflater layoutInflater;
        private List<Book> books;

        public BooksRecyclerViewAdapter(Context context, List<Book> books) {
            this.context = context;
            layoutInflater = LayoutInflater.from(context);
            this.books = books;
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.book_recyclerview_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            final Book book = books.get(position);
            String url = Common.URL + "BookServlet";
            String ISBN = book.getISBN();
            int imageSize = 250;
            new BookGetImageTask(viewHolder.imageView).execute(url, ISBN, imageSize);
            viewHolder.tvName.setText(book.getName());
            viewHolder.tvPrice.setText(String.valueOf(book.getPrice()));
            viewHolder.tvAuthor.setText(book.getAuthor());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(BookListActivity.this,
                            BookDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("book", book);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(BookListActivity.this, view, Gravity.END);
                    popupMenu.inflate(R.menu.popup_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.insert:
                                    Intent insertIntent = new Intent(BookListActivity.this, BookInsertActivity.class);
                                    startActivity(insertIntent);
                                    break;
                                case R.id.update:
                                    Intent updateIntent = new Intent(BookListActivity.this,
                                            BookUpdateActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("book", book);
                                    updateIntent.putExtras(bundle);
                                    startActivity(updateIntent);
                                    break;
                                case R.id.delete:
                                    if (Common.networkConnected(BookListActivity.this)) {
                                        String url = Common.URL + "BookServlet";
                                        String action = "bookDelete";
                                        int count = 0;
                                        try {
                                            count = new BookUpdateTask().execute(url, action, book, null).get();
                                        } catch (Exception e) {
                                            Log.e(TAG, e.toString());
                                        }
                                        if (count == 0) {
                                            Common.showToast(BookListActivity.this, R.string.msg_DeleteFail);
                                        } else {
                                            books.remove(position);
                                            BookListActivity.BooksRecyclerViewAdapter.this.notifyDataSetChanged();
                                            Common.showToast(BookListActivity.this, R.string.msg_DeleteSuccess);
                                        }
                                    } else {
                                        Common.showToast(BookListActivity.this, R.string.msg_NoNetwork);
                                    }
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                    return true;
                }
            });
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View itemView;
            ImageView imageView;
            TextView tvName, tvPrice, tvAuthor;

            public ViewHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                imageView = (ImageView) itemView.findViewById(R.id.ivBook);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
                tvAuthor = (TextView) itemView.findViewById(R.id.tvAuthor);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        for (Category category : Common.CATEGORIES) {
            int id = category.getId();
            String title = category.getTitle();
            menu.add(0, id, id, title);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        for (Category category : Common.CATEGORIES) {
            if (item.getItemId() == category.getId()) {
                Intent intent = new Intent(this, category.getFirstActivity());
                startActivity(intent);
                return true;
            }
        }
        return false;
    }

    private static final int REQ_PERMISSIONS = 0;

    // New Permission see Appendix A
    private void askPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        Set<String> permissionsRequest = new HashSet<>();
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsRequest.add(permission);
            }
        }

        if (!permissionsRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsRequest.toArray(new String[permissionsRequest.size()]),
                    REQ_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSIONS:
                String text = "";
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        text += permissions[i] + "\n";
                    }
                }
                if (!text.isEmpty()) {
                    text += getString(R.string.text_NotGranted);
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
