package com.example.tiptime

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TipTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TipTimeLayout { changeLanguage(it) }
                }
            }
        }
    }

    // Función para cambiar el idioma de la aplicación
    private fun changeLanguage(isEnglish: Boolean) {
        val newLocale = if (isEnglish) Locale("en") else Locale("es")

        // Solo cambia si el idioma es diferente
        if (Locale.getDefault().language != newLocale.language) {
            Locale.setDefault(newLocale)
            val config = Configuration()
            config.locale = newLocale
            baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
            recreate() // Recargar la actividad para aplicar el nuevo idioma
        }
    }

    @Composable
    fun TipTimeLayout(onLanguageChanged: (Boolean) -> Unit) {
        var amountInput by remember { mutableStateOf("") }
        var tipInput by remember { mutableStateOf("") }
        var roundUp by remember { mutableStateOf(false) }
        var isUSD by remember { mutableStateOf(true) }  // Para cambiar entre USD y pesos chilenos
        var isEnglish by remember { mutableStateOf(true) } // Controla el idioma

        val amount = amountInput.toDoubleOrNull() ?: 0.0
        val tipPercent = tipInput.toDoubleOrNull() ?: 0.0
        val tip = calculateTip(amount, tipPercent, roundUp, isUSD)

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 40.dp)
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.calculate_tip),
                modifier = Modifier
                    .padding(bottom = 16.dp, top = 40.dp)
                    .align(alignment = Alignment.Start)
            )
            EditNumberField(
                label = R.string.bill_amount,
                leadingIcon = R.drawable.money,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                value = amountInput,
                onValueChanged = { amountInput = it },
                modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(),
            )
            EditNumberField(
                label = R.string.suggested_percentage,
                leadingIcon = R.drawable.percent,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                value = tipInput,
                onValueChanged = { tipInput = it },
                modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(),
            )
            CurrencySwitch(isUSD = isUSD, onCurrencyChanged = { isUSD = it })
            LanguageSwitch(isEnglish = isEnglish, onLanguageChanged = {
                isEnglish = it
                onLanguageChanged(it)
            })
            RoundTheTipRow(
                roundUp = roundUp,
                onRoundUpChanged = { roundUp = it },
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Text(
                text = stringResource(R.string.tip_amount, tip),
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(150.dp))
        }
    }

    @Composable
    fun LanguageSwitch(isEnglish: Boolean, onLanguageChanged: (Boolean) -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mostrar el nombre del idioma contrario
            val languageText = if (isEnglish) stringResource(R.string.spanish) else stringResource(R.string.english)
            Text(text = languageText) // Mostrar el idioma contrario

            Switch(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End),
                checked = isEnglish,
                onCheckedChange = onLanguageChanged
            )
        }
    }

    @Composable
    fun CurrencySwitch(isUSD: Boolean, onCurrencyChanged: (Boolean) -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.currency_usd))
            Switch(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End),
                checked = isUSD,
                onCheckedChange = onCurrencyChanged
            )
            Text(text = stringResource(R.string.currency_clp))
        }
    }

    @Composable
    fun EditNumberField(
        @StringRes label: Int,
        @DrawableRes leadingIcon: Int,
        keyboardOptions: KeyboardOptions,
        value: String,
        onValueChanged: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        TextField(
            value = value,
            singleLine = true,
            leadingIcon = { Icon(painter = painterResource(id = leadingIcon), null) },
            modifier = modifier,
            onValueChange = onValueChanged,
            label = { Text(stringResource(label)) },
            keyboardOptions = keyboardOptions
        )
    }

    @Composable
    fun RoundTheTipRow(
        roundUp: Boolean,
        onRoundUpChanged: (Boolean) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.round_up_tip))
            Switch(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End),
                checked = roundUp,
                onCheckedChange = onRoundUpChanged
            )
        }
    }

    private fun calculateTip(
        amount: Double,
        tipPercent: Double,
        roundUp: Boolean,
        isUSD: Boolean
    ): String {
        var tip = tipPercent / 100 * amount
        if (roundUp) {
            tip = kotlin.math.ceil(tip)
        }

        val currencyInstance = NumberFormat.getCurrencyInstance().apply {
            currency = if (isUSD) {
                Currency.getInstance("USD")
            } else {
                Currency.getInstance("CLP")
            }
        }

        return currencyInstance.format(tip)
    }

    @Preview(showBackground = true)
    @Composable
    fun TipTimeLayoutPreview() {
        TipTimeTheme {
            TipTimeLayout(onLanguageChanged = {})
        }
    }
}
