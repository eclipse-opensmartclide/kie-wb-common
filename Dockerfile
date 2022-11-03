####### BUILD ############
FROM maven:3.8.4-jdk-11 AS build

## Kie Common Stunner Build ##
COPY /kie-wb-common-dmn/kie-wb-common-dmn-api common-api
RUN mvn -f common-api/pom.xml install -DskipTests

## Kie Common Stunner Build ##
COPY /kie-wb-common-dmn/kie-wb-common-dmn-backend common-backend
RUN mvn -f common-backend/pom.xml install -DskipTests

## Kie Common Stunner Build ##
COPY /kie-wb-common-stunner/kie-wb-common-stunner-sets/kie-wb-common-stunner-bpmn/kie-wb-common-stunner-bpmn-client common
RUN mvn -f common/pom.xml install -DskipTests

## Kie Distributions Build ##
RUN apt-get update && \
    apt-get install git
RUN git clone https://github.com/kiegroup/kie-wb-distributions
RUN cd kie-wb-distributions && git checkout 7.61.0.Final
RUN mvn -f kie-wb-distributions/business-central-parent/business-central-webapp install -DskipTests


####### WILDFLY ############
FROM jboss/wildfly:19.1.0.Final

## ENVIRONMENT ##
ENV JBOSS_BIND_ADDRESS 0.0.0.0
ENV KIE_REPOSITORY https://download.jboss.org/jbpm/release
ENV KIE_VERSION 7.61.0.Final
ENV KIE_CLASSIFIER wildfly19
ENV KIE_CONTEXT_PATH business-central
ENV KIE_SERVER_ID sample-server
ENV KIE_SERVER_LOCATION http://localhost:8080/kie-server/services/rest/server
ENV EXTRA_OPTS -Dorg.jbpm.ht.admin.group=admin -Dorg.uberfire.nio.git.ssh.host=$JBOSS_BIND_ADDRESS
ENV KEYCLOAK_VERSION 14.0.0
ENV SMARTCLIDE_THEIA_URL 127.0.0.1:3030
ENV SMARTCLIDE_SERVICE_DISCOVERY_URL 127.0.0.1:2020

## JBPM-WB ##
RUN curl -o $HOME/jbpm-server-dist.zip $KIE_REPOSITORY/$KIE_VERSION/jbpm-server-$KIE_VERSION-dist.zip && \
unzip -o -q jbpm-server-dist.zip -d $JBOSS_HOME &&  \
rm -rf $HOME/jbpm-server-dist.zip

## Replace with our own .war ##
RUN rm $JBOSS_HOME/standalone/deployments/business-central.war
COPY --from=build /kie-wb-distributions/business-central-parent/business-central-webapp/target/business-central-webapp.war $JBOSS_HOME/standalone/deployments/business-central.war

## CONFIGURATION ##
# install keycloak-wildfly-adapter
RUN curl -L -o ${JBOSS_HOME}/keycloak-oidc-wildfly-adapter.zip https://github.com/keycloak/keycloak/releases/download/${KEYCLOAK_VERSION}/keycloak-oidc-wildfly-adapter-${KEYCLOAK_VERSION}.zip && \
    unzip -o -q ${JBOSS_HOME}/keycloak-oidc-wildfly-adapter.zip -d ${JBOSS_HOME} && \
    rm -rf ${JBOSS_HOME}/keycloak-oidc-wildfly-adapter.zip
RUN ${JBOSS_HOME}/bin/jboss-cli.sh --file=${JBOSS_HOME}/bin/adapter-elytron-install-offline.cli

# remove superfluous war
RUN rm $JBOSS_HOME/standalone/deployments/jbpm-casemgmt.war

# patch business-central war 
ADD web.xml $JBOSS_HOME/standalone/deployments/WEB-INF/web.xml
USER root
RUN chown -R jboss:jboss $JBOSS_HOME/standalone/deployments/WEB-INF
USER jboss
RUN jar ufv $JBOSS_HOME/standalone/deployments/business-central.war -C $JBOSS_HOME/standalone/deployments WEB-INF && \
    rm -rf $JBOSS_HOME/standalone/deployments/WEB-INF

# remove superfluous files
#RUN rm $JBOSS_HOME/standalone/configuration/users.properties && \
#    rm $JBOSS_HOME/standalone/configuration/roles.properties

# Add scrips and configuration
ADD start_jbpm-wb.sh $JBOSS_HOME/bin/start_jbpm-wb.sh
ADD jbpm-postgres-config.cli $JBOSS_HOME/bin/jbpm-postgres-config.cli
ADD update_config.sh $JBOSS_HOME/bin/update_config.sh
ADD update_db_config.sh $JBOSS_HOME/bin/update_db_config.sh
ADD standalone.xml $JBOSS_HOME/standalone/configuration/standalone.xml

USER root
RUN chown jboss:jboss $JBOSS_HOME/standalone/deployments/*
RUN chown jboss:jboss $JBOSS_HOME/bin/start_jbpm-wb.sh
RUN chown jboss:jboss $JBOSS_HOME/bin/update_config.sh
RUN chown jboss:jboss $JBOSS_HOME/bin/update_db_config.sh
RUN chown jboss:jboss $JBOSS_HOME/bin/jbpm-postgres-config.cli
RUN chmod a+x $JBOSS_HOME/bin/start_jbpm-wb.sh
RUN chmod a+x $JBOSS_HOME/bin/update_config.sh
RUN chmod a+x $JBOSS_HOME/bin/update_db_config.sh
RUN chmod a+x $JBOSS_HOME/bin/jbpm-postgres-config.cli

## CUSTOM JBOSS USER ##
# Switchback to jboss user
USER jboss

## EXPOSE INTERNAL JBPM GIT PORT ##
EXPOSE 8001

## RUNNING JBPM-WB ##
WORKDIR $JBOSS_HOME/bin/
CMD ["./start_jbpm-wb.sh"]