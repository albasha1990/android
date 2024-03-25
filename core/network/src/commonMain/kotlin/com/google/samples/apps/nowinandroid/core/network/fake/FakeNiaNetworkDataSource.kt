/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.core.network.demo

<<<<<<<< HEAD:core/network/src/commonMain/kotlin/com/google/samples/apps/nowinandroid/core/network/fake/FakeNiaNetworkDataSource.kt
import com.google.samples.apps.nowinandroid.core.di.IODispatcher
========
import JvmUnitTestDemoAssetManager
import com.google.samples.apps.nowinandroid.core.network.Dispatcher
import com.google.samples.apps.nowinandroid.core.network.NiaDispatchers.IO
>>>>>>>> upstream/main:core/network/src/commonMain/kotlin/com/google/samples/apps/nowinandroid/core/network/demo/DemoNiaNetworkDataSource.kt
import com.google.samples.apps.nowinandroid.core.network.NiaNetworkDataSource
import com.google.samples.apps.nowinandroid.core.network.assets.NEWS_DATA
import com.google.samples.apps.nowinandroid.core.network.assets.TOPICS_DATA
import com.google.samples.apps.nowinandroid.core.network.model.NetworkChangeList
import com.google.samples.apps.nowinandroid.core.network.model.NetworkNewsResource
import com.google.samples.apps.nowinandroid.core.network.model.NetworkTopic
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

/**
 * [NiaNetworkDataSource] implementation that provides static news resources to aid development
 */
<<<<<<<< HEAD:core/network/src/commonMain/kotlin/com/google/samples/apps/nowinandroid/core/network/fake/FakeNiaNetworkDataSource.kt
class FakeNiaNetworkDataSource @Inject constructor(
    private val ioDispatcher: IODispatcher,
    private val networkJson: Json,
========
class DemoNiaNetworkDataSource @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
    private val assets: DemoAssetManager = JvmUnitTestDemoAssetManager,
>>>>>>>> upstream/main:core/network/src/commonMain/kotlin/com/google/samples/apps/nowinandroid/core/network/demo/DemoNiaNetworkDataSource.kt
) : NiaNetworkDataSource {

    override suspend fun getTopics(ids: List<String>?): List<NetworkTopic> =
        withContext(ioDispatcher) {
            networkJson.decodeFromString(TOPICS_DATA)
        }

    override suspend fun getNewsResources(ids: List<String>?): List<NetworkNewsResource> =
        withContext(ioDispatcher) {
            networkJson.decodeFromString(NEWS_DATA)
        }

    override suspend fun getTopicChangeList(after: Int?): List<NetworkChangeList> =
        getTopics().mapToChangeList(NetworkTopic::id)

    override suspend fun getNewsResourceChangeList(after: Int?): List<NetworkChangeList> =
        getNewsResources().mapToChangeList(NetworkNewsResource::id)
}

/**
 * Converts a list of [T] to change list of all the items in it where [idGetter] defines the
 * [NetworkChangeList.id]
 */
private fun <T> List<T>.mapToChangeList(
    idGetter: (T) -> String,
) = mapIndexed { index, item ->
    NetworkChangeList(
        id = idGetter(item),
        changeListVersion = index,
        isDelete = false,
    )
}
