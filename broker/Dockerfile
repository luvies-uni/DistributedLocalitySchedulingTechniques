FROM alpine as base

ENV ACTIVEMQ_HOME=/activemq \
  ACTIVEMQ_TAR=apache-activemq-5.15.12-bin.tar.gz

WORKDIR /
COPY ./${ACTIVEMQ_TAR} .
RUN mkdir -p ${ACTIVEMQ_HOME} && \
  tar xf ${ACTIVEMQ_TAR} -C ${ACTIVEMQ_HOME} --strip-components 1 && \
  sed -i "s/brokerName=\"localhost\"/useJmx=\"true\" brokerName=\"localhost\"/g" ${ACTIVEMQ_HOME}/conf/activemq.xml

FROM openjdk:15-alpine as final

ENV ACTIVEMQ_TCP=61616 \
  ACTIVEMQ_AMQP=5672 \
  ACTIVEMQ_STOMP=61613 \
  ACTIVEMQ_MQTT=1883 \
  ACTIVEMQ_WS=61614 \
  ACTIVEMQ_UI=8161 \
  ACTIVEMQ_JMX=1099 \
  ACTIVEMQ_HOME=/activemq \
  ACTIVEMQ_SUNJMX_START="-Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

WORKDIR /${ACTIVEMQ_HOME}
COPY --from=base /${ACTIVEMQ_HOME} .

CMD ["bin/activemq", "console"]
