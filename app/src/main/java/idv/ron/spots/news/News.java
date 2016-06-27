package idv.ron.spots.news;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class News {
    private int id;
    private String title;
    private String detail;
    private long date;

    public News(int id, String title, String detail, long date) {
        super();
        this.id = id;
        this.title = title;
        this.detail = detail;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getFormatedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return dateFormat.format(new Date(date));
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
