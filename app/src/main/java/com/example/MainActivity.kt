package com.example

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
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
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()
            androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
                hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    LogoScreen(onTimeout = { showSplash = false })
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->
                        QrProApp(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun LogoScreen(onTimeout: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        delay(2000) // Show for 2 seconds
        onTimeout()
    }

    val logoBitmap = remember(context) {
        try {
            android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.qr_icon_final)?.asImageBitmap()
        } catch (e: Throwable) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFF171E3A)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (logoBitmap != null) {
                Image(
                    bitmap = logoBitmap,
                    contentDescription = "App Logo",
                    modifier = Modifier.size(180.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    androidx.compose.ui.graphics.Color(0xFF00E5FF).copy(alpha = 0.25f),
                                    androidx.compose.ui.graphics.Color.Transparent
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "App Logo",
                        tint = androidx.compose.ui.graphics.Color(0xFF00E5FF),
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "zaid",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 40.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.95f),
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.35f),
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.95f)
                        )
                    )
                )
            )
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
        // Video background
        VideoBackground()

        // Super-optimized smooth visual glow background (no stellar lines or laggy elements)
        AnimatedGlowBackground()

        // Content layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant App Header
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
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
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 10.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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

            // Input Translucent Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
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
                                label = { Text("أدخل رابط الموقع الإلكتروني", color = Color(0xFF718096)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_link")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    textDirection = TextDirection.Ltr
                                ),
                                placeholder = { Text("example.com", color = Color(0xFF4A5568)) }
                            )
                        }
                        QrType.TEXT -> {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = { Text("أدخل النص المراد ترميزه", color = Color(0xFF718096)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .testTag("input_text")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    textDirection = TextDirection.ContentOrLtr
                                ),
                                placeholder = { Text("اكتب أي معلومات هنا وسيحولها التطبيق لرمز QR...", color = Color(0xFF4A5568)) }
                            )
                        }
                        QrType.WHATSAPP -> {
                            OutlinedTextField(
                                value = whatsappPhone,
                                onValueChange = { whatsappPhone = it },
                                label = { Text("رقم الهاتف (من دون + أو أصفار دولية)", color = Color(0xFF718096)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("input_whatsapp_phone")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    textDirection = TextDirection.Ltr
                                ),
                                placeholder = { Text("966500000000", color = Color(0xFF4A5568)) }
                            )
                            OutlinedTextField(
                                value = whatsappMessage,
                                onValueChange = { whatsappMessage = it },
                                label = { Text("الرسالة التلقائية الاختيارية", color = Color(0xFF718096)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_whatsapp_msg")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    textDirection = TextDirection.Content
                                ),
                                placeholder = { Text("أهلاً بك! أريد التواصل معك بخصوص...", color = Color(0xFF4A5568)) }
                            )
                        }
                        QrType.WIFI -> {
                            OutlinedTextField(
                                value = wifiSsid,
                                onValueChange = { wifiSsid = it },
                                label = { Text("اسم الشبكة (SSID)", color = Color(0xFF718096)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("input_wifi_ssid")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    textDirection = TextDirection.ContentOrLtr
                                ),
                                placeholder = { Text("My Home Wi-Fi", color = Color(0xFF4A5568)) }
                            )
                            // Security Type selector (WPA/WEP/Open)
                            Text(
                                text = "نوع الحماية",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
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
                                            .glassMorphism(cornerRadius = 16.dp, baseAlpha = if (isSecSelected) 0.35f else 0.15f)
                                            .clickable { wifiSec = sec }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (sec) {
                                                "WPA" -> "WPA/WPA2"
                                                "WEP" -> "WEP"
                                                else -> "بدون حماية"
                                            },
                                            color = if (isSecSelected) Color.White else Color(0xFFA0AEC0),
                                            fontSize = 11.sp,
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
                                    label = { Text("كلمة مرور الشبكة", color = Color(0xFF718096)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .testTag("input_wifi_pwd")
                                        .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                    colors = customTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        color = Color.White,
                                        textDirection = TextDirection.Ltr
                                    ),
                                    placeholder = { Text("••••••••", color = Color(0xFF4A5568)) }
                                )
                            }
                        }
                        QrType.LOCATION -> {
                            OutlinedTextField(
                                value = locationName,
                                onValueChange = { locationName = it },
                                label = { Text("اسم المكان أو العنوان أو رابط خريطة", color = Color(0xFF718096)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("input_location_name")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    textDirection = TextDirection.Content
                                ),
                                placeholder = { Text("مثال: الرياض، برج خليفة، أو رابط Google Maps", color = Color(0xFF4A5568)) }
                            )

                            Text(
                                text = "أو أدخل الإحداثيات الجغرافية بدقة (اختياري):",
                                color = Color(0xFFA0AEC0),
                                fontSize = 12.sp,
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
                                    label = { Text("خط الطول (Lng)", color = Color(0xFF718096), fontSize = 11.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_location_lng")
                                        .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                    colors = customTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = LocalTextStyle.current.copy(
                                        color = Color.White,
                                        textDirection = TextDirection.Ltr
                                    ),
                                    placeholder = { Text("46.6753", color = Color(0xFF4A5568)) }
                                )

                                OutlinedTextField(
                                    value = locationLatitude,
                                    onValueChange = { locationLatitude = it },
                                    label = { Text("خط العرض (Lat)", color = Color(0xFF718096), fontSize = 11.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_location_lat")
                                        .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                    colors = customTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = LocalTextStyle.current.copy(
                                        color = Color.White,
                                        textDirection = TextDirection.Ltr
                                    ),
                                    placeholder = { Text("24.7136", color = Color(0xFF4A5568)) }
                                )
                            }
                        }
                        QrType.EMAIL -> {
                            OutlinedTextField(
                                value = emailAddress,
                                onValueChange = { emailAddress = it },
                                label = { Text("عنوان البريد الإلكتروني المستلم", color = Color(0xFF718096)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("input_email_addr")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    textDirection = TextDirection.Ltr
                                ),
                                placeholder = { Text("example@gmail.com", color = Color(0xFF4A5568)) }
                            )
                            OutlinedTextField(
                                value = emailSubject,
                                onValueChange = { emailSubject = it },
                                label = { Text("عنوان الرسالة", color = Color(0xFF718096)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("input_email_sub")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    textDirection = TextDirection.Content
                                )
                            )
                            OutlinedTextField(
                                value = emailBody,
                                onValueChange = { emailBody = it },
                                label = { Text("محتوى الرسالة", color = Color(0xFF718096)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_email_body")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    textDirection = TextDirection.Content
                                )
                            )
                        }
                        QrType.PHONE -> {
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("أدخل رقم الهاتف للاتصال المباشر", color = Color(0xFF718096)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_phone")
                                    .glassMorphism(cornerRadius = 16.dp, baseAlpha = 0.15f),
                                colors = customTextFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    textDirection = TextDirection.Ltr
                                ),
                                placeholder = { Text("+966500000000", color = Color(0xFF4A5568)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Conversion trigger Button (With robust instant auto-saving)
                    Button(
                        onClick = {
                            if (structuredContent.isNotBlank()) {
                                coroutineScope.launch {
                                    val bitmap = withContext(Dispatchers.Default) {
                                        QrGeneratorUtil.generateQrCode(structuredContent, 512)
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
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    focusedLabelColor = Color(0xFF00E5FF),
    unfocusedLabelColor = Color(0xFFE2E8F0),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF00E5FF),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
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

// Super-optimized smooth visual glow background (وموض ألوان ناعمة بدون خطوط أو أشكال هندسية بطيئة)
@Composable
fun AnimatedGlowBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "glowBgAnimation")

    val float1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaPulse1"
    )

    val float2 by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaPulse2"
    )

    val movementShift1 by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movementShift1"
    )

    val movementShift2 by infiniteTransition.animateFloat(
        initialValue = 60f,
        targetValue = -60f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movementShift2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        // Semi-transparent overlay to make text readable over the video
        drawRect(color = Color(0x33000000)) // Lightened for better glass visibility

        // First moving, pulsating neon cyan orb center-left-top
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E5FF).copy(alpha = float1.coerceIn(0f, 1f)), Color.Transparent),
                center = Offset(width * 0.18f + movementShift1, height * 0.22f + movementShift2),
                radius = (width * 1.1f).coerceAtLeast(1f)
            ),
            center = Offset(width * 0.18f + movementShift1, height * 0.22f + movementShift2),
            radius = (width * 1.1f).coerceAtLeast(1f)
        )

        // Second moving, pulsating deep royalty purple orb center-right-bottom
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF7F00FF).copy(alpha = float2.coerceIn(0f, 1f)), Color.Transparent),
                center = Offset(width * 0.82f + movementShift2, height * 0.72f - movementShift1),
                radius = (width * 1.2f).coerceAtLeast(1f)
            ),
            center = Offset(width * 0.82f + movementShift2, height * 0.72f - movementShift1),
            radius = (width * 1.2f).coerceAtLeast(1f)
        )
    }
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

// QR creation utility utilizing standard Zebra Crossing - ZXing writing matrix
object QrGeneratorUtil {
    fun generateQrCode(text: String, size: Int = 512): Bitmap? {
        if (text.isBlank()) return null
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
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
fun VideoBackground() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val textureView = TextureView(ctx)
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                var mediaPlayer: MediaPlayer? = null

                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                    try {
                        mediaPlayer = MediaPlayer.create(ctx, R.raw.bg_video)?.apply {
                            setOnErrorListener { _, _, _ -> true }
                            setSurface(Surface(surface))
                            isLooping = true
                            setVolume(0f, 0f)
                            
                            setOnVideoSizeChangedListener { _, videoWidth, videoHeight ->
                                try {
                                    val viewWidth = textureView.width.toFloat()
                                    val viewHeight = textureView.height.toFloat()
                                    if (viewWidth > 0 && viewHeight > 0 && videoWidth > 0 && videoHeight > 0) {
                                        val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()
                                        val viewRatio = viewWidth / viewHeight
                                        
                                        val scaleX: Float
                                        val scaleY: Float
                                        if (videoRatio > viewRatio) {
                                            scaleX = videoRatio / viewRatio
                                            scaleY = 1f
                                        } else {
                                            scaleX = 1f
                                            scaleY = viewRatio / videoRatio
                                        }
                                        
                                        val matrix = Matrix()
                                        matrix.setScale(
                                            scaleX, scaleY,
                                            viewWidth / 2f, viewHeight / 2f
                                        )
                                        textureView.setTransform(matrix)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            start()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    try {
                        mediaPlayer?.release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    mediaPlayer = null
                    return true
                }
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }
            textureView
        },
        update = {}
    )
}
