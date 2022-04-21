FROM centos

COPY jdk-11*_linux-x64*.tar.gz /

ENV PATH=/usr/java/jdk-11.*/bin:$PATH \
    TIBCO_HOME=/home/tibuser/tibco \
    OM_HOME=/home/tibuser/tibco/om/5.1 

RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-Linux-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-Linux-*

RUN yum clean all \
    && yum -y update \
    && yum -y install wget unzip \
	  && yum -y install rsync \
    && mkdir /usr/java \
    && tar xvf jdk-11*_linux-x64*.tar.gz -C /usr/java/ \
    && rm jdk-11*_linux-x64*.tar.gz \
	  && groupadd -r tibuser && useradd -r -m -g tibuser tibuser \
    && mkdir -p /home/tibuser/tibco/om/5.1

COPY ftl_lib $OM_HOME/ftl_lib

ENV LD_LIBRARY_PATH=$OM_HOME/ftl_lib/lib
   
ENV JAVA_HOME /usr/java/jdk-11.0.4

ENV PATH $JAVA_HOME/bin:$PATH

RUN export JAVA_HOME