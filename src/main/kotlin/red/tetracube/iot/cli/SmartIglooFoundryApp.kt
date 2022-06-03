package red.tetracube.iot.cli

import picocli.CommandLine

class SmartIglooFoundryApp {
}

fun main(args: Array<String>) {
 CommandLine(SmartIglooFoundryCLI()).execute(*args)
}
