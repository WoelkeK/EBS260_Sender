package pl.woelke.printersender.util

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PasswordDecoder(private val secretKey: String) {
    /**
     * Generate activate code based on internal entry data.
     * @param input - entry data (unique android ID).
     * @return Base64 activation code.
     */
    fun generateCode(input: String): String {
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)

        val hashedData = mac.doFinal(input.toByteArray())
        return Base64.encodeToString(hashedData, Base64.NO_WRAP).substring(0, 6)
    }

    /**
     * Verification code procedure based on entry and received codes.
     * @param input - entry data (exp. Android ID).
     * @param expectedCode - received confirm code to veryfication.
     * @return true, if codes are equal.
     */
    fun verifyCode(input: String, expectedCode: String): Boolean {
//        val generatedCode = generateCode(input)
        return input == expectedCode
    }
}
