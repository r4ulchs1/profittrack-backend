package com.profitrack.aplicacion.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.profitrack.aplicacion.dto.etapaProyectoDto.EtapaProyectoResponseDto;
import com.profitrack.aplicacion.dto.metricaDto.RentabilidadResponseDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;
import com.profitrack.aplicacion.puerto.entrada.MetricaUseCase;
import com.profitrack.aplicacion.puerto.entrada.ProyectoUseCase;
import com.profitrack.aplicacion.puerto.entrada.ReporteUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService implements ReporteUseCase {

    private final ProyectoUseCase proyectoUseCase;
    private final MetricaUseCase metricaUseCase;

    @Override
    public ByteArrayInputStream generarProyectoExcel(Long proyectoId) {
        ProyectoResponseDto proyecto = proyectoUseCase.obtenerPorId(proyectoId);
        RentabilidadResponseDto rentabilidad = metricaUseCase.calcularRentabilidadActual(proyectoId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Resumen Proyecto");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);

            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 11);
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 1. Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE RENDIMIENTO DEL PROYECTO: " + safeString(proyecto.getNombre()));
            titleCell.setCellStyle(headerStyle);

            // 2. Información General
            int rowNum = 2;
            rowNum = createLabelValueRow(sheet, rowNum, "Código de Proyecto:", safeString(proyecto.getCodigo()));
            rowNum = createLabelValueRow(sheet, rowNum, "Cliente:", safeString(proyecto.getClienteNombre()));
            rowNum = createLabelValueRow(sheet, rowNum, "Líder Asignado:", safeString(proyecto.getLiderNombre()));
            rowNum = createLabelValueRow(sheet, rowNum, "Estado Actual:", safeString(proyecto.getEstado()));
            rowNum = createLabelValueRow(sheet, rowNum, "Fecha Inicio Planificada:", safeDate(proyecto.getFechaInicioPlanificada()));
            rowNum = createLabelValueRow(sheet, rowNum, "Fecha Fin Planificada:", safeDate(proyecto.getFechaFinPlanificada()));

            rowNum++; // Espacio

            // 3. Métricas Financieras
            Row metricsHeader = sheet.createRow(rowNum++);
            Cell mhCell = metricsHeader.createCell(0);
            mhCell.setCellValue("MÉTRICAS FINANCIERAS Y OPERATIVAS");
            mhCell.setCellStyle(titleStyle);

            rowNum = createLabelValueRow(sheet, rowNum, "Precio de Venta:", safeBigDecimal(proyecto.getPrecioVenta()));
            rowNum = createLabelValueRow(sheet, rowNum, "Presupuesto Planificado:", safeBigDecimal(proyecto.getPresupuestoPlanificado()));
            rowNum = createLabelValueRow(sheet, rowNum, "Costo Laboral Real:", safeBigDecimal(rentabilidad.getCostoLaboral()));
            rowNum = createLabelValueRow(sheet, rowNum, "Costo Opex Real:", safeBigDecimal(rentabilidad.getCostoOpex()));
            rowNum = createLabelValueRow(sheet, rowNum, "Costo Real Total (AC):", safeBigDecimal(rentabilidad.getCostoReal()));
            rowNum = createLabelValueRow(sheet, rowNum, "Margen Real:", safeBigDecimal(rentabilidad.getMargenReal()));
            rowNum = createLabelValueRow(sheet, rowNum, "Porcentaje Margen Real:", safeBigDecimal(rentabilidad.getPorcentajeMargen()).toString() + "%");
            rowNum = createLabelValueRow(sheet, rowNum, "CPI (Cost Performance Index):", safeBigDecimal(rentabilidad.getCpi()));
            rowNum = createLabelValueRow(sheet, rowNum, "SPI (Schedule Performance Index):", safeBigDecimal(rentabilidad.getSpi()));
            rowNum = createLabelValueRow(sheet, rowNum, "Rentable:", rentabilidad.getEsRentable() != null && rentabilidad.getEsRentable() ? "SÍ" : "NO");

            rowNum++; // Espacio

            // 4. Etapas del Proyecto
            Row stagesHeader = sheet.createRow(rowNum++);
            Cell shCell = stagesHeader.createCell(0);
            shCell.setCellValue("DESGLOSE DE ETAPAS DEL PROYECTO");
            shCell.setCellStyle(titleStyle);

            Row tableHeader = sheet.createRow(rowNum++);
            tableHeader.createCell(0).setCellValue("ID Etapa");
            tableHeader.createCell(1).setCellValue("Nombre");
            tableHeader.createCell(2).setCellValue("Horas Planificadas");
            tableHeader.createCell(3).setCellValue("Horas Reales");
            tableHeader.createCell(4).setCellValue("Estado");

            for (int i = 0; i < 5; i++) {
                Cell c = tableHeader.getCell(i);
                CellStyle s = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font f = workbook.createFont();
                f.setBold(true);
                s.setFont(f);
                c.setCellStyle(s);
            }

            List<EtapaProyectoResponseDto> etapas = proyecto.getEtapas();
            if (etapas != null) {
                for (EtapaProyectoResponseDto etapa : etapas) {
                    Row r = sheet.createRow(rowNum++);
                    r.createCell(0).setCellValue(etapa.getId() != null ? etapa.getId() : 0L);
                    r.createCell(1).setCellValue(safeString(etapa.getNombre()));
                    r.createCell(2).setCellValue(safeBigDecimal(etapa.getHorasPlanificadas()).doubleValue());
                    r.createCell(3).setCellValue(safeBigDecimal(etapa.getHorasReales()).doubleValue());
                    r.createCell(4).setCellValue(safeString(etapa.getEstado()));
                }
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte Excel: " + e.getMessage(), e);
        }
    }

    @Override
    public ByteArrayInputStream generarProyectoPdf(Long proyectoId) {
        ProyectoResponseDto proyecto = proyectoUseCase.obtenerPorId(proyectoId);
        RentabilidadResponseDto rentabilidad = metricaUseCase.calcularRentabilidadActual(proyectoId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Tipografías
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.NORMAL);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.NORMAL);
            Font normalBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);

            // Título
            Paragraph title = new Paragraph("PROFITTRACK - REPORTE DE RENDIMIENTO DE PROYECTO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Separador
            document.add(new Paragraph("______________________________________________________________________________\n\n"));

            // 1. Información General
            Paragraph pGeneral = new Paragraph("INFORMACIÓN GENERAL DEL PROYECTO", sectionFont);
            pGeneral.setSpacingAfter(10);
            document.add(pGeneral);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);

            addTableCell(infoTable, "Nombre del Proyecto:", normalBold);
            addTableCell(infoTable, safeString(proyecto.getNombre()), normal);
            addTableCell(infoTable, "Código de Proyecto:", normalBold);
            addTableCell(infoTable, safeString(proyecto.getCodigo()), normal);
            addTableCell(infoTable, "Cliente:", normalBold);
            addTableCell(infoTable, safeString(proyecto.getClienteNombre()), normal);
            addTableCell(infoTable, "Líder Asignado:", normalBold);
            addTableCell(infoTable, safeString(proyecto.getLiderNombre()), normal);
            addTableCell(infoTable, "Estado actual:", normalBold);
            addTableCell(infoTable, safeString(proyecto.getEstado()), normal);

            document.add(infoTable);

            // 2. Resumen Financiero
            Paragraph pFinanciero = new Paragraph("RENDIMIENTO FINANCIERO Y OPERATIVO (MÉTRICAS)", sectionFont);
            pFinanciero.setSpacingAfter(10);
            document.add(pFinanciero);

            PdfPTable financialTable = new PdfPTable(2);
            financialTable.setWidthPercentage(100);
            financialTable.setSpacingAfter(15);

            addTableCell(financialTable, "Precio de Venta:", normalBold);
            addTableCell(financialTable, "S/ " + safeBigDecimal(proyecto.getPrecioVenta()).toString(), normal);
            addTableCell(financialTable, "Presupuesto Planificado (PV):", normalBold);
            addTableCell(financialTable, "S/ " + safeBigDecimal(proyecto.getPresupuestoPlanificado()).toString(), normal);
            addTableCell(financialTable, "Costo Laboral Real:", normalBold);
            addTableCell(financialTable, "S/ " + safeBigDecimal(rentabilidad.getCostoLaboral()).toString(), normal);
            addTableCell(financialTable, "Costo Real Total (AC):", normalBold);
            addTableCell(financialTable, "S/ " + safeBigDecimal(rentabilidad.getCostoReal()).toString(), normal);
            addTableCell(financialTable, "Margen Real:", normalBold);
            addTableCell(financialTable, "S/ " + safeBigDecimal(rentabilidad.getMargenReal()).toString(), normal);
            addTableCell(financialTable, "CPI (Indice Rendimiento Costos):", normalBold);
            addTableCell(financialTable, safeBigDecimal(rentabilidad.getCpi()).toString(), normal);
            addTableCell(financialTable, "SPI (Indice Rendimiento Cronograma):", normalBold);
            addTableCell(financialTable, safeBigDecimal(rentabilidad.getSpi()).toString(), normal);
            addTableCell(financialTable, "Estado de Rentabilidad:", normalBold);
            addTableCell(financialTable, rentabilidad.getEsRentable() != null && rentabilidad.getEsRentable() ? "RENTABLE" : "NO RENTABLE (CRÍTICO)", normal);

            document.add(financialTable);

            // 3. Desglose de Etapas
            Paragraph pEtapas = new Paragraph("DESGLOSE DE ETAPAS DEL PROYECTO", sectionFont);
            pEtapas.setSpacingAfter(10);
            document.add(pEtapas);

            PdfPTable stagesTable = new PdfPTable(4);
            stagesTable.setWidthPercentage(100);
            stagesTable.setSpacingAfter(15);

            // Cabeceras
            addTableHeaderCell(stagesTable, "Nombre Etapa", normalBold);
            addTableHeaderCell(stagesTable, "Horas Planificadas", normalBold);
            addTableHeaderCell(stagesTable, "Horas Reales", normalBold);
            addTableHeaderCell(stagesTable, "Estado", normalBold);

            List<EtapaProyectoResponseDto> etapas = proyecto.getEtapas();
            if (etapas != null) {
                for (EtapaProyectoResponseDto etapa : etapas) {
                    addTableCell(stagesTable, safeString(etapa.getNombre()), normal);
                    addTableCell(stagesTable, safeBigDecimal(etapa.getHorasPlanificadas()).toString(), normal);
                    addTableCell(stagesTable, safeBigDecimal(etapa.getHorasReales()).toString(), normal);
                    addTableCell(stagesTable, safeString(etapa.getEstado()), normal);
                }
            }

            document.add(stagesTable);

            // Cierre
            Paragraph pFooter = new Paragraph("\nGenerado por ProfitTrack SaaS - Sistema de Control y Monitoreo de Rentabilidad.", normal);
            pFooter.setAlignment(Element.ALIGN_CENTER);
            document.add(pFooter);

            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte PDF: " + e.getMessage(), e);
        }
    }

    // Helpers
    private int createLabelValueRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value != null ? value.toString() : "");
        return rowNum + 1;
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text != null ? text : "", font));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addTableHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text != null ? text : "", font));
        cell.setPadding(6);
        cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private String safeString(String str) {
        return str != null ? str : "";
    }

    private String safeDate(LocalDate date) {
        if (date == null) return "-";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
