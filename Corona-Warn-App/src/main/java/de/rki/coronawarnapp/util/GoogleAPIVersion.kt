package de.rki.coronawarnapp.util

import dagger.Reusable
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import org.coralibre.android.sdk.fakegms.common.api.ApiException
import javax.inject.Inject
import kotlin.math.abs

@Reusable
class GoogleAPIVersion @Inject constructor() {
    /**
     * Indicates if the client runs above a certain version
     *
     * @return isAboveVersion, if connected to an old unsupported version, return false
     */
    suspend fun isAtLeast(compareVersion: Long): Boolean {
        if (!compareVersion.isCorrectVersionLength) {
            throw IllegalArgumentException("given version has incorrect length")
        }
        return try {
            val currentVersion = InternalExposureNotificationClient.getVersion()
            currentVersion >= compareVersion
        } catch (apiException: ApiException) {
            throw apiException
        }
    }

    // check if a raw long has the correct length to be considered an API version
    private val Long.isCorrectVersionLength
        get(): Boolean = abs(this).toString().length == GOOGLE_API_VERSION_FIELD_LENGTH

    companion object {
        private const val GOOGLE_API_VERSION_FIELD_LENGTH = 8
        const val V16 = 16000000L
    }
}
