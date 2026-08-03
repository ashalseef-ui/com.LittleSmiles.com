package com.LittleSmiles.com.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

enum class ParentalGateStrength {
    /** Settings / parent hub — two-number compare. */
    STANDARD,
    /** Purchases / paywall — arithmetic that young kids rarely solve. */
    STRICT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentalGateDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    strength: ParentalGateStrength = ParentalGateStrength.STANDARD
) {
    when (strength) {
        ParentalGateStrength.STANDARD -> StandardGate(onDismiss, onSuccess)
        ParentalGateStrength.STRICT -> StrictGate(onDismiss, onSuccess)
    }
}

@Composable
private fun StandardGate(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val numbers = remember { List(2) { Random.nextInt(10, 99) }.distinct().let {
        if (it.size < 2) listOf(it.first(), it.first() + 7) else it
    }.shuffled() }
    val isAskingForLowest = remember { Random.nextBoolean() }
    val correctAnswer = if (isAskingForLowest) numbers.min() else numbers.max()
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Parental Control", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tap the ${if (isAskingForLowest) "LOWEST" else "HIGHEST"} value:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    numbers.forEach { number ->
                        ElevatedFilterChip(
                            selected = false,
                            onClick = {
                                if (number == correctAnswer) onSuccess() else isError = true
                            },
                            label = {
                                Text(
                                    text = number.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
                if (isError) {
                    Text(
                        text = "Incorrect, try again!",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun StrictGate(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val a = remember { Random.nextInt(14, 29) }
    val b = remember { Random.nextInt(6, 19) }
    val correct = a + b
    val options = remember {
        (listOf(correct, correct + Random.nextInt(2, 6), correct - Random.nextInt(2, 5), correct + 10)
            .distinct()
            .shuffled())
    }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Parents Only", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "What is $a + $b?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                options.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        row.forEach { value ->
                            ElevatedFilterChip(
                                selected = false,
                                onClick = {
                                    if (value == correct) onSuccess() else isError = true
                                },
                                label = {
                                    Text(
                                        text = value.toString(),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                                    )
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
                if (isError) {
                    Text(
                        text = "Not quite — ask a grown-up!",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
