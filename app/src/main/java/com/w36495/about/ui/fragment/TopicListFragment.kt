package com.w36495.about.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.w36495.about.R
import com.w36495.about.ui.adapter.TopicListAdapter
import com.w36495.about.domain.entity.Topic
import com.w36495.about.contract.TopicContract
import com.w36495.about.data.TopicUiState
import com.w36495.about.ui.presenter.TopicPresenter
import com.w36495.about.data.local.AppDatabase
import com.w36495.about.data.repository.ThinkRepository
import com.w36495.about.data.repository.TopicRepositoryImpl
import com.w36495.about.ui.dialog.TopicAddDialogActivity
import com.w36495.about.ui.listener.TopicListClickListener
import kotlinx.coroutines.launch

class TopicListFragment : Fragment(), TopicListClickListener, TopicContract.View {

    private lateinit var topicListContext: Context
    private var database: AppDatabase? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var topicListAdapter: TopicListAdapter
    private lateinit var toolbar: MaterialToolbar

    private lateinit var presenter: TopicContract.Presenter

    private lateinit var getResultTopic: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_topic_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topicListContext = view.context

        getResultTopic = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val topic = Topic(
                        it.getStringExtra("topic")!!,
                        it.getStringExtra("color")!!,
                        it.getStringExtra("date")!!
                    )
                    (presenter as TopicPresenter).saveTopic(topic)
                }
            } else {
                println("===== getResultTopic - Failed =====")
            }
        }

        val _colors = resources.getStringArray(R.array.about_default_gradient_grey)
        val colors = arrayListOf<String>()
        _colors.forEach { colors.add(it) }

        database = AppDatabase.getInstance(view.context)

        recyclerView = view.findViewById(R.id.topic_list_recyclerview)
        toolbar = view.findViewById(R.id.topic_list_toolbar)

        topicListAdapter = TopicListAdapter(topicListContext, colors)
        topicListAdapter.setClickListener(this)

        recyclerView.adapter = topicListAdapter
        recyclerView.layoutManager = LinearLayoutManager(topicListContext)

        presenter = TopicPresenter(
            TopicRepositoryImpl(database!!.topicDao()),
            ThinkRepository(database!!.thinkDao()),
            this
        )

        toolbar.setOnMenuItemClickListener { menu ->
            when (menu.itemId) {
                R.id.main_add -> {
                    val topicAddIntent = Intent(view.context, TopicAddDialogActivity::class.java)
                    getResultTopic.launch(topicAddIntent)
                    true
                }
                else -> false
            }
        }

        showTopics()
    }

    private fun showTopics() {
        presenter.getTopicList()

        lifecycleScope.launch {
            (presenter as TopicPresenter).uiState.collect { uiState ->
                when (uiState) {
                    is TopicUiState.Loading -> {
                        println("===== loading =====")
                    }
                    is TopicUiState.Empty -> {
                        println("===== empty =====")
                    }
                    is TopicUiState.Success -> {
                        topicListAdapter.setTopicList(uiState.list)
                    }
                    is TopicUiState.Failed -> {
                        println("===== failed : ${uiState.message} =====")
                    }
                }
            }
        }
    }

    override fun onTopicListItemClicked(topicId: Long) {
        presenter.getTopic(topicId)
    }

    override fun onTopicDeleteClicked(topicId: Long) {
        presenter.deleteTopicById(topicId)
        // TODO: Think 에서도 삭제되어야 함
    }

    override fun showTopicList(topicList: List<Topic>) {
        topicListAdapter.setTopicList(topicList)
    }

    override fun showTopic(topic: Topic) {
        parentFragmentManager.commit {
            replace(R.id.main_fragment_container, ThinkListFragment(topic))
            setReorderingAllowed(true)
        }
    }

    override fun showError(tag: String, message: String?) {
        val handler = Handler(Looper.getMainLooper())

        if (tag == "TOPIC_DELETE") {
            handler.postDelayed(Runnable {
                Toast.makeText(topicListContext, "삭제하는 과정에서 오류가 발생하였습니다.", Toast.LENGTH_SHORT)
                    .show()
            }, 0)
        } else if (tag == "TOPIC_INSERT") {
            handler.postDelayed(Runnable {
                Toast.makeText(topicListContext, "저장하는 과정에서 오류가 발생하였습니다.", Toast.LENGTH_SHORT)
                    .show()
            }, 0)
        }
    }

    override fun showToast(message: String?) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable {
            Toast.makeText(topicListContext, message, Toast.LENGTH_SHORT).show()
        }, 0)
        showTopics()
    }
}