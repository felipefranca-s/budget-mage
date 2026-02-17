package com.budgetmage.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.budgetmage.R
import com.budgetmage.data.database.entity.AccountEntity

@Composable
fun AddEditAccountDialog(
    account: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var code by remember(account) { mutableStateOf(account?.code ?: "") }
    var name by remember(account) { mutableStateOf(account?.name ?: "") }
    var codeError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val isEditMode = account != null
    val title = if (isEditMode) stringResource(R.string.edit_account) else stringResource(R.string.add_account)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase()
                        codeError = null
                    },
                    label = { Text(stringResource(R.string.account_code)) },
                    placeholder = { Text(stringResource(R.string.account_code_hint)) },
                    isError = codeError != null,
                    supportingText = codeError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text(stringResource(R.string.account_name)) },
                    placeholder = { Text(stringResource(R.string.account_name_hint)) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var hasError = false
                    if (code.isBlank()) {
                        codeError = "Código é obrigatório"
                        hasError = true
                    }
                    if (name.isBlank()) {
                        nameError = "Nome é obrigatório"
                        hasError = true
                    }
                    if (!hasError) {
                        onSave(code, name)
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
