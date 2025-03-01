package ru.krymer.delivery.ui.screens.analitic.view

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.ui.screens.analitic.AnaliticViewModel
import ru.krymer.delivery.ui.screens.analitic.models.AnaliticViewState
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun AnaliticFactoryView(viewModel: AnaliticViewModel) {
    val viewState = viewModel.viewState.collectAsState().value
    val requests = viewState.requests.collectAsState().value

    if (viewState.isLoadDataFactoryInRangeDate) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            item {
                TextFactoryInformation(viewState = viewState)
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                TitlesForRequestsList()
            }

            item {
                Spacer(modifier = Modifier.height(5.dp))
            }

            items(requests) { request ->
                RequestItems(request = request)
                Spacer(modifier = Modifier.height(5.dp))
            }

            item {
                BarsView(viewState = viewState)
                LinesView(viewState = viewState)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center),
                strokeWidth = 2.dp,
                color = Color.White
            )
        }
    }
}

@Composable
fun BarsView(viewState: AnaliticViewState) {
    val bars = viewState.bars.collectAsState().value
    val stateScroll = rememberScrollState()
    if (bars.isNotEmpty()) {
        ColumnChart(
            data = bars,
            modifier = Modifier
                .horizontalScroll(stateScroll)
                .width((400 + (50 * bars.size)).dp)
                .height(400.dp)
                .heightIn(max = 1000.dp)
                .padding(bottom = 50.dp),
            barProperties = BarProperties(spacing = 0.dp),
            labelProperties = LabelProperties(
                padding = 0.dp,
                enabled = true,
                textStyle = TextStyle(color = AppTheme.colors.onSecondary),
                rotation = LabelProperties.Rotation(
                    mode = LabelProperties.Rotation.Mode.Force, degree = -90f
                ),
            ),
            indicatorProperties = HorizontalIndicatorProperties(
                textStyle = TextStyle(
                    color = AppTheme.colors.onSecondary, textAlign = TextAlign.Center
                )
            )
        )
    }
}

@Composable
fun LinesView(viewState: AnaliticViewState) {
    val line = viewState.lines.collectAsState().value
    if (line.isNotEmpty()) {
        LineChart(
            data = line,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .heightIn(max = 1000.dp),
            indicatorProperties = HorizontalIndicatorProperties(
                textStyle = TextStyle(
                    color = AppTheme.colors.onSecondary, textAlign = TextAlign.Center
                )
            )
        )

    }
}

@Composable
fun TitlesForRequestsList() {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
    ) {
        TextItemInfo(
            value = "Продукт",
            modifier = Modifier.weight(0.4f),
            size = 12.sp,
        )
        TextItemInfo(
            value = "Бонус",
            modifier = Modifier.weight(0.2f),
            size = 12.sp,
            textAlign = TextAlign.Center
        )
        TextItemInfo(
            value = "Заявка",
            modifier = Modifier.weight(0.2f),
            size = 12.sp,
            textAlign = TextAlign.Center
        )
        TextItemInfo(
            value = "Обмены",
            modifier = Modifier.weight(0.2f),
            size = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TextItemInfo(
    value: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 18.sp,
    textAlign: TextAlign = TextAlign.Left
) {
    Text(
        text = value,
        color = AppTheme.colors.onSecondary,
        fontSize = size,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun TextFactoryInformation(viewState: AnaliticViewState) {
    val dataTrips = viewState.allDataFactoryOfDateRange.collectAsState().value
    Column(modifier = Modifier.fillMaxWidth()) {
        TextItemInfo(value = "Пробег: ${dataTrips.millage}")
        TextItemInfo(value = "Выручка: ${dataTrips.money}")
        TextItemInfo(value = "Наличные: ${dataTrips.cash}")
        TextItemInfo(value = "Без/нал: ${dataTrips.noCash}")
        TextItemInfo(value = "ЗП: ${dataTrips.salary}")
        TextItemInfo(value = "Процент курьеров: ${dataTrips.percentSalary}")
        TextItemInfo(value = "Стоимость доп.продукции: ${dataTrips.otherProductMoney}")
        TextItemInfo(value = "Стоимость продукции: ${dataTrips.productPrice}")
        TextItemInfo(value = "Стоимость возврата: ${dataTrips.productExchangePrice}")
        TextItemInfo(value = "Сумма возвратов: ${dataTrips.exchange} ${dataTrips.perSumExchange}%")
    }
}

@Composable
fun RequestItems(request: RequestModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(shape = RoundedCornerShape(15.dp), color = AppTheme.colors.secondary),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextItemInfo(
            value = request.name,
            modifier = Modifier
                .weight(0.4f)
                .padding(3.dp),
            size = 14.sp,
        )
        TextItemInfo(
            value = "${request.bonus}",
            modifier = Modifier
                .weight(0.2f)
                .padding(3.dp),
            size = 14.sp,
            textAlign = TextAlign.Center
        )
        TextItemInfo(
            value = "${request.count}",
            modifier = Modifier
                .weight(0.2f)
                .padding(3.dp),
            size = 14.sp,
            textAlign = TextAlign.Center
        )
        TextItemInfo(
            value = "${request.exchange}",
            modifier = Modifier
                .weight(0.2f)
                .padding(3.dp),
            size = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
