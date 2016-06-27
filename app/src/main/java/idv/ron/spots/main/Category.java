package idv.ron.spots.main;

import android.app.Activity;

public class Category {
	private int id;
	private String title;
	private int image;
	private Class<? extends Activity> firstActivity;

	public Category(int id, String title, int image,
			Class<? extends Activity> firstActivity) {
		super();
		this.id = id;
		this.title = title;
		this.image = image;
		this.firstActivity = firstActivity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}

	public Class<? extends Activity> getFirstActivity() {
		return firstActivity;
	}

	public void setFirstActivity(Class<? extends Activity> firstActivity) {
		this.firstActivity = firstActivity;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}