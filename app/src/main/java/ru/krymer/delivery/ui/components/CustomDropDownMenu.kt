package ru.krymer.delivery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.krymer.delivery.R
import ru.krymer.delivery.ui.theme.AppTheme

@Composable
fun <T> GenericDropdown(
    modifier: Modifier = Modifier,
    selectedItem: T?,
    items: List<T>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    itemLabel: (T) -> String,
    placeholder: String = stringResource(R.string.not_select),
    contentDescription: String? = null,
    onItemSelected: (T?) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = AppTheme.colors.secondary,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clickable { onExpandedChange(!expanded) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedItem?.let { itemLabel(it) } ?: placeholder,
                modifier = Modifier.padding(start = 15.dp),
                color = AppTheme.colors.onSecondary,
                style = AppTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = contentDescription,
                modifier = Modifier.padding(end = 15.dp)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = placeholder,
                            style = AppTheme.typography.titleSmall
                        )
                    },
                    onClick = {
                        onItemSelected(null)
                        onExpandedChange(false)
                    }
                )
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemLabel(item),
                                style = AppTheme.typography.titleSmall
                            )
                        },
                        onClick = {
                            onItemSelected(item)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}