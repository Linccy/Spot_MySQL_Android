package idv.ron.spots.news;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import idv.ron.spots.R;
import idv.ron.spots.main.Category;
import idv.ron.spots.main.Common;

import static idv.ron.spots.main.Common.CATEGORIES;

public class NewsActivity extends AppCompatActivity {
    private final static String TAG = "NewsActivity";
    private RecyclerView rvNews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);
        rvNews = (RecyclerView) findViewById(R.id.rvNews);
        rvNews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Common.networkConnected(this)) {
            String url = Common.URL + "NewsServlet";
            List<News> newsList = null;

            ProgressDialog progressDialog = new ProgressDialog(NewsActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
            try {
                newsList = new NewsGetAllTask().execute(url).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (newsList == null || newsList.isEmpty()) {
                Common.showToast(NewsActivity.this, R.string.msg_NoNewsFound);
            } else {
                rvNews.setAdapter(new NewsRecyclerViewAdapter(NewsActivity.this, newsList));
            }
            progressDialog.cancel();

        } else {
            Common.showToast(this, R.string.msg_NoNetwork);
        }
    }

    private class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder> {
        private LayoutInflater layoutInflater;
        private List<News> newsList;
        private boolean[] newsExpanded;

        public NewsRecyclerViewAdapter(Context context, List<News> newsList) {
            layoutInflater = LayoutInflater.from(context);
            this.newsList = newsList;
            newsExpanded = new boolean[newsList.size()];
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNewsTitle, tvNewsDetail;

            public ViewHolder(View itemView) {
                super(itemView);
                tvNewsTitle = (TextView) itemView.findViewById(R.id.tvNewsTitle);
                tvNewsDetail = (TextView) itemView.findViewById(R.id.tvNewsDetail);
            }
        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.news_recyclerview_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            News news = newsList.get(position);
            String title = news.getFormatedDate() + " " + news.getTitle();
            viewHolder.tvNewsTitle.setText(title);
            viewHolder.tvNewsDetail.setText(news.getDetail());
            viewHolder.tvNewsDetail.setVisibility(
                    newsExpanded[position] ? View.VISIBLE : View.GONE);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    expand(position);
                }
            });
        }

        private void expand(int position) {
            // 被點擊的資料列才會彈出內容，其他資料列的內容會自動縮起來
            // for (int i=0; i<newsExpanded.length; i++) {
            // newsExpanded[i] = false;
            // }
            // newsExpanded[position] = true;

            newsExpanded[position] = !newsExpanded[position];
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        for (Category category : CATEGORIES) {
            int id = category.getId();
            String title = category.getTitle();
            menu.add(0, id, id, title);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        for (Category category : CATEGORIES) {
            if (item.getItemId() == category.getId()) {
                Intent intent = new Intent(this, category.getFirstActivity());
                startActivity(intent);
                return true;
            }
        }
        return false;
    }
}