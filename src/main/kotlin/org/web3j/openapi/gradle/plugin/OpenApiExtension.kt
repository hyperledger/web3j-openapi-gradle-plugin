package org.web3j.openapi.gradle.plugin

import org.web3j.abi.datatypes.Address.DEFAULT_LENGTH
import java.lang.Byte

open class OpenApiExtension constructor(
		/** Generated OpenAPI project name.  */
		var projectName : String,
		/** Generated package name for Web3j-OpenAPI project.  */
		var generatedPackageName: String,

		/** Excluded contract names for Web3j-OpenApi generation.  */
		var excludedContracts: List<String>,

		/** Included contract names for Web3j-OpenApi generation.  */
		var includedContracts: List<String>,

		/** Contracts ABIs list. Can be folders or files. */
		var contractsAbis: List<String>,

		/** Contracts BINs list. Can be folders or files. */
		var contractsBins: List<String>,

		/** Bit length for network addresses.  */
		var addressBitLength: Int = DEFAULT_LENGTH / Byte.SIZE,

		/**
		 * Context path for the generated OpenAPI path.
		 *
		 * The resulting URIs path will be :
		 * <code> /{contextPath}/{contractName}/... </code>
		 */
		var contextPath: String,

		/**
		 * Output directory of the generated project.
		 * Default :
		 */
		var outputDir: String
){
	val DEFAULT_GENERATED_PACKAGE = "com.$projectName"

	init {
		if(generatedPackageName.isEmpty()) generatedPackageName = DEFAULT_GENERATED_PACKAGE
	}
	companion object {
		/** Extension name used in Gradle build files.  */
		val NAME = "openapi"
	}
}