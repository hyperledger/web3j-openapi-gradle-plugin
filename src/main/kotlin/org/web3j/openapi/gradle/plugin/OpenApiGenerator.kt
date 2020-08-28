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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.web3j.abi.datatypes.Address
import org.web3j.openapi.codegen.GenerateOpenApi
import org.web3j.openapi.codegen.config.GeneratorConfiguration
import org.web3j.openapi.codegen.utils.GeneratorUtils.loadContractConfigurations
import java.io.File
import java.lang.Byte.SIZE
import javax.inject.Inject

open class OpenApiGenerator @Inject constructor() : DefaultTask() {

    @Input
    lateinit var projectName: String

    @Input
    var contractsBin: List<File> = emptyList()

    @Input
    var contractsAbi: List<File> = emptyList()

    @Input
    lateinit var generatedFilesBaseDir: String

    @Input
    lateinit var packageName: String

    @Input
    @Optional
    var addressLength = Address.DEFAULT_LENGTH / SIZE

    @Input
    lateinit var contextPath: String

    @TaskAction
    fun generateOpenApi() {
        val contractsConfig = loadContractConfigurations(contractsAbi, contractsBin)
        if (contractsConfig.isNullOrEmpty()) return

        val generatorConfiguration = GeneratorConfiguration(
            projectName = projectName,
            packageName = packageName,
            outputDir = generatedFilesBaseDir,
            contracts = contractsConfig,
            addressLength = addressLength,
            contextPath = contextPath,
            withSwaggerUi = false,
            withWrappers = false,
            withGradleResources = false,
            withCoreBuildFile = false,
            withServerBuildFile = false
        )

        val generateOpenApi = GenerateOpenApi(generatorConfiguration)
        generateOpenApi.run {
            generate()
        }
        println("Web3j-OpenAPI generated successfully in : $generatedFilesBaseDir")
    }
}
