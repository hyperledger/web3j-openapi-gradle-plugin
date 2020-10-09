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
import io.swagger.v3.plugins.gradle.tasks.ResolveTask
import org.codehaus.groovy.runtime.InvokerHelper
import org.gradle.api.Project
import org.gradle.api.internal.FactoryNamedDomainObjectContainer
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.hidetake.gradle.swagger.generator.SwaggerGeneratorPlugin
import org.hidetake.gradle.swagger.generator.SwaggerSource
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
        project.afterEvaluate { sourceSets.forEach { sourceSet -> configure(project, sourceSet) } }
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
    private fun configure(project: Project, sourceSet: SourceSet) {
        val basePath = Paths.get(project.openApiExtension.generatedFilesBaseDir)
        val projectOutputDir = if (basePath.isAbsolute) {
            project.openApiExtension.generatedFilesBaseDir
        } else {
            project.rootDir.toPath().resolve(basePath).toAbsolutePath().toString()
        }

        val sourceOutputDir = Paths.get(projectOutputDir, sourceSet.name, "kotlin").toFile()
        val resourcesOutputDir = Paths.get(projectOutputDir, sourceSet.name, "resources").toFile()

        // Add source set to the project Java source sets
        sourceSet.java.srcDir(sourceOutputDir.absolutePath)
        sourceSet.resources.srcDir(resourcesOutputDir.absolutePath)

        val generateOpenApiTask = buildOpenApiTask(project, sourceSet, sourceOutputDir)
        if (sourceSet.name == "main") configureSwaggerUi(project, sourceSet, resourcesOutputDir, generateOpenApiTask)
    }

    private fun buildOpenApiTask(
        project: Project,
        sourceSet: SourceSet,
        sourceOutputDir: File
    ): TaskProvider<GenerateOpenApi> {
        val sourceSetName = sourceSet.displayName.capitalize()
        val wrapperGeneration = project.tasks.named("generate${sourceSetName}ContractWrappers")
        val compileKotlin = project.tasks.named("compile${sourceSetName}Kotlin")
        val processResources = project.tasks.named("processResources")

        val generateOpenApiTask =
            project.tasks.register("generate${sourceSetName}Web3jOpenApi", GenerateOpenApi::class.java) {
                it.dependsOn(wrapperGeneration)
                it.group = Web3jExtension.NAME
                it.description = "Generates${
                    if (sourceSet.displayName.isNotEmpty())
                        " ${sourceSet.displayName}"
                    else ""
                } Web3j-OpenAPI classes from Solidity contracts."
                it.source = buildSourceDirectorySet(project, sourceSet)
                it.outputs.dir(sourceOutputDir)
                with(project.openApiExtension) {
                    it.addressLength = addressBitLength
                    it.contextPath = openApi.contextPath
                    it.packageName = packageName
                    it.includedContracts = includedContracts
                    it.excludedContracts = excludedContracts
                    it.projectName = openApi.projectName
                    it.generateServer = openApi.generateServer
                }
            }
        compileKotlin.configure {
            it.dependsOn(generateOpenApiTask)
        }
        processResources.configure {
            it.mustRunAfter(wrapperGeneration)
        }
        return generateOpenApiTask
    }

    private fun configureSwaggerUi(
        project: Project,
        sourceSet: SourceSet,
        resourcesOutputDir: File,
        generateOpenApiTask: TaskProvider<GenerateOpenApi>
    ) {
        project.tasks.register(
            "generateWeb3jSwaggerUi",
            GenerateSwaggerUi::class.java,
            resourcesOutputDir.absolutePath
        ).configure {
            it.group = Web3jExtension.NAME
            it.description = "Generates Web3j-OpenAPI Swagger UI from Solidity contracts."
            it.dependsOn(generateOpenApiTask)
        }

        val outputDir = File("build/resources/openapi/main")

        @Suppress("UNCHECKED_CAST")
        // Configure Swagger sources from SwaggerGeneratorPlugin
        (project.extensions.findByName("swaggerSources") as FactoryNamedDomainObjectContainer<SwaggerSource>).apply {
            add(SwaggerSource("openapi"))
            getByName("openapi").setInputFile(File(outputDir, "openapi.json"))
        }

        // Configure resolve task from SwaggerPlugin
        project.tasks.named("resolve", ResolveTask::class.java).configure {
            it.resourcePackages = setOf(project.openApiExtension.packageName)
            it.classpath = sourceSet.runtimeClasspath
            it.outputDir = outputDir
        }
    }

    private val Project.openApiExtension: OpenApiExtension
        get() = InvokerHelper.getProperty(project, Web3jExtension.NAME) as OpenApiExtension

    private val OpenApiExtension.packageName: String
        get() = generatedPackageName.substringBefore(".wrappers")

    private val SourceSet.displayName: String
        get() = if (name == "main") "" else name
}
