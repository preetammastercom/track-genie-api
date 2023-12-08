#!/bin/bash

# Get the directory of the current script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Default deployment path, port, and other settings
DEPLOY_PATH="$SCRIPT_DIR/../track-genie"
RUN_DOCKER=false
PORT=8080
CONFIG_FILE="logstash-pipeline.conf"

while [ "$1" != "" ]; do
    case $1 in
        -p )    shift
                DEPLOY_PATH=$1
                ;;
        -dc )   shift
                [ "$1" = "true" ] && RUN_DOCKER=true
                ;;
        --port ) shift
                PORT=$1
                ;;
    esac
    shift
done

# Navigate to Maven project path and run clean install
echo "INFO: Running mvn clean install..."
mvn clean install

# Check for process running on the specified port
PORT_PROCESS=$(ps aux | grep "$PORT" | grep -v 'grep' | awk '{print $2}')
if [ -n "$PORT_PROCESS" ]; then
    read -p "A process is already running on port $PORT. Would you like to kill it? (y/n): " RESP
    if [ "$RESP" = "y" ]; then
        echo "INFO: Killing process running on port $PORT..."
        kill $PORT_PROCESS
    else
        echo "INFO: Process still running on port $PORT. Exiting."
        exit 1
    fi
fi

# Check if the DEPLOY_PATH exists, if not, create it
[ ! -d "$DEPLOY_PATH" ] && mkdir -p "$DEPLOY_PATH"

# Deployment tasks for logs, sincedb, and config
[ ! -d "$DEPLOY_PATH/logs" ] && mkdir "$DEPLOY_PATH/logs" && chmod 777 "$DEPLOY_PATH/logs"
[ ! -d "$DEPLOY_PATH/sincedb" ] && mkdir "$DEPLOY_PATH/sincedb" && chmod 777 "$DEPLOY_PATH/sincedb"

if [ ! -f "$DEPLOY_PATH/$CONFIG_FILE" ]; then
    cp "$SCRIPT_DIR/$CONFIG_FILE" "$DEPLOY_PATH/"
elif [ "$SCRIPT_DIR/$CONFIG_FILE" -nt "$DEPLOY_PATH/$CONFIG_FILE" ]; then
    read -p "A newer version of $CONFIG_FILE exists. Overwrite? (y/n): " RESP
    if [ "$RESP" = "y" ]; then
        cp "$SCRIPT_DIR/$CONFIG_FILE" "$DEPLOY_PATH/"
    fi
fi

# Copy JAR file from target directory to DEPLOY_PATH
JAR_FILE=$(find target -name "*.jar")
if [ -n "$JAR_FILE" ]; then
    echo "INFO: Copying JAR file to deployment directory..."
    cp "$JAR_FILE" "$DEPLOY_PATH/"
else
    echo "ERROR: No JAR file found in target directory."
    exit 1
fi

# Navigate to deployment directory and run the JAR
cd "$DEPLOY_PATH"
echo "INFO: Starting the application on port $PORT..."
nohup java -jar "$(basename "$JAR_FILE")" --server.port=$PORT > app.log 2>&1 &

# Give the application some time to start
sleep 20

# Check if the application is running
if curl -s "http://localhost:$PORT" > /dev/null; then
    echo "INFO: Application is up and running on port $PORT!"
else
    echo "ERROR: Application did not start successfully on port $PORT. Check app.log for details."
    exit 1
fi


# Docker Compose check, manage, and run if -dc true was passed
if [ "$RUN_DOCKER" = true ]; then
    # Check and manage docker-compose file in the deployment directory
    DOCKER_COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

    echo "DEBUG: Checking for Docker Compose file Primarily at $DOCKER_COMPOSE_FILE"

echo "DEBUG: Current directory: $(pwd)"
echo "DEBUG: Script directory: $SCRIPT_DIR"
ls -l "$SCRIPT_DIR"
echo "DEBUG: Deployment directory: $DEPLOY_PATH"
ls -l "$DEPLOY_PATH"
echo "DEBUG: Checking for Docker Compose file Primarily at $DOCKER_COMPOSE_FILE"

    if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
        DOCKER_COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yaml"
    fi

    echo "DEBUG: Checking for Docker Compose file Secondarily at $DOCKER_COMPOSE_FILE"

    if [ -f "$DOCKER_COMPOSE_FILE" ]; then
        if [ ! -f "$DEPLOY_PATH/docker-compose.yml" ] && [ ! -f "$DEPLOY_PATH/docker-compose.yaml" ]; then
            cp "$DOCKER_COMPOSE_FILE" "$DEPLOY_PATH/"
        elif [ "$DOCKER_COMPOSE_FILE" -nt "$DEPLOY_PATH/docker-compose.yml" ] || [ "$DOCKER_COMPOSE_FILE" -nt "$DEPLOY_PATH/docker-compose.yaml" ]; then
            read -p "A newer version of docker-compose.yml exists. Overwrite? (y/n): " RESP
            if [ "$RESP" = "y" ]; then
                cp "$DOCKER_COMPOSE_FILE" "$DEPLOY_PATH/"
            fi
        fi
    elif [ ! -f "$DEPLOY_PATH/docker-compose.yml" ] && [ ! -f "$DEPLOY_PATH/docker-compose.yaml" ]; then
        echo "ERROR: No docker-compose.yml or docker-compose.yaml file found in either $SCRIPT_DIR or $DEPLOY_PATH."
        exit 1
    fi

    # Now, navigate to deployment directory and run docker-compose
    cd "$DEPLOY_PATH"
    if docker-compose ps | grep -q "Up"; then
        echo "INFO: Docker Compose services are already running."
    else
        echo "INFO: Running Docker Compose..."
        docker-compose up -d
    fi
fi
