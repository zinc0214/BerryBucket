package com.zinc.waver.ui_write.presentation.bottomScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.common.models.CategoryInfo
import com.zinc.common.models.YesOrNo
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray4
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_write.R
import com.zinc.waver.ui_write.viewmodel.WriteCategoryViewModel

@Composable
fun CategorySelectBottomScreen(
    confirmed: (CategoryInfo) -> Unit,
    goToAddCategory: () -> Unit
) {
    val viewModel: WriteCategoryViewModel = hiltViewModel()
    val categoryList by viewModel.categoryInfoList.observeAsState()

    // 컴포지션 본문에서 호출하면 리컴포지션마다 다시 요청한다.
    LaunchedEffect(Unit) {
        viewModel.loadCategoryList()
    }

    categoryList?.let {
        CategorySelectView(it,
            confirmed = { category ->
                confirmed(category)
            }, goToAddCategory = {
                goToAddCategory()
            })
    }
}

@Composable
private fun CategorySelectView(
    categoryInfoList: List<CategoryInfo>,
    confirmed: (CategoryInfo) -> Unit,
    goToAddCategory: () -> Unit
) {
    if (categoryInfoList.isNotEmpty()) {

        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (category, addButton) = createRefs()

            LazyColumn(
                modifier = Modifier.constrainAs(category) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(addButton.top)
                },
                contentPadding = PaddingValues(
                    start = 28.dp,
                    end = 28.dp,
                    top = 39.dp,
                    bottom = 50.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = categoryInfoList, key = { keyCategory ->
                    keyCategory.id
                }, itemContent = { category ->
                    MyText(
                        text = category.name,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                confirmed(category)
                            }
                            .padding(vertical = 10.dp)
                    )
                })
            }


            Column(modifier = Modifier
                .fillMaxWidth()
                .background(Gray1)
                .constrainAs(addButton) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }) {

                Divider(color = Gray4, thickness = 1.dp)
                MyText(
                    text = stringResource(id = R.string.optionCategoryAddButton),
                    color = Main4,
                    fontSize = dpToSp(dp = 15.dp),
                    style = TextStyle(textAlign = TextAlign.Center),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            goToAddCategory()
                        }
                        .padding(horizontal = 28.dp, vertical = 18.dp)
                )
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
private fun CategorySelectBottomScreenPreview() {
    CategorySelectView(categoryInfoList = buildList {
        repeat(20) {
            add(
                CategoryInfo(
                    id = it,
                    name = "여행+$it",
                    defaultYn = YesOrNo.Y,
                    bucketCount = "10"
                )
            )
        }
    }, confirmed = {}, goToAddCategory = {})
}