package com.ntvelop.goldengoosepda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Σχήματα τραπεζιών
enum class TableShape { T, O, K } // Τετράγωνο, Ορθογώνιο, Κυκλικό

// Μία θέση τραπεζιού στον “χάρτη”
data class TableSpot(
    val table: Int,
    val x: Int,   // στήλη (1..cols)
    val y: Int,   // γραμμή (1..rows)
    val w: Int = 1,
    val h: Int = 1,
    val shape: TableShape
)

// Ετικέτες ζώνης (με στοίχιση)
data class ZoneLabel(
    val text: String,
    val row: Int,
    val colStart: Int,
    val colSpan: Int,
    val align: Alignment = Alignment.CenterStart
)

// Απλές επιγραφές (χωρίς badge/περίγραμμα)
data class FreeText(
    val text: String,
    val col: Int,
    val row: Int,
    val align: Alignment = Alignment.CenterStart
)

// --- ΛΟΓΙΚΟ πλέγμα (όπως οι συντεταγμένες που έδωσες)
private const val LOGICAL_COLS = 10
private const val LOGICAL_ROWS = 8

// Τοποθετήσεις τραπεζιών (ΔΕΝ αλλάζω τις λογικές θέσεις)
private val tablePlan: List<TableSpot> = listOf(
    // ---------- ΧΩΡΟΣ 1: ΕΞΩ (πάνω σειρά: 4 3 2 1 από αριστερά προς δεξιά) ----------
    TableSpot(1, x = 8, y = 1, shape = TableShape.T), // T1(1,7)
    TableSpot(2, x = 7, y = 1, shape = TableShape.T), // T2(1,6)
    TableSpot(3, x = 2, y = 1, shape = TableShape.T), // T3(1,2)
    TableSpot(4, x = 1, y = 1, shape = TableShape.T), // T4(1,1)

    // ---------- ΧΩΡΟΣ 2: ΜΠΡΟΣΤΑ ----------
    TableSpot(5,  x = 3, y = 2, shape = TableShape.T),                    // T5(2,3)
    TableSpot(6,  x = 1, y = 2, w = 2, h = 1, shape = TableShape.O),      // O6(2,1)(2,2)
    TableSpot(7,  x = 1, y = 4, w = 2, h = 1, shape = TableShape.O),      // O7(4,1)(4,2)
    TableSpot(8,  x = 4, y = 3, w = 1, h = 2, shape = TableShape.O),      // O8(3,4)(4,4)

    // ---------- ΧΩΡΟΣ 3: ΜΕΣΑ ----------
    TableSpot(9,  x = 1, y = 5, w = 2, h = 1, shape = TableShape.O),      // O9(5,1)(5,2)
    TableSpot(10, x = 1, y = 7, w = 2, h = 1, shape = TableShape.O),      // O10(7,1)(7,2)
    TableSpot(11, x = 4, y = 6, shape = TableShape.T),                    // T11(6,4)

    // ---------- ΧΩΡΟΣ 4: ΑΥΛΗ ----------
    TableSpot(12, x = 6, y = 6, shape = TableShape.T),                    // T12(6,6)
    TableSpot(13, x = 6, y = 7, shape = TableShape.T),                    // T13(7,6)
    TableSpot(14, x = 8, y = 7, shape = TableShape.T),                    // T14(7,8)
    TableSpot(15, x = 8, y = 6, shape = TableShape.K),                    // K15(6,8)
    TableSpot(16, x = 6, y = 5, w = 2, h = 1, shape = TableShape.O),      // O16(5,6)(5,7)

    // ---------- ΧΩΡΟΣ 5: ΠΙΣΩ ----------
    TableSpot(17, x = 4, y = 8, shape = TableShape.T),                    // T17(8,4)
    TableSpot(19, x = 1, y = 10, w = 2, h = 1, shape = TableShape.O),     // O19(10,1)(9,1)
    TableSpot(20, x = 1, y = 8,  w = 2, h = 1, shape = TableShape.O),     // O20(8,1)(8,2)
    TableSpot(18, x = 3, y = 10, shape = TableShape.T)
)

// --- ΟΠΤΙΚΟ πλέγμα (με extra γραμμές για labels/κενά) ---
private const val VISUAL_COLS = LOGICAL_COLS
private const val VISUAL_ROWS = 14 // χωράει μέχρι και τα 19–18 κάτω

// Χαρτογράφηση λογικών y → οπτικές γραμμές, σύμφωνα με τις απαιτήσεις:
// Row1:   ΕΞΩ (label)
// Row2:   τραπέζια y=1
// Row3:   ΜΠΡΟΣΤΑ (label)
// Row4-6: τραπέζια y=2..4
// Row7:   ΜΕΣΑ (label)
// Row8:   ΑΥΛΗ (label, ίδια γραμμή με το 16), και τραπέζια y=5
// Row9-10:τραπέζια y=6..7
// Row11:  ΠΙΣΩ (label)
// Row12-14:τραπέζια y=8..10
private fun yVisual(y: Int): Int {
    // βάση: μία σταθερή ανύψωση +2 (όπως πριν), αλλά:
    // - y==1 → 2 (μία λιγότερη γραμμή πάνω)
    // - +1 μετά το y=4 (για label ΜΕΣΑ)
    // - +1 μετά το y=7 (για label ΠΙΣΩ)
    if (y == 1) return 2
    var v = y + 2
    if (y >= 5) v += 1     // κενό για "ΜΕΣΑ"
    if (y >= 8) v += 1     // κενό για "ΠΙΣΩ"
    return v
}

private fun mappedSpots(spots: List<TableSpot>) = spots.map { s -> s.copy(y = yVisual(s.y)) }

// Labels ζωνών (με στοίχιση αριστερά/δεξιά όπου ζήτησες)
private val zoneLabels = listOf(
    ZoneLabel("ΕΞΩ",     row = 1,               colStart = 1,  colSpan = 3,  align = Alignment.CenterStart),
    ZoneLabel("ΜΠΡΟΣΤΑ", row = yVisual(2) - 1,  colStart = 1,  colSpan = 3,  align = Alignment.CenterStart), // row 3
    ZoneLabel("ΜΕΣΑ",    row = yVisual(5) - 1,  colStart = 1,  colSpan = 3,  align = Alignment.CenterStart), // row 7
    ZoneLabel("ΑΥΛΗ",    row = yVisual(5),      colStart = 8,  colSpan = 3,  align = Alignment.CenterEnd),   // row 8, τέρμα δεξιά
    ZoneLabel("ΠΙΣΩ",    row = yVisual(8) - 1,  colStart = 1,  colSpan = 3,  align = Alignment.CenterStart)  // row 11
)

// Απλές επιγραφές (χωρίς περίγραμμα)
private val freeTexts = listOf(
    FreeText("ΕΙΣΟΔΟΣ", col = 5, row = yVisual(2), align = Alignment.CenterStart),  // δεξιά από το 5 (y=2)
    FreeText("ΜΠΑΡ",     col = 7, row = yVisual(3), align = Alignment.CenterEnd),   // τέρμα δεξιά στο ύψος του 8 (y=3)
    FreeText("WC",       col = 6, row = yVisual(10), align = Alignment.CenterStart), // δεξιά από το 18 (y=10)
    FreeText("ΚΟΥΖΙΝΑ",  col = 7, row = yVisual(8),  align = Alignment.CenterEnd)   // τέρμα δεξιά
)

// ---------- UI ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableSelectScreen(
    hasOpen: (Int) -> Boolean,
    onBack: () -> Unit,
    onPick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Επιλογή Τραπεζιού") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Πίσω") } }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
                .fillMaxWidth()
                .aspectRatio(VISUAL_COLS.toFloat() / VISUAL_ROWS.toFloat()) // πιο “ψηλό” box για να χωράνε όλα
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                .background(Color(0xFFF7F7F7), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            TableMap(
                cols = VISUAL_COLS,
                rows = VISUAL_ROWS,
                plan = mappedSpots(tablePlan),
                zoneLabels = zoneLabels,
                freeTexts = freeTexts,
                isOpen = { t -> hasOpen(t) },
                onPick = onPick
            )
        }
    }
}

@Composable
private fun TableMap(
    cols: Int,
    rows: Int,
    plan: List<TableSpot>,
    zoneLabels: List<ZoneLabel>,
    freeTexts: List<FreeText>,
    isOpen: (Int) -> Boolean,
    onPick: (Int) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize())  {
        val cellW = maxWidth / cols
        val cellH = maxHeight / rows

        // ΖΩΝΕΣ (labels)
        zoneLabels.forEach { z ->
            val left   = cellW * (z.colStart - 1)
            val top    = cellH * (z.row - 1)
            val width  = cellW * z.colSpan
            val height = cellH * 1

            Box(
                modifier = Modifier
                    .offset(left, top)
                    .size(width, height)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                contentAlignment = z.align
            ) {
                AreaBadge(z.text)
            }
        }

        // ΕΛΕΥΘΕΡΑ ΚΕΙΜΕΝΑ (χωρίς badge)
        freeTexts.forEach { f ->
            val left = cellW * (f.col - 1)
            val top  = cellH * (f.row - 1)
            Box(
                modifier = Modifier
                    .offset(left, top)
                    .size(cellW * 2, cellH), // μικρό “κελί” 2 στηλών
                contentAlignment = f.align
            ) {
                Text(
                    text = f.text,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF666666)
                )
            }
        }

        // ΤΡΑΠΕΖΙΑ (ήδη visual-mapped)
        plan.forEach { spot ->
            val left   = cellW * (spot.x - 1)
            val top    = cellH * (spot.y - 1)
            val width  = cellW * spot.w
            val height = cellH * spot.h
            val open   = isOpen(spot.table)

            Box(
                modifier = Modifier
                    .offset(x = left, y = top)
                    .size(width = width, height = height)
            ) {
                TableNode(
                    number = spot.table,
                    shape = spot.shape,
                    isOpen = open,
                    onClick = { onPick(spot.table) }
                )
            }
        }
    }
}

@Composable
private fun TableNode(
    number: Int,
    shape: TableShape,
    isOpen: Boolean,
    onClick: () -> Unit
) {
    val borderCol = if (isOpen) Color(0xFFCC0000) else Color(0xFF9E9E9E)
    val fillCol   = if (isOpen) Color(0xFFFFE5E5) else Color.White
    val shapeObj = when (shape) {
        TableShape.K -> CircleShape
        TableShape.T -> RoundedCornerShape(10.dp)
        TableShape.O -> RoundedCornerShape(6.dp)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(2.dp, borderCol, shapeObj)
            .background(fillCol, shapeObj)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number",
            style = MaterialTheme.typography.titleMedium,
            color = if (isOpen) Color(0xFFCC0000) else Color(0xFF333333)
        )
    }
}

@Composable
private fun AreaBadge(title: String) {
    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = Color(0xFF333333))
    }
}
