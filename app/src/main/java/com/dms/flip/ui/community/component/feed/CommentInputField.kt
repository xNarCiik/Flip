package com.dms.flip.ui.community.component.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.dms.flip.ui.theme.FlipTheme
import com.dms.flip.ui.util.LightDarkPreview

/**
 * Champ de saisie pour ajouter un commentaire.
 * Affiche un placeholder et désactive le bouton send si le texte est vide.
 */
@Composable
fun CommentInputField(
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Ajouter un commentaire..."
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = text,
            onValueChange = { newValue ->
                if (newValue.length <= 500) {
                    text = newValue
                }
            },
            modifier = Modifier
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 16.dp, vertical = 11.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (text.isNotBlank()) {
                        onSubmit(text.trim())
                        text = ""
                    }
                }
            ),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Bouton d'envoi
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSubmit(text.trim())
                    text = ""
                }
            },
            enabled = text.isNotBlank(),
            modifier = Modifier
                .background(
                    color = if (text.isNotBlank()) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    } else {
                        Color.Transparent
                    },
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Envoyer le commentaire",
                tint = if (text.isNotBlank()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                }
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun CommentInputFieldPreview() {
    FlipTheme {
        Surface {
            CommentInputField(
                onSubmit = {},
                placeholder = "Ajouter un commentaire..."
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun CommentInputFieldWithTextPreview() {
    FlipTheme {
        Surface {
            CommentInputField(
                onSubmit = { },
                placeholder = "Ajouter un commentaire..."
            )
        }
    }
}
