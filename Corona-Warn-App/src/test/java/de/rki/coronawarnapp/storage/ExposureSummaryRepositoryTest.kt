package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.coralibre.android.sdk.fakegms.nearby.exposurenotification.ExposureSummary
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * ExposureSummaryRepository test.
 */
class ExposureSummaryRepositoryTest {

    @MockK
    private lateinit var dao: ExposureSummaryDao
    private lateinit var repository: ExposureSummaryRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = ExposureSummaryRepository(dao)

        mockkObject(InternalExposureNotificationClient)
        coEvery { InternalExposureNotificationClient.asyncGetExposureSummary(any()) } returns buildSummary()
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        coEvery { dao.getExposureSummaryEntities() } returns listOf()
        coEvery { dao.getLatestExposureSummary() } returns null
        coEvery { dao.insertExposureSummaryEntity(any()) } returns 0
    }

    /**
     * Test DAO is called.
     */
    @Test
    fun testGet() {
        runBlocking {
            repository.getExposureSummaryEntities()

            coVerify {
                dao.getExposureSummaryEntities()
            }
        }
    }

    /**
     * Test DAO is called.
     */
    @Test
    fun testGetLatest() {
        runBlocking {
            val token = UUID.randomUUID().toString()
            repository.getLatestExposureSummary(token)

            coVerify {
                InternalExposureNotificationClient.asyncGetExposureSummary(token)
            }
        }
    }

    /**
     * Test DAO is called.
     */
    @Test
    fun testInsert() {
        // The following workaround for CoraLibre is required to prevent this test from failing
        // due to a mysterious "Mockk: Missing calls inside every { ... } block" error:
        // TODO: Revert this workaround, make the original test succeed

        /*
        val es = mockk<ExposureSummary>()
        every { es.attenuationDurationsInMinutes } returns intArrayOf(0)
        every { es.daysSinceLastExposure } returns 1
        every { es.matchedKeyCount } returns 1
        every { es.maximumRiskScore } returns 0
        every { es.summationRiskScore } returns 0
        */

        val es = ExposureSummary.ExposureSummaryBuilder()
            .setAttenuationDurations(intArrayOf(0))
            .setDaysSinceLastExposure(1)
            .setMatchedKeyCount(1)
            .setMaximumRiskScore(0)
            .setSummationRiskScore(0)
            .build()

        // End of workaround

        runBlocking {
            repository.insertExposureSummaryEntity(es)

            coVerify {
                dao.insertExposureSummaryEntity(any())
            }
        }
    }

    private fun buildSummary(
        maxRisk: Int = 0,
        lowAttenuation: Int = 0,
        midAttenuation: Int = 0,
        highAttenuation: Int = 0
    ): ExposureSummary {
        val intArray = IntArray(3)
        intArray[0] = lowAttenuation
        intArray[1] = midAttenuation
        intArray[2] = highAttenuation
        return ExposureSummary.ExposureSummaryBuilder()
            .setMaximumRiskScore(maxRisk)
            .setAttenuationDurations(intArray)
            .build()
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
