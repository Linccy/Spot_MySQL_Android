package idv.ron.spots.main;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import static idv.ron.spots.main.Common.*;

import idv.ron.spots.R;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        GridView gvCategories = (GridView) findViewById(R.id.gvCategories);
        gvCategories.setAdapter(new MyGridViewAdapter(this));
        gvCategories.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Class<? extends Activity> activityClass = CATEGORIES[position]
                        .getFirstActivity();
                Intent intent = new Intent(MainActivity.this, activityClass);
                startActivity(intent);
            }
        });
    }

    private class MyGridViewAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;

        public MyGridViewAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return CATEGORIES.length;
        }

        @Override
        public Object getItem(int position) {
            return CATEGORIES[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(
                        R.layout.main_gridview_item, parent, false);
            }
            Category category = CATEGORIES[position];
            ImageView ivCategory = (ImageView) convertView
                    .findViewById(R.id.ivCategory);
            ivCategory.setImageResource(category.getImage());
            TextView tvTitle = (TextView) convertView
                    .findViewById(R.id.tvTitle);
            tvTitle.setText(category.getTitle());
            return convertView;
        }
    }

}
