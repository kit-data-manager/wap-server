#!/bin/bash

# Check no of parameters.
if [ "$#" -ne 1 ]; then
  echo "Illegal number of parameters!"
  usage
fi

INSTALLATION_DIRECTORY=$1
ACTUAL_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Check if directory exists
  if [ ! -d "$INSTALLATION_DIRECTORY" ]; then
    # Create directory if it doesn't exists.
    mkdir -p "$INSTALLATION_DIRECTORY"
    if [ $? -ne 0 ]; then
      echo "Error creating directory '$INSTALLATION_DIRECTORY'!"
      echo "Please make sure that you have the correct access permissions for the specified directory."
      exit 1
    fi
  fi
  # Check if directory is empty
  if [ ! -z "$(ls -A "$INSTALLATION_DIRECTORY")" ]; then
     echo "Directory '$INSTALLATION_DIRECTORY' is not empty!"
     echo "Please enter an empty or a new directory!"
     exit 1
  fi
  # Convert variable of installation directory to an absolute path
    cd "$INSTALLATION_DIRECTORY"
    INSTALLATION_DIRECTORY=`pwd`
    cd "$ACTUAL_DIR"

APP_NAME=`./gradlew -q printProjectName`
APP_NAME=${APP_NAME##*$'\n'}
echo "$APP_NAME"

echo Build service...
./gradlew -Dprofile=minimal clean build

echo "Copy jar file to '$INSTALLATION_DIRECTORY'..."
find . -name "$APP_NAME.jar" -exec cp '{}' "$INSTALLATION_DIRECTORY" \;

# Create run script
echo "$INSTALLATION_DIRECTORY"
cd "$INSTALLATION_DIRECTORY"

# Determine name of jar file.
jarFile=(`ls $APP_NAME.jar`)

java -jar $jarFile "--create-config"

echo '#!/bin/sh' >> run.sh
echo 'pwd' >> run.sh
echo 'ls' >> run.sh
echo 'cat run.sh' >> run.sh
echo "################################################################################"         >> run.sh
echo "# Define jar file"                                                                        >> run.sh
echo "################################################################################"         >> run.sh
echo jarFile=$jarFile                                                                           >> run.sh
echo " "                                                                                        >> run.sh
echo "################################################################################"         >> run.sh
echo "# Start micro service"                                                                    >> run.sh
echo "################################################################################"         >> run.sh
echo 'java -jar $jarFile' >> run.sh

# make script executable
chmod 755 run.sh