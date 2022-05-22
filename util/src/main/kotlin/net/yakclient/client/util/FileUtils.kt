package net.yakclient.client.util

import java.io.File

public tailrec fun File.parent(name: String): File = if (this.name == name) this else this.parentFile.parent(name)

public fun File.child(vararg names: String): File =
    names.fold(this) { acc, it -> (acc.listFiles() ?: arrayOf()).first { file -> file.name == it } }

public fun File.first() : File = (listFiles() ?: arrayOf()).first()

public fun workingDir(): File = File(System.getProperty("user.dir"))