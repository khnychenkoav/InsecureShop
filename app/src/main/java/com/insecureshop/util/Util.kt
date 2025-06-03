package com.insecureshop.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.insecureshop.Config
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Util {

    private const val ENCRYPTED_PREFS_FILENAME = "user_credentials_prefs"
    private const val KEY_USERNAME = "key_username"
    private const val KEY_SALT = "key_salt"
    private const val KEY_HASHED_PASSWORD = "key_hashed_password"


    internal fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            ENCRYPTED_PREFS_FILENAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }


    fun registerUser(context: Context, username: String, password: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hashedPwBytes = hashPassword(password, salt)
        val saltB64 = android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP)
        val hashB64 = android.util.Base64.encodeToString(hashedPwBytes, android.util.Base64.NO_WRAP)
        val prefs = getEncryptedPrefs(context)
        prefs.edit {
            putString(KEY_USERNAME, username)
            putString(KEY_SALT, saltB64)
            putString(KEY_HASHED_PASSWORD, hashB64)
        }
    }

    fun verifyUserNamePassword(context: Context, inputUser: String, inputPass: String): Boolean {
        val prefs = getEncryptedPrefs(context)

        val storedUsername = prefs.getString(KEY_USERNAME, null) ?: return false
        if (inputUser != storedUsername) return false

        val saltB64 = prefs.getString(KEY_SALT, null) ?: return false
        val expectedHashB64 = prefs.getString(KEY_HASHED_PASSWORD, null) ?: return false
        val saltBytes = android.util.Base64.decode(saltB64, android.util.Base64.NO_WRAP)
        val expectedHashBytes = android.util.Base64.decode(expectedHashB64, android.util.Base64.NO_WRAP)
            ?: return false
        val inputHashBytes = hashPassword(inputPass, saltBytes)
        return expectedHashBytes.contentEquals(inputHashBytes)
    }


    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 10_000, 256)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return skf.generateSecret(spec).encoded
    }


    private fun getProductList(): ArrayList<com.insecureshop.ProductDetail> {
        val domain = Config.WEBSITE_DOMAIN
        val productList = ArrayList<com.insecureshop.ProductDetail>()
        productList.add(com.insecureshop.ProductDetail(1, "Laptop", "https://images.pexels.com/photos/7974/pexels-photo.jpg", "80", 1, domain))
        productList.add(com.insecureshop.ProductDetail(2, "Hat", "https://images.pexels.com/photos/984619/pexels-photo-984619.jpeg", "10", 2, domain))
        productList.add(com.insecureshop.ProductDetail(3, "Sunglasses", "https://images.pexels.com/photos/343720/pexels-photo-343720.jpeg", "10", 4, domain))
        productList.add(com.insecureshop.ProductDetail(4, "Watch", "https://images.pexels.com/photos/277390/pexels-photo-277390.jpeg", "30", 4, domain))
        productList.add(com.insecureshop.ProductDetail(5, "Camera", "https://images.pexels.com/photos/225157/pexels-photo-225157.jpeg", "40", 2, domain))
        productList.add(com.insecureshop.ProductDetail(6, "Perfumes", "https://images.pexels.com/photos/264819/pexels-photo-264819.jpeg", "10", 2, domain))
        productList.add(com.insecureshop.ProductDetail(7, "Bagpack", "https://images.pexels.com/photos/532803/pexels-photo-532803.jpeg", "20", 2, domain))
        productList.add(com.insecureshop.ProductDetail(8, "Jacket", "https://images.pexels.com/photos/789812/pexels-photo-789812.jpeg", "20", 2, domain))
        return productList
    }

    fun saveProductList(context: Context, productList: List<com.insecureshop.ProductDetail> = getProductList()) {
        val productJson = com.google.gson.Gson().toJson(productList)
        Prefs.getInstance(context).productList = productJson
    }

    fun getProductsPrefs(context: Context): List<com.insecureshop.ProductDetail> {
        val products = Prefs.getInstance(context).productList
        return com.google.gson.Gson().fromJson(products, object : com.google.gson.reflect.TypeToken<List<com.insecureshop.ProductDetail>>() {}.type)
    }

    fun updateProductItem(context: Context, updateProductDetail: com.insecureshop.ProductDetail) {
        val productList = getProductsPrefs(context)
        for (productDetail in productList) {
            if (productDetail.id == updateProductDetail.id) {
                productDetail.qty = updateProductDetail.qty
            }
        }
        saveProductList(context, productList)
    }

    fun getCartProduct(context: Context): ArrayList<com.insecureshop.ProductDetail> {
        val cartList = arrayListOf<com.insecureshop.ProductDetail>()
        val productList = getProductsPrefs(context)
        for (productDetail in productList) {
            if (productDetail.qty > 0) {
                cartList.add(productDetail)
            }
        }
        return cartList
    }
}
