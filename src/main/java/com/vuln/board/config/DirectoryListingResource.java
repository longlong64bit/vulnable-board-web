package com.vuln.board.config;

import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 디렉터리일 때 HTML 목록을 반환하는 Resource.
 * 웹 서버 설정 오류(인덱스 파일 없음 + 디렉토리 리스팅 허용) 시뮬레이션용.
 */
public class DirectoryListingResource implements Resource {

    private final Path directory;
    private final String requestPath;

    public DirectoryListingResource(Path directory, String requestPath) {
        this.directory = directory;
        this.requestPath = requestPath == null || requestPath.isEmpty() ? "/" : requestPath;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Index of ").append(requestPath).append("</title></head><body>");
        html.append("<h1>Index of ").append(requestPath).append("</h1><hr><pre>");
        try (Stream<Path> stream = Files.list(directory)) {
            List<Path> entries = stream.sorted((a, b) -> a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString())).collect(Collectors.toList());
            for (Path p : entries) {
                String name = p.getFileName().toString();
                String link = requestPath.endsWith("/") ? (requestPath + name) : (requestPath + "/" + name);
                if (Files.isDirectory(p)) {
                    html.append("<a href=\"").append(link).append("/\">").append(name).append("/</a>\n");
                } else {
                    html.append("<a href=\"").append(link).append("\">").append(name).append("</a>\n");
                }
            }
        }
        html.append("</pre><hr></body></html>");
        return new ByteArrayInputStream(html.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean exists() {
        return Files.exists(directory) && Files.isDirectory(directory);
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Directory listing for " + directory;
    }

    @Override
    public long contentLength() throws IOException {
        return -1;
    }

    @Override
    public String getFilename() {
        return "index.html";
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new UnsupportedOperationException("createRelative not supported");
    }

    @Override
    public long lastModified() throws IOException {
        return Files.exists(directory) ? Files.getLastModifiedTime(directory).toMillis() : 0L;
    }

    @Override
    public File getFile() throws IOException {
        return directory.toFile();
    }

    @Override
    public URL getURL() throws IOException {
        return directory.toUri().toURL();
    }

    @Override
    public URI getURI() throws IOException {
        return directory.toUri();
    }
}
