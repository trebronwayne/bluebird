package io.github.norbertweb.bluebird.editor.ui.theme
import androidx.compose.ui.graphics.Color
import io.github.norbertweb.bluebird.ui.components.DS
import io.github.norbertweb.bluebird.ui.theme.bluebirdColors
data class EditorColors(val window:Color,val chrome:Color,val editor:Color,val text:Color,val secondaryText:Color,val border:Color,val activeTab:Color,val accent:Color,val status:Color,val isDark:Boolean)
object EdThemes { fun system(dark:Boolean):EditorColors=if(dark)EditorColors(Color(0xFF202020),Color(0xFF2B2B2B),Color(0xFF1E1E1E),Color(0xFFF5F5F5),Color(0xFFC8C8C8),Color(0xFF3A3A3A),Color(0xFF1E1E1E),Color(0xFF60CDFF),Color(0xFF252525),true) else EditorColors(bluebirdColors.SurfaceLight,DS.surfaceLight,Color.White,Color(0xFF1A1A1A),Color(0xFF5F6368),Color(0xFFE0E0E0),Color.White,Color(0xFF0067C0),Color(0xFFF8F8F8),false) }
