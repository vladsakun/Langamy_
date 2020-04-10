package com.langamy

import android.app.Application
import com.langamy.database.StudySetsDatabase
import com.langamy.repositories.StudySetsRepository
import com.langamy.repositories.StudySetsRepositoryImpl
import com.langamy.retrofit.*
import com.langamy.viewmodel.StudySetsViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class LangamyApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        //Dependency Injections
        import(androidXModule(this@LangamyApplication))

        //Database
        bind() from singleton { StudySetsDatabase(instance()) }
        //DAO
        bind() from singleton { instance<StudySetsDatabase>().studySetDao() }
        //Connectivity
        bind<ConnectivityInterceptor>() with singleton { ConnectivityInterceptorImpl(instance()) }
        //Api service
        bind() from singleton { LangamyApiService(instance()) }
        //DataSource
        bind<LangamyNetworkDataSource>() with singleton { LangamyNetworkDataSourceImpl(instance()) }
        //Repository
        bind<StudySetsRepository>() with singleton { StudySetsRepositoryImpl(instance(), instance()) }

        bind() from provider { StudySetsViewModelFactory(instance()) }

    }
}