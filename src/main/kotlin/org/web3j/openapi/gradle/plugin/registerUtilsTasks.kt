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

open class SwaggerUiTaskCoordinator @Inject constructor() : DefaultTask() {

    companion object {
        const val TASK_NAME = "generateCompleteSwaggerUi"
    }

    @TaskAction
    fun coordinateSwaggerUiTasks() {
        val generateSwaggerUiTask = project.tasks.getByName("generateSwaggerUI") as DefaultTask
        val resolveTask = project.tasks.getByName("resolve") as DefaultTask
        val moveTask = project.tasks.getByName(SwaggerUiMover.TASK_NAME)

        generateSwaggerUiTask.mustRunAfter(resolveTask)
        moveTask.mustRunAfter(generateSwaggerUiTask)
        this.dependsOn(resolveTask, generateSwaggerUiTask, moveTask)
    }
}

open class SwaggerUiMover @Inject constructor() : DefaultTask() {

    companion object {
        const val TASK_NAME = "moveSwaggerUiToResources"
    }

    @TaskAction
    fun moveSwaggerUi(outputDir: String) {
        doLast {
            it.ant.invokeMethod("move",
                    """file: "${project.buildDir.absolutePath}/swagger-ui-openapi",
                    todir: "${project.rootDir}/src/main/resources/static"""")
            File("${project.rootDir}/src/main/resources/static/swagger-ui").deleteRecursively()
            File("${project.rootDir}/src/main/resources/static/swagger-ui-openapi")
                    .renameTo(File("${project.rootDir}/src/main/resources/static/swagger-ui"))
        }
    }
}
