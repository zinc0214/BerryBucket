package com.zinc.waver.ui.presentation.screen.category.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.zinc.waver.model.UICategoryInfo
import com.zinc.waver.ui.design.theme.Error2
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray10
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Main1
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.IconButton
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.presentation.component.TitleIconType
import com.zinc.waver.ui.presentation.component.TitleView
import com.zinc.waver.ui.presentation.screen.category.model.CategoryEditOptionEvent
import com.zinc.waver.ui.util.dpToSp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import com.zinc.waver.ui_common.R as CommonR

@Composable
internal fun CategoryEditTitleView(
    backClicked: () -> Unit,
    updateCategoryOrder: () -> Unit
) {
    TitleView(
        title = stringResource(id = CommonR.string.categoryEditTitle),
        leftIconType = TitleIconType.BACK,
        isDividerVisible = true,
        onLeftIconClicked = {
            backClicked()
        },
        rightText = stringResource(id = CommonR.string.finishDesc),
        onRightTextClicked = {
            updateCategoryOrder()
        }
    )
}


@Composable
internal fun CategoryEditAddView(
    addNewCategory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Main1)
            .clickable {
                addNewCategory()
            }
            .padding(horizontal = 26.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = CommonR.drawable.ico_20_add),
            contentDescription = null
        )
        MyText(
            modifier = Modifier.padding(start = 6.dp),
            fontSize = 16.sp,
            color = Main4,
            fontWeight = FontWeight.Bold,
            text = stringResource(id = CommonR.string.categoryAdd)
        )
    }
}

@Composable
fun VerticalReorderList(
    categoryList: List<UICategoryInfo>,
    addNewCategory: () -> Unit,
    optionEvent: (CategoryEditOptionEvent) -> Unit
) {

    var data by remember { mutableStateOf(categoryList) }

    // categoryList가 변경되는 시점에서 data 값을 업데이트
    LaunchedEffect(categoryList) {
        data = categoryList
    }

    val state = rememberReorderableLazyListState(onMove = { from, to ->
        data = data.toMutableList().apply {
            val toIndex = if (to.index < 1) 1 else to.index - 1
            val fromIndex = if (from.index < 1) 1 else from.index - 1
            add(toIndex, removeAt(fromIndex))
        }
    }, onDragEnd = { _, _ ->
        optionEvent.invoke(CategoryEditOptionEvent.ReorderedCategory(data))
    })
    LazyColumn(
        state = state.listState,
        modifier = Modifier.run {
            reorderable(state)
                .detectReorderAfterLongPress(state)
        }
    ) {
        item {
            CategoryEditAddView(addNewCategory)
        }
        itemsIndexed(data, key = { index, item -> item.id }) { index, item ->
            ReorderableItem(state, key = item, orientationLocked = false) {
                EditCategoryItemView(index, item, optionEvent)
            }
        }
    }
}

@Composable
private fun EditCategoryItemView(
    index: Int,
    item: UICategoryInfo,
    optionEvent: (CategoryEditOptionEvent) -> Unit
) {

    val expandedMenuIndex = remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Gray1)
                .padding(start = 28.dp, top = 16.dp, bottom = 16.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyText(text = item.name, color = Gray10, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                modifier = Modifier
                    .padding(4.dp)
                    .size(24.dp)
                    .rotate(if (expandedMenuIndex.value == index) 180f else 0f),
                onClick = {
                    expandedMenuIndex.value = index
                },
                image = CommonR.drawable.btn_24_more,
                contentDescription = stringResource(id = CommonR.string.categoryItemEdit)
            )
            IconButton(
                modifier = Modifier
                    .padding(4.dp)
                    .size(24.dp),
                onClick = {
                    // 드래그 ...
                },
                image = CommonR.drawable.btn_24_move,
                contentDescription = stringResource(id = CommonR.string.categoryItemEdit)
            )
        }

        Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Gray3)

        Card(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.TopEnd),
            backgroundColor = Color.Transparent,
            elevation = 1.dp
        ) {
            MaterialTheme(shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(8.dp))) {

                if (expandedMenuIndex.value == index) {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { expandedMenuIndex.value = -1 },
                        offset = DpOffset(58.dp, (-30).dp),
                        properties = PopupProperties(clippingEnabled = false)
                    ) {

                        MyText(
                            text = stringResource(id = CommonR.string.categoryNameEdit),
                            color = Gray10,
                            fontSize = dpToSp(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    optionEvent.invoke(CategoryEditOptionEvent.EditCategoryName(item))
                                    expandedMenuIndex.value = -1
                                }
                                .padding(
                                    start = 16.dp,
                                    end = 38.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                )
                        )

                        if (item.defaultYn.isNo()) {
                            MyText(
                                text = stringResource(id = CommonR.string.categoryDelete),
                                color = Error2,
                                fontSize = dpToSp(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        optionEvent.invoke(
                                            CategoryEditOptionEvent.DeleteCategory(
                                                item
                                            )
                                        )
                                        expandedMenuIndex.value = -1
                                    }
                                    .padding(
                                        start = 16.dp,
                                        end = 38.dp,
                                        top = 8.dp,
                                        bottom = 8.dp
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

}


@Composable
@Preview
private fun CategoryEditAddPreview() {
    CategoryEditAddView {

    }
}