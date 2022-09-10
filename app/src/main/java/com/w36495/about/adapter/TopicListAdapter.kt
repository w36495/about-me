package com.w36495.about.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.w36495.about.listener.TopicListClickListener
import com.w36495.about.R
import com.w36495.about.data.Topic
import com.w36495.about.util.calDate

class TopicListAdapter(private val context: Context) : RecyclerView.Adapter<TopicListAdapter.TopicListViewHolder>() {

    private var topicList = arrayListOf<Topic>()
    private lateinit var topicListClickListener: TopicListClickListener

    class TopicListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val count: Button
        val topic: TextView
        val date: TextView
        val cardView: CardView

        init {
            count = view.findViewById(R.id.topic_list_item_think_count)
            topic = view.findViewById(R.id.topic_list_item_topic)
            date = view.findViewById(R.id.topic_list_item_date)
            cardView = view.findViewById(R.id.topic_list_item_cardView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicListViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_topic_list_item, parent, false)
        return TopicListViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicListViewHolder, position: Int) {
        if (position != 0) {
            val margin = dpToPx(16)
            val layoutParams: FrameLayout.LayoutParams = holder.cardView.layoutParams as FrameLayout.LayoutParams
            layoutParams.setMargins(margin, 0, margin, margin)
            holder.cardView.layoutParams = layoutParams
        }

        holder.count.text = topicList[position].count.toString()
        holder.date.text = calDate(topicList[position].registDate).toString()
        holder.topic.text = topicList[position].topic
        holder.count.setBackgroundColor(Color.parseColor(topicList[position].color))

        holder.cardView.setOnClickListener {
            topicListClickListener.onTopicListItemClicked(topicList[position].id)
        }

        holder.cardView.setOnLongClickListener {
            topicListClickListener.onTopicDeleteClicked(topicList[position].id)
            true
        }
    }

    override fun getItemCount(): Int = topicList.size

    fun setClickListener(topicListClickListener: TopicListClickListener) {
        this.topicListClickListener = topicListClickListener
    }

    fun setTopicList(topicList: List<Topic>) {
        this.topicList = topicList as ArrayList<Topic>
        notifyDataSetChanged()
    }

    fun appTopicList(topic: Topic) {
        topicList.add(topic)
        notifyDataSetChanged()
    }

    fun deleteTopic(index: Int) {
        topicList.removeAt(index)
        notifyDataSetChanged()
    }

    private fun dpToPx(dp: Int): Int {
        val px: Float = dp * context.resources.displayMetrics.density
        return px.toInt()
    }
}