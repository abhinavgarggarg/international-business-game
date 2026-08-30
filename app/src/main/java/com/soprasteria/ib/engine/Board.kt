package com.soprasteria.ib.engine

/**
 * Builds the standard 40-space International Business board.
 * Rent tiers are derived algorithmically from each property's cost so the
 * whole table stays internally consistent (this mirrors how the classic
 * board scales rent, roughly: base ~ 6% of cost, doubling with each house,
 * hotel ~ 5x base).
 */
object BoardFactory {

    private fun rentTiers(cost: Int): List<Int> {
        val base = (cost * 0.06).toInt().coerceAtLeast(20)
        return listOf(
            base,            // 0 houses (monopoly doubles this at runtime)
            base * 3,        // 1 house
            base * 9,        // 2 houses
            base * 16,       // 3 houses
            base * 22,       // 4 houses
            base * 28        // hotel
        )
    }

    data class CityDef(val name: String, val color: String, val cost: Int)

    private val cities = listOf(
        // Brown
        CityDef("Cairo", "Brown", 600), CityDef("Nairobi", "Brown", 600),
        // Light Blue
        CityDef("Bangkok", "LightBlue", 1000), CityDef("Jakarta", "LightBlue", 1000), CityDef("Manila", "LightBlue", 1200),
        // Pink
        CityDef("Mumbai", "Pink", 1400), CityDef("Delhi", "Pink", 1400), CityDef("Dubai", "Pink", 1600),
        // Orange
        CityDef("Istanbul", "Orange", 1800), CityDef("Athens", "Orange", 1800), CityDef("Rome", "Orange", 2000),
        // Red
        CityDef("Berlin", "Red", 2200), CityDef("Madrid", "Red", 2200), CityDef("Amsterdam", "Red", 2400),
        // Yellow
        CityDef("Paris", "Yellow", 2600), CityDef("London", "Yellow", 2600), CityDef("Singapore", "Yellow", 2800),
        // Green
        CityDef("Sydney", "Green", 3000), CityDef("Toronto", "Green", 3000), CityDef("San Francisco", "Green", 3200),
        // Dark Blue
        CityDef("New York", "DarkBlue", 3500), CityDef("Tokyo", "DarkBlue", 4000),
    )

    private val airlines = listOf("Emirates", "Lufthansa", "Singapore Air", "Qantas")

    fun buildBoard(): Pair<List<BoardSpace>, MutableMap<Int, Property>> {
        val spaces = mutableListOf<BoardSpace>()
        val properties = mutableMapOf<Int, Property>()
        var propertyId = 1

        spaces += BoardSpace(0, "Start / Passport Control", SpaceType.START)

        val layout = buildFullLayout()
        var cityIndex = 0
        var airlineIndex = 0

        for ((i, kind) in layout.withIndex()) {
            val idx = i + 1
            when (kind) {
                "CITY" -> {
                    val c = cities[cityIndex++]
                    val prop = Property(propertyId, c.name, c.color, c.cost, rentTiers(c.cost), houseCost = (c.cost * 0.5).toInt())
                    properties[propertyId] = prop
                    spaces += BoardSpace(idx, c.name, SpaceType.PROPERTY, propertyId)
                    propertyId++
                }
                "AIRLINE" -> {
                    val name = airlines[airlineIndex++]
                    val prop = Property(propertyId, name, "Airline", 2000, listOf(500, 1000, 2000, 4000, 0, 0))
                    properties[propertyId] = prop
                    spaces += BoardSpace(idx, name, SpaceType.AIRLINE, propertyId)
                    propertyId++
                }
                "TAX_INCOME" -> spaces += BoardSpace(idx, "Income Tax", SpaceType.TAX, taxAmount = 1000)
                "TAX_LUXURY" -> spaces += BoardSpace(idx, "Luxury Tax", SpaceType.TAX, taxAmount = 750)
                "CHANCE" -> spaces += BoardSpace(idx, "Chance", SpaceType.CHANCE)
                "SURPRISE" -> spaces += BoardSpace(idx, "Surprise", SpaceType.SURPRISE)
                "JAIL" -> spaces += BoardSpace(idx, "Jail / Just Visiting", SpaceType.JAIL)
                "GO_TO_JAIL" -> spaces += BoardSpace(idx, "Go To Jail", SpaceType.GO_TO_JAIL)
                "FREE_PARKING" -> spaces += BoardSpace(idx, "Free Parking", SpaceType.FREE_PARKING)
            }
        }

        return spaces to properties
    }

    /**
     * Builds the 39 remaining spaces after Start (40-space board total).
     * Jail, Go-To-Jail and Free Parking sit at the classic quarter-board
     * positions (10, 20, 30). The other 36 slots are filled from a fixed
     * sequence that consumes exactly the 22 cities, 4 airlines, 2 taxes,
     * 4 Chance and 4 Surprise spaces this board defines (22+4+2+4+4=36),
     * so the layout can never run short of - or leave unused - data.
     */
    private fun buildFullLayout(): List<String> {
        val fixedByPosition = mapOf(10 to "JAIL", 20 to "GO_TO_JAIL", 30 to "FREE_PARKING")

        val otherSlots = ArrayDeque(
            listOf(
                "CITY", "SURPRISE", "CITY", "TAX_INCOME", "CITY", "AIRLINE", "CITY", "CHANCE", "CITY",
                "CITY", "AIRLINE", "CITY", "SURPRISE", "CITY", "CITY", "CHANCE", "CITY",
                "CITY", "AIRLINE", "CITY", "CITY", "SURPRISE", "CITY", "CHANCE", "CITY",
                "CITY", "AIRLINE", "CITY", "TAX_LUXURY", "CITY", "SURPRISE", "CITY", "CHANCE", "CITY", "CITY", "CITY"
            )
        )
        check(otherSlots.count { it == "CITY" } == cities.size) { "city slot count must equal ${cities.size}" }
        check(otherSlots.count { it == "AIRLINE" } == airlines.size) { "airline slot count must equal ${airlines.size}" }
        check(otherSlots.size == 36) { "expected 36 non-fixed slots, got ${otherSlots.size}" }

        val layout = mutableListOf<String>()
        for (position in 1..39) {
            layout += fixedByPosition[position] ?: otherSlots.removeFirst()
        }
        return layout
    }
}
