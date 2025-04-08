import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ContactScreen() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB),
                        Color(0xFFE1F5FE)
                    )
                )
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
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
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text("Your Name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Name Icon"
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF2196F3)
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
                    placeholder = { Text("Your Email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon"
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF2196F3)
                    ),
                    singleLine = true
                )

                // Message Field
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(bottom = 16.dp),
                    placeholder = { Text("Your Message") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Message Icon"
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF2196F3)
                    ),
                    maxLines = 6
                )

                // Info message for response feedback
                if (infoMessage.isNotEmpty()) {
                    Text(
                        text = infoMessage,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = if (infoMessage.contains("Failed")) Color.Red else Color.Green
                    )
                }

                // Send Button
                Button(
                    onClick = {
                        if (validateInputs(name, email, message)) {
                            isLoading = true
                            infoMessage = "Sending email..."
                            val subject = "Message from $name"

                            coroutineScope.launch {
                                val result = sendEmailWithCoroutine(
                                    recipient = email,
                                    subject = subject,
                                    messageBody = "From: $name\nEmail: $email\n\n$message"
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
                        containerColor = Color(0xFF2196F3)
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
    messageBody: String
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

        "Email sent successfully!"
    } catch (e: Exception) {
        e.printStackTrace()
        "Failed to send email: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
    }
}