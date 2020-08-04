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
    @Optional
    var generateSwaggerUI = true

    @Input
    lateinit var contextPath: String

    @TaskAction
    fun generateOpenApi() {
        val generatorConfiguration = GeneratorConfiguration(
                projectName,
                packageName,
                generatedFilesBaseDir,
                loadContractConfigurations(contractsAbi, contractsBin),
                addressLength,
                contextPath)

        // This is not generating the SwaggerUI
        val generateOpenApi = GenerateOpenApi(generatorConfiguration)
        generateOpenApi.run {
            generateCore()
            generateServer()
            generateGradleResources()
            if (generateSwaggerUI) generateSwaggerUI()
        }
    }
}
