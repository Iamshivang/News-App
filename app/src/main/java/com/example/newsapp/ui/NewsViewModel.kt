package com.example.newsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.NewsApplication
import com.example.newsapp.model.Article
import com.example.newsapp.model.NewsResponse
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.utils.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(
    app: Application,
    private val newsRepository: NewsRepository
): AndroidViewModel(app) {

    val breakingNews = MutableLiveData<Resource<NewsResponse>>()
    var breakingNewsPage= 1
    var breakingNewsResponse: NewsResponse?= null

    val searchNews = MutableLiveData<Resource<NewsResponse>>()
    var searchNewsPage= 1
    var searchNewsResponse: NewsResponse?= null

    init {
        getBreakingNews("in")
    }

    fun getBreakingNews(countryCode: String)= viewModelScope.launch{
        breakingNews.postValue(Resource.Loading())

        try {
            if (hasInternetConnection()){
                val response= newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingResponse(response))
            }else{
                breakingNews.postValue(Resource.Error("No Internet Connection."))
            }
        } catch (t: Throwable){
            when(t){
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure."))
                else -> breakingNews.postValue(Resource.Error("Conversion Failure."))
            }
        }

    }

    fun searchNews(searchQuery: String)= viewModelScope.launch{
        searchNews.postValue(Resource.Loading())

        try {
            if (hasInternetConnection()){
                val response= newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchResponse(response))
            }else{
                searchNews.postValue(Resource.Error("No Internet Connection."))
            }
        } catch (t: Throwable){
            when(t){
                is IOException -> searchNews.postValue(Resource.Error("Network Failure."))
                else -> searchNews.postValue(Resource.Error("Conversion Failure."))
            }
        }
    }

    private fun handleBreakingResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?.let {resultResponse ->
                breakingNewsPage++
                if(breakingNewsResponse== null){
                    breakingNewsResponse= resultResponse
                }else{
                    val oldArticles= breakingNewsResponse?.articles
                    val newArticles= resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if(response.isSuccessful){
            response.body()?.let {resultResponse ->
                searchNewsPage++
                if(searchNewsResponse== null){
                    searchNewsResponse= resultResponse
                }else{
                    val oldArticles= searchNewsResponse?.articles
                    val newArticles= resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveArticle(article: Article)= viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedNews()= newsRepository.getSavedNews()

    fun deleteArticle(article: Article)= viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    private fun hasInternetConnection(): Boolean{

        // getApplication<>() is only available in AndroidViewModel not in ViewModel
        val connectivityManager= getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        )  as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork= connectivityManager.activeNetwork?: return false
            val capabilities= connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }else{
            connectivityManager.activeNetworkInfo?.run {
                return when(type){
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false
    }
}