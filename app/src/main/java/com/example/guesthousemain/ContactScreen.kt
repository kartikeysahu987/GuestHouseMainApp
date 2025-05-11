import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guesthousemain.LocalThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

// Default SMTP credentials
private const val DEFAULT_SMTP_EMAIL = "aimsportal420@gmail.com"
private const val DEFAULT_SMTP_PASSWORD = "dcmsxftqpduuzwsq"

// Recipient emails
private const val DEAN_EMAIL = "2022csb1087@iitrpr.ac.in"
private const val CHAIRMAN_EMAIL = "2022csb1202@iitrpr.ac.in"
private const val IT_TEAM_EMAIL = "2022csb1097@iitrpr.ac.in"

@Composable
fun ContactScreen() {
    // Collect the current theme state
    val themeManager = LocalThemeManager.current
    val isDarkTheme by themeManager.isDarkThemeFlow.collectAsState(initial = false)
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Recipient selection state
    var selectedRecipient by remember { mutableStateOf(RecipientType.IT_TEAM) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Define theme-based colors
    val backgroundGradient = if (isDarkTheme) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF102027),
                Color(0xFF263238),
                Color(0xFF102027)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFE3F2FD),
                Color(0xFFBBDEFB),
                Color(0xFFE1F5FE)
            )
        )
    }

    val cardBackgroundColor = if (isDarkTheme) {
        Color(0xFF263238).copy(alpha = 0.9f)
    } else {
        Color.White.copy(alpha = 0.9f)
    }

    val textColor = if (isDarkTheme) Color.White else Color.Black
    val primaryColor = if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF2196F3)
    val unfocusedBorderColor = if (isDarkTheme) Color.DarkGray else Color.LightGray
    val dropdownBackgroundColor = if (isDarkTheme) Color(0xFF263238) else Color.White
    val borderColor = if (isDarkTheme) Color.DarkGray else Color.LightGray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CONTACT US",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = textColor
                )

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = {
                        Text("Your Name", color = textColor.copy(alpha = 0.6f))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Name Icon",
                            tint = primaryColor
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = unfocusedBorderColor,
                        focusedBorderColor = primaryColor,
                        cursorColor = primaryColor,
                        unfocusedTextColor = textColor,
                        focusedTextColor = textColor
                    ),
                    singleLine = true
                )

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = {
                        Text("Your Email", color = textColor.copy(alpha = 0.6f))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = primaryColor
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = unfocusedBorderColor,
                        focusedBorderColor = primaryColor,
                        cursorColor = primaryColor,
                        unfocusedTextColor = textColor,
                        focusedTextColor = textColor
                    ),
                    singleLine = true
                )

                // Recipient Dropdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = dropdownBackgroundColor
                        ),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isDropdownExpanded = !isDropdownExpanded }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Send to: ${selectedRecipient.displayName}",
                                        color = textColor
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = primaryColor
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                RecipientType.values().forEach { recipientType ->
                                    DropdownMenuItem(
                                        text = { Text(text = recipientType.displayName) },
                                        onClick = {
                                            selectedRecipient = recipientType
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Message Field
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(bottom = 16.dp),
                    placeholder = {
                        Text("Your Message", color = textColor.copy(alpha = 0.6f))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Message Icon",
                            tint = primaryColor
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = unfocusedBorderColor,
                        focusedBorderColor = primaryColor,
                        cursorColor = primaryColor,
                        unfocusedTextColor = textColor,
                        focusedTextColor = textColor
                    ),
                    maxLines = 6
                )

                // Info message for response feedback
                if (infoMessage.isNotEmpty()) {
                    Text(
                        text = infoMessage,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = if (infoMessage.contains("Failed")) Color.Red else Color(0xFF4CAF50)
                    )
                }

                // Send Button
                Button(
                    onClick = {
                        if (validateInputs(name, email, message)) {
                            isLoading = true
                            infoMessage = "Sending email..."
                            val recipientEmail = selectedRecipient.email
                            val subject = "Message from $name (To: ${selectedRecipient.displayName})"

                            coroutineScope.launch {
                                val result = sendEmailWithCoroutine(
                                    recipient = recipientEmail,
                                    subject = subject,
                                    messageBody = "From: $name\nReply Email: $email\n\n$message",
                                    displayName = selectedRecipient.displayName
                                )

                                infoMessage = result
                                isLoading = false
                            }
                        } else {
                            infoMessage = "Please fill in all fields correctly"
                        }
                    },
                    modifier = Modifier
                        .width(220.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Send Message",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// Enum for recipient types
enum class RecipientType(val displayName: String, val email: String) {
    IT_TEAM("IT Team", IT_TEAM_EMAIL),
    DEAN("Dean", DEAN_EMAIL),
    CHAIRMAN("Chairman", CHAIRMAN_EMAIL)
}

// Validate all inputs
fun validateInputs(
    name: String,
    email: String,
    message: String
): Boolean {
    // Simple validation - ensure all fields have content
    return name.isNotEmpty() &&
            email.isNotEmpty() &&
            message.isNotEmpty() &&
            email.contains("@") // Basic email validation
}

// Email sending function using coroutines
suspend fun sendEmailWithCoroutine(
    recipient: String,
    subject: String,
    messageBody: String,
    displayName: String
): String = withContext(Dispatchers.IO) {
    try {
        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.debug", "true")  // For debugging
        }

        val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(DEFAULT_SMTP_EMAIL, DEFAULT_SMTP_PASSWORD)
            }
        })

        MimeMessage(session).apply {
            setFrom(InternetAddress(DEFAULT_SMTP_EMAIL))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
            this.subject = subject
            setText(messageBody)

            Transport.send(this)
        }

        "Email sent successfully to $displayName!"
    } catch (e: Exception) {
        e.printStackTrace()
        "Failed to send email: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
    }
}