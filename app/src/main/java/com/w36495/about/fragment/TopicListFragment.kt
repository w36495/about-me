package com.w36495.about.fragment

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.w36495.about.listener.TopicListClickListener
import com.w36495.about.R
import com.w36495.about.adapter.TopicListAdapter
import com.w36495.about.data.Topic
import com.w36495.about.data.TopicContract
import com.w36495.about.data.TopicPresenter
import com.w36495.about.data.repository.TopicRepository
import com.w36495.about.data.local.AppDatabase
import com.w36495.about.dialog.TopicAddDialog
import com.w36495.about.listener.TopicDialogClickListener

class TopicListFragment : Fragment(), TopicListClickListener, TopicDialogClickListener, TopicContract.View {

    private lateinit var recyclerView: RecyclerView
    private lateinit var topicListAdapter: TopicListAdapter
    private lateinit var toolbar: MaterialToolbar

    private var database: AppDatabase? = null
    private lateinit var presenter: TopicContract.Presenter

    private val size = Point()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_topic_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val windowManager = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        display.getSize(size)

        database = AppDatabase.getInstance(view.context)

        recyclerView = view.findViewById(R.id.topic_list_recyclerview)
        toolbar = view.findViewById(R.id.topic_list_toolbar)

        topicListAdapter = TopicListAdapter()
        topicListAdapter.setClickListener(this)

        recyclerView.adapter = topicListAdapter
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)

        toolbar.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.main_add -> {
                    TopicAddDialog(size, this).show(
                        parentFragmentManager, "topic"
                    )
                    true
                }
                else -> false
            }
        }
        presenter = TopicPresenter(TopicRepository(database!!.topicDao()), this)
        presenter.getTopicList()
    }

    override fun onTopicListItemClicked(topicId: Long) {
        presenter.getTopic(topicId)
    }

    override fun onTopicSaveClicked(topic: Topic) {
        presenter.saveTopic(topic)
    }

    override fun onTopicDeleteClicked(topicId: Long) {
        presenter.deleteTopicById(topicId)
        // TODO: Think 에서도 삭제되어야 함
    }

    override fun showTopicList(topicList: List<Topic>) {
        topicListAdapter.setTopicList(topicList)
    }

    override fun setTopic(topic: Topic) {
        parentFragmentManager.commit {
            replace(R.id.main_fragment_container, ThinkListFragment(topic))
            setReorderingAllowed(true)
            addToBackStack("thinkFragment")
        }
    }
}