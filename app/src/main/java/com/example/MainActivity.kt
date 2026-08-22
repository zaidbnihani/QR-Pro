package com.example

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.graphics.SurfaceTexture
import android.graphics.Matrix
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import com.example.ui.theme.MyApplicationTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

// Center Logo options for QR code customization
enum class CenterLogoType(
    val title: String,
    val icon: ImageVector?,
    val badgeColor: Color,
    val isCustom: Boolean = false
) {
    NONE("بدون شعار", Icons.Default.Block, Color(0xFF718096)),
    CUSTOM("شعار خاص", Icons.Default.AddPhotoAlternate, Color(0xFFE040FB), isCustom = true),
    WHATSAPP("واتساب", Icons.AutoMirrored.Filled.Chat, Color(0xFF25D366)),
    LINK("رابط", Icons.Default.Link, Color(0xFF007AFF)),
    LOCATION("موقع", Icons.Default.LocationOn, Color(0xFFFF3B30)),
    WIFI("واي فاي", Icons.Default.Wifi, Color(0xFF5856D6)),
    PHONE("اتصال", Icons.Default.Phone, Color(0xFF34C759)),
    EMAIL("إيميل", Icons.Default.Email, Color(0xFFFF9500)),
    STAR("نجمة", Icons.Default.Star, Color(0xFFFFCC00))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable Fullscreen Immersive Mode
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MyApplicationTheme {
                // Auto update check from GitHub Releases
                com.example.updater.AutoUpdateChecker(currentVersionName = com.example.BuildConfig.VERSION_NAME)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    QrProApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Custom QR types for structured input
enum class QrType(
    val titleAr: String,
    val icon: ImageVector,
    val description: String
) {
    LINK("رابط ويب", Icons.Default.Link, "عنوان URL يبدأ بـ https://"),
    TEXT("نص عام", Icons.Default.Description, "نص عادي، ملاحظات، أو معلومات عامة"),
    WHATSAPP("واتساب", Icons.AutoMirrored.Filled.Chat, "محادثة فورية مع رسالة تلقائية"),
    WIFI("واي فاي", Icons.Default.Wifi, "بطاقة رمز لشبكة لا سلكية"),
    LOCATION("الموقع الجغرافي", Icons.Default.LocationOn, "موقع على الخريطة أو إحداثيات GPS"),
    EMAIL("بريد إلكتروني", Icons.Default.Email, "إرسال بريد بتفاصيل جاهزة"),
    PHONE("رقم هاتف", Icons.Default.Phone, "اتصال هاتفي مباشر")
}

@Composable
fun QrProApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // Navigation and generic inputs
    var selectedType by remember { mutableStateOf(QrType.LINK) }
    
    // Core states
    var textInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    
    // WhatsApp States
    var whatsappPhone by remember { mutableStateOf("") }
    var whatsappMessage by remember { mutableStateOf("") }
    
    // Email States
    var emailAddress by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }
    
    // Wi-Fi States
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiSec by remember { mutableStateOf("WPA") }

    // Location States & Auto-Fill Permission Handling
    var locationName by remember { mutableStateOf("") }
    var locationLatitude by remember { mutableStateOf("") }
    var locationLongitude by remember { mutableStateOf("") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            fetchCurrentLocation(context) { lat, lng ->
                locationLatitude = String.format(java.util.Locale.US, "%.6f", lat)
                locationLongitude = String.format(java.util.Locale.US, "%.6f", lng)
                if (locationName.isBlank()) {
                    locationName = "موقعي الحالي"
                }
            }
        } else {
            Toast.makeText(context, "تم رفض إذن الوصول للموقع الجغرافي", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestAndAutoFillLocation() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fetchCurrentLocation(context) { lat, lng ->
                locationLatitude = String.format(java.util.Locale.US, "%.6f", lat)
                locationLongitude = String.format(java.util.Locale.US, "%.6f", lng)
                if (locationName.isBlank()) {
                    locationName = "موقعي الحالي"
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Automatically trigger location request when Location type is selected
    LaunchedEffect(selectedType) {
        if (selectedType == QrType.LOCATION) {
            requestAndAutoFillLocation()
        }
    }

    // Result QR Code state
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var lastValueGenerated by remember { mutableStateOf("") }

    // Center Logo state (شعار في منتصف رمز QR)
    var isCenterLogoExpanded by remember { mutableStateOf(false) }
    var selectedLogoType by remember { mutableStateOf(CenterLogoType.NONE) }
    var customLogoUri by remember { mutableStateOf<Uri?>(null) }
    var customLogoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val customLogoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            customLogoUri = uri
            selectedLogoType = CenterLogoType.CUSTOM
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    customLogoBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "تعذر قراءة الصورة المحددة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Alert Notification Banner states
    var showSuccessAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    // Pulse notification disappearance
    LaunchedEffect(showSuccessAlert) {
        if (showSuccessAlert) {
            kotlinx.coroutines.delay(3200)
            showSuccessAlert = false
        }
    }

    // Direct and Safe Auto-Save implementation (حفظ الرمز تلقائياً وبأمان في الاستوديو)
    fun saveBitmapDirectly(bmp: Bitmap, isAutoSave: Boolean = false, onRequestPermission: () -> Unit) {
        coroutineScope.launch {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                
                if (hasPermission) {
                    val uri = withContext(Dispatchers.IO) { saveQrToDevice(context, bmp) }
                    if (uri != null) {
                        alertMessage = "تم حفظ رمز QR في معرض الصور بنجاح!"
                        showSuccessAlert = true
                    } else {
                        Toast.makeText(context, "فشل حفظ الرمز تلقائياً!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    onRequestPermission()
                }
            } else {
                val uri = withContext(Dispatchers.IO) { saveQrToDevice(context, bmp) }
                if (uri != null) {
                    alertMessage = "تم حفظ رمز QR في معرض الصور بنجاح!"
                    showSuccessAlert = true
                } else {
                    Toast.makeText(context, "فشل حفظ الملف!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Permission launcher for SDK <= 28
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            generatedBitmap?.let { bmp ->
                saveBitmapDirectly(bmp, isAutoSave = false) {}
            }
        } else {
            Toast.makeText(context, "يجب الموافقة على إذن الحفظ لتنزيل الصورة!", Toast.LENGTH_LONG).show()
        }
    }

    // Interactive Action to Download
    fun onDownloadClick() {
        generatedBitmap?.let { bmp ->
            saveBitmapDirectly(bmp, isAutoSave = false) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    // Interactive action to share QR
    fun onShareClick() {
        generatedBitmap?.let { bmp ->
            shareQrCode(context, bmp)
        }
    }

    // Compute active code representation based on selected tab data
    val structuredContent = remember(
        selectedType, textInput, phoneInput,
        whatsappPhone, whatsappMessage,
        emailAddress, emailSubject, emailBody,
        wifiSsid, wifiPassword, wifiSec,
        locationName, locationLatitude, locationLongitude
    ) {
        when (selectedType) {
            QrType.LINK -> {
                if (textInput.isBlank()) ""
                else if (!textInput.startsWith("http://") && !textInput.startsWith("https://")) {
                    "https://$textInput"
                } else {
                    textInput
                }
            }
            QrType.TEXT -> textInput
            QrType.WHATSAPP -> {
                if (whatsappPhone.isBlank()) ""
                else {
                    val cleanPhone = whatsappPhone.filter { it.isDigit() }
                    val encodedMsg = Uri.encode(whatsappMessage)
                    if (encodedMsg.isNotEmpty()) {
                        "https://wa.me/$cleanPhone?text=$encodedMsg"
                    } else {
                        "https://wa.me/$cleanPhone"
                    }
                }
            }
            QrType.WIFI -> {
                if (wifiSsid.isBlank()) ""
                else {
                    val passPart = if (wifiSec == "nopass") "" else "P:$wifiPassword;"
                    "WIFI:S:$wifiSsid;T:$wifiSec;${passPart};"
                }
            }
            QrType.LOCATION -> {
                val cleanLat = locationLatitude.trim()
                val cleanLng = locationLongitude.trim()
                val cleanName = locationName.trim()
                when {
                    cleanLat.isNotEmpty() && cleanLng.isNotEmpty() -> {
                        if (cleanName.isNotEmpty()) {
                            "https://maps.google.com/?q=$cleanLat,$cleanLng&label=${Uri.encode(cleanName)}"
                        } else {
                            "https://maps.google.com/?q=$cleanLat,$cleanLng"
                        }
                    }
                    cleanName.isNotEmpty() -> {
                        if (cleanName.startsWith("http://") || cleanName.startsWith("https://") || cleanName.startsWith("geo:")) {
                            cleanName
                        } else {
                            "https://maps.google.com/?q=${Uri.encode(cleanName)}"
                        }
                    }
                    else -> ""
                }
            }
            QrType.EMAIL -> {
                if (emailAddress.isBlank()) ""
                else {
                    val encodedSubject = Uri.encode(emailSubject)
                    val encodedBody = Uri.encode(emailBody)
                    "mailto:$emailAddress?subject=$encodedSubject&body=$encodedBody"
                }
            }
            QrType.PHONE -> {
                if (phoneInput.isBlank()) ""
                else "tel:$phoneInput"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Custom background image
        AppBackground()

        // Content layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant App Header
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "QR Logo",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "QR PRO",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }

            // Horizontal Selector Tab for Customizable Tokens "تخصيص الرموز"
            Text(
                text = "اختر نوع الرمز لتخصيصه",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 10.dp)
            )

            // Horizontal Selector Tab for Customizable Tokens "تخصيص الرموز" with soft edge transparency
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    reverseLayout = true // Right-to-Left alignment feel
                ) {
                    items(QrType.values()) { type ->
                        val isSelected = selectedType == type
                        val tintColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF718096)
                        val borderColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF2D3748)

                        Box(
                            modifier = Modifier
                                .testTag("type_tab_${type.name.lowercase()}")
                                .glassMorphism(cornerRadius = 24.dp, baseAlpha = if (isSelected) 0.45f else 0.15f)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable {
                                    selectedType = type
                                    keyboardController?.hide()
                                    if (type == QrType.LOCATION) {
                                        requestAndAutoFillLocation()
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = type.titleAr,
                                    color = if (isSelected) Color.White else Color(0xFFA0AEC0),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = type.titleAr,
                                    tint = tintColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Left edge smooth transparent fade
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(32.dp)
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xE607080C),
                                    Color(0x8007080C),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Right edge smooth transparent fade
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(32.dp)
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x8007080C),
                                    Color(0xE607080C)
                                )
                            )
                        )
                )
            }

            // Input Translucent Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .glassMorphism(cornerRadius = 32.dp, baseAlpha = 0.25f),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedType.description,
                            color = Color(0xFFA0AEC0),
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = selectedType.icon,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contextual Inputs based on selection
                    when (selectedType) {
                        QrType.LINK -> {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = { Text("أدخل رابط الموقع الإلكتروني", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_link")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDirection = TextDirection.Ltr
                                ),
                                placeholder = { Text("https://example.com", color = Color(0xFF94A3B8), fontSize = 14.sp) }
                            )
                        }
                        QrType.TEXT -> {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = { Text("أدخل النص المراد ترميزه", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .testTag("input_text")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDirection = TextDirection.ContentOrLtr
                                ),
                                placeholder = { Text("اكتب أي معلومات هنا وسيحولها التطبيق لرمز QR...", color = Color(0xFF94A3B8), fontSize = 14.sp) }
                            )
                        }
                        QrType.WHATSAPP -> {
                            OutlinedTextField(
                                value = whatsappPhone,
                                onValueChange = { whatsappPhone = it },
                                label = { Text("رقم الهاتف (مع رمز الدولة بدون +)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("input_whatsapp_phone")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDirection = TextDirection.Ltr
                                ),
                                placeholder = { Text("966500000000", color = Color(0xFF94A3B8), fontSize = 14.sp) }
                            )
                            OutlinedTextField(
                                value = whatsappMessage,
                                onValueChange = { whatsappMessage = it },
                                label = { Text("الرسالة التلقائية (اختياري)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_whatsapp_msg")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDirection = TextDirection.Content
                                ),
                                placeholder = { Text("أهلاً بك! أريد التواصل معك بخصوص...", color = Color(0xFF94A3B8), fontSize = 14.sp) }
                            )
                        }
                        QrType.WIFI -> {
                            OutlinedTextField(
                                value = wifiSsid,
                                onValueChange = { wifiSsid = it },
                                label = { Text("اسم شبكة الواي فاي (SSID)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("input_wifi_ssid")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDirection = TextDirection.ContentOrLtr
                                ),
                                placeholder = { Text("My Home Wi-Fi", color = Color(0xFF94A3B8), fontSize = 14.sp) }
                            )
                            // Security Type selector (WPA/WEP/Open)
                            Text(
                                text = "نوع الحماية والتشفير:",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("WPA", "WEP", "nopass").forEach { sec ->
                                    val isSecSelected = wifiSec == sec
                                    val buttonBg = if (isSecSelected) Color(0xFF1E2640) else Color(0xFF111422)
                                    val secBorder = if (isSecSelected) Color(0xFF00E5FF) else Color(0xFF252D3F)
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .glassMorphism(cornerRadius = 16.dp, baseAlpha = if (isSecSelected) 0.4f else 0.2f)
                                            .clickable { wifiSec = sec }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (sec) {
                                                "WPA" -> "WPA/WPA2"
                                                "WEP" -> "WEP"
                                                else -> "بدون كلمة سر"
                                            },
                                            color = if (isSecSelected) Color(0xFF00E5FF) else Color(0xFFE2E8F0),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = wifiSec != "nopass",
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                OutlinedTextField(
                                    value = wifiPassword,
                                    onValueChange = { wifiPassword = it },
                                    label = { Text("كلمة مرور الشبكة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .testTag("input_wifi_pwd")
                                        .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                    colors = customTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        textDirection = TextDirection.Ltr
                                    ),
                                    placeholder = { Text("••••••••", color = Color(0xFF94A3B8), fontSize = 14.sp) }
                                )
                            }
                        }
                        QrType.LOCATION -> {
                            // Section header for clarity
                            Text(
                                text = "بيانات الموقع الجغرافي الخريطة",
                                color = Color(0xFF00E5FF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = locationName,
                                onValueChange = { locationName = it },
                                label = { Text("اسم المكان أو رابط Google Maps", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("input_location_name")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDirection = TextDirection.Content
                                ),
                                placeholder = { Text("مثال: الرياض، برج خليفة، أو رابط الخريطة", color = Color(0xFF94A3B8), fontSize = 14.sp) }
                            )

                            Text(
                                text = "أو أدخل الإحداثيات الجغرافية المباشرة (اختياري):",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = locationLongitude,
                                    onValueChange = { locationLongitude = it },
                                    label = { Text("خط الطول (Lng)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_location_lng")
                                        .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                    colors = customTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = LocalTextStyle.current.copy(
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        textDirection = TextDirection.Ltr
                                    ),
                                    placeholder = { Text("46.6753", color = Color(0xFF94A3B8), fontSize = 13.sp) }
                                )

                                OutlinedTextField(
                                    value = locationLatitude,
                                    onValueChange = { locationLatitude = it },
                                    label = { Text("خط العرض (Lat)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_location_lat")
                                        .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                    colors = customTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = LocalTextStyle.current.copy(
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        textDirection = TextDirection.Ltr
                                    ),
                                    placeholder = { Text("24.7136", color = Color(0xFF94A3B8), fontSize = 13.sp) }
                                )
                            }
                        }
                        QrType.EMAIL -> {
                            // Clear separated email header
                            Text(
                                text = "بيانات البريد الإلكتروني المباشر",
                                color = Color(0xFF00E5FF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = emailAddress,
                                onValueChange = { emailAddress = it },
                                label = { Text("عنوان البريد الإلكتروني المستلم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("input_email_addr")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDirection = TextDirection.Ltr
                                ),
                                placeholder = { Text("example@gmail.com", color = Color(0xFF94A3B8), fontSize = 14.sp) }
                            )
                            OutlinedTextField(
                                value = emailSubject,
                                onValueChange = { emailSubject = it },
                                label = { Text("عنوان الرسالة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("input_email_sub")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDirection = TextDirection.Content
                                ),
                                placeholder = { Text("موضوع الإيميل", color = Color(0xFF94A3B8), fontSize = 14.sp) }
                            )
                            OutlinedTextField(
                                value = emailBody,
                                onValueChange = { emailBody = it },
                                label = { Text("محتوى أو نص الرسالة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .testTag("input_email_body")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDirection = TextDirection.Content
                                ),
                                placeholder = { Text("اكتب تفاصيل الرسالة هنا...", color = Color(0xFF94A3B8), fontSize = 14.sp) }
                            )
                        }
                        QrType.PHONE -> {
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("أدخل رقم الهاتف للاتصال المباشر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_phone")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.25f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textDirection = TextDirection.Ltr
                                ),
                                placeholder = { Text("+966500000000", color = Color(0xFF94A3B8), fontSize = 15.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Center Logo Selection Row (مش مربع - خيار شعار بالمنتصف بنص واضح وجنبه دائرة صغيرة مع علامة صح)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isCenterLogoExpanded = !isCenterLogoExpanded
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .testTag("toggle_center_logo_section")
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "شعار في منتصف الرمز",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isCenterLogoExpanded) {
                                        if (selectedLogoType == CenterLogoType.CUSTOM && customLogoBitmap != null) "مُفعّل: شعار خاص مخصص"
                                        else if (selectedLogoType != CenterLogoType.NONE) "مُفعّل: ${selectedLogoType.title}"
                                        else "اختر شعاراً أو صورة لوضعها بالوسط"
                                    } else {
                                        "إضافة لوجو أو أيقونة في مركز الكود"
                                    },
                                    color = if (isCenterLogoExpanded) Color(0xFF00E5FF) else Color(0xFFA0AEC0),
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Small circle with checkmark (دائرة صغيرة وعليها صح عند الضغط عليها)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCenterLogoExpanded) Color(0xFF00E5FF)
                                        else Color.White.copy(alpha = 0.12f)
                                    )
                                    .border(
                                        width = if (isCenterLogoExpanded) 2.dp else 1.5.dp,
                                        color = if (isCenterLogoExpanded) Color(0xFF00E5FF) else Color(0xFF718096),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCenterLogoExpanded) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "مفعّل",
                                        tint = Color(0xFF07080C),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Expanded Logo Options
                        AnimatedVisibility(
                            visible = isCenterLogoExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 4.dp)
                            ) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(CenterLogoType.values()) { logoType ->
                                        val isSelected = selectedLogoType == logoType
                                        val borderBrush = if (isSelected) {
                                            Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF7F00FF)))
                                        } else {
                                            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.06f)))
                                        }

                                        Surface(
                                            onClick = {
                                                if (logoType.isCustom) {
                                                    try {
                                                        customLogoPickerLauncher.launch("image/*")
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        Toast.makeText(context, "تعذر فتح معرض الصور", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    selectedLogoType = logoType
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.40f),
                                            border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderBrush),
                                            modifier = Modifier.testTag("logo_chip_${logoType.name}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                            ) {
                                                if (logoType.isCustom && customLogoBitmap != null) {
                                                    Image(
                                                        bitmap = customLogoBitmap!!.asImageBitmap(),
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                    )
                                                } else if (logoType.icon != null) {
                                                    Icon(
                                                        imageVector = logoType.icon,
                                                        contentDescription = null,
                                                        tint = if (isSelected) Color(0xFF00E5FF) else logoType.badgeColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (logoType.isCustom && customLogoBitmap != null) "شعارك" else logoType.title,
                                                    color = if (isSelected) Color.White else Color(0xFFCBD5E0),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }

                                if (selectedLogoType == CenterLogoType.CUSTOM && customLogoBitmap != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                    ) {
                                        TextButton(
                                            onClick = {
                                                try {
                                                    customLogoPickerLauncher.launch("image/*")
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = Color(0xFF00E5FF),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "تغيير الصورة",
                                                color = Color(0xFF00E5FF),
                                                fontSize = 11.sp
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                selectedLogoType = CenterLogoType.NONE
                                                customLogoBitmap = null
                                                customLogoUri = null
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                tint = Color(0xFFFF5252),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "إلغاء الشعار",
                                                color = Color(0xFFFF5252),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Conversion trigger Button (With robust instant auto-saving)
                    Button(
                        onClick = {
                            if (structuredContent.isNotBlank()) {
                                coroutineScope.launch {
                                    val bitmap = withContext(Dispatchers.Default) {
                                        val activeLogoType = if (isCenterLogoExpanded) selectedLogoType else CenterLogoType.NONE
                                        val logoToEmbed: Bitmap? = if (activeLogoType == CenterLogoType.CUSTOM) {
                                            customLogoBitmap
                                        } else if (activeLogoType != CenterLogoType.NONE) {
                                            renderPresetLogoBitmap(activeLogoType, 160)
                                        } else {
                                            null
                                        }
                                        QrGeneratorUtil.generateQrCode(
                                            text = structuredContent,
                                            size = 600,
                                            centerLogoBitmap = logoToEmbed,
                                            centerLogoType = activeLogoType
                                        )
                                    }
                                    if (bitmap != null) {
                                        generatedBitmap = bitmap
                                        lastValueGenerated = structuredContent
                                        keyboardController?.hide()
                                        // Robust instant auto-saving inside device memory upon click
                                        try {
                                            saveBitmapDirectly(bitmap, isAutoSave = true) {
                                                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Failed auto-saving newly created QR bitmap", e)
                                        }
                                    } else {
                                        Toast.makeText(context, "فشل إنشاء الرمز!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "الرجاء تعبئة الحقول المطلوبة أولاً!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("convert_button")
                            .glassMorphism(cornerRadius = 24.dp, baseAlpha = 0.4f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color(0xFF07080C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تحويل إلى رمز QR",
                                color = Color(0xFF07080C),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Generated QR Display Preview Card removed to save immediately in background instead of showing a preview card

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Elegant Animated Success Notification Toast at the Bottom
        AnimatedVisibility(
            visible = showSuccessAlert,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .glassMorphism(cornerRadius = 24.dp, baseAlpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = alertMessage,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun borderStrokeColors(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.2.dp, color)
}

@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00E5FF).copy(alpha = 0.8f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
    focusedLabelColor = Color(0xFF00E5FF),
    unfocusedLabelColor = Color(0xFFE2E8F0),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF00E5FF),
    focusedPlaceholderColor = Color(0xFFCBD5E0),
    unfocusedPlaceholderColor = Color(0xFFCBD5E0),
    focusedContainerColor = Color.Black.copy(alpha = 0.35f),
    unfocusedContainerColor = Color.Black.copy(alpha = 0.25f)
)

fun Modifier.glassMorphism(
    cornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    baseAlpha: Float = 0.2f,
    shineDuration: Int = 8000
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "glassShine")
    val shineOffset by transition.animateFloat(
        initialValue = -500f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(shineDuration, easing = LinearEasing, delayMillis = 1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine"
    )

    this
        .shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = Color.Black.copy(alpha = 0.2f),
            spotColor = Color.Black.copy(alpha = 0.1f)
        )
        .clip(RoundedCornerShape(cornerRadius))
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = baseAlpha + 0.03f),
                    Color.White.copy(alpha = baseAlpha)
                )
            )
        )
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                start = Offset(shineOffset, shineOffset),
                end = Offset(shineOffset + 300f, shineOffset + 300f)
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.03f)
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
}

// Static clean contrast overlay for video background (0% CPU/GPU overhead)
@Composable
fun VideoOverlayBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x80000000),
                        Color(0xA006080E),
                        Color(0xC5000000)
                    )
                )
            )
    )
}



// Location retrieval utility using LocationManager
fun fetchCurrentLocation(context: Context, onLocationRetrieved: (Double, Double) -> Unit) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            ?: return

        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return

        var bestLocation: android.location.Location? = null
        val providers = listOf(
            android.location.LocationManager.GPS_PROVIDER,
            android.location.LocationManager.NETWORK_PROVIDER,
            android.location.LocationManager.PASSIVE_PROVIDER
        )

        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (bestLocation != null) {
            onLocationRetrieved(bestLocation.latitude, bestLocation.longitude)
        } else {
            val activeProvider = providers.firstOrNull {
                try { locationManager.isProviderEnabled(it) } catch (e: Exception) { false }
            }
            if (activeProvider != null) {
                val listener = object : android.location.LocationListener {
                    override fun onLocationChanged(loc: android.location.Location) {
                        onLocationRetrieved(loc.latitude, loc.longitude)
                        try {
                            locationManager.removeUpdates(this)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                locationManager.requestLocationUpdates(
                    activeProvider,
                    1000L,
                    0f,
                    listener,
                    android.os.Looper.getMainLooper()
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Render clean, high-resolution badge bitmaps for preset center logos
fun renderPresetLogoBitmap(logoType: CenterLogoType, size: Int = 160): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val center = size / 2f
    val radius = size * 0.44f

    when (logoType) {
        CenterLogoType.WHATSAPP -> {
            paint.color = android.graphics.Color.parseColor("#25D366")
            canvas.drawCircle(center, center, radius, paint)
            paint.color = android.graphics.Color.WHITE
            paint.textSize = size * 0.46f
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD
            val yPos = (center - (paint.descent() + paint.ascent()) / 2)
            canvas.drawText("✆", center, yPos, paint)
        }
        CenterLogoType.LOCATION -> {
            paint.color = android.graphics.Color.parseColor("#FF3B30")
            canvas.drawCircle(center, center, radius, paint)
            paint.color = android.graphics.Color.WHITE
            paint.textSize = size * 0.48f
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD
            val yPos = (center - (paint.descent() + paint.ascent()) / 2)
            canvas.drawText("📍", center, yPos, paint)
        }
        CenterLogoType.LINK -> {
            paint.color = android.graphics.Color.parseColor("#007AFF")
            canvas.drawCircle(center, center, radius, paint)
            paint.color = android.graphics.Color.WHITE
            paint.textSize = size * 0.46f
            paint.textAlign = Paint.Align.CENTER
            val yPos = (center - (paint.descent() + paint.ascent()) / 2)
            canvas.drawText("🔗", center, yPos, paint)
        }
        CenterLogoType.WIFI -> {
            paint.color = android.graphics.Color.parseColor("#5856D6")
            canvas.drawCircle(center, center, radius, paint)
            paint.color = android.graphics.Color.WHITE
            paint.textSize = size * 0.46f
            paint.textAlign = Paint.Align.CENTER
            val yPos = (center - (paint.descent() + paint.ascent()) / 2)
            canvas.drawText("📶", center, yPos, paint)
        }
        CenterLogoType.PHONE -> {
            paint.color = android.graphics.Color.parseColor("#34C759")
            canvas.drawCircle(center, center, radius, paint)
            paint.color = android.graphics.Color.WHITE
            paint.textSize = size * 0.46f
            paint.textAlign = Paint.Align.CENTER
            val yPos = (center - (paint.descent() + paint.ascent()) / 2)
            canvas.drawText("📞", center, yPos, paint)
        }
        CenterLogoType.EMAIL -> {
            paint.color = android.graphics.Color.parseColor("#FF9500")
            canvas.drawCircle(center, center, radius, paint)
            paint.color = android.graphics.Color.WHITE
            paint.textSize = size * 0.46f
            paint.textAlign = Paint.Align.CENTER
            val yPos = (center - (paint.descent() + paint.ascent()) / 2)
            canvas.drawText("✉", center, yPos, paint)
        }
        CenterLogoType.STAR -> {
            paint.color = android.graphics.Color.parseColor("#FFCC00")
            canvas.drawCircle(center, center, radius, paint)
            paint.color = android.graphics.Color.WHITE
            paint.textSize = size * 0.50f
            paint.textAlign = Paint.Align.CENTER
            val yPos = (center - (paint.descent() + paint.ascent()) / 2)
            canvas.drawText("★", center, yPos, paint)
        }
        else -> {}
    }
    return bitmap
}

// QR creation utility utilizing standard Zebra Crossing - ZXing writing matrix
object QrGeneratorUtil {
    fun generateQrCode(
        text: String,
        size: Int = 600,
        centerLogoBitmap: Bitmap? = null,
        centerLogoType: CenterLogoType = CenterLogoType.NONE
    ): Bitmap? {
        if (text.isBlank()) return null
        return try {
            val hints = mutableMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1
            if (centerLogoType != CenterLogoType.NONE || centerLogoBitmap != null) {
                hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            } else {
                hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x, y,
                        if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }

            // Overlay Center Logo badge if selected
            if (centerLogoType != CenterLogoType.NONE || centerLogoBitmap != null) {
                val canvas = android.graphics.Canvas(bitmap)
                val logoAreaSize = (width * 0.23f).toInt()
                val left = (width - logoAreaSize) / 2f
                val top = (height - logoAreaSize) / 2f
                val right = left + logoAreaSize
                val bottom = top + logoAreaSize
                val rectF = RectF(left, top, right, bottom)
                val cornerRadius = logoAreaSize * 0.28f

                // Draw crisp white background card in center
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bgPaint)

                // Draw subtle modern border around center logo badge
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.rgb(226, 232, 240)
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)

                // Draw the logo content inside
                val innerPadding = logoAreaSize * 0.12f
                val innerRectF = RectF(
                    left + innerPadding,
                    top + innerPadding,
                    right - innerPadding,
                    bottom - innerPadding
                )

                if (centerLogoBitmap != null) {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    val srcRect = Rect(0, 0, centerLogoBitmap.width, centerLogoBitmap.height)
                    val dstRect = Rect(
                        innerRectF.left.toInt(),
                        innerRectF.top.toInt(),
                        innerRectF.right.toInt(),
                        innerRectF.bottom.toInt()
                    )
                    canvas.drawBitmap(centerLogoBitmap, srcRect, dstRect, paint)
                }
            }

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// MediaStore Content values handling inside modern SDKs or local fallback
fun saveQrToDevice(context: Context, bitmap: Bitmap): Uri? {
    val filename = "QR_${System.currentTimeMillis()}.png"
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QR_Pro")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                }
                imageUri
            } else {
                null
            }
        } else {
            // Older versions: save inside public pictures
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
            val folder = File(imagesDir, "QR_Pro")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val file = File(folder, filename)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
            }
            val uri = Uri.fromFile(file)
            
            // Broadcast media scanner so media scanner updates pictures database
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
            uri
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Save & share system launcher builder
fun shareQrCode(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "qr_share.png")
        
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        if (contentUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, "image/png")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
            }
            val chooserIntent = Intent.createChooser(shareIntent, "مشاركة الرمز").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun AppBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_custom),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark glass vignette & tint overlay to ensure high readability, vibrant glow, and contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66000000),
                            Color(0x8007080C),
                            Color(0xB3000000)
                        )
                    )
                )
        )
    }
}
