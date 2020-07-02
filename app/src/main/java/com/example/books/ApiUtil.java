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

import static android.provider.MediaStore.Video.VideoColumns.DESCRIPTION;

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
    public static final String API_KEY = "AIzaSyDKKa-UksyGc6igxTIz50q4iXcrkALfzNY";
    public static final String TITLE = "intitle:";
    public static final String AUTHOR = "inauthor:";
    public static final String PUBLISHER = "inpublisher:";
    public static final String ISBN = "isbn:";

    public static URL buildUrl(String title){

        URL url = null;
        /*try convert string to url*/
        Uri uri = Uri.parse(BASE_API_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAMETER_KEY, title)
                .appendQueryParameter(KEY,API_KEY)
                .build();
        try {
            url = new URL(uri.toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return url;

    }/*connect to api */
    public  static URL buildUrl (String title, String author, String publisher, String isbn ){
        URL url = null;
        StringBuilder sb = new StringBuilder();
        if(!title.isEmpty()) sb.append(TITLE + title + "+");
        if(!author.isEmpty()) sb.append(AUTHOR + author + "+");
        if(!publisher.isEmpty()) sb.append(PUBLISHER + publisher + "+");
        if(!isbn.isEmpty()) sb.append(ISBN + isbn + "+");
        if(sb.length() > 0)
            sb.setLength(sb.length() - 1);

        Uri uri = Uri.parse(BASE_API_URL)
                .buildUpon()
                .appendQueryParameter(QUERY_PARAMETER_KEY, sb.toString())
                .appendQueryParameter(KEY, API_KEY)
                .build();
        try {
            url = new URL(uri.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }
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
        final  String DESCRIPTION= "description";
        final  String  IMAGE_LINKS = "imageLinks";
        final String  THUMBNAIL = "thumbnail";
        ArrayList<Book> books = new ArrayList<Book>();
        try {
            // first convert our String to a JSONObject so that we can parse it
            JSONObject jsonBooks = new JSONObject(json);
            // we get the array that contain all the books by passing the name of the Array
            // in our case is "items"
            JSONArray arrayBooks = jsonBooks.getJSONArray(ITEMS);
            // get the number of books in our  JSONArray
            int numberOfBooks = arrayBooks.length();
            // we will now loop our arrayBooks to create each JSONObject alone and add it
            // to our Books array
            for (int i=0; i<numberOfBooks; i++){
                JSONObject bookJSON = arrayBooks.getJSONObject(i);
                // get the VolumeInfo from our JSON Object where we have the ttite, authors etc
                JSONObject volumeInfoJSON = bookJSON.getJSONObject(VOLUMEINFO);
                JSONObject imageLinksJSON= null;
                if (volumeInfoJSON.has(IMAGE_LINKS))
                    imageLinksJSON = volumeInfoJSON.getJSONObject(IMAGE_LINKS);
                //if we want any data now we call it by calling the appropriate method with the
                // appropriate key , and we must be sure to iterate if our data is an arrau
                int authorNum ;
                try {
                    authorNum = volumeInfoJSON.getJSONArray(AUTHOR).length();
                } catch (Exception e){
                    authorNum= 0;
                }
                String[] authors = new String[authorNum];
                for (int j=0; j<authorNum;j++){
                    authors[j]= volumeInfoJSON.getJSONArray(AUTHOR).get(j).toString();

                }
                Book book = new Book(
                        bookJSON.getString(ID),
                        volumeInfoJSON.getString(TITLE),
                        (volumeInfoJSON.isNull(SUBTITLE) ? "" : volumeInfoJSON.getString(SUBTITLE)),
                        authors,
                        (volumeInfoJSON.isNull(PUBLISHER) ? "" : volumeInfoJSON.getString(PUBLISHER)),
                        (volumeInfoJSON.isNull(PUBLISHED_DATE) ? "" : volumeInfoJSON.getString(PUBLISHED_DATE)),
                        (volumeInfoJSON.isNull(DESCRIPTION) ? "" : volumeInfoJSON.getString(DESCRIPTION)),
                        (imageLinksJSON==null ? "" : imageLinksJSON.getString(THUMBNAIL))
                );
                books.add(book);

            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        return  books;
    }
}
