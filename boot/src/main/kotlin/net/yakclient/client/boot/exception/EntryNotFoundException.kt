package net.yakclient.client.boot.exception

import net.yakclient.client.boot.archive.ArchiveHandle

public class EntryNotFoundException(archive: ArchiveHandle, name: String) : Exception("Entry $name in archive $archive does not exist!") {
}