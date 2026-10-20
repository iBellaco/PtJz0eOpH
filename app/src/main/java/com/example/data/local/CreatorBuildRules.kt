package com.example.data.local

/** Reglas únicas para evitar builds inválidas también en cachés antiguas. */
object CreatorBuildRules {
    private fun key(value: String) = value.trim().lowercase()
    fun overlap(core: List<String>, situational: List<String>): Boolean =
        core.map(::key).toSet().intersect(situational.map(::key).toSet()).isNotEmpty()
    fun supportStarterCount(core: List<String>): Int = core.count {
        key(it) == key("Hoz espectral") || key(it) == key("Escudo reliquia")
    }
    fun normalize(record: CustomChampionBuildRecord): CustomChampionBuildRecord {
        val coreKeys = mutableSetOf<String>()
        val core = record.coreItems.filter { key(it).isNotBlank() && coreKeys.add(key(it)) }
        val situationalKeys = mutableSetOf<String>()
        val situational = record.situationalItems.filter {
            val normalized = key(it)
            normalized.isNotBlank() && normalized !in coreKeys && situationalKeys.add(normalized)
        }
        return record.copy(
            coreItems = core,
            situationalItems = situational,
            coreItemsWithDesc = record.coreItemsWithDesc.filter { key(it.itemName) in coreKeys }.distinctBy { key(it.itemName) },
            situationalItemsWithDesc = record.situationalItemsWithDesc.filter {
                key(it.itemName) !in coreKeys && key(it.itemName) in situationalKeys
            }.distinctBy { key(it.itemName) }
        )
    }
}
