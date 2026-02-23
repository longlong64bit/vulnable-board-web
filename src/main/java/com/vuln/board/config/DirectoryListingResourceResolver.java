package com.vuln.board.config;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 정적 리소스가 디렉터리일 때(인덱스 파일 없음) 디렉토리 리스팅을 반환하도록 하는 리졸버.
 * 웹 서버 설정 오류(디렉토리 리스팅 허용) 시뮬레이션.
 */
public class DirectoryListingResourceResolver extends PathResourceResolver {

    @Override
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
        Resource r = super.getResource(resourcePath, location);

        // super가 디렉터리인 경우 null을 반환할 수 있음 → 직접 디렉터리면 목록 반환
        try {
            Path basePath = location.getFile().toPath();
            Path target = resourcePath == null || resourcePath.isEmpty()
                    ? basePath
                    : basePath.resolve(resourcePath).normalize();
            if (Files.exists(target) && Files.isDirectory(target)) {
                String pathForUrl = (resourcePath == null || resourcePath.isEmpty()) ? "" : (resourcePath.endsWith("/") ? resourcePath : resourcePath + "/");
                return new DirectoryListingResource(target, "/uploads/" + pathForUrl);
            }
        } catch (Exception ignored) {
        }

        if (r != null && r.exists() && !r.isFile()) {
            try {
                Path path = location.getFile().toPath().resolve(resourcePath).normalize();
                if (Files.exists(path) && Files.isDirectory(path)) {
                    String pathForUrl = (resourcePath == null || resourcePath.isEmpty()) ? "" : (resourcePath.endsWith("/") ? resourcePath : resourcePath + "/");
                    return new DirectoryListingResource(path, "/uploads/" + pathForUrl);
                }
            } catch (Exception ignored) {
            }
        }
        return r;
    }
}
