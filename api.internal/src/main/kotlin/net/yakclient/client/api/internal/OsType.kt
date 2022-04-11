package net.yakclient.client.api.internal

import java.util.*


public enum class OsType {
    OS_X,
    WINDOWS,
    UNIX,
    UNKNOWN;

    public companion object {
        public fun get(): OsType {
            val os = System.getProperty("os.name").lowercase(Locale.ENGLISH)
            return when {
                os.contains("win") -> OsType.WINDOWS
                os.contains("mac") -> OsType.OS_X
                os.contains("solaris") || os.contains("sunos") -> OsType.UNIX
                os.contains("linux") -> OsType.UNIX
                os.contains("unix") -> OsType.UNIX
                else -> OsType.UNKNOWN
            }
        }
    }
}