package net.yakclient.client.boot.maven

public class InvalidMavenLayoutException(resource: String, layout: String) : Exception("Given resource '$resource' was not found in layout: '$layout'") {
}