package com.example.myfirstapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private var isResult = false // Flag to track if the current display is a calculated result

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tvDisplay)

        // Digit Buttons
        val digits = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnDot
        )
        digits.forEach { id ->
            findViewById<Button>(id).setOnClickListener { onDigitClick((it as Button).text.toString()) }
        }

        // Operator Buttons
        val ops = mapOf(
            R.id.btnAdd to "+", R.id.btnSub to "-", R.id.btnMul to "×", R.id.btnDiv to "÷"
        )
        ops.forEach { (id, op) ->
            findViewById<Button>(id).setOnClickListener { onOperatorClick(op) }
        }

        // Functional Buttons
        findViewById<Button>(R.id.btnEqual).setOnClickListener { onEqualClick() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { onClearClick() }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener { onBackspaceClick() }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { onPercentClick() }
    }

    private fun onDigitClick(digit: String) {
        if (tvDisplay.text.toString() == "Error") onClearClick()

        // If we recently calculated a result, a new number starts a fresh calculation
        if (isResult) {
            tvDisplay.text = ""
            isResult = false
        }

        val currentText = tvDisplay.text.toString()

        // Prevent multiple dots in the currently typed number
        if (digit == ".") {
            // Split by space to inspect only the last number segment
            val parts = currentText.split(" ")
            val lastNumber = parts.lastOrNull() ?: ""
            if (lastNumber.contains(".")) return
        }

        if (currentText == "0" && digit != ".") {
            tvDisplay.text = digit
        } else {
            tvDisplay.append(digit)
        }
    }

    private fun onOperatorClick(op: String) {
        if (tvDisplay.text.toString() == "Error") return

        // If we have a result, we continue the expression using that result
        if (isResult) {
            isResult = false
        }

        val currentText = tvDisplay.text.toString()
        if (currentText.isEmpty()) return

        // Check if the input ends with an existing operator (e.g. "5 + ")
        if (currentText.endsWith(" ")) {
            // Replace the old operator with the new one
            val trimmed = currentText.trimEnd() // "5 +"
            val spaceIndex = trimmed.lastIndexOf(' ')
            if (spaceIndex != -1) {
                val base = trimmed.substring(0, spaceIndex)
                tvDisplay.text = "$base $op "
            }
        } else {
            // Append new operator
            tvDisplay.append(" $op ")
        }
    }

    private fun onEqualClick() {
        if (tvDisplay.text.toString() == "Error") return
        
        val expression = tvDisplay.text.toString()
        if (expression.isEmpty()) return

        try {
            val result = evaluateExpression(expression)
            tvDisplay.text = formatResult(result)
            isResult = true
        } catch (e: Exception) {
            tvDisplay.text = "Error"
            isResult = true
        }
    }

    private fun onClearClick() {
        tvDisplay.text = "0"
        isResult = false
    }

    private fun onBackspaceClick() {
        if (isResult || tvDisplay.text.toString() == "Error") {
            onClearClick()
            return
        }

        val text = tvDisplay.text.toString()
        if (text.isEmpty() || text == "0") return

        // If deleting an operator (which spans 3 chars " + "), remove the whole block
        if (text.endsWith(" ")) {
            if (text.length >= 3) {
                tvDisplay.text = text.dropLast(3)
            } else {
                tvDisplay.text = ""
            }
        } else {
            tvDisplay.text = text.dropLast(1)
        }

        if (tvDisplay.text.isEmpty()) {
            tvDisplay.text = "0"
        }
    }

    private fun onPercentClick() {
        if (tvDisplay.text.toString() == "Error") return
        
        // If result is displayed, apply % to it
        if (isResult) {
            val valStr = tvDisplay.text.toString()
            val value = valStr.toDoubleOrNull()
            if (value != null) {
                tvDisplay.text = formatResult(value / 100.0)
                isResult = true
            }
            return
        }

        // Apply % to the last number in the expression
        val text = tvDisplay.text.toString()
        val parts = text.split(" ").toMutableList()
        
        if (parts.isNotEmpty()) {
            val lastToken = parts.last()
            if (lastToken.isNotEmpty() && lastToken != "+" && lastToken != "-" && lastToken != "×" && lastToken != "÷") {
                val value = lastToken.toDoubleOrNull()
                if (value != null) {
                    parts[parts.lastIndex] = formatResult(value / 100.0)
                    tvDisplay.text = parts.joinToString(" ")
                }
            }
        }
    }

    private fun evaluateExpression(expression: String): Double {
        // Tokenize: "5 + 3" -> ["5", "+", "3"]
        val tokens = expression.split(" ").filter { it.isNotBlank() }.toMutableList()

        if (tokens.isEmpty()) return 0.0
        
        // Remove trailing operator if present
        if (tokens.size > 1 && tokens.last() in listOf("+", "-", "×", "÷")) {
            tokens.removeAt(tokens.lastIndex)
        }
        
        if (tokens.size == 1) return tokens[0].toDouble()

        // Pass 1: Multiply / Divide
        var i = 1
        while (i < tokens.size - 1) {
            val op = tokens[i]
            if (op == "×" || op == "÷") {
                val left = tokens[i - 1].toDouble()
                val right = tokens[i + 1].toDouble()
                val res = if (op == "×") left * right else {
                    if (right == 0.0) throw ArithmeticException("Div by zero")
                    left / right
                }
                
                tokens[i - 1] = res.toString()
                tokens.removeAt(i)
                tokens.removeAt(i)
            } else {
                i += 2
            }
        }

        // Pass 2: Add / Subtract
        var result = tokens[0].toDouble()
        i = 1
        while (i < tokens.size - 1) {
            val op = tokens[i]
            val nextVal = tokens[i + 1].toDouble()
            if (op == "+") result += nextVal
            else if (op == "-") result -= nextVal
            i += 2
        }

        return result
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        
        // Remove unnecessary decimal places (5.0 -> 5)
        val bd = BigDecimal(value)
        return bd.stripTrailingZeros().toPlainString()
    }
}