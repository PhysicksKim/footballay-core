package com.gyechunsik.scoreboard.domain.football.service;

import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FootballExcelService {

    private final PlayerRepository playerRepository;
    public ByteArrayInputStream createPlayerExcel(List<Player> players) throws IOException {
        String[] COLUMNs = {"ID", "Name", "Korean Name", "Number", "Photo"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Players");

            // 열 너비 및 행 높이 설정
            int photoColumnIdx = 4;
            sheet.setColumnWidth(photoColumnIdx, 30 * 256); // 사진 열 너비 넓게 설정
            sheet.setColumnWidth(0, 10 * 256); // ID 열 너비
            sheet.setColumnWidth(1, 20 * 256); // Name 열 너비
            sheet.setColumnWidth(2, 20 * 256); // Korean Name 열 너비
            sheet.setColumnWidth(3, 10 * 256); // Number 열 너비

            int rowHeightInPoints = 100; // 행 높이 (100px)

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < COLUMNs.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(COLUMNs[col]);
            }

            // 데이터 생성
            int rowIdx = 1;
            log.info("excel data players: {}", players);
            for (Player player : players) {
                Row row = sheet.createRow(rowIdx);
                row.setHeightInPoints(rowHeightInPoints); // 각 행의 높이 지정

                row.createCell(0).setCellValue(player.getId());
                row.createCell(1).setCellValue(player.getName());
                row.createCell(2).setCellValue(player.getKoreanName() != null ? player.getKoreanName() : "");
                row.createCell(3).setCellValue(player.getNumber() != null ? player.getNumber() : 0);

                // 이미지 삽입
                if (player.getPhotoUrl() != null && !player.getPhotoUrl().isEmpty()) {
                    try (InputStream inputStream = new URL(player.getPhotoUrl()).openStream()) {
                        byte[] bytes = IOUtils.toByteArray(inputStream);
                        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                        CreationHelper helper = workbook.getCreationHelper();
                        Drawing<?> drawing = sheet.createDrawingPatriarch();

                        ClientAnchor anchor = helper.createClientAnchor();
                        anchor.setCol1(photoColumnIdx);
                        anchor.setRow1(rowIdx);
                        anchor.setDx1(0);
                        anchor.setDy1(0);
                        anchor.setDy2(100 * Units.EMU_PER_PIXEL); // 이미지 높이 100px로 설정
                        Picture pict = drawing.createPicture(anchor, pictureIdx);
                        pict.resize(); // width는 자동으로 조정
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                rowIdx++;
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public void updatePlayerKoreanNames(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            log.info("sheet row count : {}", sheet.getPhysicalNumberOfRows());
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // Skip header row
                }

                Long playerId = (long) row.getCell(0).getNumericCellValue();
                String koreanName = row.getCell(2).getStringCellValue();

                Player player = playerRepository.findById(playerId).orElseThrow(() -> new RuntimeException("Player not found"));
                player.setKoreanName(koreanName);
                playerRepository.save(player);
            }
        }
    }
}
