package net.yakclient.client.boot.test

import kotlinx.coroutines.runBlocking
import net.yakclient.common.util.copyTo
import net.yakclient.common.util.readBytes
import net.yakclient.common.util.readInputStream
import net.yakclient.common.util.toResource
import org.apache.maven.model.Dependency
import org.apache.maven.model.Parent
import org.apache.maven.model.Repository
import org.apache.maven.model.building.DefaultModelBuilderFactory
import org.apache.maven.model.building.DefaultModelBuildingRequest
import org.apache.maven.model.building.FileModelSource
import org.apache.maven.model.building.ModelSource
import org.apache.maven.model.resolution.ModelResolver
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.nio.file.Files
import java.util.*
import kotlin.test.Test

class ModelBuilderTests {
    @Test
    fun `Test model building`() {
//


        fun descriptorToFile(group: String, artifact: String, version: String): File {
            val tempFile = Files.createTempFile("$artifact-$version", "pom")

            val s = "https://repo1.maven.org/maven2/${group.replace(".", "/")}/$artifact/$version/$artifact-$version"
            val resource =
                URI.create("$s.pom")
                    .toResource(
                        HexFormat.of().parseHex(
                            String(
                                URI.create("$s.pom.sha1")
                                    .readBytes()
                            )
                        )
                    )

            runBlocking { resource copyTo tempFile }

            return tempFile.toFile()
        }


        val builder = DefaultModelBuilderFactory().newInstance()

        val req = DefaultModelBuildingRequest()

        req.isProcessPlugins = true
        req.systemProperties = System.getProperties()
        req.isTwoPhaseBuilding = false
        req.pomFile = descriptorToFile("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.12.6")
        req.modelResolver = object : ModelResolver {
            override fun resolveModel(groupId: String, artifactId: String, version: String): ModelSource {
                return FileModelSource(descriptorToFile(groupId, artifactId, version))
            }

            override fun resolveModel(parent: Parent): ModelSource {
                return resolveModel(parent.groupId, parent.artifactId, parent.version)
            }

            override fun resolveModel(dependency: Dependency): ModelSource {
                return resolveModel(dependency.groupId, dependency.artifactId, dependency.version)
            }

            override fun addRepository(repository: Repository?) {

            }

            override fun addRepository(repository: Repository?, replace: Boolean) {
            }

            override fun newCopy(): ModelResolver {
                return this
            }
        }

        val result = builder.build(req)

        println("Done")
    }
}