package com.example.books;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
/**
 * this class will be our Main activity where we will show our list of books that we will call
 * with the help of google Book API
 */

/**
 * we implements OnQueryTextListener so that we can use our searchView widget and be able
 * to do search from it
 */
public class BookListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
private ProgressBar mLoadingProgress;
    /**
     * our text view who will show up if there is an error while loading
     * data from the Google Book APU
     */
    private  TextView mTvError;
    /**
     * now we will create our recyclerView and bind it to the recyclerView we just created in the
     * layout, and assign it on the onCreate method
     */

 RecyclerView rvBooks;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    /**
     * now to use out ApiUtils class we need to first build our URL
     * and we want to look for every books who has the word "Cooking" on it
     * after getting the URL and the JSON text we want to show our result in our TextView
     */
      URL bookUrl;
      String jsonResult;
    //        mTvResutlt = (TextView) findViewById(R.id.tvResponse);
        mLoadingProgress = (ProgressBar) findViewById(R.id.progressBar);
        mTvError =(TextView)findViewById(R.id.tvError);
        rvBooks =(RecyclerView)findViewById(R.id.rv_books);
    /**
     * in case we get called by an intent from advanced search
     */
        Intent intent = getIntent();
        String query = intent.getStringExtra("query");
    LinearLayoutManager booksLayoutManager = new LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,
            false
    );
    rvBooks.setLayoutManager(booksLayoutManager);

        try {
        if (query == null  || query.isEmpty()) {
        bookUrl = ApiUtil.buildUrl("cooking");
        }
        else {
        bookUrl = new URL(query);
        }
        new BooksQueryTask().execute(bookUrl);

        }
        catch (Exception e) {
        Log.d("error", e.getMessage());
        }

    /**
     * we override this method to show our menu in this activity
     *
     * @param menu
     * @return
     */


        }
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    // we use this method to add our menu to our activity
        getMenuInflater().inflate(R.menu.book_list_menu, menu);
final MenuItem searchItem=menu.findItem(R.id.action_search);
    // because the MenuItemCompact.getActionView became deprecated we will use the getActionView
    // directly from the searchItem
    //final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
    // we will add the ist of query we get from our shared preference
        ArrayList<String> recentList = SpUtil.getQueryList(getApplicationContext());
        int itemNum = recentList.size();
        MenuItem recentMenu;
        for (int i = 0; i<itemNum; i++) {
        recentMenu = menu.add(Menu.NONE, i, Menu.NONE, recentList.get(i));
        }
        return true;
        }

@Override
public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.action_advanced_search:
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
        return true;
default:
        int position = item.getItemId() + 1 ;
        String preferenceName = SpUtil.QUERY + String.valueOf(position);
        String query = SpUtil.getPreferenceString(getApplicationContext(), preferenceName);
        String[] prefParams = query.split("\\,");
        String[] queryParams = new String[4];

        for (int i=0; i<prefParams.length;i++) {
        queryParams[i] = prefParams[i];
        }


        URL bookUrl= ApiUtil.buildUrl(
        (queryParams[0] == null)?"" : queryParams[0],
        (queryParams[1] == null)?"" : queryParams[1],
        (queryParams[2] == null)?"" : queryParams[2],
        (queryParams[3] == null)?"" : queryParams[3]
        );
        new BooksQueryTask().execute(bookUrl);
        return super.onOptionsItemSelected(item);
        }
        }

@Override
public boolean onQueryTextSubmit(String query) {
        try {
        URL bookUrl = ApiUtil.buildUrl(query);
        new BooksQueryTask().execute(bookUrl);
        }
        catch (Exception e) {
        Log.d("error", e.getMessage());
        }


        return false;
        }
@Override
public boolean onQueryTextChange(String newText) {
        return false;
        }

public class BooksQueryTask extends AsyncTask<URL, Void, String> {

    @Override
    protected String doInBackground(URL... urls) {
        URL searchURL = urls[0];
        String result = null;
        try {
            result = ApiUtil.getJson(searchURL);
        }
        catch (IOException e) {
            Log.e("Error", e.getMessage());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {

        TextView tvError = (TextView) findViewById(R.id.tvError);
        mLoadingProgress.setVisibility(View.INVISIBLE);
        if (result == null) {
            rvBooks.setVisibility(View.INVISIBLE);
            tvError.setVisibility(View.VISIBLE);
        }
        else {
            rvBooks.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.INVISIBLE);
        }
        ArrayList<Book> books = ApiUtil.getBooksFromJson(result);
        String resultString = "";

        BooksAdapter adapter = new BooksAdapter(books);
        rvBooks.setAdapter(adapter);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mLoadingProgress.setVisibility(View.VISIBLE);
    }
}
}