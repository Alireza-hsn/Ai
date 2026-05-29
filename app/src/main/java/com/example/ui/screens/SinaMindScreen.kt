package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CopingInsight
import com.example.data.NeuroHabit
import com.example.ui.viewmodel.SinaMindViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SinaMindScreen(
    viewModel: SinaMindViewModel = viewModel()
) {
    val lang by viewModel.language.collectAsState()
    val isSleepMode by viewModel.isSleepMode.collectAsState()
    val sleepOverride by viewModel.sleepModeOverride.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val savedInsights by viewModel.savedInsights.collectAsState()
    val currentInsight by viewModel.currentInsight.collectAsState()
    val isLoading by viewModel.insightLoading.collectAsState()
    val apiError by viewModel.apiError.collectAsState()

    var struggleInput by remember { mutableStateOf("") }
    var showApiKeyWarning by remember { mutableStateOf(true) }

    val currentLayoutDirection = if (lang == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

    // "Sleek Interface" custom colors from specs
    val darkBg = Color(0xFF1C1B1F)
    val darkSurface = Color(0xFF2B2930)
    val darkBorder = Color(0xFF49454F)
    val lightLavender = Color(0xFFD0BCFF)
    val deepPurple = Color(0xFF381E72)
    val activePillColor = Color(0xFF49454F)

    CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // Sleek Header Card with status banner + translation + bedtime toggles
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(if (isSleepMode) darkSurface else MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = if (isSleepMode) darkBorder else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isSleepMode) {
                                    if (lang == "fa") "تأمل شامگاهی آرش" else "Evening Reflection"
                                } else {
                                    if (lang == "fa") "تلاش صبحگاهی آرش" else "Sunrise Focus"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 1.5.sp
                                ),
                                color = if (isSleepMode) lightLavender.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (lang == "fa") "موتور بینش" else "Insight Engine",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSleepMode) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Translate and Bedtime controllers
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconButton(
                                onClick = {
                                    viewModel.setLanguage(if (lang == "fa") "en" else "fa")
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSleepMode) darkBorder else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Translate Switch",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleSleepModeOverride() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSleepMode) lightLavender else MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("circadian_toggle"),
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = if (isSleepMode) deepPurple else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(
                                    imageVector = if (isSleepMode) Icons.Filled.Star else Icons.Filled.Settings,
                                    contentDescription = "Bedtime Switch",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Interactive bottom nav bar as requested in Sleek Interface HTML theme.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(if (isSleepMode) darkBg else MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = if (isSleepMode) darkBorder else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home Tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.alpha(0.6f)
                        ) {
                            Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(if (lang == "fa") "خانه" else "Home", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }

                        // Insights active highlighted tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { }
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSleepMode) activePillColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "Insights",
                                    tint = if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (lang == "fa") "بینش‌ها" else "Insights",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary
                            )
                        }

                        // Progress Tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.alpha(0.6f)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = "Progress", modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(if (lang == "fa") "پیشرفت" else "Progress", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }

                        // Profile Tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.alpha(0.6f)
                        ) {
                            Icon(Icons.Filled.Face, contentDescription = "Profile", modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(if (lang == "fa") "کاربر" else "Profile", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                    }
                }
            },
            containerColor = if (isSleepMode) darkBg else MaterialTheme.colorScheme.background
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                // SECTION 1: Greeting & Cognitive Load Reduction
                item {
                    val greetingEn = "Peace be upon you, Arash."
                    val subGreetingEn = "How is your mind feeling as the day winds down?"
                    val greetingFa = "درود و آرامش بر شما، آرش گرامی."
                    val subGreetingFa = "در این ساعات پایانی روز، ذهن و کانون افکارتان در چه وضعیتی قرار دارد؟"

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = if (lang == "fa") greetingFa else greetingEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSleepMode) Color(0xFFCAC4D0) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == "fa") subGreetingFa else subGreetingEn,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Light,
                                lineHeight = 28.sp
                            ),
                            color = if (isSleepMode) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // SECTION 2: Circadian Override Status Card (Interactive indicator panel)
                item {
                    CircadianActiveIndicatorView(
                        lang = lang,
                        isSleepMode = isSleepMode,
                        sleepOverride = sleepOverride,
                        onResetOverride = { viewModel.toggleSleepModeOverride() }
                    )
                }

                // SECTION 3: The Insight Engine Input Area
                item {
                    InsightEngineSleekInputForm(
                        lang = lang,
                        isSleepMode = isSleepMode,
                        struggleInput = struggleInput,
                        isLoading = isLoading,
                        apiError = apiError,
                        onInputChange = { struggleInput = it },
                        onSubmit = { viewModel.generateInsight(struggleInput) },
                        onFallbackSimulation = { viewModel.runLocalSimulation(struggleInput) }
                    )
                }

                // SEC 3A: LOADING GRADIENT WAVE COGNITIVE SYNTHESIS
                if (isLoading) {
                    item {
                        CalmingBreathingPulseIndicator(lang = lang, isSleepMode = isSleepMode)
                    }
                }

                // SECTION 4: Neuroplasticity Actionable Solution Card
                currentInsight?.let { insight ->
                    item {
                        SleekActionableSolutionCard(
                            insight = insight,
                            lang = lang,
                            isSleepMode = isSleepMode,
                            onClearOutput = { struggleInput = "" }
                        )
                    }
                }

                // SECTION 5: Neuroplasticity Milestones (Habit Training Tracker)
                item {
                    Text(
                        text = if (lang == "fa") "نقاط عطف همگرایی اعصاب" else "Neuroplasticity Milestones",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSleepMode) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }

                items(habits) { habit ->
                    SleekNeuroplasticityHabitCard(
                        habit = habit,
                        lang = lang,
                        isSleepMode = isSleepMode,
                        onCompleteToggle = { viewModel.toggleHabit(habit) }
                    )
                }

                // SECTION 6: Security Alert Warning (Mandatory Secrets Discipline Checklist compliance)
                item {
                    SecurityDecompileNoticeView(
                        lang = lang,
                        isVisible = showApiKeyWarning,
                        onDismiss = { showApiKeyWarning = false }
                    )
                }

                // SECTION 7: CBT Historical logs (Spaced repetition cache)
                if (savedInsights.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "fa") "تاریخچه خودشناسی و درمان" else "CBT Reframing Logs",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSleepMode) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                            TextButton(
                                onClick = { viewModel.clearSavedInsights() },
                                modifier = Modifier.testTag("clear_history_button")
                            ) {
                                Text(
                                    text = if (lang == "fa") "حذف تاریخچه" else "Clean All",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    items(savedInsights) { oldInsight ->
                        SleekStoredInsightCard(
                            insight = oldInsight,
                            lang = lang,
                            isSleepMode = isSleepMode,
                            onDelete = { viewModel.deleteInsight(oldInsight.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CircadianActiveIndicatorView(
    lang: String,
    isSleepMode: Boolean,
    sleepOverride: Boolean?,
    onResetOverride: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSleepMode) Color(0xFF2B2930) else MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = if (isSleepMode) Color(0xFF49454F) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == "fa") "تنظیم سیستم گانگلیونی چشم:" else "AUTONOMIC SYNCHRONY:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = if (isSleepMode) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSleepMode) Color(0xFF381E72) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isSleepMode) "🌙 Sunset Mode" else "☀️ Sunrise Mode",
                        color = if (isSleepMode) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSleepMode) {
                    if (lang == "fa") "محافظ نوری غده پینه‌آل فعال است. تمامی طیف‌های آبی مخرب شبانه سرکوب شدند و محیط آماده آزادسازی هورمون خواب ملاتونین زیستی است."
                    else "Melanopsin-protective filters actively shield your opto-ganglion reserves, allowing natural melatonin flow into your bloodstream."
                } else {
                    if (lang == "fa") "محرک ترشح کورتیزول جهت بیداری پرنشاط صبحگاهی فعال است. نور آبی کنترل شده انرژی ذهنی را تحریک می‌کند."
                    else "Bright focus spectrum active to safely inhibit sleep-state reserves and foster cognitive alertness for your morning challenges."
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isSleepMode) Color(0xFFCAC4D0) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )
            if (sleepOverride != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (lang == "fa") "برگشت به حالت اتوماتیک بر اساس ساعت سیستم" else "Reset to automated circadian tracking",
                    color = if (isSleepMode) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable { onResetOverride() }
                        .padding(vertical = 2.dp)
                        .drawBehind {
                            drawLine(
                                color = if (isSleepMode) Color(0xFFD0BCFF) else Color(0xFF0E7A75),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                strokeWidth = 1f
                            )
                        }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightEngineSleekInputForm(
    lang: String,
    isSleepMode: Boolean,
    struggleInput: String,
    isLoading: Boolean,
    apiError: String?,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onFallbackSimulation: () -> Unit
) {
    val darkSurface = Color(0xFF2B2930)
    val darkBorder = Color(0xFF49454F)
    val lightLavender = Color(0xFFD0BCFF)
    val deepPurple = Color(0xFF381E72)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSleepMode) darkSurface else MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = if (isSleepMode) darkBorder else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("insight_engine_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (lang == "fa") "چالش جاری شما" else "CURRENT STRUGGLE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = struggleInput,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("struggle_textfield"),
                placeholder = {
                    Text(
                        text = if (lang == "fa") "مثال: بابت آزمون فردا خیلی کلافه‌ام و حس می‌کنم قراره خرابش کنم..."
                        else "Describe your stressor (e.g. 'I am feeling overwhelmed by the upcoming project deadline...')"
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (isSleepMode) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = if (isSleepMode) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoading) {
                        if (lang == "fa") "تحلیل عمیق عصبی..." else "STATUS: ANALYZING..."
                    } else if (struggleInput.isNotEmpty()) {
                        if (lang == "fa") "آماده تحلیل شناختی" else "STATUS: READY"
                    } else {
                        if (lang == "fa") "لطفاً چالش خود را بنویسید" else "STATUS: WAITING"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSleepMode) Color(0xFF938F99) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Button(
                    onClick = onSubmit,
                    enabled = struggleInput.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary,
                        contentColor = if (isSleepMode) deepPurple else Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("insights_submit_button"),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (lang == "fa") "تحلیل و بینش" else "Synthesize",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Connection Offline/API Error Simulation Trigger
            if (apiError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = if (lang == "fa") "اتصال سرور فعال نیست (استفاده از شبیه‌ساز آفلاین)" else "No Gemini API Key Configured Yet",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (lang == "fa") "کلید هوش مصنوعی در سرور یافت نشد. جهت مشاهده نحوه سازمان‌دهی مدل‌های CBT و بازسازی عصب‌شناختی، لطفاً دکمه شبیه‌ساز محلی را فشار دهید تا موتور بینش آفلاین سینا فوراً پاسخ بهینه‌سازی را مدل‌سازی کند."
                        else "Secure sandbox server is active. Please start our built-in offline Cognitive Therapy Reframer simulator to demonstrate ACT/CBT cognitive restructuring in real-time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onFallbackSimulation,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("sim_fallback_button")
                    ) {
                        Text(
                            text = if (lang == "fa") "راه‌اندازی شبیه‌ساز محلی درمان" else "Run Offline Reframer Simulator",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Suppress("AnimateAsStateLabel")
@Composable
fun CalmingBreathingPulseIndicator(lang: String, isSleepMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSleepMode) Color(0xFF2B2930) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, if (isSleepMode) Color(0xFF49454F) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (isSleepMode) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary,
                                Color.Transparent
                            )
                        )
                    )
                    .padding((pulseScale * 2).dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (lang == "fa") "عصب‌درمان هوشمند سینا در حال سنتز عاطفی... (نفس عمیقی بکشید)"
                else "Insight Engine Synthesizing Cognitive Reframe (Take a slow diaphragmatic breath...)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = if (isSleepMode) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SleekActionableSolutionCard(
    insight: CopingInsight,
    lang: String,
    isSleepMode: Boolean,
    onClearOutput: () -> Unit
) {
    val deepPurple = Color(0xFF381E72)
    val lightPurple = Color(0xFFEADDFF)
    val lightLavender = Color(0xFFD0BCFF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_solution_card_${insight.id}"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = deepPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Draw abstract sleek gradient circle decoration
                    drawCircle(
                        color = lightLavender.copy(alpha = 0.07f),
                        radius = 120.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(size.width, 0f)
                    )
                }
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header row of synthesized output
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(lightPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Face,
                                contentDescription = "Sina Synthesis icon",
                                tint = deepPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (lang == "fa") "سنتز بازسازی شناختی" else "Cognitive Reframe: ACT",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onClearOutput,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Breakdown",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (lang == "fa") "مسئله ورودی شما:" else "Current Struggle Stated:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = lightLavender.copy(alpha = 0.8f)
                )
                Text(
                    text = "\"${insight.struggleText}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
                    color = Color.White,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Iterate segments elegantly in standard "Sleek Interface" timeline steps style is mapped here:
                insight.insightText.split(Regex("###|\\d\\.")).filter { it.trim().isNotBlank() }.take(3).forEachIndexed { index, part ->
                    val lines = part.trim().split("\n")
                    val head = if (lines.isNotEmpty()) lines[0].trim() else ""
                    val chunk = if (lines.size > 1) lines.subList(1, lines.size).joinToString("\n").trim() else ""

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 4.dp, end = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(lightLavender)
                            )
                            if (index < 2) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(48.dp)
                                        .background(lightLavender.copy(alpha = 0.3f))
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (head.isNotEmpty()) head else "${if (lang == "fa") "گام" else "Step"} ${index + 1}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = lightLavender
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (chunk.isNotEmpty()) chunk else part.trim(),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
                                color = lightPurple,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "Commit to Practice" Habituation Feedback button from HTML specs
                Button(
                    onClick = onClearOutput,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightPurple,
                        contentColor = deepPurple
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Success",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == "fa") "تعهد به تمرین و خودمراقبتی" else "Commit to Practice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SleekNeuroplasticityHabitCard(
    habit: NeuroHabit,
    lang: String,
    isSleepMode: Boolean,
    onCompleteToggle: () -> Unit
) {
    val title = if (lang == "fa") habit.titleFa else habit.titleEn
    val desc = if (lang == "fa") habit.descriptionFa else habit.descriptionEn

    val lightLavender = Color(0xFFD0BCFF)
    val deepPurple = Color(0xFF381E72)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) {
                if (isSleepMode) deepPurple.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            } else {
                if (isSleepMode) Color(0xFF2B2930) else MaterialTheme.colorScheme.surface
            }
        ),
        border = if (habit.isCompletedToday) {
            BorderStroke(1.5.dp, if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary)
        } else {
            if (isSleepMode) BorderStroke(1.dp, Color(0xFF49454F)) else null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSleepMode) {
                                    lightLavender.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                }
                            )
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = when(habit.iconName) {
                                "wb_sunny" -> Icons.Filled.Star
                                "psychology" -> Icons.Filled.Face
                                else -> Icons.Filled.Info
                            },
                            contentDescription = "Milestone icon",
                            tint = if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSleepMode) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSleepMode) Color(0xFFCAC4D0).copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Streak token
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSleepMode) deepPurple.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🔥 ${if (lang == "fa") "استمرار" else "Streak"}: ${habit.streakCount} ${if (lang == "fa") "روز" else "Days"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Large feedback touch target for rewarding habit wins (dopamine hit loop)
            IconButton(
                onClick = onCompleteToggle,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (habit.isCompletedToday) {
                            if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary
                        } else {
                            if (isSleepMode) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        }
                    )
                    .size(48.dp)
                    .testTag("habit_toggle_${habit.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Complete Action Checkmark",
                    tint = if (habit.isCompletedToday) {
                        if (isSleepMode) deepPurple else Color.White
                    } else {
                        if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}

@Composable
fun SleekStoredInsightCard(
    insight: CopingInsight,
    lang: String,
    isSleepMode: Boolean,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val lightLavender = Color(0xFFD0BCFF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("stored_insight_${insight.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSleepMode) Color(0xFF2B2930) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSleepMode) BorderStroke(1.dp, Color(0xFF49454F)) else null
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "fa") "بینش مکتوب" else "Synthesized Insight Log",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSleepMode) lightLavender else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (insight.struggleText.length > 40) "${insight.struggleText.take(40)}..." else insight.struggleText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSleepMode) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_insight_${insight.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Copy Record",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "View Details Chevron",
                        tint = if (isSleepMode) Color(0xFFCAC4D0) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = if (isSleepMode) Color(0xFF49454F) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = insight.insightText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSleepMode) Color(0xFFCAC4D0) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SecurityDecompileNoticeView(
    lang: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Security Alert",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == "fa") "بیانیه امنیتی نمونه اولیه (MANDATORY WARNING)" else "Security Warning",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Clear, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Security Warning: I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. Do not share this APK file publicly or with unauthorized individuals to prevent potential misuse.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 14.sp
            )
        }
    }
}
