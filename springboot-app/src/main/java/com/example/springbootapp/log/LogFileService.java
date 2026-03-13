package com.example.springbootapp.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 直接從磁碟讀取 log 檔案，解析為 {@link LogEntry} 清單。
 * 路徑與 logback-spring.xml 一致（LOG_PATH / LOG_FILE），支援依 traceId 篩選與 tail。
 */
@Service
public class LogFileService {

    private static final Logger logger = LoggerFactory.getLogger(LogFileService.class);
    private static final DateTimeFormatter ROLLED_SUFFIX = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${LOG_PATH:logs}")
    private String logPath;

    @Value("${LOG_FILE:springboot}")
    private String logFile;

    /**
     * 取得日誌列表。會讀取當前主檔與今日滾動檔（若存在），再依參數篩選。
     *
     * @param traceId 若非空，只回傳該 traceId 的日誌（Spring Cloud 追蹤用）
     * @param level   若非空，只回傳該 level（如 INFO、ERROR）
     * @param tail    若 &gt; 0，只回傳最後 N 筆（由時間順序取尾）
     * @return 日誌實體列表，每筆含 traceId
     */
    public List<LogEntry> getLogs(String traceId, String level, int tail) {
        List<Path> files = resolveLogFiles();
        List<LogEntry> all = new ArrayList<>();
        for (Path path : files) {
            if (!Files.isRegularFile(path)) {
                continue;
            }
            try {
                List<LogEntry> entries = readAndParse(path);
                all.addAll(entries);
            } catch (IOException e) {
                logger.warn("Failed to read log file: {}", path, e);
            }
        }
        if (traceId != null && !traceId.trim().isEmpty()) {
            all = all.stream()
                    .filter(e -> traceId.equals(e.getTraceId()))
                    .collect(Collectors.toList());
        }
        if (level != null && !level.trim().isEmpty()) {
            String levelUpper = level.trim().toUpperCase();
            all = all.stream()
                    .filter(e -> levelUpper.equals(e.getLevel()))
                    .collect(Collectors.toList());
        }
        // 依時間排序（假設 timestamp 字串可排序）
        all.sort((a, b) -> {
            String ta = a.getTimestamp() != null ? a.getTimestamp() : "";
            String tb = b.getTimestamp() != null ? b.getTimestamp() : "";
            return ta.compareTo(tb);
        });
        if (tail > 0 && all.size() > tail) {
            all = all.subList(all.size() - tail, all.size());
        }
        logger.info("total entries after filter: {}", all.size());
        return all;
    }

    /** 解析要讀取的 log 檔案：主檔 + 今日滾動檔（與 logback 檔名規則一致） */
    private List<Path> resolveLogFiles() {
        Path dir = Paths.get(logPath).toAbsolutePath();
        String baseName = logFile;
        Path main = dir.resolve(baseName + ".log");
        String today = LocalDate.now().format(ROLLED_SUFFIX);
        Path rolled = dir.resolve(baseName + "." + today + ".log");
        return Stream.of(main, rolled)
                .filter(Files::exists)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<LogEntry> readAndParse(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<LogEntry> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                LogEntry entry = objectMapper.readValue(line, LogEntry.class);
                result.add(entry);
            } catch (Exception e) {
                // 非 JSON 行（例如 stack trace 延續）可選擇略過或附到上一筆
                logger.trace("Skip non-JSON line in {}: {}", path.getFileName(), line);
            }
        }
        return result;
    }
}
