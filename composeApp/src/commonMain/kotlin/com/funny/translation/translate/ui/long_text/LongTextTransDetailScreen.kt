package com.funny.translation.translate.ui.long_text

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.token.TokenCounter
import com.funny.data_saver.core.rememberDataSaverState
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.bean.EditablePrompt
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.assetsStringLocalized
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.desktopOnly
import com.funny.translation.kmp.navigateUp
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.kmp.rememberCreateFileLauncher
import com.funny.translation.kmp.viewModel
import com.funny.translation.kmp.writeText
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ui.long_text.components.AIPointText
import com.funny.translation.translate.ui.long_text.components.RemarkDialog
import com.funny.translation.translate.ui.long_text.components.ResultTextPart
import com.funny.translation.translate.ui.long_text.components.SourceTextPart
import com.funny.translation.translate.ui.widget.TwoProgressIndicator
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.dialog.AnyPopDialog
import com.funny.translation.ui.dialog.rememberAnyPopDialogState
import com.funny.translation.ui.floatingActionBarModifier
import com.funny.translation.ui.popDialogShape
import moe.tlaster.precompose.navigation.BackHandler
import java.util.UUID

@Composable
fun LongTextTransDetailScreen(
    id: String = UUID.randomUUID().toString(),
) {
    val vm: LongTextTransViewModel = viewModel()
    val navController = LocalNavController.current
    
    var showHelpDialog by rememberDataSaverState<Boolean>(
        key = "show_long_trans_tip",
        initialValue = true
    )

    SimpleDialog(
        openDialog = showHelpDialog,
        updateOpenDialog = { showHelpDialog = it },
        content = {
            MarkdownText(
                modifier = Modifier.desktopOnly(Modifier.verticalScroll(rememberScrollState())),
                markdown = assetsStringLocalized(name = "long_text_trans_help.md")
            )
        }
    )

    // 跳转到其他页面时，暂停翻译
    DisposableEffect(key1 = Unit) {
        onDispose {
            if (vm.screenState == ScreenState.Translating && !vm.isPausing)
                vm.toggleIsPausing()
        }
    }
    
    CommonPage(
        modifier = Modifier.imePadding(),
        title = null,
        actions = {
            AIPointText()
            IconButton(onClick = { showHelpDialog = true }) {
                FixedSizeIcon(Icons.Default.Help, contentDescription = "Help")
            }
        },
        addNavPadding = false
    ) {
        // 传入参数时，先初始化各类型
        LaunchedEffect(key1 = id){
            vm.initArgs(id)
        }

        val quitAlertDialog = rememberStateOf(value = false)
        SimpleDialog(
            openDialogState = quitAlertDialog,
            title = ResStrings.tip,
            message = ResStrings.quit_translating_alert,
            confirmButtonAction = {
                navController.navigateUp()
            }
        )

        val remarkDialogState = rememberStateOf(value = false)
        RemarkDialog(
            showState = remarkDialogState,
            taskId = vm.transId,
            initialRemark = vm.task?.remark ?: "",
            updateRemarkAction = vm::updateRemark
        )

        val dialogState = rememberAnyPopDialogState()
        AnyPopDialog(
            modifier = Modifier.popDialogShape().heightIn(480.dp, 600.dp),
            state = dialogState,
            content = {
                ModelListPart()
            },
        )

        BackHandler(enabled =
            (vm.screenState == ScreenState.Translating && !vm.isPausing)
            || (vm.screenState == ScreenState.Result && vm.task?.remark?.isEmpty() == true)
        ) {
            if (vm.screenState == ScreenState.Translating) {
                quitAlertDialog.value = true
            } else {
                remarkDialogState.value = true
            }
        }

        AnimatedVisibility (vm.screenState == ScreenState.Translating) {
            TwoProgressIndicator(startedProgress = vm.startedProgress, finishedProgress = vm.progress)
        }

        DetailContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            screenState = vm.screenState,
            vm = vm
        )


        FunctionRow(
            modifier = Modifier.fillMaxWidth(),
            vm = vm,
            screenState = vm.screenState,
            showModelAction = { dialogState.animateShow() },
            remark = vm.task?.remark ?: "",
            startTranslateAction = vm::startTranslate,
            showUpdateRemarkDialog = { remarkDialogState.value = true },
            exportOnlyResultProvider = vm::resultText,
            exportBothSourceAndResultProvider = vm::generateBothExportedText
        )

    }

    AnimatedVisibility(
        visible = vm.screenState == ScreenState.Translating,
        enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
        exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center)
    ) {
        Row(
            modifier = Modifier.floatingActionBarModifier(),
        ) {
            val showRetryBtn = vm.isPausing
            if (showRetryBtn) {
                FloatingActionButton(
                    modifier = Modifier,
                    onClick = vm::retryCurrentPart
                ) {
                    FixedSizeIcon(Icons.Default.RestartAlt, contentDescription = "Retry")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            FloatingActionButton(
                modifier = Modifier,
                onClick = vm::toggleIsPausing
            ) {
                AnimatedContent(targetState = vm.isPausing, label = "TogglePause") { pausing ->
                    if (pausing) {
                        FixedSizeIcon(Icons.Default.PlayArrow, contentDescription = "Click To Play")
                    } else {
                        FixedSizeIcon(Icons.Default.Pause, contentDescription = "Click To Pause")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.DetailContent(
    modifier: Modifier = Modifier,
    screenState: ScreenState,
    vm: LongTextTransViewModel
) {
    AnimatedContent(modifier = modifier, targetState = screenState, label = "DetailContent") {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (it) {
                ScreenState.Init -> {
                    SourceTextPart(
                        text = vm.sourceText,
                        updateSourceText = vm::updateSourceText,
                        screenState = vm.screenState,
                        tokenCounter = vm.chatBot.tokenCounter
                    )
                    PromptPart(vm.prompt, vm.chatBot.tokenCounter, vm::updatePrompt, vm::resetPrompt)
                    MaxSegmentSettingsPart(vm.maxSegmentLength, vm.chatBot.model, { vm.maxSegmentLength = it})
                    Category(
                        title = ResStrings.all_corpus,
                        helpText = ResStrings.corpus_help,
                        expandable = true
                    ) { expanded ->
                        AllCorpusList(vm = vm, expanded = expanded)
                    }
                    ModelListPart()
                }
                ScreenState.Translating -> {
                    SourceTextPart(
                        text = vm.sourceText,
                        screenState = vm.screenState,
                        currentTransStartOffset = vm.translatedLength,
                        currentTransLength = vm.currentTransPartLength,
                        tokenCounter = vm.chatBot.tokenCounter
                    )
                    ResultTextPart(
                        text = vm.resultText,
                        screenState = vm.screenState,
                        currentResultStartOffset = vm.currentResultStartOffset,
                        tokenCounter = vm.chatBot.tokenCounter
                    )
                    CorpusListPart(vm = vm)
                }
                ScreenState.Result -> {
                    SourceTextPart(text = vm.sourceText, screenState = vm.screenState, tokenCounter = vm.chatBot.tokenCounter)
                    ResultTextPart(text = vm.resultText, screenState = vm.screenState, tokenCounter = vm.chatBot.tokenCounter)
                    Category(
                        title = ResStrings.task_prompt,
                        helpText = ResStrings.task_prompt_help,
                    ) { expanded ->
                        SelectionContainer {
                            Text(
                                text = vm.prompt.toPrompt(),
                                maxLines = if (expanded) Int.MAX_VALUE else 4,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                    Category(
                        title = ResStrings.all_corpus,
                        helpText = ResStrings.corpus_help,
                    ) { expanded ->
                        AllCorpusList(vm = vm, expanded = expanded)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FunctionRow(
    modifier: Modifier = Modifier,
    vm: LongTextTransViewModel,
    screenState: ScreenState,
    startTranslateAction: SimpleAction,
    showModelAction: SimpleAction,
    remark: String = "",
    showUpdateRemarkDialog: () -> Unit,
    exportOnlyResultProvider: () -> String,
    exportBothSourceAndResultProvider: () -> String,
) {
    Surface(modifier, tonalElevation = 4.dp, shadowElevation = 4.dp) {
        Row(modifier = Modifier
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(),
            horizontalArrangement = Arrangement.Start
        ) {

            if (screenState == ScreenState.Init) {
                TintIconButton(icon =Icons.Default.PlayArrow, onClick = startTranslateAction)
            }

            else if (screenState == ScreenState.Translating) {
                TintIconButton(icon = Icons.Default.Dashboard, onClick = showModelAction)
                EditPromptButton(
                    initialPrompt = vm.prompt,
                    tokenCounter = vm.chatBot.tokenCounter,
                    initialMaxSegLength = vm.maxSegmentLength,
                    model = vm.chatBot.model,
                    onConfirmPrompt = {
                        vm.updatePrompt(it)
                        vm.savePrompt()
                    },
                    onConfirmMaxSegLength = {
                        vm.maxSegmentLength = it
                    }
                )
            }

            if (screenState == ScreenState.Result || screenState == ScreenState.Translating) {
                ExportButton(
                    remark = remark,
                    showUpdateRemarkDialog = showUpdateRemarkDialog,
                    exportOnlyResultProvider = exportOnlyResultProvider,
                    exportBothSourceAndResultProvider = exportBothSourceAndResultProvider
                )
            }
        }

    }
}

@Composable
private fun EditPromptButton(
    initialPrompt: EditablePrompt,
    tokenCounter: TokenCounter,
    initialMaxSegLength: Int?,
    model: Model,
    onConfirmPrompt: (String) -> Unit,
    onConfirmMaxSegLength: (Int?) -> Unit
) {
    val showEditPromptDialog = rememberStateOf(false)
    var prompt by rememberStateOf(initialPrompt)
    var updatedMaxSegLength: Int? by rememberStateOf(initialMaxSegLength)
    SimpleDialog(
        openDialogState = showEditPromptDialog,
        closeable = false,
        content = {
            Column {
                PromptPart(
                    initialPrompt = prompt,
                    tokenCounter = tokenCounter,
                    onPrefixUpdate = { prompt = prompt.copy(prefix = it) },
                    resetPromptAction = {
                        prompt = initialPrompt
                    }
                )

                MaxSegmentSettingsPart(
                    value = updatedMaxSegLength,
                    model = model,
                    onUpdate = { updatedMaxSegLength = it }
                )
            }
        },
        confirmButtonAction = {
            onConfirmPrompt(prompt.prefix)
            onConfirmMaxSegLength(updatedMaxSegLength)
        },
        dismissButtonAction = {
            prompt = initialPrompt
            updatedMaxSegLength = initialMaxSegLength
        }
    )

    TintIconButton(icon = Icons.Default.Edit, onClick = { showEditPromptDialog.value = true })
}


/**
 * Provider： 返回生成结果的函数
 * @param exportOnlyResultProvider Function0<String>
 * @param exportBothSourceAndResultProvider Function0<Pair<String, String>>
 */
@Composable
private fun ExportButton(
    remark: String = "",
    showUpdateRemarkDialog: () -> Unit,
    exportOnlyResultProvider: () -> String,
    exportBothSourceAndResultProvider: () -> String,
) {
    var needToExportTextProvider by rememberStateOf(value = { "" })
    val context = LocalContext.current
    val exportFileLauncher = rememberCreateFileLauncher { uri ->
        if (uri == null) return@rememberCreateFileLauncher
        val text = needToExportTextProvider.invoke()
        if (text.isBlank()) {
            context.toastOnUi(ResStrings.export_text_empty)
            return@rememberCreateFileLauncher
        }
        val watermark = ResStrings.export_watermark
        context.toastOnUi(ResStrings.exporting)
        uri.writeText("$text\n\n${"-".repeat(20)}\n$watermark")
        context.toastOnUi(ResStrings.export_success)
    }
    var expand by rememberStateOf(value = false)
    TintIconButton(icon = Icons.Default.SaveAlt, onClick = { expand = true }) {
        DropdownMenu(expanded = expand, onDismissRequest = { expand = false }) {
            DropdownMenuItem(
                text = { Text(text = ResStrings.export_only_result) },
                onClick = {
                    if (remark.isEmpty()) {
                        showUpdateRemarkDialog()
                        return@DropdownMenuItem
                    }
                    needToExportTextProvider = exportOnlyResultProvider
                    exportFileLauncher.launch("result_${remark}.txt")
                }
            )
            DropdownMenuItem(
                text = { Text(text = ResStrings.export_both_source_and_result) },
                onClick = {
                    if (remark.isEmpty()) {
                        showUpdateRemarkDialog()
                        return@DropdownMenuItem
                    }
                    needToExportTextProvider = exportBothSourceAndResultProvider
                    exportFileLauncher.launch("source_and_result_${remark}.txt")
                }
            )
        }
    }
}

@Composable
private fun TintIconButton(icon: ImageVector, contentDescription: String? = null, onClick: SimpleAction, extraContent: (@Composable () -> Unit)? = null) {
    IconButton(onClick = onClick) {
        FixedSizeIcon(icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.primary)
        extraContent?.invoke()
    }
}


@Composable
private fun TranslateButton(
    progress: Float = 1f,
    isTranslating: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.secondary
    val density = LocalDensity.current
    val size48dp = remember { with(density) { 48.dp.toPx() } }
    val size12dp = remember { with(density) { 12.dp.toPx() } }

    IconButton(
        modifier =
        Modifier.drawBehind {
            if (progress < 1f) drawArc(
                borderColor,
                startAngle = -90f,
                360f * progress,
                false,
                style = Stroke(width = 4f),
                topLeft = Offset(size12dp / 2, size12dp / 2),
                size = size.copy(size48dp - size12dp, size48dp - size12dp)
            )
        }, onClick = onClick
    ) {
        if (!isTranslating) FixedSizeIcon(
            Icons.Default.Done,
            contentDescription = ResStrings.start_translate,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        else FixedSizeIcon(
            painter = painterDrawableRes("ic_pause"),
            contentDescription = ResStrings.stop_translate,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

}
