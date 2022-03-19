package net.yakclient.client.boot.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.yakclient.client.boot.maven.*
import net.yakclient.client.boot.repository.RepositorySettings
import net.yakclient.client.util.resource.SafeResource

//public class MavenSchemaRepositoryHandler(
//    settings: RepositorySettings,
//    private val schema: MavenSchema
//) : MavenRepositoryHandler(settings) {
//
////    override fun newestVersionOf(group: String, artifact: String): MavenDescriptor? {
//////        val context = schema.contextHandle
//////        if (!context.supply(MavenArtifactContext(group, artifact))) return null
//////
//////        val meta by context[schema.meta]
//////
//////        val tree = xml.readValue<Map<String, Any>>(meta.open())
//////
//////        val version = (tree["version"] as? String)
//////            ?: (tree["versioning"] as Map<String, String>)["release"]
//////            ?: return null
//////
//////        return MavenDescriptor(group, artifact, version)
////    }
//
//    override fun pomOf(desc: MavenDescriptor): SafeResource? = schema.contextHandle.takeIf {
//        it.supply(
//            MavenVersionContext(
//                desc.group,
//                desc.artifact,
//                desc.version ?: return@takeIf false
//            )
//        )
//    }?.getValue(schema.pom)
//
//    override fun jarOf(desc: MavenDescriptor): SafeResource? = schema.contextHandle.takeIf {
//        it.supply(
//            MavenVersionContext(
//                desc.group,
//                desc.artifact,
//                desc.version ?: return@takeIf false
//            )
//        )
//    }?.getValue(schema.jar)
//}