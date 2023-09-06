package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.model.Article

class NewsAdapter: RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    private var onClickListener: OnClickListener?= null

    inner class ArticleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val ivArticleImage= itemView.findViewById<ImageView>(R.id.ivArticleImage)
        val tvSource= itemView.findViewById<TextView>(R.id.tvSource)
        val tvTitle= itemView.findViewById<TextView>(R.id.tvTitle)
        val tvDescription= itemView.findViewById<TextView>(R.id.tvDescription)
        val tvPublishedAt= itemView.findViewById<TextView>(R.id.tvPublishedAt)
    }


    // De Future


    // Callback
    private val differCallBack= object: DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url== newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem== newItem
        }

    }

    // Async list differ - Tool take two list and compare
    val differ= AsyncListDiffer(this, differCallBack)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_article_preview,
                parent,
                false
            )
        )
    }



    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article= differ.currentList[position]

        Glide.with(holder.itemView).load(article.urlToImage).into(holder.ivArticleImage)
        holder.tvSource.text= article.source?.name
        holder.tvTitle.text= article.title
        holder.tvDescription.text= article.description
        holder.tvPublishedAt.text= article.publishedAt
        holder.itemView.setOnClickListener {
            onClickListener!!.onCLick(article)
        }
    }


    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onCLick(article: Article)
    }

}