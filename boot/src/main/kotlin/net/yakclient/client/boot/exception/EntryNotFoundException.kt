package net.yakclient.client.boot.exception

import net.yakclient.client.boot.archive.ArchiveReference

public class EntryNotFoundException(archive: ArchiveReference, name: String) : Exception("Entry $name in archive $archive does not exist!") {
}