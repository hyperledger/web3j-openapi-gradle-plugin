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

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import io.swagger.v3.plugins.gradle.SwaggerPlugin
import org.codehaus.groovy.runtime.InvokerHelper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceTask
import org.hidetake.gradle.swagger.generator.SwaggerGeneratorPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.web3j.gradle.plugin.Web3jExtension
import org.web3j.gradle.plugin.Web3jPlugin
import java.io.File
import java.nio.file.Paths

class OpenApiPlugin : Web3jPlugin() {

    override fun apply(project: Project) {
        super.apply(project)
        registerPlugins(project)
        registerDependencies(project)
        setProperties(project)

        val sourceSets: SourceSetContainer = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
        project.afterEvaluate { sourceSets.forEach { sourceSet -> openApiGenerationConfig(project, sourceSet) } }
    }

    private fun registerSwaggerUtilsTasks(project: Project, resourcesOutputDir: File, sourceSetName: String) {
        with(project.tasks) {
            create(
                taskNameCreator(SwaggerUiMover.BASE_TASK_NAME, SwaggerUiMover.TRAILING_TASK_NAME, sourceSetName),
                SwaggerUiMover::class.java,
                resourcesOutputDir.absolutePath
            )
            create(
                taskNameCreator(SwaggerUiTaskCoordinator.BASE_TASK_NAME, SwaggerUiTaskCoordinator.TRAILING_TASK_NAME, sourceSetName),
                SwaggerUiTaskCoordinator::class.java,
                sourceSetName
            )
        }
    }

    private fun setProperties(project: Project) {
        project.setProperty("mainClassName", "org.web3j.openapi.server.console.RunServerCommand")
    }

    override fun registerExtensions(project: Project) {
        project.extensions.create(Web3jExtension.NAME, OpenApiExtension::class.java, project)
    }

    private fun registerPlugins(project: Project) {
        with(project.pluginManager) {
            apply(JavaPlugin::class.java)
            apply(KotlinPlatformJvmPlugin::class.java)
            apply(ApplicationPlugin::class.java)
            apply(ShadowPlugin::class.java)
            apply(SwaggerGeneratorPlugin::class.java)
            apply(SwaggerPlugin::class.java)
        }
    }

    private fun registerDependencies(project: Project) {
        with(project.dependencies) {
            add("api", "org.web3j.openapi:web3j-openapi-server:4.6.4")
            add("api", "org.web3j.openapi:web3j-openapi-core:4.6.4")
            add("implementation", "io.swagger.core.v3:swagger-annotations:2.1.2")
            add("implementation", "org.glassfish.jersey.media:jersey-media-json-jackson:2.31")
            add("implementation", "org.glassfish.jersey.containers:jersey-container-servlet:2.31")
            add("implementation", "io.github.microutils:kotlin-logging:1.7.9")
            add("swaggerUI", "org.webjars:swagger-ui:3.10.0")
        }
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

        val projectOutputDir: File = File(
                if (Paths.get(openApiExtension.generatedFilesBaseDir).isAbsolute)
                    openApiExtension.generatedFilesBaseDir
                else
                    "${project.rootDir.absolutePath}${File.separator}${openApiExtension.generatedFilesBaseDir}"
        ).apply { mkdirs() }
        val sourceOutputDir = File("$projectOutputDir/${sourceSet.name.decapitalize()}${File.separator}kotlin")
        val resourcesOutputDir = File("$projectOutputDir/${sourceSet.name.decapitalize()}${File.separator}resources")

        sourceOutputDir.deleteRecursively()

        // Add source set to the project Java source sets
        sourceSet.java.srcDir(sourceOutputDir.absolutePath)
        sourceSet.resources.srcDir(resourcesOutputDir.absolutePath)

        val srcSetName = if (sourceSet.name == "main") ""
        else sourceSet.name.capitalize()

        registerSwaggerUtilsTasks(project, resourcesOutputDir, srcSetName)

        val generateOpenApiTaskName = "generate${srcSetName}Web3jOpenAPI"

        val task: OpenApiGenerator = project.tasks.create(generateOpenApiTaskName, OpenApiGenerator::class.java)

        task.apply {
            group = "web3j"
            description = "Generates Web3j-OpenAPI project from Solidity contracts."
            generatedFilesBaseDir = sourceOutputDir.absolutePath
            addressLength = openApiExtension.addressBitLength
            contextPath = openApiExtension.openApi.contextPath
            packageName = openApiExtension.generatedPackageName.substringBefore(".wrappers")
            contractsAbi = getContractsData(openApiExtension.openApi.contractsAbis, sourceSet)
            contractsBin = getContractsData(openApiExtension.openApi.contractsBins, sourceSet)
            projectName = openApiExtension.openApi.projectName
            generateServer = openApiExtension.openApi.generateServer
        }

        val wrapperGenerationTask = project.tasks.getByName("generate${srcSetName}ContractWrappers") as SourceTask
        val compileKotlin = project.tasks.getByName("compile${srcSetName}Kotlin") as SourceTask
        val processResourcesTask = project.tasks.getByName("processResources")

        task.also {
            it.dependsOn(wrapperGenerationTask)
            it.mustRunAfter(wrapperGenerationTask)
            compileKotlin.dependsOn(it)
            processResourcesTask.mustRunAfter(wrapperGenerationTask)
        }
    }

    private fun getContractsData(dataList: List<String>, sourceSet: SourceSet): List<File> {
        return dataList.toMutableList().map { File(it) }.toMutableList().also {
            it.add(buildOutputDir(sourceSet))
        }
    }
}
