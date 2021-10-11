## Local Setup

This Repository contains a sample docker-compose.yaml file which will stand up a fully functional ubirch avatar service including it's dependencies. 
In order to use this you have to export your AWS Access credentials as environment variables as described above (AWS Configuration). Once those are available you can start the containers with ($PWD should be root of the working copy):

		docker-compose up

This will start three container: ElasticSearch, Kibana, ubirch-avatar-service

If you haven not yet install docker-compose follow the instructions found here https://docs.docker.com/compose/install/.

## Create Docker Image

    ./goBuild assembly && ./goBuild containerbuild


## Generate Test Data

Running this removes all your local ElasticSearch indexes and recreates them!!

 1. start server, e.g. in a terminal

    1. set MQTT env vars:
    
        export MQTT_USER={MQTT-User}

        export MQTT_PASSWORD={MQTT-Password}

    2. if using a terminal, change inside the project folder and

        ./sbt server/run

 2. reset database

*Running `dev-scripts/resetDatabase.sh` does everything in this step.*

     ./sbt "cmdtools/runMain com.ubirch.avatar.cmd.ClearDb"

 3. start test data tool

    1. set MQTT env vars:

        ```bash
        export MQTT_USER={MQTT-User}
        export MQTT_PASSWORD={MQTT-Password}
        ```

    2. if using a terminal, change inside the project folder and

*Running `dev-scripts/initData.sh` does everything in this step.*

        ```bash
        ./sbt "cmdtools/runMain com.ubirch.avatar.cmd.InitData"
        ```

3. now you should find one device "testHans001" and 50 data points
