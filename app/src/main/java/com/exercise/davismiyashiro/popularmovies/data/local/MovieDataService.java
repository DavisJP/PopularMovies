package com.exercise.davismiyashiro.popularmovies.data.local;

import android.app.IntentService;
import androidx.lifecycle.LiveData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.Nullable;

import com.exercise.davismiyashiro.popularmovies.data.MovieDetails;

import java.util.List;

import timber.log.Timber;

import static com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbContract.MoviesEntry;

/**
 * Created by Davis Miyashiro on 07/12/2017.
 */

public class MovieDataService extends IntentService {

    private static final String TAG = MovieDataService.class.getSimpleName();

    public static final String ACTION_INSERT = TAG + ".INSERT";
    public static final String ACTION_DELETE = TAG + ".DELETE";

    public static final String EXTRA_VALUES = TAG + ".ContentValues";

    public MovieDataService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ACTION_INSERT.equals(intent.getAction())) {
            ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
            insertMovieDb (values);
        } else if (ACTION_DELETE.equals(intent.getAction())) {
            deleteMovieDb(intent.getData());
        }
    }

    private void deleteMovieDb(Uri uri) {
        int rows = getContentResolver().delete(uri, null, null);

        if (rows > 0) {
            Timber.d(TAG, "Delete Movie ok");
            Intent deleteOk = new Intent(ACTION_DELETE);
            sendBroadcast(deleteOk);
        } else {
            Timber.e(TAG, "Error deleting Movie");
        }
    }

    private void insertMovieDb(ContentValues values) {
        if (getContentResolver().insert(MoviesEntry.CONTENT_URI, values) != null) {
            Timber.d(TAG, "Insert Movie ok");
            Intent insertOk = new Intent(ACTION_INSERT);
            sendBroadcast(insertOk);
        } else {
            Timber.e(TAG, "Error inserting Movie");
        }
    }

    public static void insertNewMovie (Context context, ContentValues values) {
        Intent intent = new Intent(context, MovieDataService.class);
        intent.setAction(ACTION_INSERT);
        intent.putExtra(EXTRA_VALUES, values);
        context.startService(intent);
    }

    public static void deleteMovie (Context context, Uri uri) {
        Intent intent = new Intent(context, MovieDataService.class);
        intent.setAction(ACTION_DELETE);
        intent.setData(uri);
        context.startService(intent);
    }

    public static class AsyncTaskQueryMovie extends AsyncTask<Integer, Void, LiveData<MovieDetails>> {

        private MoviesDao localDao;
        private AsyncTaskQueryResponse asyncResponse;
        private LiveData<MovieDetails> movie;

        public interface AsyncTaskQueryResponse {
            void processFinish(LiveData<MovieDetails> movie);
        }

        public AsyncTaskQueryMovie(MoviesDao moviesDao, AsyncTaskQueryResponse delegate) {
            localDao = moviesDao;
            asyncResponse = delegate;
        }

        @Override
        protected LiveData<MovieDetails> doInBackground(Integer... id) {
            if (!isCancelled()) {
                movie = localDao.getMovie(id[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(LiveData<MovieDetails> movieDetails) {
            super.onPostExecute(movieDetails);
            asyncResponse.processFinish(movie);
        }
    }

    public static class AsyncTaskQueryAll extends AsyncTask<Void, Void, LiveData<List<MovieDetails>>> {

        private MoviesDao localDao;
        private AsyncTaskQueryAllResponse asyncResponse;
        private LiveData<List<MovieDetails>> movieList;

        public interface AsyncTaskQueryAllResponse {
            void processFinish(LiveData<List<MovieDetails>> movieList);
        }

        public AsyncTaskQueryAll(MoviesDao moviesDao, AsyncTaskQueryAllResponse delegate) {
            localDao = moviesDao;
            asyncResponse = delegate;
        }

        @Override
        protected LiveData<List<MovieDetails>> doInBackground(Void... voids) {
            if (!isCancelled()) {
                movieList = localDao.getAllMovies();
            }
            return null;
        }

        @Override
        protected void onPostExecute(LiveData<List<MovieDetails>> movieDetails) {
            super.onPostExecute(movieDetails);
            asyncResponse.processFinish(movieList);
        }
    }

    public static class AsyncTaskInsert extends AsyncTask<MovieDetails, Void, Void>{

        private MoviesDao localDao;
        private AsyncResponse asyncResponse;

        public interface AsyncResponse {
            void processFinish();
        }

        public AsyncTaskInsert(MoviesDao moviesDao, AsyncResponse delegate) {
            localDao = moviesDao;
            asyncResponse = delegate;
        }

        @Override
        protected Void doInBackground(MovieDetails... movieDetails) {
            if (!isCancelled()) {
                if (movieDetails != null) {
                    localDao.insert(movieDetails[0]);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            asyncResponse.processFinish();
        }
    }

    public static class AsyncTaskDelete extends AsyncTask<MovieDetails, Void, Void>{

        private MoviesDao localDao;
        private AsyncResponse asyncResponse;

        public interface AsyncResponse {
            void processFinish();
        }

        public AsyncTaskDelete(MoviesDao moviesDao, AsyncResponse delegate) {
            localDao = moviesDao;
            asyncResponse = delegate;
        }

        @Override
        protected Void doInBackground(MovieDetails... movieDetails) {
            if (!isCancelled()) {
                if (movieDetails != null) {
                    localDao.deleteMovies(movieDetails[0]);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            asyncResponse.processFinish();
        }
    }
}
