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
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
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
            register(
                taskNameCreator(SwaggerUiMover.BASE_TASK_NAME, SwaggerUiMover.TRAILING_TASK_NAME, sourceSetName),
                SwaggerUiMover::class.java,
                resourcesOutputDir.absolutePath
            )
            register(
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
            add("api", "org.web3j.openapi:web3j-openapi-server:$projectVersion")
            add("api", "org.web3j.openapi:web3j-openapi-core:$projectVersion")
            add("implementation", "io.swagger.core.v3:swagger-annotations:2.1.2")
            add("implementation", "org.glassfish.jersey.media:jersey-media-json-jackson:2.32")
            add("implementation", "org.glassfish.jersey.containers:jersey-container-servlet:2.32")
            add("implementation", "io.github.microutils:kotlin-logging:1.7.10")
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

        val basePath = Paths.get(openApiExtension.generatedFilesBaseDir)
        val projectOutputDir = if (basePath.isAbsolute) {
            openApiExtension.generatedFilesBaseDir
        } else {
            project.rootDir.toPath().resolve(basePath).toAbsolutePath().toString()
        }

        val sourceOutputDir = Paths.get(projectOutputDir, sourceSet.name, "kotlin").toFile()
        val resourcesOutputDir = Paths.get(projectOutputDir, sourceSet.name, "resources").toFile()

        // Add source set to the project Java source sets
        sourceSet.java.srcDir(sourceOutputDir.absolutePath)
        sourceSet.resources.srcDir(resourcesOutputDir.absolutePath)

        val srcSetName = if (sourceSet.name == "main") ""
        else sourceSet.name.capitalize()

        registerSwaggerUtilsTasks(project, resourcesOutputDir, srcSetName)

        val wrapperGenerationTask = project.tasks.named("generate${srcSetName}ContractWrappers")
        val compileKotlin = project.tasks.named("compile${srcSetName}Kotlin")
        val processResourcesTask = project.tasks.named("processResources")

        val generateOpenApiTaskName = "generate${srcSetName}Web3jOpenApi"
        val taskProvider = project.tasks.register(generateOpenApiTaskName, GenerateOpenApi::class.java) {
            it.group = Web3jExtension.NAME
            it.description = "Generates Web3j-OpenAPI project from Solidity contracts."
            it.source = buildSourceDirectorySet(project, sourceSet)
            it.outputs.dir(sourceOutputDir)
            it.addressLength = openApiExtension.addressBitLength
            it.contextPath = openApiExtension.openApi.contextPath
            it.packageName = openApiExtension.generatedPackageName.substringBefore(".wrappers")
            it.includedContracts = openApiExtension.includedContracts
            it.excludedContracts = openApiExtension.excludedContracts
            it.projectName = openApiExtension.openApi.projectName
            it.generateServer = openApiExtension.openApi.generateServer
            it.dependsOn(wrapperGenerationTask)
        }
        compileKotlin.configure {
            it.dependsOn(taskProvider)
        }
        processResourcesTask.configure {
            it.mustRunAfter(wrapperGenerationTask)
        }
    }
}
