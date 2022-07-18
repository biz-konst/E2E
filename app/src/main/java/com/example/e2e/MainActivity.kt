package com.example.e2e

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // включаем рисование за системными барами
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // далее устанавливаем цвета статус-бара и бара навигации в xml (api >= 21)
        // android:statusBarColor, android:navigationBarColor
        // (либо включаем android:windowTranslucentStatus, android:windowTranslucentNavigation для api 19-20)
        // для светлого фона дополнительно включаем флаги android:windowLightStatusBar (api > 23),
        // android:windowLightNavigationBar (api >= 27)
        // если api < 27 (api < 23 для статус-бара) и светлый фон, можно установить полупрозрачные цвета,
        // или флаги windowTranslucentXXX

        setContentView(R.layout.activity_main)
    }

}