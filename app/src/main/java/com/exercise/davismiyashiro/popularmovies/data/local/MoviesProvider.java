package com.exercise.davismiyashiro.popularmovies.data.local;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.exercise.davismiyashiro.popularmovies.data.local.MoviesDbContract.*;

/**
 * Created by Davis Miyashiro on 16/03/2017.
 */

public class MoviesProvider extends ContentProvider {

    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIES_BY_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        /* This URI is content://com.exercise.davismiyashiro.popularmovies/movies/ */
        matcher.addURI(authority, PATH_MOVIES, CODE_MOVIES);

        /*
         * This URI would look something like content://com.exercise.davismiyashiro.popularmovies/movies/1472214172
         * The "/#" signifies to the UriMatcher that if PATH_MOVIES is followed by ANY number,
         * that it should return the CODE_MOVIES_BY_ID code
         */
        matcher.addURI(authority, PATH_MOVIES + "/#", CODE_MOVIES_BY_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case CODE_MOVIES:
                retCursor = db.query(MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_MOVIES_BY_ID:
                //TODO: return row with movieID?
                retCursor = db.query(MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);

        switch (match) {
            case CODE_MOVIES:
                // directory
                return "vnd.android.cursor.dir" + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
            case CODE_MOVIES_BY_ID:
                // single item type
                return "vnd.android.cursor.item" + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CODE_MOVIES:
                long id = db.insert(MoviesEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MoviesEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int moviesDeleted;

        switch (match) {
            case CODE_MOVIES_BY_ID:
                String id = uri.getPathSegments().get(1);
                moviesDeleted = db.delete(MoviesEntry.TABLE_NAME, MoviesEntry.COLUMN_MOVIE_ID + "=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (moviesDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return moviesDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int moviesUpdated;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case CODE_MOVIES_BY_ID:
                String id = uri.getPathSegments().get(1);
                moviesUpdated = mOpenHelper.getWritableDatabase().update(MoviesEntry.TABLE_NAME, contentValues, MoviesEntry.COLUMN_MOVIE_ID + "=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (moviesUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return moviesUpdated;
    }
}
