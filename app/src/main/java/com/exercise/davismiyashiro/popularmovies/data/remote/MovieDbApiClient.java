package com.exercise.davismiyashiro.popularmovies.data.remote;

import com.exercise.davismiyashiro.popularmovies.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Retrofit client
 *
 * Created by Davis Miyashiro on 20/02/2017.
 */

public class MovieDbApiClient {

    private static final String THEMOVIEDB_API = "https://api.themoviedb.org";

    private TheMovieDb service;

    public TheMovieDb getService() {
        if (service == null) {
            Retrofit retrofitSingle = new Retrofit.Builder()
                    .baseUrl(THEMOVIEDB_API)
                    .client(getOkHttp())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            service = retrofitSingle.create(TheMovieDb.class);
        }
        return service;
    }

    private OkHttpClient getOkHttp () {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        return httpClient.addInterceptor(logging).build();
    }

    /**
     * Asynchronous Retrofit call
     *
     * @param call
     * @param listener
     */
    public static void enqueue(Call call, final RequestListener listener) {

        if (call == null) {
            return;
        }

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, retrofit2.Response response) {
                if (response.isSuccessful()) {
                    if (listener != null) {
                        listener.onRequestSuccess(response.body());
                    }
                } else {
                    int code = response.code();

                    if (code == 401) {
                        Timber.d("onRequestUnauthenticated" + response.message());
                    } else if (code >= 400 && code < 500) {
                        Timber.d("onRequestClientError" + response.message());
                    } else if (code >= 500 && code < 600){
                        Timber.d("onRequestServerError" + response.message());
                    } else {
                        onFailure(call, new UnknownError(response.code() + " " + response.message()));
                    }
                }
            }

            @Override
            public void onFailure(Call call, Throwable throwable) {

                if (listener != null) {
                    listener.onRequestFailure(throwable);
                }
            }
        });
    }

    public interface RequestListener<T> {
        void onRequestFailure(Throwable throwable);

        void onRequestSuccess(T result);
    }
}
