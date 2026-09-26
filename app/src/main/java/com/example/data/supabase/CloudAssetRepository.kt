package com.example.data.supabase

import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.download.GameAssetDownloadManager
import java.util.Locale

/**
 * Repositorio de recursos visuales y resolución dinámica de imágenes.
 * 
 * - Los avatares de usuario y marcos de perfil se mantienen de forma local en la aplicación.
 * - Las imágenes del juego (campeones, habilidades, hechizos, runas y objetos) se resuelven
 *   inmediatamente: si ya se descargó el archivo a disco, se sirve desde el archivo local;
 *   si aún no está descargado, se transmite directamente desde la CDN remota con caché en memoria.
 */
object CloudAssetRepository {

    private const val DDRAGON_CHAMP_CDN = "https://ddragon.leagueoflegends.com/cdn/14.23.1/img/champion"
    private const val DDRAGON_ITEM_CDN = "https://ddragon.leagueoflegends.com/cdn/14.23.1/img/item"
    private const val CDRAGON_PERK_CDN = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/perk-images/styles"

    private val spellCdnFallback = mapOf(
        "flash.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/4.png",
        "ignite.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/14.png",
        "smite.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/11.png",
        "barrier.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/21.png",
        "exhaust.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/3.png",
        "ghost.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/6.png",
        "heal.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/7.png",
        "clarity.jpg" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/13.png",
        "mark.jpg" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/32.png",
        "teleport.png" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/12.png",
        "cleanse.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/1.png"
    )

    private val runeCdnFallback = mapOf(
        "conqueror.png" to "$CDRAGON_PERK_CDN/precision/conqueror/conqueror.png",
        "electrocute.png" to "$CDRAGON_PERK_CDN/domination/electrocute/electrocute.png",
        "dark_harvest.png" to "$CDRAGON_PERK_CDN/domination/darkharvest/darkharvest.png",
        "first_strike.png" to "$CDRAGON_PERK_CDN/inspiration/firststrike/firststrike.png",
        "phase_rush.png" to "$CDRAGON_PERK_CDN/sorcery/phaserush/phaserush.png",
        "lethal_tempo.png" to "$CDRAGON_PERK_CDN/precision/lethaltempo/lethaltempotemp.png",
        "fleet_footwork.png" to "$CDRAGON_PERK_CDN/precision/fleetfootwork/fleetfootwork.png",
        "grasp_undying.png" to "$CDRAGON_PERK_CDN/resolve/graspoftheundying/graspoftheundying.png",
        "arcane_comet.png" to "$CDRAGON_PERK_CDN/sorcery/arcanecomet/arcanecomet.png",
        "aery.png" to "$CDRAGON_PERK_CDN/sorcery/summongaery/summongaery.png",
        "guardian.webp" to "$CDRAGON_PERK_CDN/resolve/guardian/guardian.png",
        "glacial_augment.webp" to "$CDRAGON_PERK_CDN/inspiration/glacialaugment/glacialaugment.png",
        "fortalecimiento.webp" to "$CDRAGON_PERK_CDN/precision/presstheattack/presstheattack.png",
        "cheap_shot.webp" to "$CDRAGON_PERK_CDN/domination/cheapshot/cheapshot.png",
        "sudden_impact.webp" to "$CDRAGON_PERK_CDN/domination/suddenimpact/suddenimpact.png",
        "eyeball_collection.webp" to "$CDRAGON_PERK_CDN/domination/eyeballcollection/eyeballcollection.png",
        "zombie_ward.webp" to "$CDRAGON_PERK_CDN/domination/zombieward/zombieward.png",
        "triumph.webp" to "$CDRAGON_PERK_CDN/precision/triumph.png",
        "coup_de_grace.webp" to "$CDRAGON_PERK_CDN/precision/coupdegrace/coupdegrace.png",
        "cut_down.png" to "$CDRAGON_PERK_CDN/precision/cutdown/cutdown.png",
        "last_stand.webp" to "$CDRAGON_PERK_CDN/precision/laststand/laststand.png",
        "gathering_storm.webp" to "$CDRAGON_PERK_CDN/sorcery/gatheringstorm/gatheringstorm.png",
        "scorch.webp" to "$CDRAGON_PERK_CDN/sorcery/scorch/scorch.png",
        "transcendence.webp" to "$CDRAGON_PERK_CDN/sorcery/transcendence/transcendence.png",
        "celerity.webp" to "$CDRAGON_PERK_CDN/sorcery/celerity/celeritytemp.png",
        "manaflow_band.webp" to "$CDRAGON_PERK_CDN/sorcery/manaflowband/manaflowband.png",
        "nimbus_cloak.webp" to "$CDRAGON_PERK_CDN/sorcery/nimbuscloak/nimbuscloak.png",
        "absolute_focus.webp" to "$CDRAGON_PERK_CDN/sorcery/absolutefocus/absolutefocus.png",
        "demolish.webp" to "$CDRAGON_PERK_CDN/resolve/demolish/demolish.png",
        "font_of_life.webp" to "$CDRAGON_PERK_CDN/resolve/fontoflife/fontoflife.png",
        "bone_plating.webp" to "$CDRAGON_PERK_CDN/resolve/boneplating/boneplating.png",
        "second_wind.webp" to "$CDRAGON_PERK_CDN/resolve/secondwind/secondwind.png",
        "overgrowth.webp" to "$CDRAGON_PERK_CDN/resolve/overgrowth/overgrowth.png",
        "revitalize.webp" to "$CDRAGON_PERK_CDN/resolve/revitalize/revitalize.png"
    )

    private val itemCdnFallback = mapOf(
        "ab38f2866c6c041524f8f14b1749fc1c.png" to "$DDRAGON_ITEM_CDN/3075.png",
        "3b9e64690847f3bc956e9db35a455f32.png" to "$DDRAGON_ITEM_CDN/3033.png",
        "473e58dc0df0c96012529455146ce012.png" to "$DDRAGON_ITEM_CDN/3165.png",
        "8d0a2f1589e177a2bf2ad148cb31a65f.png" to "$DDRAGON_ITEM_CDN/6695.png",
        "1420e397855c6263e113cf0a16d4bb71.png" to "$DDRAGON_ITEM_CDN/3143.png",
        "e1f9d816eb318d20e5c768c4fa05290d.png" to "$DDRAGON_ITEM_CDN/3065.png",
        "3b32bd3dbf4245dc0952c48bc603bcc8.png" to "$DDRAGON_ITEM_CDN/3139.png",
        "7f8482a5143b2c02ad323ce93df371f1.png" to "$DDRAGON_ITEM_CDN/3102.png",
        "422b305b36178590bc9ddd6e826c22ba.png" to "$DDRAGON_ITEM_CDN/3814.png",
        "c3893b84c990398e6ed58b03c16cafa0.webp" to "$DDRAGON_ITEM_CDN/3110.png",
        "dddefc0a5f24a544b89699b38a9a35e1.png" to "$DDRAGON_ITEM_CDN/3001.png",
        "89889a5db477564f0dded7057e9a1916.png" to "$DDRAGON_ITEM_CDN/4401.png",
        "f4c23d99ec30ef6893a81208846a8831.png" to "$DDRAGON_ITEM_CDN/3025.png",
        "3a33fd10d1e6f9e3f55dd6b553970311.png" to "$DDRAGON_ITEM_CDN/3036.png",
        "e361b2bafad7a688cb9f134d5c210943.png" to "$DDRAGON_ITEM_CDN/3053.png",
        "989ee173a52f7cfc8ea3fd107415dc38.png" to "$DDRAGON_ITEM_CDN/3026.png",
        "8501d4d39cb74524631ed6ef74b1f410.png" to "$DDRAGON_ITEM_CDN/3157.png",
        "e450b4ac7163f1de8de7cfe932744c45.png" to "$DDRAGON_ITEM_CDN/3135.png",
        "amaranths_twinguard.webp" to "$DDRAGON_ITEM_CDN/6665.png",
        "essence_reaver.webp" to "$DDRAGON_ITEM_CDN/3508.png",
        "stormrazor.webp" to "$DDRAGON_ITEM_CDN/3095.png",
        "whispering_headband.webp" to "$DDRAGON_ITEM_CDN/3140.png",
        "yun_tal_wildarrows.webp" to "$DDRAGON_ITEM_CDN/6676.png",
        "dawnshroud.webp" to "$DDRAGON_ITEM_CDN/6664.png",
        "unending_despair.webp" to "$DDRAGON_ITEM_CDN/6667.png",
        "echoes_of_helia.webp" to "$DDRAGON_ITEM_CDN/6620.png",
        "statikk_shiv.webp" to "$DDRAGON_ITEM_CDN/3087.png",
        "rapid_firecannon.webp" to "$DDRAGON_ITEM_CDN/3094.png",
        "immortal_shieldbow.webp" to "$DDRAGON_ITEM_CDN/6673.png"
    )

    fun isExcludedFromCloud(url: String): Boolean {
        if (url.isBlank()) return true
        val lower = url.lowercase().trim()
        return lower.contains("frame_") ||
               lower.contains("poro_") ||
               lower.contains("avatar_") ||
               lower.contains("user_avatar")
    }

    /**
     * Resuelve la ubicación óptima de la imagen:
     * 1. Si es avatar/marco de usuario -> local directo
     * 2. Si ya fue descargada por el gestor de descargas a disco -> sirve el archivo local
     * 3. Si aún no está descargada -> sirve la URL CDN remota para que se visualice de inmediato
     */
    fun resolveImageUrl(context: Context, originalUrl: String): String {
        if (originalUrl.isBlank()) return ""
        if (isExcludedFromCloud(originalUrl)) {
            return originalUrl
        }

        // 1. Comprobar si ya existe en almacenamiento interno descargado
        val downloadedFile = GameAssetDownloadManager.getDownloadedFile(context, originalUrl)
        if (downloadedFile != null && downloadedFile.exists() && downloadedFile.length() > 0) {
            return "file://${downloadedFile.absolutePath}"
        }

        val fileName = originalUrl.substringAfterLast("/")

        // 2. Si es una ruta antigua de asset local no empaquetado, mapear a CDN
        if (originalUrl.startsWith("file:///android_asset/champions/")) {
            val champId = fileName.substringBeforeLast(".")
            val champCap = champId.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            return "$DDRAGON_CHAMP_CDN/$champCap.png"
        }

        if (originalUrl.startsWith("file:///android_asset/spells/") || spellCdnFallback.containsKey(fileName)) {
            spellCdnFallback[fileName]?.let { return it }
        }

        if (originalUrl.startsWith("file:///android_asset/runes/") || runeCdnFallback.containsKey(fileName)) {
            runeCdnFallback[fileName]?.let { return it }
        }

        if (itemCdnFallback.containsKey(fileName)) {
            itemCdnFallback[fileName]?.let { return it }
        }

        return originalUrl
    }

    fun buildImageRequest(context: Context, originalUrl: String): ImageRequest {
        val resolved = resolveImageUrl(context, originalUrl)
        return ImageRequest.Builder(context)
            .data(resolved)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
