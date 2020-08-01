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

class OpenApiGenerator @Inject constructor() : DefaultTask() {

	@Input
	lateinit var projectName: String

	@Input
	var contractsBin: List<File> = emptyList()

	@Input
	var contractsAbi: List<File> = emptyList()

	@Input
	lateinit var outputDir: String

	@Input
	lateinit var packageName: String

	@Input
	@Optional
	var addressLength = Address.DEFAULT_LENGTH / SIZE

	@Input
	@Optional
	lateinit var contextPath: String

	@TaskAction
	fun generateOpenApi() {
		if (contextPath.isEmpty()) contextPath = projectName

		val generatorConfiguration = GeneratorConfiguration(
				projectName,
				packageName,
				outputDir,
				loadContractConfigurations(contractsAbi, contractsBin),
				addressLength,
				contextPath)
		// This is not generating the SwaggerUI
		val generateOpenApi = GenerateOpenApi(generatorConfiguration)
		generateOpenApi.run {
			generateCore()
			generateServer()
			generateGradleResources()
		}
	}
}