package com.example.user.movie_poster;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends YouTubeBaseActivity {
    private final String LOG_TAG = DetailActivity.class.getSimpleName();

    private String movieID;
    private String trailerLinkGlobal;
//    Button b;
    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer.OnInitializedListener onInitializedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = this.getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            movieID = intent.getStringExtra(Intent.EXTRA_TEXT);
//            Toast.makeText(this, movieID, Toast.LENGTH_SHORT).show();
        }

        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                YouTubePlayer youTubePlayer, boolean b) {
//                youTubePlayer.loadVideo("uVdV-lxRPFo");
                youTubePlayer.loadVideo(trailerLinkGlobal);
            }
            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                YouTubeInitializationResult youTubeInitializationResult) {
            }
        };

        youTubePlayerView.initialize("AIzaSyD2iDip7zqF1ls9tiu_OSDXEFPnhmUu3EE",
                onInitializedListener);

//        b = (Button) findViewById(R.id.button);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                youTubePlayerView.initialize("AIzaSyD2iDip7zqF1ls9tiu_OSDXEFPnhmUu3EE",
//                        onInitializedListener);
//            }
//        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchMoviesTask trailerTask = new FetchMoviesTask();
        trailerTask.execute(movieID);
    }

    public void ReturnThreadResult(String trailerLink) {
//        Log.v(LOG_TAG, "Trailer link: " + trailerLink);
        trailerLinkGlobal = trailerLink;
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String> {
        //        DetailFragment parent;
        DetailActivity parent;
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private String getTrailerLinkFromJson(String trailerJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_KEY = "key";

            JSONObject movieJson = new JSONObject(trailerJsonStr);
            JSONArray trailersArray = movieJson.getJSONArray(OWM_RESULTS);

            JSONObject officialTrailer = trailersArray.getJSONObject(0);
            String trailerLink = officialTrailer.getString(OWM_KEY);
//            "https://www.youtube.com/watch?v="

//                Log.v(LOG_TAG, "Trailer link: " + trailerLink);
            return trailerLink;
        }

        @Override
        protected String doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailerJsonStr = null;


            try {
                //http://api.themoviedb.org/3/movie/333484/videos?api_key=0cab1aab8bf61d1e97cd0eac96a59d6b
                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String VIDEO_OPTION = "videos";
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendPath(movieID)
                        .appendPath(VIDEO_OPTION)
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_MOVIE_DB_API_KEY)
                        .build();

                String myUrl = builtUri.toString();
                URL url = new URL(myUrl);

//                    Log.v(LOG_TAG, "myUrl: " + myUrl);

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
                trailerJsonStr = buffer.toString();

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
                return getTrailerLinkFromJson(trailerJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String trailerLink) {
            super.onPostExecute(trailerLink);
            ReturnThreadResult(trailerLink);
        }
    }

//        private VideoView myVideoView;
//        private MediaController mediaControls;
//        private ProgressDialog progressDialog;
//        private int position = 0;

//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
//
//            Intent intent = getActivity().getIntent();
//            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
//                movieID = intent.getStringExtra(Intent.EXTRA_TEXT);
////                textView = (TextView) rootView.findViewById(R.id.detail_text);
////                textView.setText(movieID);
//            }
//
//            myVideoView = (VideoView) rootView.findViewById(R.id.video_view);
//            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setTitle("Android Video View Example");
//            progressDialog.setMessage("Loading...");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//
//            try {
//                myVideoView.setVideoURI(Uri.parse("android.resource://" +
//                        BuildConfig.APPLICATION_ID + "/" + R.raw.god_of_war));
//            }
//            catch (Exception e) {
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//
//            myVideoView.requestFocus();
//            myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                public void onPrepared(MediaPlayer mediaPlayer) {
//                    progressDialog.dismiss();
//                    myVideoView.seekTo(position);
//                    if (position == 0) {
//                        myVideoView.start();
//                    } else {
//                        myVideoView.pause();
//                    }
//                }
//            });
//            return rootView;
//        }

}
