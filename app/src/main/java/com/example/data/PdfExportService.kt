package com.example.data

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.example.data.SupportTicket

object PdfExportService {
    fun exportMaintenanceReport(context: Context, tickets: List<SupportTicket>) {
        val fileName = "MaintenanceReport_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        try {
            val pdfWriter = PdfWriter(FileOutputStream(file))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            
            document.add(Paragraph("Maintenance Report").setFontSize(20f).setBold())
            document.add(Paragraph("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}"))
            document.add(Paragraph("\n"))
            
            tickets.forEach { ticket ->
                document.add(Paragraph("Ticket ID: ${ticket.ticketNumber}"))
                document.add(Paragraph("Subject: ${ticket.subject}"))
                document.add(Paragraph("Status: ${ticket.status}"))
                document.add(Paragraph("Category: ${ticket.category}"))
                document.add(Paragraph("Priority: ${ticket.priority}"))
                document.add(Paragraph("-----------------------------------"))
            }
            
            document.close()
            Toast.makeText(context, "PDF exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
