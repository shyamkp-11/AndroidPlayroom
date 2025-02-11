FROM amazoncorretto:21

ENV GRADLE_VERSION 8.12.1
# Install necessary packages and dependencies
RUN yum update -y && yum install -y \
    wget \
    unzip \
    git \
    openssl
#    && rm -rf /var/lib/yum/lists/*

RUN wget https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -O gradle.zip \
    && unzip gradle.zip -d /opt \
    && rm gradle.zip
#    && rm -rf /var/cache/apk/*
# Set environment variables
ENV GRADLE_HOME /opt/gradle-${GRADLE_VERSION}
ENV PATH $PATH:/opt/gradle-${GRADLE_VERSION}/bin/

ENV ANDROID_SDK_ROOT /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

# Download and install Android SDK command-line tools
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \
    && cd ${ANDROID_SDK_ROOT}/cmdline-tools \
    && wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O commandlinetools.zip \
    && unzip commandlinetools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools \
    && mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm commandlinetools.zip \
    && mkdir /.android

# Install SDK packages
RUN yes | sdkmanager --licenses \
    && sdkmanager "platform-tools" "platforms;android-35" "build-tools;34.0.0"

# Make the gradlew script executable
#RUN #chmod +x ./gradlew
RUN chmod -R a+rw /.android
