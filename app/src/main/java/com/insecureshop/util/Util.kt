package com.insecureshop.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.insecureshop.ProductDetail
import java.io.IOException
import java.io.InputStreamReader
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Util {

    private const val TAG = "Util"
    private const val PRODUCTS_ASSET_FILE = "products.json"
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

    private fun getProductListFromJson(context: Context): ArrayList<ProductDetail> {
        Log.d(TAG, "getProductListFromJson: Attempting to load from $PRODUCTS_ASSET_FILE")
        try {
            context.assets.open(PRODUCTS_ASSET_FILE).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val productListType = object : TypeToken<List<ProductDetail>>() {}.type
                    val products: List<ProductDetail>? = Gson().fromJson(reader, productListType)
                    Log.d(TAG, "getProductListFromJson: Parsed ${products?.size ?: 0} products from assets.")
                    products?.forEachIndexed { index, product ->
                        Log.d(TAG, "getProductListFromJson (Asset RAW): Product $index - ID: ${product.id}, Name: ${product.name}, ImageUrl: ${product.imageUrl}, Price: ${product.price}, Rating: ${product.rating}, Url: ${product.url}, Qty: ${product.qty}")
                        if (product.url == null) {
                            Log.e(TAG, "getProductListFromJson (Asset RAW EXTRA CHECK): Product $index - Name: ${product.name} HAS NULL URL!")
                        }
                    }
                    return ArrayList(products ?: emptyList())
                }
            }
        } catch (ioEx: IOException) {
            Log.e(TAG, "getProductListFromJson: IOException reading $PRODUCTS_ASSET_FILE: ${ioEx.message}", ioEx)
        } catch (jsonEx: Exception) {
            Log.e(TAG, "getProductListFromJson: Exception parsing $PRODUCTS_ASSET_FILE: ${jsonEx.message}", jsonEx)
        }
        Log.w(TAG, "getProductListFromJson: Returning empty list due to error or no products found.")
        return arrayListOf()
    }

    fun saveProductList(context: Context, productListToSave: List<ProductDetail> = getProductListFromJson(context)) {
        Log.d(TAG, "saveProductList: Attempting to save ${productListToSave.size} products.")
        productListToSave.forEachIndexed { index, product ->
            Log.d(TAG, "saveProductList (Before toJson): Product $index - ID: ${product.id}, Name: ${product.name}, ImageUrl: ${product.imageUrl}, Price: ${product.price}, Rating: ${product.rating}, Url: ${product.url}, Qty: ${product.qty}")
            if (product.url == null) {
                Log.e(TAG, "saveProductList (Before toJson EXTRA CHECK): Product $index - Name: ${product.name} HAS NULL URL!")
            }
        }
        val productJson = Gson().toJson(productListToSave)
        Log.d(TAG, "saveProductList: Generated JSON to save: $productJson")
        Prefs.getInstance(context).productList = productJson
        Log.d(TAG, "saveProductList: Product list saved to Prefs.")
    }

    fun getProductsPrefs(context: Context): List<ProductDetail> {
        val productsJsonString = Prefs.getInstance(context).productList
        Log.d(TAG, "getProductsPrefs: Raw JSON string from Prefs: '$productsJsonString'")

        if (productsJsonString.isNullOrEmpty()) {
            Log.i(TAG, "getProductsPrefs: Product list in Prefs is null or empty. Loading from assets.")
            val productsFromAssets = getProductListFromJson(context)
            if (productsFromAssets.isNotEmpty()) {
                Log.i(TAG, "getProductsPrefs: Saving freshly loaded asset list to Prefs.")
                saveProductList(context, productsFromAssets) // Сохраняем тут
            }
            return productsFromAssets
        }

        return try {
            val productListType = object : TypeToken<List<ProductDetail>>() {}.type
            val listFromPrefs: List<ProductDetail>? = Gson().fromJson(productsJsonString, productListType)
            Log.d(TAG, "getProductsPrefs: Parsed ${listFromPrefs?.size ?: 0} products from Prefs.")
            listFromPrefs?.forEachIndexed { index, product ->
                Log.d(TAG, "getProductsPrefs (Prefs Parsed): Product $index - ID: ${product.id}, Name: ${product.name}, ImageUrl: ${product.imageUrl}, Url: ${product.url}")
                if (product.url == null) {
                    Log.e(TAG, "getProductsPrefs (Prefs Parsed EXTRA CHECK): Product $index - Name: ${product.name} HAS NULL URL!")
                }
            }

            if (listFromPrefs == null) {
                Log.w(TAG, "getProductsPrefs: Parsed list from Prefs was null. Falling back to assets.")
                return getProductListFromJson(context)
            }
            listFromPrefs

        } catch (parseEx: Exception) {
            Log.e(TAG, "getProductsPrefs: Exception parsing productList from Prefs. Loading from assets. Error: ${parseEx.message}", parseEx)
            val productsFromAssetsOnError = getProductListFromJson(context)
            if (productsFromAssetsOnError.isNotEmpty()) {
                Log.i(TAG, "getProductsPrefs: Saving freshly loaded asset list to Prefs after parse error.")
                saveProductList(context, productsFromAssetsOnError)
            }
            return productsFromAssetsOnError
        }
    }

    fun updateProductItem(context: Context, updateProductDetail: ProductDetail) {
        val productList = getProductsPrefs(context).toMutableList()
        var isFound = false
        for (i in productList.indices) {
            if (productList[i].id == updateProductDetail.id) {
                productList[i] = productList[i].copy(qty = updateProductDetail.qty)
                isFound = true
                Log.d(TAG, "updateProductItem: Updated product ID ${updateProductDetail.id} to qty ${updateProductDetail.qty}")
                break
            }
        }
        if (isFound) {
            saveProductList(context, productList)
        } else {
            Log.w(TAG, "updateProductItem: Product with id=${updateProductDetail.id} not found for update.")
        }
    }

    fun getCartProduct(context: Context): ArrayList<ProductDetail> {
        val cartList = arrayListOf<ProductDetail>()
        val currentProducts = getProductsPrefs(context)
        Log.d(TAG, "getCartProduct: Processing ${currentProducts.size} products for cart.")
        for (productDetail in currentProducts) {
            if (productDetail.qty > 0) {
                cartList.add(productDetail)
            }
        }
        Log.d(TAG, "getCartProduct: Found ${cartList.size} items in cart.")
        return cartList
    }
}