# build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# production stage
FROM node:22-trixie-slim AS production
WORKDIR /app
ENV NODE_ENV=production
COPY package*.json .
RUN npm ci --omit=dev
COPY --from=build /app/.next ./.next
COPY --from=build /app/public ./public
COPY --from=build /app/next.config.ts ./next.config.ts
EXPOSE 3000
CMD ["npm", "start"]

# development stage
FROM node:22-trixie-slim AS development
WORKDIR /app
ENV NODE_ENV=development
COPY package*.json .
EXPOSE 3000
CMD ["npm", "run", "dev"]