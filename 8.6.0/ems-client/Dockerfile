FROM tibco/base:1.0

ARG EMS_VERSION

RUN mkdir -p /home/tibuser/tibco
RUN chmod -R 777 /home/tibuser/tibco/

COPY ems/ /home/tibuser/tibco/ems
RUN chmod -R 777 /home/tibuser/tibco/ems

ENV TIBCO_HOME=/home/tibuser/tibco
ENV EMS_HOME=/home/tibuser/tibco/ems/$EMS_VERSION
RUN echo $EMS_HOME
ENV PATH=$PATH:$EMS_HOME/bin
ENV LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$EMS_HOME/lib

COPY ems_scripts/. $TIBCO_HOME/ems_scripts
RUN chmod -R 777 $TIBCO_HOME/ems_scripts/*

COPY entrypoint.sh $TIBCO_HOME/entrypoint.sh
RUN chmod -R 777 $TIBCO_HOME/entrypoint.sh

USER root

ENTRYPOINT ["/home/tibuser/tibco/entrypoint.sh"]