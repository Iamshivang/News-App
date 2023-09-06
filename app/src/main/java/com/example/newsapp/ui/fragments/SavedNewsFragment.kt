package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.model.Article
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class SavedNewsFragment : Fragment(R.layout.fragment_saved_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var rvSavedNews: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvSavedNews= view.findViewById(R.id.rvSavedNews)
        viewModel= (activity as NewsActivity).viewModel

        setUpRecycleView()

        newsAdapter.setOnClickListener(object: NewsAdapter.OnClickListener{
            override fun onCLick(article: Article) {

                Log.e("BreakingNewsArticle", article.toString())
                val bundle= Bundle().apply {
                    putParcelable("article", article)
                }
                findNavController().navigate(
                    R.id.action_savedNewsFragment_to_articleFragment,
                    bundle
                )
            }
        })

        viewModel.getSavedNews().observe(viewLifecycleOwner, Observer {articles ->  // Observer function when items in bd(getSavedNews()) changed
            newsAdapter.differ.submitList(articles)
        })

        // CallBack
        val itemTouchHelperCallBack= object: ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article= newsAdapter.differ.currentList[position]
                viewModel.deleteArticle(article)
                Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo"){
                        viewModel.saveArticle(article)
                    }
                }.show()
            }

        }

        // Initializing ItemTouchHelper
        ItemTouchHelper(itemTouchHelperCallBack).apply {
            attachToRecyclerView(rvSavedNews)
        }
    }

    private fun setUpRecycleView(){
        newsAdapter= NewsAdapter()
        rvSavedNews.apply {
            adapter= newsAdapter
            layoutManager= LinearLayoutManager(activity)
        }
    }

}