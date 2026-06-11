package com.example.coficall.data

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Photo
import com.example.coficall.model.Collaborator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Base64

object ContactSyncHelper {
    const val ACCOUNT_TYPE = "com.example.coficall"
    const val ACCOUNT_NAME = "CofiCall"

    fun getOrCreateAccount(context: Context): Account {
        val accountManager = AccountManager.get(context)
        val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
        if (accounts.isNotEmpty()) {
            return accounts[0]
        }
        val account = Account(ACCOUNT_NAME, ACCOUNT_TYPE)
        accountManager.addAccountExplicitly(account, null, null)
        return account
    }

    fun removeAccountAndContacts(context: Context, callback: (Boolean) -> Unit) {
        val accountManager = AccountManager.get(context)
        val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
        if (accounts.isEmpty()) {
            callback(true)
            return
        }
        val account = accounts[0]
        accountManager.removeAccount(account, null, { future ->
            try {
                val bundle = future.result
                val success = bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
                callback(success)
            } catch (e: Exception) {
                callback(false)
            }
        }, null)
    }

    suspend fun syncContacts(context: Context, collaborators: List<Collaborator>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val account = getOrCreateAccount(context)
            val resolver = context.contentResolver

            // 1. Obter contactos existentes da conta CofiCall do Android
            // Mapeia o número de telefone (normalizado/limpo) para o ID do RawContact
            val existingContacts = HashMap<String, Long>()
            
            val rawContactUri = ContactsContract.RawContacts.CONTENT_URI
            val projection = arrayOf(ContactsContract.RawContacts._ID)
            val selection = "${ContactsContract.RawContacts.ACCOUNT_TYPE} = ? AND ${ContactsContract.RawContacts.ACCOUNT_NAME} = ?"
            val selectionArgs = arrayOf(ACCOUNT_TYPE, ACCOUNT_NAME)
            
            resolver.query(rawContactUri, projection, selection, selectionArgs, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.RawContacts._ID)
                while (cursor.moveToNext()) {
                    val rawContactId = cursor.getLong(idIndex)
                    
                    // Obter o número de telefone para este RawContact
                    val phoneUri = ContactsContract.Data.CONTENT_URI
                    val phoneProjection = arrayOf(Phone.NUMBER)
                    val phoneSelection = "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
                    val phoneSelectionArgs = arrayOf(rawContactId.toString(), Phone.CONTENT_ITEM_TYPE)
                    
                    resolver.query(phoneUri, phoneProjection, phoneSelection, phoneSelectionArgs, null)?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            val number = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(Phone.NUMBER))
                            val cleanNumber = number.replace(" ", "").replace("-", "").replace("+", "").trim()
                            if (cleanNumber.isNotEmpty()) {
                                existingContacts[cleanNumber] = rawContactId
                            }
                        }
                    }
                }
            }

            val operations = ArrayList<ContentProviderOperation>()

            // 2. Processar colaboradores
            for (colab in collaborators) {
                val rawPhone = (colab.phone ?: "").replace(" ", "").replace("-", "").replace("+", "").trim()
                if (rawPhone.isEmpty()) continue

                val rawContactId = existingContacts.remove(rawPhone)
                
                val displayName = colab.name
                val jobTitle = colab.jobTitle
                val department = colab.department
                val company = colab.businessUnit
                val emailVal = colab.email ?: ""
                val photoUrl = colab.photoUrl

                if (rawContactId == null) {
                    // INSERIR NOVO CONTACTO
                    val backRefIndex = operations.size
                    
                    // Operação 1: Inserir RawContact
                    operations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, ACCOUNT_TYPE)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, ACCOUNT_NAME)
                        .build())

                    // Operação 2: Inserir Nome
                    operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backRefIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(StructuredName.DISPLAY_NAME, displayName)
                        .build())

                    // Operação 3: Inserir Telefone
                    operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backRefIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, colab.phone)
                        .withValue(Phone.TYPE, Phone.TYPE_WORK)
                        .build())

                    // Operação 4: Inserir Email
                    if (emailVal.isNotEmpty()) {
                        operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backRefIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                            .withValue(Email.ADDRESS, emailVal)
                            .withValue(Email.TYPE, Email.TYPE_WORK)
                            .build())
                    }

                    // Operação 5: Inserir Organização (Empresa + Departamento + Cargo)
                    operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backRefIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
                        .withValue(Organization.COMPANY, company)
                        .withValue(Organization.DEPARTMENT, department)
                        .withValue(Organization.TITLE, jobTitle)
                        .withValue(Organization.TYPE, Organization.TYPE_WORK)
                        .build())

                    // Operação 6: Inserir Foto (Apenas se for Base64 local)
                    if (!photoUrl.isNullOrEmpty() && !photoUrl.startsWith("http")) {
                        try {
                            val cleanBase64 = if (photoUrl.contains(",")) photoUrl.substringAfter(",") else photoUrl
                            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backRefIndex)
                                .withValue(ContactsContract.Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                                .withValue(Photo.PHOTO, imageBytes)
                                .build())
                        } catch (e: Exception) {
                            // Ignorar erro de decode
                        }
                    }
                } else {
                    // ATUALIZAR CONTACTO EXISTENTE (Usando o rawContactId)
                    
                    // Operação 1: Atualizar Nome
                    operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection("${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?", arrayOf(rawContactId.toString(), StructuredName.CONTENT_ITEM_TYPE))
                        .withValue(StructuredName.DISPLAY_NAME, displayName)
                        .build())

                    // Operação 2: Atualizar Telefone
                    operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection("${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?", arrayOf(rawContactId.toString(), Phone.CONTENT_ITEM_TYPE))
                        .withValue(Phone.NUMBER, colab.phone)
                        .build())

                    // Operação 3: Atualizar Email
                    if (emailVal.isNotEmpty()) {
                        operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                            .withSelection("${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?", arrayOf(rawContactId.toString(), Email.CONTENT_ITEM_TYPE))
                            .withValue(Email.ADDRESS, emailVal)
                            .build())
                    }

                    // Operação 4: Atualizar Organização
                    operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection("${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?", arrayOf(rawContactId.toString(), Organization.CONTENT_ITEM_TYPE))
                        .withValue(Organization.COMPANY, company)
                        .withValue(Organization.DEPARTMENT, department)
                        .withValue(Organization.TITLE, jobTitle)
                        .build())

                    // Operação 5: Atualizar Foto
                    if (!photoUrl.isNullOrEmpty() && !photoUrl.startsWith("http")) {
                        try {
                            val cleanBase64 = if (photoUrl.contains(",")) photoUrl.substringAfter(",") else photoUrl
                            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                            
                            // Verificar se o contacto já tem uma linha de Foto
                            val photoSelection = "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
                            val photoSelectionArgs = arrayOf(rawContactId.toString(), Photo.CONTENT_ITEM_TYPE)
                            var hasPhoto = false
                            resolver.query(ContactsContract.Data.CONTENT_URI, arrayOf(ContactsContract.Data._ID), photoSelection, photoSelectionArgs, null)?.use { pc ->
                                hasPhoto = pc.count > 0
                            }

                            if (hasPhoto) {
                                operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                                    .withSelection(photoSelection, photoSelectionArgs)
                                    .withValue(Photo.PHOTO, imageBytes)
                                    .build())
                            } else {
                                operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                                    .withValue(ContactsContract.Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                                    .withValue(Photo.PHOTO, imageBytes)
                                    .build())
                            }
                        } catch (e: Exception) {
                            // Ignorar erro de decode
                        }
                    } else {
                        // Se não tem foto (ou se é link http), remove a foto existente
                        operations.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                            .withSelection("${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?", arrayOf(rawContactId.toString(), Photo.CONTENT_ITEM_TYPE))
                            .build())
                    }
                }

                // Batch com limites do Android (máximo recomendado é 500 operações por lote)
                if (operations.size >= 400) {
                    resolver.applyBatch(ContactsContract.AUTHORITY, operations)
                    operations.clear()
                }
            }

            // 3. APAGAR CONTACTOS REMOVIDOS (os que sobraram na lista existingContacts)
            for (removedId in existingContacts.values) {
                operations.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection("${ContactsContract.RawContacts._ID} = ?", arrayOf(removedId.toString()))
                    .build())

                if (operations.size >= 400) {
                    resolver.applyBatch(ContactsContract.AUTHORITY, operations)
                    operations.clear()
                }
            }

            // Aplicar o restante lote pendente
            if (operations.isNotEmpty()) {
                resolver.applyBatch(ContactsContract.AUTHORITY, operations)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CofiCallDebug", "Erro ao sincronizar contactos", e)
            Result.failure(e)
        }
    }
}
