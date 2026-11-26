FROM node:20-alpine AS builder

WORKDIR /app

# Copy server package definition & configs
COPY server/package.json server/tsconfig.json ./
RUN npm install

COPY server/src ./src
COPY server/sql ./sql
RUN npm run build

FROM node:20-alpine AS runner

WORKDIR /app
ENV NODE_ENV=production

COPY server/package.json ./
RUN npm install --omit=dev

COPY --from=builder /app/dist ./dist
COPY server/sql ./sql
COPY web ./web

EXPOSE 5000

CMD ["node", "dist/index.js"]
