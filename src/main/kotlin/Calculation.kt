package calculator

import java.math.BigInteger

class CalculateException(private val showMessage: String) : Exception() {
    override val message: String
        get() = showMessage
}
fun String.toPostfixExpression(): MutableList<Any> = Calculation.getPostfix(Calculation.splitExpression(this))
fun String.toOperator(): Char {
    var operator = '+'
    for (symbol in this) {
        operator = when {
            operator == '+' && symbol == '-' -> '-'
            operator == '-' && symbol == '+' -> '-'
            else -> '+'
        }
    }
    return operator
}
fun String.isAssignment() = ".*=.*".toRegex().matches(this)
fun String.isVariable() = "[a-zA-Z]+".toRegex().matches(this)
fun String.isNumber() = "[0-9-]+".toRegex().matches(this)
fun String.isExpression(): Boolean {
    if ("[0-9a-zA-Z)-][-*0-9a-zA-Z)(+ \\/]+[0-9a-zA-Z)]".toRegex().matches(this)) {
        val brackets = this.filter { it == ')' || it == '(' }
        var index = 0
        brackets.forEach {
            when (it) {
                '(' -> index += 1
                ')' -> index -= 1
            }
            if (index < 0) return false
        }
        return index == 0
    }
    else return false
}

object Calculation {
    private val variableList = mutableMapOf<String, BigInteger>()
    fun input(str: String) {
        var output: String? = null
        when {
            str.isAssignment() -> {
                val leftPath = str.split("=".toRegex(), 2)[0].trim()
                val rightPath = str.split("=".toRegex(), 2)[1].trim()
                if (!leftPath.isVariable())
                    throw CalculateException("Invalid identifier")
                when {
                    rightPath.isVariable() -> {
                        addVariable(leftPath, getVar(rightPath))
                    }
                    rightPath.isNumber() -> {
                        addVariable(leftPath, rightPath.toBigInteger())
                    }
                    else -> throw CalculateException("Invalid assignment")
                }
            }
            str.isVariable() -> {
                output = getVar(str).toString()
            }
            str.isExpression() -> {
                output = computePostfixExpression(str.toPostfixExpression()).toString()
            }
            else -> throw CalculateException("Invalid identifier")
        }
        if (output != null)
            println(output)
    }
    fun getVar (name: String): BigInteger {
        if (!name.isVariable()) throw CalculateException("Invalid identifier")
        return variableList[name]?:throw CalculateException("Unknown variable")
    }
    fun addVariable(name: String, value: BigInteger) {
        variableList += name to value
    }
    fun getPostfix(expression: MutableList<Any>): MutableList<Any> {
        val priority = mapOf(
            '*' to 3,
            '/' to 3,
            '+' to 2,
            '-' to 2,
            '(' to 1)
        val output = mutableListOf<Any>()
        val stack = ArrayDeque<Any>()
        for (unit in expression) {
            when (unit) {
                is BigInteger -> output.add(unit)
                '(' -> stack.addFirst(unit)
                ')' -> {
                    while (true) {
                        val stackUnit = stack.firstOrNull()?:break
                        if (stackUnit == '(') {
                            stack.remove(stackUnit)
                            break
                        }
                        else {
                            output.add(stackUnit)
                            stack.remove(stackUnit)
                        }
                    }
                }
                '*', '/', '+', '-' -> {
                    if (stack.isEmpty())
                        stack.add(unit)
                    else {
                        while (true) {
                            val stackUnit = stack.firstOrNull()?:break
                            if ((priority[unit] ?: 0) <= (priority[stackUnit] ?: 0)) {
                                output.add(stackUnit)
                                stack.remove(stackUnit)
                            }
                            else break
                        }
                        stack.addFirst(unit)
                    }
                }
            }
        }
        while (true) {
            val stackUnit = stack.firstOrNull()?:break
            output.add(stackUnit)
            stack.remove(stackUnit)
        }
        return output
    }
    fun splitExpression(str: String): MutableList<Any> {
        val innerList = mutableListOf<Any>()
        val innerStr = (if (str.startsWith("-")) "0" else "") + str.replace(" ", "")
        var caption = ""
        for (symbol in innerStr) {
            if (symbol.isLetterOrDigit()) {
                caption += symbol
            }
            else {
                if (caption.isNotEmpty()) {
                    innerList.add(if (caption.isNumber())
                        caption.toBigInteger()
                    else
                        getVar(caption))
                    caption = ""
                }
                when (symbol) {
                    ')', '(' -> innerList.add(symbol)
                    '+' -> when (innerList.last()) {
                        '-','+' -> continue
                        else -> innerList.add(symbol)
                    }
                    '-' -> when (innerList.last()) {
                        '(' -> {
                            innerList.add(0)
                            innerList.add(symbol)
                        }
                        '-' -> {
                            innerList[innerList.lastIndex] = '+'
                        }
                        '+' -> {
                            innerList[innerList.lastIndex] = '-'
                        }
                        else -> innerList.add(symbol)
                    }
                    '*', '/' -> when (innerList.last()) {
                        ')', is BigInteger -> innerList.add(symbol)
                        else -> throw CalculateException("Invalid expression")
                    }
                    else -> throw CalculateException("Invalid expression")
                }
            }
        }
        if (caption.isNotEmpty())
            innerList.add(if (caption.isNumber())
                caption.toBigInteger()
            else
                getVar(caption))
        return innerList
    }
    fun computePostfixExpression(postfixExpression: MutableList<Any>): BigInteger {
        val listOperation = listOf('*', '/', '-', '+')
        while (postfixExpression.size != 1) {
            val index = postfixExpression.indices.find { postfixExpression[it] in listOperation }?: throw CalculateException("Invalid expression")
            val operandFirst = postfixExpression[index - 2].toString().toBigInteger()
            val operandSecond = postfixExpression[index - 1].toString().toBigInteger()
            postfixExpression[index] = when(postfixExpression[index]) {
                '+' -> operandFirst + operandSecond
                '-' -> operandFirst - operandSecond
                '/' -> operandFirst / operandSecond
                '*' -> operandFirst * operandSecond
                else -> throw CalculateException("Invalid expression")
            }
            postfixExpression.removeAt(index - 1)
            postfixExpression.removeAt(index - 2)
        }
        return postfixExpression.first().toString().toBigInteger()
    }
}