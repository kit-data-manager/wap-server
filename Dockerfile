FROM openjdk:17-bullseye

ARG SERVER_NAME_DEFAULT=wapserver

#This is the post that you want to use on the HOST machine, not in the container itself
ARG PORT_DEFAULT=8090
ARG SPARQ_DEFAULT=3330
ARG HOST_DEFAULT=localhost
ARG ROOT_DIRECTORY_DEFAULT=/spring

# Install git as additional requirement
RUN apt-get -y update && \
    apt-get -y upgrade  && \
    apt-get install -y --no-install-recommends git bash && \
    apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV SERVICE_DIRECTORY=${ROOT_DIRECTORY_DEFAULT}/${SERVER_NAME_DEFAULT}
RUN mkdir -p /git
WORKDIR /git
COPY . .
# Build service in given directory
RUN echo $SERVICE_DIRECTORY
RUN bash ./build.sh $SERVICE_DIRECTORY

#You can use this to set config variables or mount in an application.properties file
ENV SPRING_APPLICATION_JSON "{\"WapPort\": ${PORT_DEFAULT}, \"Hostname\": \"${HOST_DEFAULT}\"}"

EXPOSE ${PORT_DEFAULT}
EXPOSE ${SPARQL_DEFAULT}
WORKDIR $SERVICE_DIRECTORY
COPY ./profiles ./profiles/
COPY ./schemas ./schemas/
COPY ./webcontent ./webcontent/
COPY ./doc ./doc/
ENTRYPOINT ["/spring/wapserver/run.sh"]