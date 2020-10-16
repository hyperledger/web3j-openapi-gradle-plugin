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

import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

import org.web3j.abi.datatypes.Address
import org.web3j.openapi.codegen.OpenApiGenerator
import org.web3j.openapi.codegen.config.ContractConfiguration
import org.web3j.openapi.codegen.config.GeneratorConfiguration
import org.web3j.openapi.codegen.utils.GeneratorUtils.loadContractConfigurations
import javax.inject.Inject

@CacheableTask
open class GenerateOpenApi @Inject constructor() : SourceTask() {

    @Input
    lateinit var projectName: String

    @Input
    lateinit var packageName: String

    @Input
    var addressLength = Address.DEFAULT_LENGTH / Byte.SIZE_BITS

    @Input
    @Optional
    lateinit var contextPath: String

    @Input
    var generateServer = true

    @Input
    @Optional
    var includedContracts: List<String> = emptyList()

    @Input
    @Optional
    var excludedContracts: List<String> = emptyList()

    @TaskAction
    fun generateOpenApi() {
        val contractsConfig = loadContractConfigurations(source.files.toList(), emptyList())

        if (contractsConfig.isNullOrEmpty()) return

        val generatorConfiguration = GeneratorConfiguration(
            projectName = projectName,
            packageName = packageName,
            outputDir = outputs.files.singleFile.absolutePath,
            contracts = excludeContracts(includeContracts(contractsConfig)),
            contextPath = contextPath,
            withImplementations = generateServer
        )
        OpenApiGenerator(generatorConfiguration).generate()
    }

    private fun excludeContracts(contracts: List<ContractConfiguration>): List<ContractConfiguration> {
        return if (excludedContracts.isEmpty()) {
            contracts
        } else {
            contracts.filter {
                !excludedContracts.contains(it.contractDetails.contractName)
            }
        }
    }

    private fun includeContracts(contracts: List<ContractConfiguration>): List<ContractConfiguration> {
        return if (includedContracts.isEmpty()) {
            contracts
        } else {
            contracts.filter {
                includedContracts.contains(it.contractDetails.contractName)
            }
        }
    }
}
