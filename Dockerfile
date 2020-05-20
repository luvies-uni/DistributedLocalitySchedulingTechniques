FROM gradle:jdk14 as build

WORKDIR /src
COPY . .

ARG TARGET
ENV BUILD_DIR=${TARGET}/build/distributions
ENV SNAPSHOT=${BUILD_DIR}/${TARGET}-1.0-SNAPSHOT
ENV TAR=${SNAPSHOT}.tar
ENV BASE_OUT=/src/out

RUN gradle :${TARGET}:build --no-daemon --configure-on-demand --parallel && \
  tar x -f ${TAR} -C ${BUILD_DIR} && \
  mv ${SNAPSHOT} ${BASE_OUT}

FROM openjdk:14-alpine as final

ARG TARGET
ENV TARGET=${TARGET}

WORKDIR /app
COPY --from=build /src/out .

CMD /bin/sh /app/bin/${TARGET}
