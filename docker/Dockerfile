FROM ubuntu:18.04
LABEL maintainer="ashish@dbmi.emory.edu"

# Add group Bindaas and user Bindaas
RUN groupadd -g 9999 bindaas && \
    useradd -r -u 9999 -g bindaas bindaas

### update
RUN apt-get -q update
RUN apt-get -q -y upgrade
RUN apt-get -q -y dist-upgrade
RUN apt-get install -q -y wget

WORKDIR /root/src

RUN apt-get install -y openjdk-8-jre

# Add java to path
ENV PATH /root/src/jre1.8.0_171/bin:$PATH

RUN chown -R bindaas:bindaas /root/

WORKDIR /root/bindaas
RUN wget https://github.com/sharmalab/bindaas/releases/download/v3.2.0/bindaas-dist-3.2.0.tar.gz

RUN tar -xvf bindaas-dist-3.2.0.tar.gz && rm bindaas-dist-3.2.0.tar.gz

RUN chown -R bindaas:bindaas /root/bindaas

USER bindaas

COPY bindaas.config.json /root/bindaas/bin/

EXPOSE 9099
EXPOSE 8080
WORKDIR /root/bindaas/bin

COPY /run.sh /root/bindaas/bin/run.sh


CMD ["sh", "run.sh"]
