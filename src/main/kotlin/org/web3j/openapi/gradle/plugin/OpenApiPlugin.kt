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

import org.codehaus.groovy.runtime.InvokerHelper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceTask
import org.web3j.gradle.plugin.Web3jExtension
import org.web3j.gradle.plugin.Web3jPlugin
import java.io.File
import java.nio.file.Paths

class OpenApiPlugin : Plugin<Project>, Web3jPlugin() {

    override fun registerExtensions(project: Project) {
        project.extensions.create(Web3jExtension.NAME, OpenApiExtension::class.java, project)
    }

    override fun apply(project: Project) {
        super.apply(project)
        addDependencies(project)

        val sourceSets: SourceSetContainer = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets

        project.afterEvaluate { sourceSets.forEach { sourceSet -> openApiGenerationConfig(project, sourceSet) } }
    }

    private fun addDependencies(project: Project) {
    }

    /**
     * Configures Web3j-OpenApi project generation tasks for the Solidity source sets defined in the project (e.g.
     * main, test).
     *
     * <p>The generated task name for the <code>main</code> source set will be <code>
     * generateWeb3jOpenApi</code>, and for <code>test</code> <code>generateTestWeb3jOpenApi
     * </code>.
     */
    private fun openApiGenerationConfig(project: Project, sourceSet: SourceSet) {
        val openApiExtension = InvokerHelper.getProperty(project, Web3jExtension.NAME) as OpenApiExtension

        File(openApiExtension.generatedFilesBaseDir).deleteRecursively()
        val projectOutputDir: File = File(openApiExtension.generatedFilesBaseDir).apply { mkdirs() }

        // Add source set to the project Java source sets
        sourceSet.java.srcDir(projectOutputDir)

        val srcSetName = if (sourceSet.name == "main") ""
        else sourceSet.name.capitalize()

        val generateOpenApiTaskName = "generate${srcSetName}Web3jOpenAPI"

        val task: OpenApiGenerator = project.tasks.create(generateOpenApiTaskName, OpenApiGenerator::class.java)

        task.apply {
            group = "web3j"
            description = "Generates Web3j-OpenAPI project from Solidity contracts."
            generatedFilesBaseDir = projectOutputDir.absolutePath.substringBefore("server/src/main")
            addressLength = openApiExtension.addressBitLength
            contextPath = openApiExtension.openApi.contextPath
            packageName = openApiExtension.generatedPackageName.substringBefore(".wrappers")
            projectName = openApiExtension.openApi.projectName
            contractsAbi = getContractsData(openApiExtension.openApi.contractsAbis, project)
            contractsBin = getContractsData(openApiExtension.openApi.contractsBins, project)
            generateSwaggerUI = openApiExtension.openApi.generateSwaggerUI
        }

        val wrapperGenerationTask = project.tasks.getByName("generate${srcSetName}ContractWrappers") as SourceTask
        val compileJava = project.tasks.getByName("compile${srcSetName}Java") as SourceTask

        task.also {
            it.dependsOn(wrapperGenerationTask)
            it.mustRunAfter(wrapperGenerationTask)
            compileJava.dependsOn(it)
        }
    }

    private fun getContractsData(dataList: List<String>, project: Project): List<File> {
        return dataList.toMutableList().map { File(it) }.toMutableList().also {
            it.add(File(Paths.get(project.buildDir.absolutePath, "resources", "main", "solidity").toString()))
        }
    }
}
