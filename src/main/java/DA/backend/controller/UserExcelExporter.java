package DA.backend.controller; // Đảm bảo đúng package

import DA.backend.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class UserExcelExporter {
    private XSSFWorkbook workbook;
    private Sheet sheet;
    private List<User> listUsers;

    public UserExcelExporter(List<User> listUsers) {
        this.listUsers = listUsers;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Users");
    }

    private void writeHeader() {
        Row headerRow = sheet.createRow(0);
        CellStyle style = createCellStyle(true);

        // Danh sách các tiêu đề cột
        String[] headers = {
                "User ID", "E-mail", "Full Name", "Ngày Sinh",
                "Số Điện Thoại", "Địa Chỉ", "Giới Tính",
                "Quốc Tịch", "Quê Quán", "Phòng Ban",

        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
        }
    }

    private void writeData() {
        int rowCount = 1;
        CellStyle style = createCellStyle(false);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (User user : listUsers) {
            Row row = sheet.createRow(rowCount++);

            createCell(row, 0, user.getId(), style);
            createCell(row, 1, user.getEmail(), style);
            createCell(row, 2, user.getName(), style);
            createCell(row, 3, user.getBirthDay() != null ? dateFormat.format(user.getBirthDay()) : "", style);
            createCell(row, 4, user.getPhoneNumber(), style);
            createCell(row, 5, user.getAddress(), style);
            createCell(row, 6, user.getSex(), style);
            createCell(row, 7, user.getNationality(), style);
            createCell(row, 8, user.getHomeTown(), style);
            createCell(row, 9, user.isDelete() ? "Đã nghỉ" : "Đang làm", style);
        }
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        cell.setCellStyle(style);
    }

    private CellStyle createCellStyle(boolean isHeader) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(isHeader ? 16 : 14);
        font.setBold(isHeader);
        style.setFont(font);
        return style;
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeader();
        writeData();
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
