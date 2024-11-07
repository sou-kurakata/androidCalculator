package com.ih13a213_kurakatasou.ih13a_kurakata06

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context

class MainActivity : AppCompatActivity() {

    private lateinit var formulaTextView: TextView
    private lateinit var formulaEditText: EditText
    private lateinit var resultTextView: TextView
    private var currentInput: String = ""
    private var formula: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        formulaTextView = findViewById(R.id.formulaTextView)
        formulaEditText = findViewById(R.id.formulaEditText)
        resultTextView = findViewById(R.id.resultTextView)

        val numberButtons = listOf<Button>(
            findViewById(R.id.zero),
            findViewById(R.id.one),
            findViewById(R.id.two),
            findViewById(R.id.three),
            findViewById(R.id.four),
            findViewById(R.id.five),
            findViewById(R.id.six),
            findViewById(R.id.seven),
            findViewById(R.id.eight),
            findViewById(R.id.nine)
        )

        for (button in numberButtons) {
            button.setOnClickListener {
                currentInput += (it as Button).text
                updateDisplay()
            }
        }

        val operatorButtons = listOf<Button>(
            findViewById(R.id.add),
            findViewById(R.id.min),
            findViewById(R.id.mul),
            findViewById(R.id.div)
        )

        for (button in operatorButtons) {
            button.setOnClickListener {
                if (currentInput.isNotEmpty()) {
                    formula += "$currentInput ${(it as Button).text} "
                    currentInput = ""
                    updateFormula()
                }
            }
        }

        findViewById<Button>(R.id.equal).setOnClickListener {
            if (currentInput.isNotEmpty() && formula.isNotEmpty()) {
                formula += " $currentInput"
                val result = evaluateExpression(formula)
                currentInput = result.toString()
                updateDisplay()
                resetFormula()
            }
        }

        findViewById<Button>(R.id.backSpace).setOnClickListener {
            currentInput = ""
            formula = ""
            updateFormula()
            updateDisplay()
        }

        // Edit functionality: set formulaTextView clickable to enable editing
        formulaTextView.setOnClickListener {
            enterEditMode()
        }

        formulaEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                exitEditMode()
            }
        }
    }

    private fun updateDisplay() {
        resultTextView.text = currentInput
        formulaTextView.text = formula
    }

    private fun updateFormula() {
        formulaTextView.text = formula
    }

    private fun resetFormula() {
        formula = ""
    }

    private fun enterEditMode() {
        formulaEditText.visibility = View.VISIBLE
        formulaEditText.setText(formulaTextView.text.toString())
        formulaTextView.visibility = View.GONE
        formulaEditText.requestFocus()

        formulaEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                exitEditMode()
                true
            } else {
                false
            }
        }

        formulaEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                formula = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun exitEditMode() {
        formula = formulaEditText.text.toString()

        try {
            val result = evaluateExpression(formula)
            currentInput = result.toString()
        } catch (e: ArithmeticException) {
            currentInput = "エラー: ${e.message}" // エラーメッセージを表示
        } catch (e: Exception) {
            currentInput = "エラー"
        }

        formulaTextView.text = formula
        formulaTextView.visibility = View.VISIBLE
        formulaEditText.visibility = View.GONE

        updateDisplay()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(formulaEditText.windowToken, 0)
    }


    fun evaluateExpression(expression: String): Double {
        val tokens = tokenize(expression)
        return evaluateTokens(tokens)
    }

    fun tokenize(expression: String): MutableList<String> {
        val tokens = mutableListOf<String>()
        var currentNumber = StringBuilder()

        for (char in expression) {
            if (char.isDigit() || char == '.') {
                currentNumber.append(char)
            } else if (char == '+' || char == '-' || char == '*' || char == '/') {
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber.toString())
                    currentNumber.clear()
                }
                tokens.add(char.toString())
            }
        }

        if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber.toString())
        }

        return tokens
    }

    fun evaluateTokens(tokens: MutableList<String>): Double {
        var i = 0
        try {
            while (i < tokens.size) {
                if (tokens[i] == "*" || tokens[i] == "/") {
                    val operand1 = tokens[i - 1].toDouble()
                    val operand2 = tokens[i + 1].toDouble()
                    val result = if (tokens[i] == "*") {
                        operand1 * operand2
                    } else {
                        if (operand2 == 0.0) {
                            // 0で割ろうとした場合の処理
                            return Double.NaN // NaNを返すことで、結果が無効であることを示す
                        }
                        operand1 / operand2
                    }
                    tokens[i - 1] = result.toString()
                    tokens.removeAt(i)
                    tokens.removeAt(i)
                    i--
                }
                i++
            }

            i = 0
            while (i < tokens.size) {
                if (tokens[i] == "+" || tokens[i] == "-") {
                    val operand1 = tokens[i - 1].toDouble()
                    val operand2 = tokens[i + 1].toDouble()
                    val result = if (tokens[i] == "+") {
                        operand1 + operand2
                    } else {
                        operand1 - operand2
                    }
                    tokens[i - 1] = result.toString()
                    tokens.removeAt(i)
                    tokens.removeAt(i)
                    i--
                }
                i++
            }
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("無効な数値形式")
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("不正な演算子の使用")
        }

        // 最終的な結果がNaNの場合
        if (tokens.isNotEmpty() && tokens[0].toDouble() == Double.NaN) {
            throw ArithmeticException("0で割ることはできません")
        }

        return tokens[0].toDouble()
    }

}
