FROM maven:3.9.9-amazoncorretto-21
RUN yum install yum-utils -y
RUN yum-config-manager --add-repo https://cli.github.com/packages/rpm/gh-cli.repo
RUN yum install gh -y