package idv.ron.spots.spot;

import java.io.Serializable;

/**
 * @author lcc 957109587@qq.com
 * @version 2016��5��3�� ����8:06:45
 * @Description
 */
public class Book implements Serializable {
	String ISBN;
	String name;
	double price;
	String author;

	public Book(String ISBN, String name, double price, String author) {
		this.ISBN = ISBN;
		this.name = name;
		this.price = price;
		this.author = author;
	}

	public String getISBN() {
		return ISBN;
	}

	public void setISBN(String iSBN) {
		this.ISBN = iSBN;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

}
