package com.exercise.davismiyashiro.popularmovies.data.local;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

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
            Log.d(TAG, "Delete Movie ok");
            Intent deleteOk = new Intent(ACTION_DELETE);
            sendBroadcast(deleteOk);
        } else {
            Log.e(TAG, "Error deleting Movie");
        }
    }

    private void insertMovieDb(ContentValues values) {
        if (getContentResolver().insert(MoviesEntry.CONTENT_URI, values) != null) {
            Log.d(TAG, "Insert Movie ok");
            Intent insertOk = new Intent(ACTION_INSERT);
            sendBroadcast(insertOk);
        } else {
            Log.e(TAG, "Error inserting Movie");
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
}
