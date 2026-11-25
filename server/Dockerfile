FROM node:20-alpine AS builder

WORKDIR /app

# Copy server files
COPY package.json tsconfig.json ./
RUN npm install

COPY src ./src
COPY sql ./sql
RUN npm run build

FROM node:20-alpine AS runner

WORKDIR /app
ENV NODE_ENV=production

COPY package.json ./
RUN npm install --omit=dev

COPY --from=builder /app/dist ./dist
COPY sql ./sql
COPY ../web ./web

EXPOSE 5000

CMD ["node", "dist/index.js"]
