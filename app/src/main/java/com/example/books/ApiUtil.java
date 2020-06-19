package com.example.books;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
/*class to be able to connect to query books */
public class ApiUtil {
    /*will contain static methods and will never beein intastiatied and private is added to remove the constructor */
    private ApiUtil(){

        }/*url for connecting to he books */
    /*constant for base url */
    public static final String BASE_API_URL="https://www.googleapis.com/books/v1/volumes";
    /*function to help build url  */
    public static final String QUERY_PARAMETER_KEY = "q";
    public static final String KEY = "key";
    public static final String APi_KEY = "AIzaSyDKKa-UksyGc6igxTIz50q4iXcrkALfzNY";

    public static URL buildUrl(String title){

        URL url = null;
        /*try convert string to url*/
        Uri uri = Uri.parse(BASE_API_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAMETER_KEY, title)
                .appendQueryParameter(KEY,APi_KEY)
                .build();
        try {
            url = new URL(uri.toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return url;

    }/*connect to api */
    public static  String getJson (URL url) throws IOException {
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        try {
            InputStream stream =connection.getInputStream();
            Scanner scanner= new  Scanner(stream);
            /*a is a pattern saying u want to read everything*/
            scanner.useDelimiter("\\A");
            boolean hasData = scanner.hasNext();
            if (hasData) {
                return scanner.next();
            } else {
                return null;
            }
        }
        catch (Exception e){
            Log.d("Error", e.toString());
            return null;
        }
        finally {
            connection.disconnect();
        }

    }
    public static ArrayList<Book> getBooksFromJson(String json){
        final String   ID = "id";
        final  String TITLE = "title";
        final  String SUBTITLE ="subtitle";
        final  String AUTHOR = "authors";
        final String PUBLISHER ="publisher";
        final String PUBLISHED_DATE ="publishedDate";
        final String ITEMS = "items";
        final String VOLUMEINFO = "volumeInfo";
        ArrayList<Book> books = new ArrayList<Book>();
        try {
            JSONObject jsonBooks = new JSONObject(json);
            JSONArray arrayBooks = jsonBooks.getJSONArray(ITEMS);
            int numberOfBooks = arrayBooks.length();
            for (int i=0; i<numberOfBooks; i++){
                JSONObject bookJSON = arrayBooks.getJSONObject(i);
                JSONObject volumeInfoJSON =
                        bookJSON.getJSONObject(VOLUMEINFO);
                int authorNum = volumeInfoJSON.getJSONArray(AUTHOR).length();
                String[] authors = new String[authorNum];
                for (int j=0; j<authorNum;j++){
                    authors[j]= volumeInfoJSON.getJSONArray(AUTHOR).get(j).toString();

                }
                Book book = new Book(
                        bookJSON.getString(ID),
                        volumeInfoJSON.getString(TITLE),
                        (volumeInfoJSON.isNull(SUBTITLE)? "":volumeInfoJSON.getString(SUBTITLE)),authors,
                        volumeInfoJSON.getString(PUBLISHER),
                        volumeInfoJSON.getString(PUBLISHED_DATE)
                );books.add(book);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        return  books;
    }
}
