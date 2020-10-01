/*
 * Copyright 2020 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.openapi.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class SwaggerUiTaskCoordinator @Inject constructor(sourceSetName: String) : DefaultTask() {

    companion object {
        const val BASE_TASK_NAME = "generate"
        const val TRAILING_TASK_NAME = "Web3jSwaggerUI"
    }

    init {
        group = "web3j"
        val generateSwaggerUiTask = project.tasks.getByName("generateSwaggerUI") as DefaultTask
        val resolveTask = project.tasks.getByName("resolve") as DefaultTask
        val moveTask = project.tasks.getByName("${SwaggerUiMover.BASE_TASK_NAME}${sourceSetName}${SwaggerUiMover.TRAILING_TASK_NAME}")

        generateSwaggerUiTask.mustRunAfter(resolveTask)
        moveTask.mustRunAfter(generateSwaggerUiTask)
        this.dependsOn(resolveTask, generateSwaggerUiTask, moveTask)
    }
}

open class SwaggerUiMover @Inject constructor(
    private val outputDirPath: String
) : DefaultTask() {

    companion object {
        const val BASE_TASK_NAME = "move"
        const val TRAILING_TASK_NAME = "SwaggerUiToResources"
    }

    @TaskAction
    fun moveSwaggerUi() {
        File("${project.buildDir.absolutePath}${File.separator}swagger-ui-openapi").copyRecursively(
                File("$outputDirPath${File.separator}static${File.separator}swagger-ui"),
                true)
    }
}

fun taskNameCreator(baseName: String, trailingName: String, sourceSetName: String): String =
    "${baseName}${sourceSetName}$trailingName"
