package com.example.myfirstapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var tvDisplay: TextView
    
    // Variables to hold operands and operator
    private var firstNumber: Double? = null
    private var currentOperator: String? = null
    private var isNewOperation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Display
        tvDisplay = findViewById(R.id.tvDisplay)

        // Initialize Number Buttons (0-9)
        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
            R.id.btn8, R.id.btn9
        )

        for (id in numberButtons) {
            findViewById<Button>(id).setOnClickListener { view ->
                onNumberClick((view as Button).text.toString())
            }
        }

        // Initialize Operator Buttons
        findViewById<Button>(R.id.btnAdd).setOnClickListener { onOperatorClick("+") }
        findViewById<Button>(R.id.btnSub).setOnClickListener { onOperatorClick("-") }
        findViewById<Button>(R.id.btnMul).setOnClickListener { onOperatorClick("*") }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { onOperatorClick("/") }

        // Initialize Special Buttons
        findViewById<Button>(R.id.btnEqual).setOnClickListener { onEqualClick() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { onClearClick() }
        findViewById<Button>(R.id.btnDot).setOnClickListener { onDotClick() }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener { onBackspaceClick() }
        
        // Add Percent support (optional, simple implementation)
        val btnPercent = findViewById<Button>(R.id.btnPercent)
        if (btnPercent != null) {
            btnPercent.setOnClickListener { onPercentClick() }
        }
    }

    private fun onNumberClick(number: String) {
        if (isNewOperation) {
            tvDisplay.text = ""
            isNewOperation = false
        }
        
        val currentText = tvDisplay.text.toString()
        if (currentText == "0") {
            tvDisplay.text = number
        } else {
            tvDisplay.append(number)
        }
    }

    private fun onOperatorClick(operator: String) {
        if (tvDisplay.text.isNotEmpty()) {
            firstNumber = tvDisplay.text.toString().toDoubleOrNull()
            currentOperator = operator
            isNewOperation = true
        }
    }

    private fun onEqualClick() {
        val secondText = tvDisplay.text.toString()
        val secondNumber = secondText.toDoubleOrNull()

        if (firstNumber != null && secondNumber != null && currentOperator != null) {
            var result = 0.0
            var error = false

            when (currentOperator) {
                "+" -> result = firstNumber!! + secondNumber
                "-" -> result = firstNumber!! - secondNumber
                "*" -> result = firstNumber!! * secondNumber
                "/" -> {
                    if (secondNumber == 0.0) {
                        Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show()
                        error = true
                    } else {
                        result = firstNumber!! / secondNumber
                    }
                }
            }

            if (!error) {
                // Remove decimal if it's a whole number (e.g. 5.0 -> 5)
                val finalResult = if (result % 1.0 == 0.0) {
                    result.toInt().toString()
                } else {
                    result.toString()
                }
                tvDisplay.text = finalResult
                
                // Prepare for next operation
                firstNumber = result
                isNewOperation = true
            }
        }
    }

    private fun onClearClick() {
        tvDisplay.text = "0"
        firstNumber = null
        currentOperator = null
        isNewOperation = true
    }

    private fun onDotClick() {
        if (isNewOperation) {
            tvDisplay.text = "0."
            isNewOperation = false
        } else if (!tvDisplay.text.contains(".")) {
            tvDisplay.append(".")
        }
    }
    
    private fun onBackspaceClick() {
        if (isNewOperation) return 

        val currentText = tvDisplay.text.toString()
        if (currentText.length > 1) {
            tvDisplay.text = currentText.substring(0, currentText.length - 1)
        } else {
            tvDisplay.text = "0"
        }
    }

    private fun onPercentClick() {
        val currentText = tvDisplay.text.toString()
        val number = currentText.toDoubleOrNull()
        if (number != null) {
            val result = number / 100
            tvDisplay.text = result.toString()
            isNewOperation = true
        }
    }
}
