package no.nav.aap.api.mellomlagring

import no.nav.aap.api.felles.Fødselsnummer
import org.springframework.util.StringUtils.isEmpty
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class Krypto(passphrase: String, fnr: Fødselsnummer) {
    private val key: SecretKey
    private val iv: String
    fun encrypt(plainText: String): String {
        return try {
            Base64.getEncoder().encodeToString(cipher(ENCRYPT_MODE).doFinal(plainText.toByteArray()))
        } catch (ex: Exception) {
            throw RuntimeException("Error while encrypting text", ex)
        }
    }

    fun decrypt(encrypted: String?): String {
        return try {
            String(cipher(DECRYPT_MODE).doFinal(Base64.getDecoder().decode(encrypted)))
        } catch (ex: Exception) {
            throw RuntimeException("Error while decrypting text", ex)
        }
    }

    @Throws(
            NoSuchAlgorithmException::class,
            NoSuchPaddingException::class,
            InvalidKeyException::class,
            InvalidAlgorithmParameterException::class)
    private fun cipher(mode: Int): Cipher {
        val cipher = Cipher.getInstance(ALGO)
        cipher.init(mode, key, GCMParameterSpec(128, iv.toByteArray()))
        return cipher
    }

    companion object {
        private const val ALGO = "AES/GCM/NoPadding"
        private fun key(passphrase: String, salt: String): SecretKey {
            return try {
                SecretKeySpec(
                        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                            .generateSecret(
                                    PBEKeySpec(
                                            passphrase.toCharArray(),
                                            salt.toByteArray(),
                                            10000,
                                            256)).encoded,
                        "AES")
            } catch (ex: Exception) {
                throw RuntimeException("Error while generating key", ex)
            }
        }
    }

    init {
        require(!(isEmpty(passphrase) || isEmpty(fnr))) { "Both passphrase and fnr must be provided" }
        key = key(passphrase, fnr.fnr)
        iv = fnr.fnr
    }
}