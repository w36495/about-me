package com.w36495.about.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.w36495.about.domain.entity.Topic

@Dao
interface TopicDao{
    @Query("SELECT * FROM topics")
    suspend fun getTopics(): List<Topic>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicById(topicId: Long): Topic

    @Insert
    suspend fun insertTopic(topic: Topic)

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun deleteTopicById(topicId: Long)

    @Query("DELETE FROM topics")
    suspend fun deleteAllTopic()

    @Query("SELECT count(*) FROM topics")
    suspend fun getTopicListCount(): Int
}