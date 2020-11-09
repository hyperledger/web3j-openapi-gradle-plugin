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
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Performs Swagger UI generation by calling Swagger plugins and moving the
 * generated resources to the project resources output directory for runtime access.
 */
@CacheableTask
open class GenerateSwaggerUi : DefaultTask() {

    @InputDirectory
    @PathSensitive(PathSensitivity.ABSOLUTE)
    lateinit var inputDir: File

    @OutputDirectory
    lateinit var outputDir: File

    init {
        val generateSwaggerOpenApiUi = project.tasks.getByName("generateSwaggerUIOpenapi")
        generateSwaggerOpenApiUi.dependsOn(project.tasks.getByName("resolve"))
        this.dependsOn(generateSwaggerOpenApiUi)
    }

    @TaskAction
    fun moveSwaggerUi() {
        inputDir.copyRecursively(
            outputDir.toPath()
                .resolve("static")
                .resolve("swagger-ui")
                .toFile(),
            true
        )
    }
}
