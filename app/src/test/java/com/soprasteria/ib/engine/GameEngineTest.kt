package com.soprasteria.ib.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * These mirror the standalone tests that were compiled and run with the
 * Kotlin compiler directly (outside Gradle/Android) during development —
 * see /VALIDATION.md in the project root for that raw run output. Porting
 * them to JUnit4 here makes them part of the normal `./gradlew test` flow.
 */
class GameEngineTest {

    private lateinit var engine: GameEngine
    private lateinit var alice: Player
    private lateinit var bob: Player

    private fun testBoard(): List<BoardSpace> = listOf(
        BoardSpace(0, "Start", SpaceType.START),
        BoardSpace(1, "Delhi", SpaceType.PROPERTY, propertyId = 1),
        BoardSpace(2, "Mumbai", SpaceType.PROPERTY, propertyId = 2),
        BoardSpace(3, "Tax", SpaceType.TAX, taxAmount = 500),
        BoardSpace(4, "Jail", SpaceType.JAIL),
        BoardSpace(5, "Tokyo", SpaceType.PROPERTY, propertyId = 3),
        BoardSpace(6, "Go To Jail", SpaceType.GO_TO_JAIL),
    )

    private fun testProperties(): MutableMap<Int, Property> = mutableMapOf(
        1 to Property(1, "Delhi", "Red", cost = 2000, rentTiers = listOf(200, 600, 1600, 4000, 5500, 7000), houseCost = 1000),
        2 to Property(2, "Mumbai", "Red", cost = 2200, rentTiers = listOf(220, 660, 1760, 4400, 6050, 7500), houseCost = 1000),
        3 to Property(3, "Tokyo", "Blue", cost = 3500, rentTiers = listOf(350, 1000, 3000, 9000, 12500, 15000), houseCost = 2000),
    )

    @Before
    fun setUp() {
        val players = mutableListOf(Player(1, "Alice"), Player(2, "Bob"))
        engine = GameEngine(testBoard(), testProperties(), players, Random(42L))
        alice = engine.players.first { it.id == 1 }
        bob = engine.players.first { it.id == 2 }
    }

    @Test
    fun diceRollStaysInValidRange() {
        repeat(500) {
            val roll = engine.rollDice()
            assertTrue("die1 in 1..6", roll.die1 in 1..6)
            assertTrue("die2 in 1..6", roll.die2 in 1..6)
            assertTrue("total in 2..12", roll.total in 2..12)
        }
    }

    @Test
    fun boardLoopingAwardsPassStartExactlyOncePerLap() {
        alice.position = 5
        val startBalance = alice.balance
        engine.movePlayer(alice, 4) // 5 -> 9 % 7 = 2, one lap
        assertEquals(2, alice.position)
        assertEquals(startBalance + 2000, alice.balance)

        alice.position = 0
        val before = alice.balance
        engine.movePlayer(alice, 7 * 2 + 3) // two laps + 3 -> lands on Tax (index 3)
        assertEquals(3, alice.position)
        assertEquals(before + 4000 - 500, alice.balance)
    }

    @Test
    fun rentCalculation_baseMonopolyHousesHotel() {
        val delhi = engine.properties[1]!!
        val mumbai = engine.properties[2]!!

        delhi.ownerId = alice.id
        assertEquals(200, engine.calculateRent(delhi))

        mumbai.ownerId = alice.id // full color set
        assertEquals(400, engine.calculateRent(delhi)) // monopoly doubles base

        delhi.houses = 1
        assertEquals(600, engine.calculateRent(delhi))

        delhi.houses = 5 // hotel
        assertEquals(7000, engine.calculateRent(delhi))
    }

    @Test
    fun bankruptcyReleasesPropertiesAndDeclaresWinner() {
        val tokyo = engine.properties[3]!!
        tokyo.ownerId = bob.id
        tokyo.houses = 5 // hotel rent = 15000

        alice.balance = 5000
        alice.position = 4
        engine.movePlayer(alice, 1) // 4 -> 5 (Tokyo), rent exceeds balance

        assertTrue(alice.isBankrupt)
        assertEquals(0, alice.balance)
        assertTrue(alice.ownedProperties.isEmpty())
        assertEquals(bob.id, engine.gameWinner)

        val events = engine.drainEvents()
        assertTrue(events.any { it is TurnEvent.Bankrupted })
        assertTrue(events.any { it is TurnEvent.GameWon && it.playerId == bob.id })
    }

    @Test
    fun buyingAPropertyDeductsCashAndAssignsOwnership() {
        val startBalance = alice.balance
        assertTrue(engine.buyProperty(alice, 1))
        assertEquals(startBalance - 2000, alice.balance)
        assertEquals(alice.id, engine.properties[1]!!.ownerId)

        bob.balance = 100
        assertFalse(engine.buyProperty(bob, 2))
        assertNull(engine.properties[2]!!.ownerId)
    }
}
