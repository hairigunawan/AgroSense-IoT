
import com.example.it_project_2.R
import com.example.it_project_2.utils.WeatherThemeEngine
import java.util.Calendar

// Mocking R.drawable for the test
object R {
    object drawable {
        const val mode_pagi = 1
        const val mode_sore = 2
        const val mode_malam = 3
        const val mode_siang = 4
        const val cerah = 5
        const val cuaca_malam_cerah = 6
        const val theme_rain = 7
        const val hujan_ringan = 8
        const val theme_storm = 9
        const val hujan_badai = 10
    }
}

fun testThemeEngine() {
    // We can't easily mock Calendar.getInstance() without a library,
    // so let's just inspect the logic in WeatherThemeEngine.kt
    println("Analyzing WeatherThemeEngine logic...")
}

fun main() {
    testThemeEngine()
}
