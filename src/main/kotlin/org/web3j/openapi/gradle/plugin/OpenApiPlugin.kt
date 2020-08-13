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

class OpenApiPlugin : Plugin<Project>, Web3jPlugin() {

    override fun apply(project: Project) {
        super.apply(project)
        registerPlugins(project)
        registerDependencies(project)
        registerRepositories(project)
        setProperties(project)

        val sourceSets: SourceSetContainer = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
        project.afterEvaluate { sourceSets.forEach { sourceSet -> openApiGenerationConfig(project, sourceSet) } }
    }

    private fun setProperties(project: Project) {
        project.setProperty("mainClassName", "org.web3j.openapi.server.console.RunServerCommand")
    }

    override fun registerExtensions(project: Project) {
        project.extensions.create(Web3jExtension.NAME, OpenApiExtension::class.java, project)
    }

    private fun registerPlugins(project: Project) {
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(KotlinPlatformJvmPlugin::class.java)
        project.pluginManager.apply(ApplicationPlugin::class.java)
        project.pluginManager.apply(ShadowPlugin::class.java)
        project.pluginManager.apply(SwaggerGeneratorPlugin::class.java)
        project.pluginManager.apply(SwaggerPlugin::class.java)
    }

    private fun registerRepositories(project: Project) {
        project.repositories.maven { mavenArtifactRepository -> mavenArtifactRepository.setUrl("https://dl.bintray.com/kotlin/kotlin-eap/") }
        project.repositories.mavenCentral()
    }

    private fun registerDependencies(project: Project) {
        project.dependencies.add("api", "org.web3j.openapi:web3j-openapi-server:0.0.3.1")
        project.dependencies.add("api", "org.web3j.openapi:web3j-openapi-core:0.0.3.1")
        project.dependencies.add("implementation", "io.swagger.core.v3:swagger-annotations:2.1.2")
        project.dependencies.add("implementation", "org.glassfish.jersey.media:jersey-media-json-jackson:2.31")
        project.dependencies.add("implementation", "org.glassfish.jersey.containers:jersey-container-servlet:2.31")
        project.dependencies.add("swaggerUI", "org.webjars:swagger-ui:3.10.0")
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
        val projectOutputDir: File = File("${openApiExtension.generatedFilesBaseDir}/kotlin").apply { mkdirs() }

        // Add source set to the project Java source sets
        sourceSet.java.srcDir(projectOutputDir.absolutePath)

        val srcSetName = if (sourceSet.name == "main") ""
        else sourceSet.name.capitalize()

        val generateOpenApiTaskName = "generate${srcSetName}Web3jOpenAPI"

        val task: OpenApiGenerator = project.tasks.create(generateOpenApiTaskName, OpenApiGenerator::class.java)

        task.apply {
            group = "web3j"
            description = "Generates Web3j-OpenAPI project from Solidity contracts."
            generatedFilesBaseDir = projectOutputDir.absolutePath
            addressLength = openApiExtension.addressBitLength
            contextPath = openApiExtension.openApi.contextPath
            packageName = openApiExtension.generatedPackageName.substringBefore(".wrappers")
            projectName = openApiExtension.openApi.projectName
            contractsAbi = getContractsData(openApiExtension.openApi.contractsAbis, project)
            contractsBin = getContractsData(openApiExtension.openApi.contractsBins, project)
            generateSwaggerUI = openApiExtension.openApi.generateSwaggerUI
        }

        val wrapperGenerationTask = project.tasks.getByName("generate${srcSetName}ContractWrappers") as SourceTask
        val compileKotlin = project.tasks.getByName("compile${srcSetName}Kotlin") as SourceTask

        task.also {
            it.dependsOn(wrapperGenerationTask)
            it.mustRunAfter(wrapperGenerationTask)
            compileKotlin.dependsOn(it)
        }
    }

    private fun getContractsData(dataList: List<String>, project: Project): List<File> {
        return dataList.toMutableList().map { File(it) }.toMutableList().also {
            it.add(File(Paths.get(project.buildDir.absolutePath, "resources", "main", "solidity").toString()))
        }
    }
}
