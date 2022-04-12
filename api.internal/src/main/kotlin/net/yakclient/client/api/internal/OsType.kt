package net.yakclient.client.api.internal

import java.util.*


public enum class OsType {
    OS_X,
    WINDOWS,
    UNIX;

    public companion object {
        public val type: OsType = run {
            val os = System.getProperty("os.name").lowercase(Locale.ENGLISH)
            when {
                os.contains("win") -> WINDOWS
                os.contains("mac") -> OS_X
                os.contains("solaris") || os.contains("sunos") -> UNIX
                os.contains("linux") -> UNIX
                os.contains("unix") -> UNIX
                else -> throw IllegalStateException("Unsupported OS: $os")
            }
        }
    }
}