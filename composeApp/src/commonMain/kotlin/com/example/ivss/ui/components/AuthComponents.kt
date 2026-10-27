package com.example.ivss.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ivss.composeapp.generated.resources.Res
import ivss.composeapp.generated.resources.atras
import ivss.composeapp.generated.resources.ivss_logo
import org.jetbrains.compose.resources.painterResource

/** Atoms **/

@Composable
fun AuthLogo(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(140.dp),
        painter = painterResource(Res.drawable.ivss_logo),
        contentDescription = "Logo de la aplicación"
    )
}

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(Res.drawable.atras),
            contentDescription = "Volver",
            modifier = Modifier.size(24.dp),
            tint = tint
        )
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPasswordField: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        visualTransformation = if (isPasswordField && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPasswordField) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val color = Color(0xFF757575)
                        val strokeWidth = 2.dp.toPx()
                        if (passwordVisible) {
                            // Ojo abierto
                            drawCircle(
                                color = color,
                                radius = size.width * 0.2f,
                                style = Stroke(width = strokeWidth)
                            )
                            val path = Path().apply {
                                moveTo(0f, size.height / 2)
                                quadraticTo(size.width / 2, 0f, size.width, size.height / 2)
                                quadraticTo(size.width / 2, size.height, 0f, size.height / 2)
                                close()
                            }
                            drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
                        } else {
                            // Ojo tachado (oculto)
                            val path = Path().apply {
                                moveTo(0f, size.height / 2)
                                quadraticTo(size.width / 2, 0f, size.width, size.height / 2)
                                quadraticTo(size.width / 2, size.height, 0f, size.height / 2)
                                close()
                            }
                            drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
                            drawLine(
                                color = Color(0xFFD32F2F),
                                start = Offset(size.width * 0.15f, size.height * 0.85f),
                                end = Offset(size.width * 0.85f, size.height * 0.15f),
                                strokeWidth = strokeWidth * 1.2f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        },
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun AuthButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(55.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Molecules **/

@Composable
fun AuthTitleGroup(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AuthFooterAction(
    instruction: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = instruction, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = actionText,
            modifier = Modifier.clickable { onActionClick() },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AuthClickableLabel(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.clickable { onClick() },
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}
