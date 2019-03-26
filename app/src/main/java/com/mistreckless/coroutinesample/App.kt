package com.mistreckless.coroutinesample

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import com.mistreckless.coroutinesample.service.FoursquareApiService
import com.mistreckless.coroutinesample.util.CoroutineCallAdapterFactory
import com.mistreckless.coroutinesample.util.FoursquareInterceptor
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.annotation.RetentionPolicy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Scope
import javax.inject.Singleton

class App : Application(), HasActivityInjector {

    @Inject
    lateinit var activityDispatchAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder()
            .application(this)
            .appModule(AppModule)
            .build()
            .inject(this)


    }

    override fun activityInjector() = activityDispatchAndroidInjector

}

@Singleton
@Component(
    modules = [(AndroidInjectionModule::class),
        (AppModule::class),
        (ActivityBuilder::class)]
)
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun appModule(appModule: AppModule): Builder
        fun build(): AppComponent
    }

    fun inject(app: App)
}

@Module
object AppModule {

    @Singleton
    @Provides
    @JvmStatic
    fun provideFoursquareApiService(): FoursquareApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(FoursquareInterceptor())
            .build()

        return Retrofit.Builder()
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.FoursquareUrl)
            .client(client)
            .build()
            .create(FoursquareApiService::class.java)
    }
}

@Module
abstract class ActivityBuilder {

    @PerActivity
    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun bindMainActivity(): MainActivity
}

@Module
object MainModule {

    @PerActivity
    @Provides
    @JvmStatic
    fun provideMainViewModel(activity: MainActivity, factory: MainViewModelFactory): MainViewModel =
        ViewModelProviders.of(activity, factory)[MainViewModel::class.java]
}

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory @Inject constructor(private val apiService: FoursquareApiService) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MainViewModel::class.java))
        return MainViewModel(apiService) as T
    }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerActivity