#-------------------------------------------------------------------------------
# Copyright (C) 2017 Create-Net / FBK.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
# 
# Contributors:
#     Create-Net / FBK - initial API and implementation
#-------------------------------------------------------------------------------

ARG BASEIMAGE_BUILD=agileiot/raspberry-pi3-zulujdk:8-jdk-maven
ARG BASEIMAGE_DEPLOY=agileiot/raspberry-pi3-zulujdk:8-jre

FROM $BASEIMAGE_BUILD

# Add packages
RUN apt-get update && apt-get install --no-install-recommends -y \
    build-essential \
    git\
    ca-certificates \
    apt \
    software-properties-common \
    unzip \
    cpp \
    binutils \
    maven \
    gettext \
    libc6-dev \
    make \
    cmake \
    cmake-data \
    pkg-config \
    clang \
    gcc-4.9 \
    g++-4.9 \
    qdbus \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# resin-sync will always sync to /usr/src/app, so code needs to be here.
WORKDIR /usr/src/app
ENV APATH /usr/src/app

COPY scripts scripts

COPY om2m om2m

COPY agile-dbus-java-interface agile-dbus-java-interface
RUN CC=clang CXX=clang++ CMAKE_C_COMPILER=clang CMAKE_CXX_COMPILER=clang++ \
scripts/install-agile-interfaces.sh $APATH/deps

# copy directories into WORKDIR
COPY org.eclipse.agail.protocol.ONEM2M org.eclipse.agail.protocol.ONEM2M

COPY org.eclipse.om2m.commons_1.0.0.20171019-1403.jar $APATH/deps/org.eclipse.om2m.commons_1.0.0.20171019-1403.jar

COPY org.eclipse.om2m.core.service_1.0.0.20171019-1403.jar $APATH/deps/org.eclipse.om2m.core.service_1.0.0.20171019-1403.jar

COPY org.eclipse.om2m.interworking.service_1.0.0.20171019-1403.jar $APATH/deps/org.eclipse.om2m.interworking.service_1.0.0.20171019-1403.jar

COPY onem2m-client-core-1.0.0.jar $APATH/deps/onem2m-client-core-1.0.0.jar

COPY onem2m-client-http-1.0.0.jar $APATH/deps/onem2m-client-http-1.0.0.jar

COPY onem2m-resource-mapper-1.0.0.jar $APATH/deps/onem2m-resource-mapper-1.0.0.jar

RUN mvn install:install-file -Dfile=$APATH/deps/org.eclipse.om2m.commons_1.0.0.20171019-1403.jar \
                         -DgroupId=org.eclipse.om2m \
                         -DartifactId=org.eclipse.om2m.commons \
                         -Dversion=1.0.0-SNAPSHOT \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$APATH/deps

RUN mvn install:install-file -Dfile=$APATH/deps/org.eclipse.om2m.core.service_1.0.0.20171019-1403.jar \
                         -DgroupId=org.eclipse.om2m \
                         -DartifactId=org.eclipse.om2m.core.service \
                         -Dversion=1.0.0-SNAPSHOT \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$APATH/deps

RUN mvn install:install-file -Dfile=$APATH/deps/org.eclipse.om2m.interworking.service_1.0.0.20171019-1403.jar \
                         -DgroupId=org.eclipse.om2m \
                         -DartifactId=org.eclipse.om2m.interworking.service \
                         -Dversion=1.0.0-SNAPSHOT \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$APATH/deps


RUN mvn install:install-file -Dfile=$APATH/deps/onem2m-client-core-1.0.0.jar \
                         -DgroupId=com.srcsolution.things.onem2m-client \
                         -DartifactId=onem2m-client-core \
                         -Dversion=1.0.0\
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$APATH/deps


RUN mvn install:install-file -Dfile=$APATH/deps/onem2m-client-http-1.0.0.jar \
                         -DgroupId=com.srcsolution.things.onem2m-client \
                         -DartifactId=onem2m-client-http \
                         -Dversion=1.0.0 \
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$APATH/deps


RUN mvn install:install-file -Dfile=$APATH/deps/onem2m-resource-mapper-1.0.0.jar \
                         -DgroupId=com.srcsolution.things.interworking \
                         -DartifactId=onem2m-resource-mapper \
                         -Dversion=1.0.0\
                         -Dpackaging=jar \
                         -DgeneratePom=true \
                         -DlocalRepositoryPath=$APATH/deps


RUN ls $APATH/deps

RUN cd org.eclipse.agail.protocol.ONEM2M && mvn package -DskipTests

FROM $BASEIMAGE_DEPLOY
WORKDIR /usr/src/app
ENV APATH /usr/src/app

RUN apt-get update && apt-get install --no-install-recommends -y \
    build-essential \
    git\
    apt \
    software-properties-common \
    unzip \
    cpp \
    binutils \
    gettext \
    libc6-dev \
    make \
    cmake \
    cmake-data \
    pkg-config \
    clang \
    gcc-4.9 \
    g++-4.9 \
    qdbus \
    autotools-dev \
    autoconf \
    libusb-1.0-0-dev \
    pkg-config \
    dpkg-dev \
    debhelper \
    python3 \
    python3-setuptools \
    python3-pip \
    flex \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN pip3 install pyusb

RUN git clone git://github.com/onitake/daliserver.git
RUN cd daliserver && autoreconf -i &&  dpkg-buildpackage
RUN dpkg -i daliserver*.deb

RUN git clone git://github.com/sde1000/python-dali.git
RUN cd python-dali && python3 setup.py install


COPY --from=0 $APATH/scripts scripts
COPY --from=0 $APATH/om2m om2m
COPY --from=0 $APATH/deps deps
COPY --from=0 $APATH/org.eclipse.agail.protocol.ONEM2M org.eclipse.agail.protocol.ONEM2M
RUN pkill -f org.eclipse.equinox.launcher

CMD [ "bash", "/usr/src/app/scripts/start.sh" ]
