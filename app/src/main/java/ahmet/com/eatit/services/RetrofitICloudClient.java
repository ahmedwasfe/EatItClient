package ahmet.com.eatit.services;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitICloudClient {

    private static Retrofit retrofit;

    public static Retrofit getRetrofit(){
        return retrofit == null ? new Retrofit.Builder()
                .baseUrl("https://us-central1-eat-it-f1493.cloudfunctions.net/widgets/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build() : retrofit;
    }
}
