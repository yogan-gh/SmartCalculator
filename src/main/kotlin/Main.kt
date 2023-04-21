package calculator

fun main() {
    val regCommand = "/.+".toRegex()
    while (true) {
        val consoleInput = readln().trim()
        when {
            consoleInput == "" -> continue
            regCommand.matches(consoleInput) -> when (consoleInput) {
                "/help" -> println("""
                    The program is designed to calculate complex expressions with large integer values and operations of addition, subtraction, division and multiplication. It also supports storing values in variables.
                    
                    Commands:
                    /help - show this message
                    /exit - exit the program
                    
                    Input example:
                    324 + (423 - 100) * 21
                    a = 20456
                    12 + a
                    b = a
                    b
                    a + b + 100
                    112234567890 * (100 - b)""".trimIndent())
                "/exit" -> {
                    println("Bye!")
                    break
                }
                else -> println("Unknown command")
            }
            else -> try {
                Calculation.input(consoleInput)
            }
            catch (ex: CalculateException) {
                println(ex.message)
            }
        }
    }
}