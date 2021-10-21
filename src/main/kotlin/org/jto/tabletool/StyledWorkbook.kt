package org.jto.tabletool

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*

class StyledWorkbook() : XSSFWorkbook() {

    val fonts:Map<String, XSSFFont>
    val styles: Map<String, CellStyle>

    init {
        fonts = createFonts()
        styles = createStyles()
    }

    private fun createStyles(): Map<String, XSSFCellStyle> = mapOf(
        "leftOnly" to createSolidStyle( rgb(206, 250, 221)), //IndexedColors.LIGHT_GREEN
        "rightOnly" to createSolidStyle( rgb(254, 207, 200)), //IndexedColors.ROSE
        "notComparableLeft" to createSolidStyle( IndexedColors.GREY_25_PERCENT),
        "notComparableRight" to createSolidStyle( IndexedColors.GREY_25_PERCENT),
        "comparable" to createSolidStyle( rgb(237, 197, 69)),
        "header" to createSolidStyle( IndexedColors.GREY_40_PERCENT).apply { setFont(fonts["bold"]) },
        "normal" to createStyle(),
        "regex" to createStyle().apply { setFont(fonts["regex"]) }
    )

    private fun rgb(r: Int, g: Int, b: Int): XSSFColor =
        XSSFColor(byteArrayOf(r.toByte(), g.toByte(), b.toByte()), DefaultIndexedColorMap())

    private fun createStyle(): XSSFCellStyle {
        val style = createCellStyle()
        applyAllCorners(style)
        return style
    }

    private fun createSolidStyle(customColor: XSSFColor): XSSFCellStyle {
        val style = createCellStyle()
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.setFillForegroundColor(customColor)
        applyAllCorners(style)
        return style
    }

    private fun createSolidStyle( indexedColors: IndexedColors): XSSFCellStyle {
        val style = createCellStyle()
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.fillForegroundColor = indexedColors.index
        applyAllCorners(style)
        return style
    }

    private fun applyAllCorners(style: CellStyle) {
        style.borderTop = BorderStyle.THIN
        style.borderBottom = BorderStyle.THIN
        style.borderLeft = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
    }

    private fun createFonts(): Map<String, XSSFFont> =
        mapOf(
            "regex" to createFont().apply { setColor(rgb(0x00,0x70,0xC0)) },
            "underline" to createFont().apply { underline = XSSFFont.U_SINGLE; color = IndexedColors.GREEN.index }, //color = IndexedColors.RED.index
            "strikeout" to createFont().apply {
                strikeout = true; family = FontFamily.SWISS.value; setColor(rgb(141, 25, 17))
            },
            "bold" to createFont().apply { bold = true },
            "normal" to createFont()
        )

}