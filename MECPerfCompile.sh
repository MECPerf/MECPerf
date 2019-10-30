#!/bin/bash
          
PROJECT_ROOT_PATH=$(pwd)
COMMON_PATH=$PROJECT_ROOT_PATH/MECPerfNG/common/src/main/java/
OBSERVER_PATH=$PROJECT_ROOT_PATH/MECPerfNG/observer/src/main/java/
REMOTESERVER_PATH=$PROJECT_ROOT_PATH/MECPerfNG/remoteserver/src/main/java/
AGGREGATOR_PATH=$PROJECT_ROOT_PATH/MECPerfNG/aggregator/src/main/java/
APPCOMMANDLINE_PATH=$PROJECT_ROOT_PATH/MECPerfNG/app/src/main/java/

echo "start compiling..."


javac $COMMON_PATH/it/unipi/dii/common/Measure.java 
javac $COMMON_PATH/it/unipi/dii/common/Measurements.java
javac $COMMON_PATH/it/unipi/dii/common/ControlMessages.java
echo "      Common compiled"
javac -cp $COMMON_PATH $APPCOMMANDLINE_PATH/it/unipi/dii/mecperfng/MainUtils.java
echo "      MainUtils compiled"

javac -cp $COMMON_PATH $AGGREGATOR_PATH/it/unipi/dii/aggregator/Aggregator.java
echo "      Aggregator compiled"
javac -cp $COMMON_PATH  $OBSERVER_PATH/it/unipi/dii/observer/Observer.java
echo "      Observer compiled"
javac -cp $COMMON_PATH $REMOTESERVER_PATH/it/unipi/dii/remoteserver/Server.java
echo "      RemoteServer compiled"

javac -cp $COMMON_PATH:$APPCOMMANDLINE_PATH $APPCOMMANDLINE_PATH/it/unipi/dii/mecperfng/commandlineapp/CommandLineApp.java
echo "      App for linux compiled"

       
       
          
echo "creating jars..."
# c: reate jar
# v: verbose
# f: specify output jar name

cd $COMMON_PATH
jar cf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/common/ControlMessages.class it/unipi/dii/common/ControlMessages.java
jar uf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/common/Measurements.class it/unipi/dii/common/Measurements.java 
jar uf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/common/Measure.class it/unipi/dii/common/Measure.java 
cd $APPCOMMANDLINE_PATH
jar uf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/mecperfng/commandlineapp/CommandLineApp.class it/unipi/dii/mecperfng/commandlineapp/CommandLineApp.java 
jar uf $PROJECT_ROOT_PATH/CommandLineApp.jar it/unipi/dii/mecperfng/MainUtils.class it/unipi/dii/mecperfng/MainUtils.java
echo "      CommandLineApp.jar created"

jar umf MANIFEST.MF $PROJECT_ROOT_PATH/CommandLineApp.jar
echo "      CommandLineApp manifest file updated"



cd $COMMON_PATH
jar cf $PROJECT_ROOT_PATH/Observer.jar it/unipi/dii/common/Measurements.class it/unipi/dii/common/Measurements.java 
jar uf $PROJECT_ROOT_PATH/Observer.jar it/unipi/dii/common/Measure.class it/unipi/dii/common/Measure.java 
jar uf $PROJECT_ROOT_PATH/Observer.jar it/unipi/dii/common/ControlMessages.class it/unipi/dii/common/ControlMessages.java
cd $OBSERVER_PATH
jar uf $PROJECT_ROOT_PATH/Observer.jar it/unipi/dii/observer/Observer.class it/unipi/dii/observer/Observer.java 
echo "      Observer.jar created"
jar umf MANIFEST.MF $PROJECT_ROOT_PATH/Observer.jar
echo "      Observer manifest file updated"


cd $COMMON_PATH
jar cf $PROJECT_ROOT_PATH/Server.jar it/unipi/dii/common/Measurements.class it/unipi/dii/common/Measurements.java 
jar uf $PROJECT_ROOT_PATH/Server.jar it/unipi/dii/common/Measure.class it/unipi/dii/common/Measure.java
cd $REMOTESERVER_PATH
jar uf  $PROJECT_ROOT_PATH/Server.jar it/unipi/dii/remoteserver/Server.class it/unipi/dii/remoteserver/Server.java
echo "      Server.jar done"
jar umf MANIFEST.MF $PROJECT_ROOT_PATH/Server.jar
echo "      Server manifest file updated"


cd $COMMON_PATH
jar cf $PROJECT_ROOT_PATH/Aggregator.jar it/unipi/dii/common/Measurements.class it/unipi/dii/common/Measurements.java 
jar uf $PROJECT_ROOT_PATH/Aggregator.jar it/unipi/dii/common/Measure.class it/unipi/dii/common/Measure.java 
jar uf $PROJECT_ROOT_PATH/Aggregator.jar it/unipi/dii/common/MeasureResult.class it/unipi/dii/common/MeasureResult.java 
cd $AGGREGATOR_PATH
jar uf $PROJECT_ROOT_PATH/Aggregator.jar it/unipi/dii/aggregator/Aggregator.class it/unipi/dii/aggregator/Aggregator.java
echo "      Aggregator.jar done"
jar umf MANIFEST.MF $PROJECT_ROOT_PATH/Aggregator.jar
echo "      Aggregator manifest file updated"
          