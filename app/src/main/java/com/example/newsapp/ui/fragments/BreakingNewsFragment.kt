package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.model.Article
import com.example.newsapp.model.Source
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.utils.Resource
import com.google.gson.Gson

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    val TAG= "BreakingNewsFragment"

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var rvBreakingNews: RecyclerView
    lateinit var paginationProgressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvBreakingNews= view.findViewById(R.id.rvBreakingNews)
        paginationProgressBar= view.findViewById(R.id.paginationProgressBar)

        viewModel= (activity as NewsActivity).viewModel
        setUpRecycleView()

        newsAdapter.setOnClickListener(object: NewsAdapter.OnClickListener{
            override fun onCLick(article: Article) {

                Log.e("BreakingNewsArticle", article.toString())
//                val action = BreakingNewsFragmentDirections.actionBreakingNewsFragmentToArticleFragment(article)
//                findNavController().navigate(action)


                val bundle= Bundle().apply {
                    putParcelable("article", article)
//                    putString("article", Gson().toJson(article))
                }
                findNavController().navigate(
                    R.id.action_breakingNewsFragment_to_articleFragment,
                    bundle
                )
            }
        })

        viewModel.breakingNews.observe(viewLifecycleOwner, Observer {resource ->
            when(resource){
                is Resource.Success -> {
                    hideProgressBar()
                    resource.data?.let {newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles)
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2 // reason pf 2: exclude last page and round off of division
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        if(isLastPage) {
                            rvBreakingNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    resource.message?.let {
                        Log.e(TAG, "An Error occurred: $it")
                        Toast.makeText(activity, "Error Occurred: ${it.toString()}", Toast.LENGTH_LONG).show()
                    }
                }

                 is Resource.Loading -> {
                     showProgressBar()
                 }
            }

        })
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading= false
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading= true
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if(shouldPaginate) {
                viewModel.getBreakingNews("in")
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) { // true if we are currently scrolling
                isScrolling = true
            }
        }
    }

    private fun setUpRecycleView(){
        newsAdapter= NewsAdapter()
        rvBreakingNews.apply {
            adapter= newsAdapter
            layoutManager= LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }


}