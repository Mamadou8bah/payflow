FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG NEXT_PUBLIC_API_BASE_URL
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL
ENV NEXT_TELEMETRY_DISABLED=1
RUN mkdir -p public && npm run build

FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV HOSTNAME=0.0.0.0
ENV PORT=3000
RUN addgroup -S sutura && adduser -S sutura -G sutura
COPY --from=build --chown=sutura:sutura /app/.next/standalone ./
COPY --from=build --chown=sutura:sutura /app/.next/static ./.next/static
COPY --from=build --chown=sutura:sutura /app/public ./public
USER sutura
EXPOSE 3000
CMD ["node", "server.js"]
