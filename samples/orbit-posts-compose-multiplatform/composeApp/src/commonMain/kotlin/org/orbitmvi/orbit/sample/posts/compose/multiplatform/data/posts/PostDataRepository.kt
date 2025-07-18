/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.orbitmvi.orbit.sample.posts.compose.multiplatform.data.posts

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.data.posts.network.AvatarUrlGenerator
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.data.posts.network.PostNetworkDataSource
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostComment
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostDetail
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostRepository

public class PostDataRepository(
    private val networkDataSource: PostNetworkDataSource,
    private val avatarUrlGenerator: AvatarUrlGenerator
) : PostRepository {
    override suspend fun getOverviews(): List<PostOverview> {
        return coroutineScope {
            val posts = async { networkDataSource.getPosts() }
            val users = async { networkDataSource.getUsers() }

            posts.await().map { post ->
                val user = users.await().first { it.id == post.userId }

                PostOverview(
                    post.id,
                    avatarUrlGenerator.generateUrl(user.email),
                    post.title,
                    user.name
                )
            }
        }
    }

    override suspend fun getDetail(id: Int): PostDetail {
        return coroutineScope {
            val postData = networkDataSource.getPost(id)

            val comments = async {
                networkDataSource.getComments()
                    .filter { it.postId == postData.id }
            }

            PostDetail(
                postData.id,
                postData.body,
                comments.await().map {
                    PostComment(
                        it.id,
                        it.name,
                        it.email,
                        it.body
                    )
                }
            )
        }
    }
}
