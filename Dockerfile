####### BUILD ############
FROM maven:3.8.4-jdk-11 AS build

## ENVIRONMENT ##
ENV SMARTCLIDE_THEIA_URL 127.0.0.1:3030
ENV SMARTCLIDE_SERVICE_DISCOVERY_URL 127.0.0.1:2020

## Kie Common Stunner Build ##
COPY /kie-wb-common-stunner/kie-wb-common-stunner-sets/kie-wb-common-stunner-bpmn/kie-wb-common-stunner-bpmn-client common
RUN sed -i "s;SMARTCLIDE_THEIA_URL;$SMARTCLIDE_THEIA_URL;g" "/common/src/main/java/org/kie/workbench/common/stunner/bpmn/client/forms/fields/assignmentsEditor/ActivityDataIOEditorViewImpl.java"
RUN sed -i "s;SMARTCLIDE_SERVICE_DISCOVERY_URL;$SMARTCLIDE_SERVICE_DISCOVERY_URL;g" "/common/src/main/java/org/kie/workbench/common/stunner/bpmn/client/forms/fields/assignmentsEditor/ActivityDataIOEditorViewImpl.java"
RUN mvn -f common/pom.xml install -DskipTests

## Kie Distributions Build ##
RUN apt-get update && \
    apt-get install git
RUN git clone https://github.com/eclipse-researchlabs/kie-wb-distributions
RUN cd kie-wb-distributions && git checkout b1f555795a7219f5361aec098d914a5720a61164
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

## JBPM-WB ##
RUN curl -o $HOME/jbpm-server-dist.zip $KIE_REPOSITORY/$KIE_VERSION/jbpm-server-$KIE_VERSION-dist.zip && \
unzip -o -q jbpm-server-dist.zip -d $JBOSS_HOME &&  \
rm -rf $HOME/jbpm-server-dist.zip

## Replace with our own .war ##
RUN rm $JBOSS_HOME/standalone/deployments/business-central.war
COPY --from=build /kie-wb-distributions/business-central-parent/business-central-webapp/target/business-central-webapp.war $JBOSS_HOME/standalone/deployments/business-central.war

## CONFIGURATION ##
USER root
ADD start_jbpm-wb.sh $JBOSS_HOME/bin/start_jbpm-wb.sh
ADD update_db_config.sh $JBOSS_HOME/bin/update_db_config.sh
RUN chown jboss:jboss $JBOSS_HOME/standalone/deployments/*
RUN chown jboss:jboss $JBOSS_HOME/bin/start_jbpm-wb.sh
RUN chown jboss:jboss $JBOSS_HOME/bin/update_db_config.sh
RUN chmod a+x $JBOSS_HOME/bin/start_jbpm-wb.sh
RUN chmod a+x $JBOSS_HOME/bin/update_db_config.sh
#RUN sed -i '/<property name="org.kie.server.location" value="http:\/\/localhost:8080\/kie-server\/services\/rest\/server"\/>/d' $JBOSS_HOME/standalone/configuration/standalone.xml
#RUN sed -i '/<property name="org.kie.server.id" value="sample-server"\/>/d' $JBOSS_HOME/standalone/configuration/standalone.xml

## CUSTOM JBOSS USER ##
# Switchback to jboss user
USER jboss

## EXPOSE INTERNAL JBPM GIT PORT ##
EXPOSE 8001

## RUNNING JBPM-WB ##
WORKDIR $JBOSS_HOME/bin/
CMD ["./start_jbpm-wb.sh"]