package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.data.AppDatabase
import com.example.data.CopingInsight
import com.example.data.NeuroHabit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class SinaMindViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val insightDao = database.copingInsightDao()
    private val habitDao = database.neuroHabitDao()

    // 1. Language State ("en" or "fa")
    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    // 2. Circadian Sleep Theme Override (null means automatic based on system hour)
    private val _sleepModeOverride = MutableStateFlow<Boolean?>(null)
    val sleepModeOverride: StateFlow<Boolean?> = _sleepModeOverride.asStateFlow()

    // Derived: check the current system hour dynamically. Sleep mode is active between 18:00 (6 PM) and 6:00 AM.
    val isSleepMode: StateFlow<Boolean> = _sleepModeOverride.combine(
        flow {
            while (true) {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                emit(hour < 6 || hour >= 18)
                kotlinx.coroutines.delay(60000) // check every minute
            }
        }
    ) { overrideValue, isNight ->
        overrideValue ?: isNight
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // 3. Saved Insights List
    val savedInsights: StateFlow<List<CopingInsight>> = insightDao.getAllInsights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. Neuroplasticity Habits
    val habits: StateFlow<List<NeuroHabit>> = habitDao.getAllHabits()
        .onEach { list ->
            if (list.isEmpty()) {
                initializeDefaultHabits()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 5. Insight Engine State
    private val _insightLoading = MutableStateFlow(false)
    val insightLoading: StateFlow<Boolean> = _insightLoading.asStateFlow()

    private val _currentInsight = MutableStateFlow<CopingInsight?>(null)
    val currentInsight: StateFlow<CopingInsight?> = _currentInsight.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    init {
        // Initialize default habits in database if empty
        viewModelScope.launch {
            habitDao.getAllHabits().first().let {
                if (it.isEmpty()) {
                    initializeDefaultHabits()
                }
            }
        }
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun toggleSleepModeOverride() {
        val current = _sleepModeOverride.value
        _sleepModeOverride.value = when (current) {
            null -> true
            true -> false
            false -> null // Reset to auto
        }
    }

    fun toggleHabit(habit: NeuroHabit) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val completed = !habit.isCompletedToday
            val streak = if (completed) {
                // If last completed was yesterday or recently, increment streak, or simple restart
                habit.streakCount + 1
            } else {
                maxOf(0, habit.streakCount - 1)
            }
            val updated = habit.copy(
                isCompletedToday = completed,
                streakCount = streak,
                lastCompletedTimestamp = if (completed) now else habit.lastCompletedTimestamp
            )
            habitDao.updateHabit(updated)
        }
    }

    fun clearSavedInsights() {
        viewModelScope.launch(Dispatchers.IO) {
            insightDao.clearAll()
        }
    }

    fun deleteInsight(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            insightDao.deleteInsight(id)
        }
    }

    private suspend fun initializeDefaultHabits() {
        val defaultHabits = listOf(
            NeuroHabit(
                titleEn = "Morning Light Capture",
                titleFa = "دریافت نور صبحگاهی",
                descriptionEn = "Expose your eyes to 5-10 mins of natural light upon waking to stabilize cortisol secretion and sleep cycles.",
                descriptionFa = "دریافت ۵ تا ۱۰ دقیقه نور طبیعی پس از بیداری برای تنظیم هورمون کورتیزول و بهبود چرخه خواب.",
                iconName = "wb_sunny",
                streakCount = 3
            ),
            NeuroHabit(
                titleEn = "CBT Reframe Pause",
                titleFa = "تمرین بازسازی شناختی",
                descriptionEn = "Observe your negative thoughts, separate facts from feelings, and construct a balanced, logical perspective.",
                descriptionFa = "مشاهده افکار منفی شخصی، تفکیک واقعیت از احساس، و ایجاد یک نگرش متعادل و منطقی مبتنی بر شواهد.",
                iconName = "psychology",
                streakCount = 5
            ),
            NeuroHabit(
                titleEn = "Vagus Nerve Activation",
                titleFa = "تحریک عصب واگ",
                descriptionEn = "Perform 4-7-8 ratio belly breathing for 3 minutes to activate the parasympathetic nervous system and reduce heart rate.",
                descriptionFa = "انجام ۳ دقیقه تنفس دیافراگمی با الگوی ۴-۷-۸ جهت آرام کردن آمیگدال و فعال‌سازی سیستم پاراسمپاتیک.",
                iconName = "air",
                streakCount = 2
            ),
            NeuroHabit(
                titleEn = "Dopamine Digital Fast",
                titleFa = "سم‌زدایی از ابزارهای هوشمند",
                descriptionEn = "Detach entirely from screens for 20 minutes to restore neurotransmitter reserves and protect your focus spans.",
                descriptionFa = "دوری کامل از گوشی و صفحه‌نمایش‌ها به مدت ۲۰ دقیقه جهت تنظیم مجدد گیرنده‌های دوپامین ذهن.",
                iconName = "phonelink_off",
                streakCount = 4
            )
        )
        withContext(Dispatchers.IO) {
            habitDao.insertAll(defaultHabits)
        }
    }

    fun generateInsight(struggleText: String) {
        if (struggleText.isBlank()) return

        _insightLoading.value = true
        _apiError.value = null
        _currentInsight.value = null

        viewModelScope.launch {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                val lang = _language.value

                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    // Fail gracefully to fallback simulation
                    throw IllegalStateException("API key is not configured yet.")
                }

                val systemPrompt = """
                    You are an expert mental health practitioner, cognitive neuroscientist, and CBT (Cognitive Behavioral Therapy) and ACT (Acceptance and Commitment Therapy) clinical advisor.
                    The user is stating their current psychological struggle or anxiety.
                    Your entire response must be written in ${if (lang == "fa") "Farsi (Persian)" else "English"}.
                    
                    Structure your response with elegant formatting. Divide it explicitly and clearly into exactly these 3 sections with headings:
                    
                    ${if (lang == "fa") "### ۱. بررسی عصب‌شناختی (Validation)" else "### 1. Neurological Normalization"}
                    Explain why their biological hardware (amygdala threat detection, cortisol flow, neural fatigue) is behaving this way under stress. Validate their feelings deeply and warmly.
                    
                    ${if (lang == "fa") "### ۲. بازسازی شناختی (CBT Reframe)" else "### 2. Cognitive Reframe (CBT)"}
                    Identify specific optical/mental filters (catastrophizing, black-and-white, mind reading, guilt) in their thought, and present a scientific, evidence-supported realistic reframing.
                    
                    ${if (lang == "fa") "### ۳. گام‌های کوچک و عملی (ACT Micro-actions)" else "### 3. Immediate ACT Micro-steps"}
                    Give exactly 3 physical or behavioral micro-goals they can execute within the next 5 minutes to restore a sense of agency and physical calming.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = struggleText)))
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    generationConfig = GenerationConfig(temperature = 0.7f, maxOutputTokens = 1200)
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }

                val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw RuntimeException("No response text found from AI model.")

                val insight = CopingInsight(
                    struggleText = struggleText,
                    insightText = generatedText,
                    language = lang
                )

                withContext(Dispatchers.IO) {
                    insightDao.insertInsight(insight)
                }

                _currentInsight.value = insight
                _insightLoading.value = false

            } catch (e: Exception) {
                // Return descriptive error details & activate simulation trigger
                _apiError.value = e.message ?: "Unknown Connection Error"
                _insightLoading.value = false
            }
        }
    }

    fun runLocalSimulation(struggleText: String) {
        if (struggleText.isBlank()) return
        _insightLoading.value = true
        _apiError.value = null
        _currentInsight.value = null

        viewModelScope.launch {
            // Simulate 2 seconds of cognitive synthesis
            kotlinx.coroutines.delay(1800)

            val lang = _language.value
            val simulatedText = getCannedCBTDefense(struggleText, lang)

            val insight = CopingInsight(
                struggleText = struggleText,
                insightText = simulatedText,
                language = lang
            )

            withContext(Dispatchers.IO) {
                insightDao.insertInsight(insight)
            }

            _currentInsight.value = insight
            _insightLoading.value = false
        }
    }

    private fun getCannedCBTDefense(struggle: String, lang: String): String {
        return if (lang == "fa") {
            """
                ### ۱. بررسی عصب‌شناختی (Validation)
                مغز شما در حال اجرای یک فرآیند اتوماتیک برای محافظت از شماست. غده فوق کلیوی در پاسخ به احساس تهدید، مقداری کورتیزول و هورمون استرس ترشح کرده که باعث افزایش تپش قلب و ترشح آدرنالین شده است. مغز عاطفی شما (آمیگدال) فعال است و تلاش می‌کند شما را ایمن نگه دارد؛ بنابراین، واکنش استرسی شما کاملاً طبیعی و نشان‌دهنده سلامت سیستم‌های زیستی شماست.
                
                ### ۲. بازسازی شناختی (CBT Reframe)
                جمله شما دارای فیلتر شناختی "فاجعه‌سازی" (Catastrophizing) و "پیش‌بینی منفی" است. ذهن تمایل دارد بدترین سناریو را ۱۰۰٪ قطعی تصور کند.
                * تایید عینی: در واقعیت، توانایی‌ها و تجربیات گذشته خود را نادیده گرفته‌اید. احساس شکست به معنای شکست واقعی نیست. خطری که احساس می‌کنید، بازتاب استرس درون ذهن است، نه توصیف‌کننده توان بی‌پایان شما در حل مسئله. با تمرکز بر شواهد واقعی گذشته، شما توانایی مدیریت این چالش را دارید.
                
                ### ۳. گام‌های کوچک و عملی (ACT Micro-actions)
                ۱. **زمین‌گیری حسی (۵-۴-۳-۲-۱):** ۵ شیء را در اتاق ببینید، ۴ صدا را بشنوید، و ۳ حس فیزیکی لباس یا زمین را لمس کنید تا فعال‌سازی آمیگدال متوقف شود.
                ۲. **پذیرش فیزیکی:** دست خود را روی قفسه سینه یا شکم بگذارید و ۳ تنفس عمیق و آرام بکشید (دم ۴ ثانیه، بازدم ۶ ثانیه).
                ۳. **اقدام ارزشمند ریز:** کارهای بزرگ را کاملاً رها کنید. همین حالا یک استکان آب بنوشید یا یک خط کوچک برای برنامه‌ریزی روی کاغذ یادداشت کنید.
            """.trimIndent()
        } else {
            """
                ### 1. Neurological Normalization
                Your brain is currently in a high-arousal scanning survival state. A physical trigger has activated your Amygdala, prompting an instinctive release of adrenaline and cortisol. This survival mechanism directs blood flow away from the brain's prefrontal cortex (the analytical brain) toward the limbs, causing tight chest pressure and high heart rates. What you are feeling is not a sign of collapse—it is a functional biological reaction designed to keep you alert.
                
                ### 2. Cognitive Reframe (CBT)
                Your thoughts are highly influenced by the "Catastrophizing" and "Negative Projection" cognitive distortions. 
                * Realist Reframe: Emotional intensity does not govern physical destiny. Feeling anxious about tomorrow is a prediction, not a certified fact. In your past events, your brain has managed high-pressure trials with absolute survival. Challenge the idea that failure is guaranteed. Refocus on what is present, controllable, and manageable.
                
                ### 3. Immediate ACT Micro-steps
                1. **Sensory Grounding (3x3 Rule):** Look at 3 physical colors around the room, touch 3 tactile surfaces beneath you, and identify 1 surrounding aroma. This immediately returns neural control to your Prefrontal Cortex.
                2. **Slowing Respiration Wave:** Cup your hands over your solar plexus, and breathe in for 4 seconds, holding for 2 seconds, and exhaling fully for 6 seconds. Repeat 3 times to engage the vagal brake.
                3. **The Micro-Action Anchor:** Disregard the overall hurdle for the next 5 minutes. Take one tiny, single mechanical action: adjust your sitting posture, sip a small glass of water, or write down 3 words describing your core value right now.
            """.trimIndent()
        }
    }
}
