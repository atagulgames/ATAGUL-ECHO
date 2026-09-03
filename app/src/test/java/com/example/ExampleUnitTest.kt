package com.example

import com.example.data.LevelCatalog
import com.example.game.CollisionEngine
import com.example.model.Point
import com.example.model.Segment
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun ccw_lineIntersection_crossesProperly() {
        // Two lines crossing in X shape: (0,0)-(100,100) and (0,100)-(100,0)
        val p1 = Point(0f, 0f)
        val p2 = Point(100f, 100f)
        val p3 = Point(0f, 100f)
        val p4 = Point(100f, 0f)

        assertTrue(CollisionEngine.doLinesIntersect(p1, p2, p3, p4))
    }

    @Test
    fun ccw_lineIntersection_parallelDoesNotIntersect() {
        // Parallel lines: (0,0)-(100,0) and (0,50)-(100,50)
        val p1 = Point(0f, 0f)
        val p2 = Point(100f, 0f)
        val p3 = Point(0f, 50f)
        val p4 = Point(100f, 50f)

        assertFalse(CollisionEngine.doLinesIntersect(p1, p2, p3, p4))
    }

    @Test
    fun ccw_sharedEndpoint_doesNotTriggerIntersection() {
        // Segments meeting at a vertex (node)
        val p1 = Point(0f, 0f)
        val p2 = Point(50f, 50f)
        val p3 = Point(50f, 50f)
        val p4 = Point(100f, 0f)

        assertFalse(CollisionEngine.doLinesIntersect(p1, p2, p3, p4))
    }

    @Test
    fun selfIntersection_detectsCrossInActiveStroke() {
        // Active stroke: (0,0) -> (100,0) -> (100,100)
        val seg1 = Segment(Point(0f, 0f), Point(100f, 0f))
        val seg2 = Segment(Point(100f, 0f), Point(100f, 100f))
        val activeSegments = listOf(seg1, seg2)

        // Candidate crossing seg1: from (100,100) to (50, -50)
        val candidate = Segment(Point(100f, 100f), Point(50f, -50f))

        assertTrue(CollisionEngine.checkSelfIntersection(activeSegments, candidate))
    }

    @Test
    fun proximity_distanceCalculation_isAccurate() {
        val p = Point(50f, 20f)
        val segment = Segment(Point(0f, 0f), Point(100f, 0f))

        val dist = CollisionEngine.distanceToSegment(p, segment.p1, segment.p2)
        assertEquals(20f, dist, 0.01f)
    }

    @Test
    fun levelCatalog_generates100Levels() {
        val levels = LevelCatalog.create100Levels()
        assertEquals(100, levels.size)
        assertEquals(1, levels.first().id)
        assertEquals(100, levels.last().id)
        assertTrue(levels.all { it.title.isNotEmpty() })
        assertTrue(levels.all { it.nodesJson.isNotEmpty() })

        // Verify that in all 100 levels, the character starting node is Node 1
        for (entity in levels) {
            val levelData = LevelCatalog.entityToLevelData(entity)
            assertTrue("Level ${entity.id} should have nodes", levelData.nodes.isNotEmpty())
            assertEquals("Level ${entity.id} starting node must be ID 1", 1, levelData.nodes.first().id)
            val ids = levelData.nodes.map { it.id }
            assertEquals("Level ${entity.id} nodes should have unique IDs", ids.size, ids.toSet().size)
            assertTrue("Level ${entity.id} must contain node 1", ids.contains(1))
        }
    }
}
