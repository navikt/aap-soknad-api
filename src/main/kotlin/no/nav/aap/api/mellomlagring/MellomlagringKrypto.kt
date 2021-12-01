package no.nav.aap.api.mellomlagring

import no.nav.aap.util.AuthContext
import org.springframework.beans.factory.annotation.Value

//@Component
class MellomlagringKrypto(@Value("\${storage.passphrase}") private val passphrase: String,
                          private val tokenUtil: AuthContext) {

    fun katalognavn() = hexBinary(encrypt(tokenUtil.getFnr().fnr).toByteArray())
    fun encrypt(plaintext: String) = Krypto(passphrase, tokenUtil.getFnr()).encrypt(plaintext)
    fun decrypt(encrypted: String) = Krypto(passphrase, tokenUtil.getFnr()).decrypt(encrypted)

    fun hexBinary(data: ByteArray): String {
        val r = StringBuilder(data.size * 2)
        for (b in data) {
            r.append(HEXCODE[b.toInt() shr 4 and 0xF])
            r.append(HEXCODE[b.toInt() and 0xF])
        }
        return r.toString()
    }

    companion object {
        private val HEXCODE = "0123456789ABCDEF".toCharArray()
    }

}