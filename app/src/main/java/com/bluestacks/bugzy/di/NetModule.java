package com.bluestacks.bugzy.di;

import com.bluestacks.bugzy.common.Const;
import com.bluestacks.bugzy.net.ConnectivityInterceptor;
import com.bluestacks.bugzy.net.FogbugzApiService;
import com.bluestacks.bugzy.utils.PrefsHelper;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

@Module
public class NetModule {
    private final String mBaseUrl;

    public NetModule(String url) {
        mBaseUrl = url;
    }

    @Provides @Singleton
    PrefsHelper provicePreferenceHelper(Application application) {
        return new PrefsHelper(application.getApplicationContext());
    }

    @Provides @Singleton
    FogbugzApiService provideFogBugzService(Application application) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(
                        SimpleXmlConverterFactory.create()
                );

        Retrofit retrofit = builder
                .client(
                        httpClient.addInterceptor(
                                new ConnectivityInterceptor(application.getApplicationContext())
                        ).build()
                )
                .build();
        return retrofit.create(FogbugzApiService.class);
    }
}
