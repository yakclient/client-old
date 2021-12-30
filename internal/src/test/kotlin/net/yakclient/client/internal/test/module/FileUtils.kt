package net.yakclient.client.internal.test.module

import java.io.File

tailrec fun File.parent(name: String): File = if (this.name == name) this else this.parentFile.parent(name)

fun File.child(vararg names: String): File =
    names.fold(this) { acc, it -> acc.listFiles().first { file -> file.name == it } }

fun workingDir(): File = File(System.getProperty("user.dir"))