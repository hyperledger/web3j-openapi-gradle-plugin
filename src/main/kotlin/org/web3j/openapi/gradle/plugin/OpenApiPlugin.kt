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
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.web3j.gradle.plugin.Web3jExtension
import org.web3j.gradle.plugin.Web3jPlugin
import java.io.File
import java.nio.file.Paths

class OpenApiPlugin : Plugin<Project> {

    val generateOpenApiTaskName = "generateWeb3jOpenAPI"

    override fun apply(project: Project) {
        project.extensions.create(OpenApiExtension.NAME, OpenApiExtension::class.java, project)
        project.extensions.create(Web3jExtension.NAME, Web3jExtension::class.java, project)

        generateWrappersConfig(project)
        generateOpenApiConfig(project)
    }

    private fun generateOpenApiConfig(project: Project) {
        val openApiExtension = InvokerHelper.getProperty(project, OpenApiExtension.NAME) as OpenApiExtension

        if (openApiExtension.outputDir.isEmpty())
            openApiExtension.outputDir = Paths.get(
                    project.buildDir.absolutePath,
                    "generated",
                    "source",
                    "web3j",
                    "main"
            ).toString()

        val projectOutputDir: File = File(
                Paths.get(
                        openApiExtension.outputDir,
                        openApiExtension.projectName
                ).toString()
        ).apply { mkdirs() }

        val task: OpenApiGenerator = project.tasks.create(generateOpenApiTaskName, OpenApiGenerator::class.java)

        task.apply {
            group = Web3jExtension.NAME
            description = "Generates Web3j-OpenAPI project from Solidity ABIs and BINs."
            outputDir = projectOutputDir.absolutePath
            addressLength = openApiExtension.addressBitLength
            contextPath = openApiExtension.contextPath
            packageName = openApiExtension.generatedPackageName
            projectName = openApiExtension.projectName
            contractsAbi = getContractsData(openApiExtension.contractsAbis, project)
            contractsBin = getContractsData(openApiExtension.contractsBins, project)
        }

        val wrapperGenerationTask = project.tasks.getByName("generateContractWrappers") as DefaultTask
        task.also {
            it.dependsOn(wrapperGenerationTask)
            it.mustRunAfter(wrapperGenerationTask)
        }
    }

    private fun getContractsData(dataList: List<String>, project: Project): List<File> {
        return dataList.toMutableList().map { File(it) }.toMutableList().also {
            it.add(File(Paths.get(project.buildDir.absolutePath, "main", "solidity").toString()))
        }
    }

    private fun generateWrappersConfig(project: Project) {
        project.pluginManager.apply(Web3jPlugin::class.java)

        val openApiExtension = InvokerHelper.getProperty(project, OpenApiExtension.NAME) as OpenApiExtension
        val web3jExtension = InvokerHelper.getProperty(project, Web3jExtension.NAME) as Web3jExtension

        web3jExtension.apply {
            generatedPackageName = openApiExtension.generatedPackageName
            generatedFilesBaseDir = Paths.get(
                    openApiExtension.outputDir,
                    openApiExtension.projectName,
                    "server",
                    "src",
                    "main",
                    "java"
            ).toString()
            excludedContracts = openApiExtension.excludedContracts
            includedContracts = openApiExtension.includedContracts
            addressBitLength = openApiExtension.addressBitLength
        }
    }
}
