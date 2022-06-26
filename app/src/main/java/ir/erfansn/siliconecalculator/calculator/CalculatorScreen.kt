package ir.erfansn.siliconecalculator.calculator

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import ir.erfansn.siliconecalculator.ui.component.FlatIconButton
import ir.erfansn.siliconecalculator.ui.component.SiliconeButton
import ir.erfansn.siliconecalculator.ui.layout.Grid
import ir.erfansn.siliconecalculator.ui.theme.SiliconeCalculatorTheme

@Composable
fun CalculatorScreen(
    uiState: CalculatorUiState,
    onButtonClick: (CalculatorButton) -> Unit,
    onHistoryNav: () -> Unit,
    onThemeToggle: () -> Unit,
) {
    ConstraintLayout(
        constraintSet = constraintSet,
        modifier = Modifier
            .fillMaxSize()
            .safeGesturesPadding(),
    ) {
        CalculatorTopBar(
            onThemeToggle = onThemeToggle,
            onHistoryNav = onHistoryNav,
        )
        CalculatorContent(
            onButtonClick = onButtonClick,
            mathExpression = uiState.mathExpression,
            evaluationResult = uiState.evaluationResult
        )
    }
}

@Composable
fun CalculatorTopBar(
    onThemeToggle: () -> Unit,
    onHistoryNav: () -> Unit,
) {
    Row(
        modifier = Modifier.layoutId("top_bar"),
        horizontalArrangement = Arrangement.spacedBy(10.dp,
            alignment = Alignment.Start)
    ) {
        FlatIconButton(
            modifier = Modifier
                .aspectRatio(1.25f),
            onClick = onThemeToggle,
            icon = if (MaterialTheme.colors.isLight) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
            contentDescription = "Theme changer"
        )
        FlatIconButton(
            modifier = Modifier
                .aspectRatio(1.25f),
            onClick = onHistoryNav,
            icon = Icons.Outlined.History,
            contentDescription = "History calculations"
        )
    }
}

@Composable
fun CalculatorContent(
    onButtonClick: (CalculatorButton) -> Unit,
    mathExpression: String,
    evaluationResult: String,
) {
    Display(
        mathExpression = mathExpression,
        evaluationResult = evaluationResult,
    )
    NumberPad(
        onButtonClick = onButtonClick
    )
}

@Composable
private fun Display(
    mathExpression: String,
    evaluationResult: String,
) {
    Column(
        modifier = Modifier
            .layoutId("display")
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            modifier = Modifier
                .horizontalScroll(
                    state = rememberScrollState(),
                    reverseScrolling = true
                )
                .padding(horizontal = 20.dp)
                .alpha(ContentAlpha.medium),
            text = mathExpression,
            style = MaterialTheme.typography.h4.copy(
                fontWeight = FontWeight.Light,
            ),
            textAlign = TextAlign.Center,
        )

        val resultText: @Composable () -> Unit = {
            Text(
                modifier = Modifier
                    .horizontalScroll(
                        state = rememberScrollState(),
                        reverseScrolling = true
                    )
                    .padding(horizontal = 20.dp),
                text = evaluationResult,
                style = MaterialTheme.typography.h2.copy(
                    fontWeight = FontWeight.Normal,
                ),
                textAlign = TextAlign.Center,
            )
        }
        val resultBeShown = mathExpression.isNotEmpty() && evaluationResult.toDoubleOrNull() != null
        if (resultBeShown) {
            val customTextSelectionColors = TextSelectionColors(
                handleColor = MaterialTheme.colors.secondary,
                backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.4f)
            )
            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                SelectionContainer(content = resultText)
            }
        } else {
            resultText()
        }
    }
}

@Composable
private fun NumberPad(
    calculatorState: CalculatorState = rememberCalculatorState(),
    onButtonClick: (CalculatorButton) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .aspectRatio(calculatorState.buttonsLayoutAspectRation)
            .layoutId("number_pad")
    ) {
        Grid(
            columns = BUTTON_LAYOUT_COLUMNS_COUNT,
            modifier = Modifier.fillMaxSize(),
        ) {
            val buttonSizeWithoutSpacing = maxWidth / BUTTON_LAYOUT_COLUMNS_COUNT
            val spaceBetweenButtons =
                calculatorState.calculateButtonSpacing(buttonSizeWithoutSpacing)

            for ((button, category, widthRatio) in calculatorState.buttonsCharacteristic) {
                val buttonColor = when (category) {
                    ButtonCategory.OPERATOR -> MaterialTheme.colors.secondary
                    ButtonCategory.FUNC -> MaterialTheme.colors.primaryVariant
                    ButtonCategory.DIGIT -> MaterialTheme.colors.primary
                }
                SiliconeButton(
                    modifier = Modifier
                        .span(
                            columns = widthRatio,
                            rows = 1,
                        )
                        .padding(spaceBetweenButtons),
                    lightColor = buttonColor,
                    onClick = { onButtonClick(button) }
                ) {
                    Text(
                        text = button.symbol,
                        color = contentColorFor(backgroundColor = buttonColor),
                        style = TextStyle(
                            fontSize = with(LocalDensity.current) {
                                (maxHeight * 0.33f).toSp()
                            },
                            fontWeight = FontWeight.Light,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

val constraintSet = ConstraintSet {
    val topBarRef = createRefFor("top_bar")
    val display = createRefFor("display")
    val numberPad = createRefFor("number_pad")

    val topGuideline1 = createGuidelineFromTop(0.01f)
    val topGuideline9 = createGuidelineFromTop(0.09f)
    val topGuideline40 = createGuidelineFromTop(0.4f)
    val bottomGuideline3 = createGuidelineFromBottom(0.03f)

    constrain(topBarRef) {
        linkTo(
            top = topGuideline1,
            start = parent.start,
            bottom = topGuideline9,
            end = parent.end,
            startMargin = 20.dp
        )

        width = Dimension.fillToConstraints
        height = Dimension.fillToConstraints
    }
    constrain(display) {
        bottom.linkTo(numberPad.top, margin = 16.dp)
        end.linkTo(parent.end)

        width = Dimension.wrapContent
        height = Dimension.fillToConstraints
    }
    constrain(numberPad) {
        linkTo(
            top = topGuideline40,
            bottom = bottomGuideline3,
            start = parent.start,
            end = parent.end,
            verticalBias = 1f
        )

        height = Dimension.preferredWrapContent
        width = Dimension.preferredWrapContent
    }
}

private const val BUTTON_LAYOUT_COLUMNS_COUNT = 4

@Stable
object CalculatorState {
    private val buttonsRowList = buttonList.chunked(BUTTON_LAYOUT_COLUMNS_COUNT)

    val buttonsCharacteristic
        get() = buttonList.map {
            Triple(it, buttonCategory(it), buttonWidthRatio(it))
        }

    val buttonsLayoutAspectRation = BUTTON_LAYOUT_COLUMNS_COUNT.toFloat() / buttonsRowList.size

    fun calculateButtonSpacing(buttonWidth: Dp): Dp {
        return buttonWidth * 0.04f
    }

    private fun buttonWidthRatio(button: CalculatorButton): Int {
        return if (button == CalculatorButton.Digit(0)) 2 else 1
    }

    private fun buttonCategory(button: CalculatorButton): ButtonCategory {
        return when (button) {
            in buttonsRowList.map(List<CalculatorButton>::last) -> ButtonCategory.OPERATOR
            in buttonsRowList.first() -> ButtonCategory.FUNC
            else -> ButtonCategory.DIGIT
        }
    }
}

enum class ButtonCategory { DIGIT, OPERATOR, FUNC }

@Composable
fun rememberCalculatorState() = remember { CalculatorState }

@ExperimentalMaterialApi
@Preview(
    name = "Light theme",
    showBackground = true,
)
@Preview(
    name = "Dark theme",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES,
)
@Composable
fun CalculatorScreenPreview() {
    SiliconeCalculatorTheme {
        Surface(color = MaterialTheme.colors.background) {
            CalculatorScreen(
                uiState = CalculatorUiState("4,900 + 15,910", "20,810"),
                onButtonClick = { },
                onHistoryNav = { },
                onThemeToggle = { }
            )
        }
    }
}