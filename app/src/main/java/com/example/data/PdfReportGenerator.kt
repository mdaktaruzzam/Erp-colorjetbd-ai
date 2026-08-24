package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {
  private const val TAG = "PdfReportGenerator"

  data class SummaryItem(val label: String, val value: String)
  data class TableRowItem(val columns: List<String>)

  /**
   * Generates a professional PDF report using iText7 and returns the generated File.
   */
  fun generateReportPdf(
    context: Context,
    title: String,
    subtitle: String,
    summaryItems: List<SummaryItem>,
    headers: List<String>,
    rows: List<TableRowItem>
  ): File? {
    return try {
      val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
      val fileName = "ColorJet_Report_$timeStamp.pdf"
      val pdfDir = File(context.cacheDir, "reports")
      if (!pdfDir.exists()) {
        pdfDir.mkdirs()
      }
      val file = File(pdfDir, fileName)

      val writer = PdfWriter(file)
      val pdfDoc = PdfDocument(writer)
      val document = Document(pdfDoc)

      // Colors
      val primaryColor = DeviceRgb(21, 101, 192) // Corporate Blue
      val darkTextColor = DeviceRgb(33, 33, 33)
      val lightGrayColor = DeviceRgb(245, 245, 245)

      // Title & Header
      document.add(
        Paragraph("COLORJET BANGLADESH")
          .setBold()
          .setFontSize(18f)
          .setFontColor(primaryColor)
          .setTextAlignment(TextAlignment.CENTER)
      )
      document.add(
        Paragraph("Quality • Commitment • Service")
          .setFontSize(10f)
          .setFontColor(DeviceRgb(100, 100, 100))
          .setTextAlignment(TextAlignment.CENTER)
          .setMarginBottom(10f)
      )

      document.add(
        Paragraph(title)
          .setBold()
          .setFontSize(14f)
          .setFontColor(darkTextColor)
          .setMarginTop(10f)
      )
      
      document.add(
        Paragraph("$subtitle | Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
          .setFontSize(9f)
          .setFontColor(DeviceRgb(120, 120, 120))
          .setMarginBottom(15f)
      )

      // Summary Section
      if (summaryItems.isNotEmpty()) {
        document.add(Paragraph("Executive Summary").setBold().setFontSize(11f).setFontColor(primaryColor).setMarginBottom(5f))
        
        val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
        summaryTable.setMarginBottom(15f)
        
        for (item in summaryItems) {
          val cellLabel = Cell().add(Paragraph(item.label).setFontSize(9f).setBold().setFontColor(darkTextColor))
          cellLabel.setBackgroundColor(lightGrayColor)
          cellLabel.setPadding(6f)
          
          val cellValue = Cell().add(Paragraph(item.value).setFontSize(9f).setFontColor(darkTextColor))
          cellValue.setPadding(6f)
          
          summaryTable.addCell(cellLabel)
          summaryTable.addCell(cellValue)
        }
        document.add(summaryTable)
      }

      // Data Table Section
      if (headers.isNotEmpty() && rows.isNotEmpty()) {
        document.add(Paragraph("Detailed Records").setBold().setFontSize(11f).setFontColor(primaryColor).setMarginBottom(5f))

        val columnWidths = FloatArray(headers.size) { 100f / headers.size }
        val table = Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth()

        // Headers
        for (header in headers) {
          val headerCell = Cell().add(Paragraph(header).setBold().setFontSize(9f).setFontColor(DeviceRgb(255, 255, 255)))
          headerCell.setBackgroundColor(primaryColor)
          headerCell.setPadding(6f)
          table.addHeaderCell(headerCell)
        }

        // Rows
        var alternate = false
        for (row in rows) {
          val rowBg = if (alternate) lightGrayColor else DeviceRgb(255, 255, 255)
          for (col in row.columns) {
            val cell = Cell().add(Paragraph(col).setFontSize(8f).setFontColor(darkTextColor))
            cell.setBackgroundColor(rowBg)
            cell.setPadding(5f)
            table.addCell(cell)
          }
          alternate = !alternate
        }

        document.add(table)
      }

      // Footer
      document.add(
        Paragraph("\nConfidential - ColorJet Business Management Mobile App")
          .setFontSize(8f)
          .setFontColor(DeviceRgb(150, 150, 150))
          .setTextAlignment(TextAlignment.CENTER)
          .setMarginTop(30f)
      )

      document.close()
      file
    } catch (e: Exception) {
      Log.e(TAG, "Error generating PDF report", e)
      null
    }
  }

  /**
   * Shares or opens the generated PDF file using FileProvider.
   */
  fun sharePdfReport(context: Context, pdfFile: File) {
    try {
      val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdfFile
      )

      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      val chooser = Intent.createChooser(intent, "Open PDF Report").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    } catch (e: Exception) {
      Log.e(TAG, "Error sharing/opening PDF report", e)
      try {
        // Fallback to SEND intent
        val uri: Uri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "application/pdf"
          putExtra(Intent.EXTRA_STREAM, uri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share PDF Report").apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
      } catch (ex: Exception) {
        Log.e(TAG, "Fallback share failed", ex)
      }
    }
  }
}
