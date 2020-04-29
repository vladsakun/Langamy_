package com.langamy

import android.app.Application
import com.langamy.database.StudySetsDatabase
import com.langamy.datasource.LangamyNetworkDataSource
import com.langamy.datasource.LangamyNetworkDataSourceImpl
import com.langamy.provider.UserProvider
import com.langamy.provider.UserProviderImpl
import com.langamy.repositories.StudySetsRepository
import com.langamy.repositories.StudySetsRepositoryImpl
import com.langamy.retrofit.ConnectivityInterceptor
import com.langamy.retrofit.ConnectivityInterceptorImpl
import com.langamy.retrofit.LangamyApiService
import com.langamy.viewmodel.continue_learning.ContinueLearningViewModelFactory
import com.langamy.viewmodel.detail.SpecificStudySetsViewModelFactory
import com.langamy.viewmodel.edit.EditStudySetViewModelFactory
import com.langamy.viewmodel.learn.LearnActivityViewModelFactory
import com.langamy.viewmodel.list.StudySetsViewModelFactory
import com.langamy.viewmodel.profile.ProfileViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.*

class LangamyApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        //Dependency Injections
        import(androidXModule(this@LangamyApplication))

        //Database
        bind() from singleton { StudySetsDatabase(instance()) }
        //DAO
        bind() from singleton { instance<StudySetsDatabase>().studySetDao() }
        bind() from singleton { instance<StudySetsDatabase>().userDao() }

        //Connectivity
        bind<ConnectivityInterceptor>() with singleton { ConnectivityInterceptorImpl(instance()) }
        //Api service
        bind() from singleton { LangamyApiService(instance()) }
        //DataSource
        bind<LangamyNetworkDataSource>() with singleton { LangamyNetworkDataSourceImpl(instance()) }
        //Repository
        bind<StudySetsRepository>() with singleton { StudySetsRepositoryImpl(instance(), instance(), instance(), instance()) }

        //Providers
        //User email
        bind<UserProvider>() with singleton { UserProviderImpl(instance()) }

        //ViewModel Factories
        bind() from provider { StudySetsViewModelFactory(instance(), instance(), instance()) }
        bind() from provider { ProfileViewModelFactory(instance(), instance()) }
        bind() from provider { EditStudySetViewModelFactory(instance()) }
        bind() from provider { LearnActivityViewModelFactory(instance()) }
        bind() from provider { ContinueLearningViewModelFactory(instance()) }

        bind() from factory { studySetId: Int -> SpecificStudySetsViewModelFactory(instance(), studySetId) }

    }
}