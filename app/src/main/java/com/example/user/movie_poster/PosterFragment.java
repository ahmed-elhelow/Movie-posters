package com.example.user.movie_poster;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by user on 03/10/2016.
 */

public class PosterFragment extends Fragment {

    private final String LOG_TAG = PosterFragment.class.getSimpleName();
    ImageListAdapter mPosterAdapter;

    class Movie{
        public String id;
        public String posterLink;
    }

    public PosterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.posterfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateMovies();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//            String[] eatFoodyImages = {
//                    "http://i.imgur.com/rFLNqWI.jpg",
//                    "http://i.imgur.com/C9pBVt7.jpg",
//                    "http://i.imgur.com/rT5vXE1.jpg",
//                    "http://i.imgur.com/aIy5R2k.jpg",
//                    "http://i.imgur.com/MoJs9pT.jpg",
//                    "http://i.imgur.com/S963yEM.jpg",
//                    "http://i.imgur.com/COzBnru.jpg",
//                    "http://i.imgur.com/rLR2cyc.jpg",
//                    "http://i.imgur.com/SEPdUIx.jpg",
//                    "http://i.imgur.com/aC9OjaM.jpg",
//                    "http://i.imgur.com/76Jfv9b.jpg",
//                    "http://i.imgur.com/fUX7EIB.jpg",
//                    "http://i.imgur.com/syELajx.jpg",
//                    "http://i.imgur.com/Z3QjilA.jpg",
//            };

//            ArrayList<String> eatFoodyImages = new ArrayList<String>();
//            eatFoodyImages.add("http://i.imgur.com/rFLNqWI.jpg");
//            eatFoodyImages.add("http://i.imgur.com/C9pBVt7.jpg");
//            eatFoodyImages.add("http://i.imgur.com/rT5vXE1.jpg");

        mPosterAdapter =
                new ImageListAdapter(getActivity(), new ArrayList<Movie>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(mPosterAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String movieID = mPosterAdapter.getItem(position);
//                Toast.makeText(getActivity(), movieID, Toast.LENGTH_SHORT).show();
                Intent movieIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, movieID);
//                Log.v(LOG_TAG,Intent.EXTRA_TEXT);
                startActivity(movieIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        moviesTask.execute();
    }

    public class ImageListAdapter extends ArrayAdapter {
        private Context context;
        private LayoutInflater inflater;

        ArrayList<Movie> movies_Params;
//            private String[] movies_Params;

        public ImageListAdapter(Context context, ArrayList<Movie> movies_Params) {
            super(context, R.layout.grid_item_poster, movies_Params);

            this.context = context;
            this.movies_Params = movies_Params;

            inflater = LayoutInflater.from(context);
        }

        public void add_item(Movie movieParams) {
            movies_Params.add(movieParams);
            notifyDataSetChanged();
        }

        @Override
        public String getItem(int position)
        {
            return movies_Params.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.grid_item_poster, parent, false);
            }

            ImageView imageView = (ImageView) convertView
                    .findViewById(R.id.grid_item_poster_imageview);

            Picasso.with(context)
                    .load(movies_Params.get(position).posterLink)
                    .error(R.drawable.car)
                    .into(imageView);

            return imageView;
        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private Movie[] getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_POSTER = "poster_path";
            final String OWM_ID = "id";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS);

            // missing up with the counter i
            // missing up with the counter i
            // missing up with the counter i
            Movie[] resultMovieParams = new Movie[moviesArray.length()];
            for (int i = 0; i < moviesArray.length(); i++) {
                String poster_path;
                String id;

                // Get the JSON object representing the day
                JSONObject oneMovie = moviesArray.getJSONObject(i);

                poster_path = oneMovie.getString(OWM_POSTER);
                id = oneMovie.getString(OWM_ID);

                Movie movieParams = new Movie();
                movieParams.posterLink = "http://image.tmdb.org/t/p/w185" + poster_path;
                movieParams.id = id;
                resultMovieParams[i] = movieParams;
            }

//            for (Movie s : resultMovieParams) {
//                Log.v(LOG_TAG, "Posters entry: " + s.posterLink);
//            }
            return resultMovieParams;
        }

        @Override
        protected Movie[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            String sort = "popularity.desc";

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=4e72884d73ef8321a2c976567a88ce7e
                //http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=0cab1aab8bf61d1e97cd0eac96a59d6b

                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
                final String SORT_PARAM = "sort_by";
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sort)
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_MOVIE_DB_API_KEY)
                        .build();

                String myUrl = builtUri.toString();
                URL url = new URL(myUrl);

//                Log.v(LOG_TAG, "myUrl: " + myUrl);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();

//                Log.v(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error getting data ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Movie[] strings) {
            super.onPostExecute(strings);
//            for (String s : strings) {
//                Log.v(LOG_TAG, "Forecast entry: " + s);
//            }

            if (strings != null) {
                mPosterAdapter.clear();
                for (Movie movieParams : strings) {
                    mPosterAdapter.add_item(movieParams);
                }
                mPosterAdapter.notifyDataSetChanged();
            }
        }
    }


}
