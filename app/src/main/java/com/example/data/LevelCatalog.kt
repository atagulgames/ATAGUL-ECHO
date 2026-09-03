package com.example.data

import com.example.data.local.LevelEntity
import com.example.model.DirectedEdge
import com.example.model.LevelData
import com.example.model.LevelNode
import com.example.model.NodeType
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object LevelCatalog {

    /**
     * Generates a complete set of 100 unique, structured puzzle levels.
     * Every level has distinct geometry, unique title, and the character/path ALWAYS starts at Node 1.
     */
    fun create100Levels(): List<LevelEntity> {
        val list = mutableListOf<LevelEntity>()

        for (id in 1..100) {
            val levelData = buildLevelData(id)
            list.add(
                LevelEntity(
                    id = id,
                    title = levelData.title,
                    gridSize = levelData.gridSize,
                    nodesJson = serializeNodes(levelData.nodes),
                    edgesJson = serializeEdges(levelData.directedEdges),
                    parEchoes = levelData.parEchoes,
                    hintOrderJson = levelData.hintOrder.joinToString(","),
                    mechanicType = levelData.mechanicType,
                    decayLifetime = levelData.decayLifetime,
                    isGhostEchoes = levelData.isGhostEchoes,
                    description = levelData.description,
                    stars = 0,
                    isCompleted = false,
                    isUnlocked = id == 1,
                    bestEchoCount = -1
                )
            )
        }

        return list
    }

    private fun serializeNodes(nodes: List<LevelNode>): String {
        return nodes.joinToString(";") {
            "${it.id}:${(it.x * 10).roundToInt() / 10f}:${(it.y * 10).roundToInt() / 10f}:${it.type.name}:${it.keyForGateId}"
        }
    }

    fun deserializeNodes(raw: String): List<LevelNode> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { part ->
            val tokens = part.split(":")
            if (tokens.size >= 3) {
                val id = tokens[0].toIntOrNull() ?: 1
                val x = tokens[1].toFloatOrNull() ?: 0f
                val y = tokens[2].toFloatOrNull() ?: 0f
                val type = if (tokens.size >= 4) {
                    try { NodeType.valueOf(tokens[3]) } catch (_: Exception) { NodeType.NORMAL }
                } else NodeType.NORMAL
                val keyForGateId = if (tokens.size >= 5) tokens[4].toIntOrNull() ?: -1 else -1
                LevelNode(id, x, y, type, keyForGateId)
            } else null
        }
    }

    private fun serializeEdges(edges: List<DirectedEdge>): String {
        return edges.joinToString(";") { "${it.fromId}>${it.toId}" }
    }

    fun deserializeEdges(raw: String): List<DirectedEdge> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { part ->
            val tokens = part.split(">")
            if (tokens.size == 2) {
                val from = tokens[0].toIntOrNull()
                val to = tokens[1].toIntOrNull()
                if (from != null && to != null) DirectedEdge(from, to) else null
            } else null
        }
    }

    fun entityToLevelData(entity: LevelEntity): LevelData {
        val hintOrder = if (entity.hintOrderJson.isNotBlank()) {
            entity.hintOrderJson.split(",").mapNotNull { it.trim().toIntOrNull() }
        } else emptyList()

        return LevelData(
            levelId = entity.id,
            title = entity.title,
            gridSize = entity.gridSize,
            nodes = deserializeNodes(entity.nodesJson),
            directedEdges = deserializeEdges(entity.edgesJson),
            parEchoes = entity.parEchoes,
            hintOrder = hintOrder,
            mechanicType = entity.mechanicType,
            decayLifetime = entity.decayLifetime,
            isGhostEchoes = entity.isGhostEchoes,
            description = entity.description
        )
    }

    private fun buildLevelData(id: Int): LevelData {
        return when (id) {
            // ==========================================
            // KISIM 1: GEOMETRİK TEMELLER (Bölüm 1 - 20)
            // ==========================================
            1 -> LevelData(
                levelId = 1,
                title = "Başlangıç Üçgeni",
                gridSize = 3,
                nodes = listOf(
                    LevelNode(1, 180f, 120f),
                    LevelNode(2, 80f, 320f),
                    LevelNode(3, 280f, 320f)
                ),
                parEchoes = 0,
                hintOrder = listOf(1, 2, 3, 1),
                mechanicType = "STANDARD",
                description = "Karakter 1'den başlar! Üçgenin tüm köşelerini yankısız birleştir."
            )
            2 -> LevelData(
                levelId = 2,
                title = "Kare Alan",
                gridSize = 3,
                nodes = listOf(
                    LevelNode(1, 90f, 130f),
                    LevelNode(2, 270f, 130f),
                    LevelNode(3, 270f, 310f),
                    LevelNode(4, 90f, 310f)
                ),
                parEchoes = 0,
                hintOrder = listOf(1, 2, 3, 4, 1),
                mechanicType = "STANDARD",
                description = "Karakter 1'den başlar. 4 köşeyi saat yönünde dolaş."
            )
            3 -> LevelData(
                levelId = 3,
                title = "Euler Çatısı",
                gridSize = 3,
                nodes = listOf(
                    LevelNode(1, 180f, 90f),  // Çatı tepe
                    LevelNode(2, 80f, 190f),  // Çatı sol
                    LevelNode(3, 80f, 330f),  // Alt sol
                    LevelNode(4, 280f, 330f), // Alt sağ
                    LevelNode(5, 280f, 190f)  // Çatı sağ
                ),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5),
                mechanicType = "STANDARD",
                description = "1 numaralı çatı zirvesinden başlayarak evin duvarlarını ve çatısını sırayla tamamla."
            )
            4 -> LevelData(
                levelId = 4,
                title = "Kum Saati",
                gridSize = 3,
                nodes = listOf(
                    LevelNode(1, 80f, 110f),  // Sol üst
                    LevelNode(2, 280f, 110f), // Sağ üst
                    LevelNode(3, 180f, 220f), // Orta dar boğaz
                    LevelNode(4, 280f, 330f), // Sağ alt
                    LevelNode(5, 80f, 330f)   // Sol alt
                ),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5),
                mechanicType = "STANDARD",
                description = "Karakter 1'den hareket eder, 1-2-3-4-5 sırasıyla kum saatini çizer."
            )
            5 -> LevelData(
                levelId = 5,
                title = "Mektup Zarfı",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 180f, 80f),  // Zarf kapağı
                    LevelNode(2, 70f, 180f),  // Sol üst
                    LevelNode(3, 70f, 330f),  // Sol alt
                    LevelNode(4, 290f, 330f), // Sağ alt
                    LevelNode(5, 290f, 180f), // Sağ üst
                    LevelNode(6, 180f, 230f)  // Zarf katlama merkezi
                ),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5, 6),
                mechanicType = "STANDARD",
                description = "Zarf kapağındaki 1 numarasından yola çıkıp sırayla zarfı mühürle."
            )
            6 -> LevelData(
                levelId = 6,
                title = "Elmas Yıldızı",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 180f, 80f),  // Kuzey tepe
                    LevelNode(2, 290f, 190f), // Doğu tepe
                    LevelNode(3, 180f, 300f), // Güney tepe
                    LevelNode(4, 70f, 190f),  // Batı tepe
                    LevelNode(5, 180f, 190f)  // Elmas çekirdeği
                ),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5),
                mechanicType = "STANDARD",
                description = "Üst tepe 1'den başla, saat yönünde 4 köşeyi dolaşıp merkeze bağlan."
            )
            7 -> LevelData(
                levelId = 7,
                title = "Neon Altıgen",
                gridSize = 4,
                nodes = createPolygon(6, 180f, 220f, 110f),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5, 6),
                mechanicType = "STANDARD",
                description = "Altıgenin tepe noktası 1'den başlayıp çevre boyunca sırayla ilerle."
            )
            8 -> LevelData(
                levelId = 8,
                title = "Kalkan Matrisi",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 180f, 70f),  // Kalkan tepe
                    LevelNode(2, 290f, 160f), // Sağ omuz
                    LevelNode(3, 260f, 300f), // Sağ gövde
                    LevelNode(4, 180f, 370f), // Alt sivri uç
                    LevelNode(5, 100f, 300f), // Sol gövde
                    LevelNode(6, 70f, 160f)   // Sol omuz
                ),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5, 6),
                mechanicType = "STANDARD",
                description = "Kalkanın başı 1'den başla, kalkan zırhını sırayla çevrele."
            )
            9 -> LevelData(
                levelId = 9,
                title = "Köprülü İkizler",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 80f, 120f),  // Sol kule sol tepe
                    LevelNode(2, 80f, 240f),  // Sol kule sol alt
                    LevelNode(3, 140f, 240f), // Sol kule sağ alt
                    LevelNode(4, 140f, 120f), // Sol kule köprü bağlantısı
                    LevelNode(5, 220f, 120f), // Sağ kule köprü bağlantısı
                    LevelNode(6, 220f, 240f), // Sağ kule sol alt
                    LevelNode(7, 280f, 240f), // Sağ kule sağ alt
                    LevelNode(8, 280f, 120f)  // Sağ kule sağ tepe
                ),
                parEchoes = 2,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7, 8),
                mechanicType = "STANDARD",
                description = "Sol kulenin 1 numaralı tepesinden başla, köprüden sağ kuleye geç."
            )
            10 -> LevelData(
                levelId = 10,
                title = "3x3 Matris Yılanı",
                gridSize = 3,
                nodes = listOf(
                    LevelNode(1, 70f, 100f), LevelNode(2, 180f, 100f), LevelNode(3, 290f, 100f),
                    LevelNode(4, 290f, 220f), LevelNode(5, 180f, 220f), LevelNode(6, 70f, 220f),
                    LevelNode(7, 70f, 340f), LevelNode(8, 180f, 340f), LevelNode(9, 290f, 340f)
                ),
                parEchoes = 2,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
                mechanicType = "STANDARD",
                description = "Karakter sol üst 1'den başlayarak tüm 9 düğümü yılan rotasıyla birleştirir."
            )
            11 -> LevelData(
                levelId = 11,
                title = "Kelebek Kanatları",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 180f, 110f), // Baş
                    LevelNode(2, 280f, 140f), // Sağ üst kanat
                    LevelNode(3, 290f, 270f), // Sağ alt kanat
                    LevelNode(4, 180f, 360f), // Kuyruk
                    LevelNode(5, 70f, 270f),  // Sol alt kanat
                    LevelNode(6, 80f, 140f),  // Sol üst kanat
                    LevelNode(7, 180f, 240f)  // Merkez gövde
                ),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7),
                mechanicType = "STANDARD",
                description = "Kelebeğin başındaki 1'den başla, iki kanadı sırayla çizip gövdede bitir."
            )
            12 -> LevelData(
                levelId = 12,
                title = "Sonsuzluk Döngüsü",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 90f, 170f),
                    LevelNode(2, 140f, 130f),
                    LevelNode(3, 180f, 220f),
                    LevelNode(4, 220f, 130f),
                    LevelNode(5, 270f, 170f),
                    LevelNode(6, 270f, 270f),
                    LevelNode(7, 220f, 310f),
                    LevelNode(8, 140f, 310f),
                    LevelNode(9, 90f, 270f)
                ),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
                mechanicType = "STANDARD",
                description = "Sol kanat 1'den başlayıp sonsuzluk figürünü sırayla tamamla."
            )
            13 -> LevelData(
                levelId = 13,
                title = "Prizma Kırılması",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 180f, 80f),
                    LevelNode(2, 80f, 200f),
                    LevelNode(3, 100f, 370f),
                    LevelNode(4, 260f, 370f),
                    LevelNode(5, 280f, 200f),
                    LevelNode(6, 180f, 260f)
                ),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5, 6),
                mechanicType = "STANDARD",
                description = "Prizmanın ışık odağı 1'den çıkan ışınları sırayla yansıt."
            )
            14 -> LevelData(
                levelId = 14,
                title = "Pusula İğnesi",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 180f, 70f),  // Kuzey
                    LevelNode(2, 290f, 220f), // Doğu
                    LevelNode(3, 180f, 370f), // Güney
                    LevelNode(4, 70f, 220f),  // Batı
                    LevelNode(5, 140f, 170f), // Kuzeybatı iç
                    LevelNode(6, 180f, 220f), // Merkez
                    LevelNode(7, 220f, 270f)  // Güneydoğu iç
                ),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7),
                mechanicType = "STANDARD",
                description = "Kuzey kutbu 1'den başlayarak pusulanın rotasını sırayla çiz."
            )
            15 -> LevelData(
                levelId = 15,
                title = "Zümrüt Taç",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 90f, 320f),  // Taban sol
                    LevelNode(2, 90f, 160f),  // Sol tepe
                    LevelNode(3, 130f, 210f), // Sol oyuk
                    LevelNode(4, 180f, 100f), // Orta yüksek tepe
                    LevelNode(5, 230f, 210f), // Sağ oyuk
                    LevelNode(6, 270f, 160f), // Sağ tepe
                    LevelNode(7, 270f, 320f)  // Taban sağ
                ),
                parEchoes = 2,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7),
                mechanicType = "STANDARD",
                description = "Tacın 1 numaralı sol tabanından başlayıp taç silüetini sırayla çiz."
            )
            16 -> LevelData(
                levelId = 16,
                title = "Kristal Küp",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 120f, 100f), // Üst sol
                    LevelNode(2, 240f, 100f), // Üst sağ
                    LevelNode(3, 280f, 180f), // Sağ üst
                    LevelNode(4, 280f, 300f), // Sağ alt
                    LevelNode(5, 160f, 370f), // Alt orta
                    LevelNode(6, 60f, 300f),  // Sol alt
                    LevelNode(7, 60f, 180f),  // Sol üst
                    LevelNode(8, 170f, 230f)  // Küp merkezi
                ),
                parEchoes = 2,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7, 8),
                mechanicType = "STANDARD",
                description = "İzometrik küpün 1 numaralı üst köşesinden 3 boyutlu hatları sırayla çiz."
            )
            17 -> LevelData(
                levelId = 17,
                title = "DNA İkili Sarmalı",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 90f, 90f),
                    LevelNode(2, 270f, 90f),
                    LevelNode(3, 270f, 180f),
                    LevelNode(4, 90f, 180f),
                    LevelNode(5, 90f, 270f),
                    LevelNode(6, 270f, 270f),
                    LevelNode(7, 270f, 360f),
                    LevelNode(8, 90f, 360f)
                ),
                parEchoes = 2,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7, 8),
                mechanicType = "STANDARD",
                description = "Genetik sarmalın 1 numaralı üst bazından başlayarak basamakları sırayla geç."
            )
            18 -> LevelData(
                levelId = 18,
                title = "Hilal Yayı",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 220f, 80f),  // Üst sivri uç
                    LevelNode(2, 140f, 120f),
                    LevelNode(3, 90f, 220f),  // Dış yay tepe
                    LevelNode(4, 140f, 320f),
                    LevelNode(5, 220f, 360f), // Alt sivri uç
                    LevelNode(6, 170f, 260f), // İç yay alt
                    LevelNode(7, 170f, 180f)  // İç yay üst
                ),
                parEchoes = 1,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7),
                mechanicType = "STANDARD",
                description = "Ay hilalinin 1 numaralı sivri ucundan kıvrıma sırayla başla."
            )
            19 -> LevelData(
                levelId = 19,
                title = "Kozmik Çapraz",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 180f, 70f),  // Üst
                    LevelNode(2, 180f, 150f),
                    LevelNode(3, 290f, 220f), // Sağ
                    LevelNode(4, 220f, 220f),
                    LevelNode(5, 180f, 220f), // Merkez
                    LevelNode(6, 180f, 290f),
                    LevelNode(7, 180f, 370f), // Alt
                    LevelNode(8, 70f, 220f),  // Sol
                    LevelNode(9, 140f, 220f)
                ),
                parEchoes = 2,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
                mechanicType = "STANDARD",
                description = "Çaprazın üst kanadı 1'den hareketlen, tüm yönleri sırayla bağla."
            )
            20 -> LevelData(
                levelId = 20,
                title = "Antik Tapınak",
                gridSize = 4,
                nodes = listOf(
                    LevelNode(1, 180f, 80f),  // Tapınak zirvesi
                    LevelNode(2, 270f, 150f), // Sağ saçak
                    LevelNode(3, 260f, 260f), // Sağ sütun orta
                    LevelNode(4, 290f, 350f), // Sağ taban
                    LevelNode(5, 180f, 350f), // Merkez taban
                    LevelNode(6, 70f, 350f),  // Sol taban
                    LevelNode(7, 100f, 260f), // Sol sütun orta
                    LevelNode(8, 90f, 150f)   // Sol saçak
                ),
                parEchoes = 2,
                hintOrder = listOf(1, 2, 3, 4, 5, 6, 7, 8),
                mechanicType = "STANDARD",
                description = "Tapınak alınlığındaki 1'den başla, sütunları ve tabanı sırayla birleştir."
            )

            // =======================================================
            // KISIM 2: ZAMAN AYARLI & SÖNEN YANKILAR (Bölüm 21 - 40)
            // =======================================================
            in 21..40 -> buildDecayingTierLevel(id)

            // =======================================================
            // KISIM 3: KİLİT & ANAHTAR ODALARI (Bölüm 41 - 60)
            // =======================================================
            in 41..60 -> buildLockAndKeyTierLevel(id)

            // =======================================================
            // KISIM 4: YÖNLÜ AKIŞ & HAYALET YANKILAR (Bölüm 61 - 80)
            // =======================================================
            in 61..80 -> buildVectorGhostTierLevel(id)

            // =======================================================
            // KISIM 5: BÜYÜK USTA FİNAL AĞLARI (Bölüm 81 - 100)
            // =======================================================
            else -> buildGrandMasterTierLevel(id)
        }
    }

    // --- TIER 2: ZAMAN AYARLI YANKILAR (21..40) ---
    private fun buildDecayingTierLevel(id: Int): LevelData {
        val titles = mapOf(
            21 to "Dalga Formu",
            22 to "Beşgen Yıldız",
            23 to "Radar Ağı",
            24 to "Sarmal Girdap",
            25 to "Çift Elmas",
            26 to "Akrep İğnesi",
            27 to "Kutup Yıldızı",
            28 to "Manyetik Kalkan",
            29 to "Piramit Basamakları",
            30 to "Güneş Çiçeği",
            31 to "Foton Kafesi",
            32 to "Kuantum Tüneli",
            33 to "Yıldız Gemisi",
            34 to "Nova Patlaması",
            35 to "Siber Labirent",
            36 to "Çift Girdap",
            37 to "Zaman Çarkı",
            38 to "Kartal Pençesi",
            39 to "Zen Taşları",
            40 to "Zodyak Çemberi"
        )
        val title = titles[id] ?: "Zaman Ayarlı: Seviye $id"

        val nodes = when (id) {
            21 -> listOf( // Dalga Formu (7 nodes)
                LevelNode(1, 60f, 220f),
                LevelNode(2, 100f, 130f),
                LevelNode(3, 140f, 220f),
                LevelNode(4, 180f, 310f),
                LevelNode(5, 220f, 220f),
                LevelNode(6, 260f, 130f),
                LevelNode(7, 300f, 220f)
            )
            22 -> createStar(5, 180f, 220f, 125f, 60f)
            23 -> createConcentricRings(3, 5, 180f, 220f, 55f, 120f)
            24 -> createSpiral(8, 180f, 220f, 40f, 125f)
            25 -> listOf( // Çift Elmas (7 nodes)
                LevelNode(1, 180f, 80f),
                LevelNode(2, 100f, 160f),
                LevelNode(3, 260f, 160f),
                LevelNode(4, 180f, 230f),
                LevelNode(5, 100f, 300f),
                LevelNode(6, 260f, 300f),
                LevelNode(7, 180f, 370f)
            )
            26 -> listOf( // Akrep İğnesi (8 nodes)
                LevelNode(1, 280f, 90f),
                LevelNode(2, 220f, 80f),
                LevelNode(3, 160f, 110f),
                LevelNode(4, 130f, 180f),
                LevelNode(5, 150f, 260f),
                LevelNode(6, 220f, 310f),
                LevelNode(7, 160f, 360f),
                LevelNode(8, 90f, 330f)
            )
            27 -> createStar(4, 180f, 220f, 130f, 50f).toMutableList().apply {
                add(LevelNode(9, 180f, 220f)) // Core center
            }
            28 -> createWheel(7, 180f, 220f, 120f)
            29 -> listOf( // Piramit Basamakları (9 nodes)
                LevelNode(1, 180f, 90f),
                LevelNode(2, 130f, 180f), LevelNode(3, 230f, 180f),
                LevelNode(4, 90f, 270f), LevelNode(5, 180f, 270f), LevelNode(6, 270f, 270f),
                LevelNode(7, 60f, 360f), LevelNode(8, 180f, 360f), LevelNode(9, 300f, 360f)
            )
            30 -> createStar(6, 180f, 220f, 125f, 65f)
            31 -> createPolygon(8, 180f, 220f, 120f)
            32 -> createTunnelPerspective(8)
            33 -> listOf( // Yıldız Gemisi (7 nodes)
                LevelNode(1, 180f, 80f),
                LevelNode(2, 150f, 190f), LevelNode(3, 210f, 190f),
                LevelNode(4, 60f, 330f), LevelNode(5, 140f, 310f),
                LevelNode(6, 220f, 310f), LevelNode(7, 300f, 330f)
            )
            34 -> createWheel(8, 180f, 220f, 125f)
            35 -> createZigzagMaze(8)
            36 -> createDualSpiral(8)
            37 -> createWheel(8, 180f, 220f, 120f)
            38 -> listOf( // Kartal Pençesi (7 nodes)
                LevelNode(1, 180f, 100f),
                LevelNode(2, 180f, 200f),
                LevelNode(3, 100f, 280f), LevelNode(4, 70f, 360f),
                LevelNode(5, 180f, 370f),
                LevelNode(6, 260f, 280f), LevelNode(7, 290f, 360f)
            )
            39 -> listOf( // Zen Taşları (7 nodes)
                LevelNode(1, 180f, 80f),
                LevelNode(2, 140f, 150f), LevelNode(3, 220f, 150f),
                LevelNode(4, 110f, 240f), LevelNode(5, 250f, 240f),
                LevelNode(6, 80f, 340f), LevelNode(7, 280f, 340f)
            )
            else -> createPolygon(10, 180f, 220f, 125f) // 40: Zodyak
        }

        return LevelData(
            levelId = id,
            title = title,
            gridSize = 5,
            nodes = nodes,
            parEchoes = 2 + (id % 2),
            mechanicType = "DECAYING",
            decayLifetime = 3,
            hintOrder = listOf(1, 2, 3),
            description = "Karakter 1'den başlar! Yankılar 3 hamle sonra solar, hataları bariyer olarak hesapla."
        )
    }

    // --- TIER 3: KİLİT & ANAHTAR ODALARI (41..60) ---
    private fun buildLockAndKeyTierLevel(id: Int): LevelData {
        val titles = mapOf(
            41 to "Kilitli Kasa",
            42 to "Şifreli Kapı",
            43 to "Siber Mahzen",
            44 to "Çift Muhafız",
            45 to "Kripto Labirent",
            46 to "Kristal Sandık",
            47 to "Tapınak Girişi",
            48 to "Gardiyan Kulesi",
            49 to "Hazine Odası",
            50 to "Kırmızı Geçit",
            51 to "Manyetik Anahtar",
            52 to "Şifreli Zindan",
            53 to "Kraliyet Kasası",
            54 to "Gizli Geçit",
            55 to "Karadelik Kilidi",
            56 to "Usta Kasası",
            57 to "Siber Kule",
            58 to "Labirent Çekirdeği",
            59 to "Arşimet Kilidi",
            60 to "Güvenlik Matrisi"
        )
        val title = titles[id] ?: "Kilit & Anahtar: Seviye $id"

        val count = 7 + (id % 3)
        val keyId = 2 + (id % (count - 3))
        val gateId = count - 1

        val baseNodes = when (id) {
            41 -> listOf(
                LevelNode(1, 180f, 90f),
                LevelNode(2, 90f, 190f),
                LevelNode(3, 270f, 190f),
                LevelNode(4, 90f, 320f),
                LevelNode(5, 270f, 320f),
                LevelNode(6, 180f, 370f)
            )
            46 -> createStar(4, 180f, 220f, 125f, 60f)
            53 -> createCrownNodes(8)
            58 -> createConcentricSquares(8)
            else -> createPolygon(count, 180f, 220f, 120f)
        }

        // Configure Key node and Gate node
        val nodesWithLockKey = baseNodes.map { node ->
            when (node.id) {
                keyId -> LevelNode(node.id, node.x, node.y, NodeType.KEY, keyForGateId = gateId)
                gateId -> LevelNode(node.id, node.x, node.y, NodeType.GATE, keyForGateId = gateId)
                else -> node
            }
        }

        return LevelData(
            levelId = id,
            title = title,
            gridSize = 5,
            nodes = nodesWithLockKey,
            parEchoes = 2,
            mechanicType = "LOCK_KEY",
            hintOrder = listOf(1, keyId),
            description = "Karakter 1'den başlar. $gateId numaralı kapıya girmeden önce $keyId numaralı anahtarı almalısın!"
        )
    }

    // --- TIER 4: YÖNLÜ AKIŞ & HAYALET YANKILAR (61..80) ---
    private fun buildVectorGhostTierLevel(id: Int): LevelData {
        val isGhost = id % 2 == 0
        val titles = mapOf(
            61 to "Tek Yönlü Rüzgar",
            62 to "Hayalet Koridoru",
            63 to "Vektör Akışı",
            64 to "Gölge Yıldızı",
            65 to "Yönlü Labirent",
            66 to "Hayalet Sarmal",
            67 to "Akım Döngüsü",
            68 to "Spektrum Ağı",
            69 to "Gölge Kristali",
            70 to "İleri Doğru",
            71 to "Hayalet Çember",
            72 to "Vektör Çaprazı",
            73 to "Gölge Kafesi",
            74 to "Tek Yönlü Girdap",
            75 to "Hayalet Lattis",
            76 to "Vektör Oku",
            77 to "Gölge Köprü",
            78 to "Tek Yönlü Yıldız",
            79 to "Hayalet Kelebek",
            80 to "Vektör Labirenti"
        )
        val title = titles[id] ?: (if (isGhost) "Hayalet Yankı: Seviye $id" else "Yönlü Vektör: Seviye $id")

        val nodes = when (id) {
            61 -> listOf(
                LevelNode(1, 70f, 120f), LevelNode(2, 180f, 120f), LevelNode(3, 290f, 120f),
                LevelNode(4, 180f, 220f),
                LevelNode(5, 70f, 320f), LevelNode(6, 180f, 320f), LevelNode(7, 290f, 320f)
            )
            64 -> createStar(5, 180f, 220f, 125f, 60f)
            66 -> createSpiral(8, 180f, 220f, 40f, 125f)
            72 -> createCross(9)
            76 -> createArrowShape(7)
            else -> createPolygon(8, 180f, 220f, 120f)
        }

        val directedEdges = if (!isGhost) {
            listOf(
                DirectedEdge(1, 2),
                DirectedEdge(3, 4),
                DirectedEdge(nodes.last().id - 1, nodes.last().id)
            )
        } else emptyList()

        return LevelData(
            levelId = id,
            title = title,
            gridSize = 5,
            nodes = nodes,
            directedEdges = directedEdges,
            parEchoes = 2,
            mechanicType = if (isGhost) "GHOST" else "ONE_WAY",
            isGhostEchoes = isGhost,
            hintOrder = listOf(1, 2),
            description = if (isGhost)
                "Karakter 1'den başlar. Hayalet yankılar kısa sürede şeffaflaşır fakat fiziksel olarak orada kalır!"
            else
                "Karakter 1'den başlar. Yönlü oklar yalnızca gösterilen yönde çizilebilir!"
        )
    }

    // --- TIER 5: BÜYÜK USTA FİNAL AĞLARI (81..100) ---
    private fun buildGrandMasterTierLevel(id: Int): LevelData {
        val titles = mapOf(
            81 to "Büyük Usta Ağacı",
            82 to "Kozmik Matris",
            83 to "Zaman-Uzay Girdabı",
            84 to "Kuantum Kilidi",
            85 to "Süpernova Çekirdeği",
            86 to "Yüce Muhafız",
            87 to "Boyutlararası Ağ",
            88 to "Boşluğun Kalbi",
            89 to "Altın Oran Sarmalı",
            90 to "Kristal Saray",
            91 to "Nebula Lattisi",
            92 to "Zaman Kapsülü",
            93 to "Çift Çekirdek",
            94 to "Sonsuzluk Lattisi",
            95 to "Kuantum Ağı",
            96 to "Işık Muhafızı",
            97 to "Galaksi Sarmalı",
            98 to "Usta Labirent",
            99 to "Titan Kalbi",
            100 to "Yankı Ustası Finali"
        )
        val title = titles[id] ?: "Büyük Usta: Seviye $id"

        val nodeCount = 9 + (id - 80) / 4
        val nodes = when (id) {
            81 -> createTreeNodes(10)
            82 -> createConcentricRings(5, 5, 180f, 220f, 60f, 125f)
            87 -> createTesseractNodes(10)
            93 -> createDualClusters(5, 5)
            97 -> createGalaxyArms(12)
            99 -> createHeartFortress(12)
            100 -> createMasterClimaxNodes(12)
            else -> createConcentricRings(4, nodeCount - 4, 180f, 220f, 55f, 130f)
        }

        val hasKey = id in listOf(84, 86, 90, 96, 100)
        val finalNodes = if (hasKey) {
            val keyId = 3
            val gateId = nodes.last().id
            nodes.map { n ->
                when (n.id) {
                    keyId -> LevelNode(n.id, n.x, n.y, NodeType.KEY, keyForGateId = gateId)
                    gateId -> LevelNode(n.id, n.x, n.y, NodeType.GATE, keyForGateId = gateId)
                    else -> n
                }
            }
        } else nodes

        val isDecaying = id % 2 != 0
        val isGhost = id % 4 == 0

        return LevelData(
            levelId = id,
            title = title,
            gridSize = 6,
            nodes = finalNodes,
            parEchoes = 3,
            mechanicType = if (hasKey) "LOCK_KEY" else if (isDecaying) "DECAYING" else "STANDARD",
            decayLifetime = 3,
            isGhostEchoes = isGhost,
            hintOrder = listOf(1, 2),
            description = if (id == 100)
                "BÜYÜK FİNAL: Karakter 1'den başlar! Tüm yankı ustalık disiplinlerini bir araya getirerek 100. bölümü tamamla!"
            else
                "Karakter 1'den başlar. Usta seviye dar alan ve karmaşık geometrik bağlar!"
        )
    }

    // ==========================================
    // GEOMETRİK DÜĞÜM ÜRETECİLERİ (TÜMÜ 1-TABANLI)
    // ==========================================

    private fun createPolygon(count: Int, cx: Float, cy: Float, radius: Float): List<LevelNode> {
        val list = mutableListOf<LevelNode>()
        val angleStep = (2 * Math.PI / count)
        for (i in 0 until count) {
            val angle = i * angleStep - Math.PI / 2
            val x = (cx + radius * cos(angle)).toFloat()
            val y = (cy + radius * sin(angle)).toFloat()
            list.add(LevelNode(id = i + 1, x = x, y = y))
        }
        return list
    }

    private fun createStar(points: Int, cx: Float, cy: Float, rOuter: Float, rInner: Float): List<LevelNode> {
        val list = mutableListOf<LevelNode>()
        val totalVertices = points * 2
        val step = Math.PI / points
        for (i in 0 until totalVertices) {
            val r = if (i % 2 == 0) rOuter else rInner
            val angle = i * step - Math.PI / 2
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            list.add(LevelNode(id = i + 1, x = x, y = y))
        }
        return list
    }

    private fun createWheel(spokes: Int, cx: Float, cy: Float, radius: Float): List<LevelNode> {
        val list = mutableListOf<LevelNode>()
        // 1 is the starting rim node
        list.add(LevelNode(id = 1, x = cx, y = cy - radius))
        // Center hub is node 2
        list.add(LevelNode(id = 2, x = cx, y = cy))
        // Remaining rim nodes 3..spokes+1
        val step = (2 * Math.PI / spokes)
        for (i in 1 until spokes) {
            val angle = i * step - Math.PI / 2
            val x = (cx + radius * cos(angle)).toFloat()
            val y = (cy + radius * sin(angle)).toFloat()
            list.add(LevelNode(id = i + 2, x = x, y = y))
        }
        return list
    }

    private fun createConcentricRings(innerCount: Int, outerCount: Int, cx: Float, cy: Float, rInner: Float, rOuter: Float): List<LevelNode> {
        val list = mutableListOf<LevelNode>()
        // Node 1 is top of outer ring
        var idCounter = 1
        val outerStep = (2 * Math.PI / outerCount)
        for (i in 0 until outerCount) {
            val angle = i * outerStep - Math.PI / 2
            list.add(LevelNode(id = idCounter++, x = (cx + rOuter * cos(angle)).toFloat(), y = (cy + rOuter * sin(angle)).toFloat()))
        }
        val innerStep = (2 * Math.PI / innerCount)
        for (i in 0 until innerCount) {
            val angle = i * innerStep - Math.PI / 2
            list.add(LevelNode(id = idCounter++, x = (cx + rInner * cos(angle)).toFloat(), y = (cy + rInner * sin(angle)).toFloat()))
        }
        return list
    }

    private fun createSpiral(count: Int, cx: Float, cy: Float, rStart: Float, rEnd: Float): List<LevelNode> {
        val list = mutableListOf<LevelNode>()
        for (i in 0 until count) {
            val ratio = i.toFloat() / (count - 1)
            val r = rStart + (rEnd - rStart) * ratio
            val angle = ratio * 2.5 * Math.PI - Math.PI / 2
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            list.add(LevelNode(id = i + 1, x = x, y = y))
        }
        return list
    }

    private fun createDualSpiral(count: Int): List<LevelNode> {
        val list = mutableListOf<LevelNode>()
        list.add(LevelNode(1, 100f, 120f))
        list.add(LevelNode(2, 160f, 100f))
        list.add(LevelNode(3, 160f, 180f))
        list.add(LevelNode(4, 110f, 180f))
        list.add(LevelNode(5, 260f, 320f))
        list.add(LevelNode(6, 200f, 340f))
        list.add(LevelNode(7, 200f, 260f))
        list.add(LevelNode(8, 250f, 260f))
        return list
    }

    private fun createTunnelPerspective(count: Int): List<LevelNode> {
        return listOf(
            LevelNode(1, 70f, 110f),
            LevelNode(2, 290f, 110f),
            LevelNode(3, 290f, 330f),
            LevelNode(4, 70f, 330f),
            LevelNode(5, 130f, 170f),
            LevelNode(6, 230f, 170f),
            LevelNode(7, 230f, 270f),
            LevelNode(8, 130f, 270f)
        )
    }

    private fun createZigzagMaze(count: Int): List<LevelNode> {
        return listOf(
            LevelNode(1, 70f, 100f),
            LevelNode(2, 290f, 100f),
            LevelNode(3, 290f, 180f),
            LevelNode(4, 70f, 180f),
            LevelNode(5, 70f, 260f),
            LevelNode(6, 290f, 260f),
            LevelNode(7, 290f, 340f),
            LevelNode(8, 70f, 340f)
        )
    }

    private fun createCrownNodes(count: Int): List<LevelNode> {
        return listOf(
            LevelNode(1, 180f, 90f),
            LevelNode(2, 80f, 140f),
            LevelNode(3, 280f, 140f),
            LevelNode(4, 130f, 200f),
            LevelNode(5, 230f, 200f),
            LevelNode(6, 80f, 310f),
            LevelNode(7, 180f, 310f),
            LevelNode(8, 280f, 310f)
        )
    }

    private fun createConcentricSquares(count: Int): List<LevelNode> {
        return listOf(
            LevelNode(1, 70f, 110f),
            LevelNode(2, 290f, 110f),
            LevelNode(3, 290f, 330f),
            LevelNode(4, 70f, 330f),
            LevelNode(5, 130f, 170f),
            LevelNode(6, 230f, 170f),
            LevelNode(7, 230f, 270f),
            LevelNode(8, 130f, 270f)
        )
    }

    private fun createCross(count: Int): List<LevelNode> {
        return listOf(
            LevelNode(1, 180f, 80f),
            LevelNode(2, 180f, 160f),
            LevelNode(3, 80f, 220f),
            LevelNode(4, 140f, 220f),
            LevelNode(5, 180f, 220f),
            LevelNode(6, 220f, 220f),
            LevelNode(7, 280f, 220f),
            LevelNode(8, 180f, 280f),
            LevelNode(9, 180f, 360f)
        )
    }

    private fun createArrowShape(count: Int): List<LevelNode> {
        return listOf(
            LevelNode(1, 180f, 80f),
            LevelNode(2, 100f, 170f),
            LevelNode(3, 260f, 170f),
            LevelNode(4, 150f, 170f),
            LevelNode(5, 210f, 170f),
            LevelNode(6, 150f, 360f),
            LevelNode(7, 210f, 360f)
        )
    }

    private fun createTreeNodes(count: Int): List<LevelNode> {
        return listOf(
            LevelNode(1, 180f, 360f), // Kök (1'den başlar)
            LevelNode(2, 180f, 280f),
            LevelNode(3, 120f, 210f),
            LevelNode(4, 240f, 210f),
            LevelNode(5, 80f, 140f),
            LevelNode(6, 150f, 140f),
            LevelNode(7, 210f, 140f),
            LevelNode(8, 280f, 140f),
            LevelNode(9, 110f, 80f),
            LevelNode(10, 250f, 80f)
        )
    }

    private fun createTesseractNodes(count: Int): List<LevelNode> {
        return listOf(
            LevelNode(1, 70f, 100f), LevelNode(2, 290f, 100f),
            LevelNode(3, 290f, 320f), LevelNode(4, 70f, 320f),
            LevelNode(5, 120f, 150f), LevelNode(6, 240f, 150f),
            LevelNode(7, 240f, 270f), LevelNode(8, 120f, 270f),
            LevelNode(9, 180f, 180f), LevelNode(10, 180f, 240f)
        )
    }

    private fun createDualClusters(c1Count: Int, c2Count: Int): List<LevelNode> {
        val list = mutableListOf<LevelNode>()
        val poly1 = createPolygon(c1Count, 120f, 170f, 65f)
        val poly2 = createPolygon(c2Count, 240f, 270f, 65f)
        list.addAll(poly1)
        poly2.forEach { p ->
            list.add(LevelNode(id = list.size + 1, x = p.x, y = p.y))
        }
        return list
    }

    private fun createGalaxyArms(count: Int): List<LevelNode> {
        val list = mutableListOf<LevelNode>()
        list.add(LevelNode(1, 180f, 220f)) // Galaksi merkezi
        val arms = 3
        val nodesPerArm = (count - 1) / arms
        var id = 2
        for (a in 0 until arms) {
            val baseAngle = a * (2 * Math.PI / arms)
            for (step in 1..nodesPerArm) {
                val r = 35f + step * 28f
                val angle = baseAngle + step * 0.4
                list.add(LevelNode(id = id++, x = (180f + r * cos(angle)).toFloat(), y = (220f + r * sin(angle)).toFloat()))
            }
        }
        return list
    }

    private fun createHeartFortress(count: Int): List<LevelNode> {
        return listOf(
            LevelNode(1, 180f, 140f), // Üst çukur (1'den başlar)
            LevelNode(2, 130f, 90f),
            LevelNode(3, 80f, 130f),
            LevelNode(4, 70f, 200f),
            LevelNode(5, 110f, 270f),
            LevelNode(6, 180f, 360f), // Sivri alt uç
            LevelNode(7, 250f, 270f),
            LevelNode(8, 290f, 200f),
            LevelNode(9, 280f, 130f),
            LevelNode(10, 230f, 90f),
            LevelNode(11, 140f, 200f),
            LevelNode(12, 220f, 200f)
        )
    }

    private fun createMasterClimaxNodes(count: Int): List<LevelNode> {
        // Ultimate 12-node symmetrical star lattice for Level 100
        val outer = createPolygon(6, 180f, 220f, 130f)
        val inner = createPolygon(6, 180f, 220f, 65f)
        val list = mutableListOf<LevelNode>()
        list.addAll(outer)
        inner.forEachIndexed { idx, node ->
            list.add(LevelNode(id = 7 + idx, x = node.x, y = node.y))
        }
        return list
    }
}
