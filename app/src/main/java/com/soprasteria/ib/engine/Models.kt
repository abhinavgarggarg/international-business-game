package com.soprasteria.ib.engine

enum class SpaceType { START, PROPERTY, AIRLINE, TAX, CHANCE, SURPRISE, JAIL, GO_TO_JAIL, FREE_PARKING }

data class Property(
    val id: Int,
    val name: String,
    val colorGroup: String,
    val cost: Int,
    // rentTiers[0] = base rent (no houses, not a full monopoly)
    // rentTiers[1..4] = 1..4 houses, rentTiers[5] = hotel
    val rentTiers: List<Int>,
    var houses: Int = 0,       // 0-4, 5 = hotel
    var ownerId: Int? = null,
    val houseCost: Int = 0
) {
    val isMortgaged: Boolean get() = false // reserved for future extension
}

data class BoardSpace(
    val index: Int,
    val name: String,
    val type: SpaceType,
    val propertyId: Int? = null,   // set when type == PROPERTY or AIRLINE
    val taxAmount: Int = 0
)

data class Player(
    val id: Int,
    val name: String,
    var balance: Int = 15000,
    var position: Int = 0,
    val ownedProperties: MutableList<Int> = mutableListOf(),
    var inJail: Boolean = false,
    var jailTurns: Int = 0,
    var isBankrupt: Boolean = false
)

data class DiceRoll(val die1: Int, val die2: Int) {
    val total: Int get() = die1 + die2
    val isDouble: Boolean get() = die1 == die2
}

sealed class TurnEvent {
    data class PassedStart(val playerId: Int, val amount: Int) : TurnEvent()
    data class LandedOnUnownedProperty(val playerId: Int, val propertyId: Int) : TurnEvent()
    data class RentCharged(val payerId: Int, val ownerId: Int, val amount: Int, val propertyId: Int) : TurnEvent()
    data class TaxCharged(val playerId: Int, val amount: Int) : TurnEvent()
    data class SentToJail(val playerId: Int) : TurnEvent()
    data class Bankrupted(val playerId: Int, val creditorId: Int?) : TurnEvent()
    data class GameWon(val playerId: Int) : TurnEvent()
}
