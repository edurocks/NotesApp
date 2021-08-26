package com.androiddevs.ktornoteapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.androiddevs.ktornoteapp.data.local.NotesDatabase
import com.androiddevs.ktornoteapp.data.other.Constants
import com.androiddevs.ktornoteapp.data.remote.BasicAuthInterceptor
import com.androiddevs.ktornoteapp.data.remote.NoteApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideNotesDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, NotesDatabase::class.java, Constants.DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideNotesDao(db: NotesDatabase) = db.noteDao()

    @Singleton
    @Provides
    fun provideBasicAuthInterceptor() = BasicAuthInterceptor()

    @Singleton
    @Provides
    fun provideNotesApi(basicAuthInterceptor: BasicAuthInterceptor): NoteApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(basicAuthInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(NoteApi::class.java)
    }

    @Singleton
    @Provides
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            Constants.ENCRYPTED_SHARED_PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}