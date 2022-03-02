package net.yakclient.client.util.resource

import java.net.URI

internal class DownloadFailedException(resource: URI) : Exception("Download failed for resource: $resource")