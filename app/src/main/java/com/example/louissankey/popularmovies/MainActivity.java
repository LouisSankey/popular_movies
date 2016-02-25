package com.example.louissankey.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.louissankey.popularmovies.model.Movie;
import com.example.louissankey.popularmovies.model.MoviePosterAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    @Bind(R.id.gridview)
    GridView mGridView;
    @Bind(R.id.main_activity_header)
    TextView mMainActivityHeaderTextView;

    private static final String TAG = MainActivity.class.getSimpleName();

    //I didn't know how to refactor these out or if they should be refactored
    public static final String MOVIE_TITLE = "MOVIE_TITLE";
    public static final String MOVIE_POSTER_URL = "MOVIE_POSTER_URL";
    public static final String MOVIE_OVERVIEW = "MOVIE_OVERVIEW";
    public static final String MOVIE_VOTE_AVERAGE = "MOVIE_VOTE_AVERAGE";

    private String byPopularityUrl = "&sort_by=popularity.desc";
    //also didn't know if sort_by vote_average was best becasue it returned movies that may only have 1 rating of 10. Not very informative.
    private String byHighestRatedUrl = "&sort_by=vote_average.desc";

    private List<Movie> movieList;
    private String jsonData;
    private MoviePosterAdapter moviePosterAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        getMovieJson(byPopularityUrl);
        movieList = new ArrayList<>();

        //I was reminded of how to set up an onItemClickListener here:
        //http://stackoverflow.com/questions/22473350/open-a-new-activity-for-each-item-clicked-from-gridview
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movieList.get(position);

                Bundle bundle = new Bundle();
                bundle.putString(MOVIE_TITLE, movie.getTitle());
                bundle.putString(MOVIE_POSTER_URL, movie.getPosterUrl());
                bundle.putString(MOVIE_OVERVIEW, movie.getOverview());
                bundle.putDouble(MOVIE_VOTE_AVERAGE, movie.getVoteAverage());

                Intent intent = new Intent(MainActivity.this, MovieDetails.class);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

    }

    public void getMovieJson(final String sortUrl) {

        String apiKey = BuildConfig.POPULAR_MOVIES_API_KEY;

        Request request = new Request.Builder()
                .url(getString(R.string.themovedb_base_url) + apiKey + sortUrl)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                movieList.clear();
                jsonData = response.body().string();
                Log.v(TAG, jsonData);
                JSONObject movieJson;

                try {
                    //I didn't understand if I should try to get more than only the first page of json
                    movieJson = new JSONObject(jsonData);
                    JSONArray results = movieJson.getJSONArray(getString(R.string.json_results_key));
                    for (int i = 0; i < results.length(); i++) {
                        Movie movie = new Movie();
                        JSONObject movieObject = results.getJSONObject(i);
                        movie.setPosterUrl(movieObject.getString(getString(R.string.json_poster_path_key)));
                        movie.setTitle(movieObject.getString(getString(R.string.json_original_title_key)));
                        movie.setOverview(movieObject.getString(getString(R.string.json_overview_key)));
                        movie.setReleaseDate(movieObject.getString(getString(R.string.json_release_date_key)));
                        movie.setVoteAverage(movieObject.getDouble(getString(R.string.json_vote_average_key)));
                        movieList.add(movie);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                moviePosterAdapter = new MoviePosterAdapter(MainActivity.this, movieList);

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mGridView.setAdapter(moviePosterAdapter);

                        if (sortUrl.equals(byPopularityUrl)) {
                            mMainActivityHeaderTextView.setText(R.string.main_activity_header_most_popular);
                        } else if (sortUrl.equals(byHighestRatedUrl)) {
                            mMainActivityHeaderTextView.setText(R.string.main_activity_header_highest_rated);
                        }
                    }
                });
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_popularity) {
            getMovieJson(byPopularityUrl);
        } else if (id == R.id.action_ratings) {
            getMovieJson(byHighestRatedUrl);

        }

        return super.onOptionsItemSelected(item);
    }
}