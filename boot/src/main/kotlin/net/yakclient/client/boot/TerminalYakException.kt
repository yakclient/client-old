package net.yakclient.client.boot

internal class TerminalYakException(
    message: String
) : Exception("YakClient quit unexpectedly because $message")