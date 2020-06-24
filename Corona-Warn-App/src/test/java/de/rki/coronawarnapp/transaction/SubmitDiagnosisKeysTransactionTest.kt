package de.rki.coronawarnapp.transaction

import KeyExportFormat
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyService
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.TemporaryExposureKey
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class SubmitDiagnosisKeysTransactionTest {
    private val authString = "authString"

    @Before
    fun setUp() {
        mockkObject(LocalData)
        mockkObject(SubmissionService)
        mockkObject(InternalExposureNotificationClient)
        mockkObject(DiagnosisKeyService)
        every { LocalData.numberOfSuccessfulSubmissions(any()) } just Runs
        coEvery { SubmissionService.asyncRequestAuthCode(any()) } returns authString
    }

    @Test
    fun testTransactionNoKeys() {
        coEvery { InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory() } returns listOf()
        coEvery { DiagnosisKeyService.asyncSubmitKeys(authString, listOf()) } just Runs

        runBlocking {
            SubmitDiagnosisKeysTransaction.start("123", listOf())

            coVerifyOrder {
                DiagnosisKeyService.asyncSubmitKeys(authString, listOf())
                SubmissionService.submissionSuccessful()
            }
        }
    }

    @Test
    fun testTransactionHasKeys() {
        val key = TemporaryExposureKey.TemporaryExposureKeyBuilder()
            .setKeyData(ByteArray(1))
            .setRollingPeriod(1)
            .setRollingStartIntervalNumber(1)
            .setTransmissionRiskLevel(1)
            .build()
        val testList = slot<List<KeyExportFormat.TemporaryExposureKey>>()
        coEvery { InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory() } returns listOf(
            key
        )
        coEvery { DiagnosisKeyService.asyncSubmitKeys(authString, capture(testList)) } just Runs

        runBlocking {
            SubmitDiagnosisKeysTransaction.start("123", listOf(key))

            coVerifyOrder {
                DiagnosisKeyService.asyncSubmitKeys(authString, any())
                SubmissionService.submissionSuccessful()
            }
            assertThat(testList.isCaptured, `is`(true))
            assertThat(testList.captured.size, `is`(1))
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
