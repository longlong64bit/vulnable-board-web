# RSC CVE-2025-55182 테스트용 (React 19.2.0 + @vitejs/plugin-rsc 0.5.2)
FROM node:20-alpine
WORKDIR /app
COPY frontend-rsc/package.json frontend-rsc/package-lock.json* ./
RUN npm install
COPY frontend-rsc/ .
EXPOSE 5174
CMD ["npm", "run", "dev"]
