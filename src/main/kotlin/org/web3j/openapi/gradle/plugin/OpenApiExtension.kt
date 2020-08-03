package org.web3j.openapi.gradle.plugin

import org.gradle.api.Project
import org.web3j.abi.datatypes.Address.DEFAULT_LENGTH

open class OpenApiExtension (
		private val project: Project
) {
	companion object {
		/** Extension name used in Gradle build files.  */
		val NAME = "openapi"
	}

	/** Generated OpenAPI project name.  */
	var projectName : String = ""

	val DEFAULT_GENERATED_PACKAGE = "com.$projectName"

	/** Generated package name for Web3j-OpenAPI project.  */
	var generatedPackageName: String = ""
//		set(value) {
//			field = if (value.isEmpty()) project.group.toString()
//			else value
//		}

	/** Base directory for generated Web3j-OpenApi project.  */
	var generatedFilesBaseDir: String = ""

	/** Excluded contract names for Web3j-OpenApi generation.  */
	var excludedContracts: List<String> = emptyList()

	/** Included contract names for Web3j-OpenApi generation.  */
	var includedContracts: List<String> = emptyList()

	/** Contracts ABIs list. Can be folders or files. */
	var contractsAbis: List<String> = emptyList()

	/** Contracts BINs list. Can be folders or files. */
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

	/**
	 * Output directory of the generated project.
	 * Default :
	 */
	var outputDir: String = ""
}