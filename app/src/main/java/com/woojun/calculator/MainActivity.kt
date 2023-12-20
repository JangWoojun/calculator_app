package com.woojun.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.woojun.calculator.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Stack

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setNumberButtonListeners()
        setOperatorButtonListeners()
    }

    private fun setNumberButtonListeners() {
        binding.apply {
            val numberButtons = listOf(
                button0, button00, button1, button2, button3,
                button4, button5, button6, button7, button8, button9, buttonDote
            )

            val number = listOf(
                "0", "00", "1", "2", "3", "4", "5", "6", "7", "8", "9", "."
            )


            numberButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    formulaText.append(number[index])
                }
            }

            buttonAllClear.setOnClickListener {
                formulaText.text = ""
                resultText.text = ""
            }

            buttonBracket.setOnClickListener {
                val formula = binding.formulaText.text.toString()
                val openBrackets = formula.count { it == '(' }
                val closeBrackets = formula.count { it == ')' }

                if (openBrackets > closeBrackets) {
                    binding.formulaText.append(")")
                } else {
                    binding.formulaText.append("(")
                }
            }

            buttonRemove.setOnClickListener {
                formulaText.text = formulaText.text.substring(0, formulaText.text.lastIndex)
            }
        }
    }

    private fun setOperatorButtonListeners() {
        binding.apply {
            val operatorButtons = listOf(buttonPlus, buttonMinus, buttonMultiply, buttonDivide)
            val operator = listOf("+", "-", "*", "/",)

            operatorButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    formulaText.append(operator[index])
                }
            }

            buttonEquilar.setOnClickListener {
                val result = evaluate(formulaText.text.toString())
                resultText.text = result
            }
        }
    }


    private fun evaluate(expression: String): String {
        return try {
            val postfix = infixToPostfix(expression)
            val result = calculatePostfix(postfix)
            result.toString()
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun infixToPostfix(expression: String): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<Char>()
        var numberBuffer = StringBuilder()

        fun flushNumberBuffer() {
            if (numberBuffer.isNotEmpty()) {
                output.add(numberBuffer.toString())
                numberBuffer = StringBuilder()
            }
        }

        for (char in expression) {
            when {
                char.isDigit() || char == '.' -> numberBuffer.append(char)
                char in "+-*/" -> {
                    flushNumberBuffer()
                    while (stack.isNotEmpty() && getPrecedence(char) <= getPrecedence(stack.peek())) {
                        output.add(stack.pop().toString())
                    }
                    stack.push(char)
                }
                char == '(' -> {
                    flushNumberBuffer()
                    stack.push(char)
                }
                char == ')' -> {
                    flushNumberBuffer()
                    while (stack.isNotEmpty() && stack.peek() != '(') {
                        output.add(stack.pop().toString())
                    }
                    if (stack.isNotEmpty() && stack.peek() == '(') {
                        stack.pop()
                    } else {
                        throw IllegalArgumentException("Invalid expression")
                    }
                }
            }
        }
        flushNumberBuffer()
        while (stack.isNotEmpty()) {
            output.add(stack.pop().toString())
        }
        return output
    }

    private fun calculatePostfix(postfix: List<String>): BigDecimal {
        val stack = Stack<BigDecimal>()

        for (token in postfix) {
            when {
                isNumeric(token) -> stack.push(BigDecimal(token))
                token.length == 1 && token[0] in "+-*/" -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    val result = when (token[0]) {
                        '+' -> left.add(right)
                        '-' -> left.subtract(right)
                        '*' -> left.multiply(right)
                        '/' -> left.divide(right, 8, BigDecimal.ROUND_HALF_EVEN) // 8자리 소수점, 반올림
                        else -> throw IllegalArgumentException("Unknown operator")
                    }
                    stack.push(result)
                }
                else -> throw IllegalArgumentException("Invalid token")
            }
        }

        return if (stack.size == 1) stack.pop() else throw IllegalArgumentException("Invalid expression")
    }

    private fun isNumeric(str: String): Boolean {
        return try {
            BigDecimal(str)
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun getPrecedence(operator: Char): Int {
        return when (operator) {
            '+', '-' -> 1
            '*', '/' -> 2
            else -> -1
        }
    }
}
