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

import org.gradle.api.Project
import org.web3j.abi.datatypes.Address.DEFAULT_LENGTH

open class OpenApiExtension(
    private val project: Project
) {
    companion object {
        /** Extension name used in Gradle build files.  */
        val NAME = "openapi"
    }

    /** Generated OpenAPI project name.  */
    var projectName: String = ""
        set(value) {
            field = if (value.isEmpty()) "${project.rootProject.name}Api"
            else value
        }

    /** Generated package name for Web3j-OpenAPI project.  */
    var generatedPackageName: String = ""
        set(value) {
            field = if (value.isEmpty()) "${project.group.toString()}.openapi"
            else value.removeSuffix(".{0}")
        }

    /** Excluded contract names for Web3j-OpenApi generation.  */
    var excludedContracts: List<String> = emptyList()

    /** Included contract names for Web3j-OpenApi generation.  */
    var includedContracts: List<String> = emptyList()

    /** Contracts ABIs list. Can be folders or files.  */
    var contractsAbis: List<String> = emptyList()

    /** Contracts BINs list. Can be folders or files.  */
    var contractsBins: List<String> = emptyList()

    /** Bit length for network addresses.  */
    var addressBitLength = DEFAULT_LENGTH / java.lang.Byte.SIZE

    /**
	 * Context path for the generated OpenAPI path.
	 *
	 * The resulting URIs path will be :
	 * <code> /{contextPath}/{contractName}/... </code>
	 */
    var contextPath: String = ""
        set(value) {
            field = if (field.isEmpty()) projectName
            else value.removePrefix("/")
        }

    /**
	 * Output directory of the generated project.
	 * Default : build/generated/source/web3j/main
	 */
    var generatedFilesBaseDir: String = ""

    /**
     * Checks whether to generate the SwaggerUI for the generated project.
     * Set to false not to generate it.
     */
    var generateSwaggerUI: Boolean = true
}
